package com.nononsenseapps.feeder.ui.compose.readaloud

import com.nononsenseapps.feeder.model.SpeechSegment
import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.model.html.LinearText
import com.nononsenseapps.feeder.model.html.LinearTextBlockStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechHighlightTest {
    private fun text(value: String) = LinearText(emptySet(), value, emptyList(), LinearTextBlockStyle.TEXT)

    @Test fun repeatedParagraphsHighlightedOnlyTheSpokenOccurrence() {
        val first = text("Repeated sentence.")
        val second = text("Repeated sentence.")
        val source = "Repeated sentence.\n\nRepeated sentence."
        val highlights = SpeechHighlightIndex(LinearArticle(listOf(first, second)), source).highlights(SpeechSegment(20, source.length))
        assertFalse(highlights.containsKey(first))
        assertEquals(0..17, highlights[second])
    }

    @Test fun whitespaceDifferencesKeptOriginalCharacterOffsets() {
        val paragraph = text("First\u00a0\u00a0sentence. Second sentence.")
        val source = "First sentence. Second sentence."
        val highlights = SpeechHighlightIndex(LinearArticle(listOf(paragraph)), source).highlights(SpeechSegment(16, source.length))
        assertEquals("Second sentence.", paragraph.text.substring(highlights.getValue(paragraph)))
    }

    @Test fun unrelatedContentWasNotHighlighted() {
        val paragraph = text("Original article.")
        assertTrue(SpeechHighlightIndex(LinearArticle(listOf(paragraph)), "Translated article.").highlights(SpeechSegment(0, 19)).isEmpty())
    }

    @Test fun formattedMinutesAndHours() {
        assertEquals("0:00", formatSpeechTime(-1))
        assertEquals("3:05", formatSpeechTime(185000))
        assertEquals("1:01:01", formatSpeechTime(3661000))
    }
}
