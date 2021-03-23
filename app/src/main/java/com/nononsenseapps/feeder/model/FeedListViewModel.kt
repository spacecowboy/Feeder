package com.nononsenseapps.feeder.model

import androidx.collection.ArrayMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import kotlin.collections.set

@ExperimentalCoroutinesApi
class FeedListViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein) {
    private val dao: FeedDao by instance()
    private val feedsWithUnreadCounts = dao.loadLiveFeedsWithUnreadCounts()

    val liveFeedsAndTagsWithUnreadCounts: LiveData<List<FeedUnreadCount>> by lazy {
        liveData<List<FeedUnreadCount>>(
            viewModelScope.coroutineContext + Dispatchers.Default,
            5000L
        ) {
            feedsWithUnreadCounts.collectLatest { feeds ->
                val topTag = FeedUnreadCount(id = ID_ALL_FEEDS)
                val tags: MutableMap<String, FeedUnreadCount> = ArrayMap()
                val data: MutableList<FeedUnreadCount> = mutableListOf(topTag)

                feeds.forEach { feed ->
                    if (feed.tag.isNotEmpty()) {
                        if (!tags.contains(feed.tag)) {
                            val tag = FeedUnreadCount(tag = feed.tag)
                            data.add(tag)
                            tags[feed.tag] = tag
                        }

                        tags[feed.tag]?.let { tag ->
                            tag.unreadCount += feed.unreadCount
                        }
                    }

                    topTag.unreadCount += feed.unreadCount

                    data.add(feed)
                }

                data.sortWith(Comparator { a, b -> a.compareTo(b) })

                emit(data)
            }
        }
    }

    private val _expandedTags: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    val expandedTags: StateFlow<Set<String>>
        get() = _expandedTags.asStateFlow()

    fun toggleExpansion(tag: String) {
        _expandedTags.value = _expandedTags.value.toMutableSet().also { set ->
            if (!set.add(tag)) {
                set.remove(tag)
            }
        }
    }

    fun onItemClicked(item: FeedUnreadCount) {
        // TODO
    }
}
