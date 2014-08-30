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
     * Boolean indicating if only unread items should be shown
     */
    public static final String PREF_SHOW_ONLY_UNREAD = "pref_show_only_unread";

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

    public static boolean isShowOnlyUnread(final Context context) {
        return sp(context).getBoolean(PREF_SHOW_ONLY_UNREAD, true);
    }

    public static void setPrefShowOnlyUnread(final Context context,
            final boolean value) {
        sp(context).edit().putBoolean(PREF_SHOW_ONLY_UNREAD, value).apply();
    }


    public static void registerOnSharedPreferenceChangeListener(final Context context,
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sp(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unrgisterOnSharedPreferenceChangeListener(final Context context,
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sp(context).unregisterOnSharedPreferenceChangeListener(listener);
    }
}
