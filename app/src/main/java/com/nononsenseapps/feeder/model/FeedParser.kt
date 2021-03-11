package com.nononsenseapps.feeder.model

import android.util.Log
import com.nononsenseapps.feeder.util.asFeed
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsoluteOrThrow
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.JsonFeedParser
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.Buffer
import okio.GzipSource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

val slashPattern = """<\s*slash:comments\s*/>""".toRegex(RegexOption.IGNORE_CASE)
private const val YOUTUBE_CHANNEL_ID_ATTR = "data-channel-external-id"

@FlowPreview
class FeedParser(override val kodein: Kodein) : KodeinAware {
    private val client: OkHttpClient by instance()
    private val jsonFeedParser: JsonFeedParser by instance()

    /**
     * Finds the preferred alternate link in the header of an HTML/XML document pointing to feeds.
     */
    fun findFeedUrl(html: String,
                    preferRss: Boolean = false,
                    preferAtom: Boolean = false,
                    preferJSON: Boolean = false): URL? {

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
                .map {
                    sloppyLinkToStrictURL(it.first) to it.second
                }

        return feedLinks.firstOrNull()?.first
    }

    /**
     * Returns all alternate links in the header of an HTML/XML document pointing to feeds.
     */
    suspend fun getAlternateFeedLinksAtUrl(url: URL): List<Pair<String, String>> {
        return try {
            val html = curl(url)
            when {
                html != null -> getAlternateFeedLinksInHtml(html, baseUrl = url)
                else -> emptyList()
            }
        } catch (t: Throwable) {
            Log.e("FeedParser", "Error when fetching alternate links", t)
            emptyList()
        }
    }

