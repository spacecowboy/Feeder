package com.nononsenseapps.feeder.util

import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.nononsenseapps.jsonfeed.Attachment
import com.nononsenseapps.jsonfeed.Author
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.Item
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.modules.mediarss.MediaModule
import com.rometools.rome.feed.synd.*
import org.joda.time.DateTime
import org.jsoup.parser.Parser.unescapeEntities
import java.net.URL

fun SyndFeed.asFeed(baseUrl: URL): Feed {
    val feedAuthor: Author? = this.authors?.firstOrNull()?.asAuthor()

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
            icon = this.image?.url,
            author = feedAuthor,
            items = this.entries?.map { it.asItem(baseUrl = baseUrl, feedAuthor = feedAuthor) }
    )
}

fun SyndEntry.asItem(baseUrl: URL, feedAuthor: Author? = null): Item {
    val contentText = contentText().orIfBlank {
        mediaDescription() ?: ""
    }
    return Item(
            id = relativeLinkIntoAbsoluteOrNull(baseUrl, this.uri),
            url = linkToHtml(baseUrl),
            title = plainTitle(),
            content_text = contentText,
            content_html = contentHtml(),
            summary = contentText.take(200),
            image = thumbnail(baseUrl),
            date_published = publishedRFC3339Date(),
            date_modified = modifiedRFC3339Date(),
            author = authors?.firstOrNull()?.asAuthor() ?: feedAuthor,
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

    this.uri?.let {
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
    return Author(name = this.name,
            url = url)
}

fun SyndEntry.contentProbablyHtml(): String? {
    var possiblyHtml: String? = contents?.filter {
        when (it.type) {
            "xhtml", "html" -> true
            else -> false
        }
    }?.take(1)?.map {
        it.value
    }?.firstOrNull()

    if (possiblyHtml == null) {
        possiblyHtml = contents?.firstOrNull()?.value
    }

    if (possiblyHtml == null) {
        possiblyHtml = description?.value
    }

    return possiblyHtml?.let { unescapeEntities(possiblyHtml, true) }
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
    return HtmlToPlainTextConverter().convert(possiblyHtmlFromContent(content, fallback))
}

private fun possiblyHtmlFromContent(content: SyndContent?, fallback: String?): String =
        when (content?.type == "html") {
            true -> unescapeEntities(content!!.value, true)
            false -> content?.value ?: fallback
        } ?: ""

fun SyndFeed.plainTitle(): String = convertAtomContentToPlainText(titleEx, title)
fun SyndEntry.plainTitle(): String = convertAtomContentToPlainText(titleEx, title)

fun SyndEntry.contentHtml(): String? {
    var possiblyHtml: String? = contents?.filter {
        when (it.type) {
            "xhtml", "html" -> true
            else -> false
        }
    }?.take(1)?.map {
        when (it.type) {
            "html" -> unescapeEntities(it.value, true)
            else -> it.value
        }
    }?.firstOrNull()

    if (possiblyHtml == null) {
        possiblyHtml = contents?.firstOrNull()?.value
    }

    if (possiblyHtml == null) {
        possiblyHtml = description?.value
    }

    return possiblyHtml ?: ""
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
            val imgLink: String? = naiveFindImageLink(this.contentProbablyHtml())
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

fun SyndEntry.publishedRFC3339Date(): String? =
        when (publishedDate != null) {
            true -> DateTime(publishedDate.time).toDateTimeISO().toString()
            else -> modifiedRFC3339Date() // This is the required element in atom feeds so it is a good fallback
        }

fun SyndEntry.modifiedRFC3339Date(): String? =
        when (updatedDate != null) {
            true -> DateTime(updatedDate.time).toDateTimeISO().toString()
            else -> null
        }
