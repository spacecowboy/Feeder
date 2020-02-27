package com.nononsenseapps.feeder.db.room

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nononsenseapps.feeder.db.COL_URL
import com.nononsenseapps.feeder.db.FEEDS_TABLE_NAME
import com.nononsenseapps.feeder.model.PreviewItem
import com.nononsenseapps.feeder.model.previewColumns
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import java.net.URL

@FlowPreview
@Dao
interface FeedItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeedItem(item: FeedItem): Long

    @Update
    suspend fun updateFeedItem(item: FeedItem): Int

    @Delete
    suspend fun deleteFeedItem(item: FeedItem)

    @Query("""
        DELETE FROM feed_items WHERE id IN (:ids)
        """)
    suspend fun deleteFeedItems(ids: List<Long>)

    @Query("""
        SELECT id FROM feed_items
        WHERE feed_id IS :feedId
        ORDER BY first_synced_time DESC, pub_date DESC
        LIMIT -1 OFFSET :keepCount
        """)
    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, keepCount: Int): List<Long>

    @Query("SELECT * FROM feed_items WHERE guid IS :guid AND feed_id IS :feedId")
    suspend fun loadFeedItem(guid: String, feedId: Long?): FeedItem?

    @Query("SELECT * FROM feed_items WHERE id IS :id")
    suspend fun loadFeedItem(id: Long): FeedItem?

    @Query("""
        SELECT $FEEDS_TABLE_NAME.$COL_URL
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_items.id IS :id
        """)
    suspend fun loadFeedUrlOfFeedItem(id: Long): URL?

    @Query("""
        SELECT *
        FROM feed_items
        WHERE feed_items.feed_id = :feedId
        ORDER BY first_synced_time DESC, pub_date DESC
        """)
    suspend fun loadFeedItemsInFeed(feedId: Long): List<FeedItem>

    @Query("""
        SELECT $feedItemColumnsWithFeed
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_items.id IS :id
        """)
    fun loadLiveFeedItem(id: Long): Flow<FeedItemWithFeed>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_id IS :feedId
        ORDER BY first_synced_time DESC, pub_date DESC
        """)
    fun loadLivePreviews(feedId: Long): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE tag IS :tag
        ORDER BY first_synced_time DESC, pub_date DESC
        """)
    fun loadLivePreviews(tag: String): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        ORDER BY first_synced_time DESC, pub_date DESC
        """)
    fun loadLivePreviews(): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_id IS :feedId AND unread IS :unread
        ORDER BY first_synced_time DESC, pub_date DESC
        """)
    fun loadLiveUnreadPreviews(feedId: Long?, unread: Boolean = true): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE tag IS :tag AND unread IS :unread
        ORDER BY first_synced_time DESC, pub_date DESC
        """)
    fun loadLiveUnreadPreviews(tag: String, unread: Boolean = true): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE unread IS :unread
        ORDER BY first_synced_time DESC, pub_date DESC
        """)
    fun loadLiveUnreadPreviews(unread: Boolean = true): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $feedItemColumnsWithFeed
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_id IN (:feedIds) AND notified IS 0 AND unread IS 1
        """)
    suspend fun loadItemsToNotify(feedIds: List<Long>): List<FeedItemWithFeed>

    @Query("UPDATE feed_items SET unread = 0")
    suspend fun markAllAsRead()

    @Query("UPDATE feed_items SET unread = 0 WHERE feed_id IS :feedId")
    suspend fun markAllAsRead(feedId: Long?)

    @Query("""
        UPDATE feed_items
        SET unread = 0
        WHERE id IN (
          SELECT feed_items.id
          FROM feed_items
          LEFT JOIN feeds ON feed_items.feed_id = feeds.id
          WHERE tag IS :tag
        )""")
    suspend fun markAllAsRead(tag: String)

    @Query("UPDATE feed_items SET unread = :unread WHERE id IS :id")
    suspend fun markAsRead(id: Long, unread: Boolean = false)

    @Query("UPDATE feed_items SET unread = :unread WHERE id IN (:ids)")
    suspend fun markAsRead(ids: List<Long>, unread: Boolean = false)

    @Query("UPDATE feed_items SET notified = :notified WHERE id IN (:ids)")
    suspend fun markAsNotified(ids: List<Long>, notified: Boolean = true)

    @Query("UPDATE feed_items SET notified = :notified WHERE id IS :id")
    suspend fun markAsNotified(id: Long, notified: Boolean = true)

    @Query("""
        UPDATE feed_items
        SET notified = :notified
        WHERE id IN (
          SELECT feed_items.id
          FROM feed_items
          LEFT JOIN feeds ON feed_items.feed_id = feeds.id
          WHERE tag IS :tag
        )""")
    suspend fun markTagAsNotified(tag: String, notified: Boolean = true)

    @Query("UPDATE feed_items SET notified = :notified")
    suspend fun markAllAsNotified(notified: Boolean = true)

    @Query("UPDATE feed_items SET unread = 0, notified = 1 WHERE id IS :id")
    suspend fun markAsReadAndNotified(id: Long)
}

@FlowPreview
suspend fun FeedItemDao.upsertFeedItem(item: FeedItem): Long = when (item.id > ID_UNSET) {
    true -> {
        updateFeedItem(item)
        item.id
    }
    false -> insertFeedItem(item)
}
