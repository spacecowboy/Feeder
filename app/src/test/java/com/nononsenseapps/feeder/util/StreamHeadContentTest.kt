package com.nononsenseapps.feeder.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StreamHeadContentTest {
    @Test
    fun returnsHeadSectionUpToClosingHeadTag() {
        val html = "<html><head><title>Test</title></head><body>Hello</body></html>"
        val result = streamHeadContent(html.byteInputStream(Charsets.UTF_8))

        assertEquals("<html><head><title>Test</title></head>", result)
    }

    @Test
    fun caseInsensitiveClosingHeadTag() {
        val html = "<html><head><title>Test</title></HEAD><body>Hello</body></html>"
        val result = streamHeadContent(html.byteInputStream(Charsets.UTF_8))

        assertEquals("<html><head><title>Test</title></HEAD>", result)
    }

    @Test
    fun returnsContentUpToSafetyLimitWhenNoClosingHeadTag() {
        val headContent = "<html><head><title>No closing head"
        val padding = "x".repeat(66000 - headContent.length)
        val html = headContent + padding

        val result = streamHeadContent(html.byteInputStream(Charsets.UTF_8))

        assertTrue(result.length <= 65536 + 2048)
        assertTrue(result.startsWith(headContent))
    }

    @Test
    fun handlesMultiByteUtf8CharactersAcrossBufferBoundary() {
        val cjkChar = "\u3042"
        val prefix = "<html><head><title>" + "a".repeat(2047 - "<html><head><title>".length)
        val suffix = cjkChar + "</head><body></body></html>"

        val html = prefix + suffix
        val result = streamHeadContent(html.byteInputStream(Charsets.UTF_8))

        assertTrue(result.contains(cjkChar))
        assertTrue(result.endsWith("</head>"))
    }

    @Test
    fun detectsClosingHeadTagSplitAcrossBufferBoundary() {
        val headOpen = "<html><head><title>"
        val padLength = 2046 - headOpen.length
        val padding = "d".repeat(padLength)
        val html = headOpen + padding + "</head>" + "<body>should-not-appear</body></html>"

        val result = streamHeadContent(html.byteInputStream(Charsets.UTF_8))

        assertTrue(result.endsWith("</head>"))
        assertFalse(result.contains("should-not-appear"))
    }
}
