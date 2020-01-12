package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.di.networkModule
import com.nononsenseapps.jsonfeed.cachingHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeedParserClientTest : KodeinAware {
    override val kodein by Kodein.lazy {
        bind<OkHttpClient>() with singleton {
            cachingHttpClient()
                    .newBuilder()
                    .addNetworkInterceptor(UserAgentInterceptor)
                    .build()
        }
        import(networkModule)
    }
    val server = MockWebServer()
    private val feedParser: FeedParser by instance()

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
<?xml version='1.0' encoding='UTF-8'?>
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
            val feed = feedParser.parseFeedUrl(url)
            assertEquals("No auth", feed?.title)
        }
        assertNull(server.takeRequest().headers.get("Authorization"),
                message = "First request is done with no auth")
        assertNotNull(server.takeRequest().headers.get("Authorization"),
                message = "After a 401 a new request is made with auth")
    }

    @Test
    fun reasonableUserAgentIsPassed() {
        server.enqueue(MockResponse().apply {
            setResponseCode(403)
        })

        // Some feeds return 403 unless they get a user-agent
        val url = server.url("/foo").url()

        runBlocking {
            launch {
                try {
                    feedParser.parseFeedUrl(url)
                } catch (e: Throwable) {
                    // meh
                }
            }

            val headers = withContext(Dispatchers.IO) {
                server.takeRequest().headers
            }

            val userAgents = headers.toMultimap()["User-Agent"]

            assertEquals(1, userAgents?.size)

            val userAgent = userAgents?.first()

            assertTrue(
                    userAgent!!.startsWith("Feeder")
            )
        }
    }
}
