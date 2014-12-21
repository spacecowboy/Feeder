package com.nononsenseapps.feeder.model;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.db.Util;
import com.nononsenseapps.feeder.model.apis.BackendAPIClient;
import com.nononsenseapps.feeder.util.PasswordUtils;
import com.nononsenseapps.feeder.util.PrefUtils;

import java.util.ArrayList;

import retrofit.RetrofitError;

/**
 * Synchronizes RSS feeds.
 */
public class RssSyncHelper extends IntentService {

    private static final String TAG = "RssSyncHelper";
    private static final String ACTION_PUT_FEED = "PUTFEED";
    private static final String ACTION_DELETE_FEED = "DELETEFEED";
    private static final String ACTION_SYNC_FEED = "SYNCFEED";
    private static final String ACTION_SYNC_TAG = "SYNCTAG";

    public RssSyncHelper() {
        super("RssSyncService");
    }

    public static void syncFeedAsync(Context context, long id) {
        Intent intent = new Intent(context, RssSyncHelper.class);
        intent.setAction(ACTION_SYNC_FEED);
        intent.putExtra("id", id);
        context.startService(intent);
    }

    public static void syncTagAsync(Context context, String tag) {
        Intent intent = new Intent(context, RssSyncHelper.class);
        intent.setAction(ACTION_SYNC_TAG);
        intent.putExtra("tag", tag);
        context.startService(intent);
    }

    /**
     * Adds the information contained in the feed to the list of pending
     * operations, to be committed with applyBatch.
     *
     * @param context
     * @param operations
     * @param feed
     */
    public static void syncFeedBatch(final Context context,
                                     final ArrayList<ContentProviderOperation> operations,
                                     final BackendAPIClient.Feed feed,
                                     final long feedId) {

        // This is the index of the feed, if needed for backreferences
        final int feedIndex = operations.size();

        // Create the insert/update feed operation first
        final ContentProviderOperation.Builder feedOp;

        feedOp = ContentProviderOperation.newUpdate(
                Uri.withAppendedPath(FeedSQL.URI_FEEDS,
                        Long.toString(feedId)));

        // Populate with values
        feedOp.withValue(FeedSQL.COL_TIMESTAMP, feed.timestamp);
        // Add to list of operations
        operations.add(feedOp.build());

        // Now the feeds, might be null
        if (feed.items == null) {
            return;
        }

        for (BackendAPIClient.FeedItem item : feed.items) {
            // Always insert, have on conflict replace
            ContentProviderOperation.Builder itemOp = ContentProviderOperation
                    .newInsert(FeedItemSQL.URI_FEED_ITEMS);

            // Use the actual id, because update operation will not return id
            itemOp.withValue(FeedItemSQL.COL_FEED, feedId);

            // Next all the other values. Make sure non null
            itemOp.withValue(FeedItemSQL.COL_LINK, item.link)
                    .withValue(FeedItemSQL.COL_FEEDTITLE, feed.title)
                    .withValue(FeedItemSQL.COL_TAG,
                            feed.tag == null ? "" : feed.tag)
                    .withValue(FeedItemSQL.COL_IMAGEURL, item.image)
                    .withValue(FeedItemSQL.COL_ENCLOSURELINK, item.enclosure)
                    .withValue(FeedItemSQL.COL_AUTHOR, item.author)
                    .withValue(FeedItemSQL.COL_PUBDATE,
                            FeedItemSQL.getPubDateFromString(item.published))
                            // Make sure these are non-null
                    .withValue(FeedItemSQL.COL_TITLE,
                            item.title == null ? "" : item.title)
                    .withValue(FeedItemSQL.COL_DESCRIPTION,
                            item.description == null ? "" : item.description)
                    .withValue(FeedItemSQL.COL_PLAINTITLE,
                            item.title_stripped == null ?
                                    "" :
                                    item.title_stripped)
                    .withValue(FeedItemSQL.COL_PLAINSNIPPET,
                            item.snippet == null ? "" : item.snippet);

            // Add to list of operations
            operations.add(itemOp.build());
        }
    }

