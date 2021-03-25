package com.nononsenseapps.feeder.model

import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.collections.set

class FeedListViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein) {
    private val dao: FeedDao by instance()
    private val feedsWithUnreadCounts = dao.loadLiveFeedsWithUnreadCounts()

    val liveFeedsAndTagsWithUnreadCounts: LiveData<List<FeedUnreadCount>> by lazy {
        liveData<List<FeedUnreadCount>>(
            viewModelScope.coroutineContext + Dispatchers.Default,
            5000L
        ) {
            feedsWithUnreadCounts.collectLatest { feeds ->
                val topTag = FeedUnreadCount.topTag
                val tags: MutableMap<String, FeedUnreadCount> = ArrayMap()
                val data: SortedSet<FeedUnreadCount> = sortedSetOf(topTag)

                feeds.forEach { feed ->
                    if (feed.tag.isNotEmpty()) {
                        if (feed.tag !in tags) {
                            val tag = FeedUnreadCount(tag = feed.tag)
                            data.add(tag)
                            tags[feed.tag] = tag
                            topTag.addChild(tag)
                        }

                        tags[feed.tag]?.let { tag ->
                            tag.unreadCount += feed.unreadCount
                            tag.addChild(feed)
                        }
                    }

                    topTag.unreadCount += feed.unreadCount

                    data.add(feed)
                }

//                data.sortedBy { it }
                emit(data.toList())
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
