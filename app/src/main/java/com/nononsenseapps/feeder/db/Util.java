package com.nononsenseapps.feeder.db;

/**
 * Various database utility methods
 */
public class Util {

    // Does not include "where"
    public static final String WHEREIDIS = "_ID IS ?";

    /**
     * Useful for content provider operations to insert Longs and such.
     * @param vals
     * @return
     */
    public static String[] LongsToStringArray(long... vals) {
        String[] arr = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            arr[i] = Long.toString(vals[i]);
        }
        return arr;
    }

    /**
     * Just returns an array of the provided strings
     * @param strings
     * @return
     */
    public static String[] ToStringArray(String... strings) {
        return strings;
    }
}
