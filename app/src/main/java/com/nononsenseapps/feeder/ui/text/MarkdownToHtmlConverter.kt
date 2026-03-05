package com.nononsenseapps.feeder.ui.text

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

/**
 * Converts Markdown text to HTML for display in the app.
 * This is used primarily for rendering AI-generated summaries that contain Markdown formatting.
 */
class MarkdownToHtmlConverter {
    private val flavour = CommonMarkFlavourDescriptor()
    private val parser = MarkdownParser(flavour)

    /**
     * Converts Markdown text to HTML
     */
    fun convertToHtml(markdown: String): String {
        if (markdown.isBlank()) {
            return ""
        }

        return try {
            val parsedTree = parser.buildMarkdownTreeFromString(markdown)
            val htmlGenerator = HtmlGenerator(markdown, parsedTree, flavour)
            htmlGenerator.generateHtml()
        } catch (e: Exception) {
            // If Markdown processing fails, return the original text escaped for HTML
            escapeHtml(markdown)
        }
    }

    /**
     * Converts Markdown text to plain text (similar to the existing HTML converter)
     */
    fun convertToPlainText(markdown: String): String {
        if (markdown.isBlank()) {
            return ""
        }

        return try {
            // First convert to HTML, then strip HTML tags to get clean plain text
            val html = convertToHtml(markdown)
            html.replace(Regex("<[^>]*>"), "")
        } catch (e: Exception) {
            // If Markdown processing fails, return the original text with basic markdown cleanup
            markdown
                .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Remove **bold**
                .replace(Regex("\\*(.*?)\\*"), "$1") // Remove *italic*
                .replace(Regex("`(.*?)`"), "$1") // Remove `code`
        }
    }

    private fun escapeHtml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
