package com.nononsenseapps.feeder.ui.compose.feedarticle

import com.nononsenseapps.feeder.archmodel.Enclosure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PodcastPlayerLinksTest {
    @Test
    fun detectedAudioLinksByExtension() {
        assertTrue(shouldOpenInPodcastPlayer("https://example.com/episode.mp3?source=rss", Enclosure()))
        assertTrue(shouldOpenInPodcastPlayer("https://example.com/episode.m4a", Enclosure()))
    }

    @Test
    fun detectedAudioLinksByEnclosureMimeType() {
        val enclosure =
            Enclosure(
                present = true,
                link = "https://example.com/download",
                type = "audio/mpeg",
            )

        assertTrue(shouldOpenInPodcastPlayer(enclosure.link, enclosure))
    }

    @Test
    fun ignoredNonAudioLinks() {
        assertFalse(shouldOpenInPodcastPlayer("https://example.com/article", Enclosure()))
        assertFalse(shouldOpenInPodcastPlayer("#chapter-2", Enclosure()))
    }

    @Test
    fun derivedAudioTitleFromLink() {
        assertEquals(
            "Coding Blocks Episode 1.mp3",
            audioTitleFromLink("https://example.com/Coding%20Blocks%20Episode%201.mp3"),
        )
    }
}
