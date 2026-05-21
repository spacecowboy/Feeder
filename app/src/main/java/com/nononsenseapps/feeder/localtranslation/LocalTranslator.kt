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
import java.util.concurrent.Executor

class LocalTranslator(
    override val di: DI,
) : DIAware {
    private val application: Application by instance()
    private val directExecutor = Executor { runnable -> runnable.run() }
    private val bergamotModelManager: BergamotModelManager by instance()
    private val bergamotWebTranslator: BergamotWebTranslator by instance()

    suspend fun translate(
        content: String,
        targetLanguage: String,
        sourceLangHint: String = "",
        preserveHtml: Boolean = false,
    ): TranslationResult =
        withContext(Dispatchers.IO) {
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

            val androidSystemResult =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (preserveHtml) {
                        translateHtmlWithAndroidSystem(
                            html = content,
                            sourceLang = sourceLang,
                            targetLang = targetLang,
                        )
                    } else {
                        translatePlainTextWithAndroidSystem(
                            content = content,
                            sourceLang = sourceLang,
                            targetLang = targetLang,
                        )
                    }
                } else {
                    TranslationResult.Error("Android system on-device translation requires Android 12 or newer.")
                }

            if (androidSystemResult is TranslationResult.Success) {
                androidSystemResult
            } else if (preserveHtml) {
                translateHtmlWithBergamot(
                    html = content,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                    androidSystemError = androidSystemResult.content,
                )
            } else {
                translatePlainTextWithBergamot(
                    content = content,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                    androidSystemError = androidSystemResult.content,
                )
            }
        }

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun translateHtmlWithAndroidSystem(
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
    private suspend fun translatePlainTextWithAndroidSystem(
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

    private suspend fun translateHtmlWithBergamot(
        html: String,
        sourceLang: String,
        targetLang: String,
        androidSystemError: String,
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
            translateTextValuesWithBergamot(
                content = translationTargets.map(HtmlTextNodeTranslation::text),
                sourceLang = sourceLang,
                targetLang = targetLang,
                preserveHtml = false,
                androidSystemError = androidSystemError,
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

    private suspend fun translatePlainTextWithBergamot(
        content: String,
        sourceLang: String,
        targetLang: String,
        androidSystemError: String,
    ): TranslationResult =
        when (
            val result =
                translateTextValuesWithBergamot(
                    content = listOf(content),
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                    preserveHtml = false,
                    androidSystemError = androidSystemError,
                )
        ) {
            is LocalTranslationResult.Success ->
                TranslationResult.Success(
                    content = result.values.firstOrNull().orEmpty(),
                    detectedLanguage = sourceLang,
                )

            is LocalTranslationResult.Error -> TranslationResult.Error(result.message)
        }

    private suspend fun translateTextValuesWithBergamot(
        content: List<String>,
        sourceLang: String,
        targetLang: String,
        preserveHtml: Boolean,
        androidSystemError: String,
    ): LocalTranslationResult {
        val preparation =
            bergamotModelManager.prepare(
                sourceLanguage = sourceLang,
                targetLanguage = targetLang,
            )

        val modelRegistry =
            when (preparation) {
                is BergamotModelPreparation.Ready -> preparation.modelRegistry
                is BergamotModelPreparation.Error ->
                    return LocalTranslationResult.Error(
                        listOf(
                            "Android system translation unavailable: ${androidSystemError.ifBlank { "no on-device translator was available" }}",
                            preparation.message,
                            "App model storage: ${bergamotModelManager.storageLocation().absolutePath}",
                        ).joinToString(separator = "\n"),
                    )
            }

        return try {
            when (
                val result =
                    withTimeoutOrNull(TRANSLATION_TIMEOUT_MS) {
                        bergamotWebTranslator.translate(
                            content = content,
                            sourceLanguage = sourceLang,
                            targetLanguage = targetLang,
                            preserveHtml = preserveHtml,
                            modelRegistry = modelRegistry,
                        )
                    } ?: return LocalTranslationResult.Error(
                        "Android system translation unavailable: ${androidSystemError.ifBlank { "no on-device translator was available" }}\n" +
                            "Feeder's app-provided Bergamot translator timed out.",
                    )
            ) {
                is BergamotWebTranslationResult.Success -> LocalTranslationResult.Success(result.values)
                is BergamotWebTranslationResult.Error ->
                    LocalTranslationResult.Error(
                        "Android system translation unavailable: ${androidSystemError.ifBlank { "no on-device translator was available" }}\n" +
                            "Feeder's app-provided Bergamot model failed: ${result.message}",
                    )
            }
        } catch (e: Exception) {
            LocalTranslationResult.Error(
                "Android system translation unavailable: ${androidSystemError.ifBlank { "no on-device translator was available" }}\n" +
                    "Feeder's app-provided Bergamot model failed: ${e.message ?: "unknown error"}",
            )
        }
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
            withTimeoutOrNull(ANDROID_TRANSLATOR_CREATION_TIMEOUT_MS) { translatorDeferred.await() }
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
                withTimeoutOrNull(ANDROID_TRANSLATION_TIMEOUT_MS) { responseDeferred.await() }
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

    companion object {
        private const val ANDROID_TRANSLATOR_CREATION_TIMEOUT_MS = 5_000L
        private const val ANDROID_TRANSLATION_TIMEOUT_MS = 20_000L
        private const val TRANSLATION_TIMEOUT_MS = 5 * 60_000L
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
