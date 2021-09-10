package com.nononsenseapps.feeder.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.theme.FeederDarkColorPalette
import com.nononsenseapps.feeder.ui.compose.theme.FeederLightColorPalette
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

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
 * Sort settings
 */
const val PREF_SORT = "pref_sort"

/**
 * Floating action button settings
 */
const val PREF_SHOW_FAB = "pref_show_fab"

/**
 * Sync settings
 */
const val PREF_SYNC_ONLY_CHARGING = "pref_sync_only_charging"
const val PREF_SYNC_ONLY_WIFI = "pref_sync_only_wifi"
const val PREF_SYNC_FREQ = "pref_sync_freq"
const val PREF_SYNC_ON_RESUME = "pref_sync_on_resume"
const val PREF_BATTERY_OPTIMIZATION = "pref_battery_optimization"

/**
 * Image settings
 */
const val PREF_IMG_ONLY_WIFI = "pref_img_only_wifi"
const val PREF_IMG_SHOW_THUMBNAILS = "pref_img_show_thumbnails"

/**
 * Reader settings
 */
const val PREF_DEFAULT_OPEN_ITEM_WITH = "pref_default_open_item_with"
const val PREF_OPEN_LINKS_WITH = "pref_open_links_with"
const val PREF_PRELOAD_CUSTOM_TAB = "pref_preload_custom_tab"

const val PREF_VAL_OPEN_WITH_READER = "0"
const val PREF_VAL_OPEN_WITH_WEBVIEW = "1"
const val PREF_VAL_OPEN_WITH_BROWSER = "2"
const val PREF_VAL_OPEN_WITH_CUSTOM_TAB = "3"

enum class PrefValOpenWith {
    OPEN_WITH_DEFAULT,
    OPEN_WITH_READER,
    OPEN_WITH_CUSTOM_TAB,
    OPEN_WITH_BROWSER
}

const val PREF_JAVASCRIPT_ENABLED = "pref_javascript_enabled"

/**
 * Database settings
 */
const val PREF_MAX_ITEM_COUNT_PER_FEED = "pref_max_item_count_per_feed"

fun SharedPreferences.getStringNonNull(key: String, defaultValue: String): String =
    getString(key, defaultValue) ?: defaultValue

class Prefs(override val di: DI) : DIAware {
    private val sp: SharedPreferences by instance()
    private val app: Application by instance()

    var javascriptEnabled: Boolean
        get() = sp.getBoolean(PREF_JAVASCRIPT_ENABLED, true)
        set(value) = sp.edit().putBoolean(PREF_JAVASCRIPT_ENABLED, value).apply()

    var onlyLoadImagesOnWIfi: Boolean
        get() = sp.getBoolean(PREF_IMG_ONLY_WIFI, false)
        set(value) = sp.edit().putBoolean(PREF_IMG_ONLY_WIFI, value).apply()

    var showThumbnails: Boolean
        get() = sp.getBoolean(PREF_IMG_SHOW_THUMBNAILS, true)
        set(value) = sp.edit().putBoolean(PREF_IMG_SHOW_THUMBNAILS, value).apply()

    var onlySyncOnWifi: Boolean
        get() = sp.getBoolean(PREF_SYNC_ONLY_WIFI, false)
        set(value) = sp.edit().putBoolean(PREF_SYNC_ONLY_WIFI, value).apply()

    var syncOnResume: Boolean
        get() = sp.getBoolean(PREF_SYNC_ON_RESUME, false)
        set(value) = sp.edit().putBoolean(PREF_SYNC_ON_RESUME, value).apply()

    var onlySyncWhileCharging: Boolean
        get() = sp.getBoolean(PREF_SYNC_ONLY_CHARGING, false)
        set(value) = sp.edit().putBoolean(PREF_SYNC_ONLY_CHARGING, value).apply()

    var maximumCountPerFeed: Int
        get() = sp.getStringNonNull(PREF_MAX_ITEM_COUNT_PER_FEED, "100").toInt()
        set(value) = sp.edit().putString(PREF_MAX_ITEM_COUNT_PER_FEED, "$value").apply()

    /**
     * @return true if automatic syncing is enabled, false otherwise
     */
    fun shouldSync(): Boolean = synchronizationFrequency > 0

    /**
     * @return number of minutes between syncs, or zero if none should be performed
     */
    var synchronizationFrequency: Long
        get() = sp.getStringNonNull(PREF_SYNC_FREQ, "60").toLong()
        set(value) = sp.edit().putString(PREF_SYNC_FREQ, "$value").apply()

