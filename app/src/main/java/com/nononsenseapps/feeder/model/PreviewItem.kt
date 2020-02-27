package com.nononsenseapps.feeder.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import org.threeten.bp.ZonedDateTime
import java.net.URI
import java.net.URL

const val previewColumns = "feed_items.id AS id, guid, plain_title, plain_snippet, feed_items.image_url, enclosure_link, " +
        "author, pub_date, link, unread, feeds.tag AS tag, feeds.id AS feed_id, feeds.title AS feed_title, feeds.custom_title as feed_customtitle, feeds.url AS feed_url"

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
        var unread: Boolean = true,
        @ColumnInfo(name = "feed_id") var feedId: Long? = null,
        @ColumnInfo(name = "feed_title") var feedTitle: String = "",
        @ColumnInfo(name = "feed_customtitle") var feedCustomTitle: String = "",
        @ColumnInfo(name = "feed_url") var feedUrl: URL = sloppyLinkToStrictURLNoThrows("")
) {
    constructor() : this(id = ID_UNSET)

    val feedDisplayTitle: String
        get() = if (feedCustomTitle.isBlank()) feedTitle else feedCustomTitle

    val enclosureFilename: String?
        get() {
            if (enclosureLink != null) {
                var fname: String? = null
                try {
                    fname = URI(enclosureLink).path.split("/").last()
                } catch (e: Exception) {
                }
                if (fname == null || fname.isEmpty()) {
                    return null
                } else {
                    return fname
                }
            }
            return null
        }

    val pubDateString: String?
        get() = pubDate?.toString()

    val domain: String?
        get() {
            val l: String? = enclosureLink ?: link
            if (l != null) {
                try {
                    return URL(l).host.replace("www.", "")
                } catch (e: Throwable) {
                }
            }
            return null
        }
}
