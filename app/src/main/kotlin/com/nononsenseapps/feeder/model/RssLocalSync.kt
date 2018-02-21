package com.nononsenseapps.feeder.model

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.content.OperationApplicationException
import android.net.Uri
import android.os.RemoteException
import android.util.Log
import com.nononsenseapps.feeder.db.AUTHORITY
import com.nononsenseapps.feeder.db.COL_FEED
import com.nononsenseapps.feeder.db.COL_FEEDTITLE
import com.nononsenseapps.feeder.db.COL_FEEDURL
import com.nononsenseapps.feeder.db.COL_IMAGEURL
import com.nononsenseapps.feeder.db.COL_TAG
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.db.COL_URL
import com.nononsenseapps.feeder.db.Cleanup
import com.nononsenseapps.feeder.db.FEED_FIELDS
import com.nononsenseapps.feeder.db.FeedSQL
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.Util.LongsToStringArray
import com.nononsenseapps.feeder.db.Util.ToStringArray
import com.nononsenseapps.feeder.db.Util.WHEREIDIS
import com.nononsenseapps.feeder.db.Util.WhereIs
import com.nononsenseapps.feeder.util.feedParser
import com.nononsenseapps.feeder.util.getFeeds
import com.nononsenseapps.feeder.util.getIdForFeedItem
import com.nononsenseapps.feeder.util.intoContentProviderOperation
import com.nononsenseapps.feeder.util.notifyAllUris
import com.nononsenseapps.jsonfeed.Feed
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeoutOrNull
import okhttp3.Response
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


fun syncFeeds(context: Context, feedId: Long, tag: String) {
    try {
        runBlocking {
            val time = measureTimeMillis {
                val feedsToFetch = feedsToSync(context, feedId, tag)
                feedsToFetch
                        .map { launch(coroutineContext) { syncFeed(it, context) } }
                        .forEach {
                            Log.d("CoroutineSync", "Joining a job")
                            // Await completion of asynchronous operation
                            it.join()
                        }
                // Finally, prune excessive items
                try {
                    Cleanup.prune(context)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                } catch (e: OperationApplicationException) {
                    e.printStackTrace()
                }
                // Send notifications for configured feeds
                notifyInBackground(context)
            }
            Log.d("CoroutineSync", "Completed in $time ms")
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

private suspend fun syncFeed(feedSql: FeedSQL, context: Context) {
    Log.d("CoroutineSync", "Launching sync of ${feedSql.displayTitle} on ${Thread.currentThread().name}")
    try {
        val response: Response? =
                try {
                    fetchFeed(context, feedSql)
                } catch (t: Throwable) {
                    Log.e("CoroutineSync", "Shit hit the fan: $t")
                    null
                }

        val feed: Feed? =
                response?.let {
                    try {
                        Log.d("CoroutineSync", "Parsing ${feedSql.displayTitle} on ${Thread.currentThread().name}")
                        context.feedParser.parseFeedResponse(it)
                    } catch (t: Throwable) {
                        Log.e("CoroutineSync", "Shit hit the fan: $t")
                        null
                    }
                }

        val ops: ArrayList<ContentProviderOperation>? =
                feed?.let {
                    try {
                        Log.d("CoroutineSync", "Converting ${feedSql.displayTitle} on ${Thread.currentThread().name}")
                        convertResultToOperations(it, feedSql, context.contentResolver)
                    } catch (t: Throwable) {
                        Log.e("CoroutineSync", "Shit hit the fan: $t")
                        null
                    }
                }

        ops?.let {
            try {
                Log.d("CoroutineSync", "Storing ${feedSql.displayTitle} on ${Thread.currentThread().name}")
                storeSyncResults(context, it)
                context.contentResolver.notifyAllUris()
            } catch (t: Throwable) {
                Log.e("CoroutineSync", "Shit hit the fan: $t")
                null
            }
        }
    } catch (t: Throwable) {
        Log.e("CoroutineSync", "Something went wrong: $t")
    }
}

private suspend fun fetchFeed(context: Context, feedSql: FeedSQL,
                              timeout: Long = 2L, timeUnit: TimeUnit = TimeUnit.SECONDS): Response? {
    return withTimeoutOrNull(timeout, timeUnit) {
        Log.d("CoroutineSync", "Fetching ${feedSql.displayTitle} on ${Thread.currentThread().name}")
        context.feedParser.getResponse(feedSql.url)
    }
}

private fun feedsToSync(context: Context, feedId: Long, tag: String): List<FeedSQL> {
    return when {
        feedId > 0 -> listFeed(context, feedId)
        !tag.isEmpty() -> listFeeds(context, tag)
        else -> listFeeds(context)
    }
}

private fun listFeed(context: Context, id: Long): List<FeedSQL> {
    return context.contentResolver.getFeeds(
            FEED_FIELDS.asList(), WHEREIDIS, Arrays.asList(*LongsToStringArray(id)), null)
}

private fun listFeeds(context: Context, tag: String): List<FeedSQL> {
    return context.contentResolver.getFeeds(
            FEED_FIELDS.asList(), WhereIs(COL_TAG), Arrays.asList(*ToStringArray(tag)), null)
}

private fun listFeeds(context: Context): List<FeedSQL> {
    return context.contentResolver.getFeeds(
            FEED_FIELDS.asList(), null, null, null)
}

/**
 * @param parsedFeed
 * @param feedSQL
 * @return A list of Operations containing no back references
 */
private fun convertResultToOperations(parsedFeed: Feed,
                                      feedSQL: FeedSQL,
                                      resolver: ContentResolver): ArrayList<ContentProviderOperation> {
    val operations = ArrayList<ContentProviderOperation>()

    val feedOp = ContentProviderOperation.newUpdate(Uri.withAppendedPath(URI_FEEDS,
            java.lang.Long.toString(feedSQL.id)))

    // This can be null, in that case do not override existing value
    val selfLink: String = parsedFeed.feed_url ?: feedSQL.url.toString()

    // Populate with values
    feedOp.withValue(COL_TITLE, parsedFeed.title)
            .withValue(COL_TAG, feedSQL.tag)
            .withValue(COL_URL, selfLink)
            .withValue(COL_IMAGEURL, parsedFeed.icon)

    // Add to list of operations
    operations.add(feedOp.build())

    parsedFeed.items?.forEach { entry ->
        if (entry.id != null) {
            val itemId = resolver.getIdForFeedItem(entry.id!!, feedSQL.id)
            val itemOp: ContentProviderOperation.Builder = if (itemId < 1) {
                ContentProviderOperation.newInsert(URI_FEEDITEMS)
            } else {
                ContentProviderOperation.newUpdate(Uri.withAppendedPath(URI_FEEDITEMS,
                        itemId.toString()))
            }

            // Use the actual id, because update operation will not return id
            itemOp.withValue(COL_FEED, feedSQL.id)
                    .withValue(COL_FEEDTITLE, feedSQL.displayTitle)
                    .withValue(COL_FEEDURL, selfLink)
                    .withValue(COL_TAG, feedSQL.tag)

            entry.intoContentProviderOperation(parsedFeed, itemOp)

            // Add to list of operations
            operations.add(itemOp.build())
        }
    }

    return operations
}

@Throws(RemoteException::class, OperationApplicationException::class)
private fun storeSyncResults(context: Context,
                             operations: List<ContentProviderOperation>) {
    if (!operations.isEmpty()) {
        context.contentResolver
                .applyBatch(AUTHORITY, ArrayList(operations))
    }
}
