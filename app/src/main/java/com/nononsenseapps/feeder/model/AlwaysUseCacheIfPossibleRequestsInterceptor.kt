package com.nononsenseapps.feeder.model

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Interceptor that tries to use the cache if possible
 */
object AlwaysUseCacheIfPossibleRequestsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain
                .request()
                .newBuilder()
                .cacheControl(
                    CacheControl
                        .Builder()
                        .maxStale(30, TimeUnit.DAYS)
                        .maxAge(7, TimeUnit.DAYS)
                        .build(),
                ).build(),
        )
}
