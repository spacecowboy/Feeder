package com.nononsenseapps.feeder.model.gofeed

import com.nononsenseapps.feeder.model.ImageFromHTML
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
}
