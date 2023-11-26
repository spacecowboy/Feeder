package com.nononsenseapps.feeder.model

import android.os.Parcelable
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.nononsenseapps.feeder.util.Either
import com.nononsenseapps.feeder.util.asFeed
import com.nononsenseapps.feeder.util.flatMap
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsoluteOrThrow
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLOrNull
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.JsonFeedParser
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
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
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val YOUTUBE_CHANNEL_ID_ATTR = "data-channel-external-id"

class FeedParser(override val di: DI) : DIAware {
    private val client: OkHttpClient by instance()
    private val jsonFeedParser: JsonFeedParser by instance()

    /**
     * Parses all relevant information from a main site so duplicate calls aren't needed
     */
    suspend fun getSiteMetaData(url: URL): Either<FeedParserError, SiteMetaData> {
        return curl(url)
            .flatMap { html ->
                getSiteMetaDataInHtml(url, html)
            }
    }

    @VisibleForTesting
    internal fun getSiteMetaDataInHtml(
        url: URL,
        html: String,
    ): Either<FeedParserError, SiteMetaData> {
        if (!html.contains("<head>", ignoreCase = true)) {
            // Probably a a feed URL and not a page
            return Either.Left(NotHTML(url = url.toString()))
        }

        return Either.catching(
            onCatch = { t ->
                MetaDataParseError(url = url.toString(), throwable = t).also {
                    Log.w(LOG_TAG, "Error when fetching site metadata", t)
                }
            },
        ) {
            SiteMetaData(
                url = url,
                alternateFeedLinks = getAlternateFeedLinksInHtml(html, baseUrl = url),
                feedImage = getFeedIconInHtml(html, baseUrl = url),
            )
        }
    }

    @VisibleForTesting
    internal fun getFeedIconInHtml(
        html: String,
        baseUrl: URL? = null,
    ): String? {
        val doc =
            html.byteInputStream().use {
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
                    baseUrl != null ->
                        relativeLinkIntoAbsolute(
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
        val doc =
            html.byteInputStream().use {
                Jsoup.parse(it, "UTF-8", "")
            }

        val feeds =
            doc.getElementsByAttributeValue("rel", "alternate")
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
                                    link =
                                        relativeLinkIntoAbsoluteOrThrow(
                                            base = baseUrl,
                                            link = e.attr("href"),
                                        ),
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        else ->
                            sloppyLinkToStrictURLOrNull(e.attr("href"))?.let { l ->
                                AlternateLink(
                                    type = e.attr("type"),
                                    link = l,
                                )
                            }
                    }
                } ?: emptyList()

        return when {
            feeds.isNotEmpty() -> feeds
            baseUrl?.host == "www.youtube.com" || baseUrl?.host == "youtube.com" ->
                findFeedLinksForYoutube(
                    doc,
                )

            else -> emptyList()
        }
    }

