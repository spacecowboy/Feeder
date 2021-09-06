package com.nononsenseapps.feeder.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.util.ItemOpener
import com.nononsenseapps.feeder.util.LinkOpener
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.SortingOptions
import com.nononsenseapps.feeder.util.SyncFrequency
import com.nononsenseapps.feeder.util.ThemeOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI
import org.kodein.di.instance

class SettingsViewModel(di: DI) : DIAwareViewModel(di),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val app: Application by instance()
    private val prefs: Prefs by instance()
    private val sharedPreferences: SharedPreferences by instance()

    private val _showOnlyUnread = MutableStateFlow(prefs.showOnlyUnread)
    val showOnlyUnread = _showOnlyUnread.asStateFlow()
    fun setShowOnlyUnread(onlyUnread: Boolean) {
        _showOnlyUnread.value = onlyUnread
        prefs.showOnlyUnread = onlyUnread
    }

    private val _currentFeedAndTag = MutableStateFlow(
        prefs.lastOpenFeedId to (prefs.lastOpenFeedTag ?: "")
    )
    val currentFeedAndTag = _currentFeedAndTag.asStateFlow()
    fun setCurrentFeedAndTag(feedId: Long, tag: String) {
        _currentFeedAndTag.value = feedId to tag
        prefs.lastOpenFeedId = feedId
        prefs.lastOpenFeedTag = tag
    }

    private val _currentTheme = MutableStateFlow(prefs.currentTheme)
    val currentTheme = _currentTheme.asStateFlow()
    fun setCurrentTheme(theme: ThemeOptions) {
        _currentTheme.value = theme
        prefs.currentTheme = theme
    }

    private val _currentSorting = MutableStateFlow(prefs.currentSorting)
    val currentSorting = _currentSorting.asStateFlow()
    fun setCurrentSorting(value: SortingOptions) {
        _currentSorting.value = value
        prefs.currentSorting = value
    }

    private val _showFab = MutableStateFlow(prefs.show_fab)
    val showFab = _showFab.asStateFlow()
    fun setShowFab(value: Boolean) {
        _showFab.value = value
        prefs.show_fab = value
    }

    private val _syncOnResume = MutableStateFlow(prefs.syncOnResume)
    val syncOnResume = _syncOnResume.asStateFlow()
    fun setSyncOnResume(value: Boolean) {
        _syncOnResume.value = value
        prefs.syncOnResume = value
    }

    private val _syncOnlyOnWifi = MutableStateFlow(prefs.onlySyncOnWifi)
    val syncOnlyOnWifi = _syncOnlyOnWifi.asStateFlow()
    fun setSyncOnlyOnWifi(value: Boolean) {
        _syncOnlyOnWifi.value = value
        prefs.onlySyncOnWifi = value
    }

    private val _syncOnlyWhenCharging = MutableStateFlow(prefs.onlySyncWhileCharging)
    val syncOnlyWhenCharging = _syncOnlyWhenCharging.asStateFlow()
    fun setSyncOnlyWhenCharging(value: Boolean) {
        _syncOnlyWhenCharging.value = value
        prefs.onlySyncWhileCharging = value
    }

    private val _loadImageOnlyOnWifi = MutableStateFlow(prefs.onlyLoadImagesOnWIfi)
    val loadImageOnlyOnWifi = _loadImageOnlyOnWifi.asStateFlow()
    fun setLoadImageOnlyOnWifi(value: Boolean) {
        _loadImageOnlyOnWifi.value = value
        prefs.onlyLoadImagesOnWIfi = value
    }

    private val _showThumbnails = MutableStateFlow(prefs.showThumbnails)
    val showThumbnails = _showThumbnails.asStateFlow()
    fun setShowThumbnails(value: Boolean) {
        _showThumbnails.value = value
        prefs.showThumbnails = value
    }

    private val _maximumCountPerFeed = MutableStateFlow(prefs.maximumCountPerFeed)
    val maximumCountPerFeed = _maximumCountPerFeed.asStateFlow()
    fun setMaxCountPerFeed(value: Int) {
        _maximumCountPerFeed.value = value
        prefs.maximumCountPerFeed = value
    }

    private val _itemOpener = MutableStateFlow(prefs.currentItemOpener)
    val itemOpener = _itemOpener.asStateFlow()
    fun setItemOpener(value: ItemOpener) {
        _itemOpener.value = value
        prefs.currentItemOpener = value
    }

    private val _linkOpener = MutableStateFlow(prefs.currentLinkOpener)
    val linkOpener = _linkOpener.asStateFlow()
    fun setLinkOpener(value: LinkOpener) {
        _linkOpener.value = value
        prefs.currentLinkOpener = value
    }

    private val _syncFrequency = MutableStateFlow(prefs.currentSyncFrequency)
    val syncFrequency = _syncFrequency.asStateFlow()
    fun setSyncFrequency(value: SyncFrequency) {
        _syncFrequency.value = value
        prefs.currentSyncFrequency = value
        configurePeriodicSync(app, forceReplace = true)
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // TODO update mutable state - settings may have been changed in other activity
    }
}

@ColorInt
fun Context.getColorCompat(@ColorRes color: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(color)
    } else {
        @Suppress("DEPRECATION")
        resources.getColor(color)
    }
}
