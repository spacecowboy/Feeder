package com.nononsenseapps.feeder.model

import android.os.Parcelable
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.nononsenseapps.feeder.model.gofeed.FeederGoItem
import com.nononsenseapps.feeder.model.gofeed.GoEnclosure
import com.nononsenseapps.feeder.model.gofeed.GoFeed
import com.nononsenseapps.feeder.model.gofeed.GoFeedAdapter
import com.nononsenseapps.feeder.model.gofeed.GoPerson
import com.nononsenseapps.feeder.util.Either
import com.nononsenseapps.feeder.util.flatMap
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsoluteOrThrow
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLOrNull
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import okhttp3.CacheControl
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import rust.nostr.sdk.Alphabet
import rust.nostr.sdk.Client
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindEnum
import rust.nostr.sdk.Nip19Profile
import rust.nostr.sdk.Nip21
import rust.nostr.sdk.Nip21Enum
import rust.nostr.sdk.NostrSdkException
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.SingleLetterTag
import rust.nostr.sdk.TagKind
import rust.nostr.sdk.extractRelayList
import rust.nostr.sdk.getNip05Profile
import java.io.IOException
import java.lang.NullPointerException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

private const val YOUTUBE_CHANNEL_ID_ATTR = "data-channel-external-id"

class FeedParser(override val di: DI) : DIAware {
    private val client: OkHttpClient by instance()
    private val goFeedAdapter = GoFeedAdapter()

    // Initializing the Nostr Client
    private val nostrClient = Client()
    // The default relays to get info from, separated by purpose.
    private val DEFAULT_FETCH_RELAYS = listOf("wss://relay.nostr.band", "wss://relay.damus.io")
    private val DEFAULT_METADATA_RELAYS = listOf("wss://purplepag.es", "wss://user.kindpag.es")
    private val DEFAULT_ARTICLE_FETCH_RELAYS = setOf("wss://nos.lol") + DEFAULT_FETCH_RELAYS

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

    private suspend fun parseNostrUri(adaptedNostrUri: String): Nip19Profile {
        val nostrUri = adaptedNostrUri.removePrefix("https://njump.me/")
        if (nostrUri.contains("@")) { // It means it is a Nip05 address
            val rawString = nostrUri.removePrefix("nostr:")
            val parsedNip5 = getNip05Profile(rawString)
            val (pubkey, relays) = parsedNip5.publicKey() to parsedNip5.relays()
            return Nip19Profile(pubkey, relays)
        } else {
            val parsedProfile = Nip21.parse(nostrUri).asEnum()
            when(parsedProfile) {
                is Nip21Enum.Pubkey -> return Nip19Profile(parsedProfile.publicKey)
                is Nip21Enum.Profile -> return Nip19Profile(parsedProfile.profile.publicKey(), parsedProfile.profile.relays())
                else -> throw Throwable(message = "Could not find the user's info: $nostrUri")
            }
        }
    }

    suspend fun getProfileMetadata(nostrUri: URL): Either<MetaDataParseError, AuthorNostrData> {

        return Either.catching(
            onCatch = { t -> MetaDataParseError(url = nostrUri.toString(), throwable = t) }
        ) {
            val possibleNostrProfile = parseNostrUri(nostrUri.toString())
            val publicKey = possibleNostrProfile.publicKey()
            val relayList = possibleNostrProfile.relays()
                .takeIf { it.size < 7 }.orEmpty()
                .ifEmpty { getUserPublishRelays(publicKey) }
            println("Relays from Nip19 -> ${relayList.joinToString(separator = ", ")}")
            relayList.ifEmpty { DEFAULT_FETCH_RELAYS } .forEach { relayUrl -> nostrClient.addReadRelay(relayUrl) }
            nostrClient.connect()
            val profileInfo = try {
                nostrClient.fetchMetadata(
                    publicKey = publicKey,
                    timeout = Duration.ofSeconds(5)
                )
            } catch (e: NostrSdkException) {
                println("Metadata Fetch Error: ${e.message}")
                nostrClient.removeAllRelays()
                getUserPublishRelays(publicKey)
                    .ifEmpty { DEFAULT_FETCH_RELAYS }
                    .forEach { relay -> nostrClient.addReadRelay(relay) }
                nostrClient.connect()
                nostrClient.fetchMetadata(
                    publicKey = publicKey,
                    timeout = Duration.ofSeconds(5)
                )
            }

            println(profileInfo.asPrettyJson())


            nostrClient.relays().forEach { (url, relay) -> println("Client Relay -> [$url, ${relay.status().name}]") }
            //Check if all relays in relaylist can be connected to
            AuthorNostrData(
                uri = possibleNostrProfile.toNostrUri(),
                name = profileInfo.getName().toString(),
                publicKey = publicKey,
                imageUrl = profileInfo.getPicture().toString(),
                relayList = nostrClient.relays().map { relayEntry -> relayEntry.key }
            )
        }

    }

