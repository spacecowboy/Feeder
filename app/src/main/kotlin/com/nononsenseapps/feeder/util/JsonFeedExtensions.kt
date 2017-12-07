package com.nononsenseapps.feeder.util

import android.content.ContentProviderOperation
import com.nononsenseapps.feeder.db.COL_AUTHOR
import com.nononsenseapps.feeder.db.COL_DESCRIPTION
import com.nononsenseapps.feeder.db.COL_ENCLOSURELINK
import com.nononsenseapps.feeder.db.COL_GUID
import com.nononsenseapps.feeder.db.COL_IMAGEURL
import com.nononsenseapps.feeder.db.COL_LINK
import com.nononsenseapps.feeder.db.COL_PLAINSNIPPET
import com.nononsenseapps.feeder.db.COL_PLAINTITLE
import com.nononsenseapps.feeder.db.COL_PUBDATE
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.Item

fun Item.intoContentProviderOperation(feed: Feed, builder: ContentProviderOperation.Builder) {
    // Be careful about nulls.
    val text = content_html ?: content_text ?: ""
    val summary = summary ?: content_text?.take(200) ?: HtmlToPlainTextConverter.HtmlToPlainText(text).take(200)

    val absoluteImage = when {
        feed.feed_url != null && image != null -> relativeLinkIntoAbsolute(sloppyLinkToStrictURL(feed.feed_url!!), image!!)
        else -> image
    }

    builder
            // These can be null
            .withValue(COL_LINK, url)
            .withValue(COL_IMAGEURL, absoluteImage)
            .withValue(COL_ENCLOSURELINK, attachments?.firstOrNull()?.url)
            .withValue(COL_AUTHOR, author?.name ?: feed.author?.name)
            .withValue(COL_PUBDATE, date_published)
            // Make sure these are non-null
            .withValue(COL_GUID, id ?: "")
            .withValue(COL_TITLE, title ?: "")
            .withValue(COL_DESCRIPTION, text)
            .withValue(COL_PLAINTITLE, title ?: "")
            .withValue(COL_PLAINSNIPPET, summary)
}

