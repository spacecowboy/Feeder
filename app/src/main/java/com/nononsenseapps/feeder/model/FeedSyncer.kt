package com.nononsenseapps.feeder.model

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.di.CURRENTLY_SYNCING_STATE
import com.nononsenseapps.feeder.ui.ARG_FEED_ID
import com.nononsenseapps.feeder.ui.ARG_FEED_TAG
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.currentlyCharging
import com.nononsenseapps.feeder.util.currentlyConnected
import com.nononsenseapps.feeder.util.currentlyUnmetered
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.util.concurrent.TimeUnit

const val ARG_FORCE_NETWORK = "force_network"

const val UNIQUE_PERIODIC_NAME = "feeder_periodic"
const val PARALLEL_SYNC = "parallel_sync"
const val MIN_FEED_AGE_MINUTES = "min_feed_age_minutes"
const val IGNORE_CONNECTIVITY_SETTINGS = "ignore_connectivity_settings"

fun isOkToSyncAutomatically(context: Context): Boolean {
    val di: DI by closestDI(context)
    val prefs: Prefs by di.instance()
    return (
        currentlyConnected(context) &&
            (!prefs.onlySyncWhileCharging || currentlyCharging(context)) &&
            (!prefs.onlySyncOnWifi || currentlyUnmetered(context))
        )
}

class FeedSyncer(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), DIAware {
    override val di: DI by closestDI(context)

    // TODO remove this broadcast
    private val currentlySyncing: ConflatedBroadcastChannel<Boolean> by instance(tag = CURRENTLY_SYNCING_STATE)

    override suspend fun doWork(): Result {
        val goParallel = inputData.getBoolean(PARALLEL_SYNC, false)
        val ignoreConnectivitySettings = inputData.getBoolean(IGNORE_CONNECTIVITY_SETTINGS, false)

        var success = false

        val applicationState by di.instance<ApplicationState>()

        try {
            if (ignoreConnectivitySettings || isOkToSyncAutomatically(context)) {
                applicationState.setRefreshing()

                if (!currentlySyncing.isClosedForSend) {
                    currentlySyncing.offer(true)
                }

                val feedId = inputData.getLong(ARG_FEED_ID, ID_UNSET)
                val feedTag = inputData.getString(ARG_FEED_TAG) ?: ""
                val forceNetwork = inputData.getBoolean(ARG_FORCE_NETWORK, false)
                val minFeedAgeMinutes = inputData.getInt(MIN_FEED_AGE_MINUTES, 15)

                success = syncFeeds(
                    context = applicationContext,
                    feedId = feedId,
                    feedTag = feedTag,
                    forceNetwork = forceNetwork,
                    parallel = goParallel,
                    minFeedAgeMinutes = minFeedAgeMinutes
                )
                // Send notifications for configured feeds
                notify(applicationContext)
            }

            // TODO remove this broadcast
            if (!currentlySyncing.isClosedForSend) {
                currentlySyncing.offer(false)
            }
        } finally {
            applicationState.setRefreshing(false)
        }

        return when (success) {
            true -> Result.success()
            false -> Result.failure()
        }
    }
}

fun requestFeedSync(
    di: DI,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    ignoreConnectivitySettings: Boolean = false,
    forceNetwork: Boolean = false,
    parallell: Boolean = false
) {
    val workRequest = OneTimeWorkRequestBuilder<FeedSyncer>()

    val data = workDataOf(
        ARG_FEED_ID to feedId,
        ARG_FEED_TAG to feedTag,
        PARALLEL_SYNC to parallell,
        IGNORE_CONNECTIVITY_SETTINGS to ignoreConnectivitySettings,
        ARG_FORCE_NETWORK to forceNetwork
    )

    workRequest.setInputData(data)
    val workManager by di.instance<WorkManager>()
    workManager.enqueue(workRequest.build())
}

fun configurePeriodicSync(context: Context, forceReplace: Boolean = false) {
    val di by closestDI(context)
    val workManager: WorkManager by di.instance()
    val prefs: Prefs by di.instance()
    val shouldSync = prefs.shouldSync()

    if (shouldSync) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(prefs.onlySyncWhileCharging)

        if (prefs.onlySyncOnWifi) {
            constraints.setRequiredNetworkType(NetworkType.UNMETERED)
        } else {
            constraints.setRequiredNetworkType(NetworkType.CONNECTED)
        }

        var timeInterval = prefs.synchronizationFrequency

        if (timeInterval in 1..12 || timeInterval == 24L) {
            // Old value for periodic sync was in hours, convert it to minutes
            timeInterval *= 60
            prefs.synchronizationFrequency = timeInterval
        }

        val workRequestBuilder = PeriodicWorkRequestBuilder<FeedSyncer>(
            timeInterval, TimeUnit.MINUTES,
            timeInterval / 2, TimeUnit.MINUTES
        )

        val syncWork = workRequestBuilder
            .setConstraints(constraints.build())
            .addTag("periodic_sync")
            .build()

        val existingWorkPolicy = if (forceReplace) {
            ExistingPeriodicWorkPolicy.REPLACE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_NAME,
            existingWorkPolicy,
            syncWork
        )
    } else {
        workManager.cancelUniqueWork(UNIQUE_PERIODIC_NAME)
    }
}
