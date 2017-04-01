package com.nononsenseapps.feeder.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.nononsenseapps.feeder.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            NavUtils.navigateUpFromSameTask(this);
            //finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
