package com.nononsenseapps.feeder.openai

import com.aallam.openai.api.chat.ChatChoice
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.core.RequestOptions
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OpenAIClientMock(
    private val chatCompletion: ChatCompletion,
) : OpenAIClient {
    override suspend fun models(requestOptions: RequestOptions?): List<Model> = emptyList()

    override suspend fun chatCompletion(
        request: ChatCompletionRequest,
        requestOptions: RequestOptions?,
    ): ChatCompletion = chatCompletion
}

class OpenAIApiTest {
    @Test
    fun testSummaryResponseLangNoQuotes() =
        runTest {
            val chatCompletion = createResponse("Lang: eng\n\nMy summary")
            val api = createApi(response = chatCompletion)
            val actual = api.summarize("My content", OpenAISettings(key = "test", modelId = "test-model-id"))
            val expected = createResult("My summary", "eng")
            assertEquals(expected, actual)
        }

    @Test
    fun testSummaryResponseLangWithQuotes() =
        runTest {
            val chatCompletion = createResponse("Lang: \"FR\"\nMy summary")
            val api = createApi(response = chatCompletion)
            val actual = api.summarize("My content", OpenAISettings(key = "test", modelId = "test-model-id"))
            val expected = createResult("My summary", "FR")
            assertEquals(expected, actual)
        }

    @Test
    fun deepLVerificationUsesAuthorizationHeader() =
        runTest {
            MockWebServer().use { server ->
                server.enqueue(
                    MockResponse()
                        .addHeader("Content-Type", "application/json")
                        .setBody("""{"translations":[{"detected_source_language":"EN","text":"Hallo"}]}"""),
                )

                val result =
                    createTranslationApi().listModelIds(
                        OpenAISettings(
                            baseUrl = server.url("/api.deepl.com").toString(),
                            key = "deep-key",
                        ),
                    )

                assertEquals(OpenAIApi.ModelsResult.Success(ids = emptyList()), result)

                val request = server.takeRequest()
                assertEquals("/api.deepl.com/v2/translate", request.path)
                assertEquals("DeepL-Auth-Key deep-key", request.getHeader("Authorization"))
                assertTrue(request.body.readUtf8().contains("\"target_lang\":\"DE\""))
            }
        }

    @Test
    fun deepLTranslateUsesHtmlTagHandling() =
        runTest {
            MockWebServer().use { server ->
                server.enqueue(
                    MockResponse()
                        .addHeader("Content-Type", "application/json")
                        .setBody("""{"translations":[{"detected_source_language":"EN","text":"<p>Hallo</p>"}]}"""),
                )

                val result =
                    createTranslationApi().translate(
                        content = "<p>Hello</p>",
                        targetLanguage = "DE",
                        settings =
                            OpenAISettings(
                                baseUrl = server.url("/api.deepl.com").toString(),
                                key = "deep-key",
                            ),
                        preserveHtml = true,
                    )

                assertIs<OpenAIApi.TranslationResult.Success>(result)
                val request = server.takeRequest()
                assertEquals("DeepL-Auth-Key deep-key", request.getHeader("Authorization"))
                assertTrue(request.body.readUtf8().contains("\"tag_handling\":\"html\""))
            }
        }

    @Test
    fun deepLReturnsProviderSpecificHttpFailure() =
        runTest {
            MockWebServer().use { server ->
                server.enqueue(
                    MockResponse()
                        .setStatus("HTTP/1.1 403 Forbidden")
                        .setBody("""{"error":"denied"}"""),
                )

                val result =
                    createTranslationApi().translate(
                        content = "Hello",
                        targetLanguage = "DE",
                        settings =
                            OpenAISettings(
                                baseUrl = server.url("/api.deepl.com").toString(),
                                key = "deep-key",
                            ),
                    )

                val error = assertIs<OpenAIApi.TranslationResult.Error>(result)
                assertTrue(error.content.startsWith("DeepL request failed: HTTP 403"))
            }
        }

    private fun createApi(response: ChatCompletion) = OpenAIApi("lang") { OpenAIClientMock(response) }

    private fun createTranslationApi() = OpenAIApi("en") { error("OpenAI client should not be used for DeepL translation") }

    private fun createResponse(message: String) =
        ChatCompletion(
            id = "test",
            model = ModelId(id = "test-model-id"),
            created = 0,
            choices =
                listOf(
                    ChatChoice(
                        index = 0,
                        message =
                            ChatMessage(
                                role = ChatRole.Assistant,
                                content = message,
                            ),
                        finishReason = null,
                        logprobs = null,
                    ),
                ),
            usage = null,
            systemFingerprint = null,
        )

    private fun createResult(
        content: String,
        lang: String,
    ) = OpenAIApi.SummaryResult.Success(
        id = "test",
        created = 0,
        model = "test-model-id",
        content = content,
        promptTokens = 0,
        completeTokens = 0,
        totalTokens = 0,
        detectedLanguage = lang,
    )
}
