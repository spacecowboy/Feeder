package com.nononsenseapps.feeder.model

import android.content.Context
import android.util.Log
import com.nononsenseapps.feeder.archmodel.FeedStore
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.blob.blobFile
import com.nononsenseapps.feeder.blob.blobOutputStream
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.sync.SyncRestClient
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.Item
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

val singleThreadedSync = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
val syncMutex = Mutex()

private const val LOG_TAG = "FEEDER_RssLocalSync"

suspend fun syncFeeds(
    context: Context,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    forceNetwork: Boolean = false,
    minFeedAgeMinutes: Int = 5,
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
                minFeedAgeMinutes = minFeedAgeMinutes,
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
    minFeedAgeMinutes: Int = 5,
): Boolean {
    val feedStore: FeedStore by di.instance()
    val db: AppDatabase by di.instance()
    val syncClient: SyncRestClient by di.instance()
    var result = false
    var needFullTextSync = false
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
                // Get read marks in background - this blocks sync until done
                val getReadMarksJob = launch(coroutineContext) {
                    try {
                        syncClient.getRead()
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Error when syncing readmarks in sync. ${e.message}", e)
                    }
                }
                // Fetch new feeds first
                try {
                    syncClient.getFeeds()
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Error when fetching new feeds in sync. ${e.message}", e)
                }

                val feedsToFetch = feedsToSync(db.feedDao(), feedId, feedTag, staleTime = staleTime)

                Log.d(LOG_TAG, "Syncing ${feedsToFetch.size} feeds")

                val coroutineContext =
                    Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
                        Log.e(LOG_TAG, "Error during sync", throwable)
                    }

                // Fetch latest read marks and send possible feed updates
                launch(coroutineContext) {
                    try {
                        syncClient.getDevices()
                        syncClient.sendUpdatedFeeds()
                        syncClient.markAsRead()
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Error when syncing data in sync. ${e.message}", e)
                    }
                }

                val jobs = feedsToFetch.map {
                    needFullTextSync = needFullTextSync || it.fullTextByDefault
                    launch(coroutineContext) {
                        getReadMarksJob.join()
                        try {
                            // Want unique sync times so UI gets updated state
                            feedStore.setCurrentlySyncingOn(
                                feedId = it.id,
                                syncing = true,
                                lastSync = Instant.now(),
                            )
                            syncFeed(
                                di = di,
                                feedSql = it,
                                filesDir = filesDir,
                                maxFeedItemCount = maxFeedItemCount,
                                forceNetwork = forceNetwork,
                                downloadTime = downloadTime,
                            )
                        } catch (e: Throwable) {
                            Log.e(
                                LOG_TAG,
                                "Failed to sync ${it.displayTitle}: ${it.url}",
                                e
                            )
                        } finally {
                            feedStore.setCurrentlySyncingOn(feedId = it.id, syncing = false)
                        }
                    }
                }

                jobs.joinAll()
                try {
                    val repository: Repository by di.instance()

                    repository.applyRemoteReadMarks()
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Error on final apply", e)
                }

                result = true
            }
        } catch (e: Throwable) {
            Log.e(LOG_TAG, "Outer error", e)
        } finally {
            if (needFullTextSync) {
                scheduleFullTextParse(
                    di = di,
                )
            }
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
    val repository: Repository by di.instance()
    val feedParser: FeedParser by di.instance()
    val okHttpClient: OkHttpClient by di.instance()

    val response: Response = okHttpClient.getResponse(feedSql.url, forceNetwork = forceNetwork)

    val feed: Feed? =
        response.use {
            response.body?.let { responseBody ->
                when {
                    !response.isSuccessful -> {
                        throw ResponseFailure("${response.code} when fetching ${feedSql.displayTitle}: ${feedSql.url}")
                    }
                    else -> feedParser.parseFeedResponse(
                        response.request.url.toUrl(),
                        responseBody
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
        repository.upsertFeed(feedSql)
    } else {
        val items = feed.items
        val uniqueIdCount = items?.map { it.id }?.toSet()?.size
        // This can only detect between items present in one feed. See NIXOS
        val isNotUniqueIds = uniqueIdCount != items?.size

        val alreadyReadGuids = repository.getGuidsWhichAreSyncedAsReadInFeed(feedSql)

        val feedItemSqls =
            items
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
                        repository.loadFeedItem(
                            guid = item.alternateId,
                            feedId = feedSql.id
                        ) ?: repository.loadFeedItem(
                            guid = item.id ?: item.alternateId,
                            feedId = feedSql.id
                        ) ?: FeedItem(firstSyncedTime = downloadTime)

                    feedItemSql.updateFromParsedEntry(item, guid, feed)
                    feedItemSql.feedId = feedSql.id

                    if (feedItemSql.guid in alreadyReadGuids) {
                        feedItemSql.unread = false
                        feedItemSql.notified = true
                    }

                    feedItemSql to (item.content_html ?: item.content_text ?: "")
                } ?: emptyList()

        repository.upsertFeedItems(feedItemSqls) { feedItem, text ->
            withContext(Dispatchers.IO) {
                blobOutputStream(feedItem.id, filesDir).bufferedWriter().use {
                    it.write(text)
                }
            }
        }

        // Update feed last so lastsync is only set after all items have been handled
        // for the rare case that the job is cancelled prematurely
        feedSql.title = feed.title ?: feedSql.title
        // Not changing feed url because I don't want to override auth or token params
        // See https://gitlab.com/spacecowboy/Feeder/-/issues/390
//        feedSql.url = feed.feed_url?.let { sloppyLinkToStrictURLNoThrows(it) } ?: feedSql.url
        feedSql.imageUrl = feed.icon?.let { sloppyLinkToStrictURLNoThrows(it) }
            ?: feedSql.imageUrl
        repository.upsertFeed(feedSql)

        // Finally, prune database of old items
        val ids = repository.getItemsToBeCleanedFromFeed(
            feedId = feedSql.id,
            keepCount = max(maxFeedItemCount, items?.size ?: 0)
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

        repository.deleteFeedItems(ids)
        repository.deleteStaleRemoteReadMarks()
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

/**
 * Remember that text or title literally can mean injection problems if the contain % or similar,
 * so do NOT use them literally
 */
private val Item.alternateId: String
    get() = "$id|${content_text.hashCode()}|${title.hashCode()}"
