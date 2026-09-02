package com.nononsenseapps.feeder.localtranslation

import android.app.Application
import android.icu.util.ULocale
import android.os.Build
import android.view.translation.TranslationCapability
import android.view.translation.TranslationContext
import android.view.translation.TranslationManager
import android.view.translation.TranslationRequest
import android.view.translation.TranslationRequestValue
import android.view.translation.TranslationResponse
import android.view.translation.TranslationResponseValue
import android.view.translation.TranslationSpec
import androidx.annotation.RequiresApi
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.detectLocaleFromText
import com.nononsenseapps.feeder.model.hasEnoughTextForLanguageDetection
import com.nononsenseapps.feeder.model.prepareTextForLanguageDetection
import com.nononsenseapps.feeder.openai.OpenAIApi.TranslationResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.Locale
import java.util.concurrent.Executor

class LocalTranslator(
    override val di: DI,
) : DIAware {
    private val application: Application by instance()
    private val directExecutor = Executor { runnable -> runnable.run() }

    suspend fun translate(
        content: String,
        targetLanguage: String,
        sourceLangHint: String = "",
        preserveHtml: Boolean = false,
    ): TranslationResult =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                return@withContext TranslationResult.Error(
                    content = application.getString(R.string.local_translation_requires_android_12),
                )
            }

            val text = prepareTextForLanguageDetection(content, preserveHtml)
            if (text.isBlank()) {
                return@withContext TranslationResult.Success(
                    content = content,
                    detectedLanguage = "",
                )
            }

            val targetLang = normalizeLanguageCode(targetLanguage)
            val sourceLang = sourceLangHint.ifBlank { detectSourceLanguage(text) }

            if (sourceLang == targetLang) {
                return@withContext TranslationResult.Success(
                    content = content,
                    detectedLanguage = sourceLang,
                )
            }

            if (preserveHtml) {
                translateHtml(
                    html = content,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                )
            } else {
                translatePlainText(
                    content = content,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                )
            }
        }

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun translateHtml(
        html: String,
        sourceLang: String,
        targetLang: String,
    ): TranslationResult {
        val document = Jsoup.parseBodyFragment(html)
        val translationTargets =
            document
                .body()
                .let(::collectTextNodes)
                .mapNotNull { textNode ->
                    HtmlTextNodeTranslation
                        .from(textNode)
                        .takeIf { hasEnoughTextForLanguageDetection(it.text) }
                }

        if (translationTargets.isEmpty()) {
            return TranslationResult.Success(
                content = html,
                detectedLanguage = sourceLang,
            )
        }

        val translatedTexts =
            translateTextValues(
                content = translationTargets.map(HtmlTextNodeTranslation::text),
                sourceLang = sourceLang,
                targetLang = targetLang,
            )

        return when (translatedTexts) {
            is LocalTranslationResult.Success -> {
                translationTargets.zip(translatedTexts.values).forEach { (target, translatedText) ->
                    target.textNode.text(target.leadingWhitespace + translatedText + target.trailingWhitespace)
                }
                TranslationResult.Success(
                    content = document.body().html(),
                    detectedLanguage = sourceLang,
                )
            }

            is LocalTranslationResult.Error -> TranslationResult.Error(translatedTexts.message)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun translatePlainText(
        content: String,
        sourceLang: String,
        targetLang: String,
    ): TranslationResult =
        when (
            val result =
                translateTextValues(
                    content = listOf(content),
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                )
        ) {
            is LocalTranslationResult.Success ->
                TranslationResult.Success(
                    content = result.values.firstOrNull().orEmpty(),
                    detectedLanguage = sourceLang,
                )

            is LocalTranslationResult.Error -> TranslationResult.Error(result.message)
        }

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun translateTextValues(
        content: List<String>,
        sourceLang: String,
        targetLang: String,
    ): LocalTranslationResult {
        val translationManager =
            application.getSystemService(TranslationManager::class.java)
                ?: return LocalTranslationResult.Error("Local translation is not available on this device.")

        val sourceSpec = TranslationSpec(ULocale.forLanguageTag(sourceLang), TranslationSpec.DATA_FORMAT_TEXT)
        val targetSpec = TranslationSpec(ULocale.forLanguageTag(targetLang), TranslationSpec.DATA_FORMAT_TEXT)

        val capability =
            translationManager
                .getOnDeviceTranslationCapabilities(
                    TranslationSpec.DATA_FORMAT_TEXT,
                    TranslationSpec.DATA_FORMAT_TEXT,
                ).firstOrNull { capability ->
                    capability.sourceSpec.locale.language
                        .equals(sourceLang, ignoreCase = true) &&
                        capability.targetSpec.locale.language
                            .equals(targetLang, ignoreCase = true)
                }

        when (capability?.state) {
            TranslationCapability.STATE_ON_DEVICE -> Unit
            TranslationCapability.STATE_AVAILABLE_TO_DOWNLOAD -> {
                return LocalTranslationResult.Error(
                    "Install the $sourceLang → $targetLang translation model in device settings, then retry.",
                )
            }
            TranslationCapability.STATE_DOWNLOADING ->
                return LocalTranslationResult.Error(
                    "Local translation model for $sourceLang to $targetLang is still downloading. Retry after it finishes.",
                )
            TranslationCapability.STATE_NOT_AVAILABLE ->
                return LocalTranslationResult.Error("Local translation does not support $sourceLang to $targetLang on this device.")
            null ->
                return LocalTranslationResult.Error("Local translation does not support $sourceLang to $targetLang on this device.")
        }

        val translationContext =
            TranslationContext
                .Builder(sourceSpec, targetSpec)
                .build()

        val translatorDeferred = CompletableDeferred<android.view.translation.Translator?>()
        translationManager.createOnDeviceTranslator(
            translationContext,
            directExecutor,
        ) { translator ->
            translatorDeferred.complete(translator)
        }

        val translator =
            withTimeoutOrNull(TRANSLATION_TIMEOUT_MS) { translatorDeferred.await() }
                ?: return LocalTranslationResult.Error("Local translator could not be created.")

        return try {
            val responseDeferred = CompletableDeferred<TranslationResponse>()
            val request =
                TranslationRequest
                    .Builder()
                    .setFlags(TranslationRequest.FLAG_TRANSLATION_RESULT)
                    .setTranslationRequestValues(content.map(TranslationRequestValue::forText))
                    .build()

            translator.translate(
                request,
                null,
                directExecutor,
            ) { response ->
                responseDeferred.complete(response)
            }

            val response =
                withTimeoutOrNull(TRANSLATION_TIMEOUT_MS) { responseDeferred.await() }
                    ?: return LocalTranslationResult.Error("Local translation timed out.")

            if (response.translationStatus != TranslationResponse.TRANSLATION_STATUS_SUCCESS) {
                return LocalTranslationResult.Error("Local translation failed.")
            }

            val translatedTexts =
                content.indices.map { index ->
                    val value = response.translationResponseValues[index]
                    val translatedText = value?.text?.toString().orEmpty()
                    if (value?.statusCode != TranslationResponseValue.STATUS_SUCCESS || translatedText.isBlank()) {
                        return LocalTranslationResult.Error("Local translation returned no text.")
                    }
                    translatedText
                }

            LocalTranslationResult.Success(translatedTexts)
        } catch (e: IllegalStateException) {
            val message = e.message.orEmpty()
            if (message.contains("destroyed", ignoreCase = true)) {
                LocalTranslationResult.Error("Local translator was not ready. Check device translation settings and retry.")
            } else {
                LocalTranslationResult.Error(message.ifBlank { "Local translation failed." })
            }
        } catch (e: Exception) {
            LocalTranslationResult.Error(e.message ?: "Local translation failed.")
        } finally {
            translator.destroy()
        }
    }

    private fun collectTextNodes(node: Node): List<TextNode> =
        buildList {
            fun visit(current: Node) {
                if (current is TextNode) {
                    add(current)
                }
                current.childNodes().forEach(::visit)
            }
            visit(node)
        }

    private fun detectSourceLanguage(content: String): String =
        runCatching {
            application
                .detectLocaleFromText(
                    text = content.take(1000),
                    minConfidence = 50.0f,
                ).firstOrNull()
                ?.locale
                ?.language
                ?: "en"
        }.getOrDefault("en")

    private fun normalizeLanguageCode(language: String): String {
        val normalized =
            language
                .trim()
                .lowercase(Locale.ROOT)
                .replace('_', '-')
                .substringBefore('-')

        return when (normalized) {
            "english", "en" -> "en"
            "german", "de" -> "de"
            "french", "fr" -> "fr"
            "spanish", "es" -> "es"
            "portuguese", "pt" -> "pt"
            "italian", "it" -> "it"
            "dutch", "nl" -> "nl"
            "polish", "pl" -> "pl"
            "russian", "ru" -> "ru"
            "czech", "cs" -> "cs"
            "estonian", "et" -> "et"
            "bulgarian", "bg" -> "bg"
            "icelandic", "is" -> "is"
            "norwegian", "nb", "nn" -> "nb"
            "persian", "fa" -> "fa"
            "ukrainian", "uk" -> "uk"
            else -> normalized
        }
    }

    companion object {
        private const val TRANSLATION_TIMEOUT_MS = 60_000L
    }
}

private sealed interface LocalTranslationResult {
    data class Success(
        val values: List<String>,
    ) : LocalTranslationResult

    data class Error(
        val message: String,
    ) : LocalTranslationResult
}

private data class HtmlTextNodeTranslation(
    val textNode: TextNode,
    val leadingWhitespace: String,
    val text: String,
    val trailingWhitespace: String,
) {
    companion object {
        fun from(textNode: TextNode): HtmlTextNodeTranslation {
            val wholeText = textNode.wholeText
            val leadingWhitespace = wholeText.takeWhile(Char::isWhitespace)
            val trailingWhitespace = wholeText.takeLastWhile(Char::isWhitespace)
            return HtmlTextNodeTranslation(
                textNode = textNode,
                leadingWhitespace = leadingWhitespace,
                text =
                    wholeText
                        .drop(leadingWhitespace.length)
                        .dropLast(trailingWhitespace.length),
                trailingWhitespace = trailingWhitespace,
            )
        }
    }
}