    private suspend fun getUserPublishRelays(
        userPubkey: PublicKey
    ): List<String> {
        val userRelaysFilter = Filter()
            .author(userPubkey)
            .kind(Kind.fromEnum(KindEnum.RelayList))

        nostrClient.removeAllRelays()
        nostrClient.relays().forEach { (url, relay) -> println("Client Relay -> [$url, ${relay.status().name}]") }
        DEFAULT_METADATA_RELAYS.forEach { relayUrl -> nostrClient.addReadRelay(relayUrl) }
        nostrClient.connect()
        val potentialUserRelays = nostrClient.fetchEventsFrom(
            urls = DEFAULT_METADATA_RELAYS,
            filters = listOf(userRelaysFilter),
            timeout = Duration.ofSeconds(5)
        )
        println("------------New Fetch Relays----------------")
        nostrClient.pool().relays().forEach { (url, relay) -> println("New Client Relay -> [$url, ${relay.status().name}]") }
        println("User Relay Metadata:")
        val relayList = extractRelayList(potentialUserRelays.toVec().first())
        relayList.forEach { (relayUrl, metadata) ->
            println("$relayUrl -> $metadata")
        }

        val relaysToUse = if (relayList.any { (_, relayType) -> relayType == RelayMetadata.WRITE }) {
            relayList.filter { it.value == RelayMetadata.WRITE }.map { entry -> entry.key }
        } else if (relayList.size < 7) {
            relayList.map { entry -> entry.key } // This represents the relay URL, just as the operation above.
        } else {
            DEFAULT_ARTICLE_FETCH_RELAYS.map { it }
        }

        return relaysToUse

    }

    private suspend fun fetchArticlesForAuthor(
        author: PublicKey,
        relays: List<String>
    ): List<Event> {
        val articlesByAuthorFilter = Filter()
            .author(author)
            .kind(Kind.fromEnum(KindEnum.LongFormTextNote))
        println("Relay List size: -> ${relays.size}")

        nostrClient.removeAllRelays()
        relays.forEach { relay -> nostrClient.addReadRelay(relay) }
        nostrClient.connect()
        println("-------------------FETCHING ARTICLES----------------------")
        val articleEventSet = nostrClient.fetchEvents(
//            urls = relays.map { it.url() },
            filters = listOf(articlesByAuthorFilter),
            timeout = Duration.ofSeconds(10L)
        ).toVec()
        .ifEmpty {
            DEFAULT_ARTICLE_FETCH_RELAYS.forEach { nostrClient.addReadRelay(it) }
            nostrClient.connect()
            nostrClient.fetchEventsFrom(
                urls = DEFAULT_ARTICLE_FETCH_RELAYS.toList(),
                filters = listOf(articlesByAuthorFilter),
                timeout = Duration.ofSeconds(10L)
            ).toVec()
        }
        println("Article set size: -> ${articleEventSet.size}")

        val articleEvents = articleEventSet.distinctBy { it.tags().find(TagKind.Title) }
        nostrClient.removeAllRelays() //This is necessary to avoid piling relays to fetch from(on each fetch).
        return articleEvents
    }

