package com.nononsenseapps.feeder.model

import android.annotation.SuppressLint
import android.util.Log
import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.modules.mediarss.MediaModule
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import org.joda.time.DateTime
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.Math.min
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object FeedParser {

    // Should reuse same instance to have same cache
    private var client: OkHttpClient? = null

    private fun cachingClient(cacheDirectory: File): OkHttpClient {
        if (client == null) {

            val cacheSize = 10 * 1024 * 1024 // 10 MiB
            val cache = Cache(cacheDirectory, cacheSize.toLong())

            val builder = OkHttpClient.Builder()
                    .cache(cache)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)

            trustAllCerts(builder)

            client = builder.build()
        }

        return client as OkHttpClient
    }

    private fun trustAllCerts(builder: OkHttpClient.Builder) {
        try {
            val trustManager = object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return emptyArray()
                }
            }

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
            val sslSocketFactory = sslContext.socketFactory

            builder.sslSocketFactory(sslSocketFactory, trustManager)
                    .hostnameVerifier { _, _ -> true }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

    }

    /**
     * Finds alternate links in the header of an HTML document pointing to feeds.
     */
    fun findFeedLink(html: String,
                     preferRss: Boolean = false,
                     preferAtom: Boolean = false): String? {
        val doc = Jsoup.parse(html.byteInputStream(), "UTF-8", "")
        val header = doc.head()

        val feedLinks = header.getElementsByAttributeValue("rel", "alternate")
                .filter { it.hasAttr("href") && it.hasAttr("type") }
                .filter {
                    val t = it.attr("type").toLowerCase()
                    when {
                        t.contains("application/atom") -> true
                        t.contains("application/rss") -> true
                        else -> false
                    }
                }
                .sortedBy {
                    val t = it.attr("type").toLowerCase()
                    when {
                        preferAtom && t.contains("atom") -> "0"
                        preferRss && t.contains("rss") -> "1"
                        else -> t
                    }
                }

        return feedLinks.firstOrNull()?.attr("href")
    }

    @Throws(FeedParser.FeedParsingError::class)
    fun parseFeed(feedUrl: String, cacheDir: File): SyndFeed {
        var url = feedUrl
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url
            }

            val request = Request.Builder()
                    .url(url)
                    .build()

            val cacheClient = cachingClient(cacheDir)
            val call = cacheClient.newCall(request)
            val response = call.execute()

            if (!response.isSuccessful) {
                throw IOException("Unexpected code " + response)
            }

            response.use {
                Log.d("RSSLOCAL", "cache response: " + response.cacheResponse())
                Log.d("RSSLOCAL", "network response: " + response.networkResponse())

                val body = response.body()?.string()

                if (body != null) {

                    val alternateFeedLink = findFeedLink(body,
                            preferAtom = true)

                    return if (alternateFeedLink != null) {
                        parseFeed(alternateFeedLink, cacheDir)
                    } else {
                        parseFeed(body)
                    }
                }
                throw NullPointerException("Response body was null")
            }
        } catch (e: Throwable) {
            throw FeedParsingError(e)
        }

    }

    @Throws(FeedParser.FeedParsingError::class)
    fun parseFeed(feedXml: String): SyndFeed = parseFeed(feedXml.byteInputStream())

    @Throws(FeedParser.FeedParsingError::class)
    fun parseFeed(`is`: InputStream): SyndFeed {
        `is`.use {
            try {
                val feed = SyndFeedInput().build(XmlReader(`is`))

                feed.entries
                        .filter { it.authors.isEmpty() }
                        .forEach { it.authors = feed.authors }

                return feed
            } catch (e: Throwable) {
                throw FeedParsingError(e)
            }
        }
    }

    fun firstEnclosure(entry: SyndEntry): String? {
        if (!entry.enclosures.isEmpty()) {
            val enclosure = entry.enclosures[0]
            if (enclosure.url != null) {
                return enclosure.url
            }
        }

        return null
    }

    fun publishDate(entry: SyndEntry): String? {
        if (entry.publishedDate != null) {
            return DateTime(entry.publishedDate.time).toDateTimeISO().toString()
        }
        // This is the required element in atom feeds
        return if (entry.updatedDate != null) {
            DateTime(entry.updatedDate.time).toDateTimeISO().toString()
        } else null
    }

    fun title(entry: SyndEntry): String {
        return entry.title ?: ""
    }

    fun plainTitle(entry: SyndEntry): String {
        return HtmlToPlainTextConverter.HtmlToPlainText(title(entry))
    }

    fun description(entry: SyndEntry): String {
        // Atom
        if (!entry.contents.isEmpty()) {
            val contents = entry.contents
            var content: SyndContent? = null

            // In case of multiple contents, prioritize html
            for (c in contents) {
                if (content == null) {
                    content = c
                } else if ("html".equals(content.type, ignoreCase = true) || "xhtml".equals(content.type, ignoreCase = true)) {
                    // Already html
                    break
                } else if ("html".equals(c.type, ignoreCase = true) || "xhtml".equals(c.type, ignoreCase = true)) {
                    content = c
                    break
                }
            }

            return content?.value ?: ""
        }

        // Rss
        return entry.description?.value ?: ""

        // In case of faulty feed
    }

    /**
     *
     * @return null in case no self links exist - which is true for some RSS feeds
     */
    fun selfLink(feed: SyndFeed): String? {
        // entry.getUri() can return bad data in case of atom feeds where it returns the ID element

        return feed.links
                .firstOrNull { "self".equals(it.rel, ignoreCase = true) }
                ?.href
    }

    fun snippet(entry: SyndEntry): String {
        val text = HtmlToPlainTextConverter.HtmlToPlainText(FeedParser.description(entry))
        return text.substring(0, min(200, text.length))
    }

    fun thumbnail(entry: SyndEntry): String? {
        val media = entry.getModule(MediaModule.URI) as MediaEntryModule?
        if (media != null) {
            val thumbnails = media.metadata.thumbnail
            if (thumbnails != null && thumbnails.isNotEmpty()) {
                return thumbnails[0].url.toString()
            }
            // Fallback to images
            val contents = media.mediaContents
            if (contents != null && contents.isNotEmpty()) {
                return contents
                        .firstOrNull { "image".equals(it.medium, ignoreCase = true) }
                        ?.reference?.toString()
            }
        }

        return null
    }

    class FeedParsingError(e: Throwable) : Exception(e)
}
