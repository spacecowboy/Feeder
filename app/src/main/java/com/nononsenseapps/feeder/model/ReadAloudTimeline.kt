package com.nononsenseapps.feeder.model

import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile
import java.text.BreakIterator
import java.util.Locale

data class SpeechSegment(
    val start: Int,
    val end: Int,
    val startMillis: Long = 0,
    val endMillis: Long = 0,
)

/** Sentence boundaries also provide a reliable highlight when the engine has no word timing. */
internal fun speechSegments(
    text: String,
    locale: Locale,
    maxLength: Int,
): List<SpeechSegment> {
    require(maxLength >= 2)
    val iterator = BreakIterator.getSentenceInstance(locale).apply { setText(text) }
    return buildList {
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            var cursor = start
            while (cursor < end) {
                var limit = minOf(cursor + maxLength, end)
                if (limit < end) {
                    val space = text.lastIndexOf(' ', limit - 1)
                    if (space > cursor) limit = space + 1
                    if (limit > cursor && Character.isHighSurrogate(text[limit - 1])) limit--
                }
                if (text.substring(cursor, limit).isNotBlank()) add(SpeechSegment(cursor, limit))
                cursor = limit
            }
            start = end
            end = iterator.next()
        }
    }
}

internal fun List<SpeechSegment>.segmentAt(positionMillis: Long): SpeechSegment? = lastOrNull { positionMillis >= it.startMillis }?.takeIf { positionMillis < it.endMillis }

/** Streams engine PCM to disk; never retains an article's audio in memory. */
internal class SpeechWaveFile(
    file: File,
) : Closeable {
    private val output =
        RandomAccessFile(file, "rw").apply {
            setLength(0)
            write(ByteArray(44))
        }
    private var sampleRate = 0
    private var channels = 0
    private var bits = 0
    private var format = 0
    private var dataSize = 0L

    @Synchronized
    fun setFormat(
        rate: Int,
        channelCount: Int,
        bitsPerSample: Int,
        floatingPoint: Boolean,
    ) {
        require(rate > 0 && channelCount in 1..2 && bitsPerSample in setOf(8, 16, 32))
        val newFormat = if (floatingPoint) 3 else 1
        check(sampleRate == 0 || (sampleRate == rate && channels == channelCount && bits == bitsPerSample && format == newFormat)) {
            "Speech engine changed audio format within an article"
        }
        sampleRate = rate
        channels = channelCount
        bits = bitsPerSample
        format = newFormat
    }

    @Synchronized
    fun append(bytes: ByteArray) {
        check(sampleRate > 0)
        check(dataSize + bytes.size <= 256L * 1024 * 1024) { "Read-aloud audio exceeded cache limit" }
        output.write(bytes)
        dataSize += bytes.size
    }

    @Synchronized
    fun durationMillis(): Long = if (sampleRate == 0) 0 else dataSize * 1000 / (sampleRate * channels * (bits / 8))

    @Synchronized
    fun finish() {
        check(dataSize > 0)
        val padding = (dataSize % 2).toInt()
        if (padding != 0) output.write(0)
        output.seek(0)
        output.writeBytes("RIFF")
        writeInt((dataSize + padding + 36).toInt())
        output.writeBytes("WAVEfmt ")
        writeInt(16)
        writeShort(format)
        writeShort(channels)
        writeInt(sampleRate)
        writeInt(sampleRate * channels * bits / 8)
        writeShort(channels * bits / 8)
        writeShort(bits)
        output.writeBytes("data")
        writeInt(dataSize.toInt())
    }

    private fun writeInt(value: Int) = output.writeInt(Integer.reverseBytes(value))

    private fun writeShort(value: Int) =
        output.writeShort(
            java.lang.Short
                .reverseBytes(value.toShort())
                .toInt(),
        )

    @Synchronized
    override fun close() = output.close()
}
