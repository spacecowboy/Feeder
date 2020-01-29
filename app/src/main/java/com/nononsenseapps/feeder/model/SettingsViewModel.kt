package com.nononsenseapps.feeder.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.util.CurrentTheme
import com.nononsenseapps.feeder.util.PREF_THEME
import com.nononsenseapps.feeder.util.Prefs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

@FlowPreview
@ExperimentalCoroutinesApi
class SettingsViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein), SharedPreferences.OnSharedPreferenceChangeListener {
    private val app: Application by instance()
    private val prefs: Prefs by instance()
    private val sharedPreferences: SharedPreferences by instance()

    private val keyChannel = ConflatedBroadcastChannel<String>()
    private val keyFlow = keyChannel.asFlow()

    val liveThemePreferenceNoInitial: LiveData<CurrentTheme> =
            keyFlow.filter { it == PREF_THEME }
                    .map { prefs.currentTheme }
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

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && !keyChannel.isClosedForSend) {
            keyChannel.offer(key)
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
