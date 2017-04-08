package com.nononsenseapps.feeder.util

import android.content.ContentResolver
import android.content.ContentResolver.*
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import com.nononsenseapps.feeder.db.FeedItemSQL.*
import com.nononsenseapps.feeder.db.FeedSQL.COL_NOTIFY
import com.nononsenseapps.feeder.db.FeedSQL.URI_FEEDS

fun ContentResolver.notifyAllUris(): Unit {
    val uris = listOf(URI_FEEDS,
            com.nononsenseapps.feeder.db.FeedSQL.URI_TAGSWITHCOUNTS,
            com.nononsenseapps.feeder.db.FeedSQL.URI_FEEDSWITHCOUNTS,
            com.nononsenseapps.feeder.db.FeedItemSQL.URI_FEED_ITEMS)
    uris.map {
        notifyChange(it, null, false)
    }
}

fun ContentResolver.markItemAsRead(itemId: Long, read: Boolean = true): Unit {
    updateFeedItem(itemId) {
        setInt(COL_UNREAD to (if (read) 0 else 1))
    }
}

fun ContentResolver.markItemAsUnread(itemId: Long): Unit {
    updateFeedItem(itemId) {
        setInt(COL_UNREAD to 1)
    }
}

fun ContentResolver.markFeedAsRead(feedId: Long): Unit {
    updateItems(URI_FEED_ITEMS,
            where = "$COL_FEED IS ?",
            params = arrayListOf(feedId)) {
        setInt(COL_UNREAD to 0)
    }
}

fun ContentResolver.markTagAsRead(tag: String): Unit {
    updateItems(URI_FEED_ITEMS,
            where = "$COL_TAG IS ?",
            params = arrayListOf(tag)) {
        setInt(COL_UNREAD to 0)
    }
}

/**
 * Request a manual synchronization of one or all (default) feeds
 */
fun ContentResolver.requestFeedSync(feedId: Long = -1): Unit {
    requestFeedSync {
        if (feedId > 0) {
            putLong(COL_ID, feedId)
        }
        // sync manually NOW
        putBoolean(SYNC_EXTRAS_MANUAL, true)
        putBoolean(SYNC_EXTRAS_EXPEDITED, true)
    }
}

/**
 * Request a manual synchronization of all feeds in tag
 */
fun ContentResolver.requestFeedSync(tag: String): Unit {
    requestFeedSync {
        if (tag.isNotEmpty()) {
            putString(COL_TAG, tag)
        }
        // sync manually NOW
        putBoolean(SYNC_EXTRAS_MANUAL, true)
        putBoolean(SYNC_EXTRAS_EXPEDITED, true)
    }
}

inline fun ContentResolver.requestFeedSync(init: Bundle.() -> Unit): Unit {
    val account = com.nononsenseapps.feeder.db.AccountService.Account()
    requestSync(account, com.nononsenseapps.feeder.db.AUTHORITY, bundle(init))
}

fun ContentResolver.setNotify(feedId: Long, notify: Boolean = true): Unit {
    if (notify) {
        // First mark all existing as notified so we don't spam
        updateFeedItems(where = "$COL_FEED IS ? AND $COL_NOTIFIED IS 0",
                params = arrayListOf(feedId)) {
            setInt(COL_NOTIFIED to 1)
        }
    }
    // Now toggle notifications
    updateFeed(feedId) {
        setInt(COL_NOTIFY to if (notify) 1 else 0)
    }
}

fun ContentResolver.setNotify(tag: String, notify: Boolean = true): Unit {
    if (notify) {
        // First mark all existing as notified so we don't spam
        updateFeedItems(where = "$COL_TAG IS ? AND $COL_NOTIFIED IS 0",
                params = arrayListOf(tag)) {
            setInt(COL_NOTIFIED to 1)
        }
    }
    // Now toggle notifications
    updateFeeds(where = "$COL_TAG IS ?", params = arrayListOf(tag)) {
        setInt(COL_NOTIFY to if (notify) 1 else 0)
    }
}

inline fun ContentResolver.updateFeedItem(id: Long, init: ContentValues.() -> Unit): Int {
    return updateFeedItems(where = "$COL_ID IS ?",
            params = arrayListOf(id),
            init = init)
}

inline fun ContentResolver.updateFeed(id: Long, init: ContentValues.() -> Unit): Int {
    return updateFeeds(where = "$COL_ID IS ?",
            params = arrayListOf(id),
            init = init)
}

/**
 * Update feeds which have a certain column value
 */
inline fun ContentResolver.updateFeeds(where: String? = null, params: ArrayList<Any>? = null, init: ContentValues.() -> Unit): Int {
    return updateItems(URI_FEEDS, where, params, init)
}

/**
 * Update feed items which have a certain column value
 */
inline fun ContentResolver.updateFeedItems(where: String? = null, params: ArrayList<Any>? = null, init: ContentValues.() -> Unit): Int {
    return updateItems(URI_FEED_ITEMS, where, params, init)
}

/**
 * Update items which have a certain column value
 */

inline fun ContentResolver.updateItems(uri: Uri, where: String? = null, params: ArrayList<Any>? = null, init: ContentValues.() -> Unit): Int {
    val result = update(uri,
            contentValues(init),
            where,
            params?.map(Any::toString)?.toTypedArray())
    notifyAllUris()
    return result
}
