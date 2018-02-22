package com.nononsenseapps.feeder.util

import android.content.ContentResolver
import android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED
import android.content.ContentResolver.SYNC_EXTRAS_MANUAL
import android.content.ContentResolver.requestSync
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.nononsenseapps.feeder.db.COL_FEED
import com.nononsenseapps.feeder.db.COL_GUID
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.COL_NOTIFIED
import com.nononsenseapps.feeder.db.COL_NOTIFY
import com.nononsenseapps.feeder.db.COL_TAG
import com.nononsenseapps.feeder.db.COL_UNREAD
import com.nononsenseapps.feeder.db.FEED_FIELDS
import com.nononsenseapps.feeder.db.FEED_ITEM_FIELDS
import com.nononsenseapps.feeder.db.FIELDS_TAGSWITHCOUNT
import com.nononsenseapps.feeder.db.FIELDS_VIEWCOUNT
import com.nononsenseapps.feeder.db.FeedItemSQL
import com.nononsenseapps.feeder.db.FeedSQL
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.URI_FEEDSWITHCOUNTS
import com.nononsenseapps.feeder.db.URI_TAGSWITHCOUNTS
import com.nononsenseapps.feeder.db.asFeed
import com.nononsenseapps.feeder.db.asFeedItem
import java.util.*

/**
 * Inserts or updates a feed. Always returns the id of the item.
 */
fun ContentResolver.save(feed: FeedSQL): Long = when {
    feed.id > 0 -> {
        updateFeedWith(feed.id, feed.asContentValues())
        feed.id
    }
    else -> {
        insertFeedWith(feed.asContentValues())
    }
}

fun ContentResolver.notifyAllUris() {
    panicIfOnUiThread()
    val uris = listOf(URI_FEEDS,
            URI_TAGSWITHCOUNTS,
            URI_FEEDSWITHCOUNTS,
            URI_FEEDITEMS)
    uris.map {
        notifyChange(it, null, false)
    }
}

fun ContentResolver.markItemsAsNotified(ids: LongArray, notified: Boolean = true) {
    panicIfOnUiThread()
    updateFeedItems(ids) {
        setInt(COL_NOTIFIED to (if (notified) 1 else 0))
    }
}

fun ContentResolver.markItemAsReadAndNotified(id: Long, read: Boolean = true, notified: Boolean = true) {
    panicIfOnUiThread()
    updateFeedItem(id) {
        setInt(COL_UNREAD to (if (read) 0 else 1))
        setInt(COL_NOTIFIED to (if (notified) 1 else 0))
    }
}

fun ContentResolver.markItemAsRead(itemId: Long, read: Boolean = true) {
    panicIfOnUiThread()
    updateFeedItem(itemId) {
        setInt(COL_UNREAD to (if (read) 0 else 1))
    }
}

fun ContentResolver.markItemAsUnread(itemId: Long) {
    panicIfOnUiThread()
    updateFeedItem(itemId) {
        setInt(COL_UNREAD to 1)
    }
}

fun ContentResolver.markFeedAsRead(feedId: Long) {
    panicIfOnUiThread()
    updateItems(URI_FEEDITEMS,
            where = "$COL_FEED IS ?",
            params = arrayListOf(feedId)) {
        setInt(COL_UNREAD to 0)
    }
}

fun ContentResolver.markTagAsRead(tag: String) {
    panicIfOnUiThread()
    updateItems(URI_FEEDITEMS,
            where = "$COL_TAG IS ?",
            params = arrayListOf(tag)) {
        setInt(COL_UNREAD to 0)
    }
}

fun ContentResolver.markAllAsRead() {
    panicIfOnUiThread()
    updateItems(URI_FEEDITEMS) {
        setInt(COL_UNREAD to 0)
    }
}

/**
 * Request a manual synchronization of one or all (default) feeds
 */
