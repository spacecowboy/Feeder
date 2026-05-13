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
