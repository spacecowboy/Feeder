package com.nononsenseapps.feeder.util

import android.util.Log
import com.nononsenseapps.feeder.model.EnclosureImage
import com.nononsenseapps.feeder.model.MediaImage
import com.nononsenseapps.feeder.model.ParsedArticle
import com.nononsenseapps.feeder.model.ParsedAuthor
import com.nononsenseapps.feeder.model.ParsedEnclosure
import com.nononsenseapps.feeder.model.ParsedFeed
import com.nononsenseapps.feeder.model.ThumbnailImage
import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.modules.mediarss.MediaModule
import com.rometools.modules.mediarss.types.MediaContent
import com.rometools.modules.mediarss.types.MediaGroup
import com.rometools.modules.mediarss.types.Thumbnail
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndPerson
import java.net.URL
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun SyndFeed.asFeed(baseUrl: URL): ParsedFeed {
    val feedAuthor: ParsedAuthor? = this.authors?.firstOrNull()?.asAuthor()

    val siteUrl =
        relativeLinkIntoAbsoluteOrNull(
            baseUrl,
            this.links?.firstOrNull {
                "alternate" == it.rel && "text/html" == it.type
            }?.href ?: this.link,
        )

    // Base64 encoded images can be quite large - and crash database cursors
    val icon =
        try {
            image?.url?.let { url ->
                when {
                    url.startsWith("http") -> url
                    else -> null
                }
            }
        } catch (e: Exception) {
            Log.e("FEEDER_ROME", "Unable to find feed icon", e)
            null
        }

    try {
        return ParsedFeed(
            title = plainTitle(),
            home_page_url = siteUrl,
            feed_url =
                relativeLinkIntoAbsoluteOrNull(
                    baseUrl,
                    this.links?.firstOrNull { "self" == it.rel }?.href,
                ),
            description = this.description,
            icon = icon,
            author = feedAuthor,
            items = this.entries?.map { it.asItem(baseUrl = baseUrl, feedAuthor = feedAuthor) },
        )
    } catch (t: Throwable) {
        throw t
    }
}

fun SyndEntry.asItem(
    baseUrl: URL,
    feedAuthor: ParsedAuthor? = null,
): ParsedArticle {
    try {
        val contentText =
            contentText().orIfBlank {
                mediaDescription() ?: ""
            }
        // Base64 encoded images can be quite large - and crash database cursors
        val image =
            thumbnail(baseUrl)?.let { img ->
                when {
                    img.url.startsWith("data:") -> null
                    else -> img
                }
            }
        val writer =
            when (author?.isNotBlank()) {
                true -> ParsedAuthor(name = author)
                else -> feedAuthor
            }

        return ParsedArticle(
            id = relativeLinkIntoAbsoluteOrNull(baseUrl, this.uri),
            url = linkToHtml(baseUrl),
            title = plainTitle(),
            content_text = contentText,
            content_html = contentHtml(),
            summary = contentText.take(200),
            image = image,
            date_published = publishedRFC3339Date(),
            date_modified = modifiedRFC3339Date(),
            author = writer,
            attachments = enclosures?.map { it.asAttachment(baseUrl = baseUrl) },
        )
    } catch (t: Throwable) {
        throw t
    }
}

fun String.orIfBlank(block: () -> String): String =
    when (this.isBlank()) {
        true -> block()
        false -> this
    }

/**
 * Returns an absolute link, or null
 */
fun SyndEntry.linkToHtml(feedBaseUrl: URL): String? {
    this.links?.firstOrNull { "alternate" == it.rel && "text/html" == it.type }?.let {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, it.href)
    }

    this.links?.firstOrNull { "self" == it.rel && "text/html" == it.type }?.let {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, it.href)
    }

    this.links?.firstOrNull { "self" == it.rel }?.let {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, it.href)
    }

    this.link?.let {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, it)
    }

    return null
}

