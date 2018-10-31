package com.nononsenseapps.feeder.model

import android.content.Context
import android.util.Log
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.upsertFeed
import com.nononsenseapps.feeder.db.room.upsertFeedItem
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.feedParser
import com.nononsenseapps.jsonfeed.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


suspend fun syncFeeds(context: Context, feedId: Long, tag: String,
                      forceNetwork: Boolean = false, parallel: Boolean = false): Boolean {
    var result = false
    val time = measureTimeMillis {
        coroutineScope {
            val db = AppDatabase.getInstance(context)
            try {
                val staleTime: Long = if (forceNetwork) {
                    DateTime.now(DateTimeZone.UTC).millis
                } else {
                    DateTime.now(DateTimeZone.UTC)
                            .minusMinutes(PrefUtils.synchronizationFrequency(context).toInt())
                            .millis
                }
                val feedsToFetch = feedsToSync(db, feedId, tag, staleTime = staleTime)

                feedsToFetch.map {
                    when (parallel) {
                        true -> launch(Dispatchers.Default) {
                            syncFeed(it, context, forceNetwork = forceNetwork)
                        }
                        false -> syncFeed(it, context, forceNetwork = forceNetwork)
                    }
                }

                result = true
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            Log.d("CoroutineSync", "${Thread.currentThread().name} End of scope waiting for children to complete...")
        }
    }
    Log.d("CoroutineSync", "Completed in $time ms")
    return result
}

private suspend fun syncFeed(feedSql: com.nononsenseapps.feeder.db.room.Feed,
                             context: Context, forceNetwork: Boolean = false) {
    Log.d("CoroutineSync", "${Thread.currentThread().name} Fetch ${feedSql.displayTitle}")
    try {
        val db = AppDatabase.getInstance(context)
        val response: Response? =
                try {
                    fetchFeed(context, feedSql, forceNetwork = forceNetwork)
                } catch (t: Throwable) {
                    Log.e("CoroutineSync", "Shit hit the fan1: $t")
                    null
                }

        Log.d("CoroutineSync", "${Thread.currentThread().name} Feed  ${feedSql.displayTitle}")

        if (response == null) {
            Log.e("CoroutineSync", "Timed out when fetching ${feedSql.displayTitle}")
        }

        val feed: Feed? =
                response?.let {
                    try {
                        Log.d("CoroutineSync", "response: $response")
                        Log.d("CoroutineSync", "cacheResponse: ${response?.cacheResponse()}")
                        Log.d("CoroutineSync", "networkResponse: ${response?.networkResponse()}")
                        when {
                            !response.isSuccessful -> {
                                // fail
                                Log.e("CoroutineSync", "${Thread.currentThread().name} Response fail for ${feedSql.displayTitle}: ${response.code()}")
                                null
                            }
                            feedSql.lastSync > 0 && (response.networkResponse()?.code() ?: 304) == 304 -> {
                                // no change
                                Log.d("CoroutineSync", "${Thread.currentThread().name} No change for ${feedSql.displayTitle}: ${response.networkResponse()?.code()}")
                                null
                            }
                            else -> {
                                context.feedParser.parseFeedResponse(it)
                            }
                        }
                    } catch (t: Throwable) {
                        Log.e("CoroutineSync", "Shit hit the fan2: ${feedSql.displayTitle}, $t")
                        null
                    }
                }

        try {
            @Suppress("NestedLambdaShadowedImplicitParameter")
            feed?.let {
                feedSql.updateFromParsedFeed(feed)
                feedSql.lastSync = DateTime.now(DateTimeZone.UTC).millis

                feedSql.id = db.feedDao().upsertFeed(feedSql)

                val itemDao = db.feedItemDao()

                feed.items?.asSequence()
                        ?.filter { it.id != null }
                        ?.forEach {
                            val feedItemSql = itemDao.loadFeedItem(guid = it.id!!,
                                    feedId = feedSql.id) ?: FeedItem()

                            feedItemSql.updateFromParsedEntry(it, feed)
                            feedItemSql.feedId = feedSql.id

                            itemDao.upsertFeedItem(feedItemSql)
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
        context.feedParser.getResponse(feedSql.url, forceNetwork = forceNetwork)
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
