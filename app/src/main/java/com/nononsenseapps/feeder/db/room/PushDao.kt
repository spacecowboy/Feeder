package com.nononsenseapps.feeder.db.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: QueuedMessage): Long

    @Query(
        """
            DELETE FROM push_message_queue
            WHERE id IN (:ids)
        """
    )
    suspend fun deleteMessages(ids: List<Long>): Int

    @Query(
        """
            SELECT * FROM push_message_queue
            ORDER BY ID ASC
        """
    )
    suspend fun getMessages(): List<QueuedMessage>
}

@Dao
interface KnownDevicesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(knownDevice: KnownDevice): Long

    @Update
    suspend fun update(knownDevice: KnownDevice): Int

    @Query(
        """
            DELETE FROM push_known_devices
            WHERE endpoint IN (:endpoints)
        """
    )
    suspend fun deleteDevices(endpoints: List<String>): Int

    @Query(
        """
            DELETE FROM push_known_devices
        """
    )
    suspend fun deleteAllDevices(): Int

    @Query(
        """
            SELECT * FROM push_known_devices
            WHERE endpoint = :endpoint
        """
    )
    suspend fun getKnownDevice(endpoint: String): KnownDevice?

    @Query(
        """
            SELECT * FROM push_known_devices
        """
    )
    fun getKnownDevicesFlow(): Flow<List<KnownDevice>>

    @Query(
        """
            SELECT * FROM push_known_devices
        """
    )
    suspend fun getKnownDevices(): List<KnownDevice>

    @Query(
        """
            SELECT endpoint, public_key FROM push_known_devices
        """
    )
    suspend fun getKnownDeviceDestinations(): List<KnownDeviceDestination>
}

/**
 * Inserts or updates depending on if ID is valid. Returns ID.
 */
suspend fun KnownDevicesDao.upsert(knownDevice: KnownDevice): Long =
    when (knownDevice.id > ID_UNSET) {
        true -> {
            update(knownDevice)
            knownDevice.id
        }
        false -> {
            insert(knownDevice)
        }
    }

@Dao
interface ThisDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(thisDevice: ThisDevice): Long

    @Query(
        """
            DELETE FROM push_this_device
            WHERE ID = 1
        """
    )
    suspend fun delete(): Int

    @Query(
        """
            SELECT * FROM push_this_device
            WHERE ID = 1
        """
    )
    suspend fun getThisDevice(): ThisDevice?

    @Query(
        """
            SELECT endpoint FROM push_this_device
            WHERE ID = 1
        """
    )
    suspend fun getThisDeviceEndpoint(): String?

    @Query(
        """
            SELECT * FROM push_this_device
            WHERE ID = 1
        """
    )
    fun getThisDeviceFlow(): Flow<ThisDevice?>
}
