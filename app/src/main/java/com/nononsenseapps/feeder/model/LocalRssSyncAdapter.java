/*
 * Copyright (c) 2014 Jonas Kalderstam.
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

package com.nononsenseapps.feeder.model;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;

import java.util.ArrayList;


public class LocalRssSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "FeederRssSyncAdapter";

    public static final String FEED_ADDED_BROADCAST =
            "feeder.nononsenseapps.RSS_FEED_ADDED_BROADCAST";
    public static final String SYNC_BROADCAST =
            "feeder.nononsenseapps.RSS_SYNC_BROADCAST";
    public static final String SYNC_BROADCAST_IS_ACTIVE = "IS_ACTIVE";


    /**
     * Creates an {@link android.content.AbstractThreadedSyncAdapter}.
     *
     * @param context        the {@link android.content.Context} that this is
     *                       running within.
     * @param autoInitialize if true then sync requests that have
     *                       {@link android.content.ContentResolver#SYNC_EXTRAS_INITIALIZE}
     *                       set will be internally handled by
     *                       {@link android.content.AbstractThreadedSyncAdapter}
     *                       by calling
     *                       {@link android.content.ContentResolver#setIsSyncable(android.accounts.Account,
     *                       String, int)} with 1 if it
     */
    public LocalRssSyncAdapter(final Context context, final boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Creates an {@link android.content.AbstractThreadedSyncAdapter}.
     *
     * @param context            the {@link android.content.Context} that this
     *                           is running within.
     * @param autoInitialize     if true then sync requests that have
     *                           {@link android.content.ContentResolver#SYNC_EXTRAS_INITIALIZE}
     *                           set will be internally handled by
     *                           {@link android.content.AbstractThreadedSyncAdapter}
     *                           by calling
     *                           {@link android.content.ContentResolver#setIsSyncable(android.accounts.Account,
     *                           String, int)} with 1 if it is currently set to
     *                           <0.
     * @param allowParallelSyncs if true then allow syncs for different
     *                           accounts to run at the same time, each in
     *                           their own thread.  This must be consistent
     *                           with the setting.
     */
    public LocalRssSyncAdapter(final Context context, final boolean autoInitialize,
                               final boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    /**
     * Perform a sync for this account. SyncAdapter-specific parameters may
     * be specified in extras, which is guaranteed to not be null. Invocations
     * of this method are guaranteed to be serialized.
     *
     * @param account    the account that should be synced
     * @param extras     SyncAdapter-specific parameters
     * @param authority  the authority of this sync request
     * @param provider   a ContentProviderClient that points to the
     *                   ContentProvider for this
     *                   authority
     * @param syncResult SyncAdapter-specific parameters
     */
    @Override
    public void onPerformSync(final Account account, final Bundle extras,
            final String authority, final ContentProviderClient provider,
            final SyncResult syncResult) {

      // By default, if a sync is performed, we can wait at least an hour
      // to the next one. Unit is seconds
      syncResult.delayUntil = 60L * 60L;

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(SYNC_BROADCAST)
                .putExtra(SYNC_BROADCAST_IS_ACTIVE, true));


        final ArrayList<ContentProviderOperation> operations =
                new ArrayList<ContentProviderOperation>();

        // Then downloads
        Log.d(TAG, "With timestamp: " + RssContentProvider
                .GetLatestTimestamp(getContext()));

        // TODO Only get stale feeds?

        for (FeedSQL feed: getFeeds(getContext())) {
            //try {
                Log.d("LOCAL", "Syncing: " + feed.url);
                LocalRssSyncHelper.syncFeedBatch(getContext(), feed, operations);
            //} catch (Exception exc) {
           //     Log.e("LOCAL", "Feed: " + feed.url + "\n" + exc.toString());
           // }
        }

        if (!operations.isEmpty()) {
            try {
                getContext().getContentResolver()
                        .applyBatch(RssContentProvider.AUTHORITY, operations);
            } catch (Exception e) {
                Log.e("LOCAL", e.getLocalizedMessage());
            }
        }

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(SYNC_BROADCAST)
                .putExtra(SYNC_BROADCAST_IS_ACTIVE, false));
    }

    /**
     *
     * @param context
     * @return a list of all feeds in the database
     */
    public static ArrayList<FeedSQL> getFeeds(Context context) {
        ArrayList<FeedSQL> feeds = new ArrayList<FeedSQL>();
        Cursor c = null;
        try {
            c = context.getContentResolver()
                    .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS,
                            null, null, null);
            while (c != null && c.moveToNext()) {
                feeds.add(new FeedSQL(c));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return feeds;
    }
}