fun ContentResolver.requestFeedSync(feedId: Long = -1) {
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
fun ContentResolver.requestFeedSync(tag: String) {
    requestFeedSync {
        if (tag.isNotEmpty()) {
            putString(COL_TAG, tag)
        }
        // sync manually NOW
        putBoolean(SYNC_EXTRAS_MANUAL, true)
        putBoolean(SYNC_EXTRAS_EXPEDITED, true)
    }
}

@Suppress("unused")
inline fun ContentResolver.requestFeedSync(init: Bundle.() -> Unit) {
    val account = com.nononsenseapps.feeder.db.AccountService.Account()
    requestSync(account, com.nononsenseapps.feeder.db.AUTHORITY, bundle(init))
}

fun ContentResolver.setNotify(feedId: Long, notify: Boolean = true) {
    panicIfOnUiThread()
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

fun ContentResolver.setNotify(tag: String, notify: Boolean = true) {
    panicIfOnUiThread()
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

fun ContentResolver.setNotifyOnAllFeeds(notify: Boolean = true) {
    panicIfOnUiThread()
    if (notify) {
        // First mark all existing as notified so we don't spam
        updateFeedItems(where = "$COL_NOTIFIED IS 0") {
            setInt(COL_NOTIFIED to 1)
        }
    }
    // Now toggle notifications
    updateFeeds {
        setInt(COL_NOTIFY to if (notify) 1 else 0)
    }
}

inline fun ContentResolver.updateFeedItems(ids: LongArray, init: ContentValues.() -> Unit): Int {
    if (ids.isEmpty()) {
        return 0
    }
    return updateFeedItems(where = "$COL_ID IN (${ids.joinToString(separator = ", ")})",
            init = init)
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

fun ContentResolver.updateFeedWith(id: Long, values: ContentValues): Int {
    panicIfOnUiThread()
    return updateFeedsWith(where = "$COL_ID IS ?",
            params = arrayListOf(id),
            values = values)
}

fun ContentResolver.insertFeedWith(values: ContentValues): Long {
    panicIfOnUiThread()
    return insert(URI_FEEDS, values).lastPathSegment.toLong()
}

/**
 * Update feeds which have a certain column value
 */
fun ContentResolver.updateFeedsWith(where: String? = null, params: List<Any>? = null, values: ContentValues): Int =
        update(URI_FEEDS, values, where, params?.map(Any::toString)?.toTypedArray())

/**
 * Update feeds which have a certain column value
 */
inline fun ContentResolver.updateFeeds(where: String? = null, params: List<Any>? = null, init: ContentValues.() -> Unit): Int =
        updateItems(URI_FEEDS, where, params, init)

/**
 * Update feed items which have a certain column value
 */
inline fun ContentResolver.updateFeedItems(where: String? = null, params: List<Any>? = null, init: ContentValues.() -> Unit): Int {
    return updateItems(URI_FEEDITEMS, where, params, init)
}

/**
 * Update items which have a certain column value
 */

inline fun ContentResolver.updateItems(uri: Uri, where: String? = null, params: List<Any>? = null, init: ContentValues.() -> Unit): Int {
    val result = update(uri,
            contentValues(init),
            where,
            params?.map(Any::toString)?.toTypedArray())
    notifyAllUris()
    return result
}

fun ContentResolver.queryTagsWithCounts(columns: List<String> = FIELDS_TAGSWITHCOUNT.asList(),
                                        where: String? = null, params: List<Any>? = null, order: String? = null,
                                        reader: (Cursor) -> Unit) {
    panicIfOnUiThread()
    queryItems(URI_TAGSWITHCOUNTS, columns, where, params, order, reader)
}

fun ContentResolver.queryFeeds(columns: List<String> = FEED_FIELDS.asList(),
                               where: String? = null, params: List<Any>? = null, order: String? = null,
                               reader: (Cursor) -> Unit) {
    panicIfOnUiThread()
    queryItems(URI_FEEDS, columns, where, params, order, reader)
}

fun ContentResolver.cursorForFeedsWithCounts(columns: List<String> = FIELDS_VIEWCOUNT.asList(),
                                             where: String? = null, params: List<Any>? = null, order: String? = null): Cursor? =
        cursorFor(URI_FEEDSWITHCOUNTS, columns, where, params, order)

fun ContentResolver.queryFeedItems(columns: List<String> = FEED_ITEM_FIELDS.asList(),
                                   where: String? = null,
                                   params: List<Any>? = null,
                                   order: String? = null,
                                   reader: (Cursor) -> Unit) {
    panicIfOnUiThread()
    queryItems(URI_FEEDITEMS, columns, where, params, order, reader)
}

fun ContentResolver.cursorFor(uri: Uri, columns: List<String>, where: String? = null,
                              params: List<Any>? = null, order: String? = null): Cursor? {
    panicIfOnUiThread()
    return query(uri, columns.toTypedArray(), where, params?.map(Any::toString)?.toTypedArray(), order)
}

inline fun ContentResolver.queryItems(uri: Uri, columns: List<String>, where: String? = null,
                                      params: List<Any>? = null, order: String? = null,
                                      reader: (Cursor) -> Unit) {
    query(uri, columns.toTypedArray(), where, params?.map(Any::toString)?.toTypedArray(), order)?.use(reader)
}

fun ContentResolver.getFeeds(columns: List<String> = FEED_FIELDS.asList(),
                             where: String? = null, params: List<Any>? = null, order: String? = null): List<FeedSQL> {
    panicIfOnUiThread()
    val feeds = ArrayList<FeedSQL>()
    queryFeeds(columns, where, params, order) { cursor ->
        cursor.forEach {
            feeds.add(it.asFeed())
        }
    }
    return feeds
}

fun ContentResolver.getFeedItems(columns: List<String> = FEED_ITEM_FIELDS.asList(),
                                 where: String? = null,
                                 params: List<Any>? = null,
                                 order: String? = null): List<FeedItemSQL> {
    panicIfOnUiThread()
    val items = ArrayList<FeedItemSQL>()
    queryFeedItems(columns, where, params, order) { cursor ->
        cursor.forEach {
            items.add(it.asFeedItem())
        }
    }
    return items
}

fun ContentResolver.getIdForFeedItem(guid: String, feedId: Long): Long {
    panicIfOnUiThread()
    queryItems(URI_FEEDITEMS,
            columns = listOf(COL_ID),
            where = "$COL_GUID IS ? AND $COL_FEED IS ?",
            params = listOf(guid, feedId)) { cursor ->
        cursor.forEach {
            return it.getLong(COL_ID) ?: -1L
        }
    }
    return -1L
}

/**
 * @throws DatabaseOnMainThreadException if on the Main Thread
 */
private fun panicIfOnUiThread() {
    if (Looper.getMainLooper().thread == Thread.currentThread()) {
        Log.e("SHIT", "Thread name: ${Thread.currentThread().name}")
        throw DatabaseOnMainThreadException()
    }
}

class DatabaseOnMainThreadException : Exception("Database operation was performed on the Main thread. " +
        "Please offload that work using a coroutine and the Background context.")
