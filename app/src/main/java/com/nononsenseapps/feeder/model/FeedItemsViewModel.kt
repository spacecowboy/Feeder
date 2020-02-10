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
import kotlinx.coroutines.FlowPreview
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

private val PAGE_SIZE = 50

@FlowPreview
class FeedItemsViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein) {
    private val dao: FeedItemDao by instance()
    private val liveOnlyUnread = MutableLiveData<Boolean>()

    init {
        liveOnlyUnread.value = true
    }

    private lateinit var livePagedAll: LiveData<PagedList<PreviewItem>>
    private lateinit var livePagedUnread: LiveData<PagedList<PreviewItem>>
    private lateinit var livePreviews: LiveData<PagedList<PreviewItem>>

    fun getLiveDbPreviews(feedId: Long, tag: String): LiveData<PagedList<PreviewItem>> {
        if (!this::livePreviews.isInitialized) {
            livePagedAll = LivePagedListBuilder(
                    when {
                        feedId > ID_UNSET -> dao.loadLivePreviews(feedId = feedId)
                        feedId == ID_ALL_FEEDS -> dao.loadLivePreviews()
                        tag.isNotEmpty() -> dao.loadLivePreviews(tag = tag)
                        else -> throw IllegalArgumentException("Tag was empty, but no valid feed id was provided either")
                    }, PAGE_SIZE).build()


            livePagedUnread = LivePagedListBuilder(
                    when {
                        feedId > ID_UNSET -> dao.loadLiveUnreadPreviews(feedId = feedId)
                        feedId == ID_ALL_FEEDS -> dao.loadLiveUnreadPreviews()
                        tag.isNotEmpty() -> dao.loadLiveUnreadPreviews(tag = tag)
                        else -> throw IllegalArgumentException("Tag was empty, but no valid feed id was provided either")
                    }, PAGE_SIZE).build()

            livePreviews = Transformations.switchMap(liveOnlyUnread) { onlyUnread ->
                if (onlyUnread) {
                    livePagedUnread
                } else {
                    livePagedAll
                }
            }
        }
        return livePreviews
    }

    fun setOnlyUnread(onlyUnread: Boolean) {
        liveOnlyUnread.value = onlyUnread
    }

    suspend fun markAllAsRead(feedId: Long, tag: String) {
        when {
            feedId > ID_UNSET -> dao.markAllAsRead(feedId)
            feedId == ID_ALL_FEEDS -> dao.markAllAsRead()
            tag.isNotEmpty() -> dao.markAllAsRead(tag)
        }
    }

    suspend fun toggleReadState(feedItem: PreviewItem) {
        dao.markAsRead(feedItem.id, unread = !feedItem.unread)
        cancelNotification(getApplication(), feedItem.id)
    }

    suspend fun markAsNotified(feedId: Long, tag: String) = when {
        feedId > ID_UNSET -> dao.markAsNotified(feedId)
        feedId == ID_ALL_FEEDS -> dao.markAllAsNotified()
        tag.isNotEmpty() -> dao.markTagAsNotified(tag)
        else -> error("Invalid input for markAsNotified")
    }

    suspend fun markAsNotified(ids: List<Long>, notified: Boolean = true) =
            dao.markAsNotified(ids = ids, notified = notified)

    suspend fun markAsRead(ids: List<Long>, unread: Boolean = false) =
            dao.markAsRead(ids = ids, unread = unread)

    suspend fun markAsRead(id: Long, unread: Boolean = false) =
            dao.markAsRead(id = id, unread = unread)
}
