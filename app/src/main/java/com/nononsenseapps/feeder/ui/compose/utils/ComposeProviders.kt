package com.nononsenseapps.feeder.ui.compose.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import com.nononsenseapps.feeder.archmodel.DarkThemePreferences
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.ProvideFontScale
import org.kodein.di.compose.withDI

@Composable
fun Activity.withAllProviders(
    currentTheme: ThemeOptions,
    darkThemePreference: DarkThemePreferences,
    dynamicColors: Boolean,
    textScale: Float,
    content: @Composable () -> Unit,
) {
    FeederTheme(
        currentTheme = currentTheme,
        darkThemePreference = darkThemePreference,
        dynamicColors = dynamicColors,
    ) {
        withDI {
            withWindowSize {
                ProvideFontScale(fontScale = textScale) {
                    content()
                }
            }
        }
    }
}
