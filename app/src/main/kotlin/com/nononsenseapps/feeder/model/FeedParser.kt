package com.nononsenseapps.feeder.model

import android.util.Log
import com.nononsenseapps.feeder.util.asFeed
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsoluteOrThrow
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.JsonFeedParser
import com.nononsenseapps.jsonfeed.cachingHttpClient
import com.nononsenseapps.jsonfeed.feedAdapter
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

object FeedParser {

    // Should reuse same instance to have same cache
    @Volatile private var client: OkHttpClient = cachingHttpClient()
    @Volatile private var jsonFeedParser: JsonFeedParser = JsonFeedParser(client, feedAdapter())

    /**
     * To enable caching, you need to call this method explicitly with a suitable cache directory.
     */
    @Synchronized fun setup(cacheDir: File?): FeedParser {
        this.client = cachingHttpClient(cacheDir)
        jsonFeedParser = JsonFeedParser(client, feedAdapter())

        return this
    }

    /**
     * Finds the preferred alternate link in the header of an HTML/XML document pointing to feeds.
     */
    fun findFeedLink(html: String,
                     preferRss: Boolean = false,
                     preferAtom: Boolean = false,
                     preferJSON: Boolean = false): String? {

        val feedLinks = getAlternateFeedLinksInHtml(html)
                .sortedBy {
                    val t = it.second.toLowerCase()
                    when {
                        preferAtom && t.contains("atom") -> "0"
                        preferRss && t.contains("rss") -> "1"
                        preferJSON && t.contains("json") -> "2"
                        else -> t
                    }
                }

        return feedLinks.firstOrNull()?.first
    }

    /**
     * Returns all alternate links in the header of an HTML/XML document pointing to feeds.
     */
    fun getAlternateFeedLinksAtUrl(url: String): List<Pair<String, String>> {
        @Suppress("NAME_SHADOWING")
        val url = sloppyLinkToStrictURL(url).toString()

        val html = curl(url)
        return when {
            html != null -> getAlternateFeedLinksInHtml(html, baseUrl = url)
            else -> emptyList()
        }
    }

    /**
     * Returns all alternate links in the header of an HTML/XML document pointing to feeds.
     */
    fun getAlternateFeedLinksInHtml(html: String, baseUrl: String? = null): List<Pair<String, String>> {
        val doc = Jsoup.parse(html.byteInputStream(), "UTF-8", "")
        val header = doc.head()

        return header.getElementsByAttributeValue("rel", "alternate")
                .filter { it.hasAttr("href") && it.hasAttr("type") }
                .filter {
                    val t = it.attr("type").toLowerCase()
                    when {
                        t.contains("application/atom") -> true
                        t.contains("application/rss") -> true
                        t.contains("application/json") -> true
                        else -> false
                    }
                }
                .filter {
                    val l = it.attr("href").toLowerCase()
                    try {
                        if (baseUrl != null) {
                            relativeLinkIntoAbsoluteOrThrow(base = baseUrl, link = l)
                        } else {
                            URL(l)
                        }
                        true
                    } catch (_: MalformedURLException) {
                        false
                    }
                }
                .map {
                    when {
                        baseUrl != null -> relativeLinkIntoAbsolute(base = baseUrl, link = it.attr("href")) to it.attr("type")
                        else -> sloppyLinkToStrictURL(it.attr("href")).toString() to it.attr("type")
                    }
                }
    }

    fun curl(url: String): String? {
        var result: String? = null
        curlAndOnResponse(url) {
            result = body()?.string()
        }
        return result
    }

    fun curlAndOnResponse(url: String, block: (Response.() -> Unit)) {
        val request = Request.Builder()
                .url(sloppyLinkToStrictURL(url))
                .build()

        val call = client.newCall(request)
        val response = call.execute()

        if (!response.isSuccessful) {
            throw IOException("Unexpected code " + response)
        }

        response.use {
            it.block()
        }
    }

    @Throws(FeedParser.FeedParsingError::class)
    fun parseFeedUrl(feedUrl: String): Feed {
        Log.d("RxFeedParser", "parseFeed: $feedUrl")
        val url = sloppyLinkToStrictURL(feedUrl).toString()
        try {

            var result: Feed? = null
            curlAndOnResponse(url) {
                Log.d("RxRSSLOCAL", "cache response: " + cacheResponse())
                Log.d("RxRSSLOCAL", "network response: " + networkResponse())

                val isJSON = (header("content-type") ?: "").contains("json")
                val body = body()?.string()

                if (body != null) {
                    val alternateFeedLink = findFeedLink(body,
                            preferAtom = true)

                    val feed = if (alternateFeedLink != null) {
                        parseFeedUrl(alternateFeedLink)
                    } else {
                        when (isJSON) {
                            true -> jsonFeedParser.parseJson(body)
                            false -> parseRssAtomBody(body)
                        }
                    }

                    result = if (feed.feed_url == null) {
                        // Nice to return non-null value here
                        feed.copy(feed_url = feedUrl)
                    } else {
                        feed
                    }
                } else {
                    throw NullPointerException("Response body was null")
                }
            }

            return result!!
        } catch (e: Throwable) {
            throw FeedParsingError(e)
        }

    }

    @Throws(FeedParser.FeedParsingError::class)
    fun parseRssAtomBody(feedXml: String): Feed = parseFeedInputStream(feedXml.byteInputStream())

    @Throws(FeedParser.FeedParsingError::class)
    fun parseFeedInputStream(`is`: InputStream): Feed {
        `is`.use {
            try {
                val feed = SyndFeedInput().build(XmlReader(`is`))
                return feed.asFeed()
            } catch (e: Throwable) {
                throw FeedParsingError(e)
            }
        }
    }

    class FeedParsingError(e: Throwable) : Exception(e)
}
