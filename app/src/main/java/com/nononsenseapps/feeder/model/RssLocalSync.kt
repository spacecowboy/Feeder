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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


suspend fun syncFeeds(context: Context, feedId: Long, tag: String, forceNetwork: Boolean = false): Boolean = coroutineScope {
    var result = false
    val db = AppDatabase.getInstance(context)
    try {
        val time = measureTimeMillis {
            val staleTime: Long = if (forceNetwork) {
                DateTime.now(DateTimeZone.UTC).millis
            } else {
                DateTime.now(DateTimeZone.UTC)
                        .minusMinutes(PrefUtils.synchronizationFrequency(context).toInt())
                        .millis
            }
            val feedsToFetch = feedsToSync(db, feedId, tag, staleTime = staleTime)
            feedsToFetch.forEach {
                launch { syncFeed(it, context, forceNetwork = forceNetwork) }
            }
            Log.d("CoroutineSync", "Waiting for syncs to complete...")
            joinAll()
            result = true
            // Send notifications for configured feeds
            notify(context)
        }
        Log.d("CoroutineSync", "Completed in $time ms")

    } catch (e: Throwable) {
        e.printStackTrace()
    }
    result
}

private suspend fun syncFeed(feedSql: com.nononsenseapps.feeder.db.room.Feed,
                             context: Context, forceNetwork: Boolean = false) {
    Log.d("CoroutineSync", "Syncing ${feedSql.displayTitle} on ${Thread.currentThread().name}")
    try {
        val db = AppDatabase.getInstance(context)
        val response: Response? =
                try {
                    fetchFeed(context, feedSql, forceNetwork = forceNetwork)
                } catch (t: Throwable) {
                    Log.e("CoroutineSync", "Shit hit the fan1: $t")
                    null
                }

        if (response == null) {
            Log.e("CoroutineSync", "Timed out when fetching ${feedSql.displayTitle}")
        }

        val feed: Feed? =
                response?.let {
                    try {
                        if (response.isSuccessful) {
                            context.feedParser.parseFeedResponse(it)
                        } else {
                            Log.e("CoroutineSync", "Response fail for ${feedSql.displayTitle}: ${response.code()}")
                            null
                        }
                    } catch (t: Throwable) {
                        Log.e("CoroutineSync", "Shit hit the fan2: ${feedSql.displayTitle}, $t")
                        null
                    }
                }

        try {
            feed?.let {
                feedSql.updateFromParsedFeed(feed)
                feedSql.lastSync = DateTime.now(DateTimeZone.UTC).millis

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

                // Finally, prune database of old items
                val keepCount = PrefUtils.maximumItemCountPerFeed(context)
                db.feedItemDao().cleanItemsInFeed(feedSql.id, keepCount)
            }
        } catch (t: Throwable) {
            Log.e("CoroutineSync", "Shit hit the fan3: ${feedSql.displayTitle}, $t")
        }
    } catch (t: Throwable) {
        Log.e("CoroutineSync", "Something went wrong: $t")
    }
}

private suspend fun fetchFeed(context: Context, feedSql: com.nononsenseapps.feeder.db.room.Feed,
                              timeout: Long = 2L, timeUnit: TimeUnit = TimeUnit.SECONDS,
                              forceNetwork: Boolean = false): Response? {
    return withTimeoutOrNull(timeUnit.toMicros(timeout)) {
        context.feedParser.getResponse(feedSql.url,
                maxAgeSecs = if (forceNetwork) {
                    1
                } else {
                    MAX_FEED_AGE
                })
    }
}

internal fun feedsToSync(db: AppDatabase, feedId: Long, tag: String, staleTime: Long = -1L): List<com.nononsenseapps.feeder.db.room.Feed> {
    return when {
        feedId > 0 -> {
            val feed = if (staleTime > 0) db.feedDao().loadFeedIfStale(feedId, staleTime = staleTime) else db.feedDao().loadFeed(feedId)
            if (feed != null) {
                listOf(feed)
            } else {
                emptyList()
            }
        }
        !tag.isEmpty() -> if (staleTime > 0) db.feedDao().loadFeedsIfStale(tag = tag, staleTime = staleTime) else db.feedDao().loadFeeds(tag)
        else -> if (staleTime > 0) db.feedDao().loadFeedsIfStale(staleTime) else db.feedDao().loadFeeds()
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
