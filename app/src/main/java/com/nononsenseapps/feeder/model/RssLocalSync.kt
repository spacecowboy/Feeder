package com.nononsenseapps.feeder.model

import android.util.Log
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.upsertFeed
import com.nononsenseapps.feeder.db.room.upsertFeedItem
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import com.nononsenseapps.jsonfeed.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


suspend fun syncFeeds(db: AppDatabase,
                      feedParser: FeedParser,
                      feedId: Long = ID_UNSET,
                      tag: String = "",
                      maxFeedItemCount: Int = 100,
                      forceNetwork: Boolean = false,
                      parallel: Boolean = false,
                      minFeedAgeMinutes: Int = 15): Boolean {
    var result = false
    val time = measureTimeMillis {
        coroutineScope {
            try {
                val staleTime: Long = if (forceNetwork) {
                    DateTime.now(DateTimeZone.UTC).millis
                } else {
                    DateTime.now(DateTimeZone.UTC)
                            .minusMinutes(minFeedAgeMinutes)
                            .millis
                }
                val feedsToFetch = feedsToSync(db, feedId, tag, staleTime = staleTime)

                val coroutineContext = when (parallel) {
                    true -> Dispatchers.Default
                    false -> this.coroutineContext
                }

                feedsToFetch.forEach {
                    launch(coroutineContext) {
                        syncFeed(it,
                                db = db,
                                feedParser = feedParser,
                                maxFeedItemCount = maxFeedItemCount,
                                forceNetwork = forceNetwork)
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
                             db: AppDatabase,
                             feedParser: FeedParser,
                             maxFeedItemCount: Int,
                             forceNetwork: Boolean = false) {
    Log.d("CoroutineSync", "${Thread.currentThread().name} Fetch ${feedSql.displayTitle}")
    try {
        val response: Response? =
                try {
                    fetchFeed(feedParser, feedSql, forceNetwork = forceNetwork)
                } catch (t: Throwable) {
                    Log.e("CoroutineSync", "Shit hit the fan1: $t")
                    null
                }

        Log.d("CoroutineSync", "${Thread.currentThread().name} Feed  ${feedSql.displayTitle}")

        if (response == null) {
            Log.e("CoroutineSync", "Timed out when fetching ${feedSql.displayTitle}")
        }

        var responseHash = 0

        val feed: Feed? =
                response?.use {
                    it.body()?.use { responseBody ->
                        try {
                            val body = responseBody.bytes()!!
                            responseHash = Arrays.hashCode(body)
                            Log.d("CoroutineSync", "response: $response")
                            Log.d("CoroutineSync", "cacheResponse: ${response.cacheResponse()}")
                            Log.d("CoroutineSync", "networkResponse: ${response.networkResponse()}")
                            when {
                                !response.isSuccessful -> {
                                    // fail
                                    Log.e("CoroutineSync", "${Thread.currentThread().name} Response fail for ${feedSql.displayTitle}: ${response.code()}")
                                    null
                                }
                                feedSql.responseHash == responseHash -> {
                                    // no change
                                    Log.d("CoroutineSync", "${Thread.currentThread().name} No hash change for ${feedSql.displayTitle}: ${response.networkResponse()?.code()}")
                                    null
                                }
                                else -> {
                                    feedParser.parseFeedResponse(it, body)
                                }
                            }
                        } catch (t: Throwable) {
                            Log.e("CoroutineSync", "Shit hit the fan2: ${feedSql.displayTitle}, $t")
                            null
                        }
                    }
                }

        try {
            // Always update the feeds last sync field
            feedSql.lastSync = DateTime.now(DateTimeZone.UTC).millis

            if (feed == null) {
                db.feedDao().upsertFeed(feedSql)
            } else  {
                feedSql.responseHash = responseHash
                parseFeedContent(db, feed, feedSql)
                // Finally, prune database of old items
                db.feedItemDao().cleanItemsInFeed(feedSql.id, maxFeedItemCount)
            }
        } catch (t: Throwable) {
            Log.e("CoroutineSync", "Shit hit the fan3: ${feedSql.displayTitle}, $t")
        }
    } catch (t: Throwable) {
        Log.e("CoroutineSync", "Something went wrong: $t")
    }
}

private fun parseFeedContent(db: AppDatabase, feed: Feed, feedSql: com.nononsenseapps.feeder.db.room.Feed) {
    feedSql.title = feed.title ?: feedSql.title
    feedSql.url = feed.feed_url?.let { sloppyLinkToStrictURLNoThrows(it) } ?: feedSql.url
    feedSql.imageUrl = feed.icon?.let { sloppyLinkToStrictURLNoThrows(it) } ?: feedSql.imageUrl

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
}

private suspend fun fetchFeed(feedParser: FeedParser, feedSql: com.nononsenseapps.feeder.db.room.Feed,
                              timeout: Long = 2L, timeUnit: TimeUnit = TimeUnit.SECONDS,
                              forceNetwork: Boolean = false): Response? {
    return withTimeoutOrNull(timeUnit.toMicros(timeout)) {
        feedParser.getResponse(feedSql.url, forceNetwork = forceNetwork)
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
