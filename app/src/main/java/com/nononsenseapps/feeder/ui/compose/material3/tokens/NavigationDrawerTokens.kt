package com.nononsenseapps.feeder.ui.compose.material3.tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object NavigationDrawerTokens {
    val ActiveFocusIconColor = ColorSchemeKeyTokens.OnSecondaryContainer
    val ActiveFocusLabelTextColor = ColorSchemeKeyTokens.OnSecondaryContainer
    val ActiveHoverIconColor = ColorSchemeKeyTokens.OnSecondaryContainer
    val ActiveHoverLabelTextColor = ColorSchemeKeyTokens.OnSecondaryContainer
    val ActiveIconColor = ColorSchemeKeyTokens.OnSecondaryContainer
    val ActiveIndicatorColor = ColorSchemeKeyTokens.SecondaryContainer
    val ActiveIndicatorHeight = 56.0.dp
    val ActiveIndicatorShape = ShapeKeyTokens.CornerFull
    val ActiveIndicatorWidth = 336.0.dp
    val ActiveLabelTextColor = ColorSchemeKeyTokens.OnSecondaryContainer
    val ActivePressedIconColor = ColorSchemeKeyTokens.OnSecondaryContainer
    val ActivePressedLabelTextColor = ColorSchemeKeyTokens.OnSecondaryContainer
    val BottomContainerShape = ShapeKeyTokens.CornerLargeTop
    val ContainerColor = ColorSchemeKeyTokens.Surface
    const val ContainerHeightPercent = 100.0f
    val ContainerShape = ShapeKeyTokens.CornerLargeEnd
    val ContainerSurfaceTintLayerColor = ColorSchemeKeyTokens.SurfaceTint
    private val ContainerWidth = 360.0.dp
    val HeadlineColor = ColorSchemeKeyTokens.OnSurfaceVariant
    val HeadlineFont = TypographyKeyTokens.TitleSmall
    val IconSize = 24.0.dp
    val InactiveFocusIconColor = ColorSchemeKeyTokens.OnSurface
    val InactiveFocusLabelTextColor = ColorSchemeKeyTokens.OnSurface
    val InactiveHoverIconColor = ColorSchemeKeyTokens.OnSurface
    val InactiveHoverLabelTextColor = ColorSchemeKeyTokens.OnSurface
    val InactiveIconColor = ColorSchemeKeyTokens.OnSurfaceVariant
    val InactiveLabelTextColor = ColorSchemeKeyTokens.OnSurfaceVariant
    val InactivePressedIconColor = ColorSchemeKeyTokens.OnSurface
    val InactivePressedLabelTextColor = ColorSchemeKeyTokens.OnSurface
    val LabelTextFont = TypographyKeyTokens.LabelLarge
    val LargeBadgeLabelColor = ColorSchemeKeyTokens.OnSurfaceVariant
    val LargeBadgeLabelFont = TypographyKeyTokens.LabelLarge
    val ModalContainerElevation = ElevationTokens.Level1
    val StandardContainerElevation = ElevationTokens.Level0

    @Composable
    fun getContainerWidth(): Dp {
        val configuration = LocalConfiguration.current
        return minOf(ContainerWidth, configuration.screenWidthDp.dp)
    }
}
