package com.nononsenseapps.feeder.model

import android.annotation.SuppressLint
import android.util.Log
import com.nononsenseapps.feeder.util.asFeed
import com.nononsenseapps.jsonfeed.Feed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.io.InputStream
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
    fun parseFeed(feedUrl: String, cacheDir: File): Feed {
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
    private fun parseFeed(feedXml: String): Feed = parseFeed(feedXml.byteInputStream())

    @Throws(FeedParser.FeedParsingError::class)
    fun parseFeed(`is`: InputStream): Feed {
        `is`.use {
            try {
                val feed = SyndFeedInput().build(XmlReader(`is`))

                feed.entries
                        .filter { it.authors.isEmpty() }
                        .forEach { it.authors = feed.authors }

                return feed.asFeed()
            } catch (e: Throwable) {
                throw FeedParsingError(e)
            }
        }
    }

    class FeedParsingError(e: Throwable) : Exception(e)
}
