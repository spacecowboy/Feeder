package com.nononsenseapps.feeder.model

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.blob.blobOutputStream
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.ui.TestDatabaseRule

suspend fun TestDatabaseRule.insertFeedItemWithBlob(
    feedItem: FeedItem,
    description: String
): Long {
    val feedItemId = db.feedItemDao().insertFeedItem(feedItem)

    blobOutputStream(
        itemId = feedItemId,
        filesDir = getApplicationContext<FeederApplication>().filesDir
    ).bufferedWriter().use {
        it.write(description)
    }

    return feedItemId
}
