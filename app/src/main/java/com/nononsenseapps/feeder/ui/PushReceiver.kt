package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.unifiedpush.android.connector.MessagingReceiver

class PushReceiver : MessagingReceiver() {
    // https://ntfy.sh/up0xKJNXvQFH6W?up=1
    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        Log.d("UNIFIED", "onMessage: ${message.decodeToString()}, $instance")
    }

    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        Log.d("UNIFIED", "onNewEndpoint: $endpoint, $instance")
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("UNIFIED", "onReceive")
        super.onReceive(context, intent)
    }

    override fun onRegistrationFailed(context: Context, instance: String) {
        Log.d("UNIFIED", "onRegistrationFailed: $instance")
    }

    override fun onUnregistered(context: Context, instance: String) {
        Log.d("UNIFIED", "onUnregistered: $instance")
    }
}

fun DIAware.sendPushMessage(msg: String) {
    val okHttpClient by instance<OkHttpClient>()

    val bytes = msg.encodeToByteArray()
    val body = bytes.toRequestBody()
    val request = Request.Builder()
        .url("https://ntfy.sh/up0xKJNXvQFH6W?up=1")
        .post(body)
        .build()

    try {
        Log.d("UNIFIED", "Posting ${bytes.count()} bytes")
        okHttpClient.newCall(request).execute().use { response ->
            Log.d("UNIFIED", "Response: ${response.body?.string()}")
        }
    } catch (e: Exception) {
        Log.e("UNIFIED", "BOOM", e)
    }
}
