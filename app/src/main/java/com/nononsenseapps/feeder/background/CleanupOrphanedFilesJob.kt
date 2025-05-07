package com.nononsenseapps.feeder.background

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.getSystemService
import com.nononsenseapps.feeder.blob.blobFile
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.util.FilePathProvider
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.io.File
import java.util.concurrent.TimeUnit

class CleanupOrphanedFilesJob(
    context: Context,
    override val params: JobParameters,
) : BackgroundJob, DIAware {
    override val di: DI by closestDI(context)

    private val filePathProvider: FilePathProvider by instance()
    private val feedItemDao: FeedItemDao by instance()

    override val jobId: Int = params.jobId

    override suspend fun doWork() {
        try {
            Log.i(LOG_TAG, "Starting cleanup of orphaned article files")

            // Get all valid feed item IDs from the database
            val validFeedItemIds =
                withContext(Dispatchers.IO) {
                    feedItemDao.getAllFeedItemIds()
                }

            // Clean up article files in articleDir
            cleanupDirectory(filePathProvider.articleDir, validFeedItemIds, ::blobFile)

            // Clean up full article files in fullArticleDir
            cleanupDirectory(filePathProvider.fullArticleDir, validFeedItemIds, ::blobFullFile)

            Log.i(LOG_TAG, "Completed cleanup of orphaned article files")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error during cleanup of orphaned files", e)
        }
    }

    private suspend fun cleanupDirectory(
        directory: File,
        validIds: List<Long>,
        fileProvider: (Long, File) -> File,
    ) {
        if (!directory.isDirectory) {
            logDebug(LOG_TAG, "Directory doesn't exist: ${directory.absolutePath}")
            return
        }

        val validFilePaths =
            validIds.map { id ->
                fileProvider(id, directory).canonicalPath
            }.toSet()

        var deletedCount = 0
        withContext(Dispatchers.IO) {
            directory.listFiles()?.forEach { file ->
                try {
                    val filePath = file.canonicalPath
                    if (!validFilePaths.contains(filePath)) {
                        if (file.delete()) {
                            deletedCount++
                            logDebug(LOG_TAG, "Deleted orphaned file: $filePath")
                        } else {
                            Log.w(LOG_TAG, "Failed to delete orphaned file: $filePath")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Error processing file ${file.canonicalPath}", e)
                }
            }
        }

        Log.i(LOG_TAG, "Deleted $deletedCount orphaned files from ${directory.canonicalPath}")
    }

    companion object {
        const val LOG_TAG = "FEEDER_CLEANUP_FILES"
    }
}

fun schedulePeriodicOrphanedFilesCleanup(di: DI) {
    val context: Application by di.instance()
    val jobScheduler: JobScheduler? = context.getSystemService()

    if (jobScheduler == null) {
        Log.e(CleanupOrphanedFilesJob.LOG_TAG, "JobScheduler not available")
        return
    }

    // Get current job if exists
    val currentJob = jobScheduler.getMyPendingJob(BackgroundJobId.CLEANUP_ORPHANED_FILES.jobId)

    // Only schedule if the job doesn't exist yet
    if (currentJob == null) {
        val componentName = ComponentName(context, FeederJobService::class.java)

        // Schedule to run once per day (24 hours)
        val dailyIntervalMillis = TimeUnit.DAYS.toMillis(1)

        val jobInfo =
            JobInfo.Builder(BackgroundJobId.CLEANUP_ORPHANED_FILES.jobId, componentName)
                // Run when device is idle
                .setRequiresDeviceIdle(true)
                // Run when device is charging
                .setRequiresCharging(true)
                // Set periodic interval to one day
                .setPeriodic(dailyIntervalMillis)
                // Persist across reboots
                .setPersisted(true)
                .build()

        jobScheduler.schedule(jobInfo)
        Log.i(CleanupOrphanedFilesJob.LOG_TAG, "Scheduled daily cleanup of orphaned files")
    }
}
