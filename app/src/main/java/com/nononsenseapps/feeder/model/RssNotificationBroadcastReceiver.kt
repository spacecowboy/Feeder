package com.nononsenseapps.feeder.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nononsenseapps.feeder.db.room.FeedItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

const val ACTION_MARK_AS_NOTIFIED: String = "mark_as_notified"

const val EXTRA_FEEDITEM_ID_ARRAY: String = "extra_feeditem_id_array"

class RssNotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ids = intent.getLongArrayExtra(EXTRA_FEEDITEM_ID_ARRAY)
        Log.d("RssNotificationReceiver", "onReceive: ${intent.action}; ${ids?.joinToString(", ")}")
        val kodein by closestKodein(context)
        val dao: FeedItemDao by kodein.instance()
        when (intent.action) {
            ACTION_MARK_AS_NOTIFIED -> markAsNotified(dao, ids)
        }
    }
}

private fun markAsNotified(feedItemDao: FeedItemDao, itemIds: LongArray?) {
    if (itemIds != null) {
        GlobalScope.launch(Dispatchers.Default) {
            feedItemDao.markAsNotified(itemIds.toList())
        }
    }
}
