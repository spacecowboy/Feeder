package com.nononsenseapps.feeder.archmodel

import android.database.sqlite.SQLiteConstraintException
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedForSettings
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedUnreadCount
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerFeed
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerItemWithUnreadCount
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTag
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.net.URL
import java.time.Instant

class FeedStore(override val di: DI) : DIAware {
    private val feedDao: FeedDao by instance()

    // Need only be internally consistent within the composition
    // this object outlives all compositions
    private var nextTagUiId: Long = -1000

    // But IDs need to be consistent even if tags come and go
    private val tagUiIds = mutableMapOf<String, Long>()

    private fun getTagUiId(tag: String): Long {
        return tagUiIds.getOrPut(tag) {
            --nextTagUiId
        }
    }

    suspend fun getFeed(feedId: Long): Feed? = feedDao.loadFeed(feedId)

    suspend fun getFeed(url: URL): Feed? = feedDao.loadFeedWithUrl(url)

    suspend fun saveFeed(feed: Feed): Long {
        return if (feed.id > ID_UNSET) {
            feedDao.updateFeed(feed)
            feed.id
        } else {
            feedDao.insertFeed(feed)
        }
    }

    suspend fun toggleNotifications(
        feedId: Long,
        value: Boolean,
    ) = feedDao.setNotify(id = feedId, notify = value)

    suspend fun getDisplayTitle(feedId: Long): String? = feedDao.getFeedTitle(feedId)?.displayTitle

    suspend fun deleteFeeds(feedIds: List<Long>) {
        feedDao.deleteFeeds(feedIds)
    }

    val allTags: Flow<List<String>> = feedDao.loadAllTags()

    val feedForSettings: Flow<List<FeedForSettings>> = feedDao.loadFlowOfFeedsForSettings()

    @OptIn(ExperimentalCoroutinesApi::class)
    val drawerItemsWithUnreadCounts: Flow<List<DrawerItemWithUnreadCount>> =
        feedDao.loadFlowOfFeedsWithUnreadCounts()
            .mapLatest { feeds ->
                // TODO would like to have a throttle here. Emit first immediately
                // then at most every X ms (including latest item
                // Must emit first immediately or the feed list will have a delay
                mapFeedsToSortedDrawerItems(feeds)
            }

    private fun mapFeedsToSortedDrawerItems(feeds: List<FeedUnreadCount>): List<DrawerItemWithUnreadCount> {
        var topTag = DrawerTop(unreadCount = 0, totalChildren = 0)
        val tags: MutableMap<String, DrawerTag> = mutableMapOf()
        val data: MutableList<DrawerItemWithUnreadCount> = mutableListOf()

        for (feedDbo in feeds) {
            val feed =
                DrawerFeed(
                    unreadCount = feedDbo.unreadCount,
                    tag = feedDbo.tag,
                    id = feedDbo.id,
                    displayTitle = feedDbo.displayTitle,
                    imageUrl = feedDbo.imageUrl,
                )

            data.add(feed)
            topTag =
                topTag.copy(
                    unreadCount = topTag.unreadCount + feed.unreadCount,
                    totalChildren = topTag.totalChildren + 1,
                )

            if (feed.tag.isNotEmpty()) {
                val tag =
                    tags[feed.tag] ?: DrawerTag(
                        tag = feed.tag,
                        unreadCount = 0,
                        uiId = getTagUiId(feed.tag),
                        totalChildren = 0,
                    )
                tags[feed.tag] =
                    tag.copy(
                        unreadCount = tag.unreadCount + feed.unreadCount,
                        totalChildren = tag.totalChildren + 1,
                    )
            }
        }

        data.add(topTag)
        data.addAll(tags.values)

        return data.sorted()
    }

    fun getFeedTitles(
        feedId: Long,
        tag: String,
    ): Flow<List<FeedTitle>> =
        when {
            feedId > ID_UNSET -> feedDao.getFeedTitlesWithId(feedId)
            tag.isNotBlank() -> feedDao.getFeedTitlesWithTag(tag)
            else -> feedDao.getAllFeedTitles()
        }

    fun getCurrentlySyncingLatestTimestamp(): Flow<Instant?> = feedDao.getCurrentlySyncingLatestTimestamp()

    suspend fun setCurrentlySyncingOn(
        feedId: Long,
        syncing: Boolean,
        lastSync: Instant? = null,
    ) {
        if (lastSync != null) {
            feedDao.setCurrentlySyncingOn(feedId = feedId, syncing = syncing, lastSync = lastSync)
        } else {
            feedDao.setCurrentlySyncingOn(feedId = feedId, syncing = syncing)
        }
    }

    suspend fun upsertFeed(feedSql: Feed): Long {
        return try {
            feedDao.upsert(feed = feedSql)
        } catch (e: SQLiteConstraintException) {
            // upsert only works if ID is defined - need to catch constraint errors still
            if (feedSql.id == ID_UNSET) {
                val feedId = feedDao.getFeedIdForUrl(feedSql.url)

                if (feedId != null) {
                    feedDao.upsert(feed = feedSql.copy(id = feedId))
                } else {
                    throw e
                }
            } else {
                throw e
            }
        }
    }

    suspend fun getFeedsOrderedByUrl(): List<Feed> {
        return feedDao.getFeedsOrderedByUrl()
    }

    fun getFlowOfFeedsOrderedByUrl(): Flow<List<Feed>> {
        return feedDao.getFlowOfFeedsOrderedByUrl()
    }

    suspend fun deleteFeed(url: URL) {
        feedDao.deleteFeedWithUrl(url)
    }

    suspend fun loadFeedIfStale(
        feedId: Long,
        staleTime: Long,
    ) = feedDao.loadFeedIfStale(feedId = feedId, staleTime = staleTime)

    suspend fun loadFeed(feedId: Long): Feed? = feedDao.loadFeed(feedId = feedId)

    suspend fun loadFeedsIfStale(
        tag: String,
        staleTime: Long,
    ) = feedDao.loadFeedsIfStale(tag = tag, staleTime = staleTime)

    suspend fun loadFeedsIfStale(staleTime: Long) = feedDao.loadFeedsIfStale(staleTime = staleTime)

    suspend fun loadFeeds(tag: String) = feedDao.loadFeeds(tag = tag)

    suspend fun loadFeeds() = feedDao.loadFeeds()
}
