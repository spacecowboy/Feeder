package com.nononsenseapps.feeder.model.opml

import com.nononsenseapps.feeder.db.FeedSQL
import com.nononsenseapps.feeder.model.OPMLParserToDatabase
import org.ccil.cowan.tagsoup.Parser
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.InputSource
import org.xml.sax.Locator
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.io.InputStream

class OpmlParser(val opmlToDb: OPMLParserToDatabase) : ContentHandler {

    val parser: Parser = Parser()
    var mCurrentTag: String? = null
    var ignoring = 0
    var isFeedTag = false

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
        this.mCurrentTag = null
        ignoring = 0
        this.isFeedTag = false

        parser.parse(InputSource(inputStream))
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
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
        // Ignoring, return
        if (ignoring > 0) {
            ignoring -= 1
            return
        }

        if (isFeedTag) {
            isFeedTag = false
        } else {
            // Must be a tag-tag
            mCurrentTag = null
        }
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, atts: Attributes?) {
        // Not allowing nesting below feeds
        if (ignoring > 0) {
            ignoring += 1
            return
        }

        if ("outline" == localName) {
            when {
                "rss" == atts?.getValue("type") -> {
                    isFeedTag = true
                    // Yes, tagsoup seems to make the tags lowercase
                    val feed = (opmlToDb.getFeed(atts.getValue("xmlurl") ?: "") ?: FeedSQL())
                            .copy(title = unescape(atts.getValue("title") ?: ""),
                                    customTitle = unescape(atts.getValue("title") ?: ""),
                                    tag = mCurrentTag ?: "")

                    opmlToDb.saveFeed(feed)
                }
                mCurrentTag == null -> {
                    mCurrentTag = unescape(atts?.getValue("title") ?: "")
                }
                else -> {
                    ignoring += 1
                }
            }
        }
    }

    override fun skippedEntity(name: String?) {
    }

    override fun setDocumentLocator(locator: Locator?) {
    }

    override fun endPrefixMapping(prefix: String?) {
    }

    override fun startDocument() {
    }

}
