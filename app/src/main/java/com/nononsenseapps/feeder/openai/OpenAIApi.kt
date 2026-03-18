package com.nononsenseapps.feeder.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAIHost
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.archmodel.Repository
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenAIApi(
    private val repository: Repository,
    private val appLang: String,
    private val openAIClientFactory: (OpenAISettings) -> OpenAIClient,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    @Serializable
    data class SummaryResponse(
        val lang: String,
        val content: String,
    )

    @Serializable
    data class TranslationResponse(
        val lang: String,
        val targetLang: String,
        val content: String,
    )

    @Serializable
    private data class DeepLTranslateRequest(
        val text: List<String>,
        val target_lang: String,
        val source_lang: String? = null,
        val tag_handling: String? = null,
        val split_sentences: String = "nonewlines",
        val preserve_formatting: Boolean = true,
    )

    @Serializable
    private data class DeepLTranslation(
        val detected_source_language: String,
        val text: String,
    )

    @Serializable
    private data class DeepLTranslateResponse(
        val translations: List<DeepLTranslation>,
    )

    sealed interface SummaryResult {
        val content: String

        data class Success(
            val id: String,
            val created: Long,
            val model: String,
            override val content: String,
            val promptTokens: Int,
            val completeTokens: Int,
            val totalTokens: Int,
            val detectedLanguage: String,
        ) : SummaryResult

        data class Error(
            override val content: String,
        ) : SummaryResult
    }

    sealed interface TranslationResult {
        val content: String

        data class Success(
            val id: String,
            val created: Long,
            val model: String,
            override val content: String,
            val promptTokens: Int,
            val completeTokens: Int,
            val totalTokens: Int,
            val detectedLanguage: String,
            val targetLanguage: String,
        ) : TranslationResult

        data class Error(
            override val content: String,
        ) : TranslationResult
    }

    sealed interface ModelsResult {
        data object MissingToken : ModelsResult

        data object AzureApiVersionRequired : ModelsResult

        data object AzureDeploymentIdRequired : ModelsResult

        data class Success(
            val ids: List<String>,
        ) : ModelsResult

        data class Error(
            val message: String?,
        ) : ModelsResult
    }

    companion object {
        private val LANG_REGEX = Regex("^Lang: \"?([a-zA-Z]+)\"?$")
    }

    private val openAISettings: OpenAISettings
        get() = repository.openAISettings.value

    private val openAI: OpenAIClient
        get() = openAIClientFactory(openAISettings)

    suspend fun listModelIds(settings: OpenAISettings): ModelsResult {
        if (settings.key.isEmpty()) {
            return ModelsResult.MissingToken
        }
        if (settings.isDeepL) {
            return ModelsResult.Success(ids = emptyList())
        }
        if (settings.isPerplexity) {
            return ModelsResult.Success(ids = emptyList())
        }
        if (settings.isAzure) {
            if (settings.azureApiVersion.isBlank()) {
                return ModelsResult.AzureApiVersionRequired
            }
            if (settings.azureDeploymentId.isBlank()) {
                return ModelsResult.AzureDeploymentIdRequired
            }
        }
        return try {
            openAIClientFactory(settings)
                .models()
                .sortedByDescending { it.created }
                .map { it.id.id }
                .let { ModelsResult.Success(ids = it) }
        } catch (e: Exception) {
            ModelsResult.Error(message = e.message ?: e.cause?.message)
        }
    }

    suspend fun summarize(content: String): SummaryResult {
        if (openAISettings.isDeepL) {
            return SummaryResult.Error(content = "Summarization is not supported for DeepL translation settings")
        }
        try {
            val response =
                openAI.chatCompletion(
                    request = summaryRequest(content),
                    requestOptions = null,
                )
            val summaryResponse: SummaryResponse =
                parseSummaryResponse(
                    response.choices
                        .firstOrNull()
                        ?.message
                        ?.content ?: throw IllegalStateException("Response content is null"),
                )
            return SummaryResult.Success(
                id = response.id,
                model = response.model.id,
                content = summaryResponse.content,
                created = response.created,
                promptTokens = response.usage?.promptTokens ?: 0,
                completeTokens = response.usage?.completionTokens ?: 0,
                totalTokens = response.usage?.completionTokens ?: 0,
                detectedLanguage = summaryResponse.lang,
            )
        } catch (e: Exception) {
            return SummaryResult.Error(content = e.message ?: e.cause?.message ?: "")
        }
    }

    suspend fun translate(
        content: String,
        targetLanguage: String,
        preserveHtml: Boolean = false,
    ): TranslationResult {
        if (openAISettings.isDeepL) {
            return translateWithDeepL(
                content = content,
                targetLanguage = targetLanguage,
                preserveHtml = preserveHtml,
            )
        }
        try {
            val response =
                openAI.chatCompletion(
                    request = translationRequest(content, targetLanguage, preserveHtml),
                    requestOptions = null,
                )
            val translationResponse: TranslationResponse =
                parseTranslationResponse(
                    response.choices
                        .firstOrNull()
                        ?.message
                        ?.content ?: throw IllegalStateException("Response content is null"),
                )
            return TranslationResult.Success(
                id = response.id,
                model = response.model.id,
                content = translationResponse.content,
                created = response.created,
                promptTokens = response.usage?.promptTokens ?: 0,
                completeTokens = response.usage?.completionTokens ?: 0,
                totalTokens = response.usage?.completionTokens ?: 0,
                detectedLanguage = translationResponse.lang,
                targetLanguage = translationResponse.targetLang,
            )
        } catch (e: Exception) {
            return TranslationResult.Error(content = e.message ?: e.cause?.message ?: "")
        }
    }

    private fun translateWithDeepL(
        content: String,
        targetLanguage: String,
        preserveHtml: Boolean,
    ): TranslationResult {
        return try {
            val response =
                OkHttpClient.Builder()
                    .callTimeout(openAISettings.timeoutSeconds.coerceIn(30, 600).toLong(), TimeUnit.SECONDS)
                    .build()
                    .newCall(
                        Request.Builder()
                            .url(openAISettings.toDeepLTranslateUrl())
                            .header("Authorization", "DeepL-Auth-Key ${openAISettings.key}")
                            .header("Content-Type", "application/json")
                            .post(
                                json
                                    .encodeToString(
                                        DeepLTranslateRequest(
                                            text = listOf(content),
                                            target_lang = targetLanguage.toDeepLTargetLanguageCode(),
                                            tag_handling = if (preserveHtml) "html" else "",
                                        ),
                                    ).toRequestBody("application/json".toMediaType()),
                            ).build(),
                    ).execute()

            if (!response.isSuccessful) {
                return TranslationResult.Error(
                    content = "DeepL request failed: HTTP ${response.code}${response.message.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""}",
                )
            }

            val deeplResponse =
                json.decodeFromString<DeepLTranslateResponse>(
                    response.body?.string() ?: throw IllegalStateException("Response content is null"),
                )

            val translation = deeplResponse.translations.firstOrNull()
                ?: throw IllegalStateException("DeepL returned no translations")

            TranslationResult.Success(
                id = "deepl",
                model = "deepl",
                content = translation.text,
                created = 0L,
                promptTokens = 0,
                completeTokens = 0,
                totalTokens = 0,
                detectedLanguage = translation.detected_source_language,
                targetLanguage = targetLanguage.toDeepLTargetLanguageCode(),
            )
        } catch (e: Exception) {
            TranslationResult.Error(content = e.message ?: e.cause?.message ?: "")
        }
    }

    private fun parseSummaryResponse(content: String): SummaryResponse {
        val firstLine = content.lineSequence().firstOrNull() ?: ""
        val result = LANG_REGEX.find(firstLine)
        return SummaryResponse(
            lang = result?.groupValues?.getOrNull(1) ?: "",
            content = content.replaceFirst(firstLine, "").trim(),
        )
    }

    private fun parseTranslationResponse(content: String): TranslationResponse {
        val lines = content.lineSequence().toList()
        val detectedLanguage = LANG_REGEX.find(lines.getOrNull(0) ?: "")?.groupValues?.getOrNull(1) ?: ""
        val targetLanguage = LANG_REGEX.find(lines.getOrNull(1) ?: "")?.groupValues?.getOrNull(1) ?: ""
        val translatedContent =
            lines
                .drop(2)
                .joinToString(separator = "\n")
                .trim()
                .ifBlank { content.trim() }
        return TranslationResponse(
            lang = detectedLanguage,
            targetLang = targetLanguage,
            content = translatedContent,
        )
    }

    private fun summaryRequest(content: String): ChatCompletionRequest =
        ChatCompletionRequest(
            model = ModelId(id = openAISettings.modelId),
            messages =
                listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        messageContent =
                            TextContent(
                                listOf(
                                    "You are an assistant in an RSS reader app, summarizing article content.",
                                    "The app language is '$appLang'.",
                                    "Provide summaries in the article's language if 99% recognizable; otherwise, use the app language.",
                                    "First line must be exactly: 'Lang: \"ISO code\"' with NO markdown formatting around the Lang line whatsoever.",
                                    "Keep summaries up to 100 words, 3 paragraphs, with up to 3 bullet points per paragraph.",
                                    "For readability use markdown formatting: **bold** for emphasis, *italics* for quotes, bullet points (-) for lists, # headers for sections, and > for block quotes.",
                                    "Use markdown to structure content and improve readability.",
                                    "Use only single language.",
                                    "Keep full quotes if any.",
                                ).joinToString(separator = " "),
                            ),
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        messageContent = TextContent("Summarize:\n\n$content"),
                    ),
                ),
            responseFormat = ChatResponseFormat.Text,
        )

    private fun translationRequest(
        content: String,
        targetLanguage: String,
        preserveHtml: Boolean,
    ): ChatCompletionRequest =
        ChatCompletionRequest(
            model = ModelId(id = openAISettings.modelId),
            messages =
                listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        messageContent =
                            TextContent(
                                listOf(
                                    "You are an assistant in an RSS reader app, translating article content.",
                                    "The app language is '$appLang'.",
                                    "Translate the article into '$targetLanguage'.",
                                    "First line must be exactly: 'Lang: \"ISO code\"' for the detected source language.",
                                    "Second line must be exactly: 'Lang: \"ISO code\"' for the translation target language.",
                                    "Do not use markdown formatting on either Lang line.",
                                    if (preserveHtml) {
                                        "The content is HTML. Preserve all HTML tags, attributes, links, and element order. Translate only the human-readable text while returning valid HTML."
                                    } else {
                                        "Preserve structure, paragraph breaks, bullet lists, emphasis, and quoted text."
                                    },
                                    "Output only the translated content after the two Lang lines.",
                                ).joinToString(separator = " "),
                            ),
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        messageContent = TextContent("Translate:\n\n$content"),
                    ),
                ),
            responseFormat = ChatResponseFormat.Text,
        )
}

