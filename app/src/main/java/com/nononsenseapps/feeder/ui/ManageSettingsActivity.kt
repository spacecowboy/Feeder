package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.ui.compose.navigation.SyncScreenDestination
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import org.kodein.di.compose.withDI

/**
 * Should only be opened from the MANAGE SETTINGS INTENT
 */
class ManageSettingsActivity : DIAwareComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            withDI {
                val manageSettingsViewModel: ManageSettingsViewModel = DIAwareViewModel()
                val currentTheme by manageSettingsViewModel.currentTheme.collectAsState()
                val darkThemePreference by manageSettingsViewModel.darkThemePreference.collectAsState()

                FeederTheme(
                    currentTheme = currentTheme,
                    darkThemePreference = darkThemePreference
                ) {
                    SettingsScreen(
                        onNavigateUp = {
                            onNavigateUpFromIntentActivities()
                        },
                        onNavigateToSyncScreen = {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(SyncScreenDestination.deepLinks.first().uriPattern),
                                    this,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        },
                        settingsViewModel = DIAwareViewModel(),
                    )
                }
            }
        }
    }
}
