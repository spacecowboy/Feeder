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
import com.nononsenseapps.feeder.util.logDebug
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsoluteOrThrow
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLOrNull
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import okhttp3.CacheControl
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import rust.nostr.sdk.Alphabet
import rust.nostr.sdk.ConnectionMode
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.CustomWebSocketTransport
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Nip19Profile
import rust.nostr.sdk.Nip21
import rust.nostr.sdk.Nip21Enum
import rust.nostr.sdk.NostrSdkException
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.SingleLetterTag
import rust.nostr.sdk.TagKind
import rust.nostr.sdk.WebSocketAdaptor
import rust.nostr.sdk.WebSocketAdaptorWrapper
import rust.nostr.sdk.WebSocketMessage
import rust.nostr.sdk.extractRelayList
import rust.nostr.sdk.getNip05Profile
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

private const val YOUTUBE_CHANNEL_ID_ATTR = "data-channel-external-id"

@OptIn(ExperimentalStdlibApi::class)
class MyOkhttpWebSocketListener(private val adapter: MyAdapter, private val channel: Channel<WebSocketMessage>) : WebSocketListener() {
    override fun onClosed(
        webSocket: WebSocket,
        code: Int,
        reason: String,
    ) {
        logDebug(LOG_TAG, "WebSocket closed: $code, $reason")
        webSocket.close(code, reason)
        adapter.onClose()
    }

    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String,
    ) {
        logDebug(LOG_TAG, "WebSocket closing: $code, $reason")
        webSocket.close(code, reason)
        adapter.onClose()
    }

    override fun onFailure(
        webSocket: WebSocket,
        t: Throwable,
        response: Response?,
    ) {
        Log.e(LOG_TAG, "WebSocket failure", t)
        webSocket.cancel()
        adapter.onClose()
    }

    override fun onMessage(
        webSocket: WebSocket,
        text: String,
    ) {
        logDebug(LOG_TAG, "Received WebSocket message: ${text.take(80)}")
        val result = channel.trySendBlocking(WebSocketMessage.Text(text))
            .onSuccess {
                logDebug(LOG_TAG, "Successfully sent WebSocket message: ${text.take(80)}")
            }
            .onFailure { e ->
                Log.e(LOG_TAG, "Error sending WebSocket message", e)
            }

        if (!result.isSuccess) {
            Log.e(LOG_TAG, "Error sending WebSocket message")
        }
    }

    override fun onMessage(
        webSocket: WebSocket,
        bytes: ByteString,
    ) {
        logDebug(LOG_TAG, "Received WebSocket message: ${bytes.toByteArray().toHexString().take(80)}")
        val result = channel.trySendBlocking(WebSocketMessage.Binary(bytes.toByteArray()))
            .onSuccess {
                logDebug(LOG_TAG, "Successfully sent WebSocket message: ${bytes.toByteArray().toHexString().take(80)}")
            }
            .onFailure { e ->
                Log.e(LOG_TAG, "Error sending WebSocket message", e)
            }

        if (!result.isSuccess) {
            Log.e(LOG_TAG, "Error sending WebSocket message")
        }
    }

    override fun onOpen(
        webSocket: WebSocket,
        response: Response,
    ) {
        logDebug(LOG_TAG, "WebSocket opened")
        adapter.onOpen(webSocket)
    }

    companion object {
        private const val LOG_TAG = "FEEDER_WSLISTENER"
    }
}

@OptIn(ExperimentalStdlibApi::class)
class MyAdapter(private val channel: Channel<WebSocketMessage>) : WebSocketAdaptor {
    private var websocket: WebSocket? = null

    fun onOpen(webSocket: WebSocket) {
        logDebug(LOG_TAG, "Opening WebSocket myadapter")
        this.websocket = webSocket
    }

    fun onClose() {
        logDebug(LOG_TAG, "Closing WebSocket myadapter")
        this.websocket = null
        channel.close()
    }

    override suspend fun recv(): WebSocketMessage {
        logDebug(LOG_TAG, "Waiting for message")
        return select {
            channel.onReceive {
                logDebug(LOG_TAG, "Received message: $it")
                it
            }
        }
    }

    override suspend fun send(msg: WebSocketMessage) {
        // Suspend while websocket is null - connection is not open yet
        while (websocket == null) {
            logDebug(LOG_TAG, "Waiting for websocket to open")
            kotlinx.coroutines.delay(50)
        }
        when (msg) {
            is WebSocketMessage.Text -> {
                logDebug(LOG_TAG, "Sending text message: ${msg.v1.take(80)}, ${websocket != null}")
                websocket?.send(msg.v1)
            }

            is WebSocketMessage.Binary -> {
                logDebug(LOG_TAG, "Sending binary message: ${msg.v1.toHexString().take(80)}, ${websocket != null}")
                websocket?.send(msg.v1.toByteString())
            }

            is WebSocketMessage.Ping -> {
                // Not supported
            }

            is WebSocketMessage.Pong -> {
                // Not supported
            }
        }
    }

