package com.nononsenseapps.feeder.model

import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import org.kodein.di.DI
import org.kodein.di.instance
import java.util.*
import kotlin.collections.set

class FeedListViewModel(di: DI) : DIAwareViewModel(di) {
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

    fun onItemClicked(item: FeedUnreadCount) {
        // TODO
    }
}
