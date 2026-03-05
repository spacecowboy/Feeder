package com.nononsenseapps.feeder.ui.compose.text

import com.nononsenseapps.feeder.ui.text.MarkdownToHtmlConverter
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlToAnnotatedStringTest {
    @Test
    fun `test markdown to html to annotated string flow`() {
        val markdown =
            """
            # AI Summary
            
            This is a **summary** with *formatting*:
            
            - **Key point 1**: Important information
            - *Key point 2*: More details
            
            > Quote from the article
            
            ## Conclusion
            
            The article discusses `code` and more.
            """.trimIndent()

        val markdownConverter = MarkdownToHtmlConverter()
        val html = markdownConverter.convertToHtml(markdown)

        // Convert HTML to AnnotatedString
        val annotatedStrings = htmlStringToAnnotatedString(html)

        // Should have some annotated strings
        assertTrue(annotatedStrings.isNotEmpty())

        // Check that the content is preserved
        val combinedText = annotatedStrings.joinToString("\n") { it.text }
        assertTrue(combinedText.contains("AI Summary"))
        assertTrue(combinedText.contains("summary"))
        assertTrue(combinedText.contains("Key point"))
    }

    @Test
    fun `test simple html conversion`() {
        val html = "<p>This is <strong>bold</strong> and <em>italic</em> text.</p>"

        val annotatedStrings = htmlStringToAnnotatedString(html)

        assertTrue(annotatedStrings.isNotEmpty())

        val combinedText = annotatedStrings.joinToString("\n") { it.text }
        assertTrue(combinedText.contains("This is"))
        assertTrue(combinedText.contains("bold"))
        assertTrue(combinedText.contains("italic"))
        assertTrue(combinedText.contains("text"))
    }

    @Test
    fun `test list html conversion`() {
        val html =
            """
            <ul>
                <li>Item 1</li>
                <li>Item 2</li>
                <li>Item 3</li>
            </ul>
            """.trimIndent()

        val annotatedStrings = htmlStringToAnnotatedString(html)

        assertTrue(annotatedStrings.isNotEmpty())

        val combinedText = annotatedStrings.joinToString("\n") { it.text }
        assertTrue(combinedText.contains("Item 1"))
        assertTrue(combinedText.contains("Item 2"))
        assertTrue(combinedText.contains("Item 3"))
    }
}
