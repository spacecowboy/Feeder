package com.nononsenseapps.feeder.db.room

import androidx.room.TypeConverter
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

class Converters {
    @TypeConverter
    fun dateTimeFromString(value: String?): ZonedDateTime? {
        var dt: ZonedDateTime? = null
        if (value != null) {
            try {
                dt = ZonedDateTime.parse(value)
            } catch (_: Throwable) {
            }
        }
        return dt
    }

    @TypeConverter
    fun stringFromDateTime(value: ZonedDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringFromURL(value: URL?): String? = value?.toString()

    @TypeConverter
    fun urlFromString(value: String?): URL? = value?.let { sloppyLinkToStrictURLNoThrows(it) }

    @TypeConverter
    fun instantFromLong(value: Long?): Instant? =
        try {
            value?.let { Instant.ofEpochMilli(it) }
        } catch (t: Throwable) {
            null
        }

    @TypeConverter
    fun longFromInstant(value: Instant?): Long? = value?.toEpochMilli()
}
