/*
 * Copyright (c) 2016 Jonas Kalderstam.
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

package com.nononsenseapps.feeder.db;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles periodic clean up of the database, such as making sure the database doesn't
 * grow too large.
 */
public class Cleanup {

    private static final String TAG = "Feeder.Cleanup";

    /**
     * For every feed, delete all items except the 50 first, as sorted by descending pubdate.
     */
    public static void prune(final Context context) throws RemoteException, OperationApplicationException {
        // Delete all stale items in one batch (transaction)
        final ArrayList<ContentProviderOperation> operations =
                new ArrayList<ContentProviderOperation>();

        // Fetch all feeds first
        List<Long> feedIds = getAllFeedIds(context);

        // Leave 50 items for each feed
        for (long feedId : feedIds) {
            for (long itemId : getItemsToDelete(context, feedId)) {
                addDelete(itemId, operations);
            }
        }

        // Delete stale items
        if (!operations.isEmpty()) {
            Log.d(TAG, "Prune " + operations.size() + " feed items.");
            context.getContentResolver()
                    .applyBatch(RssContentProvider.AUTHORITY, operations);
        }
    }

    private static void addDelete(final long itemId, final ArrayList<ContentProviderOperation> operations) {
        operations.add(ContentProviderOperation
                .newDelete(Uri
                        .withAppendedPath(FeedItemSQL.URI_FEED_ITEMS, Long.toString(itemId)))
                .build());
    }

    /**
     * Get a list of all feed item ids in the specified list which should be pruned.
     *
     * @param listId of the list within items are found
     */
    private static
    @NonNull
    List<Long> getItemsToDelete(final Context context, final long listId) {
        final ArrayList<Long> result = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(FeedItemSQL.URI_FEED_ITEMS.buildUpon()
                            .appendQueryParameter(RssContentProvider.QUERY_PARAM_SKIP, "50").build(),
                    new String[]{FeedItemSQL.COL_ID},
                    FeedItemSQL.COL_FEED + " IS ? ",
                    new String[]{Long.toString(listId)},
                    FeedItemSQL.COL_PUBDATE + " DESC");

            while (cursor != null && cursor.moveToNext()) {
                result.add(cursor.getLong(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * Get a list of all feed ids.
     */
    private static
    @NonNull
    List<Long> getAllFeedIds(final Context context) {
        final ArrayList<Long> result = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(FeedSQL.URI_FEEDS, new String[]{FeedSQL.COL_ID},
                    null, null, null);

            while (cursor != null && cursor.moveToNext()) {
                result.add(cursor.getLong(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }
}
