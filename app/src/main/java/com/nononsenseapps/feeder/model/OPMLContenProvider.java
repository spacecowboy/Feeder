package com.nononsenseapps.feeder.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.Util;

public class OPMLContenProvider implements OPMLParserToDatabase {

    private final Context context;

    public OPMLContenProvider(final Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public FeedSQL getFeed(@NonNull String url) {
        FeedSQL result = null;

        Cursor c = context.getContentResolver()
                          .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS, FeedSQL.COL_URL + " IS ?",
                                  Util.ToStringArray(url), null);

        try {
            if (c.moveToNext()) {
                result = new FeedSQL(c);
            } else {
                result = new FeedSQL();
                result.url = url;
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
        ContentValues values = feed.getContent();
        if (feed.id < 1) {
            Uri uri = context.getContentResolver()
                             .insert(FeedSQL.URI_FEEDS, values);
            feed.id = Long.parseLong(uri.getLastPathSegment());
        } else {
            context.getContentResolver().update(Uri.withAppendedPath(
                    FeedSQL.URI_FEEDS,
                    Long.toString(feed.id)), values, null,
                    null);
        }
    }
}
