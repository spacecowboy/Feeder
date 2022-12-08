package com.nononsenseapps.feeder.model.workmanager

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.model.workmanager.SyncServiceSendReadWorker.Companion.UNIQUE_SENDREAD_NAME
import com.nononsenseapps.feeder.sync.SyncRestClient
import com.nononsenseapps.feeder.util.logDebug
import java.util.concurrent.TimeUnit
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

class SyncServiceSendReadWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), DIAware {
    override val di: DI by closestDI(context)

    private val notificationManager: NotificationManagerCompat by instance()
    private val syncClient: SyncRestClient by di.instance()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(context, notificationManager)
    }

    override suspend fun doWork(): Result {
        return try {
            logDebug(LOG_TAG, "Doing work")
            syncClient.markAsRead()
            Result.success()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error when sending read marks", e)
            Result.failure()
        }
    }

    companion object {
        const val LOG_TAG = "FEEDER_SENDREAD"
        const val UNIQUE_SENDREAD_NAME = "feeder_sendread_onetime"
    }
}

fun scheduleSendRead(di: DI) {
    return
    logDebug(SyncServiceSendReadWorker.LOG_TAG, "Scheduling work")
    val repository by di.instance<Repository>()

    val constraints = Constraints.Builder()
        // This prevents expedited if true
        .setRequiresCharging(repository.syncOnlyWhenCharging.value)

    if (repository.syncOnlyOnWifi.value) {
        constraints.setRequiredNetworkType(NetworkType.UNMETERED)
    } else {
        constraints.setRequiredNetworkType(NetworkType.CONNECTED)
    }

    val workRequest = OneTimeWorkRequestBuilder<SyncServiceSendReadWorker>()
        .addTag("feeder")
        .keepResultsForAtLeast(5, TimeUnit.MINUTES)
        .setConstraints(constraints.build())
        .setInitialDelay(10, TimeUnit.SECONDS)

    val workManager by di.instance<WorkManager>()
    workManager.enqueueUniqueWork(
        UNIQUE_SENDREAD_NAME,
        ExistingWorkPolicy.REPLACE,
        workRequest.build()
    )
}
