package com.nononsenseapps.feeder.db;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * SQL which handles items belonging to a Feed
 */
public class FeedItemSQL {
    // SQL convention says Table name should be "singular", so not Persons
    public static final String TABLE_NAME = "FeedItem";
    // Naming the id column with an underscore is good to be consistent
    // with other Android things. This is ALWAYS needed
    public static final String COL_ID = "_id";
    // These fields can be anything you want.
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_PLAINTITLE = "plaintitle";
    public static final String COL_PLAINSNIPPET = "plainsnippet";
    public static final String COL_IMAGEURL = "imageurl";
    public static final String COL_LINK = "link";
    public static final String COL_AUTHOR = "author";
    public static final String COL_DATE = "date";
    // These fields corresponds to columns in Feed table
    public static final String COL_FEED = "feed";
    public static final String COL_TAG = "tag";

    // For database projection so order is consistent
    public static final String[] FIELDS =
            {COL_ID, COL_TITLE, COL_DESCRIPTION, COL_PLAINTITLE, COL_PLAINSNIPPET, COL_IMAGEURL,
                    COL_LINK, COL_AUTHOR, COL_DATE, COL_FEED, COL_TAG};

    /*
     * The SQL code that creates a Table for storing Persons in.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_TITLE + " TEXT NOT NULL,"
                    + COL_DESCRIPTION + " TEXT NOT NULL,"
                    + COL_PLAINTITLE + " TEXT NOT NULL,"
                    + COL_PLAINSNIPPET + " TEXT NOT NULL," +
                    COL_IMAGEURL + " TEXT," +
                    COL_LINK + " TEXT NOT NULL," +
                    COL_AUTHOR + " TEXT NOT NULL," +
                    COL_DATE + " TEXT NOT NULL," +
                    COL_FEED + " INTEGER NOT NULL," +
                    COL_TAG + " TEXT," +
                    // Handle foreign key stuff
                    " FOREIGN KEY(" + COL_FEED + ") REFERENCES " + FeedSQL.TABLE_NAME + "(" +
                    FeedSQL.COL_ID + ") ON DELETE CASCADE"
                    + ")";

    // Fields corresponding to database columns
    public long id = -1;
    public String title = null;
    public String link = null;
    // TODO

    /**
     * No need to do anything, fields are already set to default values above
     */
    public FeedItemSQL() {
    }

    /**
     * Convert information from the database into a Person object.
     */
    public FeedItemSQL(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.id = cursor.getLong(0);
        this.title = cursor.getString(1);
        this.link = cursor.getString(2);

        // TODO
    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_TITLE, title);
        values.put(COL_LINK, link);
        // TODO

        return values;
    }
}
