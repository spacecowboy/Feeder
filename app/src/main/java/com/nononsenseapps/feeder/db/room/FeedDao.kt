package com.nononsenseapps.feeder.db.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nononsenseapps.feeder.model.FeedUnreadCount
import java.net.URL

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFeed(feed: Feed): Long

    @Update
    fun updateFeed(feed: Feed)

    @Delete
    fun deleteFeed(feed: Feed)

    @Query("SELECT * FROM feeds WHERE id IS :feedId")
    fun loadLiveFeed(feedId: Long): LiveData<Feed>

    @Query("SELECT DISTINCT tag FROM feeds ORDER BY tag COLLATE NOCASE")
    fun loadTags(): List<String>

    @Query("SELECT * FROM feeds WHERE id IS :feedId")
    fun loadFeed(feedId: Long): Feed?

    @Query("SELECT * FROM feeds WHERE tag IS :tag")
    fun loadFeeds(tag: String): List<Feed>

    @Query("SELECT notify FROM feeds WHERE tag IS :tag")
    fun loadLiveFeedsNotify(tag: String): LiveData<List<Boolean>>

    @Query("SELECT notify FROM feeds")
    fun loadLiveFeedsNotify(): LiveData<List<Boolean>>

    @Query("SELECT * FROM feeds")
    fun loadFeeds(): List<Feed>

    @Query("SELECT * FROM feeds WHERE url IS :url")
    fun loadFeedWithUrl(url: URL): Feed?

    @Query("SELECT id FROM feeds")
    fun loadFeedIds(): List<Long>

    @Query("SELECT id FROM feeds WHERE notify IS 1")
    fun loadFeedIdsToNotify(): List<Long>

    @Query("""
        SELECT id, title, url, tag, custom_title, notify, image_url, unread_count
        FROM feeds
        LEFT JOIN (SELECT COUNT(1) AS unread_count, feed_id
          FROM feed_items
          WHERE unread IS 1
          GROUP BY feed_id
        )
        ON feeds.id = feed_id
    """)
    fun loadLiveFeedsWithUnreadCounts(): LiveData<List<FeedUnreadCount>>

    @Query("UPDATE feeds SET notify = :notify WHERE id IS :id")
    fun setNotify(id: Long, notify: Boolean)

    @Query("UPDATE feeds SET notify = :notify WHERE tag IS :tag")
    fun setNotify(tag: String, notify: Boolean)

    @Query("UPDATE feeds SET notify = :notify")
    fun setAllNotify(notify: Boolean)
}

/**
 * Inserts or updates feed depending on if ID is valid. Returns ID.
 */
fun FeedDao.upsertFeed(feed: Feed): Long = when (feed.id > ID_UNSET) {
    true -> {
        updateFeed(feed)
        feed.id
    }
    false -> {
        insertFeed(feed)
    }
}