val OpenAISettings.isAzure: Boolean
    get() = baseUrl.contains("openai.azure.com", ignoreCase = true)

val OpenAISettings.isPerplexity: Boolean
    get() = baseUrl.contains("api.perplexity.ai", ignoreCase = true)

val OpenAISettings.isDeepL: Boolean
    get() = baseUrl.contains("deepl.com", ignoreCase = true) || key.endsWith(":fx")

val OpenAISettings.isValid: Boolean
    get() =
        if (isDeepL) {
            key.isNotEmpty()
        } else {
            modelId.isNotEmpty() &&
                key.isNotEmpty() &&
                if (isAzure) azureApiVersion.isNotBlank() && azureDeploymentId.isNotBlank() else true
        }

val OpenAISettings.canSummarize: Boolean
    get() = isValid && !isDeepL

val OpenAISettings.canTranslate: Boolean
    get() = isValid

fun OpenAISettings.toOpenAIHost(withAzureDeploymentId: Boolean): OpenAIHost =
    baseUrl.let { baseUrl ->
        if (baseUrl.isEmpty()) {
            OpenAIHost.OpenAI
        } else {
            OpenAIHost(
                baseUrl =
                    URLBuilder()
                        .takeFrom(baseUrl)
                        .also {
                            it.appendPathSegments("openai")
                            if (withAzureDeploymentId && azureDeploymentId.isNotBlank()) {
                                it.appendPathSegments("deployments", azureDeploymentId)
                            }
                        }.buildString(),
                queryParams =
                    azureApiVersion.let { apiVersion ->
                        if (apiVersion.isEmpty()) emptyMap() else mapOf("api-version" to apiVersion)
                    },
            )
        }
    }

