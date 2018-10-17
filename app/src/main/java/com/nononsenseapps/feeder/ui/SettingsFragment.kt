package com.nononsenseapps.feeder.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.configurePeriodicSync
import com.nononsenseapps.feeder.util.PREF_DEFAULT_OPEN_ITEM_WITH
import com.nononsenseapps.feeder.util.PREF_MAX_ITEM_COUNT_PER_FEED
import com.nononsenseapps.feeder.util.PREF_OPEN_LINKS_WITH
import com.nononsenseapps.feeder.util.PREF_SYNC_FREQ
import com.nononsenseapps.feeder.util.PREF_SYNC_HOTSPOTS
import com.nononsenseapps.feeder.util.PREF_SYNC_ONLY_CHARGING
import com.nononsenseapps.feeder.util.PREF_SYNC_ONLY_WIFI
import com.nononsenseapps.feeder.util.PREF_THEME
import com.nononsenseapps.feeder.util.PrefUtils

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Fill in default values
        addPreferencesFromResource(R.xml.settings)

        PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(this)
        bindPreferenceSummaryToValue(PREF_THEME)
        bindPreferenceSummaryToValue(PREF_SYNC_FREQ)
        bindPreferenceSummaryToValue(PREF_DEFAULT_OPEN_ITEM_WITH)
        bindPreferenceSummaryToValue(PREF_OPEN_LINKS_WITH)
        bindPreferenceSummaryToValue(PREF_MAX_ITEM_COUNT_PER_FEED)
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(activity).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        context?.let { context ->
            when (key) {
                PREF_SYNC_ONLY_CHARGING, PREF_SYNC_HOTSPOTS, PREF_SYNC_ONLY_WIFI, PREF_SYNC_FREQ -> configurePeriodicSync(context, forceReplace = true)
                else -> {}
            }
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
