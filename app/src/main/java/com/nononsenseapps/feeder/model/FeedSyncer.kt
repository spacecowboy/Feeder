package com.nononsenseapps.feeder.model

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.ARG_FEED_ID
import com.nononsenseapps.feeder.ui.ARG_FEED_TAG
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.currentlyCharging
import com.nononsenseapps.feeder.util.currentlyConnected
import com.nononsenseapps.feeder.util.currentlyUnmetered
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

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

    override suspend fun doWork(): Result {
        val goParallel = inputData.getBoolean(PARALLEL_SYNC, false)
        val ignoreConnectivitySettings = inputData.getBoolean(IGNORE_CONNECTIVITY_SETTINGS, false)

        var success = false

        val repository by di.instance<Repository>()

        try {
            if (ignoreConnectivitySettings || isOkToSyncAutomatically(context)) {
                repository.setRefreshing(true)

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
        } finally {
            repository.setRefreshing(false)
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
