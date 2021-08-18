package com.nononsenseapps.feeder.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.model.ApplicationState
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import org.kodein.di.compose.withDI
import org.kodein.di.instance

/**
 * Should only be opened from the MANAGE SETTINGS INTENT
 */
class ManageSettingsActivity : DIAwareComponentActivity() {
    private val applicationState: ApplicationState by instance()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            withDI {
                val settingsViewModel: SettingsViewModel = DIAwareViewModel()

                val currentTheme by settingsViewModel.currentTheme.collectAsState()

                FeederTheme(
                    currentTheme = currentTheme
                ) {
                    SettingsScreen(
                        onNavigateUp = {
                            onNavigateUpFromIntentActivities()
                        },
                        settingsViewModel = settingsViewModel,
                        applicationState = applicationState
                    )
                }
            }
        }
    }
}
