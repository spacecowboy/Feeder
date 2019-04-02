package com.nononsenseapps.feeder.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.configurePeriodicSync
import com.nononsenseapps.feeder.util.*

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // When switching themes, the view gets recreated by navigation manager doesn't run again to set label
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.action_settings)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

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
                PREF_SYNC_ONLY_CHARGING, PREF_SYNC_ONLY_WIFI, PREF_SYNC_FREQ -> configurePeriodicSync(context, forceReplace = true)
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