    /**
     * Returns all alternate links in the HTML/XML document pointing to feeds.
     */
    fun getAlternateFeedLinksInHtml(html: String, baseUrl: URL? = null): List<Pair<String, String>> {
        val doc = Jsoup.parse(html.byteInputStream(), "UTF-8", "")

        val feeds = doc.getElementsByAttributeValue("rel", "alternate")
                ?.filter { it.hasAttr("href") && it.hasAttr("type") }
                ?.filter {
                    val t = it.attr("type").toLowerCase()
                    when {
                        t.contains("application/atom") -> true
                        t.contains("application/rss") -> true
                        // Youtube for example has alternate links with application/json+oembed type.
                        t == "application/json" -> true
                        else -> false
                    }
                }
                ?.filter {
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
                ?.map {
                    when {
                        baseUrl != null -> relativeLinkIntoAbsolute(base = baseUrl, link = it.attr("href")) to it.attr("type")
                        else -> sloppyLinkToStrictURL(it.attr("href")).toString() to it.attr("type")
                    }
                } ?: emptyList()

        return when {
            feeds.isNotEmpty() -> feeds
            baseUrl?.host == "www.youtube.com" || baseUrl?.host == "youtube.com" -> findFeedLinksForYoutube(doc)
            else -> emptyList()
        }
    }

    private fun findFeedLinksForYoutube(doc: Document): List<Pair<String, String>> {
        val channelId: String? = doc.body()?.getElementsByAttribute(YOUTUBE_CHANNEL_ID_ATTR)
                ?.firstOrNull()
                ?.attr(YOUTUBE_CHANNEL_ID_ATTR)

        return when (channelId) {
            null -> emptyList()
            else -> listOf("https://www.youtube.com/feeds/videos.xml?channel_id=$channelId" to "atom")
        }
    }

    /**
     * @throws IOException if request fails due to network issue for example
     */
    private suspend fun curl(url: URL) = client.curl(url)

    /**
     * @throws IOException if request fails due to network issue for example
     */
    private suspend fun curlAndOnResponse(url: URL, block: (suspend (Response) -> Unit)) =
        client.curlAndOnResponse(url, block)

    @Throws(FeedParsingError::class)
    suspend fun parseFeedUrl(url: URL): Feed? {
        try {
            var result: Feed? = null
            curlAndOnResponse(url) {
                result = parseFeedResponse(it)
            }
            return result
        } catch (e: Throwable) {
            throw FeedParsingError(url, e)
        }
    }

    @Throws(FeedParsingError::class)
    fun parseFeedResponse(response: Response): Feed? =
            response.safeBody()?.let { body ->
                parseFeedResponse(response, body)
            }

    /**
     * Takes body as bytes to handle encoding correctly
     */
    @Throws(FeedParsingError::class)
    fun parseFeedResponse(response: Response, body: ByteArray): Feed? {
        try {
            val feed = when ((response.header("content-type") ?: "").contains("json")) {
                true -> jsonFeedParser.parseJsonBytes(body)
                false -> parseRssAtomBytes(response.request().url().url(), body)
            }

            return if (feed.feed_url == null) {
                // Nice to return non-null value here
                feed.copy(feed_url = response.request().url().toString())
            } else {
                feed
            }
        } catch (e: Throwable) {
            throw FeedParsingError(response.request().url().url(), e)
        }
    }

    /**
     * Takes body as bytes to handle encoding correctly
     */
    @Throws(FeedParsingError::class)
    suspend fun parseFeedResponseOrFallbackToAlternateLink(response: Response): Feed? =
            response.body()?.use { responseBody ->
                responseBody.bytes().let { body ->
                    // Encoding is not an issue for reading HTML (probably)
                    val alternateFeedLink = findFeedUrl(String(body), preferAtom = true)

                    return if (alternateFeedLink != null) {
                        parseFeedUrl(alternateFeedLink)
                    } else {
                        parseFeedResponse(response, body)
                    }
                }
            }

    @Throws(FeedParsingError::class)
    internal fun parseRssAtomBytes(baseUrl: URL, feedXml: ByteArray): Feed {
        try {
            feedXml.inputStream().use { return parseFeedInputStream(baseUrl, it) }
        } catch (e: NumberFormatException) {
            try {
                // Try to work around bug in Rome
                var encoding: String? = null
                val xml: String = slashPattern.replace(
                        feedXml.inputStream().use {
                            XmlReader(it).use {
                                encoding = it.encoding
                                it.readText()
                            }
                        }, "")

                xml.byteInputStream(Charset.forName(encoding ?: "UTF-8")).use {
                    return parseFeedInputStream(baseUrl, it)
                }
            } catch (e: Throwable) {
                throw FeedParsingError(baseUrl, e)
            }
        }
    }

    @Throws(FeedParsingError::class)
    internal fun parseFeedInputStream(baseUrl: URL, `is`: InputStream): Feed {
        `is`.use {
            try {
                val feed = XmlReader(`is`).use { SyndFeedInput().build(it) }
                return feed.asFeed(baseUrl = baseUrl)
            } catch (e: NumberFormatException) {
                throw e
            } catch (e: Throwable) {
                throw FeedParsingError(baseUrl, e)
            }
        }
    }

    class FeedParsingError(val url: URL, e: Throwable) : Exception(e.message, e)
}

fun Response.safeBody(): ByteArray? {
    return this.body()?.use { body ->
        if (header("Transfer-Encoding") == "chunked") {
            val source =
                    if (header("Content-Encoding") == "gzip") {
                        GzipSource(body.source())
                    } else {
                        body.source()
                    }
            val buffer = Buffer()
            try {
                var readBytes: Long = 0
                while (readBytes != -1L) {
                    readBytes = source.read(buffer, Long.MAX_VALUE)
                }
            } catch (e: EOFException) {
                // This is not always fatal - sometimes the server might have sent the wrong
                // content-length (I suspect)
                Log.e(
                        "FeedParser",
                        "Encountered EOF exception while parsing response with headers: ${headers()}",
                        e
                )
            }
            buffer.readByteArray()
        } else {
            body.bytes()
        }
    }
}

suspend fun OkHttpClient.getResponse(url: URL, forceNetwork: Boolean = false): Response {
    val request = Request.Builder()
        .url(url)
        .cacheControl(CacheControl.Builder()
            .let {
                if (forceNetwork) {
                    // Force a cache revalidation
                    it.maxAge(0, TimeUnit.SECONDS)
                } else {
                    // Do a cache revalidation at most every minute
                    it.maxAge(1, TimeUnit.MINUTES)
                }
            }
            .build())
        .build()

    val clientToUse = if (url.userInfo?.isNotBlank() == true) {
        val parts = url.userInfo.split(':')
        val user = parts.first()
        val pass = if (parts.size > 1) {
            parts[1]
        } else {
            ""
        }
        val decodedUser = URLDecoder.decode(user, "UTF-8")
        val decodedPass = URLDecoder.decode(pass, "UTF-8")
        val credentials = Credentials.basic(decodedUser, decodedPass)
        newBuilder()
            .authenticator { _, response ->
                when {
                    response.request().header("Authorization") != null -> {
                        null
                    }
                    else -> {
                        response.request().newBuilder()
                            .header("Authorization", credentials)
                            .build()
                    }
                }
            }
            .proxyAuthenticator { _, response ->
                when {
                    response.request().header("Proxy-Authorization") != null -> {
                        null
                    }
                    else -> {
                        response.request().newBuilder()
                            .header("Proxy-Authorization", credentials)
                            .build()
                    }
                }
            }.build()
    } else {
        this
    }

    return withContext(IO) {
        clientToUse.newCall(request).execute()
    }
}

suspend fun OkHttpClient.curl(url: URL): String? {
    var result: String? = null
    curlAndOnResponse(url) {
        result = it.body()?.string()
    }
    return result
}

suspend fun OkHttpClient.curlAndOnResponse(url: URL, block: (suspend (Response) -> Unit)) {
    val response = getResponse(url)

    if (!response.isSuccessful) {
        throw IOException("Unexpected code $response")
    }

    response.use {
        block(it)
    }
}
