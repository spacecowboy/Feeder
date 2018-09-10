package com.nononsenseapps.feeder.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.FeedSQLKt;
import com.nononsenseapps.feeder.db.Util;
import com.nononsenseapps.feeder.util.ContentResolverExtensionsKt;
import com.nononsenseapps.feeder.util.ContextExtensionsKt;
import com.nononsenseapps.feeder.util.CursorExtensionsKt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_ID;
import static com.nononsenseapps.feeder.db.FeedSQLKt.COL_URL;
import static com.nononsenseapps.feeder.db.UriKt.URI_FEEDS;

// WHen Kotlinized you can probably remove a redundant RssContentProvider.NotifyAll call in use of importer in FeedActivity
public class OPMLContenProvider implements OPMLParserToDatabase {

    private final Context context;

    public OPMLContenProvider(final Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public FeedSQL getFeed(@NonNull String url) {
        FeedSQL result = null;

        Cursor c = context.getContentResolver()
                .query(URI_FEEDS, FeedSQLKt.FEED_FIELDS, FeedSQLKt.COL_URL + " IS ?",
                        Util.ToStringArray(url), null);

        try {
            if (c.moveToNext()) {
                result = FeedSQLKt.asFeed(c);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return result;
    }

    @Override
    public void saveFeed(@NonNull FeedSQL feed) {
        ContentValues values = feed.asContentValues();
        if (feed.getId() < 1) {
            List<FeedSQL> feeds = ContentResolverExtensionsKt.getFeeds(context.getContentResolver(),
                    Arrays.asList(COL_ID),
                    COL_URL + " IS ?",
                    Arrays.asList(feed.getUrl()),
                    null);
            if (feeds.isEmpty()) {
                context.getContentResolver()
                        .insert(URI_FEEDS, values);
            } else {
                context.getContentResolver().update(Uri.withAppendedPath(
                        URI_FEEDS,
                        Long.toString(feeds.get(0).getId())),
                        values,
                        null,
                        null);
            }
        } else {
            context.getContentResolver().update(Uri.withAppendedPath(
                    URI_FEEDS,
                    Long.toString(feed.getId())),
                    values,
                    null,
                    null);
        }
    }
}
