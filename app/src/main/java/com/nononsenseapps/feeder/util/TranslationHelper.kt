package com.nononsenseapps.feeder.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.nononsenseapps.feeder.archmodel.TargetLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object TranslationHelper {

    private val mutex = Mutex()
    private var translator: Translator? = null
    private var currentTargetLanguage: String? = null

    /**
     * Translates the given text from English to the specified target language.
     * Downloads the model if not already available.
     * Preserves formatting by translating paragraph by paragraph.
     *
     * @param text The text to translate
     * @param targetLanguage The target language for translation
     * @return The translated text with formatting preserved
     */
    suspend fun translate(text: String, targetLanguage: TargetLanguage): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            val mlKitLanguage = targetLanguage.toMlKitLanguage()
            
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(mlKitLanguage)
                .build()

            // Reuse translator if target language hasn't changed
            if (currentTargetLanguage != mlKitLanguage) {
                translator?.close()
                translator = Translation.getClient(options)
                currentTargetLanguage = mlKitLanguage
            }
            val activeTranslator = translator!!

            // Allow download on any network (WiFi or mobile data)
            val conditions = DownloadConditions.Builder()
                .build()

            // Wait for model download
            suspendCancellableCoroutine { continuation ->
                activeTranslator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }

            // Translate paragraph by paragraph to preserve formatting
            translateWithFormatPreservation(text, activeTranslator)
        }
    }

    /**
     * Translates text while preserving paragraph structure.
     * Splits by newlines, translates each paragraph, and rejoins.
     */
    private suspend fun translateWithFormatPreservation(
        text: String,
        translator: Translator
    ): String {
        // Split by double newlines (paragraphs) or single newlines
        val paragraphs = text.split(Regex("(\r?\n\r?\n|\r?\n)"))
        val separators = Regex("(\r?\n\r?\n|\r?\n)").findAll(text).map { it.value }.toList()
        
        val translatedParagraphs = paragraphs.mapIndexed { index, paragraph ->
            if (paragraph.isBlank()) {
                paragraph
            } else {
                translateSingleText(paragraph, translator)
            }
        }
        
        // Rejoin with original separators
        return buildString {
            translatedParagraphs.forEachIndexed { index, translated ->
                append(translated)
                if (index < separators.size) {
                    append(separators[index])
                }
            }
        }
    }

    private suspend fun translateSingleText(text: String, translator: Translator): String {
        return suspendCancellableCoroutine { continuation ->
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    continuation.resume(translatedText)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    /**
     * Closes the translator and releases resources.
     */
    fun close() {
        translator?.close()
        translator = null
        currentTargetLanguage = null
    }

    /**
     * Maps TargetLanguage enum to ML Kit TranslateLanguage code.
     */
    private fun TargetLanguage.toMlKitLanguage(): String {
        return when (this) {
            TargetLanguage.SYSTEM -> getSystemLanguage()
            TargetLanguage.ENGLISH -> TranslateLanguage.ENGLISH
            TargetLanguage.CHINESE -> TranslateLanguage.CHINESE
            TargetLanguage.JAPANESE -> TranslateLanguage.JAPANESE
            TargetLanguage.KOREAN -> TranslateLanguage.KOREAN
            TargetLanguage.FRENCH -> TranslateLanguage.FRENCH
            TargetLanguage.GERMAN -> TranslateLanguage.GERMAN
            TargetLanguage.SPANISH -> TranslateLanguage.SPANISH
            TargetLanguage.RUSSIAN -> TranslateLanguage.RUSSIAN
            TargetLanguage.PORTUGUESE -> TranslateLanguage.PORTUGUESE
            TargetLanguage.ARABIC -> TranslateLanguage.ARABIC
        }
    }

    /**
     * Gets the system's default language, falling back to English if not supported.
     */
    private fun getSystemLanguage(): String {
        val systemLang = Locale.getDefault().language
        return TranslateLanguage.fromLanguageTag(systemLang) ?: TranslateLanguage.ENGLISH
    }
}
