package com.nononsenseapps.feeder.db

import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.database.Cursor
import android.net.Uri
import android.os.RemoteException
import android.util.Log
import com.nononsenseapps.feeder.util.PrefUtils.maximumItemCountPerFeed
import java.util.*

/**
 * Handles periodic clean up of the database, such as making sure the database doesn't
 * grow too large.
 */

private val TAG = "Feeder.Cleanup"

/**
 * For every feed, delete all items except the 50 first, as sorted by descending pubdate.
 */
@Throws(RemoteException::class, OperationApplicationException::class)
fun prune(context: Context) {
    // Delete all stale items in one batch (transaction)
    val operations = ArrayList<ContentProviderOperation>()

    // Fetch all feeds first
    val feedIds = getAllFeedIds(context)

    // Leave 50 items for each feed
    for (feedId in feedIds) {
        for (itemId in getItemsToDelete(context, feedId)) {
            addDelete(itemId, operations)
        }
    }

    // Delete stale items
    if (!operations.isEmpty()) {
        Log.d(TAG, "Prune ${operations.size} feed items.")
        context.contentResolver
                .applyBatch(AUTHORITY, operations)
    }
}

private fun addDelete(itemId: Long, operations: ArrayList<ContentProviderOperation>) {
    operations.add(ContentProviderOperation
            .newDelete(Uri
                    .withAppendedPath(URI_FEEDITEMS, java.lang.Long.toString(itemId)))
            .build())
}

/**
 * Get a list of all feed item ids in the specified list which should be pruned.
 *
 * @param listId of the list within items are found
 */
private fun getItemsToDelete(context: Context, listId: Long): List<Long> {
    val result = ArrayList<Long>()
    var cursor: Cursor? = null

    try {
        cursor = context.contentResolver.query(URI_FEEDITEMS.buildUpon()
                .appendQueryParameter(QUERY_PARAM_SKIP, "${maximumItemCountPerFeed(context)}").build(),
                arrayOf(COL_ID),
                "$COL_FEED IS ? ",
                arrayOf(java.lang.Long.toString(listId)),
                "$COL_PUBDATE DESC")

        while (cursor != null && cursor.moveToNext()) {
            result.add(cursor.getLong(0))
        }
    } finally {
        if (cursor != null) {
            cursor.close()
        }
    }
    return result
}

/**
 * Get a list of all feed ids.
 */
private fun getAllFeedIds(context: Context): List<Long> {
    val result = ArrayList<Long>()
    var cursor: Cursor? = null

    try {
        cursor = context.contentResolver.query(URI_FEEDS, arrayOf(COL_ID), null, null, null)

        while (cursor != null && cursor.moveToNext()) {
            result.add(cursor.getLong(0))
        }
    } finally {
        if (cursor != null) {
            cursor.close()
        }
    }

    return result
}
