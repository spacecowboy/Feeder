package com.nononsenseapps.feeder.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.getSettingsViewModel

class SettingsActivity : AppCompatActivity() {

    private val settingsViewModel by lazy { getSettingsViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        settingsViewModel.liveThemePreference.observe(this, androidx.lifecycle.Observer {
            delegate.setLocalNightMode(it)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (android.R.id.home == item.itemId) {
            NavUtils.navigateUpFromSameTask(this)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
