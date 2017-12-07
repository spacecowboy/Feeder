package com.nononsenseapps.feeder.db

import android.database.Cursor
import com.nononsenseapps.feeder.util.getInt
import com.nononsenseapps.feeder.util.getLong
import com.nononsenseapps.feeder.util.getString
import com.nononsenseapps.feeder.util.setInt
import com.nononsenseapps.feeder.util.setString
import com.nononsenseapps.feeder.util.setStringMaybe
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import java.net.URL

// SQL convention says Table name should be "singular"
const val FEED_TABLE_NAME = "Feed"
// Naming the id column with an underscore is good to be consistent
// with other Android things. This is ALWAYS needed
const val COL_ID = "_id"
// These fields can be anything you want.
const val COL_TITLE = "title"
const val COL_CUSTOM_TITLE = "customtitle"
const val COL_URL = "url"
const val COL_TAG = "tag"
const val COL_NOTIFY = "notify"
// For database projection so order is consistent
@JvmField
val FEED_FIELDS = arrayOf(COL_ID, COL_TITLE, COL_URL, COL_TAG, COL_CUSTOM_TITLE, COL_NOTIFY, COL_IMAGEURL)

val CREATE_FEED_TABLE = """
    CREATE TABLE $FEED_TABLE_NAME (
      $COL_ID INTEGER PRIMARY KEY,
      $COL_TITLE TEXT NOT NULL,
      $COL_CUSTOM_TITLE TEXT NOT NULL,
      $COL_URL TEXT NOT NULL,
      $COL_TAG TEXT NOT NULL DEFAULT '',
      $COL_NOTIFY INTEGER NOT NULL DEFAULT 0,
      $COL_IMAGEURL TEXT,
      UNIQUE($COL_URL) ON CONFLICT REPLACE
    )"""

// A view which also reports 'unreadcount'
const val VIEWCOUNT_NAME = "WithUnreadCount"
// Used on count view
const val COL_UNREADCOUNT = "unreadcount"
@JvmField
val FIELDS_VIEWCOUNT = arrayOf(COL_ID, COL_TITLE, COL_URL, COL_TAG, COL_CUSTOM_TITLE, COL_NOTIFY, COL_IMAGEURL, COL_UNREADCOUNT)

val CREATE_COUNT_VIEW = """
    CREATE TEMP VIEW IF NOT EXISTS $VIEWCOUNT_NAME
    AS SELECT ${FIELDS_VIEWCOUNT.joinToString()}
       FROM $FEED_TABLE_NAME
       LEFT JOIN (SELECT COUNT(1) AS $COL_UNREADCOUNT, $COL_FEED
         FROM $FEED_ITEM_TABLE_NAME
         WHERE $COL_UNREAD IS 1
         GROUP BY $COL_FEED)
       ON $FEED_TABLE_NAME.$COL_ID = $COL_FEED"""


// A view of distinct tags and their unread counts
const val VIEWTAGS_NAME = "TagsWithUnreadCount"
@JvmField
val FIELDS_TAGSWITHCOUNT = arrayOf(COL_ID, COL_TAG, COL_UNREADCOUNT)

val CREATE_TAGS_VIEW = """
    CREATE TEMP VIEW IF NOT EXISTS $VIEWTAGS_NAME
    AS SELECT ${FIELDS_TAGSWITHCOUNT.joinToString()}
       FROM $FEED_TABLE_NAME
       LEFT JOIN (SELECT COUNT(1) AS $COL_UNREADCOUNT, $COL_TAG AS itemtag
         FROM $FEED_ITEM_TABLE_NAME
         WHERE $COL_UNREAD IS 1
         GROUP BY itemtag)
       ON $FEED_TABLE_NAME.$COL_TAG IS itemtag
       GROUP BY $COL_TAG"""


data class FeedSQL(val id: Long = -1,
                   val title: String = "",
                   val customTitle: String = "",
                   val url: URL = sloppyLinkToStrictURL(""),
                   val tag: String = "",
                   val notify: Boolean = false,
                   val unreadCount: Int = 0,
                   val icon: URL? = null,
                   val displayTitle: String = (if (customTitle.isBlank()) title else customTitle) ) {

    fun asContentValues() =
            com.nononsenseapps.feeder.util.contentValues {
                setString(COL_TITLE to title)
                setString(COL_CUSTOM_TITLE to customTitle)
                setString(COL_URL to url.toString())
                setString(COL_TAG to tag)
                setInt(COL_NOTIFY to if (notify) 1 else 0)
                setStringMaybe(COL_IMAGEURL to icon?.toString())
            }

}

fun Cursor.asFeed(): FeedSQL {
    return FeedSQL(id = getLong(COL_ID) ?: -1,
            tag = getString(COL_TAG) ?: "",
            title = getString(COL_TITLE) ?: "",
            customTitle = getString(COL_CUSTOM_TITLE) ?: "",
            url = sloppyLinkToStrictURLNoThrows(getString(COL_URL) ?: ""),
            notify = when (getInt(COL_NOTIFY)) {
                1 -> true
                else -> false
            },
            unreadCount = getInt(COL_UNREADCOUNT) ?: 0,
            icon = getString(COL_IMAGEURL)?.let { sloppyLinkToStrictURLNoThrows(it) })
}

