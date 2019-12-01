package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.db.room.Feed

interface OPMLParserToDatabase {
    suspend fun getFeed(url: String): Feed?

    suspend fun saveFeed(feed: Feed)
}
