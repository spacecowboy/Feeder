package com.nononsenseapps.feeder.background

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import androidx.core.content.getSystemService
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.RssLocalSync
import com.nononsenseapps.feeder.model.notify
import com.nononsenseapps.feeder.ui.ARG_FEED_ID
import com.nononsenseapps.feeder.ui.ARG_FEED_TAG
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import java.time.Duration

const val ARG_FORCE_NETWORK = "force_network"

private const val MIN_FEED_AGE_MINUTES = "min_feed_age_minutes"

class RssSyncJob(
    private val context: Context,
    override val params: JobParameters,
) : BackgroundJob,
    DIAware {
    override val di: DI by closestDI(context)

    private val rssLocalSync: RssLocalSync by instance()

    override val jobId: Int = params.jobId

    override suspend fun doWork() {
        try {
            val feedId = params.extras.getLong(ARG_FEED_ID, ID_UNSET)
            val feedTag = params.extras.getString(ARG_FEED_TAG) ?: ""
            val forceNetwork = params.extras.getBoolean(ARG_FORCE_NETWORK, false)
            val minFeedAgeMinutes = params.extras.getInt(MIN_FEED_AGE_MINUTES, 5)

            rssLocalSync.syncFeeds(
                feedId = feedId,
                feedTag = feedTag,
                forceNetwork = forceNetwork,
                minFeedAgeMinutes = minFeedAgeMinutes,
            )
        } catch (e: Exception) {
            Log.e("FeederFeedSyncer", "Failure during sync", e)
        } finally {
            // Send notifications for configured feeds
            notify(context.applicationContext)
        }
    }

    companion object {
        const val LOG_TAG = "FEEDER_RSSSYNCJOB"
    }
}

fun runOnceRssSync(
    di: DI,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    forceNetwork: Boolean = false,
    triggeredByUser: Boolean,
) {
    val repository: Repository by di.instance()
    val context: Application by di.instance()
    val jobScheduler: JobScheduler? = context.getSystemService()

    if (jobScheduler == null) {
        Log.e(RssSyncJob.LOG_TAG, "JobScheduler not available")
        return
    }

    val componentName = ComponentName(context, FeederJobService::class.java)
    val builder =
        JobInfo
            .Builder(BackgroundJobId.RSS_SYNC.jobId, componentName)
            .setRequiredNetworkType(
                if (!forceNetwork && repository.syncOnlyOnWifi.value) {
                    JobInfo.NETWORK_TYPE_UNMETERED
                } else {
                    JobInfo.NETWORK_TYPE_ANY
                },
            ).setExtras(
                PersistableBundle().apply {
                    putLong(ARG_FEED_ID, feedId)
                    putString(ARG_FEED_TAG, feedTag)
                    putBoolean(ARG_FORCE_NETWORK, forceNetwork)
                },
            )

    if (triggeredByUser && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        builder.setUserInitiated(true)
    }

    jobScheduler.schedule(builder.build())
}

fun schedulePeriodicRssSync(
    di: DI,
    replace: Boolean,
) {
    val repository: Repository by di.instance()
    val context: Application by di.instance()
    val jobScheduler: JobScheduler? by di.instanceOrNull()

    if (jobScheduler == null) {
        Log.e(RssSyncJob.LOG_TAG, "JobScheduler not available")
        return
    }

    if (repository.syncFrequency.value.minutes < 1) {
        // Cancel and return
        jobScheduler?.cancel(BackgroundJobId.RSS_SYNC_PERIODIC.jobId)
        return
    }

    val frequency = Duration.ofMinutes(repository.syncFrequency.value.minutes)

    val componentName = ComponentName(context, FeederJobService::class.java)
    val jobInfo =
        JobInfo
            .Builder(BackgroundJobId.RSS_SYNC_PERIODIC.jobId, componentName)
            .setRequiresCharging(repository.syncOnlyWhenCharging.value)
            .setRequiredNetworkType(
                if (repository.syncOnlyOnWifi.value) {
                    JobInfo.NETWORK_TYPE_UNMETERED
                } else {
                    JobInfo.NETWORK_TYPE_ANY
                },
            ).setPeriodic(frequency.toMillis())
            .setPersisted(true)
            .build()

    if (replace || jobScheduler?.getMyPendingJob(BackgroundJobId.RSS_SYNC_PERIODIC.jobId) == null) {
        jobScheduler?.schedule(jobInfo)
    }
}

fun JobScheduler.getMyPendingJob(jobId: Int): JobInfo? = allPendingJobs.firstOrNull { it.id == jobId }
