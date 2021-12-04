package com.nononsenseapps.feeder.db.room

import android.os.Build
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.nononsenseapps.feeder.db.COL_DEVICE_ID
import com.nononsenseapps.feeder.db.COL_DEVICE_NAME
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_LAST_FEEDS_REMOTE_HASH
import com.nononsenseapps.feeder.db.COL_LATEST_MESSAGE_TIMESTAMP
import com.nononsenseapps.feeder.db.COL_SECRET_KEY
import com.nononsenseapps.feeder.db.COL_SYNC_CHAIN_ID
import com.nononsenseapps.feeder.db.COL_URL
import com.nononsenseapps.feeder.db.SYNC_REMOTE_TABLE_NAME
import java.net.URL
import kotlin.random.Random
import org.threeten.bp.Instant

@Entity(
    tableName = SYNC_REMOTE_TABLE_NAME
)
data class SyncRemote @Ignore constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COL_ID) var id: Long = ID_UNSET,
    @ColumnInfo(name = COL_URL) var url: URL = URL(DEFAULT_SERVER_ADDRESS),
    @ColumnInfo(name = COL_SYNC_CHAIN_ID) var syncChainId: String = "",
    @ColumnInfo(
        name = COL_LATEST_MESSAGE_TIMESTAMP,
        typeAffinity = ColumnInfo.INTEGER
    ) var latestMessageTimestamp: Instant = Instant.EPOCH,
    @ColumnInfo(name = COL_DEVICE_ID) var deviceId: Long = 0L,
    @ColumnInfo(name = COL_DEVICE_NAME) var deviceName: String = generateDeviceName(),
    @ColumnInfo(name = COL_SECRET_KEY) var secretKey: String = "",
    @ColumnInfo(name = COL_LAST_FEEDS_REMOTE_HASH) var lastFeedsRemoteHash: Int = 0,
) {
    constructor() : this(id = ID_UNSET)
}

private const val DEFAULT_SERVER_HOST = "feederapp.nononsenseapps.com"
private const val DEFAULT_SERVER_PORT = 443
private const val DEFAULT_SERVER_ADDRESS = "https://$DEFAULT_SERVER_HOST:$DEFAULT_SERVER_PORT"

inline fun String?.ifBlankOrNull(defaultValue: () -> String?): String? =
    if (this?.isBlank() != false) defaultValue() else this

fun generateDeviceName(): String =
    Build.PRODUCT.ifBlankOrNull { Build.MODEL.ifBlankOrNull { Build.BRAND } } ?: "${Random.nextInt(100_000)}"
