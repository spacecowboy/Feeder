package com.nononsenseapps.feeder.background

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nononsenseapps.feeder.R

const val SYNC_NOTIFICATION_ID = 42623
private const val SYNC_CHANNEL_ID = "feederSyncNotifications"
private const val SYNC_NOTIFICATION_GROUP = "com.nononsenseapps.feeder.SYNC"

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

/**
 * Necessary for older Android versions.
 */
fun getNotification(
    context: Context,
    notificationManager: NotificationManagerCompat,
): Notification {
    createNotificationChannel(context, notificationManager)

    val syncingText = context.getString(R.string.syncing)

    return NotificationCompat
        .Builder(context.applicationContext, SYNC_CHANNEL_ID)
        .setContentTitle(syncingText)
        .setTicker(syncingText)
        .setGroup(SYNC_NOTIFICATION_GROUP)
        .setSmallIcon(R.drawable.ic_stat_sync)
        .setOngoing(true)
        .build()
}
