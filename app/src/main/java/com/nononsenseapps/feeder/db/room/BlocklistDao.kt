package com.nononsenseapps.feeder.db.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlocklistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: BlocklistEntry): Long

    @Query(
        """
            SELECT glob_pattern
            FROM blocklist
            ORDER BY glob_pattern
        """
    )
    fun getGlobPatterns(): Flow<List<String>>

    @Query(
        """
            DELETE FROM blocklist
            WHERE glob_pattern NOT IN (:globPatterns)
        """
    )
    suspend fun deleteMissingPatterns(globPatterns: List<String>): Int
}
