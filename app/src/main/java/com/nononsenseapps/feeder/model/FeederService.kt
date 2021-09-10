package com.nononsenseapps.feeder.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nononsenseapps.feeder.base.DIAwareIntentService
import com.nononsenseapps.feeder.db.room.FeedItemDao
import kotlinx.coroutines.runBlocking
import org.kodein.di.instance

const val ACTION_MARK_AS_UNREAD = "MARK_AS_READ"

class FeederService : DIAwareIntentService("FeederService") {
    private val dao: FeedItemDao by instance()

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_MARK_AS_UNREAD -> intent.data?.let { markAsUnread(it) }
        }
    }

    private fun markAsUnread(data: Uri) {
        data.lastPathSegment?.toLongOrNull()?.let { id ->
            runBlocking {
                dao.markAsRead(id = id, unread = true)
            }
        }
    }

    companion object {
        fun getIntentForId(context: Context, feedItemId: Long): Intent =
            Intent(context, FeederService::class.java).apply {
                action = ACTION_MARK_AS_UNREAD
                data = Uri.parse("com.nononsenseapps.feeder/feeditem/$feedItemId")
            }
    }
}
