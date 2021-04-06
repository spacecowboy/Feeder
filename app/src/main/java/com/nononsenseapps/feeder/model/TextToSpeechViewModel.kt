package com.nononsenseapps.feeder.model

import android.app.Application
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import org.kodein.di.Kodein
import java.util.*

class TextToSpeechViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech = TextToSpeech(getApplication<Application>().applicationContext, this)
    private var textToSpeechQueue: LinkedHashMap<String, String> = LinkedHashMap()
    private var textToSpeechCounter: Int = 0

    fun textToSpeechAddText(fullText: String) {
        val textArray = fullText.split("\n", ". ")
        for (text in textArray) {
            textToSpeechQueue[textToSpeechCounter.toString()] = text
            textToSpeechCounter++
        }
    }

    private var speechListener: UtteranceProgressListener? = null

    fun textToSpeechStart() {
        if (speechListener == null) {
            speechListener = object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    textToSpeechQueue.remove(utteranceId)
                }

                override fun onStart(utteranceId: String) {}
                override fun onError(utteraceId: String) {}
            }
            textToSpeech?.setOnUtteranceProgressListener(speechListener)
        }
        for (pair in textToSpeechQueue) {
            val utteranceId = pair.key
            val text = pair.value

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech?.speak(text,TextToSpeech.QUEUE_ADD,null, utteranceId);
            } else {
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
                textToSpeech?.speak(text, TextToSpeech.QUEUE_ADD, params);
            }
        }
    }

    fun textToSpeechPause() {
        textToSpeech?.stop()
    }

    fun textToSpeechClear() {
        textToSpeech?.stop()
        textToSpeechQueue.clear()
    }

    fun initializeTextToSpeechObject() {
        val context = getApplication<Application>().applicationContext
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        val logTag = "FeederTextToSpeech"
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(logTag, "The Language specified is not supported!")
            }
        } else {
            val errorMessage = "Failed to load TextToSpeech object."
            Log.e(logTag, errorMessage)
            Toast.makeText(getApplication<Application>().applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

}