    var openItemsWith: String
        get() = sp.getStringNonNull(PREF_DEFAULT_OPEN_ITEM_WITH, PREF_VAL_OPEN_WITH_READER)
        set(value) = sp.edit().putString(PREF_DEFAULT_OPEN_ITEM_WITH, value).apply()

    var openLinksWith: String
        get() = sp.getStringNonNull(PREF_OPEN_LINKS_WITH, PREF_VAL_OPEN_WITH_CUSTOM_TAB)
        set(value) = sp.edit().putString(PREF_OPEN_LINKS_WITH, value).apply()

    var preloadCustomTab: Boolean
        get() = sp.getBoolean(PREF_PRELOAD_CUSTOM_TAB, false)
        set(value) = sp.edit().putBoolean(PREF_PRELOAD_CUSTOM_TAB, value).apply()

    var welcomeDone: Boolean
        get() = sp.getBoolean(PREF_WELCOME_DONE, false)
        set(value) = sp.edit().putBoolean(PREF_WELCOME_DONE, value).apply()

    var currentTheme: ThemeOptions
        get() = ThemeOptions.fromString(
            app,
            sp.getString(PREF_THEME, app.getString(R.string.pref_theme_value_default))
        )
        set(value) = sp.edit().putString(
            PREF_THEME,
            value.toPrefValue()
        ).apply()

    var currentItemOpener: ItemOpener
        get() = when (openItemsWith) {
            PREF_VAL_OPEN_WITH_BROWSER -> ItemOpener.DEFAULT_BROWSER
            PREF_VAL_OPEN_WITH_WEBVIEW,
            PREF_VAL_OPEN_WITH_CUSTOM_TAB -> ItemOpener.CUSTOM_TAB
            else -> ItemOpener.READER
        }
        set(value) {
            openItemsWith = when (value) {
                ItemOpener.READER -> PREF_VAL_OPEN_WITH_READER
                ItemOpener.CUSTOM_TAB -> PREF_VAL_OPEN_WITH_CUSTOM_TAB
                ItemOpener.DEFAULT_BROWSER -> PREF_VAL_OPEN_WITH_BROWSER
            }
        }

    var currentLinkOpener: LinkOpener
        get() = when (openLinksWith) {
            PREF_VAL_OPEN_WITH_BROWSER -> LinkOpener.DEFAULT_BROWSER
            else -> LinkOpener.CUSTOM_TAB
        }
        set(value) {
            when (value) {
                LinkOpener.CUSTOM_TAB -> openLinksWith = PREF_VAL_OPEN_WITH_CUSTOM_TAB
                LinkOpener.DEFAULT_BROWSER -> openLinksWith = PREF_VAL_OPEN_WITH_BROWSER
            }
        }

    var currentSyncFrequency: SyncFrequency
        get() =
            SyncFrequency.values()
                .firstOrNull {
                    it.minutes == synchronizationFrequency
                }
                ?: SyncFrequency.MANUAL
        set(value) {
            synchronizationFrequency = value.minutes
        }

    var isNightMode: Boolean
        get() = when (currentTheme) {
            ThemeOptions.NIGHT -> true
            ThemeOptions.SYSTEM -> app.isSystemThemeNight
            else -> false
        }
        set(value) {
            currentTheme = when (value) {
                true -> ThemeOptions.NIGHT
                false -> ThemeOptions.DAY
            }
        }

    var currentSorting: SortingOptions
        get() = when (sp.getString(PREF_SORT, app.getString(R.string.pref_sort_value_default))) {
            app.getString(R.string.pref_sort_value_oldest_first) -> SortingOptions.OLDEST_FIRST
            else -> SortingOptions.NEWEST_FIRST
        }
        set(value) = sp.edit().putString(
            PREF_SORT,
            when (value) {
                SortingOptions.NEWEST_FIRST -> app.getString(R.string.pref_sort_value_newest_first)
                SortingOptions.OLDEST_FIRST -> app.getString(R.string.pref_sort_value_oldest_first)
            }
        ).apply()

    var isNewestFirst: Boolean
        get() = when (currentSorting) {
            SortingOptions.NEWEST_FIRST -> true
            else -> false
        }
        set(value) {
            currentSorting = when (value) {
                true -> SortingOptions.NEWEST_FIRST
                false -> SortingOptions.OLDEST_FIRST
            }
        }

    var show_fab: Boolean
        get() = sp.getBoolean(PREF_SHOW_FAB, true)
        set(value) = sp.edit().putBoolean(PREF_SHOW_FAB, value).apply()

