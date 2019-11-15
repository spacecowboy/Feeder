package com.nononsenseapps.feeder.model

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.util.PREF_THEME
import com.nononsenseapps.feeder.util.Prefs
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class SettingsViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein), SharedPreferences.OnSharedPreferenceChangeListener {
    private val app: Application by instance()
    private val prefs: Prefs by instance()
    private val sharedPreferences: SharedPreferences by instance()
    private val prefThemeDefault: String = app.getString(R.string.pref_theme_value_default)
    private val prefThemeDay: String = app.getString(R.string.pref_theme_value_day)
    private val prefThemeNight: String = app.getString(R.string.pref_theme_value_night)

    private val liveMutableThemePreference = MutableLiveData<Int>()

    val liveThemePreference: LiveData<Int>
        get() = liveMutableThemePreference

    var themePreference: Int
        get() = liveMutableThemePreference.value ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        set(value) {
            AppCompatDelegate.setDefaultNightMode(value)
            liveMutableThemePreference.value = value
        }

    init {
        liveMutableThemePreference.value = prefs.nightMode

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences != null && key != null) {
            when (key) {
                PREF_THEME -> themePreference = when (sharedPreferences.getString(PREF_THEME, prefThemeDefault)) {
                    prefThemeDay -> AppCompatDelegate.MODE_NIGHT_NO
                    prefThemeNight -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            }
        }
    }

    override fun onCleared() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onCleared()
    }
}
