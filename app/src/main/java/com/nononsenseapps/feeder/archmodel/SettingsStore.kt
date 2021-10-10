package com.nononsenseapps.feeder.archmodel

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedSyncer
import com.nononsenseapps.feeder.model.UNIQUE_PERIODIC_NAME
import com.nononsenseapps.feeder.util.PREF_MAX_ITEM_COUNT_PER_FEED
import com.nononsenseapps.feeder.util.getStringNonNull
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class SettingsStore(override val di: DI) : DIAware {
    private val sp: SharedPreferences by instance()

    private val _showOnlyUnread = MutableStateFlow(sp.getBoolean(PREF_SHOW_ONLY_UNREAD, true))
    val showOnlyUnread: StateFlow<Boolean> = _showOnlyUnread.asStateFlow()
    fun setShowOnlyUnread(value: Boolean) {
        sp.edit().putBoolean(PREF_SHOW_ONLY_UNREAD, value).apply()
        _showOnlyUnread.value = value
    }

    private val _currentFeedAndTag = MutableStateFlow(
        sp.getLong(PREF_LAST_FEED_ID, ID_UNSET) to (sp.getString(PREF_LAST_FEED_TAG, null) ?: "")
    )
    val currentFeedAndTag = _currentFeedAndTag.asStateFlow()
    fun setCurrentFeedAndTag(feedId: Long, tag: String) {
        _currentFeedAndTag.value = feedId to tag
        sp.edit().putLong(PREF_LAST_FEED_ID, feedId).apply()
        sp.edit().putString(PREF_LAST_FEED_TAG, tag).apply()
    }

    private val _currentTheme = MutableStateFlow(
        ThemeOptions.valueOf(sp.getString(PREF_THEME, null)?.uppercase()
            ?: ThemeOptions.SYSTEM.name)
    )
    val currentTheme = _currentTheme.asStateFlow()
    fun setCurrentTheme(value: ThemeOptions) {
        _currentTheme.value = value
        sp.edit().putString(PREF_THEME, value.name.lowercase()).apply()
    }

    private val _darkThemePreference = MutableStateFlow(
        DarkThemePreferences.valueOf(sp.getString(PREF_DARK_THEME, null)?.uppercase()
            ?: DarkThemePreferences.BLACK.name)
    )
    val darkThemePreference = _darkThemePreference.asStateFlow()
    fun setDarkThemePreference(value: DarkThemePreferences) {
        _darkThemePreference.value = value
        sp.edit().putString(PREF_DARK_THEME, value.name.lowercase()).apply()
    }

    private val _currentSorting = MutableStateFlow(
        SortingOptions.valueOf(sp.getString(PREF_SORT, null)?.uppercase()
            ?: SortingOptions.NEWEST_FIRST.name)
    )
    val currentSorting = _currentSorting.asStateFlow()
    fun setCurrentSorting(value: SortingOptions) {
        _currentSorting.value = value
        sp.edit().putString(PREF_SORT, value.name.lowercase()).apply()
    }

    private val _showFab = MutableStateFlow(sp.getBoolean(PREF_SHOW_FAB, true))
    val showFab = _showFab.asStateFlow()
    fun setShowFab(value: Boolean) {
        _showFab.value = value
        sp.edit().putBoolean(PREF_SHOW_FAB, value).apply()
    }

    private val _syncOnResume = MutableStateFlow(sp.getBoolean(PREF_SYNC_ON_RESUME, false))
    val syncOnResume = _syncOnResume.asStateFlow()
    fun setSyncOnResume(value: Boolean) {
        _syncOnResume.value = value
        sp.edit().putBoolean(PREF_SYNC_ON_RESUME, value).apply()
    }

    private val _syncOnlyOnWifi = MutableStateFlow(sp.getBoolean(PREF_SYNC_ONLY_WIFI, false))
    val syncOnlyOnWifi = _syncOnlyOnWifi.asStateFlow()
    fun setSyncOnlyOnWifi(value: Boolean) {
        _syncOnlyOnWifi.value = value
        sp.edit().putBoolean(PREF_SYNC_ONLY_WIFI, value).apply()
    }

    private val _syncOnlyWhenCharging = MutableStateFlow(sp.getBoolean(PREF_SYNC_ONLY_CHARGING, false))
    val syncOnlyWhenCharging = _syncOnlyWhenCharging.asStateFlow()
    fun setSyncOnlyWhenCharging(value: Boolean) {
        _syncOnlyWhenCharging.value = value
        sp.edit().putBoolean(PREF_SYNC_ONLY_CHARGING, value).apply()
    }

    private val _loadImageOnlyOnWifi = MutableStateFlow(sp.getBoolean(PREF_IMG_ONLY_WIFI, false))
    val loadImageOnlyOnWifi = _loadImageOnlyOnWifi.asStateFlow()
    fun setLoadImageOnlyOnWifi(value: Boolean) {
        _loadImageOnlyOnWifi.value = value
        sp.edit().putBoolean(PREF_IMG_ONLY_WIFI, value).apply()
    }

    private val _showThumbnails = MutableStateFlow(sp.getBoolean(PREF_IMG_SHOW_THUMBNAILS, true))
    val showThumbnails = _showThumbnails.asStateFlow()
    fun setShowThumbnails(value: Boolean) {
        _showThumbnails.value = value
        sp.edit().putBoolean(PREF_IMG_SHOW_THUMBNAILS, value).apply()
    }

    private val _maximumCountPerFeed = MutableStateFlow(sp.getStringNonNull(PREF_MAX_ITEM_COUNT_PER_FEED, "100").toInt())
    val maximumCountPerFeed = _maximumCountPerFeed.asStateFlow()
    fun setMaxCountPerFeed(value: Int) {
        _maximumCountPerFeed.value = value
        sp.edit().putString(PREF_MAX_ITEM_COUNT_PER_FEED, "$value").apply()
    }

    private val _itemOpener = MutableStateFlow(
        when (sp.getStringNonNull(PREF_DEFAULT_OPEN_ITEM_WITH, PREF_VAL_OPEN_WITH_READER)) {
            PREF_VAL_OPEN_WITH_BROWSER -> ItemOpener.DEFAULT_BROWSER
            PREF_VAL_OPEN_WITH_WEBVIEW,
            PREF_VAL_OPEN_WITH_CUSTOM_TAB,
            -> ItemOpener.CUSTOM_TAB
            else -> ItemOpener.READER
        }
    )
    val itemOpener = _itemOpener.asStateFlow()
    fun setItemOpener(value: ItemOpener) {
        _itemOpener.value = value
        sp.edit().putString(
            PREF_DEFAULT_OPEN_ITEM_WITH,
            when (value) {
                ItemOpener.READER -> PREF_VAL_OPEN_WITH_READER
                ItemOpener.CUSTOM_TAB -> PREF_VAL_OPEN_WITH_CUSTOM_TAB
                ItemOpener.DEFAULT_BROWSER -> PREF_VAL_OPEN_WITH_BROWSER
            }
        ).apply()
    }

    private val _linkOpener = MutableStateFlow(
        when (sp.getStringNonNull(PREF_OPEN_LINKS_WITH, PREF_VAL_OPEN_WITH_CUSTOM_TAB)) {
            PREF_VAL_OPEN_WITH_BROWSER -> LinkOpener.DEFAULT_BROWSER
            else -> LinkOpener.CUSTOM_TAB
        }
    )
    val linkOpener = _linkOpener.asStateFlow()
    fun setLinkOpener(value: LinkOpener) {
        _linkOpener.value = value
        sp.edit().putString(
            PREF_OPEN_LINKS_WITH,
            when (value) {
                LinkOpener.CUSTOM_TAB -> PREF_VAL_OPEN_WITH_CUSTOM_TAB
                LinkOpener.DEFAULT_BROWSER -> PREF_VAL_OPEN_WITH_BROWSER
            }
        ).apply()
    }

    private val _feedItemStyle = MutableStateFlow(
        when (sp.getStringNonNull(PREF_FEED_ITEM_STYLE, FeedItemStyle.CARD.name)) {
            FeedItemStyle.CARD.name -> FeedItemStyle.CARD
            FeedItemStyle.COMPACT.name -> FeedItemStyle.COMPACT
            FeedItemStyle.SUPER_COMPACT.name -> FeedItemStyle.SUPER_COMPACT
            else -> FeedItemStyle.CARD
        }
    )
    val feedItemStyle = _feedItemStyle.asStateFlow()
    fun setFeedItemStyle(value: FeedItemStyle) {
        _feedItemStyle.value = value
        sp.edit().putString(
            PREF_FEED_ITEM_STYLE,
            value.name
        ).apply()
    }

    private val _syncFrequency by lazy {
        val savedValue = sp.getStringNonNull(PREF_SYNC_FREQ, "60").toLong()
        MutableStateFlow(
            SyncFrequency.values()
                .firstOrNull {
                    it.minutes==savedValue
                }
                ?: SyncFrequency.MANUAL
        )
    }
    val syncFrequency = _syncFrequency.asStateFlow()
    fun setSyncFrequency(value: SyncFrequency) {
        _syncFrequency.value = value
        sp.edit().putString(PREF_SYNC_FREQ, "${value.minutes}").apply()
        configurePeriodicSync()
    }

    internal fun configurePeriodicSync() {
        val workManager: WorkManager by instance()
        val shouldSync = syncFrequency.value.minutes > 0

        if (shouldSync) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(syncOnlyWhenCharging.value)

            if (syncOnlyOnWifi.value) {
                constraints.setRequiredNetworkType(NetworkType.UNMETERED)
            } else {
                constraints.setRequiredNetworkType(NetworkType.CONNECTED)
            }

            val timeInterval = syncFrequency.value.minutes

            val workRequestBuilder = PeriodicWorkRequestBuilder<FeedSyncer>(
                timeInterval, TimeUnit.MINUTES,
                timeInterval / 2, TimeUnit.MINUTES
            )

            val syncWork = workRequestBuilder
                .setConstraints(constraints.build())
                .addTag("periodic_sync")
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                syncWork
            )
        } else {
            workManager.cancelUniqueWork(UNIQUE_PERIODIC_NAME)
        }
    }
}

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
 * Dark theme settings
 */
