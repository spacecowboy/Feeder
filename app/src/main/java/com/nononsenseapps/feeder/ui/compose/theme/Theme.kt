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
import com.nononsenseapps.feeder.archmodel.DarkThemePreferences
import com.nononsenseapps.feeder.archmodel.ThemeOptions

@Composable
fun FeederBlackColorPalette() = darkColors(
    primary = Green700,
    primaryVariant = Green900,
    secondary = DarkTealA400,
    onSecondary = Color.White,
    background = Color.Black,
)

@Composable
fun FeederDarkColorPalette() = darkColors(
    primary = Green700,
    primaryVariant = Green900,
    secondary = DarkTealA400,
    onSecondary = Color.White,
    background = DarkBackground,
)

@Composable
fun FeederLightColorPalette() = lightColors(
    primary = Green700,
    primaryVariant = Green900,
    secondary = AccentDay,
    onSecondary = Color.White,
)

@Deprecated(
    "Use dimens.margin instead",
    ReplaceWith(
        "LocalDimens.current.margin",
        "com.nononsenseapps.feeder.ui.compose.theme.LocalDimens"
    )
)
val keyline1Padding = 16.dp

/**
 * Only use this in the root of the activity
 */
@Composable
fun FeederTheme(
    currentTheme: ThemeOptions = ThemeOptions.DAY,
    darkThemePreference: DarkThemePreferences = DarkThemePreferences.BLACK,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = currentTheme.getColors(darkThemePreference),
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
private fun ThemeOptions.getColors(darkThemePreference: DarkThemePreferences): Colors =
    when (this) {
        ThemeOptions.DAY -> FeederLightColorPalette()
        ThemeOptions.NIGHT -> getPreferredDarkTheme(darkThemePreference)
        ThemeOptions.SYSTEM -> {
            if (isSystemInDarkTheme()) {
                getPreferredDarkTheme(darkThemePreference)
            } else {
                FeederLightColorPalette()
            }
        }
    }

@Composable
private fun getPreferredDarkTheme(darkThemePreference: DarkThemePreferences): Colors {
    return when (darkThemePreference) {
        DarkThemePreferences.BLACK -> FeederBlackColorPalette()
        DarkThemePreferences.DARK -> FeederDarkColorPalette()
    }
}
