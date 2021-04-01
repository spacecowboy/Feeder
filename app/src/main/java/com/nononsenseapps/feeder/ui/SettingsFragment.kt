package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.configurePeriodicSync
import com.nononsenseapps.feeder.util.PREF_BATTERY_OPTIMIZATION
import com.nononsenseapps.feeder.util.PREF_DEFAULT_OPEN_ITEM_WITH
import com.nononsenseapps.feeder.util.PREF_MAX_ITEM_COUNT_PER_FEED
import com.nononsenseapps.feeder.util.PREF_OPEN_LINKS_WITH
import com.nononsenseapps.feeder.util.PREF_SORT
import com.nononsenseapps.feeder.util.PREF_SYNC_FREQ
import com.nononsenseapps.feeder.util.PREF_SYNC_ONLY_CHARGING
import com.nononsenseapps.feeder.util.PREF_SYNC_ONLY_WIFI
import com.nononsenseapps.feeder.util.PREF_THEME
import com.nononsenseapps.feeder.util.PreferenceSummaryUpdater
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

@FlowPreview
@ExperimentalCoroutinesApi
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, DIAware {
    override val di by closestDI()
    private val sharedPreferences: SharedPreferences by instance()
    private val settingsViewModel: SettingsViewModel by instance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // When switching themes, the view gets recreated by navigation manager doesn't run again to set label
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.action_settings)

        return super.onCreateView(inflater, container, savedInstanceState).also {
            it?.setBackgroundColor(settingsViewModel.backgroundColor)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Fill in default values
        addPreferencesFromResource(R.xml.settings)

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        bindPreferenceSummaryToValue(PREF_THEME)
        bindPreferenceSummaryToValue(PREF_SORT)
        bindPreferenceSummaryToValue(PREF_SYNC_FREQ)
        bindPreferenceSummaryToValue(PREF_DEFAULT_OPEN_ITEM_WITH)
        bindPreferenceSummaryToValue(PREF_OPEN_LINKS_WITH)
        bindPreferenceSummaryToValue(PREF_MAX_ITEM_COUNT_PER_FEED)

        setupBatteryOptimizationPreference()
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        context?.let { context ->
            when (key) {
                PREF_SYNC_ONLY_CHARGING, PREF_SYNC_ONLY_WIFI, PREF_SYNC_FREQ -> configurePeriodicSync(context, forceReplace = true)
                else -> {
                }
            }
        }
    }

    private fun bindPreferenceSummaryToValue(prefKey: String) {
        val preference: Preference? = findPreference(prefKey)
        if (preference != null) {
            // Set change listener
            preference.onPreferenceChangeListener = PreferenceSummaryUpdater
            // Trigger the listener immediately with the preference's  current value.
            PreferenceSummaryUpdater.onPreferenceChange(
                preference,
                sharedPreferences.getString(preference.key, "")
            )
        }
    }

    private fun setupBatteryOptimizationPreference() {
        findPreference<Preference>(PREF_BATTERY_OPTIMIZATION)?.let { pref ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pref.setOnPreferenceClickListener {
                    context?.startActivity(
                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    )

                    true
                }
            } else {
                pref.isEnabled = false
                pref.isVisible = false
            }
        }
    }

    private fun setBatteryOptimizationSummary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findPreference<Preference>(PREF_BATTERY_OPTIMIZATION)?.let { pref ->
                val powerManager = context?.getSystemService<PowerManager>()

                pref.setSummary(
                    when (powerManager?.isIgnoringBatteryOptimizations(context?.packageName)) {
                        true -> R.string.battery_optimization_disabled
                        else -> R.string.battery_optimization_enabled
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        setBatteryOptimizationSummary()
    }
}
