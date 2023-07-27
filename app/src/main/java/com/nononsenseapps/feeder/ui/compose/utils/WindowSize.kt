package com.nononsenseapps.feeder.ui.compose.utils

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

val LocalWindowSize: ProvidableCompositionLocal<WindowSizeClass> =
    compositionLocalOf { error("Missing WindowSize container!") }

@Composable
fun LocalWindowSize(): WindowSizeClass = LocalWindowSize.current

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Activity.withWindowSize(content: @Composable () -> Unit) {
    val windowSizeclass = calculateWindowSizeClass(activity = this)

    CompositionLocalProvider(LocalWindowSize provides windowSizeclass) {
        content()
    }
}

@Composable
fun isCompactLandscape(): Boolean {
    return LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE &&
        LocalWindowSize.current.heightSizeClass == WindowHeightSizeClass.Compact
}

@Composable
fun isCompactDevice(): Boolean {
    val windowSize = LocalWindowSize.current
    return windowSize.heightSizeClass == WindowHeightSizeClass.Compact ||
        windowSize.widthSizeClass == WindowWidthSizeClass.Compact
}

enum class ScreenType {
    DUAL,
    SINGLE,
}

fun getScreenType(windowSize: WindowSizeClass) =
    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> ScreenType.SINGLE
        else -> ScreenType.DUAL
    }