    override suspend fun terminate() {
        logDebug(LOG_TAG, "Terminating WebSocket")
        websocket?.cancel()
        websocket = null
        channel.close()
    }

    companion object {
        private const val LOG_TAG = "FEEDER_WSADAPTER"
    }
}

class MyCustomWebsocketTransport(override val di: DI) : DIAware, CustomWebSocketTransport {
    private val okHttpClient: OkHttpClient by instance()
    override suspend fun connect(url: String, mode: ConnectionMode, timeout: Duration): WebSocketAdaptorWrapper {
        val proxy =
            when (mode) {
                ConnectionMode.Direct -> null
                is ConnectionMode.Proxy -> {
                    // TODO would be more convenient to get host and port from mode directly
                    // Parse mode.addr so we can extract host and port
                    val (host, port) = mode.addr.split(':')

                    java.net.Proxy(
                        java.net.Proxy.Type.HTTP,
                        java.net.InetSocketAddress(host, port.toInt()),
                    )
                }
            }

        val customClient =
            okHttpClient.newBuilder()
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .proxy(proxy)
                .build()

        val channel = Channel<WebSocketMessage>()

        val adapter = MyAdapter(channel)
        val listener = MyOkhttpWebSocketListener(adapter, channel)

        val websocket =
            customClient.newWebSocket(
                Request.Builder().url(url).build(),
                listener,
            )

        val wrapper = WebSocketAdaptorWrapper(adapter)

        return wrapper
    }

    override fun supportPing(): Boolean {
        return false
    }

    companion object {
        private const val LOG_TAG = "FEEDER_WSTRANSPORT"
    }
}

class FeedParser(override val di: DI) : DIAware {
    private val client: OkHttpClient by instance()
    private val goFeedAdapter = GoFeedAdapter()

    // Initializing the Nostr Client
    // This can crash in emulator tests so initialize it lazily.
    private val nostrClient by lazy {
        rust.nostr.sdk.ClientBuilder().websocketTransport(MyCustomWebsocketTransport(di)).build()
        // NostrClient()
    }

