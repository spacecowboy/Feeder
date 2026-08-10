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
import androidx.annotation.StringRes
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.detectLocaleFromText
import com.nononsenseapps.feeder.model.hasEnoughTextForLanguageDetection
import com.nononsenseapps.feeder.model.prepareTextForLanguageDetection
import com.nononsenseapps.feeder.model.prepareTextSamplesForLanguageDetection
import com.nononsenseapps.feeder.openai.OpenAIApi.TranslationResult
import com.nononsenseapps.feeder.openai.OpenAIApi.TranslationResult.ErrorAction
import com.nononsenseapps.feeder.util.logDebug
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
            val sourceLang =
                sourceLangHint.ifBlank {
                    detectSourceLanguage(
                        content = content,
                        preserveHtml = preserveHtml,
                    )
                }

            if (sourceLang == targetLang) {
                return@withContext TranslationResult.Success(
                    content = content,
                    detectedLanguage = sourceLang,
                )
            }

            val androidSystemResult =
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

            if (androidSystemResult is TranslationResult.Success) {
                androidSystemResult
            } else if (preserveHtml) {
                translateHtmlWithBergamot(
                    html = content,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                )
            } else {
                translatePlainTextWithBergamot(
                    content = content,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                )
            }
        }

    suspend fun canTranslateWithoutBergamotDownload(
        content: String,
        targetLanguage: String,
        sourceLangHint: String = "",
        preserveHtml: Boolean = false,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val text = prepareTextForLanguageDetection(content, preserveHtml)
            if (text.isBlank()) {
                return@withContext true
            }

            val targetLang = normalizeLanguageCode(targetLanguage)
            val sourceLang =
                sourceLangHint.ifBlank {
                    detectSourceLanguage(
                        content = content,
                        preserveHtml = preserveHtml,
                    )
                }
            if (sourceLang == targetLang) {
                return@withContext true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hasAndroidSystemModel(sourceLang, targetLang)) {
                return@withContext true
            }

            bergamotModelManager.languagePairStatus(
                sourceLanguage = sourceLang,
                targetLanguage = targetLang,
                allowNetwork = false,
            ) == BergamotLanguagePairStatus.Downloaded
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

            is LocalTranslationResult.Error ->
                TranslationResult.Error(
                    content = translatedTexts.message,
                    action = translatedTexts.action,
                )
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

            is LocalTranslationResult.Error ->
                TranslationResult.Error(
                    content = result.message,
                    action = result.action,
                )
        }

    private suspend fun translateHtmlWithBergamot(
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
            translateTextValuesWithBergamot(
                content = translationTargets.map(HtmlTextNodeTranslation::text),
                sourceLang = sourceLang,
                targetLang = targetLang,
                preserveHtml = false,
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

            is LocalTranslationResult.Error ->
                TranslationResult.Error(
                    content = translatedTexts.message,
                    action = translatedTexts.action,
                )
        }
    }

    private suspend fun translatePlainTextWithBergamot(
        content: String,
        sourceLang: String,
        targetLang: String,
    ): TranslationResult =
        when (
            val result =
                translateTextValuesWithBergamot(
                    content = listOf(content),
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                    preserveHtml = false,
                )
        ) {
            is LocalTranslationResult.Success ->
                TranslationResult.Success(
                    content = result.values.firstOrNull().orEmpty(),
                    detectedLanguage = sourceLang,
                )

            is LocalTranslationResult.Error ->
                TranslationResult.Error(
                    content = result.message,
                    action = result.action,
                )
        }

    private suspend fun translateTextValuesWithBergamot(
        content: List<String>,
        sourceLang: String,
        targetLang: String,
        preserveHtml: Boolean,
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
                    return when (preparation.reason) {
                        BergamotModelPreparation.ErrorReason.NoAppModel ->
                            LocalTranslationResult.Error(
                                message =
                                    application.getString(
                                        R.string.no_bergamot_translation_model,
                                        sourceLang,
                                        targetLang,
                                    ),
                                action = ErrorAction.OpenSystemTranslationSettings,
                            )
                        BergamotModelPreparation.ErrorReason.RegistryMissing ->
                            LocalTranslationResult.Error(
                                application.getString(R.string.bergamot_model_registry_unavailable),
                            )
                        BergamotModelPreparation.ErrorReason.DownloadFailed ->
                            LocalTranslationResult.Error(
                                application.getString(
                                    R.string.bergamot_model_download_failed,
                                    preparation.sourceLanguage,
                                    preparation.targetLanguage,
                                ),
                            )
                    }
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
                        application.getString(R.string.bergamot_translation_timed_out),
                    )
            ) {
                is BergamotWebTranslationResult.Success -> LocalTranslationResult.Success(result.values)
                is BergamotWebTranslationResult.Error ->
                    LocalTranslationResult.Error(
                        result.message.toLocalTranslationError(R.string.bergamot_translation_failed),
                    )
            }
        } catch (e: Exception) {
            LocalTranslationResult.Error(
                e.message.orEmpty().toLocalTranslationError(R.string.bergamot_translation_failed),
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
                ?: return LocalTranslationResult.Error(
                    application.getString(R.string.local_translation_unavailable),
                )

        val sourceSpec = TranslationSpec(ULocale.forLanguageTag(sourceLang), TranslationSpec.DATA_FORMAT_TEXT)
        val targetSpec = TranslationSpec(ULocale.forLanguageTag(targetLang), TranslationSpec.DATA_FORMAT_TEXT)

        val capabilities =
            translationManager.getOnDeviceTranslationCapabilities(
                TranslationSpec.DATA_FORMAT_TEXT,
                TranslationSpec.DATA_FORMAT_TEXT,
            )
        val capability =
            capabilities.firstOrNull { capability ->
                capability.sourceSpec.locale.language
                    .equals(sourceLang, ignoreCase = true) &&
                    capability.targetSpec.locale.language
                        .equals(targetLang, ignoreCase = true)
            }

        if (capability?.state != TranslationCapability.STATE_ON_DEVICE) {
            logDebug(
                LOG_TAG,
                "System translation unavailable for $sourceLang -> $targetLang. " +
                    "Match=${capability?.state.translationCapabilityStateName()}. " +
                    "Capabilities=${capabilities.toDebugSummary()}",
            )
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
                ?: return LocalTranslationResult.Error(
                    application.getString(R.string.local_translator_creation_failed),
                )

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
                    ?: return LocalTranslationResult.Error(
                        application.getString(R.string.local_translation_timed_out),
                    )

            if (response.translationStatus != TranslationResponse.TRANSLATION_STATUS_SUCCESS) {
                return LocalTranslationResult.Error(
                    application.getString(R.string.local_translation_failed),
                )
            }

            val translatedTexts =
                content.indices.map { index ->
                    val value = response.translationResponseValues[index]
                    val translatedText = value?.text?.toString().orEmpty()
                    if (value?.statusCode != TranslationResponseValue.STATUS_SUCCESS || translatedText.isBlank()) {
                        return LocalTranslationResult.Error(
                            application.getString(R.string.local_translation_empty_result),
                        )
                    }
                    translatedText
                }

            LocalTranslationResult.Success(translatedTexts)
        } catch (e: IllegalStateException) {
            val message = e.message.orEmpty()
            if (message.contains("destroyed", ignoreCase = true)) {
                LocalTranslationResult.Error(
                    application.getString(R.string.local_translator_not_ready),
                )
            } else {
                LocalTranslationResult.Error(
                    message.ifBlank { application.getString(R.string.local_translation_failed) },
                )
            }
        } catch (e: Exception) {
            LocalTranslationResult.Error(
                e.message ?: application.getString(R.string.local_translation_failed),
            )
        } finally {
            translator.destroy()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasAndroidSystemModel(
        sourceLang: String,
        targetLang: String,
    ): Boolean {
        val translationManager =
            application.getSystemService(TranslationManager::class.java)
                ?: return false

        return translationManager
            .getOnDeviceTranslationCapabilities(
                TranslationSpec.DATA_FORMAT_TEXT,
                TranslationSpec.DATA_FORMAT_TEXT,
            ).any { capability ->
                capability.state == TranslationCapability.STATE_ON_DEVICE &&
                    capability.sourceSpec.locale.language
                        .equals(sourceLang, ignoreCase = true) &&
                    capability.targetSpec.locale.language
                        .equals(targetLang, ignoreCase = true)
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

    private fun detectSourceLanguage(
        content: String,
        preserveHtml: Boolean,
    ): String =
        runCatching {
            val detectedLanguages =
                prepareTextSamplesForLanguageDetection(
                    content = content,
                    preserveHtml = preserveHtml,
                ).filter(::hasEnoughTextForLanguageDetection)
                    .mapNotNull { sample ->
                        application
                            .detectLocaleFromText(
                                text = sample,
                                minConfidence = 60.0f,
                            ).firstOrNull()
                            ?.locale
                            ?.language
                            ?.let(::normalizeLanguageCode)
                            ?.takeIf { it != "und" }
                    }

            val languageCounts = detectedLanguages.groupingBy { it }.eachCount()
            detectedLanguages.maxByOrNull { languageCounts.getValue(it) }
                ?: "und"
        }.getOrDefault("und")

    private fun String.toLocalTranslationError(
        @StringRes fallbackStringId: Int,
    ): String =
        lineSequence()
            .firstOrNull(String::isNotBlank)
            .orEmpty()
            .take(MAX_LOCAL_TRANSLATION_ERROR_LENGTH)
            .ifBlank { application.getString(fallbackStringId) }

    companion object {
        private const val LOG_TAG = "FEEDER_LOCAL_TRANSLATION"
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
        val action: ErrorAction = ErrorAction.None,
    ) : LocalTranslationResult
}

@RequiresApi(Build.VERSION_CODES.S)
private fun Int?.translationCapabilityStateName(): String =
    when (this) {
        TranslationCapability.STATE_ON_DEVICE -> "ON_DEVICE"
        TranslationCapability.STATE_AVAILABLE_TO_DOWNLOAD -> "AVAILABLE_TO_DOWNLOAD"
        TranslationCapability.STATE_DOWNLOADING -> "DOWNLOADING"
        TranslationCapability.STATE_NOT_AVAILABLE -> "NOT_AVAILABLE"
        else -> "NONE"
    }

@RequiresApi(Build.VERSION_CODES.S)
private fun Collection<TranslationCapability>.toDebugSummary(): String =
    joinToString(
        separator = ", ",
        limit = 20,
        truncated = "... ($size total)",
    ) { capability ->
        "${capability.sourceSpec.locale.toLanguageTag()}->${capability.targetSpec.locale.toLanguageTag()}:" +
            capability.state.translationCapabilityStateName()
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

private const val MAX_LOCAL_TRANSLATION_ERROR_LENGTH = 80
