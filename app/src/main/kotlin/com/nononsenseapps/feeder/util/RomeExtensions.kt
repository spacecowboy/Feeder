package com.nononsenseapps.feeder.util

import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.nononsenseapps.jsonfeed.Attachment
import com.nononsenseapps.jsonfeed.Author
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.Item
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.modules.mediarss.MediaModule
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndPerson
import org.joda.time.DateTime

fun SyndFeed.asFeed(): Feed {
    val feedAuthor: Author? = this.authors?.firstOrNull()?.asAuthor()
    return Feed(
            title = this.title,
            home_page_url = this.links?.firstOrNull {
                "alternate" == it.rel && "text/html" == it.type
            }?.href ?: this.link,
            feed_url = this.links?.firstOrNull{ "self" == it.rel }?.href,
            description = this.description,
            icon = this.image?.url,
            author = feedAuthor,
            items = this.entries?.map { it.asItem(feedAuthor = feedAuthor) }
    )
}

fun SyndEntry.asItem(feedAuthor: Author? = null): Item {
    val contentText = this.contentText()
    return Item(
            id = this.uri,
            url = this.feedUrl(),
            title = this.title,
            content_text = contentText,
            content_html = this.contentHtml(),
            summary = contentText.take(200),
            image = this.thumbnail(),
            date_published = this.publishedRFC3339Date(),
            date_modified = this.modifiedRFC3339Date(),
            author = this.authors?.firstOrNull()?.asAuthor() ?: feedAuthor,
            attachments = this.enclosures?.map { it.asAttachment() }
    )
}

fun SyndEntry.feedUrl(): String? =
    this.links?.firstOrNull{ "self" == it.rel }?.href ?: this.link

fun SyndEnclosure.asAttachment(): Attachment {
    return Attachment(
            url = this.url,
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

    val result = HtmlToPlainTextConverter.HtmlToPlainText(possiblyHtml ?: "")

    // Description consists of at least one image, avoid opening browser for this item
    return when {
        result.isBlank() && possiblyHtml?.contains("img") == true ->
            "<image>"
        else -> result
    }
}

fun SyndEntry.contentHtml(): String? {
    var possiblyHtml: String? = null

    // Atom
    if (contents != null && !contents.isEmpty()) {
        for (c in contents) {
            if ("html" == c.type || "xhtml" == c.type) {
                if (c.value != null) {
                    possiblyHtml = c.value
                }
            } else if (possiblyHtml == null) {
                possiblyHtml = c.value
            }
        }
    }

    // Rss
    if (possiblyHtml == null) {
        possiblyHtml = description?.value
    }

    return possiblyHtml ?: ""
}

fun SyndEntry.thumbnail(): String? {
    val media = this.getModule(MediaModule.URI) as MediaEntryModule?
    val thumbnails = media?.metadata?.thumbnail
    val contents = media?.mediaContents

    return when {
        thumbnails?.isNotEmpty() ?: false -> thumbnails?.firstOrNull()?.url?.toString()
        contents?.isNotEmpty() ?: false -> {
            contents?.firstOrNull { "image" == it.medium }
                    ?.reference?.toString()
        }
        else -> {
            val imgLink: String? = naiveFindImageLink(this.contentHtml())
            val feedUrl: String? = this.feedUrl()

            when {
                feedUrl != null && imgLink != null -> relativeLinkIntoAbsolute(sloppyLinkToStrictURL(feedUrl), imgLink)
                imgLink != null -> sloppyLinkToStrictURL(imgLink).toString()
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
