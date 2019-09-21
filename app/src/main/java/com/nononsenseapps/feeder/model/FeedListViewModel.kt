package com.nononsenseapps.feeder.model

import androidx.collection.ArrayMap
import androidx.lifecycle.MediatorLiveData
import com.nononsenseapps.feeder.base.CoroutineScopedKodeinAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class FeedListViewModel(kodein: Kodein): CoroutineScopedKodeinAwareViewModel(kodein) {
    private val dao: FeedDao by instance()
    private val liveFeedsWithUnreadCounts = dao.loadLiveFeedsWithUnreadCounts()

    val liveFeedsAndTagsWithUnreadCounts = MediatorLiveData<List<FeedUnreadCount>>()

    init {
        liveFeedsAndTagsWithUnreadCounts.addSource(liveFeedsWithUnreadCounts) { feeds ->
            launch(Dispatchers.Default) {
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

                liveFeedsAndTagsWithUnreadCounts.postValue(data)
            }
        }
    }
}
