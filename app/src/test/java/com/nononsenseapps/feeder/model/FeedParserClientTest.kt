package com.nononsenseapps.feeder.model

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeedParserClientTest {
    val server = MockWebServer()

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Before
    fun setup() {
        server.start()
    }

    @Test
    @Throws(Exception::class)
    fun noPasswordInAuthAlsoWorks() {
        server.enqueue(MockResponse().apply {
            setResponseCode(401)
        })
        server.enqueue(MockResponse().apply {
            setResponseCode(200)
            this.setBody("""
<feed xmlns="http://www.w3.org/2005/Atom">
	<title>No auth</title>
</feed>
            """.trimIndent())
        })

        val url = server.url("/foo").newBuilder().username("user").build().url()

        assertTrue {
            url.userInfo == "user"
        }

        runBlocking {
            val feed = FeedParser.parseFeedUrl(url)
            assertEquals("No auth", feed?.title)
        }
        assertNull(server.takeRequest().headers.get("Authorization"),
                message = "First request is done with no auth")
        assertNotNull(server.takeRequest().headers.get("Authorization"),
                message = "After a 401 a new request is made with auth")
    }
}
