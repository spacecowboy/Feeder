package com.nononsenseapps.feeder.ui.compose.text

import coil.size.PixelSize
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
        assertEquals("http://foo/image.jpg", result.getBestImageForMaxSize(PixelSize(1, 1), 1.0f))
    }

    @Test
    fun findImageOnlySingleSrcSet() {
        every { element.attr("srcset") } returns "image.jpg"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)
        assertEquals("http://foo/image.jpg", result.getBestImageForMaxSize(PixelSize(1, 1), 1.0f))
    }

    @Test
    fun findImageBestMinSrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = PixelSize(1, 1)
        assertEquals("http://foo/header.png", result.getBestImageForMaxSize(maxSize, 1.0f))
    }

    @Test
    fun findImageBest640SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = PixelSize(640, 1)
        assertEquals("http://foo/header640.png", result.getBestImageForMaxSize(maxSize, 1.0f))
    }

    @Test
    fun findImageBest960SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = PixelSize(900, 1)
        assertEquals("http://foo/header960.png", result.getBestImageForMaxSize(maxSize, 8.0f))
    }

    @Test
    fun findImageBest650SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = PixelSize(650, 1)
        assertEquals("http://foo/header640.png", result.getBestImageForMaxSize(maxSize, 7.0f))
    }

    @Test
    fun findImageBest950SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = PixelSize(950, 1)
        assertEquals("http://foo/header960.png", result.getBestImageForMaxSize(maxSize, 7.0f))
    }

    @Test
    fun findImageBest1500SrcSet() {
        every { element.attr("srcset") } returns "header640.png 640w, header960.png 960w, header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = PixelSize(1500, 1)
        assertEquals("http://foo/header960.png", result.getBestImageForMaxSize(maxSize, 8.0f))
    }

    @Test
    fun findImageBest3xSrcSet() {
        every { element.attr("srcset") } returns "header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = PixelSize(1, 1)
        assertEquals("http://foo/header3.0x.png", result.getBestImageForMaxSize(maxSize, 3.0f))
    }

    @Test
    fun findImageBest1xSrcSet() {
        every { element.attr("srcset") } returns "header2x.png 2x, header3.0x.png 3.0x, header.png"
        every { element.attr("abs:src") } returns null

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = PixelSize(1, 1)
        assertEquals("http://foo/header.png", result.getBestImageForMaxSize(maxSize, 1.0f))
    }

    @Test
    fun findImageBestJunkSrcSet() {
        every { element.attr("srcset") } returns "header2x.png 2Y"
        every { element.attr("abs:src") } returns "http://foo/header.png"

        val result = getImageSource("http://foo", element)

        assertTrue(result.hasImage)

        val maxSize = PixelSize(1, 1)
        assertEquals("http://foo/header.png", result.getBestImageForMaxSize(maxSize, 1.0f))
    }
}
