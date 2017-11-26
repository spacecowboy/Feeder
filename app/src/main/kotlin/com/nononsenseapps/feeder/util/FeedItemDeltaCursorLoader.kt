/*
 * Copyright (c) 2015 Jonas Kalderstam.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.feeder.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import com.nononsenseapps.feeder.db.FeedItemSQL
import com.nononsenseapps.feeder.db.asFeedItem

/**
 * Static library support version of the framework's [android.content.CursorLoader].
 * Used to write apps that run on platforms prior to Android 3.0.  When running
 * on Android 3.0 or above, this implementation is still used; it does not try
 * to switch to the framework's implementation.  See the framework SDK
 * documentation for a class overview.
 */
class FeedItemDeltaCursorLoader(context: Context, uri: Uri,
                                projection: Array<String>,
                                selection: String?,
                                selectionArgs: Array<String>?,
                                sortOrder: String?) :
        DeltaCursorLoader<FeedItemSQL>(context,
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder) {

    private var items: MutableMap<Long, FeedItemSQL> = mutableMapOf()

    /* Runs on a worker thread */
    @SuppressLint("Recycle")
    override fun loadInBackground(): ResultMap<FeedItemSQL>? {
        val cursor = context.contentResolver.query(uri, projection, selection,
                selectionArgs, sortOrder)
        if (cursor != null) {
            // Ensure the cursor window is filled
            cursor.count
            cursor.registerContentObserver(observer)
        }

        val oldCursor = this.cursor
        this.cursor = cursor

        @Suppress("SuspiciousEqualsCombination")
        if (oldCursor != null && oldCursor !== cursor) {
            oldCursor.close()
        }

        // Now handle contents
        if (this.cursor == null) {
            items = mutableMapOf()
            return null
        }

        val result: MutableResultMap<FeedItemSQL> = mutableMapOf()
        val oldItems = items
        items = mutableMapOf()

        // Find out which items are currently present
        this.cursor?.forEach {
            val item = it.asFeedItem()

            items.put(item.id, item)

            if (oldItems.containsKey(item.id)) {
                // 0, already in set
                result.put(item, 0)
                oldItems.remove(item.id)
            } else {
                // 1, new item
                result.put(item, 1)
            }
        }

        // Any items which are left in the old set are now deleted
        oldItems.entries.forEach {
            // -1, removed item
            result.put(it.value, -1)
        }

        return result
    }
}
