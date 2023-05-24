package com.nononsenseapps.feeder.db.room

import android.database.Cursor
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_PLAINSNIPPET
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.db.COL_URL
import com.nononsenseapps.feeder.db.FEEDS_TABLE_NAME
import com.nononsenseapps.feeder.model.PreviewItem
import com.nononsenseapps.feeder.model.previewColumns
import java.net.URL
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime

@Dao
interface FeedItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeedItem(item: FeedItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeedItems(items: List<FeedItem>): List<Long>

    @Update
    suspend fun updateFeedItem(item: FeedItem): Int

    @Update
    suspend fun updateFeedItems(items: List<FeedItem>): Int

    @Delete
    suspend fun deleteFeedItem(item: FeedItem): Int

    @Query(
        """
        DELETE FROM feed_items WHERE id IN (:ids)
        """,
    )
    suspend fun deleteFeedItems(ids: List<Long>): Int

    @Query(
        """
        SELECT id FROM feed_items
        WHERE feed_id IS :feedId AND bookmarked = 0
        ORDER BY primary_sort_time DESC, pub_date DESC
        LIMIT -1 OFFSET :keepCount
        """,
    )
    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, keepCount: Int): List<Long>

    @Query("SELECT * FROM feed_items WHERE guid IS :guid AND feed_id IS :feedId")
    suspend fun loadFeedItem(guid: String, feedId: Long?): FeedItem?

    @Query("SELECT * FROM feed_items WHERE id IS :id")
    suspend fun loadFeedItem(id: Long): FeedItem?

    @Query(
        """
        SELECT $feedItemColumnsWithFeed
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_items.id IS :id
        """,
    )
    suspend fun loadFeedItemWithFeed(id: Long): FeedItemWithFeed?

    @Query(
        """
        SELECT $FEEDS_TABLE_NAME.$COL_URL
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_items.id IS :id
        """,
    )
    suspend fun loadFeedUrlOfFeedItem(id: Long): URL?

    @Deprecated("Only used for migration and in tests")
    @Query(
        """
        SELECT *
        FROM feed_items
        WHERE feed_items.feed_id = :feedId
        ORDER BY primary_sort_time DESC, pub_date DESC
        """,
    )
    suspend fun loadFeedItemsInFeedDesc(feedId: Long): List<FeedItem>

    @Query(
        """
        SELECT $COL_ID as id, $COL_TITLE as title, $COL_PLAINSNIPPET as text
        FROM feed_items
        WHERE NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
        ORDER BY primary_sort_time DESC, pub_date DESC
        """,
    )
    fun loadFeedItemsForContentProvider(): Cursor

    @Query(
        """
        SELECT $COL_ID as id, $COL_TITLE as title, $COL_PLAINSNIPPET as text
        FROM feed_items
        WHERE feed_items.feed_id = :feedId
          AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
        ORDER BY primary_sort_time DESC, pub_date DESC
        """,
    )
    fun loadFeedItemsInFeedForContentProvider(feedId: Long): Cursor

    @Query(
        """
        SELECT $feedItemColumnsWithFeed
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_items.id IS :id
        """,
    )
    fun loadFeedItemFlow(id: Long): Flow<FeedItemWithFeed?>

    @Query(
        """
        SELECT $feedItemColumnsWithFeed
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_items.id IS :id
        """,
    )
    suspend fun getFeedItem(id: Long): FeedItemWithFeed?

    @Query(
        """
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_id IS :feedId
          AND unread in (1, :unread)
          AND bookmarked in (1, :bookmarked)
          AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
        ORDER BY $feedItemsListOrderByDesc
        """,
    )
    fun pagingUnreadPreviewsDesc(
        feedId: Long,
        unread: Boolean,
        bookmarked: Boolean,
    ): PagingSource<Int, PreviewItem>

    @Query(
        """
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE tag IS :tag
          AND unread in (1, :unread)
          AND bookmarked in (1, :bookmarked)
          AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
        ORDER BY $feedItemsListOrderByDesc
        """,
    )
    fun pagingUnreadPreviewsDesc(
        tag: String,
        unread: Boolean,
        bookmarked: Boolean,
    ): PagingSource<Int, PreviewItem>

    @Query(
        """
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE unread in (1, :unread)
          AND bookmarked in (1, :bookmarked)
          AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
        ORDER BY $feedItemsListOrderByDesc
        """,
    )
    fun pagingUnreadPreviewsDesc(
        unread: Boolean,
        bookmarked: Boolean,
    ): PagingSource<Int, PreviewItem>

    @Query(
        """
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_id IS :feedId
          AND unread in (1, :unread)
          AND bookmarked in (1, :bookmarked)
          AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
        ORDER BY $feedItemsListOrderByAsc
        """,
    )
    fun pagingUnreadPreviewsAsc(
        feedId: Long,
        unread: Boolean,
        bookmarked: Boolean,
    ): PagingSource<Int, PreviewItem>

    @Query(
        """
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE tag IS :tag 
          AND unread in (1, :unread)
          AND bookmarked in (1, :bookmarked)
          AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
        ORDER BY $feedItemsListOrderByAsc
        """,
    )
    fun pagingUnreadPreviewsAsc(
        tag: String,
        unread: Boolean,
        bookmarked: Boolean,
    ): PagingSource<Int, PreviewItem>

    @Query(
        """
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE unread in (1, :unread)
          AND bookmarked in (1, :bookmarked)
          AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
        ORDER BY $feedItemsListOrderByAsc
        """,
    )
    fun pagingUnreadPreviewsAsc(
        unread: Boolean,
        bookmarked: Boolean,
    ): PagingSource<Int, PreviewItem>

    @Query(
        """
        SELECT $feedItemColumnsWithFeed
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_id IN (:feedIds) AND notified IS 0 AND unread IS 1
          AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
        """,
    )
    suspend fun loadItemsToNotify(feedIds: List<Long>): List<FeedItemWithFeed>

    @Query("UPDATE feed_items SET unread = 0, notified = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE feed_items SET unread = 0, notified = 1 WHERE feed_id IS :feedId")
    suspend fun markAllAsRead(feedId: Long?)

    @Query(
        """
        UPDATE feed_items
        SET unread = 0, notified = 1
        WHERE id IN (
          SELECT feed_items.id
          FROM feed_items
          LEFT JOIN feeds ON feed_items.feed_id = feeds.id
          WHERE tag IS :tag
        )""",
    )
    suspend fun markAllAsRead(tag: String)

    @Query("UPDATE feed_items SET unread = :unread, notified = 1 WHERE id IS :id")
    suspend fun markAsRead(id: Long, unread: Boolean = false)

    @Query("UPDATE feed_items SET unread = :unread, notified = 1 WHERE id IN (:ids)")
    suspend fun markAsRead(ids: List<Long>, unread: Boolean = false)

    @Query("UPDATE feed_items SET bookmarked = :bookmarked WHERE id IS :id")
    suspend fun setBookmarked(id: Long, bookmarked: Boolean)

    @Query("UPDATE feed_items SET notified = :notified WHERE id IN (:ids)")
    suspend fun markAsNotified(ids: List<Long>, notified: Boolean = true)

    @Query("UPDATE feed_items SET notified = :notified WHERE id IS :id")
    suspend fun markAsNotified(id: Long, notified: Boolean = true)

    @Query(
        """
        UPDATE feed_items
        SET notified = :notified
        WHERE id IN (
          SELECT feed_items.id
          FROM feed_items
          LEFT JOIN feeds ON feed_items.feed_id = feeds.id
          WHERE tag IS :tag
        )""",
    )
    suspend fun markTagAsNotified(tag: String, notified: Boolean = true)

    @Query("UPDATE feed_items SET notified = :notified")
    suspend fun markAllAsNotified(notified: Boolean = true)

    @Query("UPDATE feed_items SET unread = 0, notified = 1 WHERE id IS :id")
    suspend fun markAsReadAndNotified(id: Long)

    @Query(
        """
            UPDATE feed_items SET unread = 0, notified = 1
            WHERE id IN (
                SELECT feed_items.id
                FROM feed_items
                WHERE unread in (1, :onlyUnread) and bookmarked in (1, :onlyBookmarked)
                AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
                -- this version of sqlite doesn't seem to support tuple comparisons
                and (
                    primary_sort_time < :primarySortTime
                    or primary_sort_time = :primarySortTime and pub_date < :pubDate
                    or primary_sort_time = :primarySortTime and pub_date = :pubDate and feed_items.id < :id
                )
            )
        """,
    )
    suspend fun markAsReadDesc(
        onlyUnread: Boolean,
        onlyBookmarked: Boolean,
        primarySortTime: Instant,
        pubDate: ZonedDateTime?,
        id: Long,
    )

    @Query(
        """
            UPDATE feed_items SET unread = 0, notified = 1
            WHERE id IN (
                SELECT feed_items.id
                FROM feed_items
                WHERE unread in (1, :onlyUnread) and bookmarked in (1, :onlyBookmarked)
                AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
                -- this version of sqlite doesn't seem to support tuple comparisons
                and (
                    primary_sort_time > :primarySortTime
                    or primary_sort_time = :primarySortTime and pub_date > :pubDate
                    or primary_sort_time = :primarySortTime and pub_date = :pubDate and feed_items.id > :id
                )
            )
        """,
    )
    suspend fun markAsReadAsc(
        onlyUnread: Boolean,
        onlyBookmarked: Boolean,
        primarySortTime: Instant,
        pubDate: ZonedDateTime?,
        id: Long,
    )

    @Query(
        """
            UPDATE feed_items SET unread = 0, notified = 1
            WHERE id IN (
                SELECT feed_items.id
                FROM feed_items
                WHERE feed_id = :feedId AND unread in (1, :onlyUnread) and bookmarked in (1, :onlyBookmarked)
                AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
                -- this version of sqlite doesn't seem to support tuple comparisons
                and (
                    primary_sort_time < :primarySortTime
                    or primary_sort_time = :primarySortTime and pub_date < :pubDate
                    or primary_sort_time = :primarySortTime and pub_date = :pubDate and feed_items.id < :id
                )
            )
        """,
    )
    suspend fun markAsReadDesc(
        feedId: Long,
        onlyUnread: Boolean,
        onlyBookmarked: Boolean,
        primarySortTime: Instant,
        pubDate: ZonedDateTime?,
        id: Long,
    )

    @Query(
        """
            UPDATE feed_items SET unread = 0, notified = 1
            WHERE id IN (
                SELECT feed_items.id
                FROM feed_items
                WHERE feed_id = :feedId AND unread in (1, :onlyUnread) and bookmarked in (1, :onlyBookmarked)
                AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
                -- this version of sqlite doesn't seem to support tuple comparisons
                and (
                    primary_sort_time > :primarySortTime
                    or primary_sort_time = :primarySortTime and pub_date > :pubDate
                    or primary_sort_time = :primarySortTime and pub_date = :pubDate and feed_items.id > :id
                )
            )
        """,
    )
    suspend fun markAsReadAsc(
        feedId: Long,
        onlyUnread: Boolean,
        onlyBookmarked: Boolean,
        primarySortTime: Instant,
        pubDate: ZonedDateTime?,
        id: Long,
    )

    @Query(
        """
            UPDATE feed_items SET unread = 0, notified = 1
            WHERE id IN (
                SELECT feed_items.id
                FROM feed_items
                LEFT JOIN feeds ON feed_items.feed_id = feeds.id
                WHERE tag IS :tag AND unread in (1, :onlyUnread) and bookmarked in (1, :onlyBookmarked)
                AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
                -- this version of sqlite doesn't seem to support tuple comparisons
                and (
                    primary_sort_time < :primarySortTime
                    or primary_sort_time = :primarySortTime and pub_date < :pubDate
                    or primary_sort_time = :primarySortTime and pub_date = :pubDate and feed_items.id < :id
                )
            )
        """,
    )
    suspend fun markAsReadDesc(
        tag: String,
        onlyUnread: Boolean,
        onlyBookmarked: Boolean,
        primarySortTime: Instant,
        pubDate: ZonedDateTime?,
        id: Long,
    )

    @Query(
        """
            UPDATE feed_items SET unread = 0, notified = 1
            WHERE id IN (
                SELECT feed_items.id
                FROM feed_items
                LEFT JOIN feeds ON feed_items.feed_id = feeds.id
                WHERE tag IS :tag and unread in (1, :onlyUnread) and bookmarked in (1, :onlyBookmarked)
                AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
                -- this version of sqlite doesn't seem to support tuple comparisons
                and (
                    primary_sort_time > :primarySortTime
                    or primary_sort_time = :primarySortTime and pub_date > :pubDate
                    or primary_sort_time = :primarySortTime and pub_date = :pubDate and feed_items.id > :id
                )
            )
        """,
    )
    suspend fun markAsReadAsc(
        tag: String,
        onlyUnread: Boolean,
        onlyBookmarked: Boolean,
        primarySortTime: Instant,
        pubDate: ZonedDateTime?,
        id: Long,
    )

    @Query(
        """
            SELECT feeds.open_articles_with
            FROM feed_items
            LEFT JOIN feeds ON feed_items.feed_id = feeds.id
            WHERE feed_items.id IS :itemId
        """,
    )
    suspend fun getOpenArticleWith(itemId: Long): String?

    @Query(
        """
            SELECT feeds.fulltext_by_default
            FROM feed_items
            LEFT JOIN feeds ON feed_items.feed_id = feeds.id
            WHERE feed_items.id IS :itemId
        """,
    )
    suspend fun getFullTextByDefault(itemId: Long): Boolean?

    @Query(
        """
            SELECT link
            FROM feed_items
            WHERE feed_items.id IS :itemid
        """,
    )
    suspend fun getLink(itemid: Long): String?

    @Query(
        """
            SELECT fi.id, fi.link
            FROM feed_items fi
            JOIN feeds f ON feed_id = f.id
            WHERE f.fulltext_by_default = 1
                AND fi.fulltext_downloaded <> 1
                AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(fi.plain_title) GLOB blocklist.glob_pattern)
        """,
    )
    fun getFeedsItemsWithDefaultFullTextNeedingDownload(): Flow<List<FeedItemIdWithLink>>

    @Query(
        """
            UPDATE feed_items
            SET fulltext_downloaded = 1
            WHERE id = :feedItemId
        """,
    )
    suspend fun markAsFullTextDownloaded(feedItemId: Long)

    @Query(
        """
            SELECT count(*)
            FROM feed_items fi
            WHERE
              unread in (1, :unread)
              and bookmarked in (1, :bookmarked)
              and NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(fi.plain_title) GLOB blocklist.glob_pattern)
        """,
    )
    fun getFeedItemCount(unread: Boolean, bookmarked: Boolean): Flow<Int>

    @Query(
        """
            SELECT count(*)
            FROM feed_items fi
            JOIN feeds f ON feed_id = f.id
            WHERE f.tag IS :tag
              and unread in (1, :unread)
              and bookmarked in (1, :bookmarked)
              AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(fi.plain_title) GLOB blocklist.glob_pattern)
        """,
    )
    fun getFeedItemCount(tag: String, unread: Boolean, bookmarked: Boolean): Flow<Int>

    @Query(
        """
            SELECT count(*)
            FROM feed_items fi
            WHERE fi.feed_id IS :feedId
              and unread in (1, :unread)
              and bookmarked in (1, :bookmarked)
              AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(fi.plain_title) GLOB blocklist.glob_pattern)
        """,
    )
    fun getFeedItemCount(feedId: Long, unread: Boolean, bookmarked: Boolean): Flow<Int>

    @Query(
        """
            SELECT fi.id
            FROM feed_items fi
            JOIN feeds f ON feed_id = f.id
            WHERE f.notify IS 1 AND fi.notified IS 0 AND fi.unread is 1
              AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(fi.plain_title) GLOB blocklist.glob_pattern)
        """,
    )
    fun getFeedItemsNeedingNotifying(): Flow<List<Long>>

    @Query(
        """
            SELECT fi.id
            FROM feed_items fi
            JOIN feeds f ON fi.feed_id = f.id
            where f.url IS :feedUrl AND fi.guid IS :articleGuid
        """,
    )
    suspend fun getItemWith(feedUrl: URL, articleGuid: String): Long?

    companion object {
        // These are backed by a database index
        private const val feedItemsListOrderByDesc =
            "primary_sort_time DESC, pub_date DESC, feed_items.id DESC"
        private const val feedItemsListOrderByAsc =
            "primary_sort_time ASC, pub_date ASC, feed_items.id ASC"
    }
}

suspend fun FeedItemDao.upsertFeedItems(
    itemsWithText: List<Pair<FeedItem, String>>,
    block: suspend (FeedItem, String) -> Unit,
) {
    val updatedItems = itemsWithText.filter { (item, _) ->
        item.id > ID_UNSET
    }
    updateFeedItems(updatedItems.map { (item, _) -> item })

    val insertedItems = itemsWithText.filter { (item, _) ->
        item.id <= ID_UNSET
    }
    val insertedIds = insertFeedItems(insertedItems.map { (item, _) -> item })

    updatedItems.forEach { (item, text) ->
        block(item, text)
    }

    insertedIds.zip(insertedItems).forEach { (itemId, itemToText) ->
        val (item, text) = itemToText

        item.id = itemId

        block(item, text)
    }
}
