package com.nononsenseapps.feeder.model;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nononsenseapps.feeder.db.DatabaseHandler;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.db.Util;
import com.shirwa.simplistic_rss.RssFeed;
import com.shirwa.simplistic_rss.RssItem;
import com.shirwa.simplistic_rss.RssReader;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RssSyncService extends IntentService {

    private static final String TAG = "RssSyncService";

    public RssSyncService() {
        super("RssSyncService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void syncFeeds(Context context) {
        Intent intent = new Intent(context, RssSyncService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Just one action now
        syncAll();
    }

    protected void syncAll() {
        // Iterate over all feeds
        Cursor cursor = getContentResolver()
                .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS, null, null,
                        null);

        try {
            while (cursor.moveToNext()) {
                syncFeed(new FeedSQL(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected void syncFeed(final FeedSQL feedSQL) {
        final DateTime latestPubDate = getLatestPubDate(feedSQL);

        Log.d("JONAS", "Syncing feed " + feedSQL.title + " with " +
                       latestPubDate);

//        SQLiteDatabase db =
//                DatabaseHandler.getInstance(this).getWritableDatabase();
        ContentResolver resolver = getContentResolver();
        // Want a transaction here
        //db.beginTransactionNonExclusive();
        try {
            RssFeed feed = new RssReader(feedSQL.url).getFeed();
            // Process each feed item
            for (RssItem item : feed.getRssItems()) {
                Log.d("JONAS", "Looping item " + item.getTitle());
                // Only care about new items
                if (latestPubDate != null && item.getPubDate() != null &&
                    item.getPubDate().isBefore(latestPubDate)) {
                    Log.d("JONAS", "Was too old, " +
                                   "moving on: " +
                                   item.getPubDate().toString());
                    continue;
                }
                // if no PubDate, fill in current time
                if (item.getPubDate() == null) {
                    item.setPubDate(DateTime.now());
                }
                // Save to database
                FeedItemSQL itemSQL = getUniqueSQLItem(item, feedSQL);
                // Set new values. Make sure some are not null
                itemSQL.title = item.getTitle();
                if (itemSQL.title == null) {
                    itemSQL.title = "";
                }
                itemSQL.description = item.getDescription();
                if (itemSQL.description == null) {
                    itemSQL.description = "";
                }
                itemSQL.plaintitle = item.getPlainTitle();
                if (itemSQL.plaintitle == null) {
                    itemSQL.plaintitle = "";
                }
                itemSQL.plainsnippet = item.getSnippet();
                if (itemSQL.plainsnippet == null) {
                    itemSQL.plainsnippet = "";
                }
                itemSQL.imageurl = item.getImageUrl();
                // TODO pre-cache ALL images
                if (itemSQL.imageurl != null && !itemSQL.imageurl.isEmpty()) {
                    Log.d("JONAS", "Pre-fetching " + itemSQL.imageurl);
                    Picasso.with(this).load(itemSQL.imageurl).fetch();
                }
                itemSQL.link = item.getLink();
                //itemSQL.author = item.getAuthor();
                itemSQL.setPubDate(item.getPubDate());
                // Always need these
                itemSQL.feed_id = feedSQL.id;
                itemSQL.tag = feedSQL.tag;
                // Save it
                Log.d("JONAS", "Saving the item: " + itemSQL.title);
                // TODO use apply batch instead
                Util.SaveOrUpdate(resolver, itemSQL);
            }
            // Mark as success
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        } finally {
            //db.endTransaction();
//            getContentResolver()
//                    .notifyChange(FeedItemSQL.URI_FEED_ITEMS, null,
//                            false);
//            getContentResolver()
//                    .notifyChange(FeedSQL.URI_FEEDSWITHCOUNTS, null,
//                            false);
//            getContentResolver()
//                    .notifyChange(FeedSQL.URI_TAGSWITHCOUNTS, null,
//                            false);
        }
    }

    /**
     * Return the latest pubdate of the selected feed
     *
     * @param feedSQL
     * @return latest DateTime or Null if no dates present
     */
    protected DateTime getLatestPubDate(final FeedSQL feedSQL) {
        DateTime latest = null;

        Cursor c = getContentResolver().query(FeedItemSQL.URI_FEED_ITEMS,
                Util.ToStringArray(FeedItemSQL.COL_PUBDATE),
                FeedItemSQL.COL_FEED + " " +
                "IS ?", Util.LongsToStringArray(feedSQL.id), null);

        try {
            while (c.moveToNext()) {
                if (!c.isNull(0)) {
                    DateTime date = DateTime.parse(c.getString(0));
                    if (latest == null) {
                        latest = date;
                    } else if (date.isAfter(latest)) {
                        latest = date;
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return latest;
    }

    /**
     * Looks and sees if an existing item exists, otherwise,
     * just creates a new item.
     *
     * @param item
     * @return a FeedItemSQL
     */
    private FeedItemSQL getUniqueSQLItem(final RssItem item,
            final FeedSQL feedSQL) {
        FeedItemSQL result = null;
        Cursor c = getContentResolver()
                .query(FeedItemSQL.URI_FEED_ITEMS, FeedItemSQL.FIELDS,
                        FeedItemSQL.COL_TITLE + " IS ? AND " +
                        FeedItemSQL.COL_FEED + " IS ?",
                        Util.ToStringArray(item.getTitle(),
                                Long.toString(feedSQL.id)), null);

        try {
            if (c.moveToNext()) {
                Log.d("JONAS", "Found existing item");
                result = new FeedItemSQL(c);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        if (result == null) {
            Log.d("JONAS", "Creating new item");
            result = new FeedItemSQL();
        }

        return result;
    }
}
