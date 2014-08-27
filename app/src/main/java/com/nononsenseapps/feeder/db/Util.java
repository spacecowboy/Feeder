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
     * Returns a String which tells the database to ignore case when sorting.
     * @param col the column to sort on
     * @return "{col} COLLATE NOCASE"
     */
    public static String SortAlphabeticNoCase(final String col) {
        return col + " COLLATE NOCASE";
    }

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

    public static String arrayToCommaString(final long... array) {
        StringBuilder result = new StringBuilder();
        for (final long val : array) {
            final String txt = Long.toString(val);
            if (result.length() > 0) result.append(",");
            result.append(txt);
        }
        return result.toString();
    }

    public static String arrayToCommaString(final String... array) {
        return arrayToCommaString("", array);
    }

    /**
     * Example (prefix=t.): [] -> "" [a] -> "t.a" [a, b] -> "t.a,t.b"
     */
    public static String arrayToCommaString(final String prefix,
            final String[] array) {
        return arrayToCommaString(prefix, array, "");
    }

    /**
     * Example (prefix=t., suffix=.45): [] -> "" [a] -> "t.a.45" [a, b] ->
     * "t.a.45,t.b.45"
     *
     * In addition, the txt itself can be referenced using %1$s in either prefix
     * or suffix. The prefix can be referenced as %2$s in suffix, and
     * vice-versa.
     *
     * So the following is valid:
     *
     * (prefix='t.', suffix=' AS %2$s%1$s')
     *
     * [listId] -> t.listId AS t.listId
     */
    protected static String arrayToCommaString(final String pfx,
            final String[] array, final String sfx) {
        StringBuilder result = new StringBuilder();
        for (final String txt : array) {
            if (result.length() > 0) result.append(",");
            result.append(String.format(pfx, txt, sfx));
            result.append(txt);
            result.append(String.format(sfx, txt, pfx));
        }
        return result.toString();
    }
}
