package com.nononsenseapps.feeder.ui.text

import android.util.Log
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import com.nononsenseapps.feeder.util.Either
import java.util.Stack

/**
 * Intended primarily to convert HTML into plaintext snippets, useful for previewing content in list.
 */
@Suppress("UNUSED_PARAMETER")
class HtmlToPlainTextConverter : KsoupHtmlHandler {
    private var builder: StringBuilder? = null
    private val listings = Stack<Listing>()
    private var ignoreCount = 0
    private val ignoredTags = listOf("style", "script")
    private var lastImageAlt: String? = null
    private var inCdata: Boolean = false

    private val isOrderedList: Boolean
        get() = !listings.isEmpty() && listings.peek().ordered

    /**
     * Converts HTML into plain text
     */
    fun convert(source: String): Either<HtmlError, String> {
        this.builder = StringBuilder()
        this.listings.clear()
        this.ignoreCount = 0
        this.lastImageAlt = null
        val parser = KsoupHtmlParser(
            handler = this,
            options = KsoupHtmlOptions(
                recognizeCDATA = true,
                decodeEntities = false,
            ),
        )

        return Either.catching(
            onCatch = {
                Log.e(LOG_TAG, "Failed to convert to plain text", it)
                HtmlError(it)
            },
        ) {
            parser.write(source)
        }
            .onEither {
                parser.end()
            }
            .map {
                // Replace non-breaking space (160) with normal space
                builder!!.toString().replace(160.toChar(), ' ').trim { it <= ' ' }
            }
    }

