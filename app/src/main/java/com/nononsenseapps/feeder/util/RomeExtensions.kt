package com.nononsenseapps.feeder.util

import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.nononsenseapps.jsonfeed.Attachment
import com.nononsenseapps.jsonfeed.Author
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.Item
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.modules.mediarss.MediaModule
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndPerson
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
    val contentText = this.contentText()
    return Item(
            id = relativeLinkIntoAbsoluteOrNull(baseUrl, this.uri),
            url = this.linkToHtml(baseUrl),
            title = plainTitle(),
            content_text = contentText,
            content_html = this.contentHtml(),
            summary = contentText.take(200),
            image = this.thumbnail(baseUrl),
            date_published = this.publishedRFC3339Date(),
            date_modified = this.modifiedRFC3339Date(),
            author = this.authors?.firstOrNull()?.asAuthor() ?: feedAuthor,
            attachments = this.enclosures?.map { it.asAttachment(baseUrl = baseUrl) }
    )
}

/**
 * Returns an absolute link, or null
 */
fun SyndEntry.linkToHtml(feedBaseUrl: URL): String? {
    val alternateHtml = this.links?.firstOrNull { "alternate" == it.rel && "text/html" == it.type }
    if (alternateHtml != null) {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, alternateHtml.href)
    }

    val selfHtml = this.links?.firstOrNull { "self" == it.rel && "text/html" == it.type }
    if (selfHtml != null) {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, selfHtml.href)
    }

    val self = this.links?.firstOrNull { "self" == it.rel }
    if (self != null) {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, self.href)
    }

    return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, this.link)
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

fun SyndEntry.contentText(): String {
    val possiblyHtml = when {
        contents != null && !contents.isEmpty() -> { // Atom
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

fun findImageLinkInEnclosures(enclosures: List<SyndEnclosure?>?): String? {
    if (enclosures != null) {
        for (enclosure in enclosures) {
            if (enclosure != null) {
                if (enclosure.type != null && enclosure.url != null && enclosure.type.startsWith("image/")) {
                    return enclosure.url
                }
            }
        }
    }
    return null
}

/**
 * Returns an absolute link, or null
 */
fun SyndEntry.thumbnail(feedBaseUrl: URL): String? {
    val media = this.getModule(MediaModule.URI) as MediaEntryModule?
    val thumbnails = media?.metadata?.thumbnail
    val contents = media?.mediaContents
    val enclosures: List<SyndEnclosure?>? = this.enclosures

    return when {
        thumbnails?.isNotEmpty()
                ?: false -> relativeLinkIntoAbsoluteOrNull(feedBaseUrl, thumbnails?.firstOrNull()?.url?.toString())
        contents?.isNotEmpty() ?: false -> {
            relativeLinkIntoAbsoluteOrNull(feedBaseUrl, contents?.firstOrNull { "image" == it.medium }
                    ?.reference?.toString())
        }
        else -> {
            val imgLink: String? = findImageLinkInEnclosures(enclosures) ?: naiveFindImageLink(this.contentHtml())
            // Now we are resolving against original, not the feed
            val siteBaseUrl: String? = this.linkToHtml(feedBaseUrl)

            when {
                siteBaseUrl != null && imgLink != null -> relativeLinkIntoAbsoluteOrNull(URL(siteBaseUrl), imgLink)
                imgLink != null -> relativeLinkIntoAbsoluteOrNull(feedBaseUrl, imgLink)
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
