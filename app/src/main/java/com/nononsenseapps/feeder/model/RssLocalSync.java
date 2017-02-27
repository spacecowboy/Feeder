package com.nononsenseapps.feeder.model;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.util.FileLog;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RssLocalSync {

    private final static Executor executor = Executors.newWorkStealingPool(4);

    public static void syncFeeds(final Context context) {
        DateTime start = DateTime.now();
        FileLog log = FileLog.instance(context);

        // Get all stored feeds
        List<FeedSQL> feeds = listFeeds(context);
        log.d(String.format("Syncing %d feeds: %s", feeds.size(), start.toString()));

        // Synchronize them in parallel
        for (FeedSQL feedSQL: feeds) {
            executor.execute(() -> {
                try {
                    SyndFeed parsedFeed = FeedParser.parseFeed(feedSQL.url, context.getExternalCacheDir());

                    ArrayList<ContentProviderOperation> operations = convertFeedToDatabase(parsedFeed, feedSQL);

                    storeSyncResults(context, operations);

                    DateTime end = DateTime.now();

                    Duration duration = Duration.millis(end.getMillis() - start.getMillis());
                    log.d(String.format("Finished sync after %s of %s with %d entries", duration.toString(),
                            parsedFeed.getTitle(), parsedFeed.getEntries().size()));

                } catch (FeedParser.FeedParsingError feedParsingError) {
                    feedParsingError.printStackTrace();
                } catch (Throwable error) {
                    error.printStackTrace();
                } finally {
                    // Notify that we've updated - too often for every one
                    //RssContentProvider.notifyAllUris(context);
                    // And broadcast end of sync
                    /*LocalBroadcastManager.getInstance(getContext()).sendBroadcast
                            (bcast.putExtra(SYNC_BROADCAST_IS_ACTIVE, false));*/
                    // Send notifications for configured feeds
                    //RssNotifications.notify(context);
                }
            });
        }
    }

    private static synchronized void storeSyncResults(Context context,
                                                      ArrayList<ContentProviderOperation> operations)
            throws RemoteException, OperationApplicationException {
        if (!operations.isEmpty()) {
            context.getContentResolver()
                   .applyBatch(RssContentProvider.AUTHORITY, operations);
        }
    }

    private static ArrayList<ContentProviderOperation> convertFeedToDatabase(SyndFeed parsedFeed, FeedSQL feedSQL) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        final ContentProviderOperation.Builder feedOp =
                ContentProviderOperation.newUpdate(Uri.withAppendedPath(FeedSQL.URI_FEEDS,
                        Long.toString(feedSQL.id)));

        // Populate with values
        feedOp.withValue(FeedSQL.COL_TITLE, parsedFeed.getTitle())
              .withValue(FeedSQL.COL_TAG, feedSQL.tag == null ? "" : feedSQL.tag)
              .withValue(FeedSQL.COL_TIMESTAMP, FeedParser.timestamp(parsedFeed))
              .withValue(FeedSQL.COL_URL, parsedFeed.getLink());

        // Add to list of operations
        operations.add(feedOp.build());

        for (SyndEntry entry : parsedFeed.getEntries()) {
            // Always insert, have on conflict clause
            ContentProviderOperation.Builder itemOp = ContentProviderOperation.newInsert(FeedItemSQL.URI_FEED_ITEMS);

            // Use the actual id, because update operation will not return id
            itemOp.withValue(FeedItemSQL.COL_FEED, feedSQL.id);

            // Next all the other values. Make sure non null
            itemOp.withValue(FeedItemSQL.COL_GUID, entry.getUri())
                  .withValue(FeedItemSQL.COL_LINK, entry.getLink())
                  .withValue(FeedItemSQL.COL_FEEDTITLE, entry.getTitle())
                  .withValue(FeedItemSQL.COL_TAG,
                          feedSQL.tag == null ? "" : feedSQL.tag)
                  //.withValue(FeedItemSQL.COL_IMAGEURL, entry.get.image)
                  .withValue(FeedItemSQL.COL_JSON, "")
                  .withValue(FeedItemSQL.COL_ENCLOSURELINK, FeedParser.firstEnclosure(entry))
                  .withValue(FeedItemSQL.COL_AUTHOR, entry.getAuthor())
                  .withValue(FeedItemSQL.COL_PUBDATE, FeedParser.publishDate(entry))
                  // Make sure these are non-null
                  .withValue(FeedItemSQL.COL_TITLE, FeedParser.title(entry))
                  .withValue(FeedItemSQL.COL_DESCRIPTION, FeedParser.description(entry))
                  .withValue(FeedItemSQL.COL_PLAINTITLE, FeedParser.title(entry))
                  .withValue(FeedItemSQL.COL_PLAINSNIPPET, FeedParser.snippet(entry));

            // Add to list of operations
            operations.add(itemOp.build());
        }

        return operations;
    }

    public static List<FeedSQL> listFeeds(Context context) {
        List<FeedSQL> result = new ArrayList<>();

        Cursor c = context.getContentResolver().query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                result.add(new FeedSQL(c));
            }
            c.close();
        }

        return result;
    }
}
