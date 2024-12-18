package com.nononsenseapps.feeder.model.workmanager

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.nononsenseapps.feeder.R

private const val SYNC_CHANNEL_ID = "feederSyncNotifications"

/**
 * This is safe to call multiple times
 */
@TargetApi(Build.VERSION_CODES.O)
@RequiresApi(Build.VERSION_CODES.O)
private fun createNotificationChannel(
    context: Context,
    notificationManager: NotificationManagerCompat,
) {
    val name = context.getString(R.string.sync_status)
    val description = context.getString(R.string.sync_status)

    val channel =
        NotificationChannel(SYNC_CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
    channel.description = description

    notificationManager.createNotificationChannel(channel)
}
