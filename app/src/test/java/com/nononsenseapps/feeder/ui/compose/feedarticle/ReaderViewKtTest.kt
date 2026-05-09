package com.nononsenseapps.feeder.ui.compose.feedarticle

import com.nononsenseapps.feeder.model.ImageFromHTML
import com.nononsenseapps.feeder.model.MediaImage
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReaderViewKtTest {
    @Test
    fun headerImageIsHiddenWhenReaderIsNotShowingContent() {
        assertFalse(
            shouldShowHeaderImage(
                showHeaderImage = false,
                image = MediaImage("https://example.com/hero.jpg"),
                contentImageUrls = emptySet(),
            ),
        )
    }

    @Test
    fun bodyDerivedImageIsNotShownInHeader() {
        assertFalse(
            shouldShowHeaderImage(
                showHeaderImage = true,
                image = ImageFromHTML("https://example.com/hero.jpg"),
                contentImageUrls = emptySet(),
            ),
        )
    }

    @Test
    fun nonBodyImageIsHiddenWhenAlreadyRenderedInline() {
        assertFalse(
            shouldShowHeaderImage(
                showHeaderImage = true,
                image = MediaImage("https://example.com/hero.jpg"),
                contentImageUrls = setOf("https://example.com/hero.jpg"),
            ),
        )
    }

    @Test
    fun nonBodyImageIsShownWhenNotRenderedInline() {
        assertTrue(
            shouldShowHeaderImage(
                showHeaderImage = true,
                image = MediaImage("https://example.com/hero.jpg"),
                contentImageUrls = setOf("https://example.com/other.jpg"),
            ),
        )
    }
}
