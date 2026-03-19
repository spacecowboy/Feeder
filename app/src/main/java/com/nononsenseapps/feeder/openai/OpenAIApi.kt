package com.nononsenseapps.feeder.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAIHost
import com.nononsenseapps.feeder.archmodel.OpenAISettings
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

    @Serializable
    private data class GoogleTranslateRequest(
        val q: List<String>,
        val target: String,
        val format: String? = null,
    )

    @Serializable
    private data class GoogleTranslation(
        val translatedText: String,
        val detectedSourceLanguage: String? = null,
    )

    @Serializable
    private data class GoogleTranslateData(
        val translations: List<GoogleTranslation>,
    )

    @Serializable
    private data class GoogleTranslateResponse(
        val data: GoogleTranslateData,
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
        private val LANG_REGEX = Regex("^Lang: \"?([a-zA-Z_-]+)\"?$")
    }

    private fun okHttpClient(timeoutSeconds: Int): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(timeoutSeconds.coerceIn(30, 600).toLong(), TimeUnit.SECONDS)
            .build()

    suspend fun listModelIds(settings: OpenAISettings): ModelsResult {
        if (settings.key.isEmpty()) {
            return ModelsResult.MissingToken
        }
        if (settings.isDeepL) {
            return verifyDeepLSettings(settings)
        }
        if (settings.isGoogleTranslate) {
            return verifyGoogleTranslateSettings(settings)
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

    private fun verifyDeepLSettings(settings: OpenAISettings): ModelsResult {
        return try {
            okHttpClient(settings.timeoutSeconds)
                .newCall(
                    Request.Builder()
                        .url(settings.toDeepLTranslateUrl())
                        .header("Authorization", "DeepL-Auth-Key ${settings.key}")
                        .header("Content-Type", "application/json")
                        .post(
                            json
                                .encodeToString(
                                    DeepLTranslateRequest(
                                        text = listOf("Hello"),
                                        target_lang = "DE",
                                    ),
                                ).toRequestBody("application/json".toMediaType()),
                        ).build(),
                ).execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        return ModelsResult.Error(
                            message = "DeepL verification failed: HTTP ${response.code}${response.message.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""}",
                        )
                    }

                    val deeplResponse =
                        json.decodeFromString<DeepLTranslateResponse>(
                            response.body?.string() ?: throw IllegalStateException("Response content is null"),
                        )

                    if (deeplResponse.translations.isEmpty()) {
                        ModelsResult.Error(message = "DeepL verification failed: no translation was returned")
                    } else {
                        ModelsResult.Success(ids = emptyList())
                    }
                }
        } catch (e: Exception) {
            ModelsResult.Error(message = e.message ?: e.cause?.message)
        }
    }

    private fun verifyGoogleTranslateSettings(settings: OpenAISettings): ModelsResult {
        return try {
            okHttpClient(settings.timeoutSeconds)
                .newCall(
                    Request.Builder()
                        .url(settings.toGoogleTranslateUrl())
                        .header("Content-Type", "application/json")
                        .post(
                            json
                                .encodeToString(
                                    GoogleTranslateRequest(
                                        q = listOf("Hello"),
                                        target = "de",
                                        format = "text",
                                    ),
                                ).toRequestBody("application/json".toMediaType()),
                        ).build(),
                ).execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        return ModelsResult.Error(
                            message = "Google Translate verification failed: HTTP ${response.code}${response.message.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""}",
                        )
                    }

                    val googleResponse =
                        json.decodeFromString<GoogleTranslateResponse>(
                            response.body?.string() ?: throw IllegalStateException("Response content is null"),
                        )

                    if (googleResponse.data.translations.isEmpty()) {
                        ModelsResult.Error(message = "Google Translate verification failed: no translation was returned")
                    } else {
                        ModelsResult.Success(ids = emptyList())
                    }
                }
        } catch (e: Exception) {
            ModelsResult.Error(message = e.message ?: e.cause?.message)
        }
    }

    suspend fun summarize(
        content: String,
        settings: OpenAISettings,
    ): SummaryResult {
        if (settings.isDeepL || settings.isGoogleTranslate) {
            return SummaryResult.Error(content = "Summarization is not supported for this translation-only provider")
        }
        try {
            val response =
                openAIClientFactory(settings).chatCompletion(
                    request = summaryRequest(content, settings),
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
                totalTokens = response.usage?.totalTokens ?: 0,
                detectedLanguage = summaryResponse.lang,
            )
        } catch (e: Exception) {
            return SummaryResult.Error(content = e.message ?: e.cause?.message ?: "")
        }
    }

    suspend fun translate(
        content: String,
        targetLanguage: String,
        settings: OpenAISettings,
        preserveHtml: Boolean = false,
    ): TranslationResult {
        if (settings.isDeepL) {
            return translateWithDeepL(
                settings = settings,
                content = content,
                targetLanguage = targetLanguage,
                preserveHtml = preserveHtml,
            )
        }
        if (settings.isGoogleTranslate) {
            return translateWithGoogle(
                settings = settings,
                content = content,
                targetLanguage = targetLanguage,
                preserveHtml = preserveHtml,
            )
        }
        try {
            val response =
                openAIClientFactory(settings).chatCompletion(
                    request = translationRequest(content, targetLanguage, settings, preserveHtml),
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
                totalTokens = response.usage?.totalTokens ?: 0,
                detectedLanguage = translationResponse.lang,
                targetLanguage = translationResponse.targetLang,
            )
        } catch (e: Exception) {
            return TranslationResult.Error(content = e.message ?: e.cause?.message ?: "")
        }
    }

    private fun translateWithDeepL(
        settings: OpenAISettings,
        content: String,
        targetLanguage: String,
        preserveHtml: Boolean,
    ): TranslationResult {
        return try {
            okHttpClient(settings.timeoutSeconds)
                .newCall(
                    Request.Builder()
                        .url(settings.toDeepLTranslateUrl())
                        .header("Authorization", "DeepL-Auth-Key ${settings.key}")
                        .header("Content-Type", "application/json")
                        .post(
                            json
                                .encodeToString(
                                    DeepLTranslateRequest(
                                        text = listOf(content),
                                        target_lang = targetLanguage.toDeepLTargetLanguageCode(),
                                        tag_handling = if (preserveHtml) "html" else null,
                                    ),
                                ).toRequestBody("application/json".toMediaType()),
                        ).build(),
                ).execute()
                .use { response ->
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
                }
        } catch (e: Exception) {
            TranslationResult.Error(content = e.message ?: e.cause?.message ?: "")
        }
    }

    private fun translateWithGoogle(
        settings: OpenAISettings,
        content: String,
        targetLanguage: String,
        preserveHtml: Boolean,
    ): TranslationResult {
        return try {
            okHttpClient(settings.timeoutSeconds)
                .newCall(
                    Request.Builder()
                        .url(settings.toGoogleTranslateUrl())
                        .header("Content-Type", "application/json")
                        .post(
                            json
                                .encodeToString(
                                    GoogleTranslateRequest(
                                        q = listOf(content),
                                        target = targetLanguage.toGoogleTargetLanguageCode(),
                                        format = if (preserveHtml) "html" else "text",
                                    ),
                                ).toRequestBody("application/json".toMediaType()),
                        ).build(),
                ).execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        return TranslationResult.Error(
                            content = "Google Translate request failed: HTTP ${response.code}${response.message.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""}",
                        )
                    }

                    val googleResponse =
                        json.decodeFromString<GoogleTranslateResponse>(
                            response.body?.string() ?: throw IllegalStateException("Response content is null"),
                        )

                    val translation = googleResponse.data.translations.firstOrNull()
                        ?: throw IllegalStateException("Google Translate returned no translations")

                    TranslationResult.Success(
                        id = "google",
                        model = "google-translate",
                        content = translation.translatedText,
                        created = 0L,
                        promptTokens = 0,
                        completeTokens = 0,
                        totalTokens = 0,
                        detectedLanguage = translation.detectedSourceLanguage.orEmpty(),
                        targetLanguage = targetLanguage.toGoogleTargetLanguageCode(),
                    )
                }
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

    private fun summaryRequest(
        content: String,
        settings: OpenAISettings,
    ): ChatCompletionRequest =
        ChatCompletionRequest(
            model = ModelId(id = settings.modelId),
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
        settings: OpenAISettings,
        preserveHtml: Boolean,
    ): ChatCompletionRequest =
        ChatCompletionRequest(
            model = ModelId(id = settings.modelId),
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
    get() = baseUrl.contains("deepl.com", ignoreCase = true)

val OpenAISettings.isGoogleTranslate: Boolean
    get() = baseUrl.contains("translation.googleapis.com", ignoreCase = true)

val OpenAISettings.isValid: Boolean
    get() =
        if (isDeepL || isGoogleTranslate) {
            key.isNotEmpty()
        } else {
            modelId.isNotEmpty() &&
                key.isNotEmpty() &&
                if (isAzure) azureApiVersion.isNotBlank() && azureDeploymentId.isNotBlank() else true
        }

val OpenAISettings.canSummarize: Boolean
    get() = isValid && !isDeepL && !isGoogleTranslate

val OpenAISettings.canTranslate: Boolean
    get() = isValid

val OpenAISettings.canUseAsTranslationApi: Boolean
    get() = canTranslate && (isDeepL || isGoogleTranslate)

val OpenAISettings.isBlankConfiguration: Boolean
    get() =
        key.isBlank() &&
            modelId.isBlank() &&
            baseUrl.isBlank() &&
            azureApiVersion.isBlank() &&
            azureDeploymentId.isBlank()

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
        .takeFrom(normalizedDeepLBaseUrl())
        .also {
            it.appendPathSegments("v2", "translate")
        }.buildString()

fun OpenAISettings.toGoogleTranslateUrl(): String =
    URLBuilder()
        .takeFrom(normalizedGoogleTranslateBaseUrl())
        .also {
            it.appendPathSegments("language", "translate", "v2")
            it.parameters.append("key", key)
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

private fun String.toGoogleTargetLanguageCode(): String =
    trim()
        .uppercase()
        .replace('-', '_')
        .let { normalized ->
            when (normalized) {
                "ENGLISH", "EN", "EN_US", "EN_GB", "ENGLISH_US", "ENGLISH_GB", "AMERICAN_ENGLISH", "BRITISH_ENGLISH" -> "en"
                "GERMAN", "DE" -> "de"
                "FRENCH", "FR" -> "fr"
                "SPANISH", "ES" -> "es"
                "PORTUGUESE", "PT", "PT_BR", "PT_PT", "PORTUGUESE_BR", "PORTUGUESE_PT", "BRAZILIAN_PORTUGUESE", "EUROPEAN_PORTUGUESE" -> "pt"
                "ITALIAN", "IT" -> "it"
                "DUTCH", "NL" -> "nl"
                "POLISH", "PL" -> "pl"
                "RUSSIAN", "RU" -> "ru"
                "JAPANESE", "JA" -> "ja"
                "CHINESE", "ZH", "ZH_CN", "ZH_TW" -> "zh"
                "CZECH", "CS" -> "cs"
                "DANISH", "DA" -> "da"
                "GREEK", "EL" -> "el"
                "FINNISH", "FI" -> "fi"
                "HUNGARIAN", "HU" -> "hu"
                "INDONESIAN", "ID" -> "id"
                "KOREAN", "KO" -> "ko"
                "LITHUANIAN", "LT" -> "lt"
                "LATVIAN", "LV" -> "lv"
                "NORWEGIAN", "NB", "NORWEGIAN_BOKMAL", "NO" -> "no"
                "ROMANIAN", "RO" -> "ro"
                "SLOVAK", "SK" -> "sk"
                "SLOVENIAN", "SL" -> "sl"
                "SWEDISH", "SV" -> "sv"
                "TURKISH", "TR" -> "tr"
                "UKRAINIAN", "UK" -> "uk"
                else -> normalized.lowercase().replace('_', '-')
            }
        }

private fun OpenAISettings.normalizedDeepLBaseUrl(): String {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/').removeSuffix("/v2/translate")
    val defaultBaseUrl =
        if (key.endsWith(":fx")) {
            "https://api-free.deepl.com"
        } else {
            "https://api.deepl.com"
        }

    return when {
        normalizedBaseUrl.isBlank() -> defaultBaseUrl
        normalizedBaseUrl.equals("https://api.deepl.com", ignoreCase = true) -> defaultBaseUrl
        normalizedBaseUrl.equals("https://api-free.deepl.com", ignoreCase = true) -> defaultBaseUrl
        else -> normalizedBaseUrl
    }
}

private fun OpenAISettings.normalizedGoogleTranslateBaseUrl(): String =
    baseUrl
        .trim()
        .trimEnd('/')
        .removeSuffix("/language/translate/v2")
        .ifBlank { "https://translation.googleapis.com" }
