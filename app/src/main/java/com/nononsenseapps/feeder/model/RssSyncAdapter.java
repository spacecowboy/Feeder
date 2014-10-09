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

import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.model.apis.BackendAPIClient;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;


public class RssSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "FeederRssSyncAdapter";

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

        final Intent bcast = new Intent(SYNC_BROADCAST)
                .putExtra(SYNC_BROADCAST_IS_ACTIVE, true);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(bcast);

        final String token = AuthHelper.getAuthToken(getContext());
        if (token == null) {
            Log.e(TAG, "No token exists! Aborting sync...");
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast
                    (bcast.putExtra(SYNC_BROADCAST_IS_ACTIVE, false));
            return;
        }

        BackendAPIClient.BackendAPI api = BackendAPIClient.GetBackendAPI(token);

        try {
            List<BackendAPIClient.Feed> feeds = api.getFeeds(
                    RssContentProvider.GetLatestTimestamp(getContext()));

            if (feeds == null) {
                Log.d(TAG, "Feeds was null");
            } else {
                Log.d(TAG, "Number of feeds to sync: " + feeds.size());
                ArrayList<ContentProviderOperation> operations =
                        new ArrayList<ContentProviderOperation>();
                /*
                If you encounter TransactionTooLargeException here, make
                sure you don't run the syncadapter in a different process.
                Sending several hundred operations across processes will
                cause the exception. Seems safe inside same process though.
                 */
                for (BackendAPIClient.Feed feed : feeds) {
                    Log.d(TAG, "Syncing: " + feed.title);
                    // Sync feed
                    RssSyncHelper.syncFeedBatch(getContext(), operations, feed);
                }
                getContext().getContentResolver()
                        .applyBatch(RssContentProvider.AUTHORITY, operations);
            }
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
                    syncResult.stats.numAuthExceptions++;
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
        }
    }
}