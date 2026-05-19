package com.nononsenseapps.feeder.model.gofeed

import com.nononsenseapps.feeder.model.ImageFromHTML
import com.nononsenseapps.feeder.model.MediaImage
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals

class GoFeedExtensionsKtTest {
    private val baseUrl = URL("http://test.com")

    // Essentially a test for XKCD
    @Test
    fun descriptionWithOnlyImageDoesNotReturnBlankSummaryAndGetsImageSet() {
        val expectedSummary = "[An image]"
        val html = "  <img src='http://google.com/image.png' alt='An image'/> "

        val item =
            FeederGoItem(
                goItem =
                    makeGoItem(
                        guid = "$baseUrl/id",
                        title = "",
                        content = html,
                        description = html,
                    ),
                feedBaseUrl = baseUrl,
                feedAuthor = null,
            )

        assertEquals(
            item.thumbnail,
            ImageFromHTML(url = "http://google.com/image.png", width = null, height = null),
        )

        assertEquals(expectedSummary, item.snippet)
    }

    @Test
    fun linkFallsBackToGuidWhenGuidIsUrl() {
        val item =
            FeederGoItem(
                goItem =
                    makeGoItem(
                        guid = "https://example.com/article/123",
                        link = null,
                    ),
                feedBaseUrl = baseUrl,
                feedAuthor = null,
            )

        assertEquals("https://example.com/article/123", item.link)
    }

    @Test
    fun linkDoesNotFallBackToGuidWhenGuidIsNonHttpUri() {
        val item =
            FeederGoItem(
                goItem =
                    makeGoItem(
                        guid = "urn:uuid:some-non-url-id",
                        link = null,
                    ),
                feedBaseUrl = baseUrl,
                feedAuthor = null,
            )

        assertEquals(null, item.link)
    }

    @Test
    fun linkDoesNotFallBackToGuidWhenGuidIsInvalidUri() {
        // Double ## makes the fragment contain a bare '#', which is invalid in RFC 3986
        val item =
            FeederGoItem(
                goItem =
                    makeGoItem(
                        guid = "http://example.com/sub/##",
                        link = null,
                    ),
                feedBaseUrl = baseUrl,
                feedAuthor = null,
            )

        assertEquals(null, item.link)
    }

    @Test
    fun linkIsPreferredOverGuidWhenBothPresent() {
        val item =
            FeederGoItem(
                goItem =
                    makeGoItem(
                        guid = "https://example.com/guid",
                        link = "https://example.com/link",
                    ),
                feedBaseUrl = baseUrl,
                feedAuthor = null,
            )

        assertEquals("https://example.com/link", item.link)
    }

    @Test
    fun feedThumbnailCandidateIsPreservedEvenWhenBodyFallbackIsChosen() {
        val imageUrl = "https://example.com/feed.jpg"
        val html = "<img src='$imageUrl' alt='Article image'/>"
        val item =
            FeederGoItem(
                goItem =
                    makeGoItem(
                        guid = "$baseUrl/id",
                        content = html,
                        description = html,
                        extensions =
                            mapOf(
                                "media" to
                                    mapOf(
                                        "content" to
                                            listOf(
                                                GoExtension(
                                                    name = "content",
                                                    value = null,
                                                    attrs = mapOf("url" to imageUrl),
                                                    children = null,
                                                ),
                                            ),
                                    ),
                            ),
                    ),
                feedBaseUrl = baseUrl,
                feedAuthor = null,
            )

        assertEquals(MediaImage(url = imageUrl), item.feedThumbnail)
        assertEquals(ImageFromHTML(url = imageUrl), item.thumbnail)
    }
}
