package com.nononsenseapps.feeder.db.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nononsenseapps.feeder.db.COL_FEEDID
import com.nononsenseapps.feeder.db.COL_FEEDURL
import com.nononsenseapps.feeder.db.COL_GUID
import com.nononsenseapps.feeder.db.COL_ID

@Dao
interface ReadStatusSyncedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(value: ReadStatusSynced): Long

    @Update
    suspend fun update(value: ReadStatusSynced): Int

    @Delete
    suspend fun delete(value: ReadStatusSynced): Int

    @Query(
        """
            DELETE FROM read_status_synced
        """,
    )
    suspend fun deleteAll(): Int

    @Query(
        """
            SELECT
                fi.id AS $COL_ID,
                f.id AS $COL_FEEDID,
                fi.guid AS $COL_GUID,
                f.url AS $COL_FEEDURL
            FROM feed_items fi
            JOIN feeds f ON f.id = fi.feed_id
            WHERE
                fi.read_time is not null and
                fi.id NOT IN (
                    SELECT feed_item
                    FROM read_status_synced
                )
            ORDER BY fi.id DESC
        """,
    )
    suspend fun getFeedItemsWithoutSyncedReadMark(): List<FeedItemForReadMark>

    @Query(
        """
            DELETE FROM read_status_synced
            WHERE feed_item = :feedItemId
        """,
    )
    suspend fun deleteReadStatusSyncForItem(feedItemId: Long): Int
}
