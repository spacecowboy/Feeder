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
import com.nononsenseapps.feeder.sync.SyncRestClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.time.Duration

class SyncChainSendReadJob(
    context: Context,
    override val params: JobParameters,
) : BackgroundJob,
    DIAware {
    override val di: DI by closestDI(context)

    private val syncClient: SyncRestClient by di.instance()

    override val jobId: Int = params.jobId

    override suspend fun doWork() {
        try {
            Log.d(LOG_TAG, "Doing work")
            syncClient
                .markAsRead()
                .onLeft {
                    Log.e(
                        LOG_TAG,
                        "Error when sending readmarks ${it.code}, ${it.body}",
                        it.throwable,
                    )
                }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error when sending read marks", e)
        }
    }

    companion object {
        const val LOG_TAG = "FEEDER_SENDREAD"
    }
}

fun runOnceSyncChainSendRead(di: DI) {
    val repository: Repository by di.instance()
    val context: Application by di.instance()

    if (!repository.isSyncChainConfigured) {
        Log.e(SyncChainSendReadJob.LOG_TAG, "Sync chain not enabled")
        return
    }

    val jobScheduler: JobScheduler? = context.getSystemService()

    if (jobScheduler == null) {
        Log.e(SyncChainSendReadJob.LOG_TAG, "JobScheduler not available")
        return
    }

    val componentName = ComponentName(context, FeederJobService::class.java)
    val jobInfo =
        JobInfo
            .Builder(BackgroundJobId.SYNC_CHAIN_SEND_READ.jobId, componentName)
            .setRequiresCharging(repository.syncOnlyWhenCharging.value)
            .setRequiredNetworkType(
                if (repository.syncOnlyOnWifi.value) {
                    JobInfo.NETWORK_TYPE_UNMETERED
                } else {
                    JobInfo.NETWORK_TYPE_ANY
                },
            )
            // Wait at least 10 seconds before running so that we can batch up
            .setMinimumLatency(Duration.ofSeconds(10).toMillis())
            .build()

    if (jobScheduler.getMyPendingJob(jobInfo.id) == null) {
        jobScheduler.schedule(jobInfo)
    }
}
