package com.nononsenseapps.feeder.db

import android.database.Cursor
import com.nononsenseapps.feeder.util.getInt
import com.nononsenseapps.feeder.util.getLong
import com.nononsenseapps.feeder.util.getString
import com.nononsenseapps.feeder.util.setInt
import com.nononsenseapps.feeder.util.setString

// SQL convention says Table name should be "singular"
const val TABLE_NAME = "Feed"
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
val FIELDS = arrayOf(COL_ID, COL_TITLE, COL_URL, COL_TAG, COL_CUSTOM_TITLE, COL_NOTIFY)

val CREATE_TABLE = """
    CREATE TABLE $TABLE_NAME (
      $COL_ID INTEGER PRIMARY KEY,
      $COL_TITLE TEXT NOT NULL,
      $COL_CUSTOM_TITLE TEXT NOT NULL,
      $COL_URL TEXT NOT NULL,
      $COL_TAG TEXT NOT NULL DEFAULT '',
      $COL_NOTIFY INTEGER NOT NULL DEFAULT 0,
      UNIQUE($COL_URL) ON CONFLICT REPLACE
    )"""

// A view which also reports 'unreadcount'
const val VIEWCOUNT_NAME = "WithUnreadCount"
// Used on count view
const val COL_UNREADCOUNT = "unreadcount"
@JvmField
val FIELDS_VIEWCOUNT = arrayOf(COL_ID, COL_TITLE, COL_URL, COL_TAG, COL_CUSTOM_TITLE, COL_NOTIFY, COL_UNREADCOUNT)

val CREATE_COUNT_VIEW = """
    CREATE TEMP VIEW IF NOT EXISTS $VIEWCOUNT_NAME
    AS SELECT ${FIELDS_VIEWCOUNT.joinToString()}
       FROM $TABLE_NAME
       LEFT JOIN (SELECT COUNT(1) AS $COL_UNREADCOUNT, ${FeedItemSQL.COL_FEED}
         FROM ${FeedItemSQL.TABLE_NAME}
         WHERE ${FeedItemSQL.COL_UNREAD} IS 1
         GROUP BY ${FeedItemSQL.COL_FEED})
       ON $TABLE_NAME.$COL_ID = ${FeedItemSQL.COL_FEED}"""


// A view of distinct tags and their unread counts
const val VIEWTAGS_NAME = "TagsWithUnreadCount"
@JvmField
val FIELDS_TAGSWITHCOUNT = arrayOf(COL_ID, COL_TAG, COL_UNREADCOUNT)

val CREATE_TAGS_VIEW = """
    CREATE TEMP VIEW IF NOT EXISTS $VIEWTAGS_NAME
    AS SELECT ${FIELDS_TAGSWITHCOUNT.joinToString()}
       FROM $TABLE_NAME
       LEFT JOIN (SELECT COUNT(1) AS $COL_UNREADCOUNT, ${FeedItemSQL.COL_TAG} AS itemtag
         FROM ${FeedItemSQL.TABLE_NAME}
         WHERE ${FeedItemSQL.COL_UNREAD} IS 1
         GROUP BY itemtag)
       ON $TABLE_NAME.$COL_TAG IS itemtag
       GROUP BY $COL_TAG"""


data class FeedSQL(val id: Long = -1, val title: String = "", val customTitle: String = "", val url: String = "",
                   val tag: String = "", val notify: Boolean = false, val unreadCount: Int = 0) {

    fun asContentValues() =
            com.nononsenseapps.feeder.util.contentValues {
                setString(COL_TITLE to title)
                setString(COL_CUSTOM_TITLE to customTitle)
                setString(COL_URL to url)
                setString(COL_TAG to tag)
                setInt(COL_NOTIFY to if (notify) 1 else 0)
            }

}

fun Cursor.asFeed(): FeedSQL {
    return FeedSQL(id = getLong(COL_ID) ?: -1,
            title = getString(COL_TITLE) ?: "",
            customTitle = getString(COL_CUSTOM_TITLE) ?: "",
            url = getString(COL_URL) ?: "",
            notify = when (getInt(COL_NOTIFY)) {
                1 -> true
                else -> false
            },
            unreadCount = getInt(COL_UNREADCOUNT) ?: 0)
}

