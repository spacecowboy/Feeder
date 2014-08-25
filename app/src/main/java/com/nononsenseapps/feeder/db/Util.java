package com.nononsenseapps.feeder.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Various database utility methods
 */
public class Util {

    // Does not include "where"
    public static final String WHEREIDIS = "_ID IS ?";

    /**
     * Useful for content provider operations to insert Longs and such.
     *
     * @param vals
     * @return
     */
    public static String[] LongsToStringArray(Long... vals) {
        String[] arr = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] == null) {
                arr[i] = "null";
            } else {
                arr[i] = Long.toString(vals[i]);
            }
        }
        return arr;
    }

    /**
     * Just returns an array of the provided strings
     *
     * @param strings
     * @return
     */
    public static String[] ToStringArray(String... strings) {
        String[] arr = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] == null) {
                arr[i] = "null";
            } else {
                arr[i] = strings[i];
            }
        }
        return arr;
    }

    /**
     * Set a value in ContentValues which might be null,
     * in which case you must use PutNull() instead.
     *
     * @param values
     * @param key
     * @param value
     */
    public static void PutNullable(ContentValues values, String key,
            String value) {
        if (value == null) {
            values.putNull(key);
        } else {
            values.put(key, value);
        }
    }

    /**
     * Set a value in ContentValues which might be null,
     * in which case you must use PutNull() instead.
     *
     * @param values
     * @param key
     * @param value
     */
    public static void PutNullable(ContentValues values, String key,
            Long value) {
        if (value == null) {
            values.putNull(key);
        } else {
            values.put(key, value);
        }
    }

    /**
     * If the item has an id, will update, else insert.
     * @param db in writable mode
     * @param itemSQL
     */
    public static void SaveOrUpdate(final SQLiteDatabase db,
            final FeedItemSQL itemSQL) {
        if (itemSQL.id > 0) {
            db.update(FeedItemSQL.TABLE_NAME, itemSQL.getContent(),
                    WHEREIDIS, LongsToStringArray(itemSQL.id));
        } else {
            db.insert(FeedItemSQL.TABLE_NAME, null, itemSQL.getContent());
        }
    }
}
