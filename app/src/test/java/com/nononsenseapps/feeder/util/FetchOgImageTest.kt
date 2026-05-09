package com.nononsenseapps.feeder.util

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import kotlin.test.assertEquals

class FetchOgImageTest {
    @Test
    fun resolvesRelativeOgImageAgainstRedirectTarget() =
        runBlocking {
            val originServer = MockWebServer()
            val destinationServer = MockWebServer()

            try {
                originServer.start()
                destinationServer.start()

                destinationServer.enqueue(
                    MockResponse().setBody(
                        """
                        <html><head>
                        <meta property="og:image" content="/images/hero.jpg">
                        </head><body></body></html>
                        """.trimIndent(),
                    ),
                )
                originServer.enqueue(
                    MockResponse()
                        .setResponseCode(302)
                        .addHeader("Location", destinationServer.url("/articles/final")),
                )

                val image =
                    OkHttpClient()
                        .fetchOgImage(originServer.url("/start").toString())

                assertEquals(destinationServer.url("/images/hero.jpg").toString(), image?.url)
            } finally {
                originServer.shutdown()
                destinationServer.shutdown()
            }
        }
}
