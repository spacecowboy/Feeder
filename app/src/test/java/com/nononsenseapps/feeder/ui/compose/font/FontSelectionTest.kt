package com.nononsenseapps.feeder.ui.compose.font

import com.nononsenseapps.feeder.ui.compose.font.FontSelection.SystemDefault
import org.junit.Assert.*
import org.junit.Test

class FontSelectionTest {
    @Test
    fun testDeserializeGarbage() {
        val serialized = "foo"
        assertEquals(FontSelection.SystemDefault, FontSelection.fromString(serialized))
    }


    @Test
    fun testSystemSerialization() {
        val font = SystemDefault
        val serialized = font.serialize()
        assertEquals(font, FontSelection.fromString(serialized))
    }

    @Test
    fun testRobotoSerialization() {
        val font = FontSelection.Roboto
        val serialized = font.serialize()
        assertEquals(font, FontSelection.fromString(serialized))
    }

    @Test
    fun testAtkinsonSerialization() {
        val font = FontSelection.AtkinsonHyperLegible
        val serialized = font.serialize()
        assertEquals(font, FontSelection.fromString(serialized))
    }

    @Test
    fun testUserFontSerialization() {
        val font = FontSelection.UserFont(
            path = "user/font/path",
            hasWeightVariation = true,
            hasItalicVariation = false,
        )
        val serialized = font.serialize()
        assertEquals(font, FontSelection.fromString(serialized))
    }
}
