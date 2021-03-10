package com.nononsenseapps.feeder.ui

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import kotlinx.coroutines.delay
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.direct
import org.kodein.di.generic.instance

class CustomTabsWarmer(override val kodein: Kodein) : KodeinAware {
    private var customClient: CustomTabsClient? = null
    private var customSession: CustomTabsSession? = null

    init {
        // This leaks - make it more static and reusable
        val conn = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName?, client: CustomTabsClient?) {
                customClient = client
                client?.warmup(0)
                customSession = client?.newSession(object : CustomTabsCallback() {})
            }

            override fun onServiceDisconnected(name: ComponentName) {
            }
        }

        CustomTabsClient.bindCustomTabsService(
            kodein.direct.instance(),
            "com.android.chrome",
            conn
        )
    }

    suspend fun preLoad(linkProvider: () -> Uri?) {
        var time = 50L
        while (customSession == null || linkProvider() == null) {
            delay(time)
            time *= 2
            if (time > 1000L) {
                // Give up
                return
            }
        }

        customSession?.mayLaunchUrl(linkProvider(), Bundle.EMPTY, emptyList())
    }
}
