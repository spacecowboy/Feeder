package com.nononsenseapps.feeder.db.room

import androidx.room.TypeConverter
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import org.joda.time.DateTime
import java.net.URL

class Converters {

    @TypeConverter
    fun dateTimeFromString(value: String?): DateTime? {
        var dt: DateTime? = null
        if (value != null) {
            try {
                dt = DateTime.parse(value)
            } catch (t: Throwable) {
            }
        }
        return dt
    }

    @TypeConverter
    fun stringFromDateTime(value: DateTime?): String? =
            value?.toString()

    @TypeConverter
    fun stringFromURL(value: URL?): String? =
            value?.toString()

    @TypeConverter
    fun urlFromString(value: String?): URL? =
        value?.let { sloppyLinkToStrictURLNoThrows(it) }
}