package com.nononsenseapps.feeder.ui.compose.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.util.ThemeOptions

val feederDarkColorPalette = darkColors(
    primary = Green700,
    primaryVariant = Green900,
    secondary = DarkTealA400
)

val feederLightColorPalette = lightColors(
    primary = Green700,
    primaryVariant = Green900,
    secondary = AccentDay
)

val keyline1Padding = 16.dp
val contentHorizontalPadding = 8.dp
val upButtonStartPadding = 4.dp

@Composable
fun FeederTheme(
    currentTheme: ThemeOptions = ThemeOptions.DAY,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = currentTheme.getColors(),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