    override fun onEnd() {
        // See test mentioning XKCD
        if (builder?.isEmpty() == true) {
            lastImageAlt?.let {
                builder?.append("[$lastImageAlt]")
            }
        }
    }

    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        handleStartTag(name, attributes)
    }

    private fun handleStartTag(tag: String, attributes: Map<String, String>) {
        when {
            tag.equals("br", ignoreCase = true) -> {
                // We don't need to handle this. Soup will ensure that there's a </br> for each <br>
                // so we can safely emit the linebreaks when we handle the close tag.
            }

            tag.equals("p", ignoreCase = true) -> ensureSpace(builder)
            tag.equals("div", ignoreCase = true) -> ensureSpace(builder)
            tag.equals("strong", ignoreCase = true) -> strong(builder)
            tag.equals("b", ignoreCase = true) -> strong(builder)
            tag.equals("em", ignoreCase = true) -> emphasize(builder)
            tag.equals("cite", ignoreCase = true) -> emphasize(builder)
            tag.equals("dfn", ignoreCase = true) -> emphasize(builder)
            tag.equals("i", ignoreCase = true) -> emphasize(builder)
            tag.equals("blockquote", ignoreCase = true) -> ensureSpace(builder)
            tag.equals("a", ignoreCase = true) -> startA(builder, attributes)
            tag.length == 2 &&
                Character.toLowerCase(tag[0]) == 'h' &&
                tag[1] >= '1' && tag[1] <= '6' -> ensureSpace(builder)

            tag.equals("ul", ignoreCase = true) -> startUl(builder)
            tag.equals("ol", ignoreCase = true) -> startOl(builder)
            tag.equals("li", ignoreCase = true) -> startLi(builder)
            ignoredTags.contains(tag.lowercase()) -> ignoreCount++
            tag.equals("img", ignoreCase = true) -> startImg(builder, attributes)
        }
    }

    private fun startImg(text: StringBuilder?, attributes: Map<String, String>) {
        // Ensure whitespace
        ensureSpace(text)

        lastImageAlt = attributes.getOrDefault("alt", "").ifBlank { "IMG" }
    }

    private fun startOl(text: StringBuilder?) {
        // Start lists with linebreak
        val len = text!!.length
        if (len > 0 && text[len - 1] != '\n') {
            text.append("\n")
        }

        // Remember list type
        listings.push(Listing(true))
    }

    private fun startLi(builder: StringBuilder?) {
        builder!!.append(repeated("  ", listings.size - 1))
        if (isOrderedList) {
            val listing = listings.peek()
            builder.append("").append(listing.number).append(". ")
            listing.number = listing.number + 1
        } else {
            builder.append("* ")
        }
    }

    private fun endLi(text: StringBuilder?) {
        // Add newline
        val len = text!!.length
        if (len > 0 && text[len - 1] != '\n') {
            text.append("\n")
        }
    }

    private fun startUl(text: StringBuilder?) {
        // Start lists with linebreak
        val len = text!!.length
        if (len > 0 && text[len - 1] != '\n') {
            text.append("\n")
        }

        // Remember list type
        listings.push(Listing(false))
    }

    private fun endOl(builder: StringBuilder?) {
        listings.pop()
    }

    private fun endUl(builder: StringBuilder?) {
        listings.pop()
    }

    private fun startA(builder: StringBuilder?, attributes: Map<String, String>) {}

    private fun endA(builder: StringBuilder?) {}

    override fun onCloseTag(name: String, isImplied: Boolean) {
        handleEndTag(name)
    }

    private fun handleEndTag(tag: String) {
        when {
            tag.equals("br", ignoreCase = true) -> ensureSpace(builder)
            tag.equals("p", ignoreCase = true) -> ensureSpace(builder)
            tag.equals("div", ignoreCase = true) -> ensureSpace(builder)
            tag.equals("strong", ignoreCase = true) -> strong(builder)
            tag.equals("b", ignoreCase = true) -> strong(builder)
            tag.equals("em", ignoreCase = true) -> emphasize(builder)
            tag.equals("cite", ignoreCase = true) -> emphasize(builder)
            tag.equals("dfn", ignoreCase = true) -> emphasize(builder)
            tag.equals("i", ignoreCase = true) -> emphasize(builder)
            tag.equals("blockquote", ignoreCase = true) -> ensureSpace(builder)
            tag.equals("a", ignoreCase = true) -> endA(builder)
            tag.length == 2 &&
                Character.toLowerCase(tag[0]) == 'h' &&
                tag[1] >= '1' && tag[1] <= '6' -> ensureSpace(builder)

            tag.equals("ul", ignoreCase = true) -> endUl(builder)
            tag.equals("ol", ignoreCase = true) -> endOl(builder)
            tag.equals("li", ignoreCase = true) -> endLi(builder)
            ignoredTags.contains(tag.lowercase()) -> ignoreCount--
        }
    }

    private fun emphasize(builder: StringBuilder?) {}

    private fun strong(builder: StringBuilder?) {}

    private fun ensureSpace(text: StringBuilder?) {
        val len = text!!.length
        if (len != 0) {
            val c = text[len - 1]
            // Non-breaking space (160) is not caught by trim or whitespace identification
            if (Character.isWhitespace(c) || c.code == 160) {
                return
            }
            text.append(" ")
        }
    }

    override fun onCDataStart() {
        inCdata = true
    }

    override fun onCDataEnd() {
        inCdata = false
    }

    override fun onText(text: String) {
        if (ignoreCount > 0) {
            return
        }

        val sb = StringBuilder()

        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         *
         * TODO handle non-breaking space (character 160)
         */
        val textToRead = when (inCdata) {
            true -> text
            else -> KsoupEntities.decodeHtml(text)
        }
        for (c in textToRead) {
            if (c == ' ' || c == '\n') {
                var len = sb.length

                val prev: Char = if (len == 0) {
                    len = builder!!.length

                    if (len == 0) {
                        '\n'
                    } else {
                        builder!![len - 1]
                    }
                } else {
                    sb[len - 1]
                }

                if (prev != ' ' && prev != '\n') {
                    sb.append(' ')
                }
            } else {
                sb.append(c)
            }
        }

        builder!!.append(sb)
    }

    class Listing(var ordered: Boolean) {
        var number: Int = 0

        init {
            number = 1
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_HTMLTOPLAIN"
    }
}

fun repeated(string: String, count: Int): String {
    val sb = StringBuilder()

    for (i in 0 until count) {
        sb.append(string)
    }

    return sb.toString()
}

data class HtmlError(val throwable: Throwable)
