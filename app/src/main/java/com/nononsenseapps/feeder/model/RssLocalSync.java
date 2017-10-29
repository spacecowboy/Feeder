package com.nononsenseapps.feeder.model;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.nononsenseapps.feeder.db.Cleanup;
import com.nononsenseapps.feeder.db.FeedItemSQLKt;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.FeedSQLKt;
import com.nononsenseapps.feeder.util.Consumer;
import com.nononsenseapps.feeder.util.ContentResolverExtensionsKt;
import com.nononsenseapps.feeder.util.FileLog;
import com.nononsenseapps.feeder.util.Optional;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_TAG;
import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_TITLE;
import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_URL;
import static com.nononsenseapps.feeder.db.FeedSQLKt.FEED_FIELDS;
import static com.nononsenseapps.feeder.db.RssContentProviderKt.AUTHORITY;
import static com.nononsenseapps.feeder.db.UriKt.URI_FEEDITEMS;
import static com.nononsenseapps.feeder.db.UriKt.URI_FEEDS;
import static com.nononsenseapps.feeder.db.Util.LongsToStringArray;
import static com.nononsenseapps.feeder.db.Util.ToStringArray;
import static com.nononsenseapps.feeder.db.Util.WHEREIDIS;
import static com.nononsenseapps.feeder.db.Util.WhereIs;

public class RssLocalSync {

    /**
     * @param feedId if less than '1' then all feeds are synchronized
     * @param tag    of feeds to sync, only used if feedId is less than 1. If empty, all feeds are synced.
     */
    public static void syncFeeds(final Context context, long feedId, @NonNull String tag) {
        final FileLog log = FileLog.singleton.getInstance(context);
        try {
            DateTime start = DateTime.now();

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

            final File cacheDir = context.getExternalCacheDir();

            for (final FeedSQL f : feeds) {
                syncFeed(f, cacheDir).ifPresent(new Consumer<SyndFeed>() {
                    @Override
                    public void accept(@NonNull final SyndFeed sf) {
                        ArrayList<ContentProviderOperation> ops = convertResultToOperations(sf, f, context.getContentResolver());
                        try {
                            storeSyncResults(context, ops);
                            // Notify that we've updated
                            ContentResolverExtensionsKt.notifyAllUris(context.getContentResolver());
                        } catch (RemoteException | OperationApplicationException e) {
                            log.d(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
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
            log.d(String.format("Finished sync after %s", duration.toString()));

            // Notify that we've updated
            ContentResolverExtensionsKt.notifyAllUris(context.getContentResolver());
            // Send notifications for configured feeds
            RssNotificationsKt.notify(context);
        } catch (Throwable e) {
            log.d("Some fatal error during sync: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private static Optional<SyndFeed> syncFeed(final FeedSQL feedSQL, final File cacheDir) {
        try {
            return Optional.of(FeedParser.INSTANCE.parseFeed(feedSQL.getUrl(), cacheDir));
        } catch (Throwable error) {
            System.err.println("Error when syncing " + feedSQL.getUrl());
            error.printStackTrace();
        }
        return Optional.empty();
    }

    private static synchronized void storeSyncResults(final Context context,
                                                      final List<ContentProviderOperation> operations)
            throws RemoteException, OperationApplicationException {
        if (!operations.isEmpty()) {
            context.getContentResolver()
                    .applyBatch(AUTHORITY, new ArrayList<>(operations));
        }
    }

    /**
     * @param parsedFeed
     * @param feedSQL
     * @return A list of Operations containing no back references
     */
    private static ArrayList<ContentProviderOperation> convertResultToOperations(final SyndFeed parsedFeed,
                                                                                 final FeedSQL feedSQL,
                                                                                 final ContentResolver resolver) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        final ContentProviderOperation.Builder feedOp =
                ContentProviderOperation.newUpdate(Uri.withAppendedPath(URI_FEEDS,
                        Long.toString(feedSQL.getId())));

        // This can be null, in that case do not override existing value
        String selfLink = FeedParser.INSTANCE.selfLink(parsedFeed);

        // Populate with values
        feedOp.withValue(COL_TITLE, parsedFeed.getTitle())
                .withValue(COL_TAG, feedSQL.getTag())
                .withValue(COL_URL, selfLink == null ? feedSQL.getUrl() : selfLink);

        // Add to list of operations
        operations.add(feedOp.build());

        for (SyndEntry entry : parsedFeed.getEntries()) {
            final ContentProviderOperation.Builder itemOp;

            final long itemId = ContentResolverExtensionsKt.getIdForFeedItem(resolver, entry.getUri(), feedSQL.getId());
            if (itemId < 1) {
                itemOp = ContentProviderOperation.newInsert(URI_FEEDITEMS);
            } else {
                itemOp = ContentProviderOperation.newUpdate(Uri.withAppendedPath(URI_FEEDITEMS, Long.toString(itemId)));
            }

            // Use the actual id, because update operation will not return id
            itemOp.withValue(FeedItemSQLKt.COL_FEED, feedSQL.getId());

            // Next all the other values. Make sure non null
            itemOp.withValue(FeedItemSQLKt.COL_GUID, entry.getUri())
                    .withValue(FeedItemSQLKt.COL_LINK, entry.getLink())
                    .withValue(FeedItemSQLKt.COL_FEEDTITLE, feedSQL.getTitle())
                    .withValue(FeedSQLKt.COL_TAG, feedSQL.getTag())
                    .withValue(FeedItemSQLKt.COL_IMAGEURL, FeedParser.INSTANCE.thumbnail(entry))
                    .withValue(FeedItemSQLKt.COL_ENCLOSURELINK, FeedParser.INSTANCE.firstEnclosure(entry))
                    .withValue(FeedItemSQLKt.COL_AUTHOR, entry.getAuthor())
                    .withValue(FeedItemSQLKt.COL_PUBDATE, FeedParser.INSTANCE.publishDate(entry))
                    // Make sure these are non-null
                    .withValue(FeedSQLKt.COL_TITLE, FeedParser.INSTANCE.title(entry))
                    .withValue(FeedItemSQLKt.COL_DESCRIPTION, FeedParser.INSTANCE.description(entry))
                    .withValue(FeedItemSQLKt.COL_PLAINTITLE, FeedParser.INSTANCE.plainTitle(entry))
                    .withValue(FeedItemSQLKt.COL_PLAINSNIPPET, FeedParser.INSTANCE.snippet(entry));

            // Add to list of operations
            operations.add(itemOp.build());
        }

        return operations;
    }

    public static List<FeedSQL> listFeed(final Context context, final long id) {
        return ContentResolverExtensionsKt.getFeeds(context.getContentResolver(),
                Arrays.asList(FEED_FIELDS), WHEREIDIS, Arrays.asList(LongsToStringArray(id)), null);
    }

    public static List<FeedSQL> listFeeds(final Context context, @NonNull final String tag) {
        return ContentResolverExtensionsKt.getFeeds(context.getContentResolver(),
                Arrays.asList(FEED_FIELDS), WhereIs(COL_TAG), Arrays.asList(ToStringArray(tag)), null);
    }

    public static List<FeedSQL> listFeeds(final Context context) {
        return ContentResolverExtensionsKt.getFeeds(context.getContentResolver(),
                Arrays.asList(FEED_FIELDS), null, null, null);
    }
}
