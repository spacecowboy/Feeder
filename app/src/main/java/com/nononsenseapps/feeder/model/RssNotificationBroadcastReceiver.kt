package com.nononsenseapps.feeder.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nononsenseapps.feeder.coroutines.Background
import com.nononsenseapps.feeder.util.markItemsAsNotified
import kotlinx.coroutines.experimental.launch

const val ACTION_MARK_AS_NOTIFIED: String = "mark_as_notified"

const val EXTRA_FEEDITEM_ID_ARRAY: String = "extra_feeditem_id_array"

class RssNotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ids = intent.getLongArrayExtra(EXTRA_FEEDITEM_ID_ARRAY)
        Log.d("RssNotificationReceiver", "onReceive: ${intent.action}; ${ids?.joinToString(", ")}")
        when (intent.action) {
            ACTION_MARK_AS_NOTIFIED -> markAsNotified(context, ids)
        }
    }
}

private fun markAsNotified(context: Context, itemIds: LongArray?) {
    if (itemIds != null) {
        launch(Background) {
            context.contentResolver.markItemsAsNotified(itemIds)
        }
    }
}
