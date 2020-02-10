package com.nononsenseapps.feeder.model.opml

import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.model.OPMLParserToDatabase
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import kotlinx.coroutines.FlowPreview

@FlowPreview
class OPMLToRoom(db: AppDatabase) : OPMLParserToDatabase {

    val dao = db.feedDao()

    override suspend fun getFeed(url: String): Feed? =
            dao.loadFeedWithUrl(sloppyLinkToStrictURLNoThrows(url))

    override suspend fun saveFeed(feed: Feed) {
        val existing = dao.loadFeedWithUrl(feed.url)

        // Don't want to remove existing feed on OPML imports
        if (existing != null) {
            dao.updateFeed(feed.copy(id = existing.id))
        } else {
            dao.insertFeed(feed)
        }
    }
}
