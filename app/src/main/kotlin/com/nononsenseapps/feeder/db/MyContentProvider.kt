package com.nononsenseapps.feeder.db

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

class MyContentProvider : ContentProvider() {

    val authority = "com.nononsenseapps.feeder.provider"
    val scheme = "content://"
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

    fun uriMatcher(init: UriMatcher.() -> Unit): UriMatcher {
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher.init()
        return uriMatcher
    }
}
