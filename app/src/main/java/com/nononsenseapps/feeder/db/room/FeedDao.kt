package com.nononsenseapps.feeder.db.room

import android.database.Cursor
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.nononsenseapps.feeder.db.COL_CUSTOM_TITLE
import com.nononsenseapps.feeder.db.COL_FOLDER_ID
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.model.FeedUnreadCount
import kotlinx.coroutines.flow.Flow
import java.net.URL
import java.time.Instant

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

    @Query("SELECT * FROM feeds WHERE id IS :feedId and retry_after < :retryAfter")
    suspend fun syncLoadFeed(
        feedId: Long,
        retryAfter: Instant,
    ): Feed?

    @Query("SELECT * FROM feeds WHERE id IS :feedId")
    suspend fun getFeed(feedId: Long): Feed?

    @Query("SELECT * FROM feeds WHERE folder_id IS NULLIF(:folderId, $ID_UNSET) ORDER BY title")
    suspend fun getFeedsByTitle(folderId: Long): List<Feed>

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
        "select * from feeds",
    )
    fun getAllFeeds(): List<Feed>

    @Query(
        """
       SELECT * FROM feeds
       WHERE id is :feedId
       AND last_sync < :staleTime
       AND retry_after < :retryAfter
    """,
    )
    suspend fun syncLoadFeedIfStale(
        feedId: Long,
        staleTime: Long,
        retryAfter: Instant,
    ): Feed?

    @Query(
        """
        SELECT * FROM feeds
        WHERE folder_id IN (SELECT descendant_id FROM folder_closures WHERE ancestor_id = :folderId)
        AND retry_after < :retryAfter ORDER BY last_sync
        """,
    )
    suspend fun syncLoadFeeds(
        folderId: Long,
        retryAfter: Instant,
    ): List<Feed>

    @Query(
        """
        SELECT * FROM feeds
        WHERE folder_id IN (SELECT descendant_id FROM folder_closures WHERE ancestor_id = :folderId)
        AND last_sync < :staleTime AND retry_after < :retryAfter
        ORDER BY last_sync
        """,
    )
    suspend fun syncLoadFeedsIfStale(
        folderId: Long,
        staleTime: Long,
        retryAfter: Instant,
    ): List<Feed>

    @Query("SELECT * FROM feeds WHERE retry_after < :retryAfter ORDER BY last_sync")
    suspend fun syncLoadFeeds(retryAfter: Instant): List<Feed>

    @Query("SELECT * FROM feeds WHERE last_sync < :staleTime AND retry_after < :retryAfter ORDER BY last_sync")
    suspend fun syncLoadFeedsIfStale(
        staleTime: Long,
        retryAfter: Instant,
    ): List<Feed>

    @Query(
        """
        SELECT $COL_ID as id, $COL_TITLE as title
        FROM feeds
        ORDER BY $COL_TITLE
    """,
    )
    fun loadFeedsForContentProvider(): Cursor

    @Query("SELECT * FROM feeds WHERE url IS :url")
    suspend fun loadFeedWithUrl(url: URL): Feed?

    @Query("SELECT id FROM feeds WHERE notify IS 1")
    suspend fun loadFeedIdsToNotify(): List<Long>

    /**
     * This query uses depth-first search to sort the rows.
     * Depth-first search in recursive CTEs is not part of the SQL standard, but it is intended behavior in sqlite as shown in the
     * [official documentation](https://sqlite.org/lang_with.html#controlling_depth_first_versus_breadth_first_search_of_a_tree_using_order_by)
     */
    @Query(
        """
            WITH RECURSIVE
                feeds_with_unread_count AS (
                    SELECT feed_id, folder_id, display_title, image_url, SUM(unread) AS unread_count
                    FROM feeds_with_items_for_nav_drawer
                    GROUP BY feed_id
                ),
                folders_with_unread_count AS (
                    SELECT folders.id AS folder_id, folders.parent_id, name, SUM(unread_count) AS unread_count, expanded
                    FROM folders
                    LEFT JOIN folder_closures
                    ON folders.id = folder_closures.ancestor_id
                    LEFT JOIN feeds_with_unread_count
                    ON folder_closures.descendant_id = feeds_with_unread_count.folder_id
                    GROUP BY folders.id
                ),
                feeds_and_folders AS (
                    SELECT $ID_UNSET AS feed_id, folder_id, parent_id, name AS display_title, '' AS image_url, unread_count, expanded, 0 AS is_feed
                    FROM folders_with_unread_count
                    UNION ALL
                    SELECT feed_id, $ID_UNSET AS folder_id, folder_id AS parent_id, display_title, image_url, unread_count, 0 AS expanded, 1 AS is_feed
                    FROM feeds_with_unread_count
                ),
                nav_drawer_items AS (
                    SELECT feed_id, folder_id, parent_id, display_title, image_url, unread_count, expanded, is_feed, 0 AS depth
                    FROM feeds_and_folders
                    WHERE parent_id IS NULL
                    UNION ALL
                    SELECT f.feed_id, f.folder_id, f.parent_id, f.display_title, f.image_url, f.unread_count, f.expanded, f.is_feed, p.depth + 1
                    FROM feeds_and_folders f
                    JOIN nav_drawer_items p
                    ON p.expanded AND f.parent_id = p.folder_id
                    ORDER BY depth DESC, is_feed, display_title COLLATE NOCASE
                )
            -- all items
            SELECT $ID_ALL_FEEDS AS feed_id, $ID_UNSET AS folder_id, '' AS display_title, '' AS image_url, SUM(unread) AS unread_count, 0 AS expanded, 0 AS depth
            FROM feeds_with_items_for_nav_drawer
            -- starred
            UNION ALL
            SELECT $ID_SAVED_ARTICLES AS feed_id, $ID_UNSET AS folder_id, '' AS display_title, '' AS image_url, SUM(bookmarked) AS unread_count, 0 AS expanded, 0 AS depth
            FROM feeds_with_items_for_nav_drawer
            WHERE bookmarked
            -- folders and feeds
            UNION ALL
            SELECT feed_id, folder_id, display_title, image_url, unread_count, expanded, depth
            FROM nav_drawer_items
        """,
    )
    fun getPagedNavDrawerItems(): PagingSource<Int, FeedUnreadCount>

    @Query("UPDATE feeds SET notify = :notify WHERE id IS :id")
    suspend fun setNotify(
        id: Long,
        notify: Boolean,
    )

    @Query("UPDATE feeds SET notify = :notify WHERE folder_id IN (SELECT descendant_id FROM folder_closures WHERE ancestor_id = :folderId)")
    suspend fun setNotifyInFolder(
        folderId: Long,
        notify: Boolean,
    )

    @Query("UPDATE feeds SET notify = :notify")
    suspend fun setAllNotify(notify: Boolean)

    @Query("SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE FROM feeds WHERE id IS :feedId")
    suspend fun getFeedTitle(feedId: Long): FeedTitle?

    @Query("SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE FROM feeds WHERE id IS :feedId")
    fun getFeedTitleFlow(feedId: Long): Flow<FeedTitle?>

    @Query("SELECT $COL_ID FROM feeds where url is :url")
    suspend fun getFeedIdForUrl(url: URL): Long?

    @Query("SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE FROM feeds WHERE id IS :feedId")
    fun getFeedTitlesWithId(feedId: Long): Flow<List<FeedTitle>>

    @Query(
        """
        SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE
        FROM feeds
        WHERE $COL_FOLDER_ID IN (SELECT descendant_id FROM folder_closures WHERE ancestor_id = :folderId)
        ORDER BY $COL_TITLE COLLATE NOCASE
        """,
    )
    fun getFeedTitlesInFolder(folderId: Long): Flow<List<FeedTitle>>

    @Query(
        """
        SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE
        FROM feeds
        WHERE $COL_FOLDER_ID IN (SELECT descendant_id FROM folder_closures WHERE ancestor_id IN (:folderIds))
        ORDER BY $COL_TITLE COLLATE NOCASE
        """,
    )
    fun getFeedTitlesInFolders(folderIds: List<Long>): Flow<List<FeedTitle>>

    @Query("SELECT $COL_ID, $COL_TITLE, $COL_CUSTOM_TITLE FROM feeds ORDER BY $COL_TITLE COLLATE NOCASE")
    fun getAllFeedTitles(): Flow<List<FeedTitle>>

    // Not filtering on currently_syncing so the refresh indicator doesn't go up and down due to
    // single threaded sync.
    @Query(
        """
            SELECT MAX(last_sync)
            FROM feeds
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
    suspend fun setCurrentlySyncingOn(
        feedId: Long,
        syncing: Boolean,
    )

    @Query(
        """
            UPDATE feeds
            SET currently_syncing = :syncing, last_sync = :lastSync
            WHERE id IS :feedId
        """,
    )
    suspend fun setCurrentlySyncingOn(
        feedId: Long,
        syncing: Boolean,
        lastSync: Instant,
    )

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

    /**
     * Using HOST because we want to match server, and the possibility of username:password
     * means it's not as simple as SCHEME://HOST to build the prefix.
     */
    @Query(
        """
            UPDATE feeds
            SET retry_after = :retryAfter
            WHERE url LIKE '%' || :host || '%'
            AND retry_after < :retryAfter
        """,
    )
    suspend fun setRetryAfterForFeedsWithBaseUrl(
        host: String,
        retryAfter: Instant,
    )

    @Query("SELECT COALESCE(folder_id, $ID_UNSET) FROM feeds WHERE id = :feedId")
    fun getFolderIdFlow(feedId: Long): Flow<Long?>

    @Query(
        """
        SELECT id, COALESCE(NULLIF(custom_title, ''), title) AS display_title, image_url
        FROM feeds
        WHERE folder_id IS NULLIF(:folderId, $ID_UNSET)
        ORDER BY COALESCE(NULLIF(custom_title, ''), title) COLLATE NOCASE
        """,
    )
    fun getFeedDisplayItemsInFolder(folderId: Long): Flow<List<FeedDisplayItem>>

    @Query(
        """
        UPDATE feeds
        SET folder_id = NULLIF(:folderId, $ID_UNSET),
            when_modified = :whenModified
        WHERE id IN (:feedIds) AND folder_id IS NOT NULLIF(:folderId, $ID_UNSET)
        """,
    )
    suspend fun setFeedsFolderId(
        feedIds: List<Long>,
        folderId: Long,
        whenModified: Instant,
    )

    @Query(
        """
        UPDATE feeds
        SET when_modified = :whenModified
        WHERE folder_id IN (SELECT descendant_id FROM folder_closures WHERE ancestor_id = :folderId)
        """,
    )
    suspend fun updateFeedsModDateInFolder(
        folderId: Long,
        whenModified: Instant,
    )
}
