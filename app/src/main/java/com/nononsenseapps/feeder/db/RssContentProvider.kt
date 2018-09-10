package com.nononsenseapps.feeder.db

import android.content.ContentProvider
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import com.nononsenseapps.feeder.util.inTransaction

const val AUTHORITY = "com.nononsenseapps.feeder.provider"
const val SCHEME = "content://"

const val QUERY_PARAM_LIMIT = "QUERY_PARAM_LIMIT"
const val QUERY_PARAM_SKIP = "QUERY_PARAM_SKIP"

inline fun initUriMatcher(init: (UriMatcher) -> Unit): UriMatcher {
    val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    init(uriMatcher)
    return uriMatcher
}

private val uriMatcher: UriMatcher = initUriMatcher(::addMatcherUris)

class RssContentProvider : ContentProvider() {

    private val databaseHandler: DatabaseHandler by lazy {
        DatabaseHandler(context!!)
    }

    override fun onCreate(): Boolean = true

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        if (uri != null) {
            val result: Uri
            val table: String
            when (uriMatcher.match(uri)) {
                FEED_CODE -> {
                    table = FEED_TABLE_NAME
                    result = URI_FEEDS
                }
                FEEDITEM_CODE -> {
                    table = FEED_ITEM_TABLE_NAME
                    result = URI_FEEDITEMS
                }
                else -> throw UnsupportedOperationException("Not yet implemented")
            }

            val id = databaseHandler.writableDatabase
                    .insert(table, null, values)
            return when {
                id > 0 -> Uri.withAppendedPath(result, java.lang.Long.toString(id))
                else -> result
            }
        } else {
            throw IllegalArgumentException("Uri can't be null")
        }
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        if (uri != null) {
            val table: String
            val where: String?
            val params: Array<out String>?

            when (uriMatcher.match(uri)) {
                FEED_ELEMENT_CODE -> {
                    table = FEED_TABLE_NAME
                    where = Util.WHEREIDIS
                    params = Util.ToStringArray(uri.lastPathSegment)
                }
                FEED_CODE -> {
                    table = FEED_TABLE_NAME
                    where = selection
                    params = selectionArgs
                }
                FEED_VIEWCOUNT_CODE -> {
                    table = VIEWCOUNT_NAME
                    where = selection
                    params = selectionArgs
                }
                FEED_VIEWTAGS_CODE -> {
                    table = VIEWTAGS_NAME
                    where = selection
                    params = selectionArgs
                }
                FEEDITEM_ELEMENT_CODE -> {
                    table = FEED_ITEM_TABLE_NAME
                    where = Util.WHEREIDIS
                    params = Util.ToStringArray(uri.lastPathSegment)
                }
                FEEDITEM_CODE -> {
                    table = FEED_ITEM_TABLE_NAME
                    where = selection
                    params = selectionArgs
                }
                else -> throw UnsupportedOperationException("Query URI not supported: " + uri)
            }
            // Must use builder in order to support OFFSET as the regular parser does not allow for
            // negative numbers in the LIMIT clause.
            val queryBuilder = SQLiteQueryBuilder()
            queryBuilder.tables = table
            val query = queryBuilder.buildQuery(projection, where, null, null, sortOrder, null) + getLimitString(uri)

            val result = databaseHandler.readableDatabase
                    .rawQuery(query, params)

            // Make sure you don't override another uri here
            context?.contentResolver?.let { contentResolver ->
                result?.setNotificationUri(contentResolver, uri)
            }
            return result
        } else {
            throw IllegalArgumentException("Uri can't be null")
        }
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val table: String
        val where: String?
        val params: Array<out String>?

        if (uri != null) {
            when (uriMatcher.match(uri)) {
                FEED_ELEMENT_CODE -> {
                    table = FEED_TABLE_NAME
                    where = Util.WHEREIDIS
                    params = Util.ToStringArray(uri.lastPathSegment)
                }
                FEED_CODE -> {
                    table = FEED_TABLE_NAME
                    where = selection
                    params = selectionArgs
                }
                FEEDITEM_ELEMENT_CODE -> {
                    table = FEED_ITEM_TABLE_NAME
                    where = Util.WHEREIDIS
                    params = Util.ToStringArray(uri.lastPathSegment)
                }
                FEEDITEM_CODE -> {
                    table = FEED_ITEM_TABLE_NAME
                    where = selection
                    params = selectionArgs
                }
                else -> TODO("not implemented")
            }

            return databaseHandler.writableDatabase
                    .update(table, values, where, params)
        } else {
            throw IllegalArgumentException("Uri can't be null")
        }
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        if (uri != null) {
            return when (uriMatcher.match(uri)) {
                FEED_ELEMENT_CODE -> delete(URI_FEEDS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.lastPathSegment))
                FEED_CODE -> databaseHandler
                        .writableDatabase
                        .delete(FEED_TABLE_NAME, selection, selectionArgs)
                FEEDITEM_ELEMENT_CODE -> delete(URI_FEEDITEMS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.lastPathSegment))
                FEEDITEM_CODE -> databaseHandler
                        .writableDatabase
                        .delete(FEED_ITEM_TABLE_NAME, selection,
                                selectionArgs)
                else -> TODO("not implemented")
            }
        } else {
            throw IllegalArgumentException("Uri can't be null")
        }
    }

    override fun applyBatch(operations: java.util.ArrayList<ContentProviderOperation>?): Array<ContentProviderResult?> {
        if (operations != null) {
            val numOperations = operations.size
            val results = arrayOfNulls<ContentProviderResult>(numOperations)

            val db = databaseHandler.writableDatabase
            db.inTransaction {
                for (i in 0 until numOperations) {
                    results[i] = operations[i].apply(this, results, i)
                }
            }
            return results
        }
        return arrayOf()
    }

    override fun getType(uri: Uri?): String {
        if (uri != null) {
            return when (uriMatcher.match(uri)) {
                FEED_ELEMENT_CODE -> "vnd.android.cursor.item/vnd.nononsenseapps.feed"
                else -> TODO("not implemented")
            }
        } else {
            throw IllegalArgumentException("Uri can't be null")
        }
    }

    /**
     * Return a limit clause as " LIMIT OFFSET,LIMIT", with parameters defined in uri.
     * If not defined, the default values of -1 are used, which means no offset/return all
     * respectively.
     */
    private fun getLimitString(uri: Uri): String {
        val offset: String = uri.getQueryParameter(QUERY_PARAM_SKIP) ?: "-1"
        val limit: String = uri.getQueryParameter(QUERY_PARAM_LIMIT) ?: "-1"

        return " LIMIT $offset,$limit"
    }


}
