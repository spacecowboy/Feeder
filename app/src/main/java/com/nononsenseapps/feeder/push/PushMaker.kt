package com.nononsenseapps.feeder.push

import android.util.Log
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class PushMaker(override val di: DI) : DIAware {
    private val okHttpClient by instance<OkHttpClient>()

    suspend fun send(endpoint: String, bytes: ByteArray): Boolean {
        val body = bytes.toRequestBody()
        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        try {
            logDebug(LOG_TAG, "Posting ${bytes.count()} bytes")
            withContext(Dispatchers.IO) {
                okHttpClient.newCall(request).execute().use { response ->
                    logDebug(LOG_TAG, "Response: ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to send update", e)
            return false
        }

        return true
    }

    companion object {
        const val LOG_TAG = "FEEDER_PUSH"
    }
}
