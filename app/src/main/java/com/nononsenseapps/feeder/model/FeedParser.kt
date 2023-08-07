package com.nononsenseapps.feeder.model

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.nononsenseapps.feeder.util.asFeed
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsoluteOrThrow
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLOrNull
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.JsonFeedParser
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

private const val YOUTUBE_CHANNEL_ID_ATTR = "data-channel-external-id"

class FeedParser(override val di: DI) : DIAware {
    private val client: OkHttpClient by instance()
    private val jsonFeedParser: JsonFeedParser by instance()

    /**
     * Parses all relevant information from a main site so duplicate calls aren't needed
     */
    suspend fun getSiteMetaData(url: URL): SiteMetaData? {
        return try {
            val html = curl(url)
            return getSiteMetaDataInHtml(url, html)
        } catch (t: Throwable) {
            Log.e(LOG_TAG, "Error when fetching site metadata", t)
            null
        }
    }

    @VisibleForTesting
    internal fun getSiteMetaDataInHtml(url: URL, html: String?): SiteMetaData? {
        return try {
            if (html?.contains("<head>", ignoreCase = true) != true) {
                // Probably a a feed URL and not a page
                return null
            }
            SiteMetaData(
                url = url,
                alternateFeedLinks = getAlternateFeedLinksInHtml(html, baseUrl = url),
                feedImage = getFeedIconInHtml(html, baseUrl = url),
            )
        } catch (t: Throwable) {
            Log.e(LOG_TAG, "Error when fetching site metadata", t)
            null
        }
    }

    @VisibleForTesting
    internal fun getFeedIconInHtml(
        html: String,
        baseUrl: URL? = null,
    ): String? {
        val doc = html.byteInputStream().use {
            Jsoup.parse(it, "UTF-8", baseUrl?.toString() ?: "")
        }

        return (
            doc.getElementsByAttributeValue("rel", "apple-touch-icon") +
                doc.getElementsByAttributeValue("rel", "icon") +
                doc.getElementsByAttributeValue("rel", "shortcut icon")
            )
            .filter { it.hasAttr("href") }
            .firstNotNullOfOrNull { e ->
                when {
                    baseUrl != null -> relativeLinkIntoAbsolute(
                        base = baseUrl,
                        link = e.attr("href"),
                    )

                    else -> sloppyLinkToStrictURLOrNull(e.attr("href"))?.toString()
                }
            }
    }

