package com.nononsenseapps.feeder.contentprovider

object RssContentProviderContract {
    const val feedsMimeTypeList = "vnd.android.cursor.dir/vnd.rssprovider.feeds"
    const val feedsUriPathList = "feeds"

    /**
     * Columns available via the content provider
     */
    val feedsColumns =
        listOf(
            "id",
            "title",
        )

    const val articlesMimeTypeList = "vnd.android.cursor.dir/vnd.rssprovider.items"
    const val articlesUriPathList = "articles"
    const val articlesMimeTypeItem = "vnd.android.cursor.item/vnd.rssprovider.item"
    const val articlesUriPathItem = "articles/#"

    /**
     * Columns available via the content provider
     */
    val articlesColumns =
        listOf(
            "id",
            "title",
            "text",
        )
}
