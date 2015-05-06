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
import com.nononsenseapps.feeder.db.PendingNetworkSQL;
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

    public RssSyncHelper() {
        super("RssSyncService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void syncFeeds(Context context) {
        Intent intent = new Intent(context, RssSyncHelper.class);
        context.startService(intent);
    }

    public static void uploadFeedAsync(Context context, long id, String title,
                                       String link, String tag) {
        Intent intent = new Intent(context, RssSyncHelper.class);
        intent.setAction(ACTION_PUT_FEED);
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("link", link);
        intent.putExtra("tag", tag);
        context.startService(intent);
    }

    public static void deleteFeedAsync(Context context, String link) {
        Intent intent = new Intent(context, RssSyncHelper.class);
        intent.setAction(ACTION_DELETE_FEED);
        intent.putExtra("link", link);
        context.startService(intent);
    }

    /**
     * Synchronize pending updates
     *
     * @param context
     * @param operations deletes will be added to operations
     */
    public static void syncPending(final Context context,
                                   final String token,
                                   final ArrayList<ContentProviderOperation> operations) {
        if (token == null) {
            throw new NullPointerException("Token was null");
        }

        Cursor c = null;
        try {
            c = context.getContentResolver()
                    .query(PendingNetworkSQL.URI, PendingNetworkSQL.FIELDS,
                            null, null, null);

            while (c != null && c.moveToNext()) {
                PendingNetworkSQL pending = new PendingNetworkSQL(c);
                boolean success = false;

                if (pending.isDelete()) {
                    try {
                        // catch 404 special
                        deleteFeed(context, token, pending.url);
                        success = true;
                    } catch (RetrofitError e) {
                        if (e.getResponse() != null && e.getResponse()
                                .getStatus() == 404) {
                            // 404 is fine, already deleted
                            success = true;
                        } else {
                            // Not OK, throw it
                            throw e;
                        }

                    }
                } else if (pending.isPut()) {
                    putFeed(context, token, pending.title, pending.url,
                            pending.tag);
                    success = true;
                }

                if (success) {
                    // Remove from db
                    operations.add(ContentProviderOperation.newDelete(Uri.withAppendedPath
                            (PendingNetworkSQL.URI,
                                    Long.toString(pending.id))).build());
                }
            }

        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    /**
     * Remove the designated feed from local storage. Adds the delete to the
     * list of operations, to be committed with applyBatch.
     *
     * @param context
     * @param operations
     * @param delete
     */
    public static void syncDeleteBatch(final Context context,
                                       final ArrayList<ContentProviderOperation> operations,
                                       final BackendAPIClient.Delete delete) {
        operations.add(ContentProviderOperation.newDelete(FeedSQL.URI_FEEDS)
                .withSelection(FeedSQL.COL_URL + " IS ?",
                        Util.ToStringArray(delete.link)).build());
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
                                     final BackendAPIClient.Feed feed) {

        // This is the index of the feed, if needed for backreferences
        final int feedIndex = operations.size();

        // Create the insert/update feed operation first
        final ContentProviderOperation.Builder feedOp;
        // Might not exist yet
        final long feedId = getFeedSQLId(context, feed);
        if (feedId < 1) {
            feedOp = ContentProviderOperation.newInsert(FeedSQL.URI_FEEDS);
        } else {
            feedOp = ContentProviderOperation.newUpdate(
                    Uri.withAppendedPath(FeedSQL.URI_FEEDS,
                            Long.toString(feedId)));
        }
        // Populate with values
        feedOp.withValue(FeedSQL.COL_TITLE, feed.title)
                .withValue(FeedSQL.COL_TAG, feed.tag == null ? "" : feed.tag)
                .withValue(FeedSQL.COL_TIMESTAMP, feed.timestamp)
                .withValue(FeedSQL.COL_URL, feed.link);
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

            // First, reference feed's id with back ref if insert
            if (feedId < 1) {
                itemOp.withValueBackReference(FeedItemSQL.COL_FEED, feedIndex);
            } else {
                // Use the actual id, because update operation will not return id
                itemOp.withValue(FeedItemSQL.COL_FEED, feedId);
            }
            // Next all the other values. Make sure non null
            itemOp.withValue(FeedItemSQL.COL_LINK, item.link)
                    .withValue(FeedItemSQL.COL_FEEDTITLE, feed.title)
                    .withValue(FeedItemSQL.COL_TAG,
                            feed.tag == null ? "" : feed.tag)
                    .withValue(FeedItemSQL.COL_IMAGEURL, item.image)
                    .withValue(FeedItemSQL.COL_JSON, item.json)
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

            // TODO pre-cache all images
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

    protected static void putFeed(final Context context,
                                  final String token,
                                  final String title,
                                  final String link,
                                  final String tag) throws RetrofitError {
        if (token == null) {
            throw new NullPointerException("No token");
        }
        final BackendAPIClient.BackendAPI api = BackendAPIClient.GetBackendAPI(PrefUtils.getServerUrl(context), token);
        final BackendAPIClient.FeedMessage f = new BackendAPIClient.FeedMessage();
        f.title = title;
        f.link = link;
        if (tag != null && !tag.isEmpty()) {
            f.tag = tag;
        }

        final BackendAPIClient.Feed feed = api.putFeed(f);
        // If any items were returned
        if (feed.items != null && !feed.items.isEmpty()) {
            // Save the items
            final ArrayList<ContentProviderOperation> operations =
                    new ArrayList<ContentProviderOperation>();

            syncFeedBatch(context, operations, feed);
            if (!operations.isEmpty()) {
                try {
                    context.getContentResolver()
                            .applyBatch(RssContentProvider.AUTHORITY, operations);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteExc.: " + e);
                } catch (OperationApplicationException e) {
                    Log.e(TAG, "OperationAppl.Exc.: " + e);
                }
            }
        }
        // Notify URIs
        RssContentProvider.notifyAllUris(context);
        // And broadcast that feed has been added, so UI may update and select it if suitable
        LocalBroadcastManager.getInstance(context).sendBroadcast
                (new Intent(RssSyncAdapter.FEED_ADDED_BROADCAST)
                        .putExtra(FeedSQL.COL_ID, getFeedSQLId(context, feed)));
    }

    protected static void deleteFeed(final Context context,
                                     final String token,
                                     final String link) throws RetrofitError {
        if (token == null) {
            throw new NullPointerException("Token was null");
        }
        BackendAPIClient.BackendAPI api =
                BackendAPIClient.GetBackendAPI(PrefUtils.getServerUrl(context), token);
        BackendAPIClient.DeleteMessage d = new BackendAPIClient.DeleteMessage();
        d.link = link;
        api.deleteFeed(d);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String token = getSuitableToken(this);
        boolean storePending = token == null;

        if (ACTION_PUT_FEED.equals(intent.getAction())) {
            try {
                if (token != null) {
                    putFeed(this, token,
                            intent.getStringExtra("title"),
                            intent.getStringExtra("link"),
                            intent.getStringExtra("tag"));
                }
            } catch (RetrofitError e) {
                Log.e(TAG, "put error: " + e.getMessage());
                storePending = true;
            }

            if (storePending) {
                Log.d(TAG, "Storing put for later...");
                PendingNetworkSQL.storePut(this, intent.getStringExtra("title"),
                        intent.getStringExtra("link"),
                        intent.getStringExtra("tag"));
            }
        } else if (ACTION_DELETE_FEED.equals(intent.getAction())) {
            try {
                if (token != null) {
                    deleteFeed(this, token, intent.getStringExtra("link"));
                }
            } catch (RetrofitError e) {
                Log.e(TAG, "put error: " + e.getMessage());
                // Store for later unless 404, which means feed is already
                // deleted
                if (e.getResponse() == null ||
                        e.getResponse().getStatus() != 404) {
                    storePending = true;
                }
            }

            if (storePending) {
                Log.d(TAG, "Storing delete for later...");
                PendingNetworkSQL.storeDelete(this,
                        intent.getStringExtra("link"));
            }
        }
    }
}