    /**
     * Returns all alternate links in the HTML/XML document pointing to feeds.
     */
    private fun getAlternateFeedLinksInHtml(
        html: String,
        baseUrl: URL? = null,
    ): List<AlternateLink> {
        val doc = html.byteInputStream().use {
            Jsoup.parse(it, "UTF-8", "")
        }

        val feeds = doc.getElementsByAttributeValue("rel", "alternate")
            ?.filter { it.hasAttr("href") && it.hasAttr("type") }
            ?.filter {
                val t = it.attr("type").lowercase(Locale.getDefault())
                when {
                    t.contains("application/atom") -> true
                    t.contains("application/rss") -> true
                    // Youtube for example has alternate links with application/json+oembed type.
                    t == "application/json" -> true
                    else -> false
                }
            }
            ?.filter {
                val l = it.attr("href").lowercase(Locale.getDefault())
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
            ?.mapNotNull { e ->
                when {
                    baseUrl != null -> {
                        try {
                            AlternateLink(
                                type = e.attr("type"),
                                link = relativeLinkIntoAbsoluteOrThrow(
                                    base = baseUrl,
                                    link = e.attr("href"),
                                ),
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    else -> sloppyLinkToStrictURLOrNull(e.attr("href"))?.let { l ->
                        AlternateLink(
                            type = e.attr("type"),
                            link = l,
                        )
                    }
                }
            } ?: emptyList()

        return when {
            feeds.isNotEmpty() -> feeds
            baseUrl?.host == "www.youtube.com" || baseUrl?.host == "youtube.com" -> findFeedLinksForYoutube(
                doc,
            )

            else -> emptyList()
        }
    }

    private fun findFeedLinksForYoutube(doc: Document): List<AlternateLink> {
        val channelId: String? = doc.body()?.getElementsByAttribute(YOUTUBE_CHANNEL_ID_ATTR)
            ?.firstOrNull()
            ?.attr(YOUTUBE_CHANNEL_ID_ATTR)

        return when (channelId) {
            null -> emptyList()
            else -> listOf(
                AlternateLink(
                    type = "atom",
                    link = URL("https://www.youtube.com/feeds/videos.xml?channel_id=$channelId"),
                ),
            )
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
            // Preserve original URL to maintain authentication data and/or tokens in query params
            return result?.copy(feed_url = url.toString())
        } catch (e: Throwable) {
            throw FeedParsingError(url, e)
        }
    }

    @Throws(FeedParsingError::class)
    internal fun parseFeedResponse(response: Response): Feed? {
        return response.body?.use {
            // OkHttp string method handles BOM and Content-Type header in request
            parseFeedResponse(
                response.request.url.toUrl(),
                it,
            )
        }
    }

    /**
     * Takes body as bytes to handle encoding correctly
     */
    @Throws(FeedParsingError::class)
    fun parseFeedResponse(
        url: URL,
        responseBody: ResponseBody,
    ): Feed {
        try {
            val feed = when (responseBody.contentType()?.subtype?.contains("json")) {
                true -> jsonFeedParser.parseJson(responseBody)
                else -> parseRssAtom(url, responseBody)
            }

            return if (feed.feed_url == null) {
                // Nice to return non-null value here
                feed.copy(feed_url = url.toString())
            } else {
                feed
            }
        } catch (e: Throwable) {
            throw FeedParsingError(url, e)
        }
    }

    /**
     * Takes body as bytes to handle encoding correctly
     */
    @Throws(FeedParsingError::class)
    internal fun parseFeedResponse(
        url: URL,
        body: String,
        contentType: MediaType?,
    ): Feed {
        try {
            val feed = when (contentType?.subtype?.contains("json")) {
                true -> jsonFeedParser.parseJson(body)
                else -> parseRssAtom(url, body)
            }

            return if (feed.feed_url == null) {
                // Nice to return non-null value here
                feed.copy(feed_url = url.toString())
            } else {
                feed
            }
        } catch (e: Throwable) {
            throw FeedParsingError(url, e)
        }
    }

    @Throws(FeedParsingError::class)
    internal fun parseRssAtom(baseUrl: URL, responseBody: ResponseBody): Feed {
        try {
            val contentType = responseBody.contentType()
            val validMimeType = when (contentType?.type) {
                "application" -> {
                    when {
                        contentType.subtype.contains("xml") -> true
                        else -> false
                    }
                }

                "text" -> {
                    // So many sites on the internet return mimetype text/html for rss feeds...
                    // So try to parse it despite it being wrong
                    true
                }

                else -> false
            }
            if (!validMimeType) {
                throw RuntimeException("Not parsing document with invalid RSS/Atom mimetype: ${contentType?.type}/${contentType?.subtype}")
            }
            responseBody.byteStream().use { bs ->
                val feed = XmlReader(bs, true, responseBody.contentType()?.charset()?.name()).use {
                    SyndFeedInput()
                        .apply {
                            isPreserveWireFeed = true
                        }
                        .build(it)
                }
                return feed.asFeed(baseUrl = baseUrl)
            }
        } catch (t: Throwable) {
            throw FeedParsingError(baseUrl, t)
        }
    }

    @Throws(FeedParsingError::class)
    internal fun parseRssAtom(baseUrl: URL, body: String): Feed {
        try {
            body.byteInputStream().use { bs ->
                val feed = XmlReader(bs, true).use {
                    SyndFeedInput()
                        .apply {
                            isPreserveWireFeed = true
                        }
                        .build(it)
                }
                return feed.asFeed(baseUrl = baseUrl)
            }
        } catch (t: Throwable) {
            throw FeedParsingError(baseUrl, t)
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_FEEDPARSER"
    }
}

class FeedParsingError(val url: URL, e: Throwable) : Exception(e.message, e)

suspend fun OkHttpClient.getResponse(url: URL, forceNetwork: Boolean = false): Response {
    val request = Request.Builder()
        .url(url)
        .cacheControl(
            CacheControl.Builder()
                .run {
                    if (forceNetwork) {
                        // Force cache-revalidation
                        maxAge(0, TimeUnit.SECONDS)
                    } else {
                        // Will accept anything generated in the last X
                        maxAge(10, TimeUnit.MINUTES)
                    }
                }
                .build(),
        )
        .build()

    @Suppress("BlockingMethodInNonBlockingContext")
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
                    response.request.header("Authorization") != null -> {
                        null
                    }

                    else -> {
                        response.request.newBuilder()
                            .header("Authorization", credentials)
                            .build()
                    }
                }
            }
            .proxyAuthenticator { _, response ->
                when {
                    response.request.header("Proxy-Authorization") != null -> {
                        null
                    }

                    else -> {
                        response.request.newBuilder()
                            .header("Proxy-Authorization", credentials)
                            .build()
                    }
                }
            }
            .build()
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
        val contentType = it.body?.contentType()
        result = when (contentType?.type) {
            "text" -> {
                when (contentType.subtype) {
                    "plain", "html" -> it.body?.string()
                    else -> null
                }
            }

            else -> null
        }
    }
    return result
}

suspend fun <T> OkHttpClient.curlAndOnResponse(url: URL, block: (suspend (Response) -> T)): T {
    val response = getResponse(url)

    if (!response.isSuccessful) {
        throw IOException("Unexpected code $response")
    }

    return response.use {
        block(it)
    }
}
