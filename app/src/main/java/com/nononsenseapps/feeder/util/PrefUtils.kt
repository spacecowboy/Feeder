package com.nononsenseapps.feeder.util

import android.app.Application
import android.content.SharedPreferences
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.archmodel.PREF_DEFAULT_OPEN_ITEM_WITH
import com.nononsenseapps.feeder.archmodel.PREF_IMG_ONLY_WIFI
import com.nononsenseapps.feeder.archmodel.PREF_IMG_SHOW_THUMBNAILS
import com.nononsenseapps.feeder.archmodel.PREF_LAST_FEED_ID
import com.nononsenseapps.feeder.archmodel.PREF_LAST_FEED_TAG
import com.nononsenseapps.feeder.archmodel.PREF_OPEN_LINKS_WITH
import com.nononsenseapps.feeder.archmodel.PREF_PRELOAD_CUSTOM_TAB
import com.nononsenseapps.feeder.archmodel.PREF_SHOW_FAB
import com.nononsenseapps.feeder.archmodel.PREF_SHOW_ONLY_UNREAD
import com.nononsenseapps.feeder.archmodel.PREF_SORT
import com.nononsenseapps.feeder.archmodel.PREF_SYNC_FREQ
import com.nononsenseapps.feeder.archmodel.PREF_SYNC_ONLY_CHARGING
import com.nononsenseapps.feeder.archmodel.PREF_SYNC_ONLY_WIFI
import com.nononsenseapps.feeder.archmodel.PREF_SYNC_ON_RESUME
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_BROWSER
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_READER
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_WEBVIEW
import com.nononsenseapps.feeder.archmodel.PREF_WELCOME_DONE
import com.nononsenseapps.feeder.archmodel.SortingOptions
import com.nononsenseapps.feeder.archmodel.SyncFrequency
import com.nononsenseapps.feeder.db.room.ID_UNSET
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


/**
 * Database settings
 */
const val PREF_MAX_ITEM_COUNT_PER_FEED = "pref_max_item_count_per_feed"

fun SharedPreferences.getStringNonNull(key: String, defaultValue: String): String =
    getString(key, defaultValue) ?: defaultValue

// TODO delete me
class Prefs(override val di: DI) : DIAware {
    private val sp: SharedPreferences by instance()
    private val app: Application by instance()

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

    var currentItemOpener: ItemOpener
        get() = when (openItemsWith) {
            PREF_VAL_OPEN_WITH_BROWSER -> ItemOpener.DEFAULT_BROWSER
            PREF_VAL_OPEN_WITH_WEBVIEW,
            PREF_VAL_OPEN_WITH_CUSTOM_TAB,
            -> ItemOpener.CUSTOM_TAB
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
                    it.minutes==synchronizationFrequency
                }
                ?: SyncFrequency.MANUAL
        set(value) {
            synchronizationFrequency = value.minutes
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
        get() = preloadCustomTab && openLinksWith==PREF_VAL_OPEN_WITH_CUSTOM_TAB
}
