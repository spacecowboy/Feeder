package com.nononsenseapps.feeder.ui.compose.text

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.text.Bidi

@Composable
inline fun withBidiDeterminedLayoutDirection(
    paragraph: String,
    crossinline content: @Composable () -> Unit,
) {
    val bidi = Bidi(paragraph, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)

    Log.d(
        "JCOMPOSE",
        "base: ${bidi.baseLevel}, rtl: ${bidi.isRightToLeft}, mixed ${bidi.isMixed}"
    )

    if (bidi.baseIsLeftToRight()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            content()
        }
    } else {
        Log.d("JCOMPOSE", "RTL provider")
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
