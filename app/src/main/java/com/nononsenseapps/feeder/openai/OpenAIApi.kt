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
import com.nononsenseapps.feeder.archmodel.TargetLanguage
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.serialization.Serializable

class OpenAIApi(
    private val repository: Repository,
    private val appLang: String,
    private val openAIClientFactory: (OpenAISettings) -> OpenAIClient,
) {
    @Serializable
    data class SummaryResponse(
        val lang: String,
        val content: String,
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
        data class Success(
            val translatedText: String,
        ) : TranslationResult

        data class Error(
            val message: String,
        ) : TranslationResult

        data object NotConfigured : TranslationResult
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

    /**
     * Translates text to the specified target language using OpenAI.
     * Preserves all HTML tags and formatting in the translation.
     */
    suspend fun translate(text: String, targetLanguage: TargetLanguage): TranslationResult {
        val settings = openAISettings
        if (!settings.isValid) {
            return TranslationResult.NotConfigured
        }

        return try {
            val response = openAI.chatCompletion(
                request = translationRequest(text, targetLanguage),
                requestOptions = null,
            )
            val translatedText = response.choices
                .firstOrNull()
                ?.message
                ?.content ?: throw IllegalStateException("Translation response is null")
            
            TranslationResult.Success(translatedText = translatedText)
        } catch (e: Exception) {
            TranslationResult.Error(message = e.message ?: e.cause?.message ?: "Translation failed")
        }
    }

    private fun translationRequest(text: String, targetLanguage: TargetLanguage): ChatCompletionRequest {
        val targetLangName = targetLanguage.getDisplayName()
        return ChatCompletionRequest(
            model = ModelId(id = openAISettings.modelId),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    messageContent = TextContent(
                        listOf(
                            "You are a professional translator.",
                            "Translate the following text to $targetLangName.",
                            "Strictly preserve all HTML tags, formatting, and structure.",
                            "Do not output markdown. Output raw translated text with the original HTML tags intact.",
                            "Do not add any explanations or notes. Only output the translation.",
                        ).joinToString(separator = " ")
                    ),
                ),
                ChatMessage(
                    role = ChatRole.User,
                    messageContent = TextContent(text),
                ),
            ),
            responseFormat = ChatResponseFormat.Text,
        )
    }

    private fun TargetLanguage.getDisplayName(): String {
        return when (this) {
            TargetLanguage.SYSTEM -> java.util.Locale.getDefault().displayLanguage
            TargetLanguage.ENGLISH -> "English"
            TargetLanguage.CHINESE -> "Chinese"
            TargetLanguage.JAPANESE -> "Japanese"
            TargetLanguage.KOREAN -> "Korean"
            TargetLanguage.FRENCH -> "French"
            TargetLanguage.GERMAN -> "German"
            TargetLanguage.SPANISH -> "Spanish"
            TargetLanguage.RUSSIAN -> "Russian"
            TargetLanguage.PORTUGUESE -> "Portuguese"
            TargetLanguage.ARABIC -> "Arabic"
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
                                    "First line must be: 'Lang: \"ISO code\"'",
                                    "Keep summaries up to 100 words, 3 paragraphs, with up to 3 bullet points per paragraph.",
                                    "For readability use bullet points, titles, quotes and new lines using plain text only.",
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
}

val OpenAISettings.isAzure: Boolean
    get() = baseUrl.contains("openai.azure.com", ignoreCase = true)

val OpenAISettings.isPerplexity: Boolean
    get() = baseUrl.contains("api.perplexity.ai", ignoreCase = true)

val OpenAISettings.isValid: Boolean
    get() =
        modelId.isNotEmpty() &&
            key.isNotEmpty() &&
            if (isAzure) azureApiVersion.isNotBlank() && azureDeploymentId.isNotBlank() else true

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

fun OpenAIHost.toUrl(): URLBuilder =
    URLBuilder()
        .takeFrom(baseUrl)
        .also {
            queryParams.forEach { (k, v) -> it.parameters.append(k, v) }
        }
