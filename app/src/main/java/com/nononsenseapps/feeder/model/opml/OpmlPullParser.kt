package com.nononsenseapps.feeder.model.opml

import android.util.Log
import android.util.Xml
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.model.OPMLParserHandler
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import kotlin.reflect.KProperty
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

private const val TAG_SETTING = "setting"

private const val TAG_OPML = "opml"

private const val TAG_BODY = "body"

private const val TAG_OUTLINE = "outline"

private const val TAG_SETTINGS = "settings"

private const val ATTR_XMLURL = "xmlUrl"

private const val ATTR_TITLE = "title"

private const val ATTR_TEXT = "text"

private const val ATTR_NOTIFY = "notify"

private const val ATTR_FULL_TEXT_BY_DEFAULT = "fullTextByDefault"

private const val ATTR_FLYM_RETRIEVE_FULL_TEXT = "retrieveFullText"

private const val ATTR_ALTERNATE_ID = "alternateId"

private const val ATTR_IMAGE_URL = "imageUrl"

private const val ATTR_OPEN_ARTICLES_WITH = "openArticlesWith"

private const val TAG_BLOCKED = "blocked"

@Suppress("NAME_SHADOWING")
class OpmlPullParser(private val opmlToDb: OPMLParserHandler) {
    private val feeds: MutableList<Feed> = mutableListOf()
    private val settings: MutableMap<String, String> = mutableMapOf()
    private val blockList: MutableSet<String> = mutableSetOf()
    private val parser: XmlPullParser = Xml.newPullParser()

    @Throws(IOException::class)
    suspend fun parseInputStream(inputStream: InputStream) = withContext(IO) {
        inputStream.use { inputStream ->
            try {
                parser.setInput(inputStream, null)
                parser.nextTag()
                readOpml()

                for (feed in feeds) {
                    opmlToDb.saveFeed(feed)
                }
                for ((key, value) in settings) {
                    opmlToDb.saveSetting(key = key, value = value)
                }

                opmlToDb.saveBlocklistPatterns(blockList)
            } catch (e: XmlPullParserException) {
                Log.e(LOG_TAG, "OPML Import exploded", e)
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readOpml() {
        parser.require(XmlPullParser.START_TAG, null, TAG_OPML)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag.
            if (parser.name == TAG_BODY) {
                readBody()
            } else {
                skip()
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBody() {
        parser.require(XmlPullParser.START_TAG, null, TAG_BODY)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == TAG_OUTLINE) {
                readOutline(parser, parentOutlineTag = "")
            } else if (parser.name == TAG_SETTINGS && parser.namespace == OPML_FEEDER_NAMESPACE) {
                readSettings()
            } else {
                skip()
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSettings() {
        parser.require(XmlPullParser.START_TAG, OPML_FEEDER_NAMESPACE, TAG_SETTINGS)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when {
                parser.name == TAG_SETTING && parser.namespace == OPML_FEEDER_NAMESPACE -> {
                    readSetting()
                }
                parser.name == TAG_BLOCKED && parser.namespace == OPML_FEEDER_NAMESPACE -> {
                    readBlocked()
                }
                else -> {
                    skip()
                }
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSetting() {
        parser.require(XmlPullParser.START_TAG, OPML_FEEDER_NAMESPACE, TAG_SETTING)

        val key by this
        val value by this

        key?.let { key ->
            value?.let { value ->
                settings[key] = unescape(value)
            }
        }

        skip()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBlocked() {
        parser.require(XmlPullParser.START_TAG, OPML_FEEDER_NAMESPACE, TAG_BLOCKED)

        val pattern by this

        pattern?.let { pattern ->
            blockList.add(
                unescape(pattern)
            )
        }

        skip()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readOutline(parser: XmlPullParser, parentOutlineTag: String) {
        parser.require(XmlPullParser.START_TAG, null, TAG_OUTLINE)

        val type by this
        val xmlUrl by this
        when {
            type == "rss" || xmlUrl != null -> readOutlineAsRss(parser, tag = parentOutlineTag)
            type == null -> readOutlineAsTag()
            else -> skip()
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readOutlineAsTag() {
        parser.require(XmlPullParser.START_TAG, null, TAG_OUTLINE)

        val tag = unescape(
            parser.getAttributeValue(null, ATTR_TITLE)
                ?: parser.getAttributeValue(null, ATTR_TEXT)
                ?: "",
        )

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == TAG_OUTLINE) {
                readOutline(parser, parentOutlineTag = tag)
            } else {
                skip()
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readOutlineAsRss(parser: XmlPullParser, tag: String) {
        parser.require(XmlPullParser.START_TAG, null, TAG_OUTLINE)

        val feedTitle = unescape(
            parser.getAttributeValue(null, ATTR_TITLE)
                ?: parser.getAttributeValue(null, ATTR_TEXT)
                ?: "",
        )
        try {
            val feed = Feed(
                title = feedTitle,
                customTitle = feedTitle,
                tag = tag,
                url = URL(parser.getAttributeValue(null, ATTR_XMLURL)),
            ).let { feed ->
                // Copy so default values can be referenced
                feed.copy(
                    notify = parser.getAttributeValue(OPML_FEEDER_NAMESPACE, ATTR_NOTIFY)
                        ?.toBoolean()
                        ?: feed.notify,
                    fullTextByDefault = (
                        parser.getAttributeValue(
                            OPML_FEEDER_NAMESPACE,
                            ATTR_FULL_TEXT_BY_DEFAULT,
                        )
                            ?.toBoolean()
                            // Support Flym's value for this
                            ?: parser.getAttributeValue(null, ATTR_FLYM_RETRIEVE_FULL_TEXT)
                                ?.toBoolean()
                        ) ?: feed.fullTextByDefault,
                    alternateId = parser.getAttributeValue(OPML_FEEDER_NAMESPACE, ATTR_ALTERNATE_ID)
                        ?.toBoolean()
                        ?: feed.alternateId,
                    openArticlesWith = parser.getAttributeValue(
                        OPML_FEEDER_NAMESPACE,
                        ATTR_OPEN_ARTICLES_WITH,
                    ) ?: feed.openArticlesWith,
                    imageUrl = parser.getAttributeValue(OPML_FEEDER_NAMESPACE, ATTR_IMAGE_URL)
                        ?.let { imageUrl ->
                            try {
                                URL(imageUrl)
                            } catch (e: MalformedURLException) {
                                Log.e(
                                    LOG_TAG,
                                    "Invalid imageUrl [$imageUrl] on feed [$feedTitle] in OPML",
                                    e,
                                )
                                null
                            }
                        } ?: feed.imageUrl,
                )
            }

            feeds.add(feed)
        } catch (e: MalformedURLException) {
            // Feed URL is REQUIRED, so don't try to add feeds without valid URLs
            Log.e(LOG_TAG, "Bad url on feed [$feedTitle] in OPML", e)
        }

        skip()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip() {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    /**
     * Lets you fetch an un-namespaced attribute like this
     *
     * val attr: String? by this
     *
     * where 'attr' is the case-sensitive attr you are fetching
     */
    operator fun getValue(
        thisRef: Nothing?,
        property: KProperty<*>,
    ): String? {
        return parser.getAttributeValue(null, property.name)
    }
}

private const val LOG_TAG = "FEEDER_OPMLPULL"
