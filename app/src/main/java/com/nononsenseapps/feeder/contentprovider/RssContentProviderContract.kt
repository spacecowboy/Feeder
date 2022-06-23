package com.nononsenseapps.feeder.contentprovider

object RssContentProviderContract {
    val feedsMimeTypeList = "vnd.android.cursor.dir/vnd.rssprovider.feeds"
    val feedsUriPathList = "feeds"

    /**
     * Columns available via the content provider
     */
    val feedsColumns = listOf(
        "id",
        "title",
    )

    val articlesMimeTypeList = "vnd.android.cursor.dir/vnd.rssprovider.items"
    val articlesUriPathList = "articles"
    val articlesMimeTypeItem = "vnd.android.cursor.item/vnd.rssprovider.item"
    val articlesUriPathItem = "articles/#"

    /**
     * Columns available via the content provider
     */
    val articlesColumns = listOf(
        "id",
        "title",
        "text",
    )
}
