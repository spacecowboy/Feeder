package com.nononsenseapps.feeder.model

import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import kotlin.collections.set

@ExperimentalCoroutinesApi
class FeedListViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein) {
    private val dao: FeedDao by instance()
    private val feedsWithUnreadCounts = dao.loadLiveFeedsWithUnreadCounts()

    val liveFeedsAndTagsWithUnreadCounts: LiveData<List<FeedUnreadCount>> by lazy {
        liveData<List<FeedUnreadCount>>(viewModelScope.coroutineContext + Dispatchers.Default, 5000L) {
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
}
