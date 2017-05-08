package com.nononsenseapps.feeder.util

fun android.database.Cursor.getString(column: String): String? {
    val index = getColumnIndex(column)
    return when {
        index < 0 -> null
        else -> if (isNull(index)) null else getString(index)
    }
}

fun android.database.Cursor.getLong(column: String): Long? {
    val index = getColumnIndex(column)
    return when {
        index < 0 -> null
        else -> if (isNull(index)) null else getLong(index)
    }
}

fun android.database.Cursor.getInt(column: String): Int? {
    val index = getColumnIndex(column)
    return when {
        index < 0 -> null
        else -> if (isNull(index)) null else getInt(index)
    }
}
