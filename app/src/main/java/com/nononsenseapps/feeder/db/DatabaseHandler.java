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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "rssDatabase";
    private static DatabaseHandler singleton;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Good idea to use process context here
        //this.context = context.getApplicationContext();
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
        sqLiteDatabase.execSQL(PendingNetworkSQL.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int i,
            final int i2) {
        //sqLiteDatabase.execSQL("ALTER TABLE Feed ADD COLUMN notify INTEGER NOT NULL DEFAULT 0;");
        sqLiteDatabase.execSQL("ALTER TABLE FeedItem ADD COLUMN json TEXT;");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.setForeignKeyConstraintsEnabled(true);
        }
        // Create triggers
        db.execSQL(FeedItemSQL.CREATE_TAG_TRIGGER);
        // Create views if not exists
        db.execSQL(FeedSQL.CREATE_COUNT_VIEW);
        db.execSQL(FeedSQL.CREATE_TAGS_VIEW);
    }
}
