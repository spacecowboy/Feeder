package com.nononsenseapps.feeder.model

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextLanguage
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.nononsenseapps.feeder.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import java.util.UUID

@Immutable
data class ReadAloudProgress(
    val articleId: Long? = null,
    val text: String = "",
    val positionMillis: Long = 0,
    val durationMillis: Long = 0,
    val preparationProgress: Float? = null,
    val segment: SpeechSegment? = null,
    val error: String? = null,
)

/** Application facade; the foreground service owns the player and its lifetime. */
class TTSStateHolder(
    val context: Context,
    val coroutineScope: CoroutineScope,
) {
    private val _ttsState = MutableStateFlow(PlaybackStatus.STOPPED)
    val ttsState = _ttsState.asStateFlow()
    private val _progress = MutableStateFlow(ReadAloudProgress())
    val progress = _progress.asStateFlow()
    private val _language = MutableStateFlow<LocaleOverride>(AppSetting)
    val language: StateFlow<LocaleOverride> = _language.asStateFlow()
    private val _availableLanguages = MutableStateFlow<List<Locale>>(emptyList())
    val availableLanguages = _availableLanguages.asStateFlow()
    private var request: SpeechRequest? = null
    private var player: ExoPlayer? = null
    private var preparation: Job? = null
    private var ticker: Job? = null
    private var segments = emptyList<SpeechSegment>()
    private var playWhenReady = true
    private var cacheDirectory: File? = null
    private var service: ReadAloudService? = null
    private var pendingServiceStart = false
    private val artwork by lazy {
        runCatching {
            val size = 256
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(Canvas(bitmap))
            ByteArrayOutputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                output.toByteArray()
            }
        }.getOrNull()
    }

    fun tts(
        textArray: List<AnnotatedString>,
        useDetectLanguage: Boolean,
        articleId: Long,
        title: String = "",
        author: String = "",
        feedTitle: String = "",
    ) {
        val text = textArray.filterNot { it.isBlank() }.joinToString("\n\n") { it.text }
        if (text.isBlank()) return
        coroutineScope.launch(Dispatchers.Main.immediate) {
            request = SpeechRequest(articleId, text, useDetectLanguage, title, author, feedTitle)
            playWhenReady = true
            _ttsState.value = PlaybackStatus.PLAYING
            _progress.value = ReadAloudProgress(articleId = articleId, text = text, preparationProgress = 0f)
            try {
                pendingServiceStart = true
                ContextCompat.startForegroundService(context, Intent(context, ReadAloudService::class.java).setAction(ReadAloudService.ACTION_PREPARE))
            } catch (e: Exception) {
                pendingServiceStart = false
                fail(e)
            }
        }
    }

    internal fun attach(
        service: ReadAloudService,
        player: ExoPlayer,
    ) {
        this.service = service
        this.player = player
        player.addListener(
            object : Player.Listener {
                override fun onPlayWhenReadyChanged(
                    playWhenReady: Boolean,
                    reason: Int,
                ) {
                    if (_progress.value.preparationProgress != null && request != null) {
                        this@TTSStateHolder.playWhenReady = playWhenReady
                        _ttsState.value = if (playWhenReady) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
                    }
                }

                override fun onEvents(
                    player: Player,
                    events: Player.Events,
                ) {
                    if (_progress.value.preparationProgress == null && _progress.value.error == null && request != null) {
                        _ttsState.value =
                            if (player.playWhenReady && player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
                        updatePosition()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    fail(error)
                }
            },
        )
        ticker =
            coroutineScope.launch(Dispatchers.Main.immediate) {
                while (isActive) {
                    updatePosition()
                    delay(100)
                }
            }
    }

    internal fun prepare() {
        pendingServiceStart = false
        val current =
            request ?: run {
                service?.stopSelf()
                return
            }
        val previous = preparation
        previous?.cancel()
        service?.setArticle(current.articleId)
        player?.stop()
        player?.clearMediaItems()
        player?.playWhenReady = playWhenReady
        segments = emptyList()
        preparation =
            coroutineScope.launch(Dispatchers.Main.immediate) {
                try {
                    previous?.join()
                    // Each job owns a separate engine and directory so stale callbacks cannot
                    // corrupt a replacement article or language-change request.
                    val directory =
                        withContext(Dispatchers.IO) {
                            File(context.cacheDir, "read-aloud").apply { mkdirs() }.let { root ->
                                root.listFiles()?.forEach { it.deleteRecursively() }
                                File(root, UUID.randomUUID().toString()).apply { mkdirs() }
                            }
                        }
                    cacheDirectory = directory
                    val audio = File(directory, "article.wav")
                    val timeline = withTimeout(5 * 60_000L) { synthesize(current, directory, audio) }
                    if (request !== current) return@launch
                    segments = timeline
                    val duration = timeline.lastOrNull()?.endMillis ?: error("No speech audio")
                    _progress.value = ReadAloudProgress(current.articleId, current.text, durationMillis = duration)
                    player?.apply {
                        setMediaItem(
                            MediaItem
                                .Builder()
                                .setMediaId(current.articleId.toString())
                                .setUri(android.net.Uri.fromFile(audio))
                                .setMediaMetadata(
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(current.title.ifBlank { context.getString(R.string.read_article) })
                                        .setArtist(current.author.ifBlank { current.feedTitle }.ifBlank { context.getString(R.string.app_name) })
                                        .setArtworkData(artwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                                        .build(),
                                ).build(),
                        )
                        prepare()
                        playWhenReady = this@TTSStateHolder.playWhenReady
                    }
                } catch (e: TimeoutCancellationException) {
                    fail(e)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    fail(e)
                }
            }
    }

    private suspend fun synthesize(
        current: SpeechRequest,
        directory: File,
        audio: File,
    ): List<SpeechSegment> {
        val initialized = CompletableDeferred<Int>()
        val engine = TextToSpeech(context) { initialized.complete(it) }
        try {
            check(withTimeout(15_000) { initialized.await() } == TextToSpeech.SUCCESS) { "Speech engine initialization failed" }
            val available = engine.availableLanguages.orEmpty().sortedBy { it.getDisplayName(it) }
            _availableLanguages.value = available
            val preferredLocale =
                when (val selected = _language.value) {
                    is ForcedLocale -> selected.locale
                    else ->
                        if (selected is ForcedAuto || current.detectLanguage) {
                            withContext(Dispatchers.Default) { context.detectLocaleFromText(current.text.take(4000)).firstOrNull()?.locale }
                                ?: context.getLocales().firstOrNull() ?: Locale.getDefault()
                        } else {
                            context.getLocales().firstOrNull() ?: Locale.getDefault()
                        }
                }
            val candidates =
                if (_language.value is ForcedLocale) {
                    sequenceOf(preferredLocale)
                } else {
                    sequenceOf(preferredLocale).plus(context.getLocales()).plus(available)
                }
            val locale =
                candidates.firstOrNull { engine.setLanguage(it) >= TextToSpeech.LANG_AVAILABLE }
                    ?: error("Speech language is not available")
            val parts = speechSegments(current.text, locale, minOf(TextToSpeech.getMaxSpeechInputLength(), 700))
            return withContext(Dispatchers.IO) {
                SpeechWaveFile(audio).use { wave ->
                    val timeline = mutableListOf<SpeechSegment>()
                    for ((index, part) in parts.withIndex()) {
                        val done = CompletableDeferred<Unit>()
                        val id = UUID.randomUUID().toString()
                        engine.setOnUtteranceProgressListener(
                            object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) = Unit

                                override fun onDone(utteranceId: String?) {
                                    if (utteranceId == id) done.complete(Unit)
                                }

                                @Deprecated("Platform compatibility")
                                override fun onError(utteranceId: String?) {
                                    if (utteranceId == id) done.completeExceptionally(IllegalStateException("Speech synthesis failed"))
                                }

                                override fun onBeginSynthesis(
                                    utteranceId: String?,
                                    sampleRateInHz: Int,
                                    audioFormat: Int,
                                    channelCount: Int,
                                ) {
                                    if (utteranceId != id || !done.isActive) return
                                    try {
                                        val bits =
                                            when (audioFormat) {
                                                AudioFormat.ENCODING_PCM_8BIT -> 8
                                                AudioFormat.ENCODING_PCM_16BIT -> 16
                                                AudioFormat.ENCODING_PCM_FLOAT -> 32
                                                else -> error("Unsupported speech audio format")
                                            }
                                        wave.setFormat(sampleRateInHz, channelCount, bits, audioFormat == AudioFormat.ENCODING_PCM_FLOAT)
                                    } catch (e: Exception) {
                                        done.completeExceptionally(e)
                                    }
                                }

                                override fun onAudioAvailable(
                                    utteranceId: String?,
                                    bytes: ByteArray?,
                                ) {
                                    if (utteranceId != id || !done.isActive || bytes == null) return
                                    try {
                                        wave.append(bytes)
                                    } catch (e: Exception) {
                                        done.completeExceptionally(e)
                                    }
                                }
                            },
                        )
                        val start = wave.durationMillis()
                        val temporary = File(directory, "segment.wav")
                        try {
                            check(engine.synthesizeToFile(current.text.substring(part.start, part.end), Bundle(), temporary, id) == TextToSpeech.SUCCESS)
                            withTimeout(45_000) { done.await() }
                        } finally {
                            done.cancel()
                            temporary.delete()
                        }
                        val end = wave.durationMillis()
                        check(end > start) { "Speech engine produced no audio" }
                        timeline += part.copy(startMillis = start, endMillis = end)
                        withContext(Dispatchers.Main.immediate) {
                            if (request === current) _progress.value = _progress.value.copy(preparationProgress = (index + 1f) / parts.size)
                        }
                    }
                    wave.finish()
                    timeline
                }
            }
        } finally {
            engine.stop()
            engine.shutdown()
        }
    }

    fun resumeIfPausedOn(articleId: Long): Boolean {
        if (_ttsState.value != PlaybackStatus.PAUSED || request?.articleId != articleId) return false
        play()
        return true
    }

    fun play() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            playWhenReady = true
            if (_progress.value.error != null) {
                request?.let { tts(listOf(AnnotatedString(it.text)), it.detectLanguage, it.articleId, it.title, it.author, it.feedTitle) }
            } else {
                player?.let {
                    if (it.playbackState == Player.STATE_ENDED) it.seekTo(0)
                    if (it.playbackState == Player.STATE_IDLE && it.mediaItemCount > 0) it.prepare()
                    it.play()
                }
                if (request != null) _ttsState.value = PlaybackStatus.PLAYING
            }
        }
    }

    fun pause() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            playWhenReady = false
            player?.pause()
            if (request != null) _ttsState.value = PlaybackStatus.PAUSED
        }
    }

    fun seekTo(positionMillis: Long) {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            if (_progress.value.preparationProgress == null) {
                player?.seekTo(positionMillis.coerceIn(0, _progress.value.durationMillis))
                updatePosition()
            }
        }
    }

    fun skipNext() {
        val position = _progress.value.positionMillis
        seekTo(segments.firstOrNull { it.startMillis > position + 50 }?.startMillis ?: _progress.value.durationMillis)
    }

    fun setLanguage(lang: LocaleOverride) {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            _language.value = lang
            request?.let { tts(listOf(AnnotatedString(it.text)), it.detectLanguage, it.articleId, it.title, it.author, it.feedTitle) }
        }
    }

    fun stop() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            val pending = preparation
            pending?.cancel()
            pendingServiceStart = false
            request = null
            player?.stop()
            player?.clearMediaItems()
            _ttsState.value = PlaybackStatus.STOPPED
            _progress.value = ReadAloudProgress()
            segments = emptyList()
            val directory = cacheDirectory
            cacheDirectory = null
            coroutineScope.launch(Dispatchers.IO) {
                pending?.join()
                directory?.deleteRecursively()
                withContext(Dispatchers.Main.immediate) {
                    if (preparation === pending) preparation = null
                }
            }
            service?.stopForeground(android.app.Service.STOP_FOREGROUND_REMOVE)
            service?.stopSelf()
        }
    }

    private fun updatePosition() {
        val player = player ?: return
        if (_progress.value.preparationProgress != null || _progress.value.error != null || request == null) return
        val position = player.currentPosition.coerceAtLeast(0)
        _progress.value = _progress.value.copy(positionMillis = position, segment = segments.segmentAt(position))
    }

    private fun fail(error: Exception) {
        Log.e(LOG_TAG, "Read-aloud failed", error)
        player?.pause()
        _ttsState.value = PlaybackStatus.PAUSED
        _progress.value = _progress.value.copy(preparationProgress = null, error = context.getString(R.string.audio_playback_failed), segment = null)
        service?.stopForeground(android.app.Service.STOP_FOREGROUND_REMOVE)
    }

    internal fun detach() {
        ticker?.cancel()
        preparation?.cancel()
        player = null
        service = null
        segments = emptyList()
        // A new user request may already be starting a replacement service instance.
        if (!pendingServiceStart) {
            request = null
            _ttsState.value = PlaybackStatus.STOPPED
            _progress.value = ReadAloudProgress()
        }
        val directory = cacheDirectory
        cacheDirectory = null
        coroutineScope.launch(Dispatchers.IO) { directory?.deleteRecursively() }
    }

    fun shutdown() = stop()

    private data class SpeechRequest(
        val articleId: Long,
        val text: String,
        val detectLanguage: Boolean,
        val title: String,
        val author: String,
        val feedTitle: String,
    )

    companion object {
        private const val LOG_TAG = "FEEDER_READ_ALOUD"
    }
}