    private static long getFeedSQLId(final Context context,
                                     final BackendAPIClient.Feed feed) {
        long result = -1;
        Cursor c = context.getContentResolver()
                .query(FeedSQL.URI_FEEDS, Util.ToStringArray(FeedSQL.COL_ID),
                        FeedSQL.COL_URL + " IS ?",
                        Util.ToStringArray(feed.link), null);

        try {
            if (c.moveToNext()) {
                result = c.getLong(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    /**
     * Get a suitable token depending on the user specified google login or user/password
     *
     * @param context
     * @return
     */
    public static String getSuitableToken(final Context context) {
        String token;
        if (PrefUtils.getUseGoogleAccount(context)) {
            token = AuthHelper.getAuthToken(context);
        } else {
            try {
                token = PasswordUtils.getBase64BasicHeader(PrefUtils.getUsername(context, null),
                        PrefUtils.getPassword(context, null));
            } catch (NullPointerException e) {
                token = null;
            }
        }
        return token;
    }

    /**
     * @param context
     * @return a list of all feeds in the database
     */
    public static ArrayList<FeedSQL> getFeeds(Context context, String tag) {
        ArrayList<FeedSQL> feeds = new ArrayList<FeedSQL>();
        Cursor c = null;
        try {
            c = context.getContentResolver()
                    .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS,
                            tag == null ? null : FeedSQL.COL_TAG + " IS ?",
                            tag == null ? null : Util.ToStringArray(tag),
                            null);
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

    public static void syncFeed(final Context context, final long id) throws RemoteException, OperationApplicationException {
        ArrayList<FeedSQL> feeds = new ArrayList<FeedSQL>();
        Cursor c = null;
        try {
            c = context.getContentResolver()
                    .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS,
                            Util.WHEREIDIS,
                            Util.LongsToStringArray(id),
                            null);
            while (c != null && c.moveToNext()) {
                feeds.add(new FeedSQL(c));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        syncFeeds(context, feeds);
    }

    public static void syncFeed(final Context context, final FeedSQL feed) throws RemoteException, OperationApplicationException {
        ArrayList<FeedSQL> feeds = new ArrayList<FeedSQL>(1);
        feeds.add(feed);
        syncFeeds(context, feeds);
    }

    public static void syncFeeds(final Context context, final ArrayList<FeedSQL> dbfeeds) throws RemoteException, OperationApplicationException {
        final Intent bcast = new Intent(RssSyncAdapter.SYNC_BROADCAST)
                .putExtra(RssSyncAdapter.SYNC_BROADCAST_IS_ACTIVE, true);

        LocalBroadcastManager.getInstance(context).sendBroadcast(bcast);

        final String token = RssSyncHelper.getSuitableToken(context);
        if (token == null) {
            // TODO allow no account
            Log.e(TAG, "No token exists! Aborting sync...");
            LocalBroadcastManager.getInstance(context).sendBroadcast
                    (bcast.putExtra(RssSyncAdapter.SYNC_BROADCAST_IS_ACTIVE, false));
            return;
        }

        BackendAPIClient.BackendAPI api = BackendAPIClient.GetBackendAPI(PrefUtils.getServerUrl(context), token);

        try {
            final ArrayList<ContentProviderOperation> operations =
                    new ArrayList<ContentProviderOperation>();

            final BackendAPIClient.MiddleManMessage msg = new BackendAPIClient.MiddleManMessage();
            msg.links = new ArrayList<String>();

            msg.links.clear();
            for (int i = 0; i < dbfeeds.size(); i++) {
                // Query server
                msg.links.clear();
                msg.links.add(dbfeeds.get(i).url);
                try {
                    BackendAPIClient.MiddleManResponse feedsResponse =
                            api.getFreshFeeds(msg);

                    if (feedsResponse.feeds == null || feedsResponse.feeds.isEmpty()) {
                        continue;
                    } else {
                        BackendAPIClient.Feed feed = feedsResponse.feeds.get(0);
                /*
                If you encounter TransactionTooLargeException here, make
                sure you don't run the syncadapter in a different process.
                Sending several hundred operations across processes will
                cause the exception. Seems safe inside same process though.
                 */

                        Log.d(TAG, "Syncing: " + feed.title + "(" + (feed.items
                                == null ? 0 : feed.items.size()) + ")");
                        // Sync feed with database
                        RssSyncHelper.syncFeedBatch(context, operations, feed, dbfeeds.get(i).id);
                    }

                    // Could put this after as well, checking speed
                    if (!operations.isEmpty()) {
                        context.getContentResolver()
                                .applyBatch(RssContentProvider.AUTHORITY, operations);

                        operations.clear();
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
//                    switch (status) {
//                        case 401: // Unauthorized, token could possibly just be stale
//                            // auth-exceptions are hard errors, and if the token is stale,
//                            // that's too harsh
//                            //syncResult.stats.numAuthExceptions++;
//                            // Instead, report ioerror, which is a soft error
//                            syncResult.stats.numIoExceptions++;
//                            break;
//                        case 404: // No such item, should never happen, programming error
//                        case 415: // Not proper body, programming error
//                        case 400: // Didn't specify url, programming error
//                            syncResult.databaseError = true;
//                            break;
//                        default: // Default is to consider it a networking/server issue
//                            syncResult.stats.numIoExceptions++;
//                            break;
//                    }
                }
            }
        } finally {
            // Notify that we've updated
            RssContentProvider.notifyAllUris(context);
            // And broadcast end of sync
            LocalBroadcastManager.getInstance(context).sendBroadcast
                    (bcast.putExtra(RssSyncAdapter.SYNC_BROADCAST_IS_ACTIVE, false));
        }
    }

    public static void syncAll(final Context context) throws RemoteException, OperationApplicationException {
        syncFeeds(context, getFeeds(context, null));
    }

    public static void syncTag(final Context context, final String tag) throws RemoteException, OperationApplicationException {
        syncFeeds(context, getFeeds(context, tag));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (ACTION_SYNC_FEED.equals(intent.getAction())) {
                syncFeed(this, intent.getLongExtra("id", -1));
            } else if (ACTION_SYNC_TAG.equals(intent.getAction())) {
                syncTag(this, intent.getStringExtra("tag"));
            }
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, e.toString());
        }
    }
}