fun SyndEnclosure.asAttachment(baseUrl: URL): ParsedEnclosure {
    return ParsedEnclosure(
        url =
            relativeLinkIntoAbsoluteOrNull(
                baseUrl,
                this.url,
            ),
        mime_type = this.type,
        size_in_bytes = this.length,
    )
}

fun SyndPerson.asAuthor(): ParsedAuthor {
    val url: String? =
        when {
            this.uri != null -> this.uri
            this.email != null -> "mailto:${this.email}"
            else -> null
        }
    return ParsedAuthor(
        name = this.name,
        url = url,
    )
}

fun SyndEntry.contentText(): String {
    val possiblyHtml =
        when {
            contents != null && contents.isNotEmpty() -> { // Atom
                val contents = contents
                var possiblyHtml: String? = null

                for (c in contents) {
                    if ("text" == c.type && c.value != null) {
                        return c.value
                    } else if (null == c.type && c.value != null) {
                        // Suspect it might be text as per the Rome docs
                        // https://github.com/ralph-tice/rome/blob/master/src/main/java/com/sun/syndication/feed/synd/SyndContent.java
                        possiblyHtml = c.value
                        break
                    } else if (("html" == c.type || "xhtml" == c.type) && c.value != null) {
                        possiblyHtml = c.value
                    } else if (possiblyHtml == null && c.value != null) {
                        possiblyHtml = c.value
                    }
                }

                possiblyHtml
            }
            else -> // Rss
                description?.value
        }

    val result = HtmlToPlainTextConverter().convert(possiblyHtml ?: "")

    // Description consists of at least one image, avoid opening browser for this item
    return when {
        result.isBlank() && possiblyHtml?.contains("img") == true ->
            "<image>"
        else -> result
    }
}

private fun convertAtomContentToPlainText(
    content: SyndContent?,
    fallback: String?,
): String {
    return HtmlToPlainTextConverter().convert(content?.value ?: fallback ?: "")
}

fun SyndFeed.plainTitle(): String = convertAtomContentToPlainText(titleEx, title)

fun SyndEntry.plainTitle(): String = convertAtomContentToPlainText(titleEx, title)

fun SyndEntry.contentHtml(): String? {
    contents?.minByOrNull {
        when (it.type) {
            "xhtml", "html" -> 0
            else -> 1
        }
    }?.let {
        return it.value
    }

    return description?.value
}

fun SyndEntry.mediaDescription(): String? {
    val media = this.getModule(MediaModule.URI) as MediaEntryModule?

    return media?.metadata?.description
        ?: media?.mediaContents?.firstOrNull {
            it.metadata?.description?.isNotBlank() == true
        }?.metadata?.description
        ?: media?.mediaGroups?.firstOrNull {
            it.metadata?.description?.isNotBlank() == true
        }?.metadata?.description
}

/**
 * Returns an absolute link, or null
 */
fun SyndEntry.thumbnail(feedBaseUrl: URL): ThumbnailImage? {
    val media = this.getModule(MediaModule.URI) as MediaEntryModule?

    val thumbnailCandidates =
        sequence {
            media?.findThumbnailCandidates(feedBaseUrl)?.let {
                yieldAll(it)
            }
            enclosures?.asSequence()
                ?.mapNotNull { it.findThumbnailCandidate(feedBaseUrl) }
                ?.let {
                    yieldAll(it)
                }
        }

    val thumbnail = thumbnailCandidates.maxByOrNull { it.width ?: -1 }

    if (thumbnail is EnclosureImage && (thumbnail.length == 0L || thumbnail.length > 50_000L)) {
        // Enclosures don't have width/height, so guessing from length
        return thumbnail
    } else if (thumbnail != null && (thumbnail.width ?: 0) >= 640) {
        return thumbnail
    }

    // Either image is too small for liking, or no image found. Try to find one in the content first

    // Now we are resolving against original, not the feed
    val baseUrl: String = this.linkToHtml(feedBaseUrl) ?: feedBaseUrl.toString()

    return findFirstImageInHtml(this.contentHtml(), baseUrl) ?: thumbnail
}

