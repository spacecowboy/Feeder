package com.nononsenseapps.feeder.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Utilities and constants related to app preferences.
 */
public class PrefUtils {
    /**
     * Boolean indicating whether we performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";

    /**
     * Boolean indicating if night mode should be engaged.
     */
    public static final String PREF_NIGHT_MODE = "pref_night_mode";

    /**
     * Boolean indicating if only unread items should be shown
     */
    public static final String PREF_SHOW_ONLY_UNREAD = "pref_show_only_unread";

    /**
     * These indicate which fragment to open by default
     */
    public static final String PREF_LAST_FEED_TAG = "pref_last_feed_tag";
    public static final String PREF_LAST_FEED_ID = "pref_last_feed_id";

    /**
     * Sync settings
     */
    public static final String PREF_SYNC_ONLY_CHARGING = "pref_sync_only_charging";
    public static final String PREF_SYNC_HOTSPOTS = "pref_sync_hotspots";
    public static final String PREF_SYNC_ONLY_WIFI = "pref_sync_only_wifi";

    /**
     * Image settings
     */
    public static final String PREF_IMG_ONLY_WIFI = "pref_img_only_wifi";

    public static boolean shouldLoadImagesOnlyOnWIfi(final Context context) {
        return sp(context).getBoolean(PREF_IMG_ONLY_WIFI, false);
    }

    public static boolean shouldSyncOnlyOnWIfi(final Context context) {
        return sp(context).getBoolean(PREF_SYNC_ONLY_WIFI, false);
    }

    public static boolean shouldSyncOnlyWhenCharging(final Context context) {
        return sp(context).getBoolean(PREF_SYNC_ONLY_CHARGING, false);
    }

    public static boolean shouldSyncOnHotSpots(final Context context) {
        return sp(context).getBoolean(PREF_SYNC_HOTSPOTS, false);
    }

    /**
     * A shorthand method
     */
    private static SharedPreferences sp(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isWelcomeDone(final Context context) {
        return sp(context).getBoolean(PREF_WELCOME_DONE, false);
    }

    public static void markWelcomeDone(final Context context) {
        sp(context).edit().putBoolean(PREF_WELCOME_DONE, true).apply();
    }

    public static boolean isNightMode(final Context context) {
        return sp(context).getBoolean(PREF_NIGHT_MODE, false);
    }

    public static void setNightMode(final Context context, final boolean value) {
        sp(context).edit().putBoolean(PREF_NIGHT_MODE, value).apply();
    }

    public static boolean isShowOnlyUnread(final Context context) {
        return sp(context).getBoolean(PREF_SHOW_ONLY_UNREAD, true);
    }

    public static void setPrefShowOnlyUnread(final Context context,
                                             final boolean value) {
        sp(context).edit().putBoolean(PREF_SHOW_ONLY_UNREAD, value).apply();
    }

    /**
     * Remember the open fragment, either by id, or tag if id < 1.
     * Clears the other one.
     *
     * @param context
     * @param id
     * @param tag
     */
    public static void setLastOpenFeed(final Context context, final long id,
                                       final String tag) {
        sp(context).edit().putLong(PREF_LAST_FEED_ID, id)
                .putString(PREF_LAST_FEED_TAG, tag).apply();
    }

    /**
     * Get which feed tag was last open. Check id first.
     *
     * @param context
     * @return last open tag, or null
     */
    public static String getLastOpenFeedTag(final Context context) {
        return sp(context).getString(PREF_LAST_FEED_TAG, null);
    }

    /**
     * Get which feed id was last open. If -1, check tag.
     *
     * @param context
     * @return last open id, or -1
     */
    public static long getLastOpenFeedId(final Context context) {
        return sp(context).getLong(PREF_LAST_FEED_ID, -1);
    }


    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sp(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sp(context).unregisterOnSharedPreferenceChangeListener(listener);
    }
}