const val PREF_DARK_THEME = "pref_dark_theme"

/**
 * Sort settings
 */
const val PREF_SORT = "pref_sort"

/**
 * Floating action button settings
 */
const val PREF_SHOW_FAB = "pref_show_fab"

const val PREF_FEED_ITEM_STYLE = "pref_feed_item_style"

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

enum class ThemeOptions(
    @StringRes val stringId: Int,
) {
    DAY(R.string.theme_day),
    NIGHT(R.string.theme_night),
    SYSTEM(R.string.theme_system),
}

enum class DarkThemePreferences(
    @StringRes val stringId: Int,
) {
    BLACK(R.string.dark_theme_preference_black),
    DARK(R.string.dark_theme_preference_dark)
}

enum class SortingOptions(
    @StringRes val stringId: Int,
) {
    NEWEST_FIRST(R.string.sort_newest_first),
    OLDEST_FIRST(R.string.sort_oldest_first),
}

enum class ItemOpener(
    @StringRes val stringId: Int,
) {
    READER(R.string.open_in_reader),
    CUSTOM_TAB(R.string.open_in_custom_tab),
    DEFAULT_BROWSER(R.string.open_in_default_browser),
}

enum class LinkOpener(
    @StringRes val stringId: Int,
) {
    CUSTOM_TAB(R.string.open_in_custom_tab),
    DEFAULT_BROWSER(R.string.open_in_default_browser),
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
}

enum class FeedItemStyle(
    @StringRes val stringId: Int,
) {
    CARD(R.string.feed_item_style_card),
    COMPACT(R.string.feed_item_style_compact),
    SUPER_COMPACT(R.string.feed_item_style_super_compact)
}
