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
import com.nononsenseapps.feeder.BuildConfig
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.archmodel.Repository
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.url
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private fun OpenAISettings.toOpenAIConfig(): OpenAIConfig =
    OpenAIConfig(
        token = key,
        logging = LoggingConfig(logLevel = LogLevel.Headers, sanitize = !BuildConfig.DEBUG),
        host = toOpenAIHost(withAzureDeploymentId = false),
        httpClientConfig = {
            if (isAzure) {
                install(HttpSend)
                install("azure-interceptor") {
                    plugin(HttpSend).intercept { request ->
                        request.headers.remove("Authorization")
                        request.headers.append("api-key", key)
                        // models path doesn't include azureDeploymentId
                        val path = request.url.pathSegments.takeLastWhile { it != "openai" || it.isEmpty() }
                        val url =
                            toOpenAIHost(withAzureDeploymentId = path.last() != "models")
                                .toUrl()
                                .appendPathSegments(path)
                                .build()
                        request.url(url)
                        execute(request)
                    }
                }
            }
        },
    )

class OpenAIApi(
    private val repository: Repository,
    private val appLang: String,
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
            val detectedLanguage: String,
        ) : SummaryResult

        data class Error(override val content: String) : SummaryResult
    }

    sealed interface ModelsResult {
        data object MissingToken : ModelsResult

        data object AzureApiVersionRequired : ModelsResult

        data object AzureDeploymentIdRequired : ModelsResult

        data class Success(val ids: List<String>) : ModelsResult

        data class Error(val message: String?) : ModelsResult
    }

    private val openAISettings: OpenAISettings
        get() = repository.openAISettings.value

    private val openAI: OpenAI
        get() = OpenAI(config = openAISettings.toOpenAIConfig())

    suspend fun listModelIds(settings: OpenAISettings): ModelsResult {
        if (settings.key.isEmpty()) {
            return ModelsResult.MissingToken
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
            OpenAI(config = settings.toOpenAIConfig()).models()
                .sortedByDescending { it.created }
                .map { it.id.id }.let { ModelsResult.Success(it) }
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
                response.choices.firstOrNull()?.message?.content?.let { text ->
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
                detectedLanguage = summaryResponse.lang,
            )
        } catch (e: Exception) {
            return SummaryResult.Error(content = e.message ?: e.cause?.message ?: "")
        }
    }

    private fun summaryRequest(content: String): ChatCompletionRequest {
        return ChatCompletionRequest(
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
                                    "Format response as JSON: { \"lang\": \"ISO code\", \"content\": \"summary\" }.",
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
            responseFormat = ChatResponseFormat.JsonObject,
        )
    }
}

val OpenAISettings.isAzure: Boolean
    get() = baseUrl.contains("openai.azure.com", ignoreCase = true)

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
                        .takeFrom(baseUrl).also {
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
        .takeFrom(baseUrl).also {
            queryParams.forEach { (k, v) -> it.parameters.append(k, v) }
        }
