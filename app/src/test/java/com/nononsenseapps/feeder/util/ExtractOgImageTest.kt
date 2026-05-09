package com.nononsenseapps.feeder.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExtractOgImageTest {
    @Test
    fun extractsOgImageFromPropertyAttribute() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="https://example.com/image.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/image.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun extractsOgImageFromNameAttribute() {
        val html =
            """
            <html><head>
            <meta name="og:image" content="https://example.com/image.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/image.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun prefersPropertyOverNameAttribute() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="https://example.com/property.jpg">
            <meta name="og:image" content="https://example.com/name.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/property.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun fallsBackToTwitterImageWhenNoOgImage() {
        val html =
            """
            <html><head>
            <meta property="twitter:image" content="https://example.com/twitter.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/twitter.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun fallsBackToTwitterImageFromNameAttribute() {
        val html =
            """
            <html><head>
            <meta name="twitter:image" content="https://example.com/twitter-name.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/twitter-name.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun prefersOgImageOverTwitterImage() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="https://example.com/og.jpg">
            <meta property="twitter:image" content="https://example.com/twitter.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/og.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun firstOgImageWinsWhenMultiplePresent() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="https://example.com/first.jpg">
            <meta property="og:image" content="https://example.com/second.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/first.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun resolvesRelativeUrlAgainstBaseUrl() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="/images/thumb.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/images/thumb.jpg",
            extractOgImage(html, "https://example.com/article/123")?.url,
        )
    }

    @Test
    fun resolvesProtocolRelativeUrlAgainstBaseUrl() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="//cdn.example.com/image.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://cdn.example.com/image.jpg",
            extractOgImage(html, "https://example.com/article")?.url,
        )
    }

    @Test
    fun returnsNullWhenNoMetaTagsPresent() {
        val html =
            """
            <html><head><title>No image here</title></head><body></body></html>
            """.trimIndent()

        assertNull(extractOgImage(html, "https://example.com/"))
    }

    @Test
    fun ignoresBlankOgImageContent() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="   ">
            </head><body></body></html>
            """.trimIndent()

        assertNull(extractOgImage(html, "https://example.com/"))
    }

    @Test
    fun ignoresEmptyOgImageContent() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="">
            </head><body></body></html>
            """.trimIndent()

        assertNull(extractOgImage(html, "https://example.com/"))
    }

    @Test
    fun returnsNullWhenContentAttributeMissing() {
        val html =
            """
            <html><head>
            <meta property="og:image">
            </head><body></body></html>
            """.trimIndent()

        assertNull(extractOgImage(html, "https://example.com/"))
    }

    @Test
    fun blankOgImageFallsBackToTwitterImage() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="">
            <meta property="twitter:image" content="https://example.com/twitter.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/twitter.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun handlesUpperCaseMetaTag() {
        val html =
            """
            <html><head>
            <META PROPERTY="og:image" CONTENT="https://example.com/image.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/image.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun matchesAttributeValueCaseInsensitively() {
        val html =
            """
            <html><head>
            <meta property="og:Image" content="https://example.com/image.jpg">
            </head><body></body></html>
            """.trimIndent()

        assertEquals(
            "https://example.com/image.jpg",
            extractOgImage(html, "https://example.com/")?.url,
        )
    }

    @Test
    fun returnedImageIsNotFromBody() {
        val html =
            """
            <html><head>
            <meta property="og:image" content="https://example.com/image.jpg">
            </head><body></body></html>
            """.trimIndent()

        val result = assertNotNull(extractOgImage(html, "https://example.com/"))

        assertEquals("https://example.com/image.jpg", result.url)
        assertNull(result.width)
        assertNull(result.height)
        assertFalse(result.fromBody)
    }

    @Test
    fun returnsNullForEmptyHtml() {
        assertNull(extractOgImage("", "https://example.com/"))
    }

    @Test
    fun returnsNullForMinimalHtmlWithNoHead() {
        assertNull(extractOgImage("<html><body>Hello</body></html>", "https://example.com/"))
    }

    @Test
    fun extractsOgImageFromRealisticPage() {
        val html =
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <title>Test Article</title>
                <meta property="og:title" content="Test Article">
                <meta property="og:description" content="A description">
                <meta property="og:image" content="https://cdn.example.com/photos/2024/article-hero.jpg">
                <meta property="og:type" content="article">
                <meta name="twitter:card" content="summary_large_image">
                <meta name="twitter:image" content="https://cdn.example.com/photos/2024/article-twitter.jpg">
                <link rel="stylesheet" href="/style.css">
            </head>
            <body><p>Article content</p></body>
            </html>
            """.trimIndent()

        assertEquals(
            "https://cdn.example.com/photos/2024/article-hero.jpg",
            extractOgImage(html, "https://example.com/article")?.url,
        )
    }
}
