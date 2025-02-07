package com.nononsenseapps.feeder.model

import android.util.Log
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Interceptor that handles Too Many Requests (429) responses for Coil.
 *
 * It intercepts requests, checking if a 429 timeout was received. If so, it cancels the request.
 */
object TooManyRequestsInterceptor : Interceptor {
    private val tooManyResponses = ConcurrentHashMap<String, Instant>()
    private const val LOG_TAG = "FEEDER_TOOMANY"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val now = Instant.now()

        if (tooManyResponses[request.url.host]?.isAfter(now) == true) {
            Log.i(LOG_TAG, "Too many requests for ${request.url.host}, blocked until ${tooManyResponses[request.url.host]}")
            return chain.proceed(
                request
                    .newBuilder()
                    .cacheControl(
                        CacheControl
                            .Builder()
                            .onlyIfCached()
                            .maxStale(Int.MAX_VALUE, TimeUnit.SECONDS)
                            .maxAge(Int.MAX_VALUE, TimeUnit.SECONDS)
                            .build(),
                    ).build(),
            )
        }

        // Drop the host
        tooManyResponses.remove(request.url.host)
        val response = chain.proceed(request)

        if (response.code == 429) {
            tooManyResponses.computeIfAbsent(request.url.host) {
                val retryAfter = response.header("Retry-After")
                val retryAfterSeconds = retryAfter?.toIntOrNull() ?: 60
                Log.i(LOG_TAG, "Too many requests for ${request.url.host}, will intercept for $retryAfterSeconds seconds")

                now.plusSeconds(retryAfterSeconds.toLong() + 1)
            }
        }

        return response
    }
}
