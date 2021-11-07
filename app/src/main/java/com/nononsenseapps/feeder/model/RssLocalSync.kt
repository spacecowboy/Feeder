package com.nononsenseapps.feeder.model

import android.content.Context
import android.util.Log
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.blob.blobFile
import com.nononsenseapps.feeder.blob.blobOutputStream
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.upsertFeed
import com.nononsenseapps.feeder.db.room.upsertFeedItems
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.Item
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.readBomAsCharset
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

val singleThreadedSync = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
val syncMutex = Mutex()

private const val LOG_TAG = "FeederRssLocalSync"

suspend fun syncFeeds(
    context: Context,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    forceNetwork: Boolean = false,
    parallel: Boolean = false,
    minFeedAgeMinutes: Int = 15,
): Boolean {
    val di: DI by closestDI(context)
    val repository: Repository by di.instance()
    Log.d(LOG_TAG, "${Thread.currentThread().name}: Taking sync mutex")
    return syncMutex.withLock {
        withContext(singleThreadedSync) {
            syncFeeds(
                di,
                filesDir = context.filesDir,
                feedId = feedId,
                feedTag = feedTag,
                maxFeedItemCount = repository.maximumCountPerFeed.value,
                forceNetwork = forceNetwork,
                parallel = parallel,
                minFeedAgeMinutes = minFeedAgeMinutes
            )
        }
    }
}

internal suspend fun syncFeeds(
    di: DI,
    filesDir: File,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    maxFeedItemCount: Int = 100,
    forceNetwork: Boolean = false,
    parallel: Boolean = false,
    minFeedAgeMinutes: Int = 15,
): Boolean {
    val db: AppDatabase by di.instance()
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

                Log.d(LOG_TAG, "Syncing ${feedsToFetch.size} feeds")

                val coroutineContext = when (parallel) {
                    true -> Dispatchers.Default
                    false -> this.coroutineContext
                } + CoroutineExceptionHandler { _, throwable ->
                    Log.e(LOG_TAG, "Error during sync", throwable)
                }

                feedsToFetch.forEach {
                    launch(coroutineContext) {
                        try {
                            syncFeed(
                                di = di,
                                feedSql = it,
                                filesDir = filesDir,
                                maxFeedItemCount = maxFeedItemCount,
                                forceNetwork = forceNetwork,
                                downloadTime = downloadTime
                            )
                        } catch (e: Throwable) {
                            Log.e(
                                LOG_TAG,
                                "Failed to sync ${it.displayTitle}: ${it.url}",
                                e
                            )
                        }
                    }
                }

                result = true
            }
        } catch (e: Throwable) {
            Log.e(LOG_TAG, "Outer error", e)
        }
    }
    Log.d(LOG_TAG, "Completed in $time ms")
    return result
}

