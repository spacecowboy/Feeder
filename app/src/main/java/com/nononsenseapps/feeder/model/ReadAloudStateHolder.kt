package com.nononsenseapps.feeder.model

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextLanguage
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.AnnotatedString
import com.nononsenseapps.feeder.R
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Any callers must call #shutdown when shutting down
 */
class ReadAloudStateHolder(
    val context: Context,
    val coroutineScope: CoroutineScope,
) : TextToSpeech.OnInitListener {
    private val mutex: Mutex = Mutex()
    private var textToSpeech: TextToSpeech? = null
    private val speechListener: UtteranceProgressListener by lazy {
        object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                textToSpeechQueue.removeFirstOrNull()
                if (textToSpeechQueue.isEmpty()) {
                    coroutineScope.launch {
                        delay(100)
                        if (textToSpeechQueue.isEmpty()) {
                            // If still empty after the delay
                            _readAloudState.value = PlaybackStatus.STOPPED
                        }
                    }
                }
            }

            override fun onStart(utteranceId: String) {
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(LOG_TAG, "onError utteranceId $utteranceId, errorCode $errorCode")

                if (utteranceId != null) {
                    textToSpeechQueue.removeFirstOrNull()
                }
            }

            @Deprecated(
                "Deprecated in super class",
                replaceWith = ReplaceWith("onError(utteranceId, errorCode)")
            )
            override fun onError(utteranceId: String) {
                Log.e(LOG_TAG, "onError utteranceId $utteranceId")
                textToSpeechQueue.removeFirstOrNull()
            }
        }
    }
    private val textToSpeechQueue = mutableListOf<CharSequence>()
    private var initializedState: Int? = null
    private var startJob: Job? = null
    private var localesToUse: Sequence<Locale> = emptySequence()

    private val _readAloudState = MutableStateFlow(PlaybackStatus.STOPPED)
    val readAloudState: StateFlow<PlaybackStatus> = _readAloudState.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    fun readAloud(title: String, textArray: List<AnnotatedString>, useDetectLanguage: Boolean) {
//        val textArray = fullText.split(*PUNCTUATION)
        for (text in textArray) {
            if (text.isBlank()) {
                continue
            }
            textToSpeechQueue.add(text)
        }
        _title.value = title
        localesToUse = if (useDetectLanguage && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.detectLocaleFromText(textArray.joinToString("\n\n")) + context.getLocales()
        } else {
            context.getLocales()
        }
        play()
    }

    fun play() {
        startJob?.cancel()
        startJob = coroutineScope.launch {
            if (mutex.isLocked) {
                // Oops, I was double clicked
                return@launch
            }
            mutex.withLock {
                if (textToSpeech == null) {
                    initializedState = null
                    textToSpeech = TextToSpeech(
                        context,
                        this@ReadAloudStateHolder
                    )
                }
                while (initializedState == null) {
                    delay(100)
                }
                if (initializedState != TextToSpeech.SUCCESS) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            R.string.failed_to_load_text_to_speech,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    return@launch
                }
                _readAloudState.value = PlaybackStatus.PLAYING

                // Can only set this once engine has been initialized
                textToSpeech?.setOnUtteranceProgressListener(speechListener)
                try {
                    textToSpeechQueue.forEachIndexed { index, text ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech?.speak(
                                text,
                                TextToSpeech.QUEUE_ADD,
                                null,
                                index.toString()
                            )
                        } else {
                            textToSpeech?.speak(
                                text,
                                TextToSpeech.QUEUE_ADD,
                                Bundle.EMPTY,
                                index.toString()
                            )
                        }
                    }
                } catch (e: ConcurrentModificationException) {
                    Log.e(LOG_TAG, "User probably double clicked play", e)
                    // State will be weird. But mutex should prevent it happening
                }
            }
        }
    }

    fun pause() {
        startJob?.cancel()
        textToSpeech?.stop()
        _readAloudState.value = PlaybackStatus.PAUSED
    }

    fun stop() {
        startJob?.cancel()
        textToSpeech?.stop()
        textToSpeechQueue.clear()
        _readAloudState.value = PlaybackStatus.STOPPED
        localesToUse = emptySequence()
        textToSpeech = null
    }

    fun skipNext() {
        coroutineScope.launch {
            startJob?.cancel()
            textToSpeech?.stop()
            startJob?.join()
            textToSpeechQueue.removeFirstOrNull()
            when (textToSpeechQueue.isEmpty()) {
                true -> stop()
                false -> play()
            }
        }
    }

    override fun onInit(status: Int) {
        initializedState = status

        if (status == TextToSpeech.SUCCESS) {
            val selectedLocale =
                localesToUse
                    .firstOrNull { locale ->
                        when (textToSpeech?.setLanguage(locale)) {
                            TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> {
                                Log.e(LOG_TAG, "${locale.displayLanguage} is not supported!")
                                false
                            }
                            else -> {
                                true
                            }
                        }
                    }

            if (selectedLocale == null) {
                Log.e(LOG_TAG, "None of the user's locales was supported by text to speech")
            }
        } else {
            Log.e(LOG_TAG, "Failed to load TextToSpeech object: $status")
        }
    }

    fun shutdown() {
        textToSpeech?.shutdown()
    }

    companion object {
        private const val LOG_TAG = "FeederTextToSpeech"
        private val PUNCTUATION = arrayOf(
            // New-lines
            "\n",
            // Very useful: https://unicodelookup.com/
            // Full stop
            ".",
            "։",
            "۔",
            "܁",
            "܂",
            "。",
            "︒",
            "﹒",
            "．",
            "｡",
            // Question mark
            "?",
            ";",
            "՞",
            "؟",
            "⁇",
            "⁈",
            "⁉",
            "︖",
            "﹖",
            "？",
            // Exclamation mark
            "!",
            "՜",
            "‼",
            "︕",
            "﹗",
            "！",
            // Colon and semi-colon
            ":",
            ";",
            "؛",
            "︓",
            "︔",
            "﹔",
            "﹕",
            "：",
            "；",
            // Ellipsis
            "...",
            "…",
            "⋯",
            "⋮",
            "︙",
            // Dash
            "—",
            "〜",
            "〰",
        )
    }
}

fun Context.getLocales(): Sequence<Locale> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        sequence {
            val locales = resources.configuration.locales

            for (i in 0..locales.size()) {
                yield(locales[i])
            }
        }
    } else {
        @Suppress("DEPRECATION")
        sequenceOf(resources.configuration.locale)
    }

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.detectLocaleFromText(text: String): Sequence<Locale> {
    val textClassificationManager = getSystemService(TextClassificationManager::class.java)
    val textClassifier = textClassificationManager.textClassifier

    val textRequest = TextLanguage.Request.Builder(text).build()
    val detectedLanguage = textClassifier.detectLanguage(textRequest)

    return sequence {
        for (i in 0 until detectedLanguage.localeHypothesisCount) {
            val localeDetected = detectedLanguage.getLocale(i)
//            val confidence = detectedLanguage.getConfidenceScore(localeDetected) * 100.0
//            if (confidence >= 80f)
            yield(localeDetected.toLocale())
        }
    }
}

enum class PlaybackStatus {
    STOPPED,
    PAUSED,
    PLAYING
}
