package com.nononsenseapps.feeder.ui.compose.settings

import android.app.Application
import android.os.PowerManager
import androidx.compose.runtime.Immutable
import androidx.core.content.getSystemService
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.archmodel.DarkThemePreferences
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.archmodel.SortingOptions
import com.nononsenseapps.feeder.archmodel.SwipeAsRead
import com.nononsenseapps.feeder.archmodel.SyncFrequency
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.base.DIAwareViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class SettingsViewModel(di: DI) : DIAwareViewModel(di) {
    private val repository: Repository by instance()
    private val context: Application by instance()

    fun setCurrentTheme(value: ThemeOptions) {
        repository.setCurrentTheme(value)
    }

    fun setPreferredDarkTheme(value: DarkThemePreferences) {
        repository.setPreferredDarkTheme(value)
    }

    fun setCurrentSorting(value: SortingOptions) {
        repository.setCurrentSorting(value)
    }

    fun setShowFab(value: Boolean) {
        repository.setShowFab(value)
    }

    fun setSyncOnResume(value: Boolean) {
        repository.setSyncOnResume(value)
    }

    fun setSyncOnlyOnWifi(value: Boolean) = viewModelScope.launch {
        repository.setSyncOnlyOnWifi(value)
    }

    fun setSyncOnlyWhenCharging(value: Boolean) = viewModelScope.launch {
        repository.setSyncOnlyWhenCharging(value)
    }

    fun setLoadImageOnlyOnWifi(value: Boolean) {
        repository.setLoadImageOnlyOnWifi(value)
    }

    fun setShowThumbnails(value: Boolean) {
        repository.setShowThumbnails(value)
    }

    fun setMaxCountPerFeed(value: Int) {
        repository.setMaxCountPerFeed(value)
    }

    fun setItemOpener(value: ItemOpener) {
        repository.setItemOpener(value)
    }

    fun setLinkOpener(value: LinkOpener) {
        repository.setLinkOpener(value)
    }

    fun setSyncFrequency(value: SyncFrequency) = viewModelScope.launch {
        repository.setSyncFrequency(value)
    }

    fun setFeedItemStyle(value: FeedItemStyle) {
        repository.setFeedItemStyle(value)
    }

    fun setBlockList(value: Iterable<String>) {
        repository.setBlockList(value)
    }

    fun setSwipeAsRead(value: SwipeAsRead) {
        repository.setSwipeAsRead(value)
    }

    private val batteryOptimizationIgnoredFlow: Flow<Boolean> = repository.resumeTime.map {
        val powerManager: PowerManager? = context.getSystemService()
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    }.buffer(1)

    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState: StateFlow<SettingsViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.currentTheme,
                repository.preferredDarkTheme,
                repository.currentSorting,
                repository.showFab,
                repository.syncOnResume,
                repository.syncOnlyOnWifi,
                repository.syncOnlyWhenCharging,
                repository.loadImageOnlyOnWifi,
                repository.showThumbnails,
                repository.maximumCountPerFeed,
                repository.itemOpener,
                repository.linkOpener,
                repository.syncFrequency,
                batteryOptimizationIgnoredFlow,
                repository.feedItemStyle,
                repository.swipeAsRead,
                repository.blockList,
            ) { params: Array<Any> ->
                SettingsViewState(
                    currentTheme = params[0] as ThemeOptions,
                    darkThemePreference = params[1] as DarkThemePreferences,
                    currentSorting = params[2] as SortingOptions,
                    showFab = params[3] as Boolean,
                    syncOnResume = params[4] as Boolean,
                    syncOnlyOnWifi = params[5] as Boolean,
                    syncOnlyWhenCharging = params[6] as Boolean,
                    loadImageOnlyOnWifi = params[7] as Boolean,
                    showThumbnails = params[8] as Boolean,
                    maximumCountPerFeed = params[9] as Int,
                    itemOpener = params[10] as ItemOpener,
                    linkOpener = params[11] as LinkOpener,
                    syncFrequency = params[12] as SyncFrequency,
                    batteryOptimizationIgnored = params[13] as Boolean,
                    feedItemStyle = params[14] as FeedItemStyle,
                    swipeAsRead = params[15] as SwipeAsRead,
                    blockList = params[16] as Set<String>,
                )
            }.collect {
                _viewState.value = it
            }
        }
    }
}

@Immutable
data class SettingsViewState(
    val currentTheme: ThemeOptions = ThemeOptions.SYSTEM,
    val darkThemePreference: DarkThemePreferences = DarkThemePreferences.BLACK,
    val currentSorting: SortingOptions = SortingOptions.NEWEST_FIRST,
    val showFab: Boolean = true,
    val feedItemStyle: FeedItemStyle = FeedItemStyle.CARD,
    val blockList: Set<String> = emptySet(),
    val syncOnResume: Boolean = false,
    val syncOnlyOnWifi: Boolean = false,
    val syncOnlyWhenCharging: Boolean = false,
    val loadImageOnlyOnWifi: Boolean = false,
    val showThumbnails: Boolean = false,
    val maximumCountPerFeed: Int = 100,
    val itemOpener: ItemOpener = ItemOpener.READER,
    val linkOpener: LinkOpener = LinkOpener.CUSTOM_TAB,
    val syncFrequency: SyncFrequency = SyncFrequency.EVERY_1_HOURS,
    val batteryOptimizationIgnored: Boolean = false,
    val swipeAsRead: SwipeAsRead = SwipeAsRead.ONLY_FROM_END,
)
