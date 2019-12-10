package com.nononsenseapps.feeder.model

import androidx.lifecycle.asLiveData
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.upsertFeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class FeedViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein) {
    private val dao: FeedDao by instance()

    fun getLiveFeed(id: Long) = dao.loadLiveFeed(id).asLiveData()

    suspend fun deleteFeedWithId(id: Long) = withContext(Dispatchers.Default) {
        dao.deleteFeedWithId(id)
    }

    fun loadLiveFeedsNotify(tag: String) = dao.loadLiveFeedsNotify(tag = tag).asLiveData()

    fun loadLiveFeedsNotify() = dao.loadLiveFeedsNotify().asLiveData()

    suspend fun setNotify(id: Long, notify: Boolean) = dao.setNotify(id = id, notify = notify)

    suspend fun setNotify(tag: String, notify: Boolean) = dao.setNotify(tag = tag, notify = notify)

    suspend fun setAllNotify(notify: Boolean) = dao.setAllNotify(notify = notify)

    suspend fun upsertFeed(feed: Feed): Long = dao.upsertFeed(feed)

    suspend fun loadTags(): List<String> = dao.loadTags()
}
