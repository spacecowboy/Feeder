package com.nononsenseapps.feeder.util

fun android.database.Cursor.getString(column: String): String? {
    val index = getColumnIndex(column)
    return when {
        index < 0 -> null
        else -> if (isNull(index)) null else {
            try {
                getString(index)
            } catch (t: Throwable) {
                // Can happen if too much data stored in database
                // Cursor can only hold 2MB per window
                // See https://gitlab.com/spacecowboy/Feeder/issues/48
                null
            }
        }
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
