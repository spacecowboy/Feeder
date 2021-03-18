package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

object UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request()
                .newBuilder()
                .header("User-Agent", USER_AGENT_STRING)
                .build()
        )
    }
}

const val USER_AGENT_STRING = "Feeder / ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
