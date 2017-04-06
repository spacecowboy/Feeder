package com.nononsenseapps.feeder.db

import android.accounts.AccountManager
import android.content.*
import android.content.ContentResolver.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import com.nononsenseapps.feeder.db.FeedItemSQL.*
import com.nononsenseapps.feeder.db.FeedSQL.COL_NOTIFY
import com.nononsenseapps.feeder.db.FeedSQL.URI_FEEDS
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.PrefUtils.PREF_SYNC_ONLY_CHARGING

val AUTHORITY = "com.nononsenseapps.feeder.provider"
val SCHEME = "content://"

fun ContentResolver.notifyAllUris(): Unit {
    val uris = listOf(URI_FEEDS,
            FeedSQL.URI_TAGSWITHCOUNTS,
            FeedSQL.URI_FEEDSWITHCOUNTS,
            FeedItemSQL.URI_FEED_ITEMS)
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
    val account = AccountService.Account()
    requestSync(account, AUTHORITY, bundle(init))
}

fun Context.setupSync(): Unit {
    val account = AccountService.Account()
    val accountManager: AccountManager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

    if (accountManager.addAccountExplicitly(account, null, null)) {
        // New account was added so...
        // Enable syncing
        ContentResolver.setIsSyncable(account, AUTHORITY, 1)
        // Set sync automatic
        ContentResolver.setSyncAutomatically(account, AUTHORITY, true)
    }

    val extras = Bundle()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        extras.putBoolean(SYNC_EXTRAS_REQUIRE_CHARGING,
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_SYNC_ONLY_CHARGING, false))
    }
    if (PrefUtils.shouldSync(this)) {
        // Once per hour: mins * secs
        ContentResolver.addPeriodicSync(account,
                RssContentProvider.AUTHORITY,
                extras,
                60L * 60L * PrefUtils.synchronizationFrequency(this))
    } else {
        ContentResolver.getPeriodicSyncs(account, RssContentProvider.AUTHORITY).map {
            ContentResolver.removePeriodicSync(it.account, it.authority, it.extras)
        }
    }
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

inline fun bundle(init: Bundle.() -> Unit): Bundle {
    val bundle = Bundle()
    bundle.init()
    return bundle
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

fun ContentValues.setInt(pair: Pair<String, Int>) =
        put(pair.first, pair.second)

fun ContentValues.setString(pair: Pair<String, String>) =
        put(pair.first, pair.second)

fun ContentValues.setNull(column: String) =
        putNull(column)

inline fun contentValues(init: ContentValues.() -> Unit): ContentValues {
    val values = ContentValues()
    values.init()
    return values
}

class MyContentProvider : ContentProvider() {

    val uriMatcher = uriMatcher {
        FeedSQL.addMatcherUris(this)
        FeedItemSQL.addMatcherUris(this)
    }
    val query_param_limit = "QUERY_PARAM_LIMIT"
    val query_param_skip = "QUERY_PARAM_SKIP"

    override fun onCreate(): Boolean {
        return true
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getType(uri: Uri?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inline fun uriMatcher(init: UriMatcher.() -> Unit): UriMatcher {
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher.init()
        return uriMatcher
    }
}
