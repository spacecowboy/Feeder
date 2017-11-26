package com.nononsenseapps.feeder.util

import android.database.Cursor


fun Cursor.getString(column: String): String? {
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

fun Cursor.getLong(column: String): Long? {
    val index = getColumnIndex(column)
    return when {
        index < 0 -> null
        else -> if (isNull(index)) null else getLong(index)
    }
}

fun Cursor.getInt(column: String): Int? {
    val index = getColumnIndex(column)
    return when {
        index < 0 -> null
        else -> if (isNull(index)) null else getInt(index)
    }
}

/**
 * Executes the block of code for each cursor position. Once finished the cursor will be pointing beyond the last item.
 * Assumes that the cursor is already pointing before the first item.
 */
inline fun Cursor.forEach(block: (Cursor) -> Unit) {
    while (moveToNext()) {
        block(this)
    }
}

/**
 * Returns the cursor at the first position, or null if the cursor is empty.
 * Assumes that the cursor is already pointing before the first item.
 */
fun Cursor.firstOrNull(): Cursor? = when (moveToFirst()) {
    true -> this
    false -> null
}
