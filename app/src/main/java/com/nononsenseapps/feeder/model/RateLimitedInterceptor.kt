package com.nononsenseapps.feeder.model

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that prevents more than one request to a host at a time
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
