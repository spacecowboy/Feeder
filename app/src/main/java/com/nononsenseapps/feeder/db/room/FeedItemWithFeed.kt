package com.nononsenseapps.feeder.db.room

import android.os.Bundle
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.nononsenseapps.feeder.db.COL_AUTHOR
import com.nononsenseapps.feeder.db.COL_DESCRIPTION
import com.nononsenseapps.feeder.db.COL_ENCLOSURELINK
import com.nononsenseapps.feeder.db.COL_FEEDID
import com.nononsenseapps.feeder.db.COL_FEEDTITLE
import com.nononsenseapps.feeder.db.COL_FEEDURL
import com.nononsenseapps.feeder.db.COL_GUID
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_IMAGEURL
import com.nononsenseapps.feeder.db.COL_LINK
import com.nononsenseapps.feeder.db.COL_PLAINSNIPPET
import com.nononsenseapps.feeder.db.COL_PLAINTITLE
import com.nononsenseapps.feeder.db.COL_PUBDATE
import com.nononsenseapps.feeder.db.COL_TAG
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.db.COL_UNREAD
import com.nononsenseapps.feeder.db.COL_URL
import com.nononsenseapps.feeder.db.FEEDS_TABLE_NAME
import com.nononsenseapps.feeder.db.FEED_ITEMS_TABLE_NAME
import com.nononsenseapps.feeder.ui.ARG_AUTHOR
import com.nononsenseapps.feeder.ui.ARG_DATE
import com.nononsenseapps.feeder.ui.ARG_ENCLOSURE
import com.nononsenseapps.feeder.ui.ARG_FEEDTITLE
import com.nononsenseapps.feeder.ui.ARG_FEED_URL
import com.nononsenseapps.feeder.ui.ARG_ID
import com.nononsenseapps.feeder.ui.ARG_IMAGEURL
import com.nononsenseapps.feeder.ui.ARG_LINK
import com.nononsenseapps.feeder.ui.ARG_TITLE
import com.nononsenseapps.feeder.util.setLong
import com.nononsenseapps.feeder.util.setString
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import org.joda.time.DateTime
import java.net.URI
import java.net.URL

const val feedItemColumnsWithFeed = "$FEED_ITEMS_TABLE_NAME.$COL_ID AS $COL_ID, $COL_GUID, $FEED_ITEMS_TABLE_NAME.$COL_TITLE AS $COL_TITLE, " +
        "$COL_DESCRIPTION, $COL_PLAINTITLE, $COL_PLAINSNIPPET, $FEED_ITEMS_TABLE_NAME.$COL_IMAGEURL, $COL_ENCLOSURELINK, " +
        "$COL_AUTHOR, $COL_PUBDATE, $COL_LINK, $COL_UNREAD, $FEEDS_TABLE_NAME.$COL_TAG AS $COL_TAG, $FEEDS_TABLE_NAME.$COL_ID AS $COL_FEEDID, " +
        "$FEEDS_TABLE_NAME.$COL_TITLE AS $COL_FEEDTITLE, $FEEDS_TABLE_NAME.$COL_URL AS $COL_FEEDURL"

data class FeedItemWithFeed @Ignore constructor(
        var id: Long = ID_UNSET,
        var guid: String = "",
        var title: String = "",
        var description: String = "",
        @ColumnInfo(name = COL_PLAINTITLE) var plainTitle: String = "",
        @ColumnInfo(name = COL_PLAINSNIPPET) var plainSnippet: String = "",
        @ColumnInfo(name = COL_IMAGEURL) var imageUrl: String? = null,
        @ColumnInfo(name = COL_ENCLOSURELINK) var enclosureLink: String? = null,
        var author: String? = null,
        @ColumnInfo(name = COL_PUBDATE) var pubDate: DateTime? = null,
        var link: String? = null,
        var tag: String = "",
        var unread: Boolean = true,
        @ColumnInfo(name = COL_FEEDID) var feedId: Long? = null,
        @ColumnInfo(name = COL_FEEDTITLE) var feedTitle: String = "",
        @ColumnInfo(name = COL_FEEDURL) var feedUrl: URL = sloppyLinkToStrictURLNoThrows("")
) {
    constructor() : this(id = ID_UNSET)

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

    fun storeInBundle(bundle: Bundle): Bundle {
        bundle.storeFeedItem()
        return bundle
    }

    private fun Bundle.storeFeedItem() {
        setLong(ARG_ID to id)
        setString(ARG_TITLE to title)
        setString(ARG_LINK to link)
        setString(ARG_ENCLOSURE to enclosureLink)
        setString(ARG_IMAGEURL to imageUrl)
        setString(ARG_FEEDTITLE to feedTitle)
        setString(ARG_AUTHOR to author)
        setString(ARG_DATE to pubDateString)
        setString(ARG_FEED_URL to feedUrl.toString())
    }
}
