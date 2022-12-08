package com.nononsenseapps.feeder.push

import android.content.Context
import android.util.Log
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import org.unifiedpush.android.connector.MessagingReceiver

class PushReceiver : MessagingReceiver() {

    // https://ntfy.sh/up0xKJNXvQFH6W?up=1
    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        logDebug(LOG_TAG, "onMessage: ${message.decodeToString()}, $instance")

        try {
            val di by closestDI(context)
            val pushHandler by di.instance<PushHandler>()
            runBlocking(Dispatchers.IO) {
                pushHandler.onUpdate(Update.ADAPTER.decode(message))
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to handle message", e)
        }
    }

    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        logDebug(LOG_TAG, "onNewEndpoint: $endpoint, $instance")

        val di by closestDI(context)
        val repository by di.instance<Repository>()

        runBlocking(Dispatchers.IO) {
            repository.updateThisDeviceEndpoint(endpoint = endpoint)
        }
    }

    override fun onRegistrationFailed(context: Context, instance: String) {
        logDebug(LOG_TAG, "onRegistrationFailed: $instance")
        val di by closestDI(context)
        val pushStore by di.instance<PushStore>()

        runBlocking(Dispatchers.IO) {
            pushStore._setDistributorValue("")
            pushStore.setRegistrationFailed(true)
        }
    }

    override fun onUnregistered(context: Context, instance: String) {
        logDebug(LOG_TAG, "onUnregistered: $instance")
        val di by closestDI(context)
        val pushStore by di.instance<PushStore>()

        runBlocking(Dispatchers.IO) {
            pushStore._setDistributorValue("")
        }
    }

    companion object {
        const val LOG_TAG = "FEEDER_PUSH_RECEIVER"
    }
}
