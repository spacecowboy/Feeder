package com.nononsenseapps.feeder.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET

/**
 * Boolean indicating whether we performed the (one-time) welcome flow.
 */
const val PREF_WELCOME_DONE = "pref_welcome_done"

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
 * Theme settings
 */
const val PREF_THEME = "pref_theme"

/**
 * Sync settings
 */
const val PREF_SYNC_ONLY_CHARGING = "pref_sync_only_charging"
const val PREF_SYNC_ONLY_WIFI = "pref_sync_only_wifi"
const val PREF_SYNC_FREQ = "pref_sync_freq"
const val PREF_SYNC_ON_RESUME = "pref_sync_on_resume"

/**
 * Image settings
 */
const val PREF_IMG_ONLY_WIFI = "pref_img_only_wifi"

/**
 * Reader settings
 */
const val PREF_DEFAULT_OPEN_ITEM_WITH = "pref_default_open_item_with"
const val PREF_OPEN_LINKS_WITH = "pref_open_links_with"

const val PREF_VAL_OPEN_WITH_READER = "0"
const val PREF_VAL_OPEN_WITH_WEBVIEW = "1"
const val PREF_VAL_OPEN_WITH_BROWSER = "2"

/**
 * Database settings
 */
const val PREF_MAX_ITEM_COUNT_PER_FEED = "pref_max_item_count_per_feed"

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

    fun shouldLoadImagesOnlyOnWIfi(context: Context): Boolean = sp(context).getBoolean(PREF_IMG_ONLY_WIFI, false)

    fun shouldSyncOnlyOnWIfi(context: Context): Boolean = sp(context).getBoolean(PREF_SYNC_ONLY_WIFI, false)

    fun shouldSyncOnResume(context: Context): Boolean = sp(context).getBoolean(PREF_SYNC_ON_RESUME, false)

    fun shouldSyncOnlyWhenCharging(context: Context): Boolean = sp(context).getBoolean(PREF_SYNC_ONLY_CHARGING, false)

    fun maximumItemCountPerFeed(context: Context): Int =
            sp(context).getStringNonNull(PREF_MAX_ITEM_COUNT_PER_FEED, "100").toInt()

    /**
     * @return true if automatic syncing is enabled, false otherwise
     */
    fun shouldSync(context: Context): Boolean = synchronizationFrequency(context) > 0

    /**
     * @return number of minutes between syncs, or zero if none should be performed
     */
    fun synchronizationFrequency(context: Context): Long =
            sp(context).getStringNonNull(PREF_SYNC_FREQ, "60").toLong()

    fun setSynchronizationFrequency(context: Context, value: Long) =
            sp(context).edit().putString(PREF_SYNC_FREQ, "$value").apply()

    fun shouldOpenItemWith(context: Context): String =
            sp(context).getStringNonNull(PREF_DEFAULT_OPEN_ITEM_WITH, PREF_VAL_OPEN_WITH_READER)

    fun shouldOpenLinkWith(context: Context): String =
            sp(context).getStringNonNull(PREF_OPEN_LINKS_WITH, PREF_VAL_OPEN_WITH_BROWSER)

    /**
     * A shorthand method
     */
    private fun sp(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isWelcomeDone(context: Context): Boolean = sp(context).getBoolean(PREF_WELCOME_DONE, false)

    fun markWelcomeDone(context: Context) {
        sp(context).edit().putBoolean(PREF_WELCOME_DONE, true).apply()
    }

    fun isNightMode(context: Context): Boolean =
            when (sp(context).getString(PREF_THEME, context.getString(R.string.pref_theme_value_default))) {
                context.getString(R.string.pref_theme_value_night) -> true
                else -> false
            }

    fun getNightMode(context: Context): Int =
            when (sp(context).getString(PREF_THEME, context.getString(R.string.pref_theme_value_default))) {
                context.getString(R.string.pref_theme_value_day) -> AppCompatDelegate.MODE_NIGHT_NO
                context.getString(R.string.pref_theme_value_night) -> AppCompatDelegate.MODE_NIGHT_YES
                context.getString(R.string.pref_theme_value_auto) -> AppCompatDelegate.MODE_NIGHT_AUTO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

    internal fun setNightMode(context: Context, nightMode: Boolean) =
            sp(context).edit().putString(PREF_THEME,
                    when (nightMode) {
                        true -> context.getString(R.string.pref_theme_value_night)
                        false -> context.getString(R.string.pref_theme_value_day)
                    }
            ).apply()

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

    fun clearLastOpenFeed(context: Context) {
        sp(context).edit().remove(PREF_LAST_FEED_ID)
                .remove(PREF_LAST_FEED_TAG).apply()
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
    fun getLastOpenFeedId(context: Context): Long = sp(context).getLong(PREF_LAST_FEED_ID, ID_UNSET)


    fun registerOnSharedPreferenceChangeListener(context: Context,
                                                 listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sp(context).registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(context: Context,
                                                   listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sp(context).unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun shouldLoadImages(context: Context): Boolean {
        return when {
            shouldLoadImagesOnlyOnWIfi(context) -> currentlyUnmetered(context)
            else -> true
        }
    }
}

fun SharedPreferences.getStringNonNull(key: String, defaultValue: String): String =
        getString(key, defaultValue) ?: defaultValue
