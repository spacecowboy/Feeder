package com.nononsenseapps.feeder.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nononsenseapps.feeder.archmodel.ThemeOptions

@Composable
fun FeederDarkColorPalette() = darkColors(
    primary = Green700,
    primaryVariant = Green900,
    secondary = DarkTealA400,
    onSecondary = Color.White,
)

@Composable
fun FeederLightColorPalette() = lightColors(
    primary = Green700,
    primaryVariant = Green900,
    secondary = AccentDay,
    onSecondary = Color.White,
)

@Deprecated("")
val keyline1Padding = 16.dp
@Deprecated("")
val contentHorizontalPadding = 8.dp
@Deprecated("")
val upButtonStartPadding = 4.dp

/**
 * Only use this in the root of the activity
 */
@Composable
fun FeederTheme(
    currentTheme: ThemeOptions = ThemeOptions.DAY,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = currentTheme.getColors(),
        typography = Typography,
        shapes = Shapes
    ) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setSystemBarsColor(
                Color.Transparent
            )
        }
        ProvideWindowInsets {
            ProvideDimens {
                content()
            }
        }
    }
}

@Composable
private fun ThemeOptions.getColors(): Colors =
    when (this) {
        ThemeOptions.DAY -> FeederLightColorPalette()
        ThemeOptions.NIGHT -> FeederDarkColorPalette()
        ThemeOptions.SYSTEM -> {
            if (isSystemInDarkTheme()) {
                FeederDarkColorPalette()
            } else {
                FeederLightColorPalette()
            }
        }
    }
