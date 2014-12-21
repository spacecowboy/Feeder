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



    @Override
    protected void onHandleIntent(Intent intent) {
        final String token = getSuitableToken(this);
        boolean storePending = token == null;


    }
}