    private fun findFeedLinksForYoutube(doc: Document): List<AlternateLink> {
        val channelId: String? =
            doc.body()?.getElementsByAttribute(YOUTUBE_CHANNEL_ID_ATTR)
                ?.firstOrNull()
                ?.attr(YOUTUBE_CHANNEL_ID_ATTR)

        return when (channelId) {
            null -> emptyList()
            else ->
                listOf(
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

    suspend fun parseFeedUrl(url: URL): Either<FeedParserError, Feed> {
        return client.curlAndOnResponse(url) {
            parseFeedResponse(it)
        }
            .map {
                // Preserve original URL to maintain authentication data and/or tokens in query params
                it.copy(feed_url = url.toString())
            }
    }

    internal fun parseFeedResponse(response: Response): Either<FeedParserError, Feed> {
        return response.body?.use {
            // OkHttp string method handles BOM and Content-Type header in request
            parseFeedResponse(
                response.request.url.toUrl(),
                it,
            )
        } ?: Either.Left(NoBody(url = response.request.url.toString()))
    }

    /**
     * Takes body as bytes to handle encoding correctly
     */
    fun parseFeedResponse(
        url: URL,
        responseBody: ResponseBody,
    ): Either<FeedParserError, Feed> {
        return when (responseBody.contentType()?.subtype?.contains("json")) {
            true ->
                Either.catching(
                    onCatch = { t ->
                        JsonFeedParseError(url = url.toString(), throwable = t)
                    },
                ) {
                    jsonFeedParser.parseJson(responseBody)
                }

            else -> parseRssAtom(url, responseBody)
        }
            .map { feed ->
                if (feed.feed_url == null) {
                    // Nice to return non-null value here
                    feed.copy(feed_url = url.toString())
                } else {
                    feed
                }
            }
    }

    /**
     * Takes body as bytes to handle encoding correctly
     */
    internal fun parseFeedResponse(
        url: URL,
        body: String,
        contentType: MediaType?,
    ): Either<FeedParserError, Feed> {
        return when (contentType?.subtype?.contains("json")) {
            true ->
                Either.catching(
                    onCatch = { t ->
                        JsonFeedParseError(url = url.toString(), throwable = t)
                    },
                ) {
                    jsonFeedParser.parseJson(body)
                }

            else -> parseRssAtom(url, body)
        }.map { feed ->

            if (feed.feed_url == null) {
                // Nice to return non-null value here
                feed.copy(feed_url = url.toString())
            } else {
                feed
            }
        }
    }

    private fun parseRssAtom(
        url: URL,
        responseBody: ResponseBody,
    ): Either<FeedParserError, Feed> {
        val contentType = responseBody.contentType()
        val validMimeType =
            when (contentType?.type) {
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
            return Either.Left(
                UnsupportedContentType(url = url.toString(), mimeType = contentType.toString()),
            )
        }

        return Either.catching(
            onCatch = { t ->
                RSSParseError(url = url.toString(), throwable = t)
            },
        ) {
            responseBody.byteStream().use { bs ->
                val feed =
                    XmlReader(bs, true, responseBody.contentType()?.charset()?.name()).use {
                        SyndFeedInput()
                            .apply {
                                isPreserveWireFeed = true
                            }
                            .build(it)
                    }
                feed.asFeed(baseUrl = url)
            }
        }
    }

    @Throws(FeedParsingError::class)
    internal fun parseRssAtom(
        baseUrl: URL,
        body: String,
    ): Either<FeedParserError, Feed> {
        return Either.catching(
            onCatch = { t ->
                RSSParseError(url = baseUrl.toString(), throwable = t)
            },
        ) {
            body.byteInputStream().use { bs ->
                val feed =
                    XmlReader(bs, true).use {
                        SyndFeedInput()
                            .apply {
                                isPreserveWireFeed = true
                            }
                            .build(it)
                    }
                feed.asFeed(baseUrl = baseUrl)
            }
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_FEEDPARSER"
    }
}

class FeedParsingError(val url: URL, e: Throwable) : Exception(e.message, e)

suspend fun OkHttpClient.getResponse(
    url: URL,
    forceNetwork: Boolean = false,
): Response {
    val request =
        Request.Builder()
            .url(url)
            .cacheControl(
                CacheControl.Builder()
                    // The time between cache re-validations
                    .maxAge(
                        if (forceNetwork) {
                            0
                        } else {
                            // Matches fastest sync schedule
                            15
                        },
                        TimeUnit.MINUTES,
                    )
                    .build(),
            )
            .build()

    @Suppress("BlockingMethodInNonBlockingContext")
    val clientToUse =
        if (url.userInfo?.isNotBlank() == true) {
            val parts = url.userInfo.split(':')
            val user = parts.first()
            val pass =
                if (parts.size > 1) {
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

suspend fun OkHttpClient.curl(url: URL): Either<FeedParserError, String> {
    return curlAndOnResponse(url) {
        val contentType = it.body?.contentType()
        when (contentType?.type) {
            "text" -> {
                when (contentType.subtype) {
                    "plain", "html" -> {
                        it.body?.let { body ->
                            Either.catching(
                                onCatch = { throwable ->
                                    FetchError(
                                        throwable = throwable,
                                        url = url.toString(),
                                    )
                                },
                            ) {
                                body.string()
                            }
                        } ?: Either.Left(
                            NoBody(
                                url = url.toString(),
                            ),
                        )
                    }

                    else ->
                        Either.Left(
                            UnsupportedContentType(
                                url = url.toString(),
                                mimeType = contentType.toString(),
                            ),
                        )
                }
            }

            else ->
                Either.Left(
                    UnsupportedContentType(
                        url = url.toString(),
                        mimeType = contentType.toString(),
                    ),
                )
        }
    }
}

suspend fun <T> OkHttpClient.curlAndOnResponse(
    url: URL,
    block: (suspend (Response) -> Either<FeedParserError, T>),
): Either<FeedParserError, T> {
    return Either.catching(
        onCatch = { t ->
            FetchError(url = url.toString(), throwable = t)
        },
    ) {
        getResponse(url)
    }.flatMap { response ->
        if (response.isSuccessful) {
            response.use {
                block(it)
            }
        } else {
            Either.Left(
                HttpError(
                    url = url.toString(),
                    code = response.code,
                    message = response.message,
                ),
            )
        }
    }
}

@Parcelize
sealed class FeedParserError : Parcelable {
    abstract val url: String
    abstract val description: String
    abstract val throwable: Throwable?
}

/*
 * Data object would be ideal for this
 */
@Parcelize
data class NotInitializedYet(
    override val url: String = "",
    override val description: String = "",
    override val throwable: Throwable? = null,
) : FeedParserError()

@Parcelize
data class FetchError(
    override val url: String,
    override val throwable: Throwable?,
    override val description: String = throwable?.message ?: "",
) : FeedParserError()

@Parcelize
data class NotHTML(
    override val url: String,
    override val description: String = "",
    override val throwable: Throwable? = null,
) : FeedParserError()

@Parcelize
data class MetaDataParseError(
    override val url: String,
    override val throwable: Throwable?,
    override val description: String = throwable?.message ?: "",
) : FeedParserError()

@Parcelize
data class RSSParseError(
    override val throwable: Throwable?,
    override val url: String,
    override val description: String = throwable?.message ?: "",
) : FeedParserError()

@Parcelize
data class JsonFeedParseError(
    override val throwable: Throwable?,
    override val url: String,
    override val description: String = throwable?.message ?: "",
) : FeedParserError()

@Parcelize
data class NoAlternateFeeds(
    override val url: String,
    override val description: String = "",
    override val throwable: Throwable? = null,
) : FeedParserError()

@Parcelize
data class HttpError(
    override val url: String,
    val code: Int,
    val message: String,
    override val description: String = "$code: $message",
    override val throwable: Throwable? = null,
) : FeedParserError()

@Parcelize
data class UnsupportedContentType(
    override val url: String,
    val mimeType: String,
    override val description: String = mimeType,
    override val throwable: Throwable? = null,
) : FeedParserError()

@Parcelize
data class NoBody(
    override val url: String,
    override val description: String = "",
    override val throwable: Throwable? = null,
) : FeedParserError()

@Parcelize
data class NoUrl(
    override val description: String = "",
    override val url: String = "",
    override val throwable: Throwable? = null,
) : FeedParserError()

@Parcelize
data class FullTextDecodingFailure(
    override val url: String,
    override val throwable: Throwable?,
    override val description: String = throwable?.message ?: "",
) : FeedParserError()
