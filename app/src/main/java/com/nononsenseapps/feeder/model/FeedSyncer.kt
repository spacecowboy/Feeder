package com.nononsenseapps.feeder.model

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.workDataOf
import com.nononsenseapps.feeder.ui.ARG_FEED_ID
import com.nononsenseapps.feeder.ui.ARG_FEED_TAG
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.SystemUtils.currentlyOnWifi
import java.util.concurrent.TimeUnit

const val ARG_FORCE_NETWORK = "force_network"

const val LOG_TAG = "FEED_SYNC"
const val UNIQUE_PERIODIC_NAME = "feeder_periodic"
const val IS_MANUAL_SYNC = "is_manual_sync"

const val FEED_ADDED_BROADCAST = "feeder.nononsenseapps.RSS_FEED_ADDED_BROADCAST"
const val SYNC_BROADCAST = "feeder.nononsenseapps.RSS_SYNC_BROADCAST"
const val SYNC_BROADCAST_IS_ACTIVE = "IS_ACTIVE"

class FeedSyncer : Worker() {
    override fun doWork(): Result {

        val wifiStatusOK = when {
            inputData.getBoolean(IS_MANUAL_SYNC, false) -> true
            currentlyOnWifi(applicationContext) -> true
            else -> !PrefUtils.shouldSyncOnlyOnWIfi(applicationContext)
        }

        val bcast = Intent(SYNC_BROADCAST)
                .putExtra(SYNC_BROADCAST_IS_ACTIVE, true)

        var success = false

        if (wifiStatusOK) {
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(bcast)

            val feedId = inputData.getLong(ARG_FEED_ID, -1)
            val feedTag = inputData.getString(ARG_FEED_TAG) ?: ""
            val forceNetwork = inputData.getBoolean(ARG_FORCE_NETWORK, false)

            success = syncFeeds(applicationContext, feedId, feedTag, forceNetwork = forceNetwork)
        } else {
            Log.d(LOG_TAG, "Skipping sync work because wifistatus not OK")
        }

        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(bcast.putExtra(SYNC_BROADCAST_IS_ACTIVE, false))

        return if (success) {
            Result.SUCCESS
        } else {
            Result.FAILURE
        }
    }
}

fun requestFeedSync(feedId: Long = -1, feedTag: String = "") {
    val workRequest = OneTimeWorkRequestBuilder<FeedSyncer>()

    val data = workDataOf(ARG_FEED_ID to feedId,
            ARG_FEED_TAG to feedTag,
            ARG_FORCE_NETWORK to true)

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
