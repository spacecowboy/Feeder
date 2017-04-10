package com.nononsenseapps.feeder.db

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import com.nononsenseapps.feeder.util.inTransaction

const val AUTHORITY = "com.nononsenseapps.feeder.provider"
const val SCHEME = "content://"

const val QUERY_PARAM_LIMIT = "QUERY_PARAM_LIMIT"
const val QUERY_PARAM_SKIP = "QUERY_PARAM_SKIP"

class RssContentProvider : ContentProvider() {

    val uriMatcher = uriMatcher {
        FeedSQL.addMatcherUris(this)
        FeedItemSQL.addMatcherUris(this)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        if (uri != null) {
            val result: Uri
            val table: String
            when (uriMatcher.match(uri)) {
                FeedSQL.URICODE -> {
                    table = FeedSQL.TABLE_NAME
                    result = FeedSQL.URI_FEEDS
                }
                FeedItemSQL.URICODE -> {
                    table = FeedItemSQL.TABLE_NAME
                    result = FeedItemSQL.URI_FEED_ITEMS
                }
                else -> throw UnsupportedOperationException("Not yet implemented")
            }

            val id = DatabaseHandler.getInstance(context).writableDatabase
                    .insert(table, null, values)
            if (id > 0) {
                return Uri.withAppendedPath(result, java.lang.Long.toString(id))
            } else {
                return result
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
                FeedSQL.ITEMCODE -> {
                    table = FeedSQL.TABLE_NAME
                    where = Util.WHEREIDIS
                    params = Util.ToStringArray(uri.lastPathSegment)
                }
                FeedSQL.URICODE -> {
                    table = FeedSQL.TABLE_NAME
                    where = selection
                    params = selectionArgs
                }
                FeedSQL.VIEWCOUNTCODE -> {
                    table = FeedSQL.VIEWCOUNT_NAME
                    where = selection
                    params = selectionArgs
                }
                FeedSQL.VIEWTAGSCODE -> {
                    table = FeedSQL.VIEWTAGS_NAME
                    where = selection
                    params = selectionArgs
                }
                FeedItemSQL.ITEMCODE -> {
                    table = FeedItemSQL.TABLE_NAME
                    where = Util.WHEREIDIS
                    params = Util.ToStringArray(uri.lastPathSegment)
                }
                FeedItemSQL.URICODE -> {
                    table = FeedItemSQL.TABLE_NAME
                    where = selection
                    params = selectionArgs
                }
                else -> throw UnsupportedOperationException("Not yet implemented")
            }
            // Must use builder in order to support OFFSET as the regular parser does not allow for
            // negative numbers in the LIMIT clause.
            val queryBuilder = SQLiteQueryBuilder()
            queryBuilder.tables = table
            val query = queryBuilder.buildQuery(projection, where, null, null, sortOrder, null) + getLimitString(uri)

            val result = DatabaseHandler.getInstance(context).readableDatabase
                    .rawQuery(query, params)

            // Make sure you don't override another uri here
            result?.setNotificationUri(context.contentResolver, uri)
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
                FeedSQL.ITEMCODE -> {
                    table = FeedSQL.TABLE_NAME
                    where = Util.WHEREIDIS
                    params = Util.ToStringArray(uri.lastPathSegment)
                }
                FeedSQL.URICODE -> {
                    table = FeedSQL.TABLE_NAME
                    where = selection
                    params = selectionArgs
                }
                FeedItemSQL.ITEMCODE -> {
                    table = FeedItemSQL.TABLE_NAME
                    where = Util.WHEREIDIS
                    params = Util.ToStringArray(uri.lastPathSegment)
                }
                FeedItemSQL.URICODE -> {
                    table = FeedItemSQL.TABLE_NAME
                    where = selection
                    params = selectionArgs
                }
                else -> TODO("not implemented")
            }

            return DatabaseHandler.getInstance(context).writableDatabase
                    .update(table, values, where, params)
        } else {
            throw IllegalArgumentException("Uri can't be null")
        }
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        if (uri != null) {
            val result: Int
            when (uriMatcher.match(uri)) {
                FeedSQL.ITEMCODE -> result = delete(FeedSQL.URI_FEEDS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.lastPathSegment))
                FeedSQL.URICODE -> result = DatabaseHandler.getInstance(context)
                        .writableDatabase
                        .delete(FeedSQL.TABLE_NAME, selection, selectionArgs)
                FeedItemSQL.ITEMCODE -> result = delete(FeedItemSQL.URI_FEED_ITEMS, Util.WHEREIDIS,
                        Util.ToStringArray(uri.lastPathSegment))
                FeedItemSQL.URICODE -> result = DatabaseHandler.getInstance(context)
                        .writableDatabase
                        .delete(FeedItemSQL.TABLE_NAME, selection,
                                selectionArgs)
                else -> TODO("not implemented")
            }
            return result
        } else {
            throw IllegalArgumentException("Uri can't be null")
        }
    }

    override fun applyBatch(operations: java.util.ArrayList<ContentProviderOperation>?): Array<ContentProviderResult?> {
        if (operations != null) {
            val numOperations = operations.size
            val results = arrayOfNulls<ContentProviderResult>(numOperations)

            val db = DatabaseHandler.getInstance(context).writableDatabase
            db.inTransaction {
                for (i in 0..numOperations - 1) {
                    results[i] = operations[i].apply(this, results, i)
                }
            }
            return results
        }
        return arrayOf()
    }

    override fun getType(uri: Uri?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    inline fun uriMatcher(init: UriMatcher.() -> Unit): UriMatcher {
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher.init()
        return uriMatcher
    }
}
