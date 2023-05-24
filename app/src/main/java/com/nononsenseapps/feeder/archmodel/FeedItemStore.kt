package com.nononsenseapps.feeder.archmodel

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.FeedItemCursor
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.FeedItemIdWithLink
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.db.room.ID_SAVED_ARTICLES
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

    fun getFeedItemCount(
        feedId: Long,
        tag: String,
        onlyUnread: Boolean,
    ): Flow<Int> =
        when {
            feedId > ID_UNSET -> dao.getFeedItemCount(
                feedId = feedId,
                unread = onlyUnread,
                bookmarked = false,
            )

            tag.isNotEmpty() -> dao.getFeedItemCount(
                tag = tag,
                unread = onlyUnread,
                bookmarked = false,
            )

            feedId == ID_SAVED_ARTICLES -> dao.getFeedItemCount(
                unread = onlyUnread,
                bookmarked = true,
            )

            else -> dao.getFeedItemCount(unread = onlyUnread, bookmarked = false)
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
                enablePlaceholders = false,
            ),
        ) {
            val onlyBookmarks = feedId == ID_SAVED_ARTICLES
            when {
                newestFirst -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingUnreadPreviewsDesc(
                            feedId = feedId,
                            unread = onlyUnread,
                            bookmarked = onlyBookmarks,
                        )

                        tag.isNotEmpty() -> dao.pagingUnreadPreviewsDesc(
                            tag = tag,
                            unread = onlyUnread,
                            bookmarked = onlyBookmarks,
                        )

                        else -> dao.pagingUnreadPreviewsDesc(
                            unread = onlyUnread,
                            bookmarked = onlyBookmarks,
                        )
                    }
                }

                else -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingUnreadPreviewsAsc(
                            feedId = feedId,
                            unread = onlyUnread,
                            bookmarked = onlyBookmarks,
                        )

                        tag.isNotEmpty() -> dao.pagingUnreadPreviewsAsc(
                            tag = tag,
                            unread = onlyUnread,
                            bookmarked = onlyBookmarks,
                        )

                        else -> dao.pagingUnreadPreviewsAsc(
                            unread = onlyUnread,
                            bookmarked = onlyBookmarks,
                        )
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

    suspend fun markAsRead(
        feedId: Long,
        tag: String,
        onlyUnread: Boolean,
        descending: Boolean,
        cursor: FeedItemCursor,
    ) {
        val onlyBookmarks = feedId == ID_SAVED_ARTICLES
        when {
            descending -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadDesc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        feedId = feedId,
                        onlyUnread = onlyUnread,
                        onlyBookmarked = onlyBookmarks,
                    )

                    tag.isNotEmpty() -> dao.markAsReadDesc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        tag = tag,
                        onlyUnread = onlyUnread,
                        onlyBookmarked = onlyBookmarks,
                    )

                    else -> dao.markAsReadDesc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        onlyUnread = onlyUnread,
                        onlyBookmarked = onlyBookmarks,
                    )
                }
            }

            else -> {
                when {
                    feedId > ID_UNSET -> dao.markAsReadAsc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        feedId = feedId,
                        onlyUnread = onlyUnread,
                        onlyBookmarked = onlyBookmarks,
                    )

                    tag.isNotEmpty() -> dao.markAsReadAsc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        tag = tag,
                        onlyUnread = onlyUnread,
                        onlyBookmarked = onlyBookmarks,
                    )

                    else -> dao.markAsReadAsc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        onlyUnread = onlyUnread,
                        onlyBookmarked = onlyBookmarks,
                    )
                }
            }
        }
    }

    fun getFeedsItemsWithDefaultFullTextNeedingDownload(): Flow<List<FeedItemIdWithLink>> =
        dao.getFeedsItemsWithDefaultFullTextNeedingDownload()

    suspend fun markAsFullTextDownloaded(feedItemId: Long) =
        dao.markAsFullTextDownloaded(feedItemId)

    fun getFeedItemsNeedingNotifying(): Flow<List<Long>> {
        return dao.getFeedItemsNeedingNotifying()
    }

    suspend fun loadFeedItem(guid: String, feedId: Long): FeedItem? =
        dao.loadFeedItem(guid = guid, feedId = feedId)

    suspend fun upsertFeedItems(
        itemsWithText: List<Pair<FeedItem, String>>,
        block: suspend (FeedItem, String) -> Unit,
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
        bookmarked = bookmarked,
        feedImageUrl = feedImageUrl,
        rawPubDate = pubDate,
        primarySortTime = primarySortTime,
    )
