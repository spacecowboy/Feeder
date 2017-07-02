package com.nononsenseapps.feeder.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import static com.nononsenseapps.feeder.db.UriKt.URI_FEEDITEMS;

/**
 * Various database utility methods
 */
public class Util {

    // Does not include "where"
    public static final String WHEREIDIS = "_ID IS ?";

    public static String WhereIs(final String col) {
        return String.format("%s IS ?", col);
    }

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
                throw new NullPointerException("Can't have null arguments " +
                                               "here, " +
                                               "since they can't be combined " +
                                               "with '?' in SQL.");
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
                throw new NullPointerException("Can't have null arguments " +
                                               "here, " +
                                               "since they can't be combined " +
                                               "with '?' in SQL.");
            } else {
                arr[i] = strings[i];
            }
        }
        return arr;
    }

    public static int[] ToIntArray(final int... numbers) {
        return numbers;
    }
}
