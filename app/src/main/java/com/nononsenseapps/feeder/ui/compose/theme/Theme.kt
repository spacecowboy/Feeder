package com.nononsenseapps.feeder.ui.compose.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.nononsenseapps.feeder.util.CurrentTheme

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

@Composable
fun FeederTheme(currentTheme: CurrentTheme, content: @Composable () -> Unit) {
    MaterialTheme(
        colors = currentTheme.getColors(),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
