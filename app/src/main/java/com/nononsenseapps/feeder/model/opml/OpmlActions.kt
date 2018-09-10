package com.nononsenseapps.feeder.model.opml

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.nononsenseapps.feeder.coroutines.Background
import com.nononsenseapps.feeder.db.COL_TAG
import com.nononsenseapps.feeder.db.FeedSQL
import com.nononsenseapps.feeder.db.asFeed
import com.nononsenseapps.feeder.model.OPMLContenProvider
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.util.forEach
import com.nononsenseapps.feeder.util.getString
import com.nononsenseapps.feeder.util.makeToast
import com.nononsenseapps.feeder.util.notifyAllUris
import com.nononsenseapps.feeder.util.queryFeeds
import com.nononsenseapps.feeder.util.queryTagsWithCounts
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Exports OPML on a background thread
 */
fun exportOpmlInBackground(context: Context, uri: Uri) = launch(Background) {

    val appContext = context.applicationContext
    try {
        val time = measureTimeMillis {
            appContext.contentResolver.openOutputStream(uri)?.let {
                writeOutputStream(it,
                        tags(appContext.contentResolver),
                        feedsWithTags(appContext.contentResolver))
            }
        }
        Log.d("OPML", "Exported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        e.printStackTrace()
        Log.e("OMPL", "Failed to export OMPL: $e")
        launch(UI) {
            appContext.makeToast("Failed to export OMPL")
        }
    }
}

/**
 * Imports OPML on a background thread
 */
fun importOpmlInBackground(context: Context, uri: Uri) = launch(Background) {
    val appContext = context.applicationContext
    try {
        val time = measureTimeMillis {
            val parser = OpmlParser(OPMLContenProvider(appContext))
            appContext.contentResolver.openInputStream(uri)?.use {
                parser.parseInputStream(it)
            }
            appContext.contentResolver.notifyAllUris()
            requestFeedSync()
        }
        Log.d("OPML", "Imported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e("OMPL", "Failed to import OMPL: $e")
        launch(UI) {
            appContext.makeToast("Failed to import OMPL")
        }
    }
}

private fun tags(contentResolver: ContentResolver): Iterable<String?> {
    val tags = ArrayList<String?>()

    contentResolver.queryTagsWithCounts(columns = listOf(COL_TAG)) { cursor ->
        cursor.forEach {
            tags.add(it.getString(COL_TAG))
        }
    }

    return tags
}

private fun feedsWithTags(contentResolver: ContentResolver): (String?) -> Iterable<FeedSQL> {
    return { tag ->
        val feeds = ArrayList<FeedSQL>()

        contentResolver.queryFeeds(where = "$COL_TAG IS ?", params = listOf(tag ?: "")) { cursor ->
            cursor.forEach {
                feeds.add(it.asFeed())
            }
        }

        feeds
    }
}
