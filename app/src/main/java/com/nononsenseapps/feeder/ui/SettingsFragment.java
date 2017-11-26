package com.nononsenseapps.feeder.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.util.ContextExtensionsKt;
import com.nononsenseapps.feeder.util.PrefUtils;

import static com.nononsenseapps.feeder.util.PrefUtilsKt.PREF_SYNC_FREQ;
import static com.nononsenseapps.feeder.util.PrefUtilsKt.PREF_SYNC_HOTSPOTS;
import static com.nononsenseapps.feeder.util.PrefUtilsKt.PREF_SYNC_ONLY_CHARGING;
import static com.nononsenseapps.feeder.util.PrefUtilsKt.PREF_SYNC_ONLY_WIFI;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fill in default values
        addPreferencesFromResource(R.xml.settings);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        bindPreferenceSummaryToValue(PREF_SYNC_FREQ);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, final String key) {
        switch (key) {
            case PREF_SYNC_ONLY_CHARGING:
            case PREF_SYNC_HOTSPOTS:
            case PREF_SYNC_ONLY_WIFI:
            case PREF_SYNC_FREQ:
                ContextExtensionsKt.setupSync(getActivity());
                break;
        }
    }

    private void bindPreferenceSummaryToValue(@NonNull String prefKey) {
        Preference preference = findPreference(prefKey);
        if (preference != null) {
            // Set change listener
            preference.setOnPreferenceChangeListener(PrefUtils.INSTANCE.getSummaryUpdater());
            // Trigger the listener immediately with the preference's  current value.
            PrefUtils.INSTANCE.getSummaryUpdater().onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        }
    }
}
