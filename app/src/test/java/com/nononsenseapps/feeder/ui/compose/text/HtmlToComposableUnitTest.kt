package com.nononsenseapps.feeder.ui.compose.text

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.jsoup.nodes.Element
import org.junit.Test

class HtmlToComposableUnitTest {

    private val element = mockk<Element>()

    @Test
    fun findImageSrcWithNoSrc() {
        every { element.attr("srcset") } returns null
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertFalse(result.hasImage)
    }

    @Test
    fun findImageOnlySrc() {
        every { element.attr("srcset") } returns null
        every { element.attr("abs:src") } returns "http://foo/image.jpg"

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)
        assertEquals("http://foo/image.jpg", result.getBestImageForMaxSize(1, 1.0f))
    }

    @Test
    fun findImageOnlySingleSrcSet() {
        every { element.attr("srcset") } returns "image.jpg"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)
        assertEquals("http://foo/image.jpg", result.getBestImageForMaxSize(1, 1.0f))
    }

    @Test
    fun findImageBestMinSrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1
        assertEquals("http://foo/header.png", result.getBestImageForMaxSize(maxSize, 1.0f))
    }

    @Test
    fun findImageBest640SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 640
        assertEquals("http://foo/header640.png", result.getBestImageForMaxSize(maxSize, 1.0f))
    }

    @Test
    fun findImageBest960SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 900
        assertEquals("http://foo/header960.png", result.getBestImageForMaxSize(maxSize, 8.0f))
    }

    @Test
    fun findImageBest650SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 650
        assertEquals("http://foo/header640.png", result.getBestImageForMaxSize(maxSize, 7.0f))
    }

    @Test
    fun findImageBest950SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 950
        assertEquals("http://foo/header960.png", result.getBestImageForMaxSize(maxSize, 7.0f))
    }

    @Test
    fun findImageBest1500SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1500
        assertEquals("http://foo/header960.png", result.getBestImageForMaxSize(maxSize, 8.0f))
    }

    @Test
    fun findImageBest3xSrcSet() {
        every { element.attr("srcset") } returns "header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1
        assertEquals("http://foo/header3.0x.png", result.getBestImageForMaxSize(maxSize, 3.0f))
    }

    @Test
    fun findImageBest1xSrcSet() {
        every { element.attr("srcset") } returns "header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1
        assertEquals("http://foo/header.png", result.getBestImageForMaxSize(maxSize, 1.0f))
    }

    @Test
    fun findImageBestJunkSrcSet() {
        every { element.attr("srcset") } returns "header2x.png 2Y"
        every { element.attr("abs:src") } returns "http://foo/header.png"

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = 1
        assertEquals("http://foo/header.png", result.getBestImageForMaxSize(maxSize, 1.0f))
    }

    @Test
    fun findImageBestPoliticoSrcSet() {
        every { element.attr("srcset") } returns "https://www.politico.eu/cdn-cgi/image/width=1024,quality=80,onerror=redirect,format=auto/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd.jpeg 1024w, https://www.politico.eu/cdn-cgi/image/width=300,quality=80,onerror=redirect,format=auto/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd.jpeg 300w, https://www.politico.eu/cdn-cgi/image/width=1280,quality=80,onerror=redirect,format=auto/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd.jpeg 1280w"
        every { element.attr("abs:src") } returns "https://www.politico.eu/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd-1024x683.jpeg"
        every { element.attr("width") } returns "1024"
        every { element.attr("height") } returns "683"

        val result = getImageSource("https://www.politico.eu/feed/", element)

        assertTrue(result.hasImage)

        val maxSize = 1024
        assertEquals(
            "https://www.politico.eu/cdn-cgi/image/width=1024,quality=80,onerror=redirect,format=auto/wp-content/uploads/2022/10/07/thumbnail_Kal-econ-cartoon-10-7-22synd.jpeg",
            result.getBestImageForMaxSize(
                maxSize,
                8.0f
            )
        )
    }
}
