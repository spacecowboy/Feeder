package com.nononsenseapps.feeder.db;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * SQL which handles the feeds
 */
public class FeedSQL {
    // SQL convention says Table name should be "singular", so not Persons
    public static final String TABLE_NAME = "Feed";
    // Naming the id column with an underscore is good to be consistent
    // with other Android things. This is ALWAYS needed
    public static final String COL_ID = "_id";
    // These fields can be anything you want.
    public static final String COL_TITLE = "title";
    public static final String COL_URL = "url";
    public static final String COL_TAG = "tag";

    // Does not include "where"
    public static final String WHEREIDIS = COL_ID + " IS ?";

    // For database projection so order is consistent
    public static final String[] FIELDS = {COL_ID, COL_TITLE, COL_URL, COL_TAG};

    /*
     * The SQL code that creates a Table for storing Persons in.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_TITLE + " TEXT NOT NULL,"
                    + COL_URL + " TEXT NOT NULL,"
                    + COL_TAG + " TEXT"
                    + ")";

    // Fields corresponding to database columns
    public long id = -1;
    public String title = null;
    public String url = null;
    public String tag = null;

    /**
     * No need to do anything, fields are already set to default values above
     */
    public FeedSQL() {
    }

    /**
     * Convert information from the database into a Person object.
     */
    public FeedSQL(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.id = cursor.getLong(0);
        this.title = cursor.getString(1);
        this.url = cursor.getString(2);
        this.tag = cursor.getString(3);
    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_TITLE, title);
        values.put(COL_URL, url);
        if (tag == null)
            values.putNull(COL_TAG);
        else
            values.put(COL_TAG, tag);

        return values;
    }
}
