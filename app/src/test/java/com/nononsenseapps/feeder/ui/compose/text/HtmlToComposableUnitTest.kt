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
                8.0f,
            ),
        )
    }

    @Test
    fun findImageForTheVerge() {
        /*
        <img alt="A pen pointing to a piece of LK-99 standing on its side above a magnet." sizes="(max-width: 768px) calc(100vw - 100px), (max-width: 1180px) 700px, 600px" srcSet="https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/16x11/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 16w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/32x21/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 32w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/48x32/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 48w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/64x43/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 64w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/96x64/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 96w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/128x85/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 128w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/256x171/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 256w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/376x251/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 376w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/384x256/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 384w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/415x277/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 415w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/480x320/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 480w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/540x360/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 540w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/640x427/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 640w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/750x500/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 750w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/828x552/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 828w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1080x720/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1080w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1200x800/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1200w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1440x960/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1440w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1920x1280/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1920w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2048x1365/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 2048w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2400x1600/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 2400w" src="https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2400x1600/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png" decoding="async" data-nimg="responsive" style="position:absolute;top:0;left:0;bottom:0;right:0;box-sizing:border-box;padding:0;border:none;margin:auto;display:block;width:0;height:0;min-width:100%;max-width:100%;min-height:100%;max-height:100%;object-fit:cover"/>
         */

        every { element.attr("srcset") } returns "https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/16x11/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 16w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/32x21/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 32w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/48x32/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 48w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/64x43/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 64w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/96x64/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 96w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/128x85/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 128w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/256x171/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 256w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/376x251/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 376w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/384x256/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 384w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/415x277/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 415w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/480x320/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 480w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/540x360/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 540w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/640x427/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 640w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/750x500/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 750w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/828x552/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 828w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1080x720/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1080w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1200x800/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1200w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1440x960/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1440w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1920x1280/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 1920w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2048x1365/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 2048w, https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2400x1600/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png 2400w"
        every { element.attr("abs:src") } returns "https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/2400x1600/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png"
        every { element.attr("width") } returns null
        every { element.attr("height") } returns null

        val result = getImageSource("https://www.politico.eu/feed/", element)

        assertTrue(result.hasImage)

        val maxSize = 1024
        assertEquals(
            "https://duet-cdn.vox-cdn.com/thumbor/184x0:2614x1535/1080x720/filters:focal(1847x240:1848x241):format(webp)/cdn.vox-cdn.com/uploads/chorus_asset/file/24842461/Screenshot_2023_08_10_at_12.22.58_PM.png",
            result.getBestImageForMaxSize(
                maxSize,
                8.0f,
            ),
        )
    }

    @Test
    fun noSourcesMeansEmptyResult() {
        every { element.attr("srcset") } returns ""
        every { element.attr("abs:src") } returns ""
        every { element.attr("width") } returns null
        every { element.attr("height") } returns null

        val result = getImageSource("https://www.politico.eu/feed/", element)

        assertFalse(result.hasImage)

        val maxSize = 1024
        assertEquals(
            "",
            result.getBestImageForMaxSize(
                maxSize,
                8.0f,
            ),
        )
    }
}
