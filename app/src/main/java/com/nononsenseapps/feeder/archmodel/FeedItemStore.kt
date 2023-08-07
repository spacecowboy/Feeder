package com.nononsenseapps.feeder.archmodel

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.nononsenseapps.feeder.db.FAR_FUTURE
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
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
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
        minReadTime: Instant?,
    ): Flow<Int> =
        when {
            feedId > ID_UNSET -> dao.getFeedItemCount(
                feedId = feedId,
                minReadTime = minReadTime ?: FAR_FUTURE,
                bookmarked = false,
            )

            tag.isNotEmpty() -> dao.getFeedItemCount(
                tag = tag,
                minReadTime = minReadTime ?: FAR_FUTURE,
                bookmarked = false,
            )

            feedId == ID_SAVED_ARTICLES -> dao.getFeedItemCount(
                minReadTime = minReadTime ?: FAR_FUTURE,
                bookmarked = true,
            )

            else -> dao.getFeedItemCount(minReadTime = minReadTime ?: FAR_FUTURE, bookmarked = false)
        }

    fun getPagedFeedItems(
        feedId: Long,
        tag: String,
        minReadTime: Instant,
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
                            minReadTime = minReadTime,
                            bookmarked = onlyBookmarks,
                        )

                        tag.isNotEmpty() -> dao.pagingUnreadPreviewsDesc(
                            tag = tag,
                            minReadTime = minReadTime,
                            bookmarked = onlyBookmarks,
                        )

                        else -> dao.pagingUnreadPreviewsDesc(
                            minReadTime = minReadTime,
                            bookmarked = onlyBookmarks,
                        )
                    }
                }

                else -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingUnreadPreviewsAsc(
                            feedId = feedId,
                            minReadTime = minReadTime,
                            bookmarked = onlyBookmarks,
                        )

                        tag.isNotEmpty() -> dao.pagingUnreadPreviewsAsc(
                            tag = tag,
                            minReadTime = minReadTime,
                            bookmarked = onlyBookmarks,
                        )

                        else -> dao.pagingUnreadPreviewsAsc(
                            minReadTime = minReadTime,
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

    suspend fun markAsReadAndNotified(itemId: Long, readTime: Instant = Instant.now()) {
        dao.markAsReadAndNotified(
            id = itemId,
            readTime = readTime.coerceAtLeast(Instant.EPOCH),
        )
    }

    suspend fun markAsReadAndNotifiedAndOverwriteReadTime(itemId: Long, readTime: Instant) {
        dao.markAsReadAndNotifiedAndOverwriteReadTime(
            id = itemId,
            readTime = readTime.coerceAtLeast(Instant.EPOCH),
        )
    }

    suspend fun markAsUnread(itemId: Long) {
        dao.markAsUnread(itemId)
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
        queryReadTime: Instant,
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
                        queryReadTime = queryReadTime,
                        onlyBookmarked = onlyBookmarks,
                    )

                    tag.isNotEmpty() -> dao.markAsReadDesc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        tag = tag,
                        queryReadTime = queryReadTime,
                        onlyBookmarked = onlyBookmarks,
                    )

                    else -> dao.markAsReadDesc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        queryReadTime = queryReadTime,
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
                        queryReadTime = queryReadTime,
                        onlyBookmarked = onlyBookmarks,
                    )

                    tag.isNotEmpty() -> dao.markAsReadAsc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        tag = tag,
                        queryReadTime = queryReadTime,
                        onlyBookmarked = onlyBookmarks,
                    )

                    else -> dao.markAsReadAsc(
                        primarySortTime = cursor.primarySortTime,
                        pubDate = cursor.pubDate,
                        id = cursor.id,
                        queryReadTime = queryReadTime,
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

val mediumDateTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

val shortTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())

private fun PreviewItem.toFeedListItem() =
    FeedListItem(
        id = id,
        title = plainTitle,
        snippet = plainSnippet,
        feedTitle = feedDisplayTitle,
        unread = readTime == null,
        pubDate = pubDate?.toLocalDateTime()?.formatDynamically() ?: "",
        imageUrl = imageUrl,
        link = link,
        bookmarked = bookmarked,
        feedImageUrl = feedImageUrl,
        rawPubDate = pubDate,
        primarySortTime = primarySortTime,
    )

private fun LocalDateTime.formatDynamically(): String {
    val today = LocalDate.now().atStartOfDay()
    return when {
        this >= today -> format(shortTimeFormat)
        else -> format(mediumDateTimeFormat)
    }
}
