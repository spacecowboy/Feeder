package com.nononsenseapps.feeder.background

import android.app.job.JobParameters
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.nononsenseapps.feeder.base.DIAwareJobService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kodein.di.instance

class FeederJobService : DIAwareJobService() {
    private val notificationManager: NotificationManagerCompat by instance()

    private val coroutineScope = BackgroundCoroutineScope()
    private val jobs: MutableMap<Int, Job> = mutableMapOf()

    /*
    This service executes each incoming job on a Handler running on
    your application's main thread. This means that you must offload
    your execution logic to another thread/handler/AsyncTask of your
    choosing. Not doing so will result in blocking any future
    callbacks from the JobScheduler - specifically onStopJob(android.app.job.JobParameters),
    which is meant to inform you that the scheduling requirements
    are no longer being met.
     */
    override fun onStartJob(params: JobParameters): Boolean {
        /*
        Provide JobScheduler with a notification to post and tie to this job's lifecycle.
        This is only required for those user-initiated jobs which return true via
        JobParameters.isUserInitiatedJob().
        Note that certain types of jobs (e.g. data transfer jobs) may require the
        notification to have certain characteristics and their documentation will
        state any such requirements.
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            showNotificationIfNecessary(params)
        }

        return when (params.jobId) {
            BackgroundJobId.RSS_SYNC.jobId -> {
                runJob(RssSyncJob(context = this, params = params))
            }
            BackgroundJobId.RSS_SYNC_PERIODIC.jobId -> {
                runJob(RssSyncJob(context = this, params = params))
            }
            BackgroundJobId.FULL_TEXT_SYNC.jobId -> {
                runJob(FullTextSyncJob(context = this, params = params))
            }
            BackgroundJobId.SYNC_CHAIN_GET_UPDATES.jobId -> {
                runJob(SyncChainGetUpdatesJob(context = this, params = params))
            }
            BackgroundJobId.SYNC_CHAIN_SEND_READ.jobId -> {
                runJob(SyncChainSendReadJob(context = this, params = params))
            }
            BackgroundJobId.BLOCKLIST_UPDATE.jobId -> {
                runJob(BlocklistUpdateJob(context = this, params = params))
            }
            else -> {
                Log.i(LOG_TAG, "Unknown job id: ${params.jobId}")
                false
            }
        }
    }

    override fun onStopJob(params: JobParameters): Boolean {
        jobs[params.jobId]?.cancel()

        // False means no reschedule necessary.
        // Periodic jobs will re-run at their next interval.
        return false
    }

    private fun runJob(job: BackgroundJob): Boolean {
        jobs[job.jobId] =
            coroutineScope.launch {
                job.doWork()
                jobFinished(job.params, false)
                jobs.remove(job.jobId)
            }

        // True means we're doing work in a coroutine.
        return true
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun showNotificationIfNecessary(params: JobParameters) {
        // Only show notifications for user-initiated jobs.
        if (!params.isUserInitiatedJob) {
            return
        }

        setNotification(
            params,
            SYNC_NOTIFICATION_ID,
            getNotification(this, notificationManager),
            JOB_END_NOTIFICATION_POLICY_REMOVE,
        )
    }

    companion object {
        private const val LOG_TAG = "FeederJobService"
    }
}
