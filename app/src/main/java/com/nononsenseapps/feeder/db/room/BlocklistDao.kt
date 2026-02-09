package com.nononsenseapps.feeder.db.room

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface BlocklistDao {
    @Query(
        """
            INSERT INTO blocklist (id, glob_pattern)
            VALUES (null, '*' || :pattern || '*')
        """,
    )
    suspend fun insertSafely(pattern: String)

    @Query(
        """
            DELETE FROM blocklist
            WHERE glob_pattern = ('*' || :pattern || '*')
        """,
    )
    suspend fun deletePattern(pattern: String)

    @Query(
        """
            SELECT glob_pattern
            FROM blocklist
            ORDER BY glob_pattern
        """,
    )
    fun getGlobPatterns(): Flow<List<String>>

    suspend fun setItemBlockStatus(
        blockTime: Instant,
        applyToSummaries: Boolean,
    ) {
        if (applyToSummaries) {
            setItemBlockStatusWithSummaries(blockTime)
        } else {
            setItemBlockStatusTitleOnly(blockTime)
        }
    }

    @Query(
        """
            update feed_items
            set block_time = case
                when exists(select 1 from blocklist where lower(feed_items.plain_title) glob blocklist.glob_pattern)
                then coalesce(block_time, :blockTime)
                else null
                end
        """,
    )
    suspend fun setItemBlockStatusTitleOnly(blockTime: Instant)

    @Query(
        """
            update feed_items
            set block_time = case
                when exists(select 1 from blocklist where lower(feed_items.plain_title) glob blocklist.glob_pattern or lower(feed_items.plain_snippet) glob blocklist.glob_pattern)
                then coalesce(block_time, :blockTime)
                else null
                end
        """,
    )
    suspend fun setItemBlockStatusWithSummaries(blockTime: Instant)

    suspend fun setItemBlockStatusWhereNull(
        blockTime: Instant,
        applyToSummaries: Boolean,
    ) {
        if (applyToSummaries) {
            setItemBlockStatusWhereNullWithSummaries(blockTime)
        } else {
            setItemBlockStatusWhereNullTitleOnly(blockTime)
        }
    }

    @Query(
        """
            update feed_items
            set block_time = case
                when exists(select 1 from blocklist where lower(feed_items.plain_title) glob blocklist.glob_pattern)
                then :blockTime
                else null
                end
            where block_time is null
        """,
    )
    suspend fun setItemBlockStatusWhereNullTitleOnly(blockTime: Instant)

    @Query(
        """
            update feed_items
            set block_time = case
                when exists(select 1 from blocklist where lower(feed_items.plain_title) glob blocklist.glob_pattern or lower(feed_items.plain_snippet) glob blocklist.glob_pattern)
                then :blockTime
                else null
                end
            where block_time is null
        """,
    )
    suspend fun setItemBlockStatusWhereNullWithSummaries(blockTime: Instant)

    suspend fun setItemBlockStatusForNewInFeed(
        feedId: Long,
        blockTime: Instant,
        applyToSummaries: Boolean,
    ) {
        if (applyToSummaries) {
            setItemBlockStatusForNewInFeedWithSummaries(feedId, blockTime)
        } else {
            setItemBlockStatusForNewInFeedTitleOnly(feedId, blockTime)
        }
    }

    @Query(
        """
            update feed_items
            set block_time = case
                when exists(select 1 from blocklist where lower(feed_items.plain_title) glob blocklist.glob_pattern)
                then :blockTime
                else null
                end
            where feed_id = :feedId and block_time is null
        """,
    )
    suspend fun setItemBlockStatusForNewInFeedTitleOnly(
        feedId: Long,
        blockTime: Instant,
    )

    @Query(
        """
            update feed_items
            set block_time = case
                when exists(select 1 from blocklist where lower(feed_items.plain_title) glob blocklist.glob_pattern or lower(feed_items.plain_snippet) glob blocklist.glob_pattern)
                then :blockTime
                else null
                end
            where feed_id = :feedId and block_time is null
        """,
    )
    suspend fun setItemBlockStatusForNewInFeedWithSummaries(
        feedId: Long,
        blockTime: Instant,
    )
}
