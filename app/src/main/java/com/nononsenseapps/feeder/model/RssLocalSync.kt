package com.nononsenseapps.feeder.model

import android.content.Context
import android.util.Log
import com.nononsenseapps.feeder.db.room.*
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.feedParser
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

suspend fun syncFeeds(context: Context,
                      feedId: Long = ID_UNSET,
                      feedTag: String = "",
                      forceNetwork: Boolean = false,
                      parallel: Boolean = false,
                      minFeedAgeMinutes: Int = 15): Boolean =
        syncFeeds(
                db = AppDatabase.getInstance(context),
                feedParser = context.feedParser,
                feedId = feedId,
                feedTag = feedTag,
                maxFeedItemCount = PrefUtils.maximumItemCountPerFeed(context),
                forceNetwork = forceNetwork,
                parallel = parallel,
                minFeedAgeMinutes = minFeedAgeMinutes
        )

internal suspend fun syncFeeds(db: AppDatabase,
                               feedParser: FeedParser,
                               feedId: Long = ID_UNSET,
                               feedTag: String = "",
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
                val feedsToFetch = feedsToSync(db, feedId, feedTag, staleTime = staleTime)

                Log.d("CoroutineSync", "Syncing ${feedsToFetch.size} feeds")

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
    try {
        val response: Response = fetchFeed(feedParser, feedSql, forceNetwork = forceNetwork)
                ?: throw ResponseFailure("Timed out when fetching ${feedSql.url}")

        var responseHash = 0

        val feed: Feed? =
                response.use {
                    it.body()?.use { responseBody ->
                        val body = responseBody.bytes()!!
                        responseHash = Arrays.hashCode(body)
                        when {
                            !response.isSuccessful -> {
                                throw ResponseFailure("${response.code()} when fetching ${feedSql.displayTitle}: ${feedSql.url}")
                            }
                            feedSql.responseHash == responseHash -> null // no change
                            else -> feedParser.parseFeedResponse(it, body)
                        }
                    }
                }

        // Always update the feeds last sync field
        feedSql.lastSync = DateTime.now(DateTimeZone.UTC)

        if (feed == null) {
            db.feedDao().upsertFeed(feedSql)
        } else {
            val itemDao = db.feedItemDao()
            val idCount = feed.items?.map { it.id ?: 0 }?.toSet()?.size

            val itemIds = when (idCount == feed.items?.size) {
                true -> {
                    feed.items?.map { it.id ?: "shouldnotbepossible" }
                }
                false -> {
                    feed.items?.mapIndexed { i, it ->
                        "${it.title}-${it.summary}"
                    }
                }
            } ?: emptyList()

            feed.items?.zip(itemIds)
                    ?.forEach { (item, id) ->
                        val feedItemSql = itemDao.loadFeedItem(guid = id,
                                feedId = feedSql.id) ?: FeedItem()

                        feedItemSql.updateFromParsedEntry(item.copy(id = id), feed)
                        feedItemSql.feedId = feedSql.id

                        itemDao.upsertFeedItem(feedItemSql)
                    }

            // Update feed last so lastsync is only set after all items have been handled
            // for the rare case that the job is cancelled prematurely
            feedSql.responseHash = responseHash
            feedSql.title = feed.title ?: feedSql.title
            feedSql.url = feed.feed_url?.let { sloppyLinkToStrictURLNoThrows(it) } ?: feedSql.url
            feedSql.imageUrl = feed.icon?.let { sloppyLinkToStrictURLNoThrows(it) } ?: feedSql.imageUrl
            db.feedDao().upsertFeed(feedSql)

            // Finally, prune database of old items
            db.feedItemDao().cleanItemsInFeed(feedSql.id, maxFeedItemCount)
        }
    } catch (e: ResponseFailure) {
        Log.e("CoroutineSync", "Failed to fetch ${feedSql.displayTitle}: ${e.message}")
    } catch (t: Throwable) {
        Log.e("CoroutineSync", "Something went wrong: $t")
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

class ResponseFailure(message: String?) : Exception(message)
