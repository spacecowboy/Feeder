package com.nononsenseapps.feeder.model

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

// See discussion on https://gitlab.com/spacecowboy/Feeder/-/issues/590
// const val USER_AGENT_STRING = "Feeder / ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
const val USER_AGENT_STRING = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.5195.136 Mobile Safari/537.36"