private fun MediaEntryModule.findThumbnailCandidates(feedBaseUrl: URL): Sequence<ThumbnailImage> {
    return sequence {
        mediaContents?.forEach { mediaContent ->
            yieldAll(mediaContent.findThumbnailCandidates(feedBaseUrl))
        }
        metadata?.thumbnail?.let { thumbnails ->
            yieldAll(
                thumbnails.mapNotNull { it.findThumbnailCandidate(feedBaseUrl) },
            )
        }
        mediaGroups?.forEach { mediaGroup ->
            yieldAll(mediaGroup.findThumbnailCandidates(feedBaseUrl))
        }
    }
}

private fun SyndEnclosure.findThumbnailCandidate(feedBaseUrl: URL): ThumbnailImage? {
    if (type?.startsWith("image/") == true) {
        url?.let { url ->
            return EnclosureImage(
                url = relativeLinkIntoAbsolute(feedBaseUrl, url),
                length = this.length,
            )
        }
    }
    return null
}

private fun MediaGroup.findThumbnailCandidates(feedBaseUrl: URL): Sequence<ThumbnailImage> =
    sequence {
        metadata.thumbnail?.forEach { thumbnail ->
            thumbnail.findThumbnailCandidate(feedBaseUrl)?.let { thumbnailCandidate ->
                yield(thumbnailCandidate)
            }
        }
    }

private fun Thumbnail.findThumbnailCandidate(feedBaseUrl: URL): ThumbnailImage? {
    return url?.let { url ->
        MediaImage(
            width = width,
            height = height,
            url = relativeLinkIntoAbsolute(feedBaseUrl, url.toString()),
        )
    }
}

private fun MediaContent.findThumbnailCandidates(feedBaseUrl: URL): Sequence<ThumbnailImage> =
    sequence {
        metadata?.thumbnail?.forEach { thumbnail ->
            thumbnail.findThumbnailCandidate(feedBaseUrl)?.let { thumbnailCandidate ->
                yield(thumbnailCandidate)
            }
        }

        if (isImage()) {
            reference?.let { ref ->
                yield(
                    MediaImage(
                        width = width,
                        height = height,
                        url = relativeLinkIntoAbsolute(feedBaseUrl, ref.toString()),
                    ),
                )
            }
        }
    }

private fun MediaContent.isImage(): Boolean {
    return when {
        medium == "image" -> true
        pointsToImage(reference.toString()) -> true
        else -> false
    }
}

private fun pointsToImage(url: String): Boolean {
    return try {
        val u = URL(url)

        u.path.endsWith(".jpg", ignoreCase = true) ||
            u.path.endsWith(".jpeg", ignoreCase = true) ||
            u.path.endsWith(".gif", ignoreCase = true) ||
            u.path.endsWith(".png", ignoreCase = true) ||
            u.path.endsWith(".webp", ignoreCase = true)
    } catch (_: Exception) {
        false
    }
}

fun SyndEntry.publishedRFC3339ZonedDateTime(): ZonedDateTime? =
    when (publishedDate != null) {
        true ->
            ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(publishedDate.time),
                ZoneOffset.systemDefault(),
            )
        // This is the required element in atom feeds so it is a good fallback
        else -> modifiedRFC3339ZonedDateTime()
    }

fun SyndEntry.modifiedRFC3339ZonedDateTime(): ZonedDateTime? =
    when (updatedDate != null) {
        true ->
            ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(updatedDate.time),
                ZoneOffset.systemDefault(),
            )
        else -> null
    }

fun SyndEntry.publishedRFC3339Date(): String? = publishedRFC3339ZonedDateTime()?.toString()

fun SyndEntry.modifiedRFC3339Date(): String? = modifiedRFC3339ZonedDateTime()?.toString()
