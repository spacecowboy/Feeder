package com.nononsenseapps.feeder.model

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.ARG_FEED_ID
import com.nononsenseapps.feeder.ui.ARG_FEED_TAG
import com.nononsenseapps.feeder.util.currentlyCharging
import com.nononsenseapps.feeder.util.currentlyConnected
import com.nononsenseapps.feeder.util.currentlyUnmetered
import java.util.concurrent.TimeUnit
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

const val ARG_FORCE_NETWORK = "force_network"

const val UNIQUE_PERIODIC_NAME = "feeder_periodic_3"
// Clear this for scheduler
val oldPeriodics = listOf(
    "feeder_periodic",
    "feeder_periodic_2"
)
const val UNIQUE_ONETIME_NAME = "feeder_sync_onetime"
const val PARALLEL_SYNC = "parallel_sync"
const val MIN_FEED_AGE_MINUTES = "min_feed_age_minutes"

fun isOkToSyncAutomatically(context: Context): Boolean {
    val di: DI by closestDI(context)
    val repository: Repository by di.instance()
    return (
        currentlyConnected(context) &&
            (!repository.syncOnlyWhenCharging.value || currentlyCharging(context)) &&
            (!repository.syncOnlyOnWifi.value || currentlyUnmetered(context))
        )
}

class FeedSyncer(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), DIAware {
    override val di: DI by closestDI(context)

    private val notificationManager: NotificationManagerCompat by instance()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val syncingText = context.getString(R.string.syncing)

        val notification = NotificationCompat.Builder(applicationContext, syncChannelId)
            .setContentTitle(syncingText)
            .setTicker(syncingText)
//            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_stat_f)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
//            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                syncNotificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            )
        } else {
            ForegroundInfo(syncNotificationId, notification)
        }
    }

    /**
     * This is safe to call multiple times
     */
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = context.getString(R.string.sync_status)
        val description = context.getString(R.string.sync_status)

        val channel = NotificationChannel(syncChannelId, name, NotificationManager.IMPORTANCE_LOW)
        channel.description = description

        notificationManager.createNotificationChannel(channel)
    }

    override suspend fun doWork(): Result {
        val goParallel = inputData.getBoolean(PARALLEL_SYNC, false)

        var success: Boolean

        try {
            val feedId = inputData.getLong(ARG_FEED_ID, ID_UNSET)
            val feedTag = inputData.getString(ARG_FEED_TAG) ?: ""
            val forceNetwork = inputData.getBoolean(ARG_FORCE_NETWORK, false)
            val minFeedAgeMinutes = inputData.getInt(MIN_FEED_AGE_MINUTES, 5)

            success = syncFeeds(
                context = applicationContext,
                feedId = feedId,
                feedTag = feedTag,
                forceNetwork = forceNetwork,
                parallel = goParallel,
                minFeedAgeMinutes = minFeedAgeMinutes
            )
        } catch (e: Exception) {
            success = false
            Log.e("FeederFeedSyncer", "Failure during sync", e)
        } finally {
            // Send notifications for configured feeds
            notify(applicationContext)
        }

        return when (success) {
            true -> Result.success()
            false -> Result.failure()
        }
    }

    companion object {
        private const val syncNotificationId = 42623
        private const val syncChannelId = "feederSyncNotifications"
    }
}

fun requestFeedSync(
    di: DI,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    forceNetwork: Boolean = false,
    parallel: Boolean = false
) {
    val workRequest = OneTimeWorkRequestBuilder<FeedSyncer>()
        .addTag("feeder")
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .keepResultsForAtLeast(5, TimeUnit.MINUTES)

    val data = workDataOf(
        ARG_FEED_ID to feedId,
        ARG_FEED_TAG to feedTag,
        PARALLEL_SYNC to parallel,
        ARG_FORCE_NETWORK to forceNetwork
    )

    workRequest.setInputData(data)
    val workManager by di.instance<WorkManager>()
    workManager.enqueueUniqueWork(
        UNIQUE_ONETIME_NAME,
        ExistingWorkPolicy.KEEP,
        workRequest.build()
    )
}
