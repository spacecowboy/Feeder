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
import com.nononsenseapps.feeder.archmodel.Repository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

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
    @MockK
    private lateinit var repository: Repository

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { repository.openAISettings } returns MutableStateFlow(OpenAISettings())
    }

    @Test
    fun testSummaryResponseLangNoQuotes() =
        runTest {
            val chatCompletion = createResponse("Lang: eng\n\nMy summary")
            val api = createApi(response = chatCompletion)
            val actual = api.summarize("My content")
            val expected = createResult("My summary", "eng")
            assertEquals(expected, actual)
        }

    @Test
    fun testSummaryResponseLangWithQuotes() =
        runTest {
            val chatCompletion = createResponse("Lang: \"FR\"\nMy summary")
            val api = createApi(response = chatCompletion)
            val actual = api.summarize("My content")
            val expected = createResult("My summary", "FR")
            assertEquals(expected, actual)
        }

    private fun createApi(response: ChatCompletion) = OpenAIApi(repository, "lang", { OpenAIClientMock(response) })

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
