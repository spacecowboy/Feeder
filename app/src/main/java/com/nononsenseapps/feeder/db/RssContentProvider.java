package com.nononsenseapps.feeder.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class RssContentProvider extends ContentProvider {
    // All URIs share these parts
    public static final String AUTHORITY = "com.nononsenseapps.feeder.provider";
    public static final String SCHEME = "content://";

    // URIs
    public static final String FEEDS = SCHEME + AUTHORITY + "/feed";
    public static final Uri URI_FEEDS = Uri.parse(FEEDS);
    // Used for a single person, just add the id to the end
    public static final String FEED_BASE = FEEDS + "/";
    public static final Uri URI_FEED_BASE = Uri.parse(FEED_BASE);

    public RssContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = 0;
        if (uri.toString().startsWith(FEED_BASE)) {
            return delete(URI_FEEDS, FeedSQL.COL_ID + " IS ?",
                    new String[] {uri.getLastPathSegment()});
        } else if (URI_FEEDS.equals(uri)) {
            result += DatabaseHandler.getInstance(getContext())
                    .getWritableDatabase().delete(FeedSQL.TABLE_NAME,
                            selection, selectionArgs);
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        return result;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        if (URI_FEEDS.equals(uri)) {
            long id = DatabaseHandler.getInstance(getContext())
                    .getWritableDatabase()
                    .insert(FeedSQL.TABLE_NAME, null, values);
            if (id > -1) {
                result = Uri.withAppendedPath(URI_FEED_BASE, Long.toString(id));
                getContext().getContentResolver().notifyChange(URI_FEEDS,
                        null, false);
            }
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        return result;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Cursor result = null;
        if (URI_FEEDS.equals(uri)) {
            result = DatabaseHandler.getInstance(getContext())
                    .getReadableDatabase()
                    .query(FeedSQL.TABLE_NAME, projection, selection,
                            selectionArgs, null, null, sortOrder, null);
        } else if (uri.toString().startsWith(FEED_BASE)) {
            final long id = Long.parseLong(uri.getLastPathSegment());
            result = DatabaseHandler.getInstance(getContext())
                    .getReadableDatabase()
                    .query(FeedSQL.TABLE_NAME, projection,
                            FeedSQL.COL_ID + " IS ?",
                            new String[]{String.valueOf(id)}, null, null, null,
                            null);
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        // Make sure you don't override another uri here
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int result = 0;
        // TODO compare URIs better later
        if (uri.toString().startsWith(FEED_BASE)) {
            String id = uri.getLastPathSegment();
            result =  DatabaseHandler.getInstance(getContext())
                    .getWritableDatabase()
                    .update(FeedSQL.TABLE_NAME, values, FeedSQL.WHEREIDIS,
                           new String[] {id});
            if (result > 0) {
                getContext().getContentResolver().notifyChange(URI_FEEDS,
                        null, false);
            }
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        return result;
    }
}