fun Context.getLocales(): Sequence<Locale> =
    sequence {
        val locales = resources.configuration.locales

        for (i in 0 until locales.size()) {
            yield(locales[i])
        }
    }

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.detectLocaleFromText(
    text: CharSequence,
    minConfidence: Float = 80.0f,
): Sequence<LocaleWithConfidence> {
    val textClassificationManager = getSystemService(TextClassificationManager::class.java)
    val textClassifier = textClassificationManager.textClassifier

    val textRequest = TextLanguage.Request.Builder(text).build()
    val detectedLanguage = textClassifier.detectLanguage(textRequest)

    return sequence {
        for (i in 0 until detectedLanguage.localeHypothesisCount) {
            val localeDetected = detectedLanguage.getLocale(i)
            val confidence = detectedLanguage.getConfidenceScore(localeDetected) * 100.0f
            if (confidence >= minConfidence) {
                yield(
                    LocaleWithConfidence(
                        locale = localeDetected.toLocale(),
                        confidence = confidence,
                    ),
                )
            }
        }
    }
}

data class LocaleWithConfidence(
    val locale: Locale,
    val confidence: Float,
)

enum class PlaybackStatus {
    STOPPED,
    PAUSED,
    PLAYING,
}

@Immutable
sealed class LocaleOverride

@Immutable
object AppSetting : LocaleOverride()

@Immutable
object ForcedAuto : LocaleOverride()

@Immutable
data class ForcedLocale(
    val locale: Locale,
) : LocaleOverride()
