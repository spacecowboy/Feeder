package com.nononsenseapps.feeder.model

import android.net.Uri
import android.util.Log
import coil3.intercept.Interceptor
import coil3.request.ImageResult
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Interceptor that ensures only one image request is made per host at a time.
 *
 * If a request is already in progress for a host, the new request suspends until the first one is done.
 *
 * This ensures that we don't flood a server with requests for images.
 *
 * And that cached responses are used when possible.
 */
object OneImageRequestPerHostInterceptor : Interceptor {
    private const val LOG_TAG = "FEEDER_IMAGE"

    // Uses Hash to bin hosts into separate locks so we don't accumulate too many locks
    private val inProgressHosts = ConcurrentHashMap<Int, Mutex>()
    private const val MAX_LOCKS = 8

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        if (chain.request.data !is String) {
            return chain.proceed()
        }

        val url = chain.request.data as String

        // It can be a data url
        if (!url.startsWith("http")) {
            return chain.proceed()
        }

        val uri =
            try {
                Uri.parse(url)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to parse url: $url", e)
                return chain.proceed()
            }
        val host = uri.host ?: return chain.proceed()

        // If we are already fetching an image from this host, wait for it to finish
        return inProgressHosts.computeIfAbsent(host.hashCode() % MAX_LOCKS) { Mutex() }.withLock {
            logDebug(LOG_TAG, "Loading image [$host] $url")
            chain.proceed()
        }
    }
}
