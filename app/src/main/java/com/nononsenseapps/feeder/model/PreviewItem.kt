package com.nononsenseapps.feeder.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.nononsenseapps.feeder.db.COL_BOOKMARKED
import com.nononsenseapps.feeder.db.COL_PRIMARYSORTTIME
import com.nononsenseapps.feeder.db.COL_READ_TIME
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import java.net.URI
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

const val previewColumns = """
    feed_items.id AS id, guid, plain_title, plain_snippet, feed_items.image_url, enclosure_link,
    author, pub_date, link, read_time, feeds.tag AS tag, feeds.id AS feed_id, feeds.title AS feed_title,
    feeds.custom_title as feed_customtitle, feeds.url AS feed_url,
    feeds.open_articles_with AS feed_open_articles_with, bookmarked,
    feeds.image_url as feed_image_url, primary_sort_time
"""

data class PreviewItem @Ignore constructor(
    var id: Long = ID_UNSET,
    var guid: String = "",
    @ColumnInfo(name = "plain_title") var plainTitle: String = "",
    @ColumnInfo(name = "plain_snippet") var plainSnippet: String = "",
    @ColumnInfo(name = "image_url") var imageUrl: String? = null,
    @ColumnInfo(name = "enclosure_link") var enclosureLink: String? = null,
    var author: String? = null,
    @ColumnInfo(name = "pub_date") var pubDate: ZonedDateTime? = null,
    var link: String? = null,
    var tag: String = "",
    @ColumnInfo(name = COL_READ_TIME) var readTime: Instant? = null,
    @ColumnInfo(name = "feed_id") var feedId: Long? = null,
    @ColumnInfo(name = "feed_title") var feedTitle: String = "",
    @ColumnInfo(name = "feed_customtitle") var feedCustomTitle: String = "",
    @ColumnInfo(name = "feed_url") var feedUrl: URL = sloppyLinkToStrictURLNoThrows(""),
    @ColumnInfo(name = "feed_open_articles_with") var feedOpenArticlesWith: String = "",
    @ColumnInfo(name = COL_BOOKMARKED) var bookmarked: Boolean = false,
    @ColumnInfo(name = "feed_image_url") var feedImageUrl: URL? = null,
    @ColumnInfo(name = COL_PRIMARYSORTTIME) var primarySortTime: Instant = Instant.EPOCH,
) {
    constructor() : this(id = ID_UNSET)

    val feedDisplayTitle: String
        get() = feedCustomTitle.ifBlank { feedTitle }

    val domain: String?
        get() {
            return (enclosureLink ?: link)?.host()
        }
}

fun String?.host(): String? {
    val l: String? = this
    if (l != null) {
        try {
            return URI(l).host
        } catch (_: Throwable) {
        }
    }
    return null
}
