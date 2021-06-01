package com.nononsenseapps.feeder.model

import android.app.Application
import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.util.removeDynamicShortcutToFeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import java.util.*
import kotlin.collections.set

class FeedListViewModel(di: DI) : DIAwareViewModel(di) {
    private val dao: FeedDao by instance()
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()
    private val feedsWithUnreadCounts = dao.loadLiveFeedsWithUnreadCounts()

    val liveFeedsAndTagsWithUnreadCounts: LiveData<List<FeedUnreadCount>> by lazy {
        liveData<List<FeedUnreadCount>>(
            viewModelScope.coroutineContext + Dispatchers.Default,
            5000L
        ) {
            feedsWithUnreadCounts.collectLatest { feeds ->
                val topTag = FeedUnreadCount(id = ID_ALL_FEEDS)
                val tags: MutableMap<String, FeedUnreadCount> = ArrayMap()
                val data: SortedSet<FeedUnreadCount> = sortedSetOf(topTag)

                feeds.forEach { feed ->
                    if (feed.tag.isNotEmpty()) {
                        if (feed.tag !in tags) {
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

//                data.sortedBy { it }
                emit(data.toList())
            }
        }
    }

    fun deleteFeeds(ids: List<Long>) {
        applicationCoroutineScope.launch {
            dao.deleteFeeds(ids)

            val application: Application by instance()
            for (id in ids) {
                application.removeDynamicShortcutToFeed(id)
            }
        }
    }

    fun getFeedTitles(feedId: Long, tag: String): Flow<List<FeedTitle>> = flow {
        emit(
            when {
                feedId > ID_UNSET -> dao.getFeedTitle(feedId = feedId)
                tag.isNotEmpty() -> dao.getFeedTitlesWithTag(feedTag = tag)
                else -> dao.getAllFeedTitles()
            }
        )
    }
}
