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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.nononsenseapps.feeder.db.FeedItemSQL;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Static library support version of the framework's {@link android.content.CursorLoader}.
 * Used to write apps that run on platforms prior to Android 3.0.  When running
 * on Android 3.0 or above, this implementation is still used; it does not try
 * to switch to the framework's implementation.  See the framework SDK
 * documentation for a class overview.
 */
public class DeltaCursorLoader extends AsyncTaskLoader<HashMap<FeedItemSQL, Integer>> {
    private static final String TAG = "DeltaCursorLoader";
    // This will call ForceLoad() when data changes
    final ForceLoadContentObserver mObserver;

    Uri mUri;
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;

    Cursor mCursor;

    HashMap<Long, FeedItemSQL> mItems = null;
    private HashMap<FeedItemSQL, Integer> mLastResult = null;

    /* Runs on a worker thread */
    @Override
    public HashMap<FeedItemSQL, Integer> loadInBackground() {
        Log.d(TAG, "loadInBackground");
        Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                mSelectionArgs, mSortOrder);
        if (cursor != null) {
            // Ensure the cursor window is filled
            cursor.getCount();
            cursor.registerContentObserver(mObserver);
        }

        Cursor oldCursor = mCursor;
        mCursor = cursor;

        Log.d(TAG, "Time to close...");
        if (oldCursor != null && oldCursor != cursor) {
            Log.d(TAG, "Closing oldCursor.");
            oldCursor.close();
        }

        // Now handle contents

        if (mCursor == null) {
            mItems = null;
            Log.d(TAG, "Cursor was null, returning null");
            return null;
        }

        HashMap<FeedItemSQL, Integer> result = new HashMap<>();
        HashMap<Long, FeedItemSQL> oldItems = mItems;
        mItems = new HashMap<>();

        // Find out which items are currently present
        while (mCursor.moveToNext()) {
            FeedItemSQL item = new FeedItemSQL(mCursor);

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

    /* Runs on the UI thread */
    @Override
    public void deliverResult(HashMap<FeedItemSQL, Integer> result) {
        if (isReset()) {
            // An async query came in while the loader is stopped
//            if (mCursor != null) {
//                mCursor.close();
//            }
            return;
        }

        mLastResult = result;
        if (isStarted()) {
            Log.d(TAG, "Delivering result");
            super.deliverResult(result);
        } else {
            Log.d(TAG, "Not started, so not delivering");
        }
    }

    /**
     * Creates an empty unspecified CursorLoader.  You must follow this with
     * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc
     * to specify the query to perform.
     */
    public DeltaCursorLoader(Context context) {
        super(context);
        mObserver = new ForceLoadContentObserver();
    }

    /**
     * Creates a fully-specified CursorLoader.  See
     * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)
     * ContentResolver.query()} for documentation on the meaning of the
     * parameters.  These will be passed as-is to that call.
     */
    public DeltaCursorLoader(Context context, Uri uri, String[] projection, String selection,
                             String[] selectionArgs, String sortOrder) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mLastResult != null) {
            deliverResult(mLastResult);
        }
        if (takeContentChanged() || mLastResult == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(HashMap<FeedItemSQL, Integer> map) {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
        mLastResult = null;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public String[] getProjection() {
        return mProjection;
    }

    public void setProjection(String[] projection) {
        mProjection = projection;
    }

    public String getSelection() {
        return mSelection;
    }

    public void setSelection(String selection) {
        mSelection = selection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        mSelectionArgs = selectionArgs;
    }

    public String getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(String sortOrder) {
        mSortOrder = sortOrder;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print("mUri=");
        writer.println(mUri);
        writer.print(prefix);
        writer.print("mProjection=");
        writer.println(Arrays.toString(mProjection));
        writer.print(prefix);
        writer.print("mSelection=");
        writer.println(mSelection);
        writer.print(prefix);
        writer.print("mSelectionArgs=");
        writer.println(Arrays.toString(mSelectionArgs));
        writer.print(prefix);
        writer.print("mSortOrder=");
        writer.println(mSortOrder);
        writer.print(prefix);
        writer.print("mCursor=");
        writer.println(mCursor);
        //writer.print(prefix); writer.print("mContentChanged="); writer.println(mContentChanged);
    }
}