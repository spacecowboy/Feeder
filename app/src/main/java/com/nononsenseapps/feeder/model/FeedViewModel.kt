package com.nononsenseapps.feeder.model

import androidx.lifecycle.LiveData
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class FeedViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein) {
    private val dao: FeedDao by instance()

    fun getLiveFeed(id: Long): LiveData<Feed> = dao.loadLiveFeed(id)

    suspend fun deleteFeedWithId(id: Long) = withContext(Dispatchers.Default) {
        dao.deleteFeedWithId(id)
    }
}
