package com.nononsenseapps.feeder.model

import android.app.Application
import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DI
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.mutableMapOf
import kotlin.collections.set

class TextToSpeechViewModel(di: DI) : DIAwareViewModel(di), TextToSpeech.OnInitListener {
    private val textToSpeech = TextToSpeech(getApplication<Application>().applicationContext, this)
    private val speechListener: UtteranceProgressListener = object : UtteranceProgressListener() {
        override fun onDone(utteranceId: String) {
            textToSpeechQueue.remove(utteranceId)
            if (textToSpeechQueue.isEmpty()) {
                viewModelScope.launch {
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

        override fun onError(utteranceId: String) {
            textToSpeechQueue.remove(utteranceId)
        }
    }
    private val textToSpeechQueue = mutableMapOf<String, String>()
    private var textToSpeechId: Int = 0
    private var initialized: Boolean = false
    private var startJob: Job? = null

    private val _readAloudState = mutableStateOf(PlaybackStatus.STOPPED)
    val readAloudState: State<PlaybackStatus> = _readAloudState

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    @Deprecated("Stop", ReplaceWith("readAloud"))
    fun textToSpeechAddText(fullText: String) {
        val textArray = fullText.split("\n", ". ")
        for (text in textArray) {
            if (text.isBlank()) {
                continue
            }
            textToSpeechQueue[textToSpeechId.toString()] = text
            textToSpeechId++
        }
    }

    fun readAloud(title: String, fullText: String) {
        val textArray = fullText.split("\n", ". ")
        for (text in textArray) {
            if (text.isBlank()) {
                continue
            }
            textToSpeechQueue[textToSpeechId.toString()] = text
            textToSpeechId++
        }
        _title.value = title
        play()
    }

    @Deprecated("Stop using this", replaceWith = ReplaceWith("play"))
    fun textToSpeechStart(coroutineScope: CoroutineScope) {
        startJob?.cancel()
        startJob = coroutineScope.launch {
            while (!initialized) {
                delay(100)
            }
            // Can only set this once engine has been initialized
            textToSpeech.setOnUtteranceProgressListener(speechListener)
            for ((utteranceId, text) in textToSpeechQueue) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
                } else {
                    val params = HashMap<String, String>()
                    params[KEY_PARAM_UTTERANCE_ID] = utteranceId
                    @Suppress("DEPRECATION")
                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, params)
                }
            }
        }
    }

    fun play() {
        startJob?.cancel()
        startJob = viewModelScope.launch {
            while (!initialized) {
                delay(100)
            }
            _readAloudState.value = PlaybackStatus.PLAYING
            // Can only set this once engine has been initialized
            textToSpeech.setOnUtteranceProgressListener(speechListener)
            for ((utteranceId, text) in textToSpeechQueue) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
                } else {
                    val params = HashMap<String, String>()
                    params[KEY_PARAM_UTTERANCE_ID] = utteranceId
                    @Suppress("DEPRECATION")
                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, params)
                }
            }
        }
    }

    fun pause() {
        textToSpeech.stop()
        _readAloudState.value = PlaybackStatus.PAUSED
    }

    @Deprecated("Stop using this confusing name", replaceWith = ReplaceWith("stop"))
    fun textToSpeechClear() {
        textToSpeech.stop()
        textToSpeechQueue.clear()
    }

    fun stop() {
        textToSpeech.stop()
        textToSpeechQueue.clear()
        _readAloudState.value = PlaybackStatus.STOPPED
    }

    override fun onInit(status: Int) {
        val context = getApplication<Application>().applicationContext

        if (status == TextToSpeech.SUCCESS) {
            val selectedLocale = context.getLocales()
                .firstOrNull { locale ->
                    when (textToSpeech.setLanguage(locale)) {
                        TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> {
                            Log.e(LOG_TAG, "${locale.displayLanguage} is not supported!")
                            false
                        }
                        else -> {
                            true
                        }
                    }
                }
            initialized = true

            if (selectedLocale == null) {
                Log.e(LOG_TAG, "None of the user's locales was supported by text to speech")
            }
        } else {
            Log.e(LOG_TAG, "Failed to load TextToSpeech object.")
            Toast.makeText(context, R.string.failed_to_load_text_to_speech, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech.shutdown()
    }

    companion object {
        private const val LOG_TAG = "FeederTextToSpeech"
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

enum class PlaybackStatus {
    STOPPED,
    PAUSED,
    PLAYING
}
