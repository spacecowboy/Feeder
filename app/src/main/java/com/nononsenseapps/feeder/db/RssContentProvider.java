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

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import static android.content.ContentResolver.SYNC_EXTRAS_REQUIRE_CHARGING;
import static com.nononsenseapps.feeder.util.PrefUtils.PREF_SYNC_ONLY_CHARGING;

public class RssContentProvider extends ContentProvider {
    // All URIs share these parts
    public static final String AUTHORITY = "com.nononsenseapps.feeder.provider";
    public static final String SCHEME = "content://";
    // Match Uris with this
    private static final UriMatcher sURIMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TAG = "RssContentProvider";
    public static final String QUERY_PARAM_LIMIT = "QUERY_PARAM_LIMIT";
    public static final String QUERY_PARAM_SKIP = "QUERY_PARAM_SKIP";

    static {
        FeedSQL.addMatcherUris(sURIMatcher);
        FeedItemSQL.addMatcherUris(sURIMatcher);
    }

    public RssContentProvider() {
    }

    /**
     * Notify all uris that changes have happened. Should be called after any updates.
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
        notifyAllUris(context);
    }

    /**
     * Mark a feedItem as unread in the database.
     *
     * @param context
     * @param itemId
     */
    public static void MarkItemAsUnread(final Context context,
                                        final long itemId) {
        ContentValues values = new ContentValues();
        values.put(FeedItemSQL.COL_UNREAD, 1);
        context.getContentResolver()
                .update(FeedItemSQL.URI_FEED_ITEMS, values, Util.WHEREIDIS,
                        Util.LongsToStringArray(itemId));
        notifyAllUris(context);
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
        notifyAllUris(context);
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
        notifyAllUris(context);
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
        // Must use builder in order to support OFFSET as the regular parser does not allow for
        // negative numbers in the LIMIT clause.
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(table);
        String query = queryBuilder.buildQuery(projection, selection, null, null, sortOrder, null);
        query += getLimitString(uri);

        result = DatabaseHandler.getInstance(getContext()).getReadableDatabase()
                .rawQuery(query, selectionArgs);

        // Make sure you don't override another uri here
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    /**
     * Return a limit clause as " LIMIT OFFSET,LIMIT", with parameters defined in uri.
     * If not defined, the default values of -1 are used, which means no offset/return all
     * respectively.
     */
    private String getLimitString(Uri uri) {
        String offset = uri.getQueryParameter(QUERY_PARAM_SKIP);
        if (offset == null) {
            offset = "-1";
        }
        String limit = uri.getQueryParameter(QUERY_PARAM_LIMIT);
        if (limit == null) {
            limit = "-1";
        }

        return String.format(" LIMIT %s,%s", offset, limit);
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
        final String table;

        switch (sURIMatcher.match(uri)) {
            case FeedSQL.URICODE:
                table = FeedSQL.TABLE_NAME;
                result = FeedSQL.URI_FEEDS;
                break;
            case FeedItemSQL.URICODE:
                table = FeedItemSQL.TABLE_NAME;
                result = FeedItemSQL.URI_FEED_ITEMS;
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

        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = 0;

        switch (sURIMatcher.match(uri)) {
            case FeedSQL.ITEMCODE:
                result += delete(FeedSQL.URI_FEEDS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.getLastPathSegment()));
                break;
            case FeedSQL.URICODE:
                result += DatabaseHandler.getInstance(getContext())
                        .getWritableDatabase()
                        .delete(FeedSQL.TABLE_NAME, selection, selectionArgs);
                break;
            case FeedItemSQL.ITEMCODE:
                result += delete(FeedItemSQL.URI_FEED_ITEMS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.getLastPathSegment()));
                break;
            case FeedItemSQL.URICODE:
                result += DatabaseHandler.getInstance(getContext())
                        .getWritableDatabase()
                        .delete(FeedItemSQL.TABLE_NAME, selection,
                                selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final String table;
        int result = 0;

        switch (sURIMatcher.match(uri)) {
            case FeedSQL.ITEMCODE:
                table = FeedSQL.TABLE_NAME;
                selection = Util.WHEREIDIS;
                selectionArgs = Util.ToStringArray(uri.getLastPathSegment());
                break;
            case FeedSQL.URICODE:
                table = FeedSQL.TABLE_NAME;
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

        result = DatabaseHandler.getInstance(getContext()).getWritableDatabase()
                .update(table, values, selection, selectionArgs);

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
        }
    }

    /**
     * Request a manual synchronization of all feeds.
     */
    public static void RequestSync() {
        RequestSync(-1);
    }

    /**
     * Request a manual synchronization of one feed.
     */
    public static void RequestSync(long id) {
        Account account = AccountService.Account();
        final Bundle settingsBundle = new Bundle();
        if (id > 0) {
            settingsBundle.putLong(FeedSQL.COL_ID, id);
        }
        // sync manually NOW
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, RssContentProvider.AUTHORITY, settingsBundle);
    }

    /**
     * Request a manual synchronization of feeds with a specific tag.
     */
    public static void RequestSync(@NonNull String tag) {
        Account account = AccountService.Account();
        final Bundle settingsBundle = new Bundle();
        if (!tag.isEmpty()) {
            settingsBundle.putString(FeedSQL.COL_TAG, tag);
        }
        // sync manually NOW
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, RssContentProvider.AUTHORITY, settingsBundle);
    }

    /**
     * Adds the account and enables automatic syncing
     */
    public static void SetupSync(final Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account account = AccountService.Account();

        if (accountManager.addAccountExplicitly(account, null, null)) {
            // New account was added so...
            // Enable syncing
            ContentResolver.setIsSyncable(account, RssContentProvider.AUTHORITY, 1);
            // Set sync automatic
            ContentResolver.setSyncAutomatically(account, RssContentProvider.AUTHORITY, true);
        }
        Bundle extras = new Bundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            extras.putBoolean(SYNC_EXTRAS_REQUIRE_CHARGING,
                    PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SYNC_ONLY_CHARGING, false));
        }
        // Once per hour: mins * secs
        ContentResolver.addPeriodicSync(account,
                RssContentProvider.AUTHORITY,
                extras,
                60L * 60L);

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

        notifyAllUris(context);
    }

    /**
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