    suspend fun findNostrFeed(authorNostrData: AuthorNostrData): Either<FeedParserError, ParsedFeed> {
        return Either.catching(
            onCatch = { t -> FetchError(url = authorNostrData.uri, throwable = t) }
        ) {
            val fetchedArticles = fetchArticlesForAuthor(authorNostrData.publicKey, authorNostrData.relayList)
            fetchedArticles.mapToFeed(authorNostrData.uri, authorNostrData.name, authorNostrData.imageUrl)
        }
    }

    suspend fun parseFeedUrl(url: URL): Either<FeedParserError, ParsedFeed> {
        return client.curlAndOnResponse(url) {
            parseFeedResponse(it)
        }
            .map {
                // Preserve original URL to maintain authentication data and/or tokens in query params
                // but this is also done inside parse from the request URL
                it.copy(feed_url = url.toString())
            }
    }

    internal fun parseFeedResponse(response: Response): Either<FeedParserError, ParsedFeed> {
        return response.body?.use {
            // OkHttp string method handles BOM and Content-Type header in request
            parseFeedResponse(
                response.request.url.toUrl(),
                it,
            )
        } ?: Either.Left(NoBody(url = response.request.url.toString()))
    }

    private fun parseFeedBytes(
        url: URL,
        body: ByteArray,
    ): ParsedFeed? {
        return goFeedAdapter.parseBody(body)?.asFeed(url)
    }

    /**
     * Takes body as bytes to handle encoding correctly
     */
    fun parseFeedResponse(
        url: URL,
        responseBody: ResponseBody,
    ): Either<FeedParserError, ParsedFeed> {
        val primaryType = responseBody.contentType()?.type
        val subType = responseBody.contentType()?.subtype ?: ""
        return when {
            primaryType == null || primaryType == "text" || subType.contains("json") || subType.contains("xml") ->
                Either.catching(
                    onCatch = { t ->
                        RSSParseError(url = url.toString(), throwable = t)
                    },
                ) {
                    responseBody.byteStream().use { bs ->
                        parseFeedBytes(url, bs.readBytes())
                            ?: throw NullPointerException("Parsed feed is null")
                    }
                }

            else -> return Either.Left(
                UnsupportedContentType(
                    url = url.toString(),
                    mimeType = responseBody.contentType().toString(),
                ),
            )
        }
    }

