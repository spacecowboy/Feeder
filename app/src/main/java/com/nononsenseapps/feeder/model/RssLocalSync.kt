package com.nononsenseapps.feeder.model

import android.content.Context
import android.content.OperationApplicationException
import android.os.RemoteException
import android.util.Log
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.upsertFeed
import com.nononsenseapps.feeder.db.room.upsertFeedItem
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.feedParser
import com.nononsenseapps.jsonfeed.Feed
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeoutOrNull
import okhttp3.Response
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


fun syncFeeds(context: Context, feedId: Long, tag: String, forceNetwork: Boolean = false): Boolean {
    var result = false
    val db = AppDatabase.getInstance(context)
    try {
        runBlocking {
            val time = measureTimeMillis {
                val feedsToFetch = feedsToSync(db, feedId, tag)
                feedsToFetch
                        .map { launch(coroutineContext) { syncFeed(it, context, forceNetwork = forceNetwork) } }
                        .forEach {
                            Log.d("CoroutineSync", "Joining a job")
                            // Await completion of asynchronous operation
                            it.join()
                        }
                // Finally, prune excessive items
                try {
                    prune(context, db)
                    result = true
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
    return result
}

private suspend fun syncFeed(feedSql: com.nononsenseapps.feeder.db.room.Feed,
                             context: Context, forceNetwork: Boolean = false) {
    Log.d("CoroutineSync", "Launching sync of ${feedSql.displayTitle} on ${Thread.currentThread().name}")
    try {
        val db = AppDatabase.getInstance(context)
        val response: Response? =
                try {
                    fetchFeed(context, feedSql, forceNetwork = forceNetwork)
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

        try {
            feed?.let { _ ->
                feedSql.updateFromParsedFeed(feed)

                feedSql.id = db.feedDao().upsertFeed(feedSql)

                db.runInTransaction {

                    val itemDao = db.feedItemDao()

                    feed.items?.asSequence()
                            ?.filter { it.id != null }
                            ?.map {
                                val feedItemSql = itemDao.loadFeedItem(guid = it.id!!,
                                        feedId = feedSql.id) ?: FeedItem()

                                feedItemSql.updateFromParsedEntry(it, feed)
                                feedItemSql.feedId = feedSql.id

                                itemDao.upsertFeedItem(feedItemSql)
                            }?.toList()
                }
            }
        } catch (t: Throwable) {
            Log.e("CoroutineSync", "Shit hit the fan: $t")
        }
    } catch (t: Throwable) {
        Log.e("CoroutineSync", "Something went wrong: $t")
    }
}

private suspend fun fetchFeed(context: Context, feedSql: com.nononsenseapps.feeder.db.room.Feed,
                              timeout: Long = 2L, timeUnit: TimeUnit = TimeUnit.SECONDS,
                              forceNetwork: Boolean = false): Response? {
    return withTimeoutOrNull(timeout, timeUnit) {
        Log.d("CoroutineSync", "Fetching ${feedSql.displayTitle} on ${Thread.currentThread().name}")
        context.feedParser.getResponse(feedSql.url,
                maxAgeSecs = if (forceNetwork) {
                    1
                } else {
                    MAX_FEED_AGE
                })
    }
}

private fun feedsToSync(db: AppDatabase, feedId: Long, tag: String): List<com.nononsenseapps.feeder.db.room.Feed> {
    return when {
        feedId > 0 -> {
            val feed = db.feedDao().loadFeed(feedId)
            if (feed != null) {
                listOf(feed)
            } else {
                emptyList()
            }
        }
        !tag.isEmpty() -> db.feedDao().loadFeeds(tag)
        else -> db.feedDao().loadFeeds()
    }
}

fun prune(context: Context, db: AppDatabase) {
    val keepCount = PrefUtils.maximumItemCountPerFeed(context)

    db.runInTransaction {
        db.feedDao().loadFeedIds().forEach { feedId ->
            db.feedItemDao().cleanItemsInFeed(feedId, keepCount)
        }
    }
}
