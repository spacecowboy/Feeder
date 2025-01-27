package com.nononsenseapps.feeder.ui.compose.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.text.Bidi

@Composable
inline fun WithBidiDeterminedLayoutDirection(
    paragraph: String,
    crossinline content: @Composable () -> Unit,
) {
    val bidi =
        remember(paragraph) {
            Bidi(paragraph, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)
        }

    val content = remember { movableContentOf { content() } }

    if (bidi.baseIsLeftToRight()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            content()
        }
    } else {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
