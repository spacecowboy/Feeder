package com.nononsenseapps.feeder.ui.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nononsenseapps.feeder.archmodel.DarkThemePreferences
import com.nononsenseapps.feeder.archmodel.ThemeOptions

private val lightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
)

private val darkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
)

/**
 * Only use this in the root of the activity
 */
@Composable
fun FeederTheme(
    currentTheme: ThemeOptions = ThemeOptions.DAY,
    darkThemePreference: DarkThemePreferences = DarkThemePreferences.BLACK,
    content: @Composable () -> Unit,
) {
    val darkIcons = currentTheme.isDarkSystemIcons()
    val colorScheme = currentTheme.getColorScheme(darkThemePreference)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setStatusBarColor(
                Color.Transparent,
                darkIcons = false,
            )
            systemUiController.setNavigationBarColor(
                colorScheme.scrim,
//                TODO
//                if (darkIcons) NavBarScrimLight else NavBarScrimDark,
                darkIcons = darkIcons,
            )
        }
        ProvideDimens {
            content()
        }
    }
}

@Composable
private fun ThemeOptions.isDarkSystemIcons(): Boolean {
    val isDarkTheme = when (this) {
        ThemeOptions.DAY -> false
        ThemeOptions.NIGHT -> true
        ThemeOptions.SYSTEM -> isSystemInDarkTheme()
    }

    // Only Api 27+ supports dark nav bar icons
    return (Build.VERSION.SDK_INT >= 27) && !isDarkTheme
}

@Composable
private fun ThemeOptions.getColorScheme(darkThemePreference: DarkThemePreferences): ColorScheme =
    when (this) {
        ThemeOptions.DAY -> lightColors
        ThemeOptions.NIGHT -> getPreferredDarkColorScheme(darkThemePreference)
        ThemeOptions.SYSTEM -> {
            if (isSystemInDarkTheme()) {
                getPreferredDarkColorScheme(darkThemePreference)
            } else {
                lightColors
            }
        }
    }

@Composable
private fun getPreferredDarkColorScheme(darkThemePreference: DarkThemePreferences): ColorScheme {
    return when (darkThemePreference) {
        // TODO
        DarkThemePreferences.BLACK -> darkColors
        DarkThemePreferences.DARK -> darkColors
    }
}
