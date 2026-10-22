package com.nononsenseapps.feeder.model

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorInput
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.SeekMap
import androidx.media3.extractor.SeekPoint
import androidx.media3.extractor.TrackOutput
import java.io.File
import java.io.IOException
import java.io.InterruptedIOException
import java.io.RandomAccessFile

internal data class SpeechPcmFormat(
    val rate: Int,
    val channels: Int,
    val bits: Int,
    val floatingPoint: Boolean,
) {
    init {
        require(rate > 0 && channels in 1..2)
        require(if (floatingPoint) bits == 32 else bits == 8 || bits == 16)
    }

    val frameSize: Int = channels * bits / 8
    val bytesPerSecond: Long = rate.toLong() * frameSize

    fun timeUs(bytes: Long): Long = bytes * 1_000_000 / bytesPerSecond
}

/** Only complete utterances are published. Readers wait at the synthesis frontier, not EOF. */
internal class SpeechAudioBuffer(
    val file: File,
    val format: SpeechPcmFormat,
) {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val signal = Object()
    private val completionListeners = mutableSetOf<() -> Unit>()

    @Volatile var publishedBytes: Long = 0
        private set

    @Volatile var complete: Boolean = false
        private set

    @Volatile private var failure: IOException? = null

    fun publish(
        bytes: Long,
        finished: Boolean = false,
    ) {
        val listeners =
            synchronized(signal) {
                failure?.let { throw it }
                check(!complete) { "Speech audio was already completed" }
                require(bytes >= publishedBytes && bytes % format.frameSize == 0L)
                publishedBytes = bytes
                complete = finished
                signal.notifyAll()
                if (finished) completionListeners.toList() else emptyList()
            }
        listeners.forEach { it() }
    }

    fun addCompletionListener(listener: () -> Unit) {
        val completed =
            synchronized(signal) {
                completionListeners.add(listener)
                complete
            }
        if (completed) listener()
    }

    fun removeCompletionListener(listener: () -> Unit) {
        synchronized(signal) { completionListeners.remove(listener) }
    }

    fun abort() {
        synchronized(signal) {
            failure = IOException("Speech generation stopped")
            signal.notifyAll()
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun available(position: Long): Int {
        synchronized(signal) {
            while (true) {
                failure?.let { throw it }
                val available = publishedBytes - position
                if (available > 0) return minOf(available, Int.MAX_VALUE.toLong()).toInt()
                if (complete) return C.RESULT_END_OF_INPUT
                try {
                    signal.wait()
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw InterruptedIOException("Speech read cancelled")
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal fun speechMediaSource(
    buffer: SpeechAudioBuffer,
    item: MediaItem,
): ProgressiveMediaSource =
    ProgressiveMediaSource
        .Factory(
            { SpeechDataSource(buffer) },
            ExtractorsFactory { arrayOf(SpeechPcmExtractor(buffer)) },
        ).createMediaSource(item)

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private class SpeechDataSource(
    private val buffer: SpeechAudioBuffer,
) : BaseDataSource(false) {
    private var file: RandomAccessFile? = null
    private var uri: Uri? = null
    private var position = 0L

    override fun open(dataSpec: DataSpec): Long {
        transferInitializing(dataSpec)
        uri = dataSpec.uri
        position = dataSpec.position
        file = RandomAccessFile(buffer.file, "r").apply { seek(44 + position) }
        transferStarted(dataSpec)
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(
        target: ByteArray,
        offset: Int,
        length: Int,
    ): Int {
        if (length == 0) return 0
        val available = buffer.available(position)
        if (available == C.RESULT_END_OF_INPUT) return available
        val count = minOf(length, available)
        checkNotNull(file).readFully(target, offset, count)
        position += count
        bytesTransferred(count)
        return count
    }

    override fun getUri(): Uri? = uri

    override fun close() {
        file?.close()
        if (file != null) transferEnded()
        file = null
        uri = null
    }
}

/** Raw PCM has exact byte/time mapping, including seeks within the generated prefix. */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private class SpeechPcmExtractor(
    private val buffer: SpeechAudioBuffer,
) : Extractor {
    private lateinit var output: ExtractorOutput
    private lateinit var track: TrackOutput
    private val bytes = ByteArray(32 * 1024)

    @Volatile private var finalMapSent = false
    private val completionListener: () -> Unit = { publishSeekMap() }

    override fun sniff(input: ExtractorInput): Boolean = true

    override fun init(output: ExtractorOutput) {
        this.output = output
        track = output.track(0, C.TRACK_TYPE_AUDIO)
        val pcm = buffer.format
        track.format(
            Format
                .Builder()
                .setSampleMimeType(MimeTypes.AUDIO_RAW)
                .setSampleRate(pcm.rate)
                .setChannelCount(pcm.channels)
                .setPcmEncoding(
                    if (pcm.floatingPoint) {
                        C.ENCODING_PCM_FLOAT
                    } else if (pcm.bits == 8) {
                        C.ENCODING_PCM_8BIT
                    } else {
                        C.ENCODING_PCM_16BIT
                    },
                ).build(),
        )
        output.endTracks()
        publishSeekMap()
        // ProgressiveMediaSource posts seek-map updates onto its playback thread.
        buffer.addCompletionListener(completionListener)
    }

    private fun publishSeekMap() {
        val pcm = buffer.format
        val duration = if (buffer.complete) pcm.timeUs(buffer.publishedBytes) else C.TIME_UNSET
        output.seekMap(
            object : SeekMap {
                override fun isSeekable(): Boolean = true

                override fun getDurationUs(): Long = duration

                override fun getSeekPoints(timeUs: Long): SeekMap.SeekPoints {
                    val frames =
                        (timeUs.coerceAtLeast(0) * pcm.rate / 1_000_000)
                            .coerceAtMost((buffer.publishedBytes / pcm.frameSize - 1).coerceAtLeast(0))
                    val position = frames * pcm.frameSize
                    return SeekMap.SeekPoints(SeekPoint(pcm.timeUs(position), position))
                }
            },
        )
        finalMapSent = buffer.complete
    }

    override fun read(
        input: ExtractorInput,
        seekPosition: PositionHolder,
    ): Int {
        val position = input.position
        val available = buffer.available(position)
        if (buffer.complete && !finalMapSent) publishSeekMap()
        if (available == C.RESULT_END_OF_INPUT) return Extractor.RESULT_END_OF_INPUT
        val size = minOf(bytes.size, available).let { it - it % buffer.format.frameSize }
        input.readFully(bytes, 0, size)
        track.sampleData(ParsableByteArray(bytes), size)
        track.sampleMetadata(buffer.format.timeUs(position), C.BUFFER_FLAG_KEY_FRAME, size, 0, null)
        return Extractor.RESULT_CONTINUE
    }

    override fun seek(
        position: Long,
        timeUs: Long,
    ) = Unit

    override fun release() {
        buffer.removeCompletionListener(completionListener)
    }
}
