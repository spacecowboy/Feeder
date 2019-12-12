package com.nononsenseapps.feeder.ui

import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.configurePeriodicSync
import com.nononsenseapps.feeder.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

@ExperimentalCoroutinesApi
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, KodeinAware {
    override val kodein by closestKodein()
    private val sharedPreferences: SharedPreferences by instance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // When switching themes, the view gets recreated by navigation manager doesn't run again to set label
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.action_settings)

        return super.onCreateView(inflater, container, savedInstanceState)?.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                it.background = ColorDrawable(resources.getColor(R.color.window_background, it.context?.theme))
            } else {
                @Suppress("DEPRECATION")
                it.background = ColorDrawable(resources.getColor(R.color.window_background))
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Fill in default values
        addPreferencesFromResource(R.xml.settings)

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        bindPreferenceSummaryToValue(PREF_THEME)
        bindPreferenceSummaryToValue(PREF_SYNC_FREQ)
        bindPreferenceSummaryToValue(PREF_DEFAULT_OPEN_ITEM_WITH)
        bindPreferenceSummaryToValue(PREF_OPEN_LINKS_WITH)
        bindPreferenceSummaryToValue(PREF_MAX_ITEM_COUNT_PER_FEED)
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
}
