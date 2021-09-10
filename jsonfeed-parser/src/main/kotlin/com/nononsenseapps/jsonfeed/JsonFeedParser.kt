package com.nononsenseapps.jsonfeed

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import okio.buffer
import okio.source
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

// 10 mins
const val MIN_MAXAGE = 600
val MAX_AGE_PATTERN = """max-age=(\d+)""".toRegex()

fun cachingHttpClient(
    cacheDirectory: File? = null,
    cacheSize: Long = 10L * 1024L * 1024L,
    trustAllCerts: Boolean = true,
    connectTimeoutSecs: Long = 30L,
    readTimeoutSecs: Long = 30L
): OkHttpClient {
    val builder: OkHttpClient.Builder = OkHttpClient.Builder()

    if (cacheDirectory != null) {
        builder.cache(Cache(cacheDirectory, cacheSize))
    }

    // Not all web servers have good cache settings defined, so overwrite feeds to allow caching
    // for 20 mins if whatever it says is bad
    builder.addNetworkInterceptor {
        val response = it.proceed(it.request())

        try {
            val cacheHeaders = response.headers("Cache-Control")

            var maxAge = -1

            for (header in cacheHeaders) {
                MAX_AGE_PATTERN.find(header)?.let {
                    maxAge = it.groupValues.last().toInt()
                }
            }

            maxAge = maxOf(maxAge, MIN_MAXAGE)

            response.newBuilder()
                .header("Cache-Control", "public, max-age=$maxAge")
                .build()
        } catch (ignored: Throwable) {
            response
        }
    }

    builder
        .connectTimeout(connectTimeoutSecs, TimeUnit.SECONDS)
        .readTimeout(readTimeoutSecs, TimeUnit.SECONDS)
        .followRedirects(true)

    if (trustAllCerts) {
        builder.trustAllCerts()
    }

    return builder.build()
}

fun feedAdapter(): JsonAdapter<Feed> = Moshi.Builder().build().adapter(Feed::class.java)

/**
 * A parser for JSONFeeds. CacheDirectory and CacheSize are only relevant if feeds are downloaded. They are not used
 * for parsing JSON directly.
 */
class JsonFeedParser(
    private val httpClient: OkHttpClient,
    private val jsonFeedAdapter: JsonAdapter<Feed>
) {

    constructor(
        cacheDirectory: File? = null,
        cacheSize: Long = 10L * 1024L * 1024L,
        trustAllCerts: Boolean = true,
        connectTimeoutSecs: Long = 5L,
        readTimeoutSecs: Long = 5L
    ) : this(
        cachingHttpClient(
            cacheDirectory = cacheDirectory,
            cacheSize = cacheSize,
            trustAllCerts = trustAllCerts,
            connectTimeoutSecs = connectTimeoutSecs,
            readTimeoutSecs = readTimeoutSecs
        ),
        feedAdapter()
    )

    /**
     * Download a JSONFeed and parse it
     */
    fun parseUrl(url: String): Feed {
        val request: Request
        try {
            request = Request.Builder()
                .url(url)
                .build()
        } catch (error: Throwable) {
            throw IllegalArgumentException("Bad URL. Perhaps it is missing an http:// prefix?", error)
        }

        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IOException("Failed to download feed: $response")
        }

        val body = response.body
        if (body != null) {
            body.source().inputStream().use {
                return parseJsonStream(it.source().buffer())
            }
        } else {
            throw IOException("Failed to parse feed: body was NULL")
        }
    }

    /**
     * Parse a JSONFeed
     */
    fun parseJson(json: String): Feed =
        json.byteInputStream().use { return parseJsonStream(it.source().buffer()) }

    /**
     * Parse a JSONFeed
     */
    fun parseJsonBytes(json: ByteArray, charset: Charset = Charset.forName("UTF-8")): Feed {


        return json.inputStream().use { parseJsonStream(it.source().buffer()) }
    }

    /**
     * Parse a JSONFeed
     */
    @Suppress("MemberVisibilityCanPrivate")
    fun parseJsonStream(json: BufferedSource): Feed {
        val result = jsonFeedAdapter.fromJson(json)

        when {
            result != null -> return result
            else -> throw IOException("Failed to parse JSONFeed")
        }
    }
}

data class Feed(
    val version: String? = "https://jsonfeed.org/version/1",
    val title: String?,
    val home_page_url: String? = null,
    val feed_url: String? = null,
    val description: String? = null,
    val user_comment: String? = null,
    val next_url: String? = null,
    val icon: String? = null,
    val favicon: String? = null,
    val author: Author? = null,
    val expired: Boolean? = null,
    val hubs: List<Hub>? = null,
    val items: List<Item>?
)

data class Author(
    val name: String? = null,
    val url: String? = null,
    val avatar: String? = null
)

data class Item(
    val id: String?,
    val url: String? = null,
    val external_url: String? = null,
    val title: String? = null,
    val content_html: String? = null,
    val content_text: String? = null,
    val summary: String? = null,
    val image: String? = null,
    val banner_image: String? = null,
    val date_published: String? = null,
    val date_modified: String? = null,
    val author: Author? = null,
    val tags: List<String>? = null,
    val attachments: List<Attachment>? = null
)

data class Attachment(
    val url: String?,
    val mime_type: String? = null,
    val title: String? = null,
    val size_in_bytes: Long? = null,
    val duration_in_seconds: Long? = null
)

data class Hub(
    val type: String?,
    val url: String?
)