private suspend fun syncFeed(
    di: DI,
    feedSql: com.nononsenseapps.feeder.db.room.Feed,
    filesDir: File,
    maxFeedItemCount: Int,
    forceNetwork: Boolean = false,
    downloadTime: Instant,
) {
    Log.d(LOG_TAG, "Fetching ${feedSql.displayTitle}")
    val db: AppDatabase by di.instance()
    val feedParser: FeedParser by di.instance()
    val okHttpClient: OkHttpClient by di.instance()

    val response: Response = okHttpClient.getResponse(feedSql.url, forceNetwork = forceNetwork)

    var responseHash = 0

    val feed: Feed? =
        response.use {
            val contentType = response.body?.contentType()
            val charset = response.body?.source()?.readBomAsCharset(
                contentType?.charset() ?: StandardCharsets.UTF_8
            )
            val responseBody = it.safeBody()
            responseBody?.let { body ->
                responseHash = body.contentHashCode()
                when {
                    !response.isSuccessful -> {
                        throw ResponseFailure("${response.code} when fetching ${feedSql.displayTitle}: ${feedSql.url}")
                    }
                    feedSql.responseHash == responseHash -> null // no change
                    else -> feedParser.parseFeedResponse(
                        response.request.url.toUrl(),
                        contentType,
                        charset,
                        body
                    )
                }
            }
        }?.let {
            // Double check that icon is not base64
            when {
                it.icon?.startsWith("data") == true -> it.copy(icon = null)
                else -> it
            }
        }

    // Always update the feeds last sync field
    feedSql.lastSync = Instant.now()

    if (feed == null) {
        db.feedDao().upsertFeed(feedSql)
    } else {
        val itemDao = db.feedItemDao()

        val uniqueIdCount = feed.items?.map { it.id }?.toSet()?.size
        // This can only detect between items present in one feed. See NIXOS
        val isNotUniqueIds = uniqueIdCount != feed.items?.size

        val feedItemSqls =
            feed.items
                ?.map {
                    val guid = when (isNotUniqueIds || feedSql.alternateId) {
                        true -> it.alternateId
                        else -> it.id ?: it.alternateId
                    }

                    it to guid
                }
                ?.reversed()
                ?.map { (item, guid) ->
                    // Always attempt to load existing items using both id schemes
                    // Id is rewritten to preferred on update
                    val feedItemSql =
                        itemDao.loadFeedItem(
                            guid = item.alternateId,
                            feedId = feedSql.id
                        ) ?: itemDao.loadFeedItemWithAlmostId(
                            guidPattern = "${item.id}%${item.title}",
                            feedId = feedSql.id
                        ) ?: itemDao.loadFeedItem(
                            guid = item.id ?: item.alternateId,
                            feedId = feedSql.id
                        ) ?: FeedItem(firstSyncedTime = downloadTime)

                    feedItemSql.updateFromParsedEntry(item, guid, feed)
                    feedItemSql.feedId = feedSql.id
                    feedItemSql to (item.content_html ?: item.content_text ?: "")
                } ?: emptyList()

        itemDao.upsertFeedItems(feedItemSqls) { feedItem, text ->
            if (feedSql.fullTextByDefault) {
                scheduleFullTextParse(
                    di = di,
                    feedItem = feedItem
                )
            }

            withContext(Dispatchers.IO) {
                blobOutputStream(feedItem.id, filesDir).bufferedWriter().use {
                    it.write(text)
                }
            }
        }

        // Update feed last so lastsync is only set after all items have been handled
        // for the rare case that the job is cancelled prematurely
        feedSql.responseHash = responseHash
        feedSql.title = feed.title ?: feedSql.title
        // Not changing feed url because I don't want to override auth or token params
        // See https://gitlab.com/spacecowboy/Feeder/-/issues/390
//        feedSql.url = feed.feed_url?.let { sloppyLinkToStrictURLNoThrows(it) } ?: feedSql.url
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
                Log.e(LOG_TAG, "Failed to delete $file", e)
            }
        }

        db.feedItemDao().deleteFeedItems(ids)
    }
}

internal suspend fun feedsToSync(
    feedDao: FeedDao,
    feedId: Long,
    tag: String,
    staleTime: Long = -1L
): List<com.nononsenseapps.feeder.db.room.Feed> {
    return when {
        feedId > 0 -> {
            val feed = if (staleTime > 0) feedDao.loadFeedIfStale(
                feedId,
                staleTime = staleTime
            ) else feedDao.loadFeed(feedId)
            if (feed != null) {
                listOf(feed)
            } else {
                emptyList()
            }
        }
        tag.isNotEmpty() -> if (staleTime > 0) feedDao.loadFeedsIfStale(
            tag = tag,
            staleTime = staleTime
        ) else feedDao.loadFeeds(tag)
        else -> if (staleTime > 0) feedDao.loadFeedsIfStale(staleTime) else feedDao.loadFeeds()
    }
}

class ResponseFailure(message: String?) : Exception(message)

private val Item.alternateId: String
    get() = "$id|${content_text?.hashCode()}|$title"
