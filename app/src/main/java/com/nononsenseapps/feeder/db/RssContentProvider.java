package com.nononsenseapps.feeder.db;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.nononsenseapps.feeder.model.AuthHelper;
import com.nononsenseapps.feeder.ui.FeedActivity;

import java.util.ArrayList;

public class RssContentProvider extends ContentProvider {
    // All URIs share these parts
    public static final String AUTHORITY = "com.nononsenseapps.feeder.provider";
    public static final String SCHEME = "content://";
    // Match Uris with this
    private static final UriMatcher sURIMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        FeedSQL.addMatcherUris(sURIMatcher);
        FeedItemSQL.addMatcherUris(sURIMatcher);
        PendingNetworkSQL.addMatcherUris(sURIMatcher);
    }

    // If the contentprovider notifies changes on uris
    private static boolean sShouldNotify = true;

    public RssContentProvider() {
    }

    /**
     * Notify all uris that changes have happened. Should be called if you
     * ever disabled notifications on the provider.
     *
     * @param context
     */
    public static void notifyAllUris(final Context context) {
        for (Uri uri : new Uri[]{FeedSQL.URI_FEEDS,
                FeedSQL.URI_TAGSWITHCOUNTS,
                FeedSQL.URI_FEEDSWITHCOUNTS,
                FeedItemSQL.URI_FEED_ITEMS}) {
            context.getContentResolver().notifyChange(uri, null, false);
        }
    }

    /**
     * Mark a feedItem as read in the database.
     *
     * @param context
     * @param itemId
     */
    public static void MarkItemAsRead(final Context context,
            final long itemId) {
        ContentValues values = new ContentValues();
        values.put(FeedItemSQL.COL_UNREAD, 0);
        context.getContentResolver()
                .update(FeedItemSQL.URI_FEED_ITEMS, values, Util.WHEREIDIS,
                        Util.LongsToStringArray(itemId));
    }

    /**
     * Mark all items in a feed as read in the database.
     *
     * @param context
     * @param feedId
     */
    public static void MarkFeedAsRead(final Context context,
            final long feedId) {
        ContentValues values = new ContentValues();
        values.put(FeedItemSQL.COL_UNREAD, 0);
        context.getContentResolver().update(FeedItemSQL.URI_FEED_ITEMS, values,
                FeedItemSQL.COL_FEED + " IS ?",
                Util.LongsToStringArray(feedId));
    }

    /**
     * Mark all items in feeds as read in the database.
     *
     * @param context
     * @param tag     all feeds with this tag will be marked as read
     */
    public static void MarkItemsAsRead(final Context context,
            final String tag) {
        ContentValues values = new ContentValues();
        values.put(FeedItemSQL.COL_UNREAD, 0);
        context.getContentResolver().update(FeedItemSQL.URI_FEED_ITEMS, values,
                FeedItemSQL.COL_TAG + " IS ?", Util.ToStringArray(tag));
    }

    /**
     * Fetch the latest timestamp used in syncing
     *
     * @param context
     * @return timestamp or null if none exists
     */
    public static String GetLatestTimestamp(final Context context) {
        String result = null;
        // Adding distinct manually works atleast for single columns
        Cursor c = context.getContentResolver().query(FeedSQL.URI_FEEDS,
                new String[]{"DISTINCT " + FeedSQL.COL_TIMESTAMP},
                FeedSQL.COL_TIMESTAMP +
                " IS NOT NULL AND " +
                FeedSQL.COL_TIMESTAMP +
                " IS NOT ''", null, FeedSQL.COL_TIMESTAMP + " DESC");

        try {
            if (c.moveToNext()) {
                result = c.getString(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
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
            case PendingNetworkSQL.ITEMCODE:
                table = PendingNetworkSQL.TABLE_NAME;
                selection = Util.WHEREIDIS;
                selectionArgs = Util.ToStringArray(uri.getLastPathSegment());
                break;
            case PendingNetworkSQL.URICODE:
                table = PendingNetworkSQL.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        result = DatabaseHandler.getInstance(getContext()).getReadableDatabase()
                .query(table, projection, selection, selectionArgs, null, null,
                        sortOrder, null);

        // Make sure you don't override another uri here
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
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
                notifyUris = new Uri[]{FeedSQL.URI_FEEDS,
                        FeedSQL.URI_FEEDSWITHCOUNTS,
                        FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case FeedItemSQL.URICODE:
                table = FeedItemSQL.TABLE_NAME;
                result = FeedItemSQL.URI_FEED_ITEMS;
                notifyUris = new Uri[]{FeedItemSQL.URI_FEED_ITEMS,
                        FeedSQL.URI_FEEDSWITHCOUNTS,
                        FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case PendingNetworkSQL.URICODE:
                table = PendingNetworkSQL.TABLE_NAME;
                result = PendingNetworkSQL.URI;
                notifyUris = null;
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        long id =
                DatabaseHandler.getInstance(getContext()).getWritableDatabase()
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
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = 0;
        Uri[] notifyUris = null;

        switch (sURIMatcher.match(uri)) {
            case FeedSQL.ITEMCODE:
                result += delete(FeedSQL.URI_FEEDS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.getLastPathSegment()));
                notifyUris = new Uri[]{FeedSQL.URI_FEEDS,
                        FeedItemSQL.URI_FEED_ITEMS};
                break;
            case FeedSQL.URICODE:
                result += DatabaseHandler.getInstance(getContext())
                        .getWritableDatabase()
                        .delete(FeedSQL.TABLE_NAME, selection, selectionArgs);
                notifyUris = new Uri[]{FeedSQL.URI_FEEDS,
                        FeedItemSQL.URI_FEED_ITEMS};
                break;
            case FeedItemSQL.ITEMCODE:
                result += delete(FeedItemSQL.URI_FEED_ITEMS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.getLastPathSegment()));
                notifyUris = new Uri[]{FeedItemSQL.URI_FEED_ITEMS};
                break;
            case FeedItemSQL.URICODE:
                result += DatabaseHandler.getInstance(getContext())
                        .getWritableDatabase()
                        .delete(FeedItemSQL.TABLE_NAME, selection,
                                selectionArgs);
                notifyUris = new Uri[]{FeedItemSQL.URI_FEED_ITEMS};
                break;
            case PendingNetworkSQL.ITEMCODE:
                result += delete(PendingNetworkSQL.URI, Util.WHEREIDIS,
                        Util.ToStringArray(uri.getLastPathSegment()));
                break;
            case PendingNetworkSQL.URICODE:
                result += DatabaseHandler.getInstance(getContext())
                        .getWritableDatabase()
                        .delete(PendingNetworkSQL.TABLE_NAME, selection,
                                selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        if (notifyUris != null) {
            notifyChange(notifyUris);
        }
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
                notifyUris = new Uri[]{FeedSQL.URI_FEEDS,
                        FeedItemSQL.URI_FEED_ITEMS,
                        FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case FeedSQL.URICODE:
                table = FeedSQL.TABLE_NAME;
                notifyUris = new Uri[]{FeedSQL.URI_FEEDS,
                        FeedItemSQL.URI_FEED_ITEMS,
                        FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case FeedItemSQL.ITEMCODE:
                table = FeedItemSQL.TABLE_NAME;
                selection = Util.WHEREIDIS;
                selectionArgs = Util.ToStringArray(uri.getLastPathSegment());
                notifyUris = new Uri[]{FeedItemSQL.URI_FEED_ITEMS,
                        FeedSQL.URI_FEEDSWITHCOUNTS,
                        FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case FeedItemSQL.URICODE:
                table = FeedItemSQL.TABLE_NAME;
                notifyUris = new Uri[]{FeedItemSQL.URI_FEED_ITEMS,
                        FeedSQL.URI_FEEDSWITHCOUNTS,
                        FeedSQL.URI_TAGSWITHCOUNTS};
                break;
            case PendingNetworkSQL.ITEMCODE:
                table = PendingNetworkSQL.TABLE_NAME;
                selection = Util.WHEREIDIS;
                selectionArgs = Util.ToStringArray(uri.getLastPathSegment());
                notifyUris = null;
                break;
            case PendingNetworkSQL.URICODE:
                table = PendingNetworkSQL.TABLE_NAME;
                notifyUris = null;
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        result = DatabaseHandler.getInstance(getContext()).getWritableDatabase()
                .update(table, values, selection, selectionArgs);

        if (result > 0 && notifyUris != null) {
            notifyChange(notifyUris);
        }

        return result;
    }

    /**
     * Apply the given set of {@link android.content.ContentProviderOperation},
     * executing inside
     * a {@link android.database.sqlite.SQLiteDatabase} transaction. All changes
     * will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(
            @NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db =
                DatabaseHandler.getInstance(getContext()).getWritableDatabase();
        // UI will explode if we don't block notifications
        setShouldNotify(false);
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results =
                    new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
            // Enable them again
            setShouldNotify(true);
        }
    }

    public static void setShouldNotify(final boolean b) {
        sShouldNotify = b;
    }

    /**
     * Notify that a change has happened on each specified uri
     *
     * @param uris
     */
    private void notifyChange(Uri... uris) {
        if (!sShouldNotify) {
            return;
        }
        for (Uri uri : uris) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }
    }

    /**
     * Request a manual synchronization. Also configures automatic syncing.
     * @param context
     */
    public static void RequestSync(final Context context) {
        final Account account = AuthHelper.getSavedAccount(context);
        if (account == null) {
            // Can't do shit without an account
            return;
        }
        // Enable syncing
        ContentResolver
                .setIsSyncable(account, RssContentProvider.AUTHORITY, 1);
        // Set sync automatic
        ContentResolver.setSyncAutomatically(account,
                RssContentProvider.AUTHORITY, true);
        // Once per hour: mins * secs
        ContentResolver.addPeriodicSync(account,
                RssContentProvider.AUTHORITY,
                Bundle.EMPTY,
                60L * 60L);
        // And sync manually NOW
        final Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account,
                RssContentProvider.AUTHORITY, settingsBundle);
    }

    public static void SetNotify(Context context, long id, boolean on) {
        ContentValues values = new ContentValues();
        if (on) {
            // First mark all existing as notified so we don't spam
            values.put(FeedItemSQL.COL_NOTIFIED, 1);
            context.getContentResolver()
                    .update(FeedItemSQL.URI_FEED_ITEMS, values,
                            FeedItemSQL.COL_FEED + " IS ? AND " + FeedItemSQL.COL_NOTIFIED + " IS 0",
                            Util.LongsToStringArray(id));
        }
        // Now toggle notifications
        values.clear();
        values.put(FeedSQL.COL_NOTIFY, on ? 1 : 0);
        context.getContentResolver()
                .update(FeedSQL.URI_FEEDS, values,
                        Util.WHEREIDIS,
                        Util.LongsToStringArray(id));
    }

    public static void SetNotify(Context context, String tag, boolean on) {
        ContentValues values = new ContentValues();
        if (on) {
            // First mark all existing as notified so we don't spam
            values.put(FeedItemSQL.COL_NOTIFIED, 1);
            context.getContentResolver()
                    .update(FeedItemSQL.URI_FEED_ITEMS, values,
                            FeedItemSQL.COL_TAG + " IS ? AND " + FeedItemSQL.COL_NOTIFIED + " IS 0",
                            Util.ToStringArray(tag));
        }
        // Now toggle notifications
        values.clear();
        values.put(FeedSQL.COL_NOTIFY, on ? 1 : 0);
        context.getContentResolver()
                .update(FeedSQL.URI_FEEDS, values,
                        FeedSQL.COL_TAG + " IS ?",
                        Util.ToStringArray(tag));
    }

    /**
     *
     * @param context
     * @param id
     * @return True if feed has notifications on, false otherwise
     */
    public static boolean GetNotify(Context context, long id) {
        boolean result = false;

        Cursor c = context.getContentResolver().query(FeedSQL.URI_FEEDS,
                Util.ToStringArray(FeedSQL.COL_NOTIFY),
                Util.WHEREIDIS, Util.LongsToStringArray(id), null);

        try {
            if (c.moveToFirst()) {
                result = (c.getInt(0) == 1);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return result;
    }
    /**
     *
     * @param context
     * @param tag
     * @return True if all items have notifications on, false otherwise
     */
    public static boolean GetNotify(Context context, String tag) {
        boolean result = false;

        Cursor c = context.getContentResolver().query(FeedSQL.URI_FEEDS,
                Util.ToStringArray("DISTINCT " + FeedSQL.COL_NOTIFY),
                FeedSQL.COL_TAG + " IS ?", Util.ToStringArray(tag), null);

        try {
            if (c.getCount() == 1 && c.moveToFirst()) {
                // Conclusive results
                result = (c.getInt(0) == 1);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return result;
    }
}