fun OpenAISettings.toDeepLTranslateUrl(): String =
    URLBuilder()
        .takeFrom(
            when {
                baseUrl.isNotBlank() -> baseUrl
                key.endsWith(":fx") -> "https://api-free.deepl.com"
                else -> "https://api.deepl.com"
            },
        ).also {
            it.appendPathSegments("v2", "translate")
        }.buildString()

fun OpenAIHost.toUrl(): URLBuilder =
    URLBuilder()
        .takeFrom(baseUrl)
        .also {
            queryParams.forEach { (k, v) -> it.parameters.append(k, v) }
        }

private fun String.toDeepLTargetLanguageCode(): String =
    trim()
        .uppercase()
        .replace('-', '_')
        .let { normalized ->
            when (normalized) {
                "ENGLISH", "EN" -> "EN"
                "EN_GB", "ENGLISH_UK", "ENGLISH_GB", "BRITISH_ENGLISH" -> "EN-GB"
                "EN_US", "ENGLISH_US", "AMERICAN_ENGLISH" -> "EN-US"
                "GERMAN", "DE" -> "DE"
                "FRENCH", "FR" -> "FR"
                "SPANISH", "ES" -> "ES"
                "PORTUGUESE", "PT" -> "PT"
                "PT_BR", "PORTUGUESE_BR", "BRAZILIAN_PORTUGUESE" -> "PT-BR"
                "PT_PT", "PORTUGUESE_PT", "EUROPEAN_PORTUGUESE" -> "PT-PT"
                "ITALIAN", "IT" -> "IT"
                "DUTCH", "NL" -> "NL"
                "POLISH", "PL" -> "PL"
                "RUSSIAN", "RU" -> "RU"
                "JAPANESE", "JA" -> "JA"
                "CHINESE", "ZH" -> "ZH"
                "CZECH", "CS" -> "CS"
                "DANISH", "DA" -> "DA"
                "GREEK", "EL" -> "EL"
                "FINNISH", "FI" -> "FI"
                "HUNGARIAN", "HU" -> "HU"
                "INDONESIAN", "ID" -> "ID"
                "KOREAN", "KO" -> "KO"
                "LITHUANIAN", "LT" -> "LT"
                "LATVIAN", "LV" -> "LV"
                "NORWEGIAN", "NB", "NORWEGIAN_BOKMAL" -> "NB"
                "ROMANIAN", "RO" -> "RO"
                "SLOVAK", "SK" -> "SK"
                "SLOVENIAN", "SL" -> "SL"
                "SWEDISH", "SV" -> "SV"
                "TURKISH", "TR" -> "TR"
                "UKRAINIAN", "UK" -> "UK"
                else -> normalized.replace('_', '-')
            }
        }
