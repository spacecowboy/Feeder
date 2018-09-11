package com.nononsenseapps.feeder.model.opml

import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.model.OPMLParserToDatabase
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import org.ccil.cowan.tagsoup.Parser
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.InputSource
import org.xml.sax.Locator
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

class OpmlParser(val opmlToDb: OPMLParserToDatabase) : ContentHandler {

    val parser: Parser = Parser()
    val tagStack: Stack<String> = Stack()
    var isFeedTag = false
    var ignoring = 0

    init {
        parser.contentHandler = this
    }

    @Throws(IOException::class, SAXException::class)
    fun parseFile(path: String) {
        // Open file
        val file = File(path)

        file.inputStream().use {
            parseInputStream(it)
        }
    }

    @Throws(IOException::class, SAXException::class)
    fun parseInputStream(inputStream: InputStream) {
        tagStack.clear()
        isFeedTag = false
        ignoring = 0

        parser.parse(InputSource(inputStream))
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if ("outline" == localName) {
            when {
                ignoring > 0 -> ignoring--
                isFeedTag -> isFeedTag = false
                else -> tagStack.pop()
            }
        }
    }

    override fun processingInstruction(target: String?, data: String?) {
    }

    override fun startPrefixMapping(prefix: String?, uri: String?) {
    }

    override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) {
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
    }

    override fun endDocument() {
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, atts: Attributes?) {
        if ("outline" == localName) {
            when {
            // Nesting not allowed
                ignoring > 0 || isFeedTag -> ignoring++
                outlineIsFeed(atts) -> {
                    isFeedTag = true
                    val feed = Feed(
                            title = unescape(atts?.getValue("title") ?: atts?.getValue("text") ?: ""),
                            tag = if (tagStack.isNotEmpty()) tagStack.peek() else "",
                            url = sloppyLinkToStrictURL(atts?.getValue("xmlurl") ?: ""))

                    opmlToDb.saveFeed(feed)
                }
                else -> tagStack.push(unescape(atts?.getValue("title") ?: atts?.getValue("text") ?: ""))
            }
        }
    }

    private fun outlineIsFeed(atts: Attributes?): Boolean =
            atts?.getValue("xmlurl") != null

    override fun skippedEntity(name: String?) {
    }

    override fun setDocumentLocator(locator: Locator?) {
    }

    override fun endPrefixMapping(prefix: String?) {
    }

    override fun startDocument() {
    }

}
