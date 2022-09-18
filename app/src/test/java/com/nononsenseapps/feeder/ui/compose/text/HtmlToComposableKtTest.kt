package com.nononsenseapps.feeder.ui.compose.text

import kotlin.test.assertEquals
import org.junit.Test

class HtmlToComposableKtTest {
    @Test
    fun htmlIsStrippedFromAlt() {
        assertEquals(
            "Hello there",
            stripHtml("<em>Hello</em> there")
        )
    }
}
