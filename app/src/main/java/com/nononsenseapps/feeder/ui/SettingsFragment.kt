package com.nononsenseapps.feeder.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.PREF_DEFAULT_OPEN_ITEM_WITH
import com.nononsenseapps.feeder.util.PREF_OPEN_LINKS_WITH
import com.nononsenseapps.feeder.util.PREF_SYNC_FREQ
import com.nononsenseapps.feeder.util.PREF_SYNC_HOTSPOTS
import com.nononsenseapps.feeder.util.PREF_SYNC_ONLY_CHARGING
import com.nononsenseapps.feeder.util.PREF_SYNC_ONLY_WIFI
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.setupSync

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fill in default values
        addPreferencesFromResource(R.xml.settings)

        PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(this)
        bindPreferenceSummaryToValue(PREF_SYNC_FREQ)
        bindPreferenceSummaryToValue(PREF_DEFAULT_OPEN_ITEM_WITH)
        bindPreferenceSummaryToValue(PREF_OPEN_LINKS_WITH)
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(activity).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_SYNC_ONLY_CHARGING, PREF_SYNC_HOTSPOTS, PREF_SYNC_ONLY_WIFI, PREF_SYNC_FREQ -> activity.setupSync()
        }
    }

    private fun bindPreferenceSummaryToValue(prefKey: String) {
        val preference = findPreference(prefKey)
        if (preference != null) {
            // Set change listener
            preference.onPreferenceChangeListener = PrefUtils.summaryUpdater
            // Trigger the listener immediately with the preference's  current value.
            PrefUtils.summaryUpdater.onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.context).getString(preference.key, ""))
        }
    }
}
