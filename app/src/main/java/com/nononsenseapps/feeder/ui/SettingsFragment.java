package com.nononsenseapps.feeder.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.RssContentProvider;
import com.nononsenseapps.feeder.util.PrefUtils;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, final String key) {
        switch (key) {
            case PrefUtils.PREF_SYNC_ONLY_CHARGING:
            case PrefUtils.PREF_SYNC_HOTSPOTS:
            case PrefUtils.PREF_SYNC_ONLY_WIFI:
                RssContentProvider.SetupSync(getActivity());
                break;
        }
    }
}
