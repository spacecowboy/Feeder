package com.nononsenseapps.feeder.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.nononsenseapps.feeder.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
