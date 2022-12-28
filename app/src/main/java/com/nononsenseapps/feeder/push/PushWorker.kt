package com.nononsenseapps.feeder.push

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.util.logDebug
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import org.threeten.bp.Instant

class PushWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), DIAware {

    override val di: DI by closestDI(context)

    private val repository by instance<Repository>()
    private val pushMaker by instance<PushMaker>()

    override suspend fun doWork(): Result {
        return when {
            inputData.getBoolean(UNIQUE_PROOF_OF_LIFE_NAME, false) -> sendProofOfLife()
            else -> sendQueuedMessages()
        }
    }

    private suspend fun sendProofOfLife(): Result {
        val senderEndpoint = repository.getThisDeviceEndpoint()
            ?: throw RuntimeException("Can't be part of a sync chain if thisDevice is null")

        val body = Update(
            sender = Device(
                endpoint = senderEndpoint,
            ),
            timestamp = Instant.now().toProto(),
            proof_of_life = ProofOfLife(),
        ).encode()

        for (device in repository.getKnownDevices()) {
            pushMaker.send(device.endpoint, body)
        }

        return Result.success()
    }

    private suspend fun sendQueuedMessages(): Result {
        var success = true
        val toDelete = mutableListOf<Long>()
        for (message in repository.getMessagesInQueue()) {
            if (message.body.size >= 4000) {
                val update = Update.ADAPTER.decode(message.body)
                val suffix = when {
                    update.devices != null -> "${update.devices.devices.size} devices"
                    update.feeds != null -> "${update.feeds.feeds.size} feeds"
                    update.read_marks != null -> "${update.read_marks.read_marks.size} read marks"
                    update.deleted_feeds != null -> "${update.deleted_feeds.deleted_feeds.size} deleted feeds"
                    update.deleted_devices != null -> "${update.deleted_devices.deleted_devices.size} deleted devices"
                    update.proof_of_life != null -> "proof of life"
                    update.snapshot_request != null -> "snapshot request"
                    else -> "UNKNOWN UPDATE TYPE?!"
                }
                logDebug(LOG_TAG, "WHOAH. Message is ${message.body.size}, trying to send $suffix")
                toDelete.add(message.id)
            } else {
                if (pushMaker.send(message.toEndpoint, message.body)) {
                    toDelete.add(message.id)
                } else {
                    // TODO what if messages never are able to be sent? Then it must be possible for them to
                    // get cleared somehow. Maybe a retry count on the message?
                    success = false
                }
            }
        }

        repository.deleteMessagesInQueue(toDelete)

        return when (success) {
            true -> Result.success()
            false -> Result.failure()
        }
    }

    companion object {
        const val LOG_TAG = "FEEDER_PUSHWORKER"
        const val UNIQUE_PUSH_NAME = "feeder_push_onetime"
        const val UNIQUE_PROOF_OF_LIFE_NAME = "feeder_push_onetime"
    }
}
