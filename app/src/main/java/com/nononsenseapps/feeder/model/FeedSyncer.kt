package com.nononsenseapps.feeder.model

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.ARG_FEED_ID
import com.nononsenseapps.feeder.ui.ARG_FEED_TAG
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.SystemUtils.currentlyOnWifi
import com.nononsenseapps.feeder.util.feedParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

const val ARG_FORCE_NETWORK = "force_network"

const val LOG_TAG = "FEED_SYNC"
const val UNIQUE_PERIODIC_NAME = "feeder_periodic"
const val PARALLEL_SYNC = "parallel_sync"
const val MIN_FEED_AGE_MINUTES = "min_feed_age_minutes"
const val IGNORE_CONNECTIVITY_SETTINGS = "ignore_connectivity_settings"

const val FEED_ADDED_BROADCAST = "feeder.nononsenseapps.RSS_FEED_ADDED_BROADCAST"
const val SYNC_BROADCAST = "feeder.nononsenseapps.RSS_SYNC_BROADCAST"
const val SYNC_BROADCAST_IS_ACTIVE = "IS_ACTIVE"

class FeedSyncer(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun doWork(): Result = runBlocking {

        val goParallel = inputData.getBoolean(PARALLEL_SYNC, false)
        val ignoreConnectivitySettings = inputData.getBoolean(IGNORE_CONNECTIVITY_SETTINGS, false)

        val wifiStatusOK = when {
            ignoreConnectivitySettings -> true
            currentlyOnWifi(applicationContext) -> true
            else -> !PrefUtils.shouldSyncOnlyOnWIfi(applicationContext)
        }

        val bcast = Intent(SYNC_BROADCAST)
                .putExtra(SYNC_BROADCAST_IS_ACTIVE, true)

        var success = false

        if (wifiStatusOK) {
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(bcast)

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
        } else {
            Log.d(LOG_TAG, "Skipping sync work because wifistatus not OK")
        }

        LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(bcast.putExtra(SYNC_BROADCAST_IS_ACTIVE, false))

        when {
            success -> Result.SUCCESS
            else -> Result.FAILURE
        }
    }

    override fun onStopped(cancelled: Boolean) {
        job.cancel()
        super.onStopped(cancelled)
    }
}

fun requestFeedSync(feedId: Long = ID_UNSET,
                    feedTag: String = "",
                    ignoreConnectivitySettings: Boolean = false,
                    forceNetwork: Boolean = false,
                    parallell: Boolean = false) {
    val workRequest = OneTimeWorkRequestBuilder<FeedSyncer>()

    val data = workDataOf(ARG_FEED_ID to feedId,
            ARG_FEED_TAG to feedTag,
            PARALLEL_SYNC to parallell,
            IGNORE_CONNECTIVITY_SETTINGS to ignoreConnectivitySettings,
            ARG_FORCE_NETWORK to forceNetwork)

    workRequest.setInputData(data)

    WorkManager.getInstance().enqueue(workRequest.build())
}

fun configurePeriodicSync(context: Context, forceReplace: Boolean = false) {
    val shouldSync = PrefUtils.shouldSync(context)

    if (shouldSync) {
        val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(PrefUtils.shouldSyncOnlyWhenCharging(context))

        if (PrefUtils.shouldSyncOnHotSpots(context)) {
            constraints.setRequiredNetworkType(NetworkType.CONNECTED)
        } else {
            constraints.setRequiredNetworkType(NetworkType.UNMETERED)
        }

        var timeInterval = PrefUtils.synchronizationFrequency(context)

        if (timeInterval in 1..12 || timeInterval == 24L) {
            // Old value for periodic sync was in hours, convert it to minutes
            timeInterval *= 60
            PrefUtils.setSynchronizationFrequency(context, timeInterval)
        }

        val workRequestBuilder = PeriodicWorkRequestBuilder<FeedSyncer>(
                timeInterval, TimeUnit.MINUTES,
                timeInterval, TimeUnit.MINUTES)

        val syncWork = workRequestBuilder
                .setConstraints(constraints.build())
                .addTag("periodic_sync")
                .build()

        val existingWorkPolicy = if (forceReplace) {
            ExistingPeriodicWorkPolicy.REPLACE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }

        WorkManager.getInstance()
                .enqueueUniquePeriodicWork(UNIQUE_PERIODIC_NAME,
                        existingWorkPolicy,
                        syncWork)
    } else {
        WorkManager.getInstance()
                .cancelUniqueWork(UNIQUE_PERIODIC_NAME)
    }
}