    /**
     * Takes body as bytes to handle encoding correctly
     */
    @VisibleForTesting
    internal fun parseFeedResponse(
        url: URL,
        body: String,
    ): Either<FeedParserError, ParsedFeed> {
        return Either.catching(
            onCatch = { t ->
                RSSParseError(url = url.toString(), throwable = t)
            },
        ) {
            parseFeedBytes(url, body.toByteArray())
                ?: throw NullPointerException("Parsed feed is null")
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_FEEDPARSER"
    }
}

private fun List<Event>.mapToFeed(nostrUri: String, authorName: String, imageUrl: String): ParsedFeed {
    val description = "Nostr articles by $authorName"
    val author = ParsedAuthor(
        name = authorName,
        url = "https://njump.me/${first().author().toBech32()}",
        avatar = imageUrl
    )
    val articles = this.sortedByDescending { it.tags().find(TagKind.PublishedAt)?.content()?.toLong() ?: 0L }.map { event: Event -> event.asArticle(authorName, imageUrl) }

    return ParsedFeed(
        title = authorName,
        home_page_url = null,
        feed_url = nostrUri,
        description = description,
        user_comment = null,
        next_url = null,
        icon = imageUrl,
        favicon = null,
        author = author,
        expired = null,
        items = articles
    )
}

fun Event.asArticle(authorName: String, imageUrl: String): ParsedArticle {
    val articleId = id().toString()
    val articleUri = id().toNostrUri()
    val articleNostrAddress = Coordinate(
        Kind.fromEnum(KindEnum.LongFormTextNote),
        author(),
        tags().find(
            TagKind.SingleLetter(SingleLetterTag.lowercase(Alphabet.D))
        )?.content().toString()
    ).toBech32()
    // Highlighter is a service for reading Nostr articles on the web.
    val externalLink = "https://highlighter.com/a/$articleNostrAddress"
    val articleAuthor = ParsedAuthor(
        name = authorName,
        url = "https://njump.me/${author().toBech32()}",
        avatar = imageUrl
    )
    val articleTitle = tags().find(TagKind.Title)?.content()
    val publishDate = Instant.ofEpochSecond(
        tags().find(TagKind.PublishedAt)?.content()?.toLong() ?: Instant.EPOCH.epochSecond
    ).atZone(ZoneId.systemDefault()).withFixedOffsetZone()
    val articleTags = tags().hashtags()
    val articleImage = tags().find(TagKind.Image)?.content()
    val articleSummary = tags().find(TagKind.Summary)?.content()
    val articleContent = content()
    val parsedMarkdown = markDownParser.buildMarkdownTreeFromString(articleContent)
    val htmlFromContent = HtmlGenerator(articleContent, parsedMarkdown, CommonMarkFlavourDescriptor()).generateHtml()

    return ParsedArticle(
        id = articleId,
        url = articleUri,
        external_url = externalLink,
        title = articleTitle,
        content_html = htmlFromContent,
        content_text = articleContent,
        summary = articleSummary,
        image = if (articleImage != null) MediaImage(url = articleImage.toString()) else null,
        date_published = publishDate.toString(),
        date_modified = publishDate.toString(),
        author = articleAuthor,
        tags = articleTags,
        attachments = null
    )

}

private fun GoFeed.asFeed(url: URL): ParsedFeed =
    ParsedFeed(
        title = title,
        home_page_url = link?.let { relativeLinkIntoAbsolute(url, it) },
        // Keep original URL to maintain authentication data and/or tokens in query params
        feed_url = url.toString(),
        description = description,
        user_comment = "",
        next_url = "",
        icon = image?.url?.let { relativeLinkIntoAbsolute(url, it) },
        favicon = null,
        author = author?.asParsedAuthor(),
        expired = null,
        items = items?.mapNotNull { it?.let { FeederGoItem(it, author, url).asParsedArticle() } },
    )

private fun FeederGoItem.asParsedArticle() =
    ParsedArticle(
        id = guid,
        url = link,
        external_url = null,
        title = title,
        content_html = content,
        content_text = plainContent,
        summary = snippet,
        image = thumbnail,
        date_published = published,
        date_modified = updated,
        author = author?.asParsedAuthor(),
        tags = categories,
        attachments = enclosures?.map { it.asParsedEnclosure() },
    )

private fun GoEnclosure.asParsedEnclosure() =
    ParsedEnclosure(
        url = url,
        title = null,
        mime_type = type,
        size_in_bytes = length?.toLongOrNull(),
        duration_in_seconds = null,
    )

private fun GoPerson.asParsedAuthor() =
    ParsedAuthor(
        name = name,
        url = null,
        avatar = null,
    )

suspend fun OkHttpClient.getResponse(
    url: URL,
    forceNetwork: Boolean = false,
): Response {
    val request =
        Request.Builder()
            .url(url)
            .run {
                if (forceNetwork) {
                    cacheControl(
                        // Force network will make conditional requests for servers which support them
                        CacheControl.FORCE_NETWORK,
                    )
                } else {
                    this
                }
            }
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
                    retryAfterSeconds = response.retryAfterSeconds,
                    message = response.message,
                ),
            )
        }
    }
}

private val markDownParser = MarkdownParser(CommonMarkFlavourDescriptor())

class AuthorNostrData(
    val uri: String,
    val name: String,
    val publicKey: PublicKey,
    val imageUrl: String,
    val relayList: List<String>
)

@Parcelize
sealed class FeedParserError : Parcelable {
    abstract val url: String
    abstract val description: String
    abstract val throwable: Throwable?
}

@Parcelize
data object NotInitializedYet : FeedParserError() {
    @IgnoredOnParcel
    override val url: String = ""

    @IgnoredOnParcel
    override val description: String = ""

    @IgnoredOnParcel
    override val throwable: Throwable? = null
}

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
    val retryAfterSeconds: Long?,
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
