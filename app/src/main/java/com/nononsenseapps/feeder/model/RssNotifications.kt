package com.nononsenseapps.feeder.model

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.Background
import com.nononsenseapps.feeder.db.COL_FEED
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_NOTIFIED
import com.nononsenseapps.feeder.db.COL_NOTIFY
import com.nononsenseapps.feeder.db.COL_UNREAD
import com.nononsenseapps.feeder.db.FeedItemSQL
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.ui.ARG_FEED_URL
import com.nononsenseapps.feeder.ui.EXTRA_FEEDITEMS_TO_MARK_AS_NOTIFIED
import com.nononsenseapps.feeder.ui.FeedActivity
import com.nononsenseapps.feeder.ui.ReaderActivity
import com.nononsenseapps.feeder.util.ARG_FEEDTITLE
import com.nononsenseapps.feeder.util.getFeedItems
import com.nononsenseapps.feeder.util.getFeeds
import com.nononsenseapps.feeder.util.notificationManager
import kotlinx.coroutines.experimental.launch


const val notificationId = 73583
const val channelId = "feederNotifications"

fun notifyInBackground(context: Context) {
    val appContext = context.applicationContext
    launch(Background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(appContext)
        }

        val nm = appContext.notificationManager

        val feedItems = getItemsToNotify(appContext)

        val notifications: List<Pair<Int, Notification>> = if (feedItems.isEmpty()) {
            emptyList()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || feedItems.size < 4) {
                // Cancel inbox notification if present
                nm.cancel(notificationId)
                // Platform automatically bundles 4 or more notifications
                feedItems.map {
                    it.id.toInt() to singleNotification(appContext, it)
                }
            } else {
                // In this case, also cancel any individual notifications
                feedItems.forEach {
                    nm.cancel(it.id.toInt())
                }
                // Use an inbox style notification to bundle many notifications together
                listOf(notificationId to inboxNotification(appContext, feedItems))
            }
        }

        notifications.forEach { (id, notification) ->
            nm.notify(id, notification)
        }

    }
}

fun cancelNotificationInBackground(context: Context, feedItemId: Long) {
    val appContext = context.applicationContext
    launch(Background) {
        val nm = appContext.notificationManager
        nm.cancel(feedItemId.toInt())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            notifyInBackground(appContext)
        }
    }
}

/**
 * This is an update operation if channel already exists so it's safe to call multiple times
 */
@RequiresApi(Build.VERSION_CODES.O)
fun createNotificationChannel(context: Context) {
    val name = context.getString(R.string.notification_channel_name)
    val description = context.getString(R.string.notification_channel_description)

    val notificationManager: NotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW)
    channel.description = description

    notificationManager.createNotificationChannel(channel)
}

