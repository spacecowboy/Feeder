package com.nononsenseapps.feeder.ui.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.diAwareViewModel
import com.nononsenseapps.feeder.ui.CommonActivityViewModel
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.ProvideFontScale
import org.kodein.di.compose.withDI

@Composable
fun DIAwareComponentActivity.withAllProviders(
    content: @Composable () -> Unit,
) {
    withDI {
        val viewModel: CommonActivityViewModel = diAwareViewModel()
        val currentTheme by viewModel.currentTheme.collectAsState()
        val darkThemePreference by viewModel.darkThemePreference.collectAsState()
        val dynamicColors by viewModel.dynamicColors.collectAsState()
        val textScale by viewModel.textScale.collectAsState()
        FeederTheme(
            currentTheme = currentTheme,
            darkThemePreference = darkThemePreference,
            dynamicColors = dynamicColors,
        ) {
            withWindowSize {
                ProvideFontScale(fontScale = textScale) {
                    content()
                }
            }
        }
    }
}
