package com.nononsenseapps.feeder.ui.compose.utils

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator

/**
 * Opinionated set of viewport breakpoints
 *     - CompactTall: Most phones in portrait mode
 *     - CompactShort: Most phones in landscape - and small phones in portrait too
 *     - Medium: Most foldables and tablets in portrait mode
 *     - Expanded: Most tablets in landscape mode
 *
 * More info: https://material.io/archive/guidelines/layout/responsive-ui.html
 */
enum class WindowSize {
    CompactTall,
    CompactShort,
    Medium,
    Expanded
}

val localWindowSize: ProvidableCompositionLocal<WindowSize> =
    compositionLocalOf { error("Missing WindowSize container!") }

@Composable
fun LocalWindowSize(): WindowSize = localWindowSize.current

@Composable
fun Activity.withWindowSize(content: @Composable () -> Unit) {
    // Get the size (in pixels) of the window
    val windowSize = rememberWindowSize()

    // Convert the window size to [Dp]
    val windowDpSize = with(LocalDensity.current) {
        windowSize.toDpSize()
    }

    // Calculate the window size class
    val windowSizeclass = getWindowSizeClass(windowDpSize)

    CompositionLocalProvider(localWindowSize provides windowSizeclass) {
        content()
    }
}

/**
 * Remembers the [WindowSize] class for the window corresponding to the current window metrics.
 */
@Composable
fun Activity.rememberWindowSizeClass(): WindowSize {
    // Get the size (in pixels) of the window
    val windowSize = rememberWindowSize()

    // Convert the window size to [Dp]
    val windowDpSize = with(LocalDensity.current) {
        windowSize.toDpSize()
    }

    // Calculate the window size class
    return getWindowSizeClass(windowDpSize)
}

/**
 * Remembers the [Size] in pixels of the window corresponding to the current window metrics.
 */
@Composable
private fun Activity.rememberWindowSize(): Size {
    val configuration = LocalConfiguration.current
    // WindowMetricsCalculator implicitly depends on the configuration through the activity,
    // so re-calculate it upon changes.
    val windowMetrics = remember(configuration) {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    }
    return windowMetrics.bounds.toComposeRect().size
}

// TODO experiment and see what is reasonable. Pixel emultor is 390 x 850
private val shortHeightLimit = 500.dp

/**
 * Partitions a [DpSize] into a enumerated [WindowSize] class.
 */
@VisibleForTesting
fun getWindowSizeClass(windowDpSize: DpSize): WindowSize = when {
    windowDpSize.width < 0.dp -> throw IllegalArgumentException("Dp value cannot be negative")
    windowDpSize.width < 600.dp -> {
        when {
            windowDpSize.height < shortHeightLimit -> WindowSize.CompactShort
            else -> WindowSize.CompactTall
        }
    }
    windowDpSize.width < 840.dp -> {
        WindowSize.Medium
    }
    else -> WindowSize.Expanded
}

enum class ScreenType {
    DUAL,
    SINGLE,
}

fun getScreenType(windowSize: WindowSize) =
    when (windowSize) {
        WindowSize.CompactTall -> ScreenType.SINGLE
        WindowSize.CompactShort, WindowSize.Medium, WindowSize.Expanded -> ScreenType.DUAL
    }
