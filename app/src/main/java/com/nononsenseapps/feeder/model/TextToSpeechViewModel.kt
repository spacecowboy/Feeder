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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareViewModel
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DI

class TextToSpeechViewModel(di: DI) : DIAwareViewModel(di), TextToSpeech.OnInitListener {
    private var textToSpeech: TextToSpeech? = null
    private val speechListener: UtteranceProgressListener by lazy {
        object : UtteranceProgressListener() {
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
    }
    private val textToSpeechQueue = mutableMapOf<String, String>()
    private var textToSpeechId: Int = 0
    private var initializedState: Int? = null
    private var startJob: Job? = null

    private val _readAloudState = mutableStateOf(PlaybackStatus.STOPPED)
    val readAloudState: State<PlaybackStatus> = _readAloudState
    val notStopped: State<Boolean> = derivedStateOf {
        _readAloudState.value != PlaybackStatus.STOPPED
    }

    private val _title = mutableStateOf("")
    val title: State<String> = _title

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

    fun play() {
        startJob?.cancel()
        startJob = viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            if (textToSpeech == null) {
                initializedState = null
                textToSpeech = TextToSpeech(
                    context,
                    this@TextToSpeechViewModel
                )
            }
            while (initializedState == null) {
                Log.d(LOG_TAG, "Delaying a little")
                delay(100)
            }
            if (initializedState != TextToSpeech.SUCCESS) {
                Toast.makeText(context, R.string.failed_to_load_text_to_speech, Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }
            _readAloudState.value = PlaybackStatus.PLAYING
            // Can only set this once engine has been initialized
            textToSpeech?.setOnUtteranceProgressListener(speechListener)
            for ((utteranceId, text) in textToSpeechQueue) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech?.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
                } else {
                    val params = HashMap<String, String>()
                    params[KEY_PARAM_UTTERANCE_ID] = utteranceId
                    @Suppress("DEPRECATION")
                    textToSpeech?.speak(text, TextToSpeech.QUEUE_ADD, params)
                }
            }
        }
    }

    fun pause() {
        textToSpeech?.stop()
        _readAloudState.value = PlaybackStatus.PAUSED
    }

    fun stop() {
        textToSpeech?.stop()
        textToSpeechQueue.clear()
        _readAloudState.value = PlaybackStatus.STOPPED
    }

    override fun onInit(status: Int) {
        initializedState = status
        val context = getApplication<Application>().applicationContext

        if (status==TextToSpeech.SUCCESS) {
            val selectedLocale = context.getLocales()
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

            if (selectedLocale==null) {
                Log.e(LOG_TAG, "None of the user's locales was supported by text to speech")
            }
        } else {
            Log.e(LOG_TAG, "Failed to load TextToSpeech object: $status")
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.shutdown()
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
