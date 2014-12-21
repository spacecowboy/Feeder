package com.nononsenseapps.feeder.db;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * SQL which handles the feeds
 */
public class FeedSQL {
    // SQL convention says Table name should be "singular"
    public static final String TABLE_NAME = "Feed";
    // A view which also reports 'unreadcount'
    public static final String VIEWCOUNT_NAME = "WithUnreadCount";
    // A view of distinct tags and their unread counts
    public static final String VIEWTAGS_NAME = "TagsWithUnreadCount";
    // URIs
    public static final Uri URI_FEEDS = Uri.withAppendedPath(
            Uri.parse(RssContentProvider.SCHEME + RssContentProvider.AUTHORITY),
            TABLE_NAME);
    public static final Uri URI_FEEDSWITHCOUNTS =
            Uri.withAppendedPath(URI_FEEDS, VIEWCOUNT_NAME);
    public static final Uri URI_TAGSWITHCOUNTS =
            Uri.withAppendedPath(URI_FEEDS, VIEWTAGS_NAME);
    // URI codes, must be unique
    public static final int URICODE = 101;
    public static final int ITEMCODE = 102;
    public static final int VIEWCOUNTCODE = 103;
    public static final int VIEWTAGSCODE = 104;
    // Naming the id column with an underscore is good to be consistent
    // with other Android things. This is ALWAYS needed
    public static final String COL_ID = "_id";
    // These fields can be anything you want.
    public static final String COL_TITLE = "title";
    public static final String COL_URL = "url";
    public static final String COL_TAG = "tag";
    public static final String COL_ETAG = "etag";
    public static final String COL_MODIFIED = "modified";
    public static final String COL_TIMESTAMP = "timestamp";
    // Used on count view
    public static final String COL_UNREADCOUNT = "unreadcount";
    // For database projection so order is consistent
    public static final String[] FIELDS = {COL_ID, COL_TITLE, COL_URL,
            COL_TAG, COL_TIMESTAMP, COL_ETAG, COL_MODIFIED};
    public static final String[] FIELDS_VIEWCOUNT = {COL_ID, COL_TITLE,
            COL_URL, COL_TAG, COL_TIMESTAMP, COL_UNREADCOUNT};
    public static final String[] FIELDS_TAGSWITHCOUNT = {COL_ID, COL_TAG,
            COL_UNREADCOUNT};
    /*
     * The SQL code that creates a Table for storing stuff in.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_TITLE + " TEXT NOT NULL,"
                    + COL_URL + " TEXT NOT NULL,"
                    + COL_TAG + " TEXT NOT NULL DEFAULT '',"
                    + COL_TIMESTAMP + " TEXT,"
                    + COL_ETAG + " TEXT,"
                    + COL_MODIFIED + " TEXT,"
                    // Unique constraint
                    + " UNIQUE(" + COL_URL +") ON CONFLICT REPLACE"
                    + ")";
    public static final String CREATE_COUNT_VIEW =
            "CREATE TEMP VIEW IF NOT EXISTS " + VIEWCOUNT_NAME
            + " AS SELECT " + Util.arrayToCommaString(FIELDS_VIEWCOUNT)
            + " FROM " + TABLE_NAME
            + " LEFT JOIN " + " (SELECT COUNT(1) AS " + COL_UNREADCOUNT
            + "," + FeedItemSQL.COL_FEED + " FROM " + FeedItemSQL.TABLE_NAME
            + " WHERE " + FeedItemSQL.COL_UNREAD + " IS 1 " + " GROUP BY "
            + FeedItemSQL.COL_FEED + ") ON " + TABLE_NAME + "." + COL_ID
            + " = " + FeedItemSQL.COL_FEED;
    public static final String CREATE_TAGS_VIEW =
            "CREATE TEMP VIEW IF NOT EXISTS " + VIEWTAGS_NAME
            + " AS SELECT " + Util.arrayToCommaString(COL_ID, COL_TAG)
            //+ ",SUM(" + COL_UNREADCOUNT + ") AS " + COL_UNREADCOUNT
            + "," + COL_UNREADCOUNT
            + " FROM " + TABLE_NAME
            + " LEFT JOIN " + " (SELECT COUNT(1) AS " + COL_UNREADCOUNT
            + "," + FeedItemSQL.COL_TAG + " AS itemtag "
            + " FROM " + FeedItemSQL.TABLE_NAME
            + " WHERE " + FeedItemSQL.COL_UNREAD + " IS 1 "
            + " GROUP BY " + "itemtag"
            + ") ON " + TABLE_NAME + "." + COL_TAG
            + " IS " + "itemtag"
            + " GROUP BY " + COL_TAG;
    // Fields corresponding to database columns
    public long id = -1;
    public String title = null;
    public String url = null;
    public String tag = null;
    public String timestamp = null;
    // Used in server sync
    public String etag = null;
    public String modified = null;
    /**
     * No need to do anything, fields are already set to default values above
     */
    public FeedSQL() {
    }

    /**
     * Convert information from the database into a Feed object.
     */
    public FeedSQL(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.id = cursor.getLong(0);
        this.title = cursor.getString(1);
        this.url = cursor.getString(2);
        this.tag = cursor.getString(3);
        this.timestamp = cursor.getString(4);
        this.etag = cursor.getString(5);
        this.modified = cursor.getString(6);
    }

    public static void addMatcherUris(UriMatcher sURIMatcher) {
        sURIMatcher.addURI(RssContentProvider.AUTHORITY, URI_FEEDS.getPath(),
                URICODE);
        sURIMatcher.addURI(RssContentProvider.AUTHORITY,
                URI_FEEDS.getPath() + "/#", ITEMCODE);
        sURIMatcher.addURI(RssContentProvider.AUTHORITY,
                URI_FEEDSWITHCOUNTS.getPath(), VIEWCOUNTCODE);
        sURIMatcher.addURI(RssContentProvider.AUTHORITY,
                URI_TAGSWITHCOUNTS.getPath(), VIEWTAGSCODE);
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
            tag = "";
        values.put(COL_TAG, tag);
        if (timestamp == null)
            values.putNull(COL_TIMESTAMP);
        else
            values.put(COL_TIMESTAMP, timestamp);

        if (etag == null)
            values.putNull(COL_ETAG);
        else
            values.put(COL_ETAG, etag);

        if (modified == null)
            values.putNull(COL_MODIFIED);
        else
            values.put(COL_MODIFIED, modified);

        return values;
    }
}
