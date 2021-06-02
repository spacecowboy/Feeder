package com.nononsenseapps.feeder.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.util.CurrentTheme
import com.nononsenseapps.feeder.util.PREF_SORT
import com.nononsenseapps.feeder.util.PREF_THEME
import com.nononsenseapps.feeder.util.Prefs
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.kodein.di.DI
import org.kodein.di.instance

class SettingsViewModel(di: DI) : DIAwareViewModel(di), SharedPreferences.OnSharedPreferenceChangeListener {
    private val app: Application by instance()
    private val prefs: Prefs by instance()
    private val sharedPreferences: SharedPreferences by instance()

    // TODO remove
    private val keyChannel = ConflatedBroadcastChannel<String>()
    private val keyFlow = keyChannel.asFlow()

    val liveThemePreferenceNoInitial: LiveData<CurrentTheme> =
        keyFlow.filter { it == PREF_THEME }
            .map { prefs.currentTheme }
            .conflate()
            .asLiveData()

    val liveIsNightMode: MutableLiveData<Boolean> by lazy { MutableLiveData(prefs.isNightMode) }

    val liveIsNewestFirst: LiveData<Boolean> =
        keyFlow.filter { it == PREF_SORT }
            .map { prefs.isNewestFirst }
            .conflate()
            .asLiveData()

    val backgroundColor: Int
        get() =
            when (prefs.isNightMode) {
                true -> app.getColorCompat(R.color.night_background)
                false -> app.getColorCompat(R.color.day_background)
            }

    val accentColor: Int
        get() =
            when (prefs.isNightMode) {
                true -> app.getColorCompat(R.color.accentNight)
                false -> app.getColorCompat(R.color.accentDay)
            }

    private val _showOnlyUnread = MutableStateFlow(prefs.showOnlyUnread)
    val showOnlyUnread = _showOnlyUnread.asStateFlow()
    fun setShowOnlyUnread(onlyUnread: Boolean) {
        Log.d("JONAS", "Setting only unread: $onlyUnread")
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
    fun setCurrentTheme(theme: CurrentTheme) {
        _currentTheme.value = theme
        prefs.currentTheme = theme
    }


    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && !keyChannel.isClosedForSend) {
            keyChannel.offer(key)
        }

        // TODO update mutable state - or just force everything through the view model
        when (key) {

        }
    }

    override fun onCleared() {
        keyChannel.close()
        super.onCleared()
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