    // The default relays to get info from, separated by purpose.
    private val defaultFetchRelays = listOf("wss://relay.nostr.band", "wss://relay.damus.io")
    private val defaultMetadataRelays = listOf("wss://purplepag.es", "wss://user.kindpag.es")
    private val defaultArticleFetchRelays = setOf("wss://nos.lol") + defaultFetchRelays

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
            when (parsedProfile) {
                is Nip21Enum.Pubkey -> return Nip19Profile(parsedProfile.publicKey)
                is Nip21Enum.Profile -> return Nip19Profile(parsedProfile.profile.publicKey(), parsedProfile.profile.relays())
                else -> throw NostrUriParserException(message = "Could not find the user's info: $nostrUri")
            }
        }
    }

    suspend fun getProfileMetadata(nostrUri: URL): Either<MetaDataParseError, AuthorNostrData> {
        return Either.catching(
            onCatch = { t -> MetaDataParseError(url = nostrUri.toString(), throwable = t) },
        ) {
            val possibleNostrProfile = parseNostrUri(nostrUri.toString())
            val publicKey = possibleNostrProfile.publicKey()
            val relayList =
                possibleNostrProfile.relays()
                    .takeIf {
                        it.size < 4
                    }.orEmpty()
                    .ifEmpty { getUserPublishRelays(publicKey) }
            logDebug(LOG_TAG, "Relays from Nip19 -> ${relayList.joinToString(separator = ", ")}")
            relayList
                .ifEmpty { defaultFetchRelays }
                .forEach { relayUrl ->
                    nostrClient.addReadRelay(relayUrl)
                }
            nostrClient.connect()
            val profileInfo =
                try {
                    nostrClient.fetchMetadata(
                        publicKey = publicKey,
                        timeout = Duration.ofSeconds(5L),
                    )
                } catch (e: NostrSdkException) {
                    // We will use a default relay regardless of whether it is added above, to keep things simple.
                    nostrClient.addReadRelay(defaultFetchRelays.random())
                    nostrClient.connect()
                    nostrClient.fetchMetadata(
                        publicKey = publicKey,
                        timeout = Duration.ofSeconds(5L),
                    )
                }
            logDebug(LOG_TAG, profileInfo.asPrettyJson())

            // Check if all relays in relaylist can be connected to
            AuthorNostrData(
                uri = possibleNostrProfile.toNostrUri(),
                name = profileInfo.getName().toString(),
                publicKey = publicKey,
                imageUrl = profileInfo.getPicture().toString(),
                relayList = nostrClient.relays().map { relayEntry -> relayEntry.key },
            )
        }
    }

    private suspend fun getUserPublishRelays(userPubkey: PublicKey): List<String> {
        val userRelaysFilter =
            Filter()
                .author(userPubkey)
                .kind(
                    Kind.fromStd(KindStandard.RELAY_LIST),
                )

        nostrClient.removeAllRelays()
        defaultMetadataRelays.forEach { relayUrl ->
            nostrClient.addReadRelay(relayUrl)
        }
        nostrClient.connect()
        val potentialUserRelays =
            nostrClient.fetchEventsFrom(
                urls = defaultMetadataRelays,
                filters = listOf(userRelaysFilter),
                timeout = Duration.ofSeconds(5),
            )
        val relayList = extractRelayList(potentialUserRelays.toVec().first())
        val relaysToUse =
            if (relayList.any { (_, relayType) -> relayType == RelayMetadata.WRITE }) {
                relayList.filter { it.value == RelayMetadata.WRITE }.map { entry -> entry.key }
            } else if (relayList.size < 7) {
                relayList.map { entry -> entry.key } // This represents the relay URL, just as the operation above.
            } else {
                defaultArticleFetchRelays.map { it }
            }

        return relaysToUse
    }

    private suspend fun fetchArticlesForAuthor(
        author: PublicKey,
        relays: List<String>,
    ): List<Event> {
        val articlesByAuthorFilter =
            Filter()
                .author(author)
                .kind(Kind.fromStd(KindStandard.LONG_FORM_TEXT_NOTE))
        logDebug(LOG_TAG, "Relay List size: ${relays.size}")

        nostrClient.removeAllRelays()
        val relaysToUse =
            relays.take(3).plus(defaultArticleFetchRelays.random())
                .ifEmpty { defaultFetchRelays }
        relaysToUse.forEach { relay -> nostrClient.addReadRelay(relay) }
        nostrClient.connect()
        logDebug(LOG_TAG, "FETCHING ARTICLES")
        val articleEventSet =
            nostrClient.fetchEventsFrom(
                urls = relaysToUse,
                filters =
                listOf(
                    articlesByAuthorFilter,
                ),
                timeout = Duration.ofSeconds(10L),
            ).toVec()
        val articleEvents = articleEventSet.distinctBy { it.tags().find(TagKind.Title) }
        nostrClient.removeAllRelays() // This is necessary to avoid piling relays to fetch from(on each fetch).
        return articleEvents
    }

    suspend fun findNostrFeed(authorNostrData: AuthorNostrData): Either<FeedParserError, ParsedFeed> {
        return Either.catching(
            onCatch = { t -> FetchError(url = authorNostrData.uri, throwable = t) },
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

private fun List<Event>.mapToFeed(
    nostrUri: String,
    authorName: String,
    imageUrl: String,
): ParsedFeed {
    val description = "Nostr: $authorName"
    val author =
        ParsedAuthor(
            name = authorName,
            url = "https://njump.me/${first().author().toBech32()}",
            avatar = imageUrl,
        )
    val articles =
        this.sortedByDescending {
            it.tags().find(TagKind.PublishedAt)?.content()?.toLong() ?: 0L
        }.map { event: Event ->
            event.asArticle(authorName, imageUrl)
        }

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
        items = articles,
    )
}

fun Event.asArticle(
    authorName: String,
    imageUrl: String,
): ParsedArticle {
    val articleId = id().toString()
    val articleUri = id().toNostrUri()
    val articleNostrAddress =
        Coordinate(
            Kind.fromStd(KindStandard.LONG_FORM_TEXT_NOTE),
            author(),
            tags().find(
                TagKind.SingleLetter(
                    SingleLetterTag.lowercase(Alphabet.D),
                ),
            )?.content().toString(),
        ).toBech32()
    // Highlighter is a service for reading Nostr articles on the web.
    val externalLink = "https://highlighter.com/a/$articleNostrAddress"
    val articleAuthor =
        ParsedAuthor(
            name = authorName,
            url = "https://njump.me/${author().toBech32()}",
            avatar = imageUrl,
        )
    val articleTitle = tags().find(TagKind.Title)?.content()
    val publishDate =
        Instant.ofEpochSecond(
            tags().find(TagKind.PublishedAt)?.content()?.toLong() ?: Instant.EPOCH.epochSecond,
        ).atZone(ZoneId.systemDefault()).withFixedOffsetZone()
    val articleTags = tags().hashtags()
    val articleImage = tags().find(TagKind.Image)?.content()
    val articleSummary = tags().find(TagKind.Summary)?.content()
    val articleContent = content()
    val parsedMarkdown = markDownParser.buildMarkdownTreeFromString(articleContent)
    val htmlContent = HtmlGenerator(articleContent, parsedMarkdown, CommonMarkFlavourDescriptor()).generateHtml()

    return ParsedArticle(
        id = articleId,
        url = articleUri,
        external_url = externalLink,
        title = articleTitle,
        content_html = htmlContent,
        content_text = articleContent,
        summary = articleSummary,
        image = if (articleImage != null) MediaImage(url = articleImage.toString()) else null,
        date_published = publishDate.toString(),
        date_modified = publishDate.toString(),
        author = articleAuthor,
        tags = articleTags,
        attachments = null,
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
    val relayList: List<String>,
)

sealed class NostrException(message: String) : Exception(message)

class NostrUriParserException(override val message: String) : NostrException(message)

class NostrMetadataException(override val message: String) : NostrException(message)

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
