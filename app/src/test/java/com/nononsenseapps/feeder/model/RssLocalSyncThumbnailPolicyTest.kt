package com.nononsenseapps.feeder.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RssLocalSyncThumbnailPolicyTest {
    @Test
    fun keepsFeedProvidedThumbnailEvenWhenOgExists() {
        val feedImage = MediaImage(url = "https://feed.example.com/feed.jpg")
        val ogImage = MediaImage(url = "https://site.example.com/og.jpg")

        val result = ThumbnailImagePolicy.applyOgImage(feedImage, ogImage)

        assertEquals(feedImage, result)
    }

    @Test
    fun upgradesBodyDerivedThumbnailToOgImageWhenAvailable() {
        val bodyImage = ImageFromHTML(url = "https://site.example.com/body.jpg")
        val ogImage = MediaImage(url = "https://site.example.com/og.jpg")

        val result = ThumbnailImagePolicy.applyOgImage(bodyImage, ogImage)

        assertEquals(ogImage, result)
    }

    @Test
    fun keepsBodyDerivedThumbnailWhenNoOgImage() {
        val bodyImage = ImageFromHTML(url = "https://site.example.com/body.jpg")

        val result = ThumbnailImagePolicy.applyOgImage(bodyImage, null)

        assertEquals(bodyImage, result)
    }

    @Test
    fun usesOgImageWhenCurrentIsMissing() {
        val ogImage = MediaImage(url = "https://site.example.com/og.jpg")

        val result = ThumbnailImagePolicy.applyOgImage(null, ogImage)

        assertEquals(ogImage, result)
    }

    @Test
    fun returnsNullWhenBothCurrentAndOgAreMissing() {
        val result = ThumbnailImagePolicy.applyOgImage(null, null)

        assertNull(result)
    }

    @Test
    fun keepsExistingNonBodyImageWhenParsedEntryOnlyHasBodyImage() {
        val existingImage = MediaImage(url = "https://site.example.com/og.jpg")
        val bodyImage = ImageFromHTML(url = "https://site.example.com/body.jpg")

        val result = ThumbnailImagePolicy.applyParsedEntryImage(existingImage, bodyImage)

        assertEquals(existingImage, result)
    }

    @Test
    fun takesIncomingFeedProvidedImageOverExistingNonBodyImage() {
        val existingImage = MediaImage(url = "https://site.example.com/old-og.jpg")
        val feedImage = MediaImage(url = "https://feed.example.com/feed.jpg")

        val result = ThumbnailImagePolicy.applyParsedEntryImage(existingImage, feedImage)

        assertEquals(feedImage, result)
    }

    @Test
    fun onlyFetchesOgImageForMissingOrBodyDerivedThumbnailsWithoutFeedImage() {
        val feedImage = MediaImage(url = "https://feed.example.com/feed.jpg")
        val bodyImage = ImageFromHTML(url = "https://site.example.com/body.jpg")

        assertEquals(false, ThumbnailImagePolicy.shouldFetchOgImage(feedImage, "https://example.com/article", hasFeedImage = false))
        assertEquals(true, ThumbnailImagePolicy.shouldFetchOgImage(bodyImage, "https://example.com/article", hasFeedImage = false))
        assertEquals(true, ThumbnailImagePolicy.shouldFetchOgImage(null, "https://example.com/article", hasFeedImage = false))
        assertEquals(false, ThumbnailImagePolicy.shouldFetchOgImage(bodyImage, null, hasFeedImage = false))
    }

    @Test
    fun skipsOgImageWhenFeedAlreadyProvidedAnyThumbnailCandidate() {
        val bodyImage = ImageFromHTML(url = "https://site.example.com/body.jpg")

        assertEquals(false, ThumbnailImagePolicy.shouldFetchOgImage(bodyImage, "https://example.com/article", hasFeedImage = true))
        assertEquals(false, ThumbnailImagePolicy.shouldFetchOgImage(null, "https://example.com/article", hasFeedImage = true))
    }
}
