/*
 * Copyright (c) 2016 Jonas Kalderstam.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.feeder.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.nononsenseapps.feeder.model.OPMLDatabaseHandler;
import com.nononsenseapps.feeder.model.OPMLParser;
import com.nononsenseapps.feeder.model.OPMLWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.nononsenseapps.feeder.db.Util.WhereIs;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "rssDatabase";
    private static DatabaseHandler singleton;
    private final Context context;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Good idea to use process context here
        this.context = context.getApplicationContext();
    }

    public static DatabaseHandler getInstance(final Context context) {
        if (singleton == null) {
            singleton = new DatabaseHandler(context);
        }
        return singleton;
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(FeedSQL.CREATE_TABLE);
        sqLiteDatabase.execSQL(FeedItemSQL.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int i1, final int i2) {
        try {
            createViewsAndTriggers(db);
            // Export to OPML
            File tempFile = new File(context.getExternalCacheDir(), "upgrade.opml");
            OPMLWriter opmlWriter = new OPMLWriter();
            opmlWriter.writeFile(tempFile.getAbsolutePath(),
                    tagSupplier(db), feedsWithTag(db));

            // Delete database
            deleteEverything(db);

            // Create database
            onCreate(db);

            // Import OMPL
            OPMLParser parser = new OPMLParser(new OPMLDatabaseHandler(db));
            parser.parseFile(tempFile.getAbsolutePath());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.setForeignKeyConstraintsEnabled(true);
        }
        createViewsAndTriggers(db);
    }

    private void deleteEverything(SQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS " + FeedItemSQL.TRIGGER_NAME);

        db.execSQL("DROP VIEW IF EXISTS " + FeedSQL.VIEWCOUNT_NAME);
        db.execSQL("DROP VIEW IF EXISTS " + FeedSQL.VIEWTAGS_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + FeedSQL.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FeedItemSQL.TABLE_NAME);
    }

    private void createViewsAndTriggers(SQLiteDatabase db ) {
        // Create triggers
        db.execSQL(FeedItemSQL.CREATE_TAG_TRIGGER);
        // Create views if not exists
        db.execSQL(FeedSQL.CREATE_COUNT_VIEW);
        db.execSQL(FeedSQL.CREATE_TAGS_VIEW);
    }

    private Function<String, Iterable<FeedSQL>> feedsWithTag(final SQLiteDatabase db) {
        return tag -> {
            ArrayList<FeedSQL> feeds = new ArrayList<>();

            final String where = WhereIs(FeedSQL.COL_TAG);
            final String[] args = Util.ToStringArray(tag == null ? "": tag);

            Cursor c = db.query(FeedSQL.TABLE_NAME, FeedSQL.FIELDS, where, args,
                    null, null, null);

            try {
                while (c.moveToNext()) {
                    feeds.add(new FeedSQL(c));
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            return feeds;
        };
    }

    private Supplier<Iterable<String>> tagSupplier(SQLiteDatabase db) {
        return () -> {
            ArrayList<String> tags = new ArrayList<>();

            Cursor c = db.query(FeedSQL.VIEWTAGS_NAME,
                    Util.ToStringArray(FeedSQL.COL_TAG),
                    null, null, null, null, null);

            try {
                while (c.moveToNext()) {
                    tags.add(c.getString(0));
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            return tags;
        };
    }
}
