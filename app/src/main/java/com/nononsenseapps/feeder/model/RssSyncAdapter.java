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

package com.nononsenseapps.feeder.model;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.nononsenseapps.feeder.db.Cleanup;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.model.apis.BackendAPIClient;
import com.nononsenseapps.feeder.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;


public class RssSyncAdapter extends AbstractThreadedSyncAdapter {

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
    public RssSyncAdapter(final Context context, final boolean autoInitialize) {
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
    public RssSyncAdapter(final Context context, final boolean autoInitialize,
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

        final Intent bcast = new Intent(SYNC_BROADCAST)
                .putExtra(SYNC_BROADCAST_IS_ACTIVE, true);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(bcast);

        final String token = RssSyncHelper.getSuitableToken(getContext());
        if (token == null) {
            Log.e(TAG, "No token exists! Aborting sync...");
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast
                    (bcast.putExtra(SYNC_BROADCAST_IS_ACTIVE, false));
            return;
        }

        BackendAPIClient.BackendAPI api = BackendAPIClient.GetBackendAPI(PrefUtils.getServerUrl(getContext()), token);

        try {
            final ArrayList<ContentProviderOperation> operations =
                    new ArrayList<ContentProviderOperation>();

            // First perform uploads
            RssSyncHelper.syncPending(getContext(), token, operations);

            // Then downloads
            Log.d(TAG, "With timestamp: " + RssContentProvider
                    .GetLatestTimestamp(getContext()));
            BackendAPIClient.FeedsResponse feedsResponse =
                api.getFeeds(
                    RssContentProvider.GetLatestTimestamp(getContext()));

            // Start with feeds
            List<BackendAPIClient.Feed> feeds = feedsResponse.feeds;

            if (feeds == null) {
                Log.d(TAG, "Feeds was null");
            } else {
                Log.d(TAG, "Number of feeds to sync: " + feeds.size());
                /*
                If you encounter TransactionTooLargeException here, make
                sure you don't run the syncadapter in a different process.
                Sending several hundred operations across processes will
                cause the exception. Seems safe inside same process though.
                 */
                for (BackendAPIClient.Feed feed : feeds) {
                    Log.d(TAG, "Syncing: " + feed.title + "(" + (feed.items
                            == null ? 0 : feed.items.size()) + ")");
                    // Sync feed
                    RssSyncHelper.syncFeedBatch(getContext(), operations, feed);
                }
            }

            // End with deletes
            List<BackendAPIClient.Delete> deletes = feedsResponse.deletes;
            if (deletes == null) {
                Log.d(TAG, "Deletes was null");
            } else {
                Log.d(TAG, "Number of deletes to sync: " + deletes.size());
                for (BackendAPIClient.Delete delete : deletes) {
                    Log.d(TAG, "Deleting: " + delete.link);
                    // Delete feed
                    RssSyncHelper
                            .syncDeleteBatch(getContext(), operations, delete);
                }
            }

            if (!operations.isEmpty()) {
                getContext().getContentResolver()
                        .applyBatch(RssContentProvider.AUTHORITY, operations);
            }

            // Finally, prune excessive items
            Cleanup.prune(getContext());
        } catch (RetrofitError e) {
            Log.d(TAG, "Retrofit: " + e);
            final int status;
            if (e.getResponse() != null) {
                Log.e(TAG, "" +
                           e.getResponse().getStatus() +
                           "; " +
                           e.getResponse().getReason());
                status = e.getResponse().getStatus();
            } else {
                status = 999;
            }
            // An HTTP error was encountered.
            switch (status) {
                case 401: // Unauthorized, token could possibly just be stale
                  // auth-exceptions are hard errors, and if the token is stale,
                  // that's too harsh
                  //syncResult.stats.numAuthExceptions++;
                  // Instead, report ioerror, which is a soft error
                  syncResult.stats.numIoExceptions++;
                  break;
                case 404: // No such item, should never happen, programming error
                case 415: // Not proper body, programming error
                case 400: // Didn't specify url, programming error
                    syncResult.databaseError = true;
                    break;
                default: // Default is to consider it a networking/server issue
                    syncResult.stats.numIoExceptions++;
                    break;
            }
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteExc.: " + e);
            syncResult.databaseError = true;
        } catch (OperationApplicationException e) {
            Log.d(TAG, "OperationAppl.Exc.: " + e);
            syncResult.databaseError = true;
        } finally {
            // Notify that we've updated
            RssContentProvider.notifyAllUris(getContext());
            // And broadcast end of sync
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast
                    (bcast.putExtra(SYNC_BROADCAST_IS_ACTIVE, false));
            // Send notifications for configured feeds
            RssNotifications.notify(getContext());
        }
    }
}
