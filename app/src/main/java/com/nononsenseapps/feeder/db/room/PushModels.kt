package com.nononsenseapps.feeder.db.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nononsenseapps.feeder.db.COL_BODY
import com.nononsenseapps.feeder.db.COL_DEVICE_NAME
import com.nononsenseapps.feeder.db.COL_ENDPOINT
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_LAST_SEEN
import com.nononsenseapps.feeder.db.COL_PUBLIC_KEY
import com.nononsenseapps.feeder.db.COL_SECRET_KEY
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
    @ColumnInfo(name = COL_SECRET_KEY) val secretKey: ByteArray,
    @ColumnInfo(name = COL_PUBLIC_KEY) val publicKey: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThisDevice

        if (id != other.id) return false
        if (endpoint != other.endpoint) return false
        if (name != other.name) return false
        if (!secretKey.contentEquals(other.secretKey)) return false
        if (!publicKey.contentEquals(other.publicKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + endpoint.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + secretKey.contentHashCode()
        result = 31 * result + publicKey.contentHashCode()
        return result
    }
}

@Entity(
    tableName = PUSH_KNOWN_DEVICES,
    indices = [
        Index(value = [COL_ENDPOINT], unique = true),
    ],
)
data class KnownDevice(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COL_ID) val id: Long = ID_UNSET,
    @ColumnInfo(name = COL_ENDPOINT) override val endpoint: String,
    @ColumnInfo(name = COL_DEVICE_NAME) val name: String,
    @ColumnInfo(name = COL_LAST_SEEN) val lastSeen: Instant,
    @ColumnInfo(name = COL_PUBLIC_KEY) override val publicKey: ByteArray,
) : DeviceDestination {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KnownDevice

        if (id != other.id) return false
        if (endpoint != other.endpoint) return false
        if (name != other.name) return false
        if (lastSeen != other.lastSeen) return false
        if (!publicKey.contentEquals(other.publicKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + endpoint.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + lastSeen.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        return result
    }
}

data class KnownDeviceDestination @Ignore constructor(
    @ColumnInfo(name = COL_ENDPOINT) override val endpoint: String,
    @ColumnInfo(name = COL_PUBLIC_KEY) override val publicKey: ByteArray,
) : DeviceDestination {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KnownDeviceDestination

        if (endpoint != other.endpoint) return false
        if (!publicKey.contentEquals(other.publicKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = endpoint.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        return result
    }
}

interface DeviceDestination {
    val endpoint: String
    val publicKey: ByteArray
}
