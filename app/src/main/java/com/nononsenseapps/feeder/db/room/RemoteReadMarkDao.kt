package com.nononsenseapps.feeder.db.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nononsenseapps.feeder.db.COL_ID
import java.net.URL
import java.time.Instant

@Dao
interface RemoteReadMarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(readMark: RemoteReadMark): Long

    @Delete
    suspend fun delete(readMark: RemoteReadMark): Int

    @Query(
        """
            DELETE FROM remote_read_mark
            WHERE timestamp < :oldestTimestamp
        """,
    )
    suspend fun deleteStaleRemoteReadMarks(oldestTimestamp: Instant): Int

    /**
     * Deletes a set of remote read marks by their primary key ids.
     * Used after the marks have been successfully applied locally.
     */
    @Query(
        """
            DELETE FROM remote_read_mark
            WHERE id in (:ids)
        """,
    )
    suspend fun deleteByIds(ids: List<Long>): Int

    /**
     * Deletes remote read marks for items that have already been marked as read locally.
     * This prevents stale marks from re-applying a read status after the user has
     * explicitly marked an item as unread.
     */
    @Query(
        """
            DELETE FROM remote_read_mark
            WHERE id IN (
                SELECT rrm.id FROM remote_read_mark rrm
                INNER JOIN feed_items fi ON rrm.guid = fi.guid
                INNER JOIN feeds f ON f.id = fi.feed_id
                WHERE f.url IS rrm.feed_url AND fi.read_time IS NOT NULL
            )
        """,
    )
    suspend fun deleteRemoteReadMarksForReadItems(): Int

    @Query(
        """
            SELECT remote_read_mark.id as id, fi.id as feed_item_id
            FROM remote_read_mark
            INNER JOIN feed_items fi ON remote_read_mark.guid = fi.guid
            INNER JOIN feeds f on f.id = fi.feed_id
            WHERE f.url IS remote_read_mark.feed_url AND fi.read_time is null
        """,
    )
    suspend fun getRemoteReadMarksReadyToBeApplied(): List<RemoteReadMarkReadyToBeApplied>

    @Query(
        """
            SELECT remote_read_mark.guid
            FROM remote_read_mark
            WHERE remote_read_mark.feed_url = :feedUrl
        """,
    )
    suspend fun getGuidsWhichAreSyncedAsReadInFeed(feedUrl: URL): List<String>
}

data class RemoteReadMarkReadyToBeApplied
    @Ignore
    constructor(
        @ColumnInfo(name = COL_ID) var id: Long = ID_UNSET,
        @ColumnInfo(name = "feed_item_id") var feedItemId: Long = ID_UNSET,
    ) {
        constructor() : this(id = ID_UNSET)
    }
