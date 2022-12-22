package com.nononsenseapps.feeder.db.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nononsenseapps.feeder.db.COL_BODY
import com.nononsenseapps.feeder.db.COL_DEVICE_NAME
import com.nononsenseapps.feeder.db.COL_ENDPOINT
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_LAST_SEEN
import com.nononsenseapps.feeder.db.PUSH_KNOWN_DEVICES
import com.nononsenseapps.feeder.db.PUSH_MESSAGE_QUEUE
import com.nononsenseapps.feeder.db.PUSH_THIS_DEVICE
import org.threeten.bp.Instant

@Entity(
    tableName = PUSH_MESSAGE_QUEUE,
    indices = [
        Index(value = [COL_ENDPOINT])
    ],
    foreignKeys = [
        ForeignKey(
            entity = KnownDevice::class,
            parentColumns = [COL_ENDPOINT],
            childColumns = [COL_ENDPOINT],
            onDelete = CASCADE,
        )
    ],
)
class QueuedMessage(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COL_ID) val id: Long = ID_UNSET,
    @ColumnInfo(name = COL_ENDPOINT) val toEndpoint: String,
    @ColumnInfo(name = COL_BODY) val body: ByteArray,
)

@Entity(
    tableName = PUSH_THIS_DEVICE,
    indices = [
        Index(value = [COL_ENDPOINT], unique = true),
    ],
)
data class ThisDevice(
    // Only one ever exists so no need to autogenerate ids
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = COL_ID) val id: Long = 1L,
    @ColumnInfo(name = COL_ENDPOINT) val endpoint: String,
    @ColumnInfo(name = COL_DEVICE_NAME) val name: String,
)

@Entity(
    tableName = PUSH_KNOWN_DEVICES,
    indices = [
        Index(value = [COL_ENDPOINT], unique = true),
    ],
)
data class KnownDevice(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COL_ID) val id: Long = ID_UNSET,
    @ColumnInfo(name = COL_ENDPOINT) val endpoint: String,
    @ColumnInfo(name = COL_DEVICE_NAME) val name: String,
    @ColumnInfo(name = COL_LAST_SEEN) val lastSeen: Instant,
)
