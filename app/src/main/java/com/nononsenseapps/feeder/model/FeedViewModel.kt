package com.nononsenseapps.feeder.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.upsertFeed
import com.nononsenseapps.feeder.util.removeDynamicShortcutToFeed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance


private const val KEY_FEED_ID = "FeedViewModel feedid"

class FeedViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val dao: FeedDao by instance()
    private val coroutineScope: ApplicationCoroutineScope by instance()

    var currentFeedId: Long
        get() = state[KEY_FEED_ID] ?: ID_UNSET
        set(value) = state.set(KEY_FEED_ID, value)

    private val currentLiveFeedId: LiveData<Long> =
        state.getLiveData(KEY_FEED_ID, ID_UNSET)

    val currentLiveFeed: LiveData<Feed?> =
        currentLiveFeedId.switchMap { feedId ->
            dao.loadFeedFlow(feedId).asLiveData()
        }

    private lateinit var liveFeedsNotify: LiveData<List<Boolean>>

    fun getLiveFeedsNotify(id: Long, tag: String): LiveData<List<Boolean>> {
        if (!this::liveFeedsNotify.isInitialized) {
            liveFeedsNotify = when {
                id > ID_UNSET -> dao.loadLiveFeedsNotify(feedId = id)
                id == ID_UNSET && tag.isNotEmpty() -> dao.loadLiveFeedsNotify(tag = tag)
                else -> dao.loadLiveFeedsNotify()
            }.asLiveData()
        }
        return liveFeedsNotify
    }

    private lateinit var liveFeed: LiveData<Feed?>

    fun getLiveFeed(id: Long): LiveData<Feed?> {
        if (!this::liveFeed.isInitialized) {
            liveFeed = dao.loadFeedFlow(feedId = id).asLiveData()
        }
        return liveFeed
    }

    private val _feedState = MutableStateFlow<Feed?>(null)
    private val feedState: StateFlow<Feed?> = _feedState.asStateFlow()

    fun getFeedState(id: Long): StateFlow<Feed?> {
        viewModelScope.launch {
            _feedState.value = dao.loadFeed(feedId = id)
        }
        return feedState
    }

    suspend fun getFeed(id: Long): Feed? {
        return dao.loadFeed(feedId = id)
    }

    suspend fun setNotify(tag: String, notify: Boolean) {
        dao.setNotify(tag = tag, notify = notify)
    }

    suspend fun setNotify(id: Long, notify: Boolean) {
        dao.setNotify(id = id, notify = notify)
    }

    suspend fun setAllNotify(notify: Boolean) {
        dao.setAllNotify(notify = notify)
    }

    @Deprecated("Use FeedListViewModel instead")
    suspend fun deleteFeed(id: Long) {
        dao.deleteFeedWithId(feedId = id)

        val context: Context by instance()
        context.removeDynamicShortcutToFeed(id)
    }

    @Deprecated("Use FeedListViewModel instead")
    suspend fun deleteFeeds(ids: List<Long>) {
        dao.deleteFeeds(ids)

        val context: Context by instance()
        for (id in ids) {
            context.removeDynamicShortcutToFeed(id)
        }
    }

    suspend fun getVisibleFeeds(id: Long, feedTag: String?): List<FeedTitle> {
        return when {
            id == ID_UNSET && feedTag?.isNotEmpty() == true -> {
                dao.getFeedTitlesWithTag(feedTag = feedTag)
            }
            id == ID_UNSET || id == ID_ALL_FEEDS -> {
                dao.getAllFeedTitles()
            }
            id > ID_UNSET -> {
                dao.getFeedTitle(id)
            }
            else -> emptyList()
        }
    }

    fun save(feed: Feed) {
        coroutineScope.launch {
            dao.upsertFeed(feed)
        }
    }
}