    var showOnlyUnread: Boolean
        get() = sp.getBoolean(PREF_SHOW_ONLY_UNREAD, true)
        set(value) = sp.edit().putBoolean(PREF_SHOW_ONLY_UNREAD, value).apply()

    /**
     * Remember the open fragment, either by id, or tag if id < 1.
     * Clears the other one.
     *
     * @param id
     * @param tag
     */
    fun setLastOpenFeed(id: Long, tag: String?) =
        sp.edit().putLong(PREF_LAST_FEED_ID, id)
            .putString(PREF_LAST_FEED_TAG, tag)
            .apply()

    /**
     * Get which feed tag was last open. Check id first.
     */
    var lastOpenFeedTag: String?
        get() = sp.getString(PREF_LAST_FEED_TAG, null)
        set(value) = sp.edit().putString(PREF_LAST_FEED_TAG, value).apply()

    /**
     * Get which feed id was last open. If -1, check tag.
     */
    var lastOpenFeedId: Long
        get() = sp.getLong(PREF_LAST_FEED_ID, ID_UNSET)
        set(value) = sp.edit().putLong(PREF_LAST_FEED_ID, value).apply()

    fun shouldLoadImages(): Boolean {
        return when {
            onlyLoadImagesOnWIfi -> currentlyUnmetered(app)
            else -> true
        }
    }

    val shouldPreloadCustomTab: Boolean
        get() = preloadCustomTab && openLinksWith == PREF_VAL_OPEN_WITH_CUSTOM_TAB
}

enum class ThemeOptions(
    @StringRes val stringId: Int
) {
    DAY(R.string.theme_day),
    NIGHT(R.string.theme_night),
    SYSTEM(R.string.theme_system);

    @Composable
    fun asString() = stringResource(id = stringId)

    @Composable
    fun getColors(): Colors {
        return when (this) {
            DAY -> FeederLightColorPalette()
            NIGHT -> FeederDarkColorPalette()
            SYSTEM -> if (isSystemInDarkTheme()) FeederDarkColorPalette() else FeederLightColorPalette()
        }
    }

    @Composable
    fun isDarkTheme(): Boolean {
        return when (this) {
            DAY -> false
            NIGHT -> true
            SYSTEM -> isSystemInDarkTheme()
        }
    }

    fun toPrefValue(): String =
        when (this) {
            DAY -> "day"
            NIGHT -> "night"
            SYSTEM -> "system"
        }

    companion object {
        fun fromString(context: Context, value: String?): ThemeOptions =
            when (value) {
                context.getString(R.string.pref_theme_value_night) -> NIGHT
                context.getString(R.string.pref_theme_value_day) -> DAY
                else -> SYSTEM
            }
    }
}

enum class SortingOptions(
    @StringRes val stringId: Int
) {
    NEWEST_FIRST(R.string.sort_newest_first),
    OLDEST_FIRST(R.string.sort_oldest_first);

    @Composable
    fun asString() = stringResource(id = stringId)
}

enum class ItemOpener(
    @StringRes val stringId: Int
) {
    READER(R.string.open_in_reader),
    CUSTOM_TAB(R.string.open_in_custom_tab),
    DEFAULT_BROWSER(R.string.open_in_default_browser);

    @Composable
    fun asString() = stringResource(id = stringId)
}

enum class LinkOpener(
    @StringRes val stringId: Int
) {
    CUSTOM_TAB(R.string.open_in_custom_tab),
    DEFAULT_BROWSER(R.string.open_in_default_browser);

    @Composable
    fun asString() = stringResource(id = stringId)
}

enum class SyncFrequency(
    val minutes: Long,
    @StringRes val stringId: Int
) {
    MANUAL(-1L, R.string.sync_option_manually),
    EVERY_15_MIN(15L, R.string.sync_option_every_15min),
    EVERY_30_MIN(30L, R.string.sync_option_every_30min),
    EVERY_1_HOURS(60L, R.string.sync_option_every_hour),
    EVERY_3_HOURS(180L, R.string.sync_option_every_3_hours),
    EVERY_6_HOURS(360L, R.string.sync_option_every_6_hours),
    EVERY_12_HOURS(720L, R.string.sync_option_every_12_hours),
    EVERY_DAY(1440L, R.string.sync_option_every_day);

    @Composable
    fun asString() = stringResource(id = stringId)
}

val Context.isSystemThemeNight: Boolean
    get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
