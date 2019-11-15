package com.nononsenseapps.feeder.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

private val PAGE_SIZE = 50

class FeedItemsViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein) {
    private val dao: FeedItemDao by instance()
    private val liveOnlyUnread = MutableLiveData<Boolean>()

    init {
        liveOnlyUnread.value = true
    }

    fun getLiveDbPreviews(feedId: Long, tag: String): LiveData<PagedList<PreviewItem>> {
        val livePagedAll = LivePagedListBuilder(
                when {
                    feedId > ID_UNSET -> dao.loadLivePreviews(feedId = feedId)
                    feedId == ID_ALL_FEEDS -> dao.loadLivePreviews()
                    tag.isNotEmpty() -> dao.loadLivePreviews(tag = tag)
                    else -> throw IllegalArgumentException("Tag was empty, but no valid feed id was provided either")
                }, PAGE_SIZE).build()

        val livePagedUnread = LivePagedListBuilder(
                when {
                    feedId > ID_UNSET -> dao.loadLiveUnreadPreviews(feedId = feedId)
                    feedId == ID_ALL_FEEDS -> dao.loadLiveUnreadPreviews()
                    tag.isNotEmpty() -> dao.loadLiveUnreadPreviews(tag = tag)
                    else -> throw IllegalArgumentException("Tag was empty, but no valid feed id was provided either")
                }, PAGE_SIZE).build()

        return Transformations.switchMap(liveOnlyUnread) { onlyUnread ->
            if (onlyUnread) {
                livePagedUnread
            } else {
                livePagedAll
            }
        }
    }

    fun setOnlyUnread(onlyUnread: Boolean) {
        liveOnlyUnread.value = onlyUnread
    }

    suspend fun markAllAsRead(feedId: Long, tag: String) = withContext(Dispatchers.Default) {
        when {
            feedId > ID_UNSET -> dao.markAllAsRead(feedId)
            feedId == ID_ALL_FEEDS -> dao.markAllAsRead()
            tag.isNotEmpty() -> dao.markAllAsRead(tag)
        }
    }

    suspend fun toggleReadState(feedItem: PreviewItem) = withContext(Dispatchers.Default) {
        dao.markAsRead(feedItem.id, unread = !feedItem.unread)
        cancelNotification(getApplication(), feedItem.id)
    }

    suspend fun markAsNotified(feedId: Long, tag: String) = withContext(Dispatchers.Default) {
        when {
            feedId > ID_UNSET -> dao.markAsNotified(feedId)
            feedId == ID_ALL_FEEDS -> dao.markAllAsNotified()
            tag.isNotEmpty() -> dao.markTagAsNotified(tag)
        }
    }
}
/*

class FeedItemsViewModel(application: Application, val feedId: Long, val tag: String, onlyUnread: Boolean = true) : CoroutineScopedViewModel(application), KodeinAware {
    override val kodein by closestKodein()

    private val dao: FeedItemDao by instance()
    private val liveOnlyUnread = MutableLiveData<Boolean>()

    private val livePagedAll = LivePagedListBuilder(
            when {
                feedId > ID_UNSET -> dao.loadLivePreviews(feedId = feedId)
                feedId == ID_ALL_FEEDS -> dao.loadLivePreviews()
                tag.isNotEmpty() -> dao.loadLivePreviews(tag = tag)
                else -> throw IllegalArgumentException("Tag was empty, but no valid feed id was provided either")
            }, PAGE_SIZE).build()

    private val livePagedUnread = LivePagedListBuilder(
            when {
                feedId > ID_UNSET -> dao.loadLiveUnreadPreviews(feedId = feedId)
                feedId == ID_ALL_FEEDS -> dao.loadLiveUnreadPreviews()
                tag.isNotEmpty() -> dao.loadLiveUnreadPreviews(tag = tag)
                else -> throw IllegalArgumentException("Tag was empty, but no valid feed id was provided either")
            }, PAGE_SIZE).build()

    val liveDbPreviews: LiveData<PagedList<PreviewItem>> = Transformations.switchMap(liveOnlyUnread) { onlyUnread ->
        if (onlyUnread) {
            livePagedUnread
        } else {
            livePagedAll
        }
    }

    init {
        liveOnlyUnread.value = onlyUnread
    }

    fun setOnlyUnread(onlyUnread: Boolean) {
        liveOnlyUnread.value = onlyUnread
    }

    fun markAllAsRead() = launch(Dispatchers.Default) {
        when {
            feedId > ID_UNSET -> dao.markAllAsRead(feedId)
            feedId == ID_ALL_FEEDS -> dao.markAllAsRead()
            tag.isNotEmpty() -> dao.markAllAsRead(tag)
        }
    }

    fun toggleReadState(feedItem: PreviewItem) = launch(Dispatchers.Default) {
        dao.markAsRead(feedItem.id, unread = !feedItem.unread)
        cancelNotification(getApplication(), feedItem.id)
    }

    */
/**
     * Already called within a coroutine scope, do not launch another to keep ordering guarantees
     *//*

    suspend fun markAsNotified() = withContext(Dispatchers.Default) {
        when {
            feedId > ID_UNSET -> dao.markAsNotified(feedId)
            feedId == ID_ALL_FEEDS -> dao.markAllAsNotified()
            tag.isNotEmpty() -> dao.markTagAsNotified(tag)
        }
    }
}


class FeedItemsViewModelFactory(private val application: Application,
                                private val feedId: Long,
                                private val tag: String,
                                private val onlyUnread: Boolean) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FeedItemsViewModel(application = application, feedId = feedId, tag = tag, onlyUnread = onlyUnread) as T
    }
}

fun Fragment.getFeedItemsViewModel(onlyUnread: Boolean = true, feedId: Long = ID_UNSET, tag: String = ""): FeedItemsViewModel {
    val factory = FeedItemsViewModelFactory(activity!!.application,
            onlyUnread = onlyUnread,
            feedId = feedId,
            tag = tag)
    return ViewModelProviders.of(this, factory).get(FeedItemsViewModel::class.java)
}
*/
