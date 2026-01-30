package com.nononsenseapps.feeder.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.nononsenseapps.feeder.archmodel.TargetLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object TranslationHelper {

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
        val mlKitLanguage = targetLanguage.toMlKitLanguage()
        
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(mlKitLanguage)
            .build()
        
        // Create a new translator instance for this request (thread-safe)
        val translator = Translation.getClient(options)
        
        try {
            // Allow download on any network (WiFi or mobile data)
            val conditions = DownloadConditions.Builder()
                .build()

            // Wait for model download
            suspendCancellableCoroutine { continuation ->
                translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }

            // Translate paragraph by paragraph to preserve formatting
            translateWithFormatPreservation(text, translator)
        } finally {
            translator.close()
        }
    }

    /**
     * Translates text while preserving paragraph structure.
     * Tokenizes by newlines to preserve all separators.
     */
    private suspend fun translateWithFormatPreservation(
        text: String,
        translator: Translator
    ): String {
        // Use lookahead/lookbehind to keep delimiters in the list
        // This regex splits but keeps the delimiters (newlines) as separate tokens
        val tokens = text.split(Regex("(?<=[\n])|(?=[\n])"))

        val translatedTokens = tokens.map { token ->
            if (token.isBlank()) {
                // Preserve whitespace/newline tokens exactly as is
                token
            } else {
                // Translate actual content
                translateSingleText(token, translator)
            }
        }
        
        return translatedTokens.joinToString("")
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
