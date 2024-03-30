package com.nononsenseapps.feeder.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.archmodel.Repository
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Locale

class OpenAIApi(
    private val repository: Repository
) {

    @Serializable
    data class SummaryResponse(val lang: String, val content: String)

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
            val detectedLanguage: String
        ) : SummaryResult

        data class Error(override val content: String) : SummaryResult
    }

    sealed interface ModelsResult {
        data object MissingToken: ModelsResult
        data class Success(val ids: List<String>): ModelsResult
        data class Error(val ex: Exception) : ModelsResult
    }

    private val openAISettings: OpenAISettings
        get() = repository.openAISettings.value

    private var openAIConfig = openAISettings.toOpenAIConfig()

    private var _openAI = OpenAI(config = openAIConfig)
    private val openAI: OpenAI
        get() = if (openAISettings != openAIConfig.toOpenAISettings(openAISettings.modelId)) {
            openAIConfig = openAISettings.toOpenAIConfig()
            OpenAI(config = openAIConfig).also { _openAI = it }
        } else _openAI

    suspend fun listModelIds(): ModelsResult {
        if (openAISettings.key.isEmpty()) {
            return ModelsResult.MissingToken
        }
        try {
            return openAI.models()
                .sortedByDescending { it.created }
                .map { it.id.id }.let { ModelsResult.Success(it) }
        } catch (e: Exception) {
            return ModelsResult.Error(ex = e)
        }
    }

    suspend fun summarize(content: String): SummaryResult {
        val request = ChatCompletionRequest(
            model = ModelId(id = openAISettings.modelId),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    messageContent = TextContent(
                        "You are assistant inside RSS reader app. " +
                        "You are helping summarize content of an article. " +
                        "App language is '${Locale.getDefault().isO3Language}'. " +
                        "Provide summary in the language of the article, " +
                        "if the language can be recognized for 99% use language of this app." +
                        "Format response as JSON: In key 'lang' return article language ISO code, in key 'content' text of the summary (Example: { \"lang\": \"en\", \"content\": \"a summary\" })." +
                        "For readability of summary content use bullet points, titles and new lines using plain text only (no markdown or html) when it's appropriate. "
                    )
                ),
                ChatMessage(
                    role = ChatRole.User,
                    messageContent = TextContent("Summarize:\n\n$content")
                )
            ),
            responseFormat = ChatResponseFormat.JsonObject
        )
        try {
            val response = openAI.chatCompletion(
                request = request,
                requestOptions = null
            )
            val summaryResponse: SummaryResponse = response.choices.firstOrNull()?.message?.content?.let { text ->
                Json.decodeFromString(text)
            } ?: throw IllegalStateException("Response content is null")

            return SummaryResult.Success(
                id = response.id,
                model = response.model.id,
                content = summaryResponse.content,
                created = response.created,
                promptTokens = response.usage?.promptTokens ?: 0,
                completeTokens = response.usage?.completionTokens ?: 0,
                totalTokens = response.usage?.completionTokens ?: 0,
                detectedLanguage = summaryResponse.lang
            )
        } catch (e: Exception) {
            return SummaryResult.Error(content = e.message ?: "Unknown error")
        }
    }
}

private fun OpenAISettings.toOpenAIConfig(): OpenAIConfig = OpenAIConfig(
    token = key,
    logging = LoggingConfig(logLevel = LogLevel.All),
    host = toOpenAIHost()
)

fun OpenAISettings.toOpenAIHost(): OpenAIHost = baseUrl.let { baseUrl ->
    if (baseUrl.isEmpty()) {
        OpenAIHost.OpenAI
    } else OpenAIHost(
        baseUrl = baseUrl,
        queryParams = azureApiVersion.let { apiVersion ->
            if (apiVersion.isEmpty()) emptyMap() else mapOf("api-version" to apiVersion)
        }
    )
}

fun OpenAIHost.toUrlString(): String = baseUrl + if (queryParams.isEmpty()) "" else "?" + queryParams.map { "${it.key}=${it.value}" }

private fun OpenAIConfig.toOpenAISettings(modelId: String): OpenAISettings = OpenAISettings(
    key = token,
    modelId = modelId,
    baseUrl = if (host == OpenAIHost.OpenAI) "" else host.baseUrl,
    azureApiVersion = host.queryParams.get("api-version") ?: ""
)
