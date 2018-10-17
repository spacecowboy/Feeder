package com.nononsenseapps.feeder.model

import android.app.Application
import androidx.collection.ArrayMap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProviders
import com.nononsenseapps.feeder.coroutines.Background
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class FeedListViewModel(application: Application): AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).feedDao()
    private val liveFeedsWithUnreadCounts = dao.loadLiveFeedsWithUnreadCounts()

    val liveFeedsAndTagsWithUnreadCounts = MediatorLiveData<List<FeedUnreadCount>>()

    init {
        liveFeedsAndTagsWithUnreadCounts.addSource(liveFeedsWithUnreadCounts) { feeds ->
            launch(Background) {
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

                data.sortWith(Comparator {a, b -> a.compareTo(b)})

                withContext(UI) {
                    liveFeedsAndTagsWithUnreadCounts.value = data
                }
            }
        }
    }
}

fun FragmentActivity.getFeedListViewModel(): FeedListViewModel {
    return ViewModelProviders.of(this).get(FeedListViewModel::class.java)
}
