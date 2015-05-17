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

package com.nononsenseapps.feeder.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.nononsenseapps.feeder.db.FeedItemSQL;

import java.util.HashMap;

/**
 * Static library support version of the framework's {@link android.content.CursorLoader}.
 * Used to write apps that run on platforms prior to Android 3.0.  When running
 * on Android 3.0 or above, this implementation is still used; it does not try
 * to switch to the framework's implementation.  See the framework SDK
 * documentation for a class overview.
 */
public class FeedItemDeltaCursorLoader extends DeltaCursorLoader<FeedItemSQL> {
    HashMap<Long, FeedItemSQL> mItems = null;

    /**
     * Creates an empty unspecified CursorLoader.  You must follow this with
     * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc
     * to specify the query to perform.
     *
     * @param context
     */
    public FeedItemDeltaCursorLoader(Context context) {
        super(context);
    }

    /**
     * Creates a fully-specified CursorLoader.  See
     * {@link ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     *
     * @param context
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     */
    public FeedItemDeltaCursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    /* Runs on a worker thread */
    @Override
    public HashMap<FeedItemSQL, Integer> loadInBackground() {
        Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                mSelectionArgs, mSortOrder);
        if (cursor != null) {
            // Ensure the cursor window is filled
            cursor.getCount();
            cursor.registerContentObserver(mObserver);
        }

        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (oldCursor != null && oldCursor != cursor) {
            oldCursor.close();
        }

        // Now handle contents

        if (mCursor == null) {
            mItems = null;
            return null;
        }

        HashMap<FeedItemSQL, Integer> result = new HashMap<>();
        HashMap<Long, FeedItemSQL> oldItems = mItems;
        mItems = new HashMap<>();

        // Find out which items are currently present
        while (mCursor.moveToNext()) {
            FeedItemSQL item = new FeedItemSQL(mCursor);
            // Also parse JSON while in background
            item.getJson();

            mItems.put(item.id, item);

            if (oldItems != null && oldItems.containsKey(item.id)) {
                // 0, already in set
                result.put(item, 0);
                oldItems.remove(item.id);
            } else {
                // 1, new item
                result.put(item, 1);
            }
        }
        // Any items which are left in the old set are now deleted
        if (oldItems != null) {
            for (FeedItemSQL item : oldItems.values()) {
                // -1, removed item
                result.put(item, -1);
            }
        }

        return result;
    }
}