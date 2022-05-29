package com.nononsenseapps.feeder.archmodel

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.FeedItemIdWithLink
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.upsertFeedItems
import com.nononsenseapps.feeder.model.PreviewItem
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import com.nononsenseapps.feeder.ui.compose.feed.shortDateTimeFormat
import java.net.URL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class FeedItemStore(override val di: DI) : DIAware {
    private val dao: FeedItemDao by instance()

    fun getVisibleFeedItemCount(
        feedId: Long,
        tag: String,
        onlyUnread: Boolean,
    ): Flow<Int> = when {
        onlyUnread -> {
            when {
                feedId > ID_UNSET -> dao.getUnreadFeedItemCount(feedId = feedId)
                tag.isNotEmpty() -> dao.getUnreadFeedItemCount(tag = tag)
                else -> dao.getUnreadFeedItemCount()
            }
        }
        else -> {
            when {
                feedId > ID_UNSET -> dao.getFeedItemCount(feedId = feedId)
                tag.isNotEmpty() -> dao.getFeedItemCount(tag = tag)
                else -> dao.getFeedItemCount()
            }
        }
    }

    fun getPagedFeedItems(
        feedId: Long,
        tag: String,
        onlyUnread: Boolean,
        newestFirst: Boolean,
    ): Flow<PagingData<FeedListItem>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            )
        ) {
            when {
                onlyUnread && newestFirst -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingUnreadPreviewsDesc(feedId = feedId)
                        tag.isNotEmpty() -> dao.pagingUnreadPreviewsDesc(tag = tag)
                        else -> dao.pagingUnreadPreviewsDesc()
                    }
                }
                onlyUnread -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingUnreadPreviewsAsc(feedId = feedId)
                        tag.isNotEmpty() -> dao.pagingUnreadPreviewsAsc(tag = tag)
                        else -> dao.pagingUnreadPreviewsAsc()
                    }
                }
                newestFirst -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingPreviewsDesc(feedId = feedId)
                        tag.isNotEmpty() -> dao.pagingPreviewsDesc(tag = tag)
                        else -> dao.pagingPreviewsDesc()
                    }
                }
                else -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingPreviewsAsc(feedId = feedId)
                        tag.isNotEmpty() -> dao.pagingPreviewsAsc(tag = tag)
                        else -> dao.pagingPreviewsAsc()
                    }
                }
            }
        }.flow.map { pagingData ->
            pagingData
                .map { it.toFeedListItem() }
        }

    suspend fun markAsNotified(itemIds: List<Long>) {
        dao.markAsNotified(itemIds)
    }

    suspend fun markAsRead(itemIds: List<Long>) {
        dao.markAsRead(itemIds)
    }

    suspend fun markAsReadAndNotified(itemId: Long) {
        dao.markAsReadAndNotified(itemId)
    }

    suspend fun markAsUnread(itemId: Long, unread: Boolean) {
        dao.markAsRead(itemId, unread)
    }

    suspend fun setPinned(itemId: Long, pinned: Boolean) {
        dao.setPinned(itemId, pinned)
    }

    suspend fun setBookmarked(itemId: Long, bookmarked: Boolean) {
        dao.setBookmarked(itemId, bookmarked)
    }

    suspend fun getFullTextByDefault(itemId: Long): Boolean {
        return dao.getFullTextByDefault(itemId) ?: false
    }

    fun getFeedItem(itemId: Long): Flow<FeedItemWithFeed?> {
        return dao.loadFeedItemFlow(itemId)
    }

    suspend fun getFeedItemId(feedUrl: URL, articleGuid: String): Long? {
        return dao.getItemWith(feedUrl = feedUrl, articleGuid = articleGuid)
    }

    suspend fun getLink(itemId: Long): String? {
        return dao.getLink(itemId)
    }

    suspend fun getArticleOpener(itemId: Long): String? {
        return dao.getOpenArticleWith(itemId)
    }

    suspend fun markAllAsReadInFeed(feedId: Long) {
        dao.markAllAsRead(feedId)
    }

    suspend fun markAllAsReadInTag(tag: String) {
        dao.markAllAsRead(tag)
    }

    suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    suspend fun markBeforeAsRead(
        index: Int,
        feedId: Long,
        tag: String,
        onlyUnread: Boolean,
        newestFirst: Boolean
    ) {
        val offset = 0
        when {
            onlyUnread && newestFirst -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadDesc(
                        offset = offset,
                        limit = index,
                        feedId = feedId,
                        onlyUnread = 1
                    )
                    tag.isNotEmpty() -> dao.markAsReadDesc(
                        offset = offset,
                        limit = index,
                        tag = tag,
                        onlyUnread = 1
                    )
                    else -> dao.markAsReadDesc(
                        offset = offset,
                        limit = index,
                        onlyUnread = 1
                    )
                }
            }
            onlyUnread -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadAsc(
                        offset = offset,
                        limit = index,
                        feedId = feedId,
                        onlyUnread = 1
                    )
                    tag.isNotEmpty() -> dao.markAsReadAsc(
                        offset = offset,
                        limit = index,
                        tag = tag,
                        onlyUnread = 1
                    )
                    else -> dao.markAsReadAsc(
                        offset = offset,
                        limit = index,
                        onlyUnread = 1
                    )
                }
            }
            newestFirst -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadDesc(
                        offset = offset,
                        limit = index,
                        feedId = feedId,
                        onlyUnread = 0
                    )
                    tag.isNotEmpty() -> dao.markAsReadDesc(
                        offset = offset,
                        limit = index,
                        tag = tag,
                        onlyUnread = 0
                    )
                    else -> dao.markAsReadDesc(
                        offset = offset,
                        limit = index,
                        onlyUnread = 0
                    )
                }
            }
            else -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadAsc(
                        offset = offset,
                        limit = index,
                        feedId = feedId,
                        onlyUnread = 0
                    )
                    tag.isNotEmpty() -> dao.markAsReadAsc(
                        offset = offset,
                        limit = index,
                        tag = tag,
                        onlyUnread = 0
                    )
                    else -> dao.markAsReadAsc(
                        offset = offset,
                        limit = index,
                        onlyUnread = 0
                    )
                }
            }
        }
    }

    suspend fun markAfterAsRead(
        index: Int,
        feedId: Long,
        tag: String,
        onlyUnread: Boolean,
        newestFirst: Boolean
    ) {
        val offset = index + 1
        when {
            onlyUnread && newestFirst -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadDesc(
                        offset = offset,
                        feedId = feedId,
                        onlyUnread = 1
                    )
                    tag.isNotEmpty() -> dao.markAsReadDesc(
                        offset = offset,
                        tag = tag,
                        onlyUnread = 1
                    )
                    else -> dao.markAsReadDesc(
                        offset = offset,
                        onlyUnread = 1
                    )
                }
            }
            onlyUnread -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadAsc(
                        offset = offset,
                        feedId = feedId,
                        onlyUnread = 1
                    )
                    tag.isNotEmpty() -> dao.markAsReadAsc(
                        offset = offset,
                        tag = tag,
                        onlyUnread = 1
                    )
                    else -> dao.markAsReadAsc(
                        offset = offset,
                        onlyUnread = 1
                    )
                }
            }
            newestFirst -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadDesc(
                        offset = offset,
                        feedId = feedId,
                        onlyUnread = 0
                    )
                    tag.isNotEmpty() -> dao.markAsReadDesc(
                        offset = offset,
                        tag = tag,
                        onlyUnread = 0
                    )
                    else -> dao.markAsReadDesc(
                        offset = offset,
                        onlyUnread = 0
                    )
                }
            }
            else -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadAsc(
                        offset = offset,
                        feedId = feedId,
                        onlyUnread = 0
                    )
                    tag.isNotEmpty() -> dao.markAsReadAsc(
                        offset = offset,
                        tag = tag,
                        onlyUnread = 0
                    )
                    else -> dao.markAsReadAsc(
                        offset = offset,
                        onlyUnread = 0
                    )
                }
            }
        }
    }

    fun getFeedsItemsWithDefaultFullTextParse(): Flow<List<FeedItemIdWithLink>> =
        dao.getFeedsItemsWithDefaultFullTextParse()

    fun getFeedItemsNeedingNotifying(): Flow<List<Long>> {
        return dao.getFeedItemsNeedingNotifying()
    }

    suspend fun loadFeedItem(guid: String, feedId: Long): FeedItem? =
        dao.loadFeedItem(guid = guid, feedId = feedId)

    suspend fun upsertFeedItems(
        itemsWithText: List<Pair<FeedItem, String>>,
        block: suspend (FeedItem, String) -> Unit
    ) {
        dao.upsertFeedItems(itemsWithText = itemsWithText, block = block)
    }

    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, keepCount: Int) =
        dao.getItemsToBeCleanedFromFeed(feedId = feedId, keepCount = keepCount)

    suspend fun deleteFeedItems(ids: List<Long>) {
        dao.deleteFeedItems(ids)
    }

    companion object {
        private const val PAGE_SIZE = 100
    }
}

private fun PreviewItem.toFeedListItem() =
    FeedListItem(
        id = id,
        title = plainTitle,
        snippet = plainSnippet,
        feedTitle = feedDisplayTitle,
        unread = unread,
        pubDate = pubDate?.toLocalDate()?.format(shortDateTimeFormat) ?: "",
        imageUrl = imageUrl,
        link = link,
        pinned = pinned,
        bookmarked = bookmarked,
    )
