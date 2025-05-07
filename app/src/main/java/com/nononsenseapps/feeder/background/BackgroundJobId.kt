package com.nononsenseapps.feeder.background

import android.app.job.JobParameters

enum class BackgroundJobId(
    val jobId: Int,
) {
    RSS_SYNC(1),
    RSS_SYNC_PERIODIC(2),
    FULL_TEXT_SYNC(3),
    SYNC_CHAIN_GET_UPDATES(4),
    SYNC_CHAIN_SEND_READ(5),
    BLOCKLIST_UPDATE(6),
    CLEANUP_ORPHANED_FILES(7),
}

interface BackgroundJob {
    val jobId: Int
    val params: JobParameters

    suspend fun doWork()
}
