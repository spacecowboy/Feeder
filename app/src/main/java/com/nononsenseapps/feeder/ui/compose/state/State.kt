package com.nononsenseapps.feeder.ui.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.SettingsViewModel

@Composable
fun getImagePlaceholder(settingsViewModel: SettingsViewModel): State<Int> {
    val currentTheme by settingsViewModel.currentTheme.collectAsState()
    val isDarkTheme = currentTheme.isDarkTheme()

    return remember(isDarkTheme) {
        derivedStateOf {
            if (isDarkTheme) {
                R.drawable.placeholder_image_article_night
            } else {
                R.drawable.placeholder_image_article_day
            }
        }
    }
}
