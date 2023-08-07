package com.nononsenseapps.feeder.db.room

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.nononsenseapps.feeder.db.COL_CUSTOM_TITLE
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_TAG
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.model.FeedUnreadCount
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: Feed): Long

    @Update
    suspend fun updateFeed(feed: Feed): Int

    @Delete
    suspend fun deleteFeed(feed: Feed): Int

    @Query("DELETE FROM feeds WHERE id IS :feedId")
    suspend fun deleteFeedWithId(feedId: Long): Int

    @Query(
        """
        DELETE FROM feeds WHERE id IN (:ids)
        """,
    )
    suspend fun deleteFeeds(ids: List<Long>): Int

    @Query("SELECT * FROM feeds WHERE id IS :feedId")
    fun loadFeedFlow(feedId: Long): Flow<Feed?>

    @Query("SELECT DISTINCT tag FROM feeds ORDER BY tag COLLATE NOCASE")
    suspend fun loadTags(): List<String>

    @Query("SELECT DISTINCT tag FROM feeds ORDER BY tag COLLATE NOCASE")
    fun loadAllTags(): Flow<List<String>>

    @Query("SELECT * FROM feeds WHERE id IS :feedId")
    suspend fun loadFeed(feedId: Long): Feed?

    @Query(
        """
        SELECT 
            id,
            COALESCE(NULLIF(custom_title, ''), title) as title,
            notify
        FROM feeds
        ORDER BY COALESCE(NULLIF(custom_title, ''), title) COLLATE NOCASE
        """,
    )
    fun loadFlowOfFeedsForSettings(): Flow<List<FeedForSettings>>

    @Query(
        """
       SELECT * FROM feeds
       WHERE id is :feedId
       AND last_sync < :staleTime
    """,
    )
    suspend fun loadFeedIfStale(feedId: Long, staleTime: Long): Feed?

    @Query("SELECT * FROM feeds WHERE tag IS :tag")
    suspend fun loadFeeds(tag: String): List<Feed>

    @Query("SELECT * FROM feeds WHERE tag IS :tag AND last_sync < :staleTime")
    suspend fun loadFeedsIfStale(tag: String, staleTime: Long): List<Feed>

    @Query("SELECT * FROM feeds")
    suspend fun loadFeeds(): List<Feed>

    @Query(
        """
        SELECT $COL_ID as id, $COL_TITLE as title
        FROM feeds
        ORDER BY $COL_TITLE
    """,
    )
    fun loadFeedsForContentProvider(): Cursor

    @Query("SELECT * FROM feeds WHERE last_sync < :staleTime")
    suspend fun loadFeedsIfStale(staleTime: Long): List<Feed>

    @Query("SELECT * FROM feeds WHERE url IS :url")
    suspend fun loadFeedWithUrl(url: URL): Feed?

    @Query("SELECT id FROM feeds WHERE notify IS 1")
    suspend fun loadFeedIdsToNotify(): List<Long>

    @Query(
        """
        SELECT id, title, url, tag, custom_title, notify, currently_syncing, image_url, unread_count
        FROM feeds
        LEFT JOIN (SELECT COUNT(1) AS unread_count, feed_id
          FROM feed_items
          WHERE read_time is null
            AND NOT EXISTS (SELECT 1 FROM blocklist WHERE lower(feed_items.plain_title) GLOB blocklist.glob_pattern)
          GROUP BY feed_id
        )
        ON feeds.id = feed_id
    """,
    )
    fun loadFlowOfFeedsWithUnreadCounts(): Flow<List<FeedUnreadCount>>

    @Query("UPDATE feeds SET notify = :notify WHERE id IS :id")
    suspend fun setNotify(id: Long, notify: Boolean)

    @Query("UPDATE feeds SET notify = :notify WHERE tag IS :tag")
    suspend fun setNotify(tag: String, notify: Boolean)

    @Query("UPDATE feeds SET notify = :notify")
    suspend fun setAllNotify(notify: Boolean)

    @Query("SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE FROM feeds WHERE id IS :feedId")
    suspend fun getFeedTitle(feedId: Long): FeedTitle?

    @Query("SELECT $COL_ID FROM feeds where url is :url")
    suspend fun getFeedIdForUrl(url: URL): Long?

    @Query("SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE FROM feeds WHERE id IS :feedId")
    fun getFeedTitlesWithId(feedId: Long): Flow<List<FeedTitle>>

    @Query(
        """
        SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE
        FROM feeds
        WHERE $COL_TAG IS :feedTag
        ORDER BY $COL_TITLE COLLATE NOCASE
        """,
    )
    fun getFeedTitlesWithTag(feedTag: String): Flow<List<FeedTitle>>

    @Query("SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE FROM feeds ORDER BY $COL_TITLE COLLATE NOCASE")
    fun getAllFeedTitles(): Flow<List<FeedTitle>>

    @Query(
        """
            SELECT MAX(last_sync)
            FROM feeds
            WHERE currently_syncing
        """,
    )
    fun getCurrentlySyncingLatestTimestamp(): Flow<Instant?>

    @Query(
        """
            UPDATE feeds
            SET currently_syncing = :syncing
            WHERE id IS :feedId
        """,
    )
    suspend fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean)

    @Query(
        """
            UPDATE feeds
            SET currently_syncing = :syncing, last_sync = :lastSync
            WHERE id IS :feedId
        """,
    )
    suspend fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean, lastSync: Instant)

    @Query(
        """
            SELECT *
            FROM feeds
            ORDER BY url
        """,
    )
    suspend fun getFeedsOrderedByUrl(): List<Feed>

    @Query(
        """
            SELECT *
            FROM feeds
            ORDER BY url
        """,
    )
    fun getFlowOfFeedsOrderedByUrl(): Flow<List<Feed>>

    @Query(
        """
            DELETE FROM feeds
            WHERE url is :url
        """,
    )
    suspend fun deleteFeedWithUrl(url: URL): Int

    @Upsert
    suspend fun upsert(feed: Feed): Long
}
