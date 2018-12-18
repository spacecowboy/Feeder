package com.nononsenseapps.feeder.db.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nononsenseapps.feeder.db.COL_AUTHOR
import com.nononsenseapps.feeder.db.COL_DESCRIPTION
import com.nononsenseapps.feeder.db.COL_ENCLOSURELINK
import com.nononsenseapps.feeder.db.COL_FEEDID
import com.nononsenseapps.feeder.db.COL_GUID
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_IMAGEURL
import com.nononsenseapps.feeder.db.COL_LINK
import com.nononsenseapps.feeder.db.COL_NOTIFIED
import com.nononsenseapps.feeder.db.COL_PLAINSNIPPET
import com.nononsenseapps.feeder.db.COL_PLAINTITLE
import com.nononsenseapps.feeder.db.COL_PUBDATE
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.db.COL_UNREAD
import com.nononsenseapps.feeder.db.FEED_ITEMS_TABLE_NAME
import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.jsonfeed.Item
import org.joda.time.DateTime
import java.net.URI
import java.net.URL

@Entity(tableName = FEED_ITEMS_TABLE_NAME,
        indices = [Index(value = [COL_GUID, COL_FEEDID], unique = true),
            Index(value = [COL_FEEDID])],
        foreignKeys = [ForeignKey(entity = Feed::class,
                parentColumns = [COL_ID],
                childColumns = [COL_FEEDID],
                onDelete = CASCADE)])
data class FeedItem @Ignore constructor(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = COL_ID) var id: Long = ID_UNSET,
        @ColumnInfo(name = COL_GUID) var guid: String = "",
        @ColumnInfo(name = COL_TITLE) var title: String = "",
        @ColumnInfo(name = COL_DESCRIPTION) var description: String = "",
        @ColumnInfo(name = COL_PLAINTITLE) var plainTitle: String = "",
        @ColumnInfo(name = COL_PLAINSNIPPET) var plainSnippet: String = "",
        @ColumnInfo(name = COL_IMAGEURL) var imageUrl: String? = null,
        @ColumnInfo(name = COL_ENCLOSURELINK) var enclosureLink: String? = null,
        @ColumnInfo(name = COL_AUTHOR) var author: String? = null,
        @ColumnInfo(name = COL_PUBDATE, typeAffinity = ColumnInfo.TEXT) var pubDate: DateTime? = null,
        @ColumnInfo(name = COL_LINK) var link: String? = null,
        @ColumnInfo(name = COL_UNREAD) var unread: Boolean = true,
        @ColumnInfo(name = COL_NOTIFIED) var notified: Boolean = false,
        @ColumnInfo(name = COL_FEEDID) var feedId: Long? = null) {

    constructor() : this(id = ID_UNSET)

    fun updateFromParsedEntry(entry: Item, feed: com.nononsenseapps.jsonfeed.Feed) {
        val converter = HtmlToPlainTextConverter()
        // Be careful about nulls.
        val text = entry.content_html ?: entry.content_text ?: this.description
        val summary: String? = entry.summary ?: entry.content_text?.take(200) ?: converter.convert(text).take(200)
        val absoluteImage = when {
            feed.feed_url != null && entry.image != null -> relativeLinkIntoAbsolute(sloppyLinkToStrictURL(feed.feed_url!!), entry.image!!)
            else -> entry.image
        }

        entry.id?.let { this.guid = it }
        entry.title?.let { this.title = it }
        text.let { this.description = it }
        entry.title?.let { this.plainTitle = converter.convert(it) }
        summary?.let { this.plainSnippet = it }

        this.imageUrl = absoluteImage
        this.enclosureLink = entry.attachments?.firstOrNull()?.url
        this.author = entry.author?.name ?: feed.author?.name
        this.link = entry.url

        var dt: DateTime? = null
        try {
            dt = DateTime.parse(entry.date_published)
        } catch(t: Throwable) {
        }
        this.pubDate = dt
    }

    val pubDateString: String?
        get() = pubDate?.toString()

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
