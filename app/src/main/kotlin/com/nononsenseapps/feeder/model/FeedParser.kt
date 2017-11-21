package com.nononsenseapps.feeder.model

import android.util.Log
import com.nononsenseapps.feeder.util.asFeed
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.JsonFeedParser
import com.nononsenseapps.jsonfeed.cachingHttpClient
import com.nononsenseapps.jsonfeed.feedAdapter
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.io.InputStream

object FeedParser {

    // Should reuse same instance to have same cache
    @Volatile private var client: OkHttpClient? = null
    @Volatile private var jsonFeedParser: JsonFeedParser? = null

    /**
     * Finds alternate links in the header of an HTML document pointing to feeds.
     */
    fun findFeedLink(html: String,
                     preferRss: Boolean = false,
                     preferAtom: Boolean = false,
                     preferJSON: Boolean = false): String? {
        val doc = Jsoup.parse(html.byteInputStream(), "UTF-8", "")
        val header = doc.head()

        val feedLinks = header.getElementsByAttributeValue("rel", "alternate")
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
                .sortedBy {
                    val t = it.attr("type").toLowerCase()
                    when {
                        preferAtom && t.contains("atom") -> "0"
                        preferRss && t.contains("rss") -> "1"
                        preferJSON && t.contains("json") -> "2"
                        else -> t
                    }
                }

        return feedLinks.firstOrNull()?.attr("href")
    }

    @Throws(FeedParser.FeedParsingError::class)
    fun parseFeed(feedUrl: String, cacheDir: File?): Feed {
        Log.d("RxFeedParser", "parseFeed: $feedUrl")
        var url = feedUrl
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url
            }

            if (client == null) {
                client = cachingHttpClient(cacheDirectory = cacheDir)
            }
            if (jsonFeedParser == null) {
                jsonFeedParser = JsonFeedParser(client!!, feedAdapter())
            }

            val request = Request.Builder()
                    .url(url)
                    .build()

            val client: OkHttpClient = client!!
            val jsonFeedParser: JsonFeedParser = jsonFeedParser!!

            val call = client.newCall(request)
            val response = call.execute()

            if (!response.isSuccessful) {
                throw IOException("Unexpected code " + response)
            }

            response.use {
                Log.d("RxRSSLOCAL", "cache response: " + response.cacheResponse())
                Log.d("RxRSSLOCAL", "network response: " + response.networkResponse())

                val isJSON = (response.header("content-type") ?: "").contains("json")
                val body = response.body()?.string()

                if (body != null) {
                    val alternateFeedLink = findFeedLink(body,
                            preferAtom = true)

                    return if (alternateFeedLink != null) {
                        parseFeed(alternateFeedLink, cacheDir)
                    } else {
                        when (isJSON) {
                            true -> jsonFeedParser.parseJson(body)
                            false -> parseFeed(body)
                        }
                    }
                }
                throw NullPointerException("Response body was null")
            }
        } catch (e: Throwable) {
            throw FeedParsingError(e)
        }

    }

    @Throws(FeedParser.FeedParsingError::class)
    private fun parseFeed(feedXml: String): Feed = parseFeed(feedXml.byteInputStream())

    @Throws(FeedParser.FeedParsingError::class)
    fun parseFeed(`is`: InputStream): Feed {
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
