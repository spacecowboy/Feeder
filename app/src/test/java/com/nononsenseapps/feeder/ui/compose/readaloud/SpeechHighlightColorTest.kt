package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechHighlightColorTest {
    @Test fun neutralHighlightPreservedBodyAndLinkContrastInBothThemes() {
        for (scheme in listOf(lightColorScheme(), darkColorScheme(), darkColorScheme(surface = Color.Black))) {
            val highlight = speechHighlightColor(scheme.surface, scheme.onSurface, scheme.primary)
            assertTrue(highlight.alpha > 0f && highlight.alpha <= 0.15f)
            assertTrue(highlight.red == highlight.green && highlight.green == highlight.blue)
            val background = highlight.compositeOver(scheme.surface)
            for (text in listOf(scheme.onSurface, scheme.primary)) {
                val a = text.luminance()
                val b = background.luminance()
                assertTrue((maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f) >= 4.5f)
            }
        }
    }
}
