package com.nononsenseapps.feeder.background

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.getSystemService
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.db.room.BlocklistDao
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.ARG_FEED_ID
import com.nononsenseapps.feeder.ui.ARG_ONLY_NEW
import com.nononsenseapps.feeder.util.logDebug
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.time.Instant

class BlocklistUpdateJob(
    context: Context,
    override val params: JobParameters,
) : BackgroundJob, DIAware {
    override val di: DI by closestDI(context)

    private val blocklistDao: BlocklistDao by instance()

    override val jobId: Int = params.jobId

    override suspend fun doWork() {
        logDebug(LOG_TAG, "Doing work...")

        val onlyNew = params.extras.getBoolean(ARG_ONLY_NEW, false)
        val feedId = params.extras.getLong(ARG_FEED_ID, ID_UNSET)

        when {
            feedId != ID_UNSET -> blocklistDao.setItemBlockStatusForNewInFeed(feedId, Instant.now())
            onlyNew -> blocklistDao.setItemBlockStatusWhereNull(Instant.now())
            else -> blocklistDao.setItemBlockStatus(Instant.now())
        }

        logDebug(LOG_TAG, "Work done!")
    }

    companion object {
        const val LOG_TAG = "FEEDER_BLOCKLIST"
    }
}

fun runOnceBlocklistUpdate(di: DI) {
    val repository: Repository by di.instance()
    val context: Application by di.instance()
    val jobScheduler: JobScheduler? = context.getSystemService()

    if (jobScheduler == null) {
        Log.e(BlocklistUpdateJob.LOG_TAG, "JobScheduler not available")
        return
    }

    val componentName = ComponentName(context, FeederJobService::class.java)
    val jobInfo =
        JobInfo.Builder(BackgroundJobId.BLOCKLIST_UPDATE.jobId, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            // Older versions of Android enforce a constraint to be present. Hence the small delay
            .setMinimumLatency(1)
            .build()

    jobScheduler.schedule(jobInfo)
}
