package com.nononsenseapps.feeder.archmodel

import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
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

class FeedStore(override val di: DI) : DIAware {
    private val feedDao: FeedDao by instance()

    suspend fun getFeed(feedId: Long): Feed? = feedDao.loadFeed(feedId)

    suspend fun saveFeed(feed: Feed): Long {
        return if (feed.id > ID_UNSET) {
            feedDao.updateFeed(feed)
            feed.id
        } else {
            feedDao.insertFeed(feed)
        }
    }

    suspend fun getDisplayTitle(feedId: Long): String? =
        feedDao.getFeedTitle(feedId)?.displayTitle

    suspend fun deleteFeeds(feedIds: List<Long>) {
        feedDao.deleteFeeds(feedIds)
    }

    val allTags: Flow<List<String>> = feedDao.loadAllTags()

    @OptIn(ExperimentalCoroutinesApi::class)
    val drawerItemsWithUnreadCounts: Flow<List<DrawerItemWithUnreadCount>> =
        feedDao.loadFlowOfFeedsWithUnreadCounts()
            .mapLatest { feeds ->
                mapFeedsToSortedDrawerItems(feeds)
            }

    private fun mapFeedsToSortedDrawerItems(
        feeds: List<FeedUnreadCount>,
    ): List<DrawerItemWithUnreadCount> {
        var topTag = DrawerTop(unreadCount = 0)
        val tags: MutableMap<String, DrawerTag> = mutableMapOf()
        val data: MutableList<DrawerItemWithUnreadCount> = mutableListOf()

        for (feedDbo in feeds) {
            val feed = DrawerFeed(
                unreadCount = feedDbo.unreadCount,
                tag = feedDbo.tag,
                id = feedDbo.id,
                displayTitle = feedDbo.displayTitle
            )

            data.add(feed)
            topTag = topTag.copy(unreadCount = topTag.unreadCount + feed.unreadCount)

            if (feed.tag.isNotEmpty()) {
                val tag = tags[feed.tag] ?: DrawerTag(tag = feed.tag, unreadCount = 0)
                tags[feed.tag] = tag.copy(unreadCount = tag.unreadCount + feed.unreadCount)
            }
        }

        data.add(topTag)
        data.addAll(tags.values)

        return data.sorted()
    }

    fun getFeedTitles(feedId: Long, tag: String): Flow<List<FeedTitle>> =
        when {
            feedId > ID_UNSET -> feedDao.getFeedTitlesWithId(feedId)
            tag.isNotBlank() -> feedDao.getFeedTitlesWithTag(tag)
            else -> feedDao.getAllFeedTitles()
        }
}
