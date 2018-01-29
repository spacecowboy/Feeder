package com.nononsenseapps.feeder.db

import android.content.UriMatcher
import android.net.Uri

// URIs
// Feed
@JvmField
val URI_FEEDS: Uri = Uri.withAppendedPath(Uri.parse(SCHEME + AUTHORITY), FEED_TABLE_NAME)
@JvmField
val URI_FEEDSWITHCOUNTS: Uri = Uri.withAppendedPath(URI_FEEDS, VIEWCOUNT_NAME)
@JvmField
val URI_TAGSWITHCOUNTS: Uri = Uri.withAppendedPath(URI_FEEDS, VIEWTAGS_NAME)
// Feed item
@JvmField
val URI_FEEDITEMS: Uri = Uri.withAppendedPath(Uri.parse(SCHEME + AUTHORITY), FEED_ITEM_TABLE_NAME)

// URI codes, must be unique
// Feed codes
const val FEED_CODE = 101
const val FEED_ELEMENT_CODE = 102
const val FEED_VIEWCOUNT_CODE = 103
const val FEED_VIEWTAGS_CODE = 104
// Feed item codes
const val FEEDITEM_CODE = 201
const val FEEDITEM_ELEMENT_CODE = 202

fun addMatcherUris(uriMatcher: UriMatcher) {
    // Feed
    uriMatcher.addURI(AUTHORITY, URI_FEEDS.path, FEED_CODE)
    uriMatcher.addURI(AUTHORITY, URI_FEEDS.path + "/#", FEED_ELEMENT_CODE)
    uriMatcher.addURI(AUTHORITY, URI_FEEDSWITHCOUNTS.path, FEED_VIEWCOUNT_CODE)
    uriMatcher.addURI(AUTHORITY, URI_TAGSWITHCOUNTS.path, FEED_VIEWTAGS_CODE)
    // Feed item
    uriMatcher.addURI(AUTHORITY, URI_FEEDITEMS.path, FEEDITEM_CODE)
    uriMatcher.addURI(AUTHORITY, URI_FEEDITEMS.path + "/#", FEEDITEM_ELEMENT_CODE)
}
