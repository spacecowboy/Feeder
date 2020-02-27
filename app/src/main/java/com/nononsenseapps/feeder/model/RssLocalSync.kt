package com.nononsenseapps.feeder.model

import android.content.Context
import android.util.Log
import com.nononsenseapps.feeder.blob.blobFile
import com.nononsenseapps.feeder.blob.blobOutputStream
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.upsertFeed
import com.nononsenseapps.feeder.db.room.upsertFeedItem
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import com.nononsenseapps.jsonfeed.Feed
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.Response
import org.kodein.di.Kodein
import org.kodein.di.android.closestKodein
import org.kodein.di.direct
import org.kodein.di.generic.instance
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.system.measureTimeMillis

@FlowPreview
suspend fun syncFeeds(context: Context,
                      feedId: Long = ID_UNSET,
                      feedTag: String = "",
                      forceNetwork: Boolean = false,
                      parallel: Boolean = false,
                      minFeedAgeMinutes: Int = 15): Boolean {
    val kodein: Kodein by closestKodein(context)
    val prefs: Prefs by kodein.instance()
    return syncFeeds(
            db = kodein.direct.instance(),
            filesDir = context.filesDir,
            feedParser = kodein.direct.instance(),
            feedId = feedId,
            feedTag = feedTag,
            maxFeedItemCount = prefs.maximumCountPerFeed,
            forceNetwork = forceNetwork,
            parallel = parallel,
            minFeedAgeMinutes = minFeedAgeMinutes
    )
}

@FlowPreview
internal suspend fun syncFeeds(db: AppDatabase,
                               filesDir: File,
                               feedParser: FeedParser,
                               feedId: Long = ID_UNSET,
                               feedTag: String = "",
                               maxFeedItemCount: Int = 100,
                               forceNetwork: Boolean = false,
                               parallel: Boolean = false,
                               minFeedAgeMinutes: Int = 15): Boolean {
    var result = false
    // Let all new items share download time
    val downloadTime = Instant.now()
    val time = measureTimeMillis {
        try {
            supervisorScope {
                val staleTime: Long = if (forceNetwork) {
                    Instant.now().toEpochMilli()
                } else {
                    Instant.now().minus(minFeedAgeMinutes.toLong(), ChronoUnit.MINUTES)
                            .toEpochMilli()
                }
                val feedsToFetch = feedsToSync(db.feedDao(), feedId, feedTag, staleTime = staleTime)

                Log.d("CoroutineSync", "Syncing ${feedsToFetch.size} feeds")

                val coroutineContext = when (parallel) {
                    true -> Dispatchers.Default
                    false -> this.coroutineContext
                } + CoroutineExceptionHandler { _, throwable ->
                    Log.e("CoroutineSync", "Error during sync: ${throwable.message}")
                }

                feedsToFetch.forEach {
                    launch(coroutineContext) {
                        syncFeed(it,
                                db = db,
                                filesDir = filesDir,
                                feedParser = feedParser,
                                maxFeedItemCount = maxFeedItemCount,
                                forceNetwork = forceNetwork,
                                downloadTime = downloadTime)
                    }
                }

                result = true
            }
        } catch (e: Throwable) {
            Log.e("CoroutineSync", "Outer error: ${e.message}")
        }
    }
    Log.d("CoroutineSync", "Completed in $time ms")
    return result
}

@FlowPreview
private suspend fun syncFeed(feedSql: com.nononsenseapps.feeder.db.room.Feed,
                             db: AppDatabase,
                             filesDir: File,
                             feedParser: FeedParser,
                             maxFeedItemCount: Int,
                             forceNetwork: Boolean = false,
                             downloadTime: Instant) = withContext(Dispatchers.IO) {
    val response: Response = fetchFeed(feedParser, feedSql, forceNetwork = forceNetwork)

    var responseHash = 0

    val feed: Feed? =
            response.use {
                it.body()?.use { responseBody ->
                    val body = responseBody.bytes()!!
                    responseHash = body.contentHashCode()
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
    feedSql.lastSync = Instant.now()

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
                feed.items?.map {
                    "${it.title}-${it.summary}"
                }
            }
        } ?: emptyList()

        feed.items?.zip(itemIds)
                ?.reversed()
                ?.forEach { (item, id) ->
                    val feedItemSql = itemDao.loadFeedItem(guid = id,
                            feedId = feedSql.id) ?: FeedItem(firstSyncedTime = downloadTime)

                    feedItemSql.updateFromParsedEntry(item.copy(id = id), feed)
                    feedItemSql.feedId = feedSql.id

                    val feedItemId = itemDao.upsertFeedItem(feedItemSql)
                    val text = item.content_html ?: item.content_text ?: ""
                    blobOutputStream(feedItemId, filesDir).bufferedWriter().use {
                        it.write(text)
                    }
                }

        // Update feed last so lastsync is only set after all items have been handled
        // for the rare case that the job is cancelled prematurely
        feedSql.responseHash = responseHash
        feedSql.title = feed.title ?: feedSql.title
        feedSql.url = feed.feed_url?.let { sloppyLinkToStrictURLNoThrows(it) } ?: feedSql.url
        feedSql.imageUrl = feed.icon?.let { sloppyLinkToStrictURLNoThrows(it) }
                ?: feedSql.imageUrl
        db.feedDao().upsertFeed(feedSql)

        // Finally, prune database of old items
        val ids = db.feedItemDao().getItemsToBeCleanedFromFeed(
                feedId = feedSql.id,
                keepCount = max(maxFeedItemCount, feed.items?.size ?: 0)
        )

        for (id in ids) {
            val file = blobFile(itemId = id, filesDir = filesDir)
            try {
                if (file.isFile) {
                    file.delete()
                }
            } catch (e: IOException) {
                Log.e("CoroutineSync", "Failed to delete $file")
            }
        }

        db.feedItemDao().deleteFeedItems(ids)
    }
}

@FlowPreview
private suspend fun fetchFeed(
        feedParser: FeedParser,
        feedSql: com.nononsenseapps.feeder.db.room.Feed,
        forceNetwork: Boolean = false
): Response =
        feedParser.getResponse(feedSql.url, forceNetwork = forceNetwork)

internal suspend fun feedsToSync(feedDao: FeedDao, feedId: Long, tag: String, staleTime: Long = -1L): List<com.nononsenseapps.feeder.db.room.Feed> {
    return when {
        feedId > 0 -> {
            val feed = if (staleTime > 0) feedDao.loadFeedIfStale(feedId, staleTime = staleTime) else feedDao.loadFeed(feedId)
            if (feed != null) {
                listOf(feed)
            } else {
                emptyList()
            }
        }
        !tag.isEmpty() -> if (staleTime > 0) feedDao.loadFeedsIfStale(tag = tag, staleTime = staleTime) else feedDao.loadFeeds(tag)
        else -> if (staleTime > 0) feedDao.loadFeedsIfStale(staleTime) else feedDao.loadFeeds()
    }
}

class ResponseFailure(message: String?) : Exception(message)
