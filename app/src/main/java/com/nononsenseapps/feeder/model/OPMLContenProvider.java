package com.nononsenseapps.feeder.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.FeedSQLKt;
import com.nononsenseapps.feeder.db.Util;

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
            context.getContentResolver()
                    .insert(URI_FEEDS, values);
        } else {
            context.getContentResolver().update(Uri.withAppendedPath(
                    URI_FEEDS,
                    Long.toString(feed.getId())), values, null,
                    null);
        }
    }
}
