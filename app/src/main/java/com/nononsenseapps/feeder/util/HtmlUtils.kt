package com.nononsenseapps.feeder.util

import com.nononsenseapps.feeder.model.ImageFromHTML
import com.nononsenseapps.feeder.model.MediaImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.parser.Parser.unescapeEntities
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

fun findFirstImageInHtml(
    text: String?,
    baseUrl: String?,
): ImageFromHTML? =
    if (text != null) {
        val doc =
            unescapeEntities(text, true).byteInputStream().use {
                Jsoup.parse(it, "UTF-8", baseUrl ?: "")
            }

        doc
            .getElementsByTag("img")
            .asSequence()
            .filterNot { it.attr("width") == "1" || it.attr("height") == "1" }
            .map {
                // abs: will resolve relative urls against the baseurl
                ImageFromHTML(
                    url = it.attr("abs:src"),
                    width = it.attr("width").toIntOrNull(),
                    height = it.attr("height").toIntOrNull(),
                )
            }.firstOrNull {
                it.url.isNotBlank() == true &&
                    !it.url.contains("twitter_icon", ignoreCase = true) &&
                    !it.url.contains("facebook_icon", ignoreCase = true)
            }
    } else {
        null
    }

/**
 * Extract og:image (or twitter:image) from HTML head content.
 * The [html] should ideally be the `<head>` portion of the page,
 * but a full page works too — Jsoup will parse either.
 *
 * @param html  HTML content (at minimum the `<head>` section)
 * @param baseUrl base URL for resolving relative URLs
 * @return a [MediaImage] if a suitable meta image is found, null otherwise
 */
fun extractOgImage(
    html: String,
    baseUrl: String,
): MediaImage? {
    val doc = Jsoup.parse(html, baseUrl)

    // Prefer og:image, fall back to twitter:image
    val candidates = sequenceOf("og:image", "twitter:image")

    for (property in candidates) {
        val url =
            doc
                .select("meta[property=\"$property\"]")
                .mapNotNull { it.metaImageUrl() }
                .firstOrNull { it.isNotBlank() }
                ?: doc
                    .select("meta[name=\"$property\"]")
                    .mapNotNull { it.metaImageUrl() }
                    .firstOrNull { it.isNotBlank() }

        if (url != null) {
            return MediaImage(url = url)
        }
    }

    return null
}

private fun org.jsoup.nodes.Element.metaImageUrl(): String? {
    val rawUrl = attr("content").trim()
    if (rawUrl.isBlank()) return null

    return absUrl("content").ifBlank { rawUrl }
}

private fun MediaType.isHtml(): Boolean = subtype == "html" || subtype == "xhtml+xml"

/**
 * Stream only the `<head>` portion of a web page.
 * Reads just enough bytes to find `</head>`, keeping network usage minimal
 * (typically 1–2 KB vs 100+ KB for a full page).
 */
internal fun streamHeadContent(inputStream: InputStream): String {
    val sb = StringBuilder()
    val reader = InputStreamReader(inputStream, Charsets.UTF_8)
    val buffer = CharArray(READ_BUFFER_SIZE)
    val headEndMarker = "</head>"

    reader.use { stream ->
        while (true) {
            val read = stream.read(buffer)
            if (read == -1) break

            sb.append(buffer, 0, read)

            val headEndIndex = sb.toString().indexOf(headEndMarker, ignoreCase = true)
            if (headEndIndex >= 0) {
                // Trim to just past </head> — we only need the <head> section
                sb.setLength(headEndIndex + headEndMarker.length)
                break
            }

            // Safety: don't read more than MAX_HEAD_BYTES even without </head>
            if (sb.length > MAX_HEAD_BYTES) break
        }
    }

    return sb.toString()
}

/**
 * Fetch a web page's <head> section and extract og:image.
 * Returns null when no og:image is found or content is not HTML.
 * Throws [IOException] on network errors or non-2xx responses.
 */
internal suspend fun OkHttpClient.fetchOgImage(url: String): MediaImage? =
    withContext(Dispatchers.IO) {
        val request =
            Request
                .Builder()
                .url(url)
                .header("Accept", "text/html")
                .build()

        val response = newCall(request).execute()
        response.use {
            if (!it.isSuccessful) {
                throw IOException("HTTP ${it.code} fetching og:image for $url")
            }

            val body = it.body
            val contentType = body.contentType()
            if (contentType != null && !contentType.isHtml()) {
                return@withContext null
            }

            val headContent = streamHeadContent(body.byteStream())

            if (headContent.isBlank()) return@withContext null

            extractOgImage(headContent, it.request.url.toString())
        }
    }

private const val READ_BUFFER_SIZE = 2048
private const val MAX_HEAD_BYTES = 65536
