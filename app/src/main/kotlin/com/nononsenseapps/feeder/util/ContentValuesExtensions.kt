package com.nononsenseapps.feeder.util

import android.content.ContentValues

fun ContentValues.setBoolean(pair: Pair<String, Boolean>) =
        put(pair.first, pair.second)

fun ContentValues.setLong(pair: Pair<String, Long>) =
        put(pair.first, pair.second)

fun ContentValues.setInt(pair: Pair<String, Int>) =
        put(pair.first, pair.second)

fun ContentValues.setString(pair: Pair<String, String>) =
        put(pair.first, pair.second)

fun ContentValues.setNull(column: String) =
        putNull(column)

inline fun contentValues(init: ContentValues.() -> Unit): ContentValues {
    val values = ContentValues()
    values.init()
    return values
}
