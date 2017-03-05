package com.nononsenseapps.feeder.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.Util;

import static com.nononsenseapps.feeder.db.Util.LongsToStringArray;
import static com.nononsenseapps.feeder.db.Util.WHEREIDIS;
import static com.nononsenseapps.feeder.db.Util.WhereIs;

public class OPMLDatabaseHandler implements OPMLParserToDatabase {
    private final SQLiteDatabase db;

    public OPMLDatabaseHandler(@NonNull final SQLiteDatabase db) {
        this.db = db;
    }

    @NonNull
    @Override
    public FeedSQL getFeed(@NonNull String url) {
        FeedSQL result = null;

        Cursor c = db.query(FeedSQL.TABLE_NAME,
                FeedSQL.FIELDS, WhereIs(FeedSQL.COL_URL),
                Util.ToStringArray(url),
                null, null, null);

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
            feed.id = db.insert(FeedSQL.TABLE_NAME, null, values);
        } else {
            db.update(FeedSQL.TABLE_NAME, values, WHEREIDIS, LongsToStringArray(feed.id));
        }
    }
}
