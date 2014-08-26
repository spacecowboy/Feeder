package com.nononsenseapps.feeder.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class RssContentProvider extends ContentProvider {
    // All URIs share these parts
    public static final String AUTHORITY = "com.nononsenseapps.feeder.provider";
    public static final String SCHEME = "content://";

    // URIs
    public static final String FEEDS = SCHEME + AUTHORITY + "/feed";
    public static final Uri URI_FEEDS = Uri.parse(FEEDS);
    public static final String FEED_ITEMS = SCHEME + AUTHORITY + "/items";
    public static final Uri URI_FEED_ITEMS = Uri.parse(FEED_ITEMS);
    // Used for a single item, just add the id to the end
    public static final String FEED_BASE = FEEDS + "/";
    public static final Uri URI_FEED_BASE = Uri.parse(FEED_BASE);
    public static final String FEED_ITEM_BASE = FEED_ITEMS + "/";
    public static final Uri URI_FEED_ITEM_BASE = Uri.parse(FEED_ITEM_BASE);

    public RssContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = 0;
        if (uri.toString().startsWith(FEED_BASE)) {
            return delete(URI_FEEDS, Util.WHEREIDIS,
                    Util.ToStringArray(uri.getLastPathSegment()));
        } else if (URI_FEEDS.equals(uri)) {
            result += DatabaseHandler.getInstance(getContext())
                    .getWritableDatabase().delete(FeedSQL.TABLE_NAME,
                            selection, selectionArgs);
        } else if (uri.toString().startsWith(FEED_ITEM_BASE)) {
            return delete(URI_FEED_ITEMS, Util.WHEREIDIS,
                    Util.ToStringArray(uri.getLastPathSegment()));
        } else if (URI_FEED_ITEMS.equals(uri)) {
            result += DatabaseHandler.getInstance(getContext())
                    .getWritableDatabase().delete(FeedItemSQL.TABLE_NAME,
                            selection, selectionArgs);
        }
        else {
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
        } else if (URI_FEED_ITEMS.equals(uri)) {
            long id = DatabaseHandler.getInstance(getContext())
                    .getWritableDatabase()
                    .insert(FeedItemSQL.TABLE_NAME, null, values);
            if (id > -1) {
                result = Uri.withAppendedPath(URI_FEED_ITEM_BASE,
                        Long.toString(id));
                getContext().getContentResolver().notifyChange(URI_FEED_ITEMS,
                        null, false);
            }
        }
        else {
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
                            Util.WHEREIDIS,
                            Util.LongsToStringArray(id), null, null, null,
                            null);
        } else if (URI_FEED_ITEMS.equals(uri)) {
            result = DatabaseHandler.getInstance(getContext())
                    .getReadableDatabase()
                    .query(FeedItemSQL.TABLE_NAME, projection, selection,
                            selectionArgs, null, null, sortOrder, null);
        } else if (uri.toString().startsWith(FEED_ITEM_BASE)) {
            final long id = Long.parseLong(uri.getLastPathSegment());
            result = DatabaseHandler.getInstance(getContext())
                    .getReadableDatabase()
                    .query(FeedItemSQL.TABLE_NAME, projection,
                            Util.WHEREIDIS, Util.LongsToStringArray(id),
                            null, null, null, null);
        }
        else {
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
                    .update(FeedSQL.TABLE_NAME, values, Util.WHEREIDIS,
                           Util.ToStringArray(id));
            if (result > 0) {
                getContext().getContentResolver().notifyChange(URI_FEEDS,
                        null, false);
            }
        } else if (uri.toString().startsWith(FEED_ITEM_BASE)) {
            String id = uri.getLastPathSegment();
            result =  DatabaseHandler.getInstance(getContext())
                    .getWritableDatabase()
                    .update(FeedItemSQL.TABLE_NAME, values, Util.WHEREIDIS,
                            Util.ToStringArray(id));
            if (result > 0) {
                getContext().getContentResolver().notifyChange(URI_FEED_ITEMS,
                        null, false);
            }
        } else if (uri.equals(URI_FEED_ITEMS)) {
            result = DatabaseHandler.getInstance(getContext())
                    .getWritableDatabase().update(FeedItemSQL.TABLE_NAME,
                            values, selection, selectionArgs);
            if (result > 0) {
                getContext().getContentResolver().notifyChange(URI_FEED_ITEMS,
                        null, false);
            }
        } else if (uri.equals(URI_FEEDS)) {
            result = DatabaseHandler.getInstance(getContext())
                    .getWritableDatabase()
                    .update(FeedSQL.TABLE_NAME, values, selection,
                            selectionArgs);
            if (result > 0) {
                getContext().getContentResolver()
                        .notifyChange(URI_FEEDS, null, false);
            }
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        return result;
    }

    /**
     * Mark a feedItem as read in the database.
     * @param context
     * @param itemId
     */
    public static void MarkAsRead(final Context context, final long itemId) {
        ContentValues values = new ContentValues();
        values.put(FeedItemSQL.COL_UNREAD, 0);
        context.getContentResolver().update(URI_FEED_ITEMS, values,
                Util.WHEREIDIS, Util.LongsToStringArray(itemId));
    }
}
