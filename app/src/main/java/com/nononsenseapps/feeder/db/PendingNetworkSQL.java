package com.nononsenseapps.feeder.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * A table which stores pending network operations which could not be
 * completed due to lack of network.
 */
public class PendingNetworkSQL {
    // SQL convention says Table name should be "singular"
    public static final String TABLE_NAME = "PendingNetwork";
    // URIs
    public static final Uri URI = Uri.withAppendedPath(
            Uri.parse(RssContentProvider.SCHEME + RssContentProvider.AUTHORITY),
            TABLE_NAME);
    // URI codes, must be unique
    private static final int BASECODE = 300;
    public static final int URICODE = BASECODE + 1;
    public static final int ITEMCODE = BASECODE + 2;
    // Naming the id column with an underscore is good to be consistent
    // with other Android things. This is ALWAYS needed
    public static final String COL_ID = "_id";
    // These fields can be anything you want.
    public static final String COL_TYPE = "type";
    public static final String COL_URL = "url";
    public static final String COL_FEED_ID = "feed_id";
    // For database projection so order is consistent
    public static final String[] FIELDS = {COL_ID, COL_TYPE, COL_URL, COL_FEED_ID};
    /*
     * The SQL code that creates a Table for storing stuff in.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
            + COL_ID + " INTEGER PRIMARY KEY,"
            + COL_TYPE + " TEXT NOT NULL,"
            + COL_FEED_ID + " INTEGER NOT NULL DEFAULT -1,"
            + COL_URL + " TEXT NOT NULL,"
            // Handle foreign key stuff
            + " FOREIGN KEY(" + COL_FEED_ID + ") REFERENCES " + FeedSQL.TABLE_NAME + "(" +
            FeedSQL.COL_ID + ") ON DELETE CASCADE,"
            // Unique constraint
            + " UNIQUE(" + COL_URL + ") ON CONFLICT REPLACE"
            + ")";

    // Fields corresponding to database columns
    public long id = -1;
    public String type = null;
    public String url = null;
    public long feed_id = -1;

    // Put is both insert and update
    public final static String TYPE_PUT = "put";
    public final static String TYPE_DELETE = "delete";

    public static void storePut(final Context context,
            final long feed_id,
            final String url) {
        storePending(context, feed_id, url, TYPE_PUT);
    }
    public static void storeDelete(final Context context,
            final long feed_id,
            final String url) {
        storePending(context, feed_id, url, TYPE_DELETE);
    }

    private static void storePending(final Context context,
            final long feed_id,
            final String url, final String type) {
        PendingNetworkSQL pending = new PendingNetworkSQL();
        pending.feed_id = feed_id;
        pending.type = type;
        pending.url = url;

        // If something already is present, it will override it
        // TODO changing existing item would create second item on server
        context.getContentResolver().insert(URI, pending.getContent());
    }

    public PendingNetworkSQL() {}

    public PendingNetworkSQL(final Cursor cursor) {
        this.id = cursor.getLong(0);
        this.type = cursor.getString(1);
        this.url = cursor.getString(2);
        this.feed_id = cursor.getLong(3);
    }

    public boolean isPut() {
        return TYPE_PUT.equals(type);
    }
    public boolean isDelete() {
        return TYPE_DELETE.equals(type);
    }

    public static void addMatcherUris(UriMatcher sURIMatcher) {
        sURIMatcher.addURI(RssContentProvider.AUTHORITY, URI.getPath(),
                URICODE);
        sURIMatcher.addURI(RssContentProvider.AUTHORITY,
                URI.getPath() + "/#", ITEMCODE);
    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_TYPE, type);
        values.put(COL_URL, url);
        values.put(COL_FEED_ID, feed_id);

        return values;
    }
}
