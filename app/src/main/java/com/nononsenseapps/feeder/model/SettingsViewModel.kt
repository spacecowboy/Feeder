package com.nononsenseapps.feeder.model

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.CoroutineScopedKodeinAwareViewModel
import com.nononsenseapps.feeder.util.PREF_THEME
import com.nononsenseapps.feeder.util.PrefUtils
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class SettingsViewModel(kodein: Kodein) : CoroutineScopedKodeinAwareViewModel(kodein), SharedPreferences.OnSharedPreferenceChangeListener {
    private val app: Application by instance()
    private val prefThemeDefault: String = app.getString(R.string.pref_theme_value_default)
    private val prefThemeDay: String = app.getString(R.string.pref_theme_value_day)
    private val prefThemeNight: String = app.getString(R.string.pref_theme_value_night)
    private val prefThemeAuto: String = app.getString(R.string.pref_theme_value_auto)

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
        liveMutableThemePreference.value = PrefUtils.getNightMode(app)

        PreferenceManager.getDefaultSharedPreferences(app).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences != null && key != null) {
            when (key) {
                PREF_THEME -> themePreference = when (sharedPreferences.getString(PREF_THEME, prefThemeDefault)) {
                    prefThemeDay -> AppCompatDelegate.MODE_NIGHT_NO
                    prefThemeNight -> AppCompatDelegate.MODE_NIGHT_YES
                    prefThemeAuto -> AppCompatDelegate.MODE_NIGHT_AUTO
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            }
        }
    }

    override fun onCleared() {
        PreferenceManager.getDefaultSharedPreferences(app).unregisterOnSharedPreferenceChangeListener(this)
        super.onCleared()
    }
}
