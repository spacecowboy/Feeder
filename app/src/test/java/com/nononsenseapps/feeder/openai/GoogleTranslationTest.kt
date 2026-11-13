package com.nononsenseapps.feeder.openai

import com.nononsenseapps.feeder.archmodel.OpenAISettings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleTranslationTest {
    @Test
    fun languageAliases() {
        assertEquals("en", googleLanguageCode("English"))
        assertEquals("zh-CN", googleLanguageCode("zh"))
        assertEquals("zh-TW", googleLanguageCode("zh-Hant"))
        assertEquals("iw", googleLanguageCode("he"))
    }

    @Test
    fun responseParsing() {
        assertEquals("Bonjour", googleFirstTranslation(Json.parseToJsonElement("[[\"Bonjour\",\"hello\"],\"en\"]")))
        assertNull(googleFirstTranslation(Json.parseToJsonElement("[[],null]")))
    }

    @Test
    fun translatesTextAndSendsExpectedParameters() =
        runTest {
            MockWebServer().use { server ->
                server.enqueue(MockResponse().setBody("[[\"你好\",\"en\"]]"))
                val api = api(server)
                val result = api.translate("hello", "zh", settings())
                assertEquals(OpenAIApi.TranslationResult.Success("你好", ""), result)
                val request = server.takeRequest()
                assertEquals("dict-chrome-ex", request.requestUrl?.queryParameter("client"))
                assertEquals("auto", request.requestUrl?.queryParameter("sl"))
                assertEquals("zh-CN", request.requestUrl?.queryParameter("tl"))
                assertEquals("hello", request.requestUrl?.queryParameter("q"))
            }
        }

    @Test
    fun preservesHtmlAndDoesNotTranslateCode() =
        runTest {
            MockWebServer().use { server ->
                server.enqueue(MockResponse().setBody("[[\"Bonjour\",\"en\"]]"))
                val result =
                    api(server).translate(
                        "<p> Hello </p><pre><span>code</span></pre>",
                        "fr",
                        settings(),
                        preserveHtml = true,
                    )
                assertTrue(result is OpenAIApi.TranslationResult.Success)
                val document = Jsoup.parseBodyFragment(result.content)
                assertEquals(" Bonjour ", document.selectFirst("p")?.wholeText())
                assertEquals("code", document.selectFirst("pre span")?.text())
                assertEquals(1, server.requestCount)
            }
        }

    @Test
    fun reportsHttpErrors() =
        runTest {
            MockWebServer().use { server ->
                server.enqueue(MockResponse().setResponseCode(429))
                val result = api(server).translate("hello", "fr", settings())
                assertTrue(result is OpenAIApi.TranslationResult.Error)
                assertTrue(result.content.contains("429"))
            }
        }

    @Test
    fun chunksLongText() =
        runTest {
            MockWebServer().use { server ->
                server.enqueue(MockResponse().setBody("[[\"first\",\"en\"]]"))
                server.enqueue(MockResponse().setBody("[[\"second\",\"en\"]]"))
                val result = api(server).translate("x".repeat(2001), "fr", settings())
                assertEquals(OpenAIApi.TranslationResult.Success("firstsecond", ""), result)
                assertEquals(2, server.requestCount)
            }
        }

    private fun settings() = OpenAISettings(baseUrl = GOOGLE_TRANSLATION_PROVIDER_URL)

    private fun api(server: MockWebServer) =
        OpenAIApi(
            appLang = "en",
            openAIClientFactory = { error("Unexpected OpenAI request") },
        ).apply { googleTranslationUrl = server.url("/translate_a/t").toString() }
}
