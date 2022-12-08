package com.nononsenseapps.feeder.push

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.util.logDebug
import java.util.concurrent.TimeUnit
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
            if (pushMaker.send(message.toEndpoint, message.body)) {
                toDelete.add(message.id)
            } else {
                // TODO what if messages never are able to be sent? Then it must be possible for them to
                // get cleared somehow. Maybe a retry count on the message?
                success = false
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

fun scheduleSendPush(di: DI) {
    logDebug(PushWorker.LOG_TAG, "scheduleSendPush")
    val repository by di.instance<Repository>()

    val constraints = Constraints.Builder()
        // This prevents expedited if true
        .setRequiresCharging(repository.syncOnlyWhenCharging.value)

    if (repository.syncOnlyOnWifi.value) {
        constraints.setRequiredNetworkType(NetworkType.UNMETERED)
    } else {
        constraints.setRequiredNetworkType(NetworkType.CONNECTED)
    }

    val workRequest = OneTimeWorkRequestBuilder<PushWorker>()
        .addTag("feeder")
        .keepResultsForAtLeast(5, TimeUnit.MINUTES)
        .setConstraints(constraints.build())
        .setInitialDelay(5, TimeUnit.SECONDS)

    val workManager by di.instance<WorkManager>()
    workManager.enqueueUniqueWork(
        PushWorker.UNIQUE_PUSH_NAME,
        ExistingWorkPolicy.REPLACE,
        workRequest.build()
    )
}

fun schedulePeriodicProofOfLife(di: DI) {
    logDebug(PushWorker.LOG_TAG, "schedulePeriodicProofOfLife")
    val repository by di.instance<Repository>()

    val constraints = Constraints.Builder()
        // This prevents expedited if true
        .setRequiresCharging(repository.syncOnlyWhenCharging.value)

    if (repository.syncOnlyOnWifi.value) {
        constraints.setRequiredNetworkType(NetworkType.UNMETERED)
    } else {
        constraints.setRequiredNetworkType(NetworkType.CONNECTED)
    }

    val workRequest = PeriodicWorkRequestBuilder<PushWorker>(
        24,
        TimeUnit.HOURS,
        12,
        TimeUnit.HOURS
    )
        .addTag("feeder")
        .keepResultsForAtLeast(5, TimeUnit.MINUTES)
        .setConstraints(constraints.build())
        .setInputData(
            Data.Builder()
                .putBoolean(PushWorker.UNIQUE_PROOF_OF_LIFE_NAME, true)
                .build()
        )

    val workManager by di.instance<WorkManager>()
    workManager.enqueueUniquePeriodicWork(
        PushWorker.UNIQUE_PROOF_OF_LIFE_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest.build()
    )
}
