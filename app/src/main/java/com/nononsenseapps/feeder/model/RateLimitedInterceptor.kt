package com.nononsenseapps.feeder.model

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that handles Too Many Requests (429) responses for Coil.
 *
 * It intercepts requests, checking if a 429 timeout was received. If so, it cancels the request.
 */
object RateLimitedInterceptor : Interceptor {
    private const val LOG_TAG = "FEEDER_RATEINTER"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return RateLimiter.blockingRateLimited(request.url.host) {
            chain.proceed(request)
        }
    }
}
