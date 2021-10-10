package com.nononsenseapps.feeder.util

import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.nononsenseapps.jsonfeed.Attachment
import com.nononsenseapps.jsonfeed.Author
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.Item
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.modules.mediarss.MediaModule
import com.rometools.rome.feed.atom.Entry
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndPerson
import org.jsoup.parser.Parser.unescapeEntities
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import java.net.URL

fun SyndFeed.asFeed(baseUrl: URL): Feed {
    val feedAuthor: Author? = this.authors?.firstOrNull()?.asAuthor()

    // Base64 encoded images can be quite large - and crash database cursors
    val icon = image?.url?.let { url ->
        when {
            url.startsWith("data:") -> null
            else -> url
        }
    }

    return Feed(
        title = plainTitle(),
        home_page_url = relativeLinkIntoAbsoluteOrNull(
            baseUrl,
            this.links?.firstOrNull {
                "alternate" == it.rel && "text/html" == it.type
            }?.href ?: this.link
        ),
        feed_url = relativeLinkIntoAbsoluteOrNull(
            baseUrl,
            this.links?.firstOrNull { "self" == it.rel }?.href
        ),
        description = this.description,
        icon = icon,
        author = feedAuthor,
        items = this.entries?.map { it.asItem(baseUrl = baseUrl, feedAuthor = feedAuthor) }
    )
}

fun SyndEntry.asItem(baseUrl: URL, feedAuthor: Author? = null): Item {
    val contentText = contentText().orIfBlank {
        mediaDescription() ?: ""
    }
    // Base64 encoded images can be quite large - and crash database cursors
    val image = thumbnail(baseUrl)?.let { img ->
        when {
            img.startsWith("data:") -> null
            else -> img
        }
    }
    val writer = when (author?.isNotBlank()) {
        true -> Author(name = author)
        else -> feedAuthor
    }

    val fromRss = when (this.wireEntry) {
        is Entry -> false // Trust Atom feeds to have correct ids until proven otherwise
        else -> true
    }

    val rssSafeId = when (fromRss || this.uri == null) {
        true -> "${relativeLinkIntoAbsoluteOrNull(baseUrl, this.uri)}|${publishedRFC3339ZonedDateTime()?.toInstant()}|${plainTitle()}"
        false -> relativeLinkIntoAbsoluteOrNull(baseUrl, this.uri)
    }

    return Item(
        id = rssSafeId,
        url = linkToHtml(baseUrl),
        title = plainTitle(),
        content_text = contentText,
        content_html = contentHtml(),
        summary = contentText.take(200),
        image = image,
        date_published = publishedRFC3339Date(),
        date_modified = modifiedRFC3339Date(),
        author = writer,
        attachments = enclosures?.map { it.asAttachment(baseUrl = baseUrl) }
    )
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

fun SyndEnclosure.asAttachment(baseUrl: URL): Attachment {
    return Attachment(
        url = relativeLinkIntoAbsoluteOrNull(
            baseUrl,
            this.url
        ),
        mime_type = this.type,
        size_in_bytes = this.length
    )
}

fun SyndPerson.asAuthor(): Author {
    val url: String? = when {
        this.uri != null -> this.uri
        this.email != null -> "mailto:${this.email}"
        else -> null
    }
    return Author(
        name = this.name,
        url = url
    )
}

fun SyndEntry.contentText(): String {
    val possiblyHtml = when {
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

private fun convertAtomContentToPlainText(content: SyndContent?, fallback: String?): String {
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
        ?: media?.mediaContents?.firstOrNull { it.metadata?.description?.isNotBlank() == true }?.metadata?.description
        ?: media?.mediaGroups?.firstOrNull { it.metadata?.description?.isNotBlank() == true }?.metadata?.description
}

/**
 * Returns an absolute link, or null
 */
fun SyndEntry.thumbnail(feedBaseUrl: URL): String? {
    val media = this.getModule(MediaModule.URI) as MediaEntryModule?

    val thumbnail: String? = media?.metadata?.thumbnail?.firstOrNull()?.url?.toString()
        ?: media?.mediaContents?.firstOrNull { "image" == it.medium }?.reference?.toString()
        ?: media?.mediaGroups?.mapNotNull { it.metadata?.thumbnail?.firstOrNull() }?.firstOrNull()?.url?.toString()
        ?: enclosures?.asSequence()
            ?.filterNotNull()
            ?.filter { it.type?.startsWith("image/") == true }
            ?.mapNotNull { it.url }
            ?.firstOrNull()

    return when {
        thumbnail != null -> relativeLinkIntoAbsolute(feedBaseUrl, thumbnail)
        else -> {
            val imgLink: String? = naiveFindImageLink(this.contentHtml())?.let { unescapeEntities(it, true) }
            // Now we are resolving against original, not the feed
            val siteBaseUrl: String? = this.linkToHtml(feedBaseUrl)

            when {
                siteBaseUrl != null && imgLink != null -> relativeLinkIntoAbsolute(URL(siteBaseUrl), imgLink)
                imgLink != null -> relativeLinkIntoAbsolute(feedBaseUrl, imgLink)
                else -> null
            }
        }
    }
}

fun SyndEntry.publishedRFC3339ZonedDateTime(): ZonedDateTime? =
    when (publishedDate != null) {
        true -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(publishedDate.time), ZoneOffset.systemDefault())
        else -> modifiedRFC3339ZonedDateTime() // This is the required element in atom feeds so it is a good fallback
    }

fun SyndEntry.modifiedRFC3339ZonedDateTime(): ZonedDateTime? =
    when (updatedDate != null) {
        true -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(updatedDate.time), ZoneOffset.systemDefault())
        else -> null
    }

fun SyndEntry.publishedRFC3339Date(): String? =
    publishedRFC3339ZonedDateTime()?.toString()

fun SyndEntry.modifiedRFC3339Date(): String? =
    modifiedRFC3339ZonedDateTime()?.toString()
