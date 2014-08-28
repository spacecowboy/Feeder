package com.nononsenseapps.feeder.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class RssContentProvider extends ContentProvider {
    // Match Uris with this
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        FeedSQL.addMatcherUris(sURIMatcher);
        FeedItemSQL.addMatcherUris(sURIMatcher);
    }

    // All URIs share these parts
    public static final String AUTHORITY = "com.nononsenseapps.feeder.provider";
    public static final String SCHEME = "content://";

    public RssContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = 0;
        Uri[] notifyUris = null;

        switch (sURIMatcher.match(uri)) {
            case FeedSQL.ITEMCODE:
                return delete(FeedSQL.URI_FEEDS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.getLastPathSegment()));
            case FeedSQL.URICODE:
                result += DatabaseHandler.getInstance(getContext())
                        .getWritableDatabase().delete(FeedSQL.TABLE_NAME,
                                selection, selectionArgs);
                notifyUris = new Uri[] {FeedSQL.URI_FEEDS,
                        FeedItemSQL.URI_FEED_ITEMS};
                break;
            case FeedItemSQL.ITEMCODE:
                return delete(FeedItemSQL.URI_FEED_ITEMS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.getLastPathSegment()));
            case FeedItemSQL.URICODE:
                result += DatabaseHandler.getInstance(getContext())
                        .getWritableDatabase().delete(FeedItemSQL.TABLE_NAME,
                                selection, selectionArgs);
                notifyUris = new Uri[] {FeedItemSQL.URI_FEED_ITEMS};
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        if (notifyUris != null) {
            notifyChange(notifyUris);
        }
        return result;
    }

    /**
     * Notify that a change has happened on each specified uri
     * @param uris
     */
    private void notifyChange(Uri... uris) {
        for (Uri uri: uris) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }
    }

    @Override
    public String getType(Uri uri) {
        // Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result;
        Uri[] notifyUris;
        final String table;

        switch (sURIMatcher.match(uri)) {
            case FeedSQL.URICODE:
                table = FeedSQL.TABLE_NAME;
                result = FeedSQL.URI_FEEDS;
                notifyUris = new Uri[] {FeedSQL.URI_FEEDS,
                        FeedSQL.URI_FEEDSWITHCOUNTS,
                        FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case FeedItemSQL.URICODE:
                table = FeedItemSQL.TABLE_NAME;
                result = FeedItemSQL.URI_FEED_ITEMS;
                notifyUris = new Uri[] {FeedItemSQL.URI_FEED_ITEMS,
                        FeedSQL.URI_FEEDSWITHCOUNTS,
                        FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        long id = DatabaseHandler.getInstance(getContext())
                .getWritableDatabase()
                .insert(table, null, values);
        if (id > -1) {
            result = Uri.withAppendedPath(result, Long.toString(id));
        }

        if (result != null && notifyUris != null) {
            notifyChange(notifyUris);
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
        final String table;
        Cursor result = null;

        switch (sURIMatcher.match(uri)) {
            case FeedSQL.ITEMCODE:
                table = FeedSQL.TABLE_NAME;
                selection = Util.WHEREIDIS;
                selectionArgs = Util.ToStringArray(uri.getLastPathSegment());
                break;
            case FeedSQL.URICODE:
                table = FeedSQL.TABLE_NAME;
                break;
            case FeedSQL.VIEWCOUNTCODE:
                table = FeedSQL.VIEWCOUNT_NAME;
                break;
            case FeedSQL.VIEWTAGSCODE:
                table = FeedSQL.VIEWTAGS_NAME;
                break;
            case FeedItemSQL.ITEMCODE:
                table = FeedItemSQL.TABLE_NAME;
                selection = Util.WHEREIDIS;
                selectionArgs = Util.ToStringArray(uri.getLastPathSegment());
                break;
            case FeedItemSQL.URICODE:
                table = FeedItemSQL.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        result = DatabaseHandler.getInstance(getContext())
                .getReadableDatabase()
                .query(table, projection, selection,
                        selectionArgs, null, null, sortOrder, null);

        // Make sure you don't override another uri here
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        final String table;
        Uri[] notifyUris = null;
        int result = 0;
        switch (sURIMatcher.match(uri)) {
            case FeedSQL.ITEMCODE:
                table = FeedSQL.TABLE_NAME;
                selection = Util.WHEREIDIS;
                selectionArgs = Util.ToStringArray(uri.getLastPathSegment());
                notifyUris = new Uri[] {FeedSQL.URI_FEEDS,
                        FeedItemSQL.URI_FEED_ITEMS, FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case FeedSQL.URICODE:
                table = FeedSQL.TABLE_NAME;
                notifyUris = new Uri[] {FeedSQL.URI_FEEDS,
                        FeedItemSQL.URI_FEED_ITEMS, FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case FeedItemSQL.ITEMCODE:
                table = FeedItemSQL.TABLE_NAME;
                selection = Util.WHEREIDIS;
                selectionArgs = Util.ToStringArray(uri.getLastPathSegment());
                notifyUris = new Uri[] {FeedItemSQL.URI_FEED_ITEMS,
                        FeedSQL.URI_FEEDSWITHCOUNTS, FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case FeedItemSQL.URICODE:
                table = FeedItemSQL.TABLE_NAME;
                notifyUris = new Uri[] {FeedItemSQL.URI_FEED_ITEMS,
                        FeedSQL.URI_FEEDSWITHCOUNTS, FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        result = DatabaseHandler.getInstance(getContext())
                .getWritableDatabase().update(table,
                        values, selection, selectionArgs);

        if (result > 0 && notifyUris != null) {
            notifyChange(notifyUris);
        }

        return result;
    }

    /**
     * Mark a feedItem as read in the database.
     * @param context
     * @param itemId
     */
    public static void MarkItemAsRead(final Context context, final long itemId) {
        ContentValues values = new ContentValues();
        values.put(FeedItemSQL.COL_UNREAD, 0);
        context.getContentResolver().update(FeedItemSQL.URI_FEED_ITEMS, values,
                Util.WHEREIDIS, Util.LongsToStringArray(itemId));
    }
    /**
     * Mark all items in a feed as read in the database.
     * @param context
     * @param feedId
     */
    public static void MarkFeedAsRead(final Context context,
            final long feedId) {
        ContentValues values = new ContentValues();
        values.put(FeedItemSQL.COL_UNREAD, 0);
        context.getContentResolver().update(FeedItemSQL.URI_FEED_ITEMS, values,
                FeedItemSQL.COL_FEED + " IS ?", Util.LongsToStringArray
                        (feedId));
    }
}
