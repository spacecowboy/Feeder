package com.nononsenseapps.feeder.ui.compose.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal for [FeederTypography]. This is used to provide dynamic fonts to the app.
 */
val LocalFeederTypography: ProvidableCompositionLocal<FeederTypography> = compositionLocalOf { error("Missing FeederTypography!") }
