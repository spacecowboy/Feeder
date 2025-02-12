package com.nononsenseapps.feeder.ui.compose.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.text.Bidi

@Composable
fun WithBidiDeterminedLayoutDirection(
    paragraph: String,
    content: @Composable () -> Unit,
) {
    val bidi =
        remember(paragraph) {
            Bidi(paragraph, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)
        }

    if (bidi.baseIsLeftToRight()) {
        LeftToRight(content)
    } else {
        RightToLeft(content)
    }
}

@Composable
fun LeftToRight(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        content()
    }
}

@Composable
fun RightToLeft(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        content()
    }
}
