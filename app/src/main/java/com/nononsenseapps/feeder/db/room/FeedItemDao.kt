package com.nononsenseapps.feeder.db.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nononsenseapps.feeder.model.PreviewItem
import com.nononsenseapps.feeder.model.previewColumns

@Dao
interface FeedItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFeedItem(item: FeedItem): Long

    @Update
    fun updateFeedItem(item: FeedItem)

    @Delete
    fun deleteFeedItem(item: FeedItem)

    @Query("""
        DELETE FROM feed_items WHERE id IN (
          SELECT id FROM feed_items
          WHERE feed_id IS :feedId
          ORDER BY pub_date DESC
          LIMIT -1 OFFSET :keepCount
        )""")
    fun cleanItemsInFeed(feedId: Long, keepCount: Int)

    @Query("SELECT * FROM feed_items WHERE guid IS :guid AND feed_id IS :feedId")
    fun loadFeedItem(guid: String, feedId: Long?): FeedItem?

    @Query("""
        SELECT *
        FROM feed_items
        WHERE feed_items.feed_id = :feedId
        ORDER BY pub_date DESC
        """)
    fun loadFeedItemsInFeed(feedId: Long): List<FeedItem>

    @Query("""
        SELECT $feedItemColumnsWithFeed
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_items.id IS :id
        """)
    fun loadLiveFeedItem(id: Long): LiveData<FeedItemWithFeed>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_id IS :feedId
        ORDER BY pub_date DESC
        """)
    fun loadLivePreviews(feedId: Long): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE tag IS :tag
        ORDER BY pub_date DESC
        """)
    fun loadLivePreviews(tag: String): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        ORDER BY pub_date DESC
        """)
    fun loadLivePreviews(): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_id IS :feedId AND unread IS :unread
        ORDER BY pub_date DESC
        """)
    fun loadLiveUnreadPreviews(feedId: Long?, unread: Boolean = true): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE tag IS :tag AND unread IS :unread
        ORDER BY pub_date DESC
        """)
    fun loadLiveUnreadPreviews(tag: String, unread: Boolean = true): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $previewColumns
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE unread IS :unread
        ORDER BY pub_date DESC
        """)
    fun loadLiveUnreadPreviews(unread: Boolean = true): DataSource.Factory<Int, PreviewItem>

    @Query("""
        SELECT $feedItemColumnsWithFeed
        FROM feed_items
        LEFT JOIN feeds ON feed_items.feed_id = feeds.id
        WHERE feed_id IN (:feedIds) AND notified IS 0 AND unread IS 1
        """)
    fun loadItemsToNotify(feedIds: List<Long>): List<FeedItemWithFeed>

    @Query("UPDATE feed_items SET unread = 0")
    fun markAllAsRead()

    @Query("UPDATE feed_items SET unread = 0 WHERE feed_id IS :feedId")
    fun markAllAsRead(feedId: Long?)

    @Query("""
        UPDATE feed_items
        SET unread = 0
        WHERE id IN (
          SELECT feed_items.id
          FROM feed_items
          LEFT JOIN feeds ON feed_items.feed_id = feeds.id
          WHERE tag IS :tag
        )""")
    fun markAllAsRead(tag: String)

    @Query("UPDATE feed_items SET unread = :unread WHERE id IS :id")
    fun markAsRead(id: Long, unread: Boolean = false)

    @Query("UPDATE feed_items SET notified = :notified WHERE id IN (:ids)")
    fun markAsNotified(ids: List<Long>, notified: Boolean = true)

    @Query("UPDATE feed_items SET notified = :notified WHERE id IS :id")
    fun markAsNotified(id: Long, notified: Boolean = true)

    @Query("""
        UPDATE feed_items
        SET notified = :notified
        WHERE id IN (
          SELECT feed_items.id
          FROM feed_items
          LEFT JOIN feeds ON feed_items.feed_id = feeds.id
          WHERE tag IS :tag
        )""")
    fun markTagAsNotified(tag: String, notified: Boolean = true)

    @Query("UPDATE feed_items SET notified = :notified")
    fun markAllAsNotified(notified: Boolean = true)

    @Query("UPDATE feed_items SET unread = 0, notified = 1 WHERE id IS :id")
    fun markAsReadAndNotified(id: Long)
}

fun FeedItemDao.upsertFeedItem(item: FeedItem): Long = when (item.id > ID_UNSET) {
    true -> {
        updateFeedItem(item)
        item.id
    }
    false -> insertFeedItem(item)
}
