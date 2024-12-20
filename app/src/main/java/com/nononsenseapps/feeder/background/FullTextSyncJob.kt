package com.nononsenseapps.feeder.background

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.model.FullTextParser
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

class FullTextSyncJob(
    context: Context,
    override val params: JobParameters,
) : BackgroundJob, DIAware {
    override val di: DI by closestDI(context)
    private val fullTextParser: FullTextParser by instance()

    override val jobId: Int = params.jobId

    override suspend fun doWork() {
        Log.i(LOG_TAG, "Performing full text parse work")
        fullTextParser.parseFullArticlesForMissing()
        Log.i(LOG_TAG, "Finished full text parse work")
    }

    companion object {
        const val LOG_TAG = "FEEDER_FULLTEXT"
    }
}

fun runOnceFullTextSync(
    di: DI,
    triggeredByUser: Boolean,
) {
    val repository: Repository by di.instance()
    val context: Application by di.instance()
    val jobScheduler: JobScheduler? = context.getSystemService()

    if (jobScheduler == null) {
        Log.e(FullTextSyncJob.LOG_TAG, "JobScheduler not available")
        return
    }

    val componentName = ComponentName(context, FeederJobService::class.java)
    val builder =
        JobInfo.Builder(BackgroundJobId.FULL_TEXT_SYNC.jobId, componentName)
            .apply {
                if (!triggeredByUser) {
                    setRequiresCharging(repository.syncOnlyWhenCharging.value)
                        .setRequiredNetworkType(
                            if (repository.syncOnlyOnWifi.value) {
                                JobInfo.NETWORK_TYPE_UNMETERED
                            } else {
                                JobInfo.NETWORK_TYPE_ANY
                            },
                        )
                }
            }

    if (triggeredByUser && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        builder.setUserInitiated(true)
    }

    jobScheduler.schedule(builder.build())
}