private fun singleNotification(context: Context, item: FeedItemSQL): Notification {
    val style = NotificationCompat.BigTextStyle()
    val title = item.plaintitle
    val text = item.feedtitle

    style.bigText(text)
    style.setBigContentTitle(title)

    val contentIntent = when (item.description.isBlank()) {
        true -> {
            // TODO make use of ArticleTextExtractor here when available
            val i = Intent(context, FeedActivity::class.java)
            i.data = Uri.withAppendedPath(URI_FEEDS, "${item.feedid}")
            i.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
            PendingIntent.getActivity(context, item.id.toInt(), i,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        }
        false -> {
            val i = Intent(context, ReaderActivity::class.java)
            ReaderActivity.setRssExtras(i, item)
            i.data = Uri.withAppendedPath(URI_FEEDITEMS, "${item.id}")
            val stackBuilder = TaskStackBuilder.create(context)
            // Add the parent of the specified activity - as stated in the manifest
            stackBuilder.addParentStack(ReaderActivity::class.java)
            stackBuilder.addNextIntent(i)
            // Now, modify the parent intent so that it navigates to the appropriate feed
            val parentIntent = stackBuilder.editIntentAt(0)
            if (parentIntent != null) {
                parentIntent.data = Uri.withAppendedPath(URI_FEEDS, "${item.feedid}")
                parentIntent.putExtra(ARG_FEEDTITLE, item.feedtitle)
                parentIntent.putExtra(ARG_FEED_URL, item.feedUrl)
            }
            stackBuilder.getPendingIntent(item.id.toInt(), PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    val builder = notificationBuilder(context)

    builder.setContentText(text)
            .setContentTitle(title)
            .setContentIntent(contentIntent)
            .setDeleteIntent(getDeleteIntent(context, item))
            .setNumber(1)

    if (item.enclosurelink != null) {
        builder.addAction(R.drawable.ic_action_av_play_circle_outline,
                context.getString(R.string.open_enclosed_media), PendingIntent.getActivity(context, item.id.toInt(), Intent(Intent.ACTION_VIEW,
                Uri.parse(item.enclosurelink)), PendingIntent.FLAG_UPDATE_CURRENT))
    }

    builder.addAction(R.drawable.ic_action_location_web_site,
            context.getString(R.string.open_link_in_browser),
            PendingIntent.getActivity(context, item.id.toInt(), Intent(Intent.ACTION_VIEW,
                    Uri.parse(item.link)), PendingIntent.FLAG_UPDATE_CURRENT))

    style.setBuilder(builder)
    return style.build()
}

/**
 * Use this on platforms older than 24 to bundle notifications together
 */
private fun inboxNotification(context: Context, feedItems: List<FeedItemSQL>): Notification {
    val style = NotificationCompat.InboxStyle()
    val title = context.getString(R.string.updated_feeds)
    val text = feedItems.map { it.feedtitle }.toSet().joinToString(separator = ", ")

    style.setBigContentTitle(title)
    feedItems.forEach {
        style.addLine("${it.feedtitle} \u2014 ${it.plaintitle}")
    }

    val intent = Intent(context, FeedActivity::class.java)
    intent.putExtra(EXTRA_FEEDITEMS_TO_MARK_AS_NOTIFIED, LongArray(feedItems.size, { i -> feedItems[i].id }))

    // We can be a little bit smart - if all items are from the same feed then go to that feed
    // Otherwise we should go to All feeds
    val feedIds = feedItems.map { it.feedid }.toSet()
    intent.data = if (feedIds.toSet().size == 1) {
        Uri.withAppendedPath(URI_FEEDS, "${feedIds.first()}")
    } else {
        Uri.withAppendedPath(URI_FEEDS, "-1")
    }

    val builder = notificationBuilder(context)

    builder.setContentText(text)
            .setContentTitle(title)
            .setContentIntent(PendingIntent.getActivity(context, notificationId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT))
            .setDeleteIntent(getDeleteIntent(context, feedItems))
            .setNumber(feedItems.size)

    style.setBuilder(builder)
    return style.build()
}

private fun getDeleteIntent(context: Context, feedItems: List<FeedItemSQL>): PendingIntent {
    val intent = Intent(context, RssNotificationBroadcastReceiver::class.java)
    intent.action = ACTION_MARK_AS_NOTIFIED

    val ids = LongArray(feedItems.size, { i -> feedItems[i].id })
    intent.putExtra(EXTRA_FEEDITEM_ID_ARRAY, ids)

    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

private fun getDeleteIntent(context: Context, feedItem: FeedItemSQL): PendingIntent {
    val intent = Intent(context, RssNotificationBroadcastReceiver::class.java)
    intent.action = ACTION_MARK_AS_NOTIFIED
    intent.data = Uri.withAppendedPath(URI_FEEDITEMS, "$feedItem.id")
    val ids: LongArray = longArrayOf(feedItem.id)
    intent.putExtra(EXTRA_FEEDITEM_ID_ARRAY, ids)

    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

private fun notificationBuilder(context: Context): NotificationCompat.Builder {
    val bm = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)

    return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_f)
            .setLargeIcon(bm)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
}

private fun getItemsToNotify(context: Context): List<FeedItemSQL> {
    val feeds = getFeedIdsToNotify(context)

    return when (feeds.isEmpty()) {
        true -> emptyList()
        false -> context.contentResolver.getFeedItems(
                where = "$COL_FEED IN (${feeds.joinToString(separator = ",")}) AND $COL_NOTIFIED IS 0 AND $COL_UNREAD IS 1")
    }
}

private fun getFeedIdsToNotify(context: Context): List<Long> =
        context.contentResolver.getFeeds(columns = listOf(COL_ID),
                where = "$COL_NOTIFY IS 1").map { it.id }
