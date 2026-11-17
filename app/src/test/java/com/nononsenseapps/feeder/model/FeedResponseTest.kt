package com.nononsenseapps.feeder.model

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Call
import okhttp3.Credentials
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class FeedResponseTest {
    private val server = MockWebServer()
    private val client = OkHttpClient()

    @After
    fun tearDown() {
        server.shutdown()
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }

    @Test
    fun buffersSuccessfulResponseAndBoundsOverallCallTime() =
        runBlocking {
            var timeout = 0L
            val observingClient =
                client
                    .newBuilder()
                    .eventListener(
                        object : EventListener() {
                            override fun callStart(call: Call) {
                                timeout = call.timeout().timeoutNanos()
                            }
                        },
                    ).build()
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setHeader("ETag", "version").setBody("{\"title\":\"Feed\"}"))
            val url = server.url("/feed").toUrl()
            observingClient.getBufferedFeedResponse(url).use {
                assertEquals(url.toString(), it.request.url.toString())
                assertEquals("version", it.header("ETag"))
                assertEquals("{\"title\":\"Feed\"}", it.body.string())
            }
            assertEquals(TimeUnit.SECONDS.toNanos(60), timeout)
            awaitNoRunningCalls()
        }

    @Test
    fun preservesAuthenticationAndConditionalRefreshHeaders() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(401).setHeader("WWW-Authenticate", "Basic realm=feed"))
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("{}"))
            val url =
                server
                    .url("/private")
                    .newBuilder()
                    .username("user")
                    .password("password")
                    .build()
                    .toUrl()
            client.getBufferedFeedResponse(url, forceNetwork = true).use { assertEquals("{}", it.body.string()) }
            val initial = server.takeRequest(5, TimeUnit.SECONDS)!!
            val authenticated = server.takeRequest(5, TimeUnit.SECONDS)!!
            assertEquals("max-age=60", initial.getHeader("Cache-Control"))
            assertEquals(Credentials.basic("user", "password"), authenticated.getHeader("Authorization"))
            awaitNoRunningCalls()
        }

    @Test
    fun cancelsWhileWaitingForHeaders() =
        runBlocking {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val url = server.url("/headers").toUrl()
            val job = async(Dispatchers.Default) { client.getBufferedFeedResponse(url) }
            assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            withTimeout(2_000) { job.cancelAndJoin() }
            awaitNoRunningCalls()
        }

    @Test
    fun cancelsWhileReadingTheBody() =
        runBlocking {
            val reading = CompletableDeferred<Unit>()
            val observingClient =
                client
                    .newBuilder()
                    .eventListener(
                        object : EventListener() {
                            override fun responseBodyStart(call: Call) {
                                reading.complete(Unit)
                            }
                        },
                    ).build()
            server.enqueue(slowResponse())
            val job = async(Dispatchers.Default) { observingClient.getBufferedFeedResponse(server.url("/body").toUrl()) }
            withTimeout(5_000) { reading.await() }
            withTimeout(2_000) { job.cancelAndJoin() }
            awaitNoRunningCalls()
        }

    @Test
    fun rejectsErrorAndUnsupportedBodiesWithoutDownloadingThem() =
        runBlocking {
            for ((code, type) in listOf(429 to "application/json", 200 to "image/png")) {
                server.enqueue(slowResponse().setResponseCode(code).setHeader("Content-Type", type).setHeader("Retry-After", "120"))
                withTimeout(2_000) {
                    client.getBufferedFeedResponse(server.url("/rejected").toUrl()).use {
                        assertEquals(code, it.code)
                        assertEquals("120", it.header("Retry-After"))
                        assertEquals(type, it.body.contentType().toString())
                        assertEquals(0L, it.body.contentLength())
                    }
                }
            }
            awaitNoRunningCalls()
        }

    @Test
    fun retainsAnExplicitShorterClientTimeout() =
        runBlocking {
            server.enqueue(slowResponse())
            val timedClient = client.newBuilder().callTimeout(200, TimeUnit.MILLISECONDS).build()
            var timedOut = false
            withTimeout(2_000) {
                try {
                    timedClient.getBufferedFeedResponse(server.url("/timeout").toUrl()).close()
                } catch (_: IOException) {
                    timedOut = true
                }
            }
            assertTrue(timedOut)
            awaitNoRunningCalls()
        }

    private fun slowResponse(): MockResponse =
        MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("x".repeat(128))
            .throttleBody(1, 1, TimeUnit.SECONDS)

    private suspend fun awaitNoRunningCalls() =
        withTimeout(2_000) {
            while (client.dispatcher.runningCallsCount() != 0) delay(10)
        }
}
