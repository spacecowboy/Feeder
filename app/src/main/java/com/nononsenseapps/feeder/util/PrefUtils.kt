package com.nononsenseapps.feeder.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceManager
import com.nononsenseapps.feeder.db.DATABASE_VERSION

/**
 * Boolean indicating whether we performed the (one-time) welcome flow.
 */
const val PREF_WELCOME_DONE = "pref_welcome_done"

/**
 * Boolean indicating if night mode should be engaged.
 */
const val PREF_NIGHT_MODE = "pref_night_mode"

/**
 * Boolean indicating if only unread items should be shown
 */
const val PREF_SHOW_ONLY_UNREAD = "pref_show_only_unread"

/**
 * These indicate which fragment to open by default
 */
const val PREF_LAST_FEED_TAG = "pref_last_feed_tag"
const val PREF_LAST_FEED_ID = "pref_last_feed_id"

/**
 * Sync settings
 */
const val PREF_SYNC_ONLY_CHARGING = "pref_sync_only_charging"
const val PREF_SYNC_HOTSPOTS = "pref_sync_hotspots"
const val PREF_SYNC_ONLY_WIFI = "pref_sync_only_wifi"
const val PREF_SYNC_FREQ = "pref_sync_freq"

/**
 * Image settings
 */
const val PREF_IMG_ONLY_WIFI = "pref_img_only_wifi"
const val PREF_IMG_HOTSPOTS = "pref_img_hotspots"

const val PREF_LAST_DATABASE_VERSION = "pref_last_database_version"

/**
 * Reader settings
 */
const val PREF_DEFAULT_OPEN_ITEM_WITH = "pref_default_open_item_with"
const val PREF_OPEN_EMPTY_ITEM_WITH = "pref_open_empty_item_with"
const val PREF_OPEN_LINKS_WITH = "pref_open_links_with"

const val PREF_VAL_OPEN_WITH_READER = "0"
const val PREF_VAL_OPEN_WITH_WEBVIEW = "1"
const val PREF_VAL_OPEN_WITH_BROWSER = "2"

/**
 * Utilities and constants related to app preferences.
 */
object PrefUtils {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value. Handles ringtone preferences and list preferences
     * specially.
     */
    val summaryUpdater: Preference.OnPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, value ->
        val stringValue = value.toString()
        if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            val index = preference.findIndexOfValue(stringValue)
            // Set the summary to reflect the new value.
            preference.setSummary(if (index >= 0) preference.entries[index] else null)
        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.summary = stringValue
        }
        true
    }

    fun isFirstBootAfterDatabaseUpgrade(context: Context): Boolean =
            sp(context).getInt(PREF_LAST_DATABASE_VERSION, -1) != DATABASE_VERSION

    fun markFirstBootAfterDatabaseUpgradeDone(context: Context) =
            sp(context).edit().putInt(PREF_LAST_DATABASE_VERSION, DATABASE_VERSION).apply()

    fun shouldLoadImagesOnlyOnWIfi(context: Context): Boolean = sp(context).getBoolean(PREF_IMG_ONLY_WIFI, false)

    fun shouldLoadImagesOnHotSpots(context: Context): Boolean = sp(context).getBoolean(PREF_IMG_HOTSPOTS, false)

    fun shouldSyncOnlyOnWIfi(context: Context): Boolean = sp(context).getBoolean(PREF_SYNC_ONLY_WIFI, false)

    fun shouldSyncOnlyWhenCharging(context: Context): Boolean = sp(context).getBoolean(PREF_SYNC_ONLY_CHARGING, false)

    fun shouldSyncOnHotSpots(context: Context): Boolean = sp(context).getBoolean(PREF_SYNC_HOTSPOTS, false)

    /**
     * @return true if automatic syncing is enabled, false otherwise
     */
    fun shouldSync(context: Context): Boolean = synchronizationFrequency(context) > 0

    /**
     * @return number of minutes between syncs, or zero if none should be performed
     */
    fun synchronizationFrequency(context: Context): Long =
            sp(context).getString(PREF_SYNC_FREQ, "60").toLong()

    fun shouldOpenItemWith(context: Context): String =
            sp(context).getString(PREF_DEFAULT_OPEN_ITEM_WITH, PREF_VAL_OPEN_WITH_READER)

    fun shouldOpenEmptyItemWith(context: Context): String =
            sp(context).getString(PREF_OPEN_EMPTY_ITEM_WITH, PREF_VAL_OPEN_WITH_BROWSER)

    fun shouldOpenLinkWith(context: Context): String =
            sp(context).getString(PREF_OPEN_LINKS_WITH, PREF_VAL_OPEN_WITH_BROWSER)

    /**
     * A shorthand method
     */
    private fun sp(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isWelcomeDone(context: Context): Boolean = sp(context).getBoolean(PREF_WELCOME_DONE, false)

    fun markWelcomeDone(context: Context) {
        sp(context).edit().putBoolean(PREF_WELCOME_DONE, true).apply()
    }

    fun isNightMode(context: Context): Boolean = sp(context).getBoolean(PREF_NIGHT_MODE, false)

    fun setNightMode(context: Context, value: Boolean) {
        sp(context).edit().putBoolean(PREF_NIGHT_MODE, value).apply()
    }

    fun isShowOnlyUnread(context: Context): Boolean = sp(context).getBoolean(PREF_SHOW_ONLY_UNREAD, true)

    fun setPrefShowOnlyUnread(context: Context,
                              value: Boolean) {
        sp(context).edit().putBoolean(PREF_SHOW_ONLY_UNREAD, value).apply()
    }

    /**
     * Remember the open fragment, either by id, or tag if id < 1.
     * Clears the other one.
     *
     * @param context
     * @param id
     * @param tag
     */
    fun setLastOpenFeed(context: Context, id: Long,
                        tag: String?) {
        sp(context).edit().putLong(PREF_LAST_FEED_ID, id)
                .putString(PREF_LAST_FEED_TAG, tag).apply()
    }

    /**
     * Get which feed tag was last open. Check id first.
     *
     * @param context
     * @return last open tag, or null
     */
    fun getLastOpenFeedTag(context: Context): String? = sp(context).getString(PREF_LAST_FEED_TAG, null)

    /**
     * Get which feed id was last open. If -1, check tag.
     *
     * @param context
     * @return last open id, or -1
     */
    fun getLastOpenFeedId(context: Context): Long = sp(context).getLong(PREF_LAST_FEED_ID, -1)


    fun registerOnSharedPreferenceChangeListener(context: Context,
                                                 listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sp(context).registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(context: Context,
                                                   listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sp(context).unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun shouldLoadImages(context: Context): Boolean {
        return if (SystemUtils.currentlyOnWifi(context)) {
            !SystemUtils.currentlyMetered(context) || shouldLoadImagesOnHotSpots(context)
        } else !shouldLoadImagesOnlyOnWIfi(context)
    }
}
