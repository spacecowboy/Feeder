package com.nononsenseapps.feeder.db.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nononsenseapps.feeder.db.COL_AUTHOR
import com.nononsenseapps.feeder.db.COL_BOOKMARKED
import com.nononsenseapps.feeder.db.COL_ENCLOSURELINK
import com.nononsenseapps.feeder.db.COL_FEEDID
import com.nononsenseapps.feeder.db.COL_FIRSTSYNCEDTIME
import com.nononsenseapps.feeder.db.COL_FULLTEXT_DOWNLOADED
import com.nononsenseapps.feeder.db.COL_GUID
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_IMAGEURL
import com.nononsenseapps.feeder.db.COL_LINK
import com.nononsenseapps.feeder.db.COL_NOTIFIED
import com.nononsenseapps.feeder.db.COL_PLAINSNIPPET
import com.nononsenseapps.feeder.db.COL_PLAINTITLE
import com.nononsenseapps.feeder.db.COL_PRIMARYSORTTIME
import com.nononsenseapps.feeder.db.COL_PUBDATE
import com.nononsenseapps.feeder.db.COL_READ_TIME
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.db.FEED_ITEMS_TABLE_NAME
import com.nononsenseapps.feeder.model.host
import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.jsonfeed.Item
import java.net.URI
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

const val MAX_TITLE_LENGTH = 200
const val MAX_SNIPPET_LENGTH = 200

@Entity(
    tableName = FEED_ITEMS_TABLE_NAME,
    indices = [
        Index(value = [COL_GUID, COL_FEEDID], unique = true),
        Index(value = [COL_FEEDID]),
        Index(
            name = "idx_feed_items_cursor",
            value = [COL_PRIMARYSORTTIME, COL_PUBDATE, COL_ID],
            unique = true,
        ),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Feed::class,
            parentColumns = [COL_ID],
            childColumns = [COL_FEEDID],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class FeedItem @Ignore constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COL_ID)
    override var id: Long = ID_UNSET,
    @ColumnInfo(name = COL_GUID) var guid: String = "",
    @Deprecated("This is never different from plainTitle", replaceWith = ReplaceWith("plainTitle"))
    @ColumnInfo(name = COL_TITLE)
    var title: String = "",
    @ColumnInfo(name = COL_PLAINTITLE) var plainTitle: String = "",
    @ColumnInfo(name = COL_PLAINSNIPPET) var plainSnippet: String = "",
    @ColumnInfo(name = COL_IMAGEURL) var imageUrl: String? = null,
    @ColumnInfo(name = COL_ENCLOSURELINK) var enclosureLink: String? = null,
    @ColumnInfo(name = COL_AUTHOR) var author: String? = null,
    @ColumnInfo(name = COL_PUBDATE, typeAffinity = ColumnInfo.TEXT) override var pubDate: ZonedDateTime? = null,
    @ColumnInfo(name = COL_LINK) override var link: String? = null,
    @Deprecated("This column has been 'removed' but sqlite doesn't support drop column.", replaceWith = ReplaceWith("readTime"))
    @ColumnInfo(name = "unread")
    var oldUnread: Boolean = true,
    @ColumnInfo(name = COL_NOTIFIED) var notified: Boolean = false,
    @ColumnInfo(name = COL_FEEDID) var feedId: Long? = null,
    @ColumnInfo(name = COL_FIRSTSYNCEDTIME, typeAffinity = ColumnInfo.INTEGER) var firstSyncedTime: Instant = Instant.EPOCH,
    @ColumnInfo(name = COL_PRIMARYSORTTIME, typeAffinity = ColumnInfo.INTEGER) override var primarySortTime: Instant = Instant.EPOCH,
    @Deprecated("This column has been 'removed' but sqlite doesn't support drop column.")
    @ColumnInfo(name = "pinned")
    var oldPinned: Boolean = false,
    @ColumnInfo(name = COL_BOOKMARKED) var bookmarked: Boolean = false,
    @ColumnInfo(name = COL_FULLTEXT_DOWNLOADED) var fullTextDownloaded: Boolean = false,
    @ColumnInfo(name = COL_READ_TIME, typeAffinity = ColumnInfo.INTEGER) var readTime: Instant? = null,
) : FeedItemForFetching, FeedItemCursor {

    constructor() : this(id = ID_UNSET)

    val unread: Boolean
        get() = readTime == null

    fun updateFromParsedEntry(
        entry: Item,
        entryGuid: String,
        feed: com.nononsenseapps.jsonfeed.Feed,
    ) {
        val converter = HtmlToPlainTextConverter()
        // Be careful about nulls.
        val text = entry.content_html ?: entry.content_text ?: ""
        val summary: String = (
            entry.summary ?: entry.content_text
                ?: converter.convert(text)
            ).take(MAX_SNIPPET_LENGTH)

        // Make double sure no base64 images are used as thumbnails
        val safeImage = when {
            entry.image?.startsWith("data") == true -> null
            else -> entry.image
        }

        val absoluteImage = when {
            feed.feed_url != null && safeImage != null -> {
                relativeLinkIntoAbsolute(sloppyLinkToStrictURL(feed.feed_url), safeImage)
            }
            else -> safeImage
        }

        this.guid = entryGuid
        entry.title?.let { this.plainTitle = it.take(MAX_TITLE_LENGTH) }
        @Suppress("DEPRECATION")
        this.title = this.plainTitle
        this.plainSnippet = summary

        this.imageUrl = absoluteImage
        this.enclosureLink = entry.attachments?.firstOrNull()?.url
        this.author = entry.author?.name ?: feed.author?.name
        this.link = entry.url

        this.pubDate =
            try {
                // Allow an actual pubdate to be updated
                ZonedDateTime.parse(entry.date_published)
            } catch (t: Throwable) {
                // If a pubdate is missing, then don't update if one is already set
                this.pubDate ?: ZonedDateTime.now(ZoneOffset.UTC)
            }
        primarySortTime = minOf(firstSyncedTime, pubDate?.toInstant() ?: firstSyncedTime)
    }

    val enclosureFilename: String?
        get() {
            enclosureLink?.let { enclosureLink ->
                var fname: String? = null
                try {
                    fname = URI(enclosureLink).path.split("/").last()
                } catch (_: Exception) {
                }
                return if (fname.isNullOrEmpty()) {
                    null
                } else {
                    fname
                }
            }
            return null
        }

    val domain: String?
        get() {
            return (enclosureLink ?: link)?.host()
        }
}

interface FeedItemForFetching {
    val id: Long
    val link: String?
}

interface FeedItemCursor {
    val primarySortTime: Instant
    val pubDate: ZonedDateTime?
    val id: Long
}
