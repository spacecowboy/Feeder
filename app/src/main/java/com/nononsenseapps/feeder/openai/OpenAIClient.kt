package com.nononsenseapps.feeder.openai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.core.RequestOptions
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.Model
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.nononsenseapps.feeder.BuildConfig
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.url
import io.ktor.http.appendPathSegments
import kotlin.time.Duration.Companion.seconds

interface OpenAIClient {
    suspend fun models(requestOptions: RequestOptions? = null): List<Model>

    suspend fun chatCompletion(
        request: ChatCompletionRequest,
        requestOptions: RequestOptions?,
    ): ChatCompletion
}

class OpenAIClientDefault(
    settings: OpenAISettings,
) : OpenAIClient {
    private val client = OpenAI(config = settings.toOpenAIConfig())

    override suspend fun models(requestOptions: RequestOptions?): List<Model> = client.models(requestOptions)

    override suspend fun chatCompletion(
        request: ChatCompletionRequest,
        requestOptions: RequestOptions?,
    ): ChatCompletion = client.chatCompletion(request, requestOptions)
}

private fun OpenAISettings.toOpenAIConfig(): OpenAIConfig =
    OpenAIConfig(
        token = key,
        timeout = Timeout(socket = timeout.toInt().seconds),
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
