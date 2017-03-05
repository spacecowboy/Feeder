package com.nononsenseapps.feeder.model;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import com.nononsenseapps.feeder.db.Cleanup;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.util.FileLog;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nononsenseapps.feeder.db.Util.LongsToStringArray;
import static com.nononsenseapps.feeder.db.Util.ToStringArray;
import static com.nononsenseapps.feeder.db.Util.WHEREIDIS;
import static com.nononsenseapps.feeder.db.Util.WhereIs;

public class RssLocalSync {

    /**
     *
     * @param feedId if less than '1' then all feeds are synchronized
     * @param tag of feeds to sync, only used if feedId is less than 1. If empty, all feeds are synced.
     */
    public static void syncFeeds(final Context context, long feedId, @NonNull String tag) {
        DateTime start = DateTime.now();
        FileLog log = FileLog.instance(context);

        // Get all stored feeds
        final List<FeedSQL> feeds;
        if (feedId > 0) {
            feeds = listFeed(context, feedId);
        } else if (!tag.isEmpty()) {
            feeds = listFeeds(context, tag);
        } else {
            feeds = listFeeds(context);
        }
        log.d(String.format("Syncing %d feeds: %s", feeds.size(), start.toString()));

        // Run in parallel - important each thread finishes in reasonable time
        List<ContentProviderOperation> ops =
                feeds.parallelStream()
                     .flatMap(f -> syncAndParseFeed(f, context.getExternalCacheDir()))
                     .collect(Collectors.toList());

        try {
            storeSyncResults(context, ops);
        } catch (RemoteException | OperationApplicationException e) {
            log.d(e.getMessage());
            e.printStackTrace();
        }

        // Finally, prune excessive items
        try {
            Cleanup.prune(context);
        } catch (RemoteException | OperationApplicationException e) {
            log.d(e.getMessage());
            e.printStackTrace();
        }

        DateTime end = DateTime.now();

        Duration duration = Duration.millis(end.getMillis() - start.getMillis());
        log.d(String.format("Finished sync after %s with %d operations", duration.toString(),
                ops.size()));

        // Notify that we've updated
        RssContentProvider.notifyAllUris(context);
        // Send notifications for configured feeds
        RssNotifications.notify(context);

    }

    private static Stream<ContentProviderOperation> syncAndParseFeed(final FeedSQL feedSQL, final File cacheDir) {
        try {
            SyndFeed parsedFeed = FeedParser.parseFeed(feedSQL.url, cacheDir);
            return convertResultToOperations(parsedFeed, feedSQL).stream();
        } catch (Throwable error) {
            System.err.println("Error when parsing " + feedSQL.url);
            error.printStackTrace();
        }
        return Stream.empty();
    }

    private static synchronized void storeSyncResults(final Context context,
                                                      final List<ContentProviderOperation> operations)
            throws RemoteException, OperationApplicationException {
        if (!operations.isEmpty()) {
            context.getContentResolver()
                   .applyBatch(RssContentProvider.AUTHORITY, new ArrayList<>(operations));
        }
    }

    /**
     * @param parsedFeed
     * @param feedSQL
     * @return A list of Operations containing no back references
     */
    private static ArrayList<ContentProviderOperation> convertResultToOperations(final SyndFeed parsedFeed,
                                                                                 final FeedSQL feedSQL) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        final ContentProviderOperation.Builder feedOp =
                ContentProviderOperation.newUpdate(Uri.withAppendedPath(FeedSQL.URI_FEEDS,
                        Long.toString(feedSQL.id)));

        // This can be null, in that case do not override existing value
        String selfLink = FeedParser.selfLink(parsedFeed);

        // Populate with values
        feedOp.withValue(FeedSQL.COL_TITLE, parsedFeed.getTitle())
              .withValue(FeedSQL.COL_TAG, feedSQL.tag == null ? "" : feedSQL.tag)
              .withValue(FeedSQL.COL_URL, selfLink == null ? feedSQL.url : selfLink);

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
                  .withValue(FeedItemSQL.COL_ENCLOSURELINK, FeedParser.firstEnclosure(entry))
                  .withValue(FeedItemSQL.COL_AUTHOR, entry.getAuthor())
                  .withValue(FeedItemSQL.COL_PUBDATE, FeedParser.publishDate(entry))
                  // Make sure these are non-null
                  .withValue(FeedItemSQL.COL_TITLE, FeedParser.title(entry))
                  .withValue(FeedItemSQL.COL_DESCRIPTION, FeedParser.description(entry))
                  .withValue(FeedItemSQL.COL_PLAINTITLE, FeedParser.plainTitle(entry))
                  .withValue(FeedItemSQL.COL_PLAINSNIPPET, FeedParser.snippet(entry));

            // Add to list of operations
            operations.add(itemOp.build());
        }

        return operations;
    }

    public static List<FeedSQL> listFeed(final Context context, final long id) {
        return FeedSQL.getFeeds(context,
                WHEREIDIS,
                LongsToStringArray(id), null);
    }

    public static List<FeedSQL> listFeeds(final Context context, @NonNull final String tag) {
        return FeedSQL.getFeeds(context,
                WhereIs(FeedSQL.COL_TAG),
                ToStringArray(tag), null);
    }

    public static List<FeedSQL> listFeeds(final Context context) {
        return FeedSQL.getFeeds(context, null, null, null);
    }
}
