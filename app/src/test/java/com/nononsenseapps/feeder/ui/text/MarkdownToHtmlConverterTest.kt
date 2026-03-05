package com.nononsenseapps.feeder.ui.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownToHtmlConverterTest {
    private val converter = MarkdownToHtmlConverter()

    @Test
    fun `test basic markdown conversion`() {
        val markdown = "This is **bold** and this is *italic*"
        val html = converter.convertToHtml(markdown)

        // Should contain HTML tags
        assertTrue(html.contains("<strong>"))
        assertTrue(html.contains("</strong>"))
        assertTrue(html.contains("<em>"))
        assertTrue(html.contains("</em>"))
    }

    @Test
    fun `test list conversion`() {
        val markdown =
            """
            - Item 1
            - Item 2
            - Item 3
            """.trimIndent()

        val html = converter.convertToHtml(markdown)

        // Should contain list HTML tags
        assertTrue(html.contains("<ul>"))
        assertTrue(html.contains("</ul>"))
        assertTrue(html.contains("<li>"))
        assertTrue(html.contains("</li>"))
    }

    @Test
    fun `test heading conversion`() {
        val markdown = "# Heading 1\n## Heading 2"
        val html = converter.convertToHtml(markdown)

        // Should contain heading HTML tags
        assertTrue(html.contains("<h1>"))
        assertTrue(html.contains("</h1>"))
        assertTrue(html.contains("<h2>"))
        assertTrue(html.contains("</h2>"))
    }

    @Test
    fun `test plain text conversion`() {
        val markdown = "This is **bold** and this is *italic*"
        val plainText = converter.convertToPlainText(markdown)

        // Should not contain markdown syntax
        assertTrue(!plainText.contains("**"))
        assertTrue(!plainText.contains("*"))
        assertTrue(plainText.contains("bold"))
        assertTrue(plainText.contains("italic"))
    }

    @Test
    fun `test empty input`() {
        val html = converter.convertToHtml("")
        assertEquals("", html)

        val plainText = converter.convertToPlainText("")
        assertEquals("", plainText)
    }

    @Test
    fun `test complex markdown`() {
        val markdown =
            """
            # Summary
            
            This is a **summary** of the article:
            
            - **Key point 1**: Important information
            - *Key point 2*: More details
            
            > Quote from the article
            
            ## Conclusion
            
            The article discusses `code` and more.
            """.trimIndent()

        val html = converter.convertToHtml(markdown)

        // Should contain various HTML elements
        assertTrue(html.contains("<h1>"))
        assertTrue(html.contains("<strong>"))
        assertTrue(html.contains("<ul>"))
        assertTrue(html.contains("<blockquote>"))
        assertTrue(html.contains("<h2>"))
        assertTrue(html.contains("<code>"))
    }
}
