package com.nononsenseapps.feeder.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedItem
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
    private val liveNewestFirst = MutableLiveData<Boolean>()

    init {
        liveOnlyUnread.value = true
        liveNewestFirst.value = true
    }

    private lateinit var livePagedAll: LiveData<PagedList<PreviewItem>>
    private lateinit var livePagedUnread: LiveData<PagedList<PreviewItem>>
    private lateinit var livePreviews: LiveData<PagedList<PreviewItem>>

    fun getLiveDbPreviews(feedId: Long, tag: String): LiveData<PagedList<PreviewItem>> {
        if (!this::livePreviews.isInitialized) {
            livePagedAll = Transformations.switchMap(liveNewestFirst) { newestFirst ->
                LivePagedListBuilder(
                    when {
                        feedId > ID_UNSET -> loadLivePreviews(feedId = feedId, newestFirst = newestFirst)
                        feedId == ID_ALL_FEEDS -> loadLivePreviews(newestFirst = newestFirst)
                        tag.isNotEmpty() -> loadLivePreviews(tag = tag, newestFirst = newestFirst)
                        else -> throw IllegalArgumentException("Tag was empty, but no valid feed id was provided either")
                    },
                    PAGE_SIZE
                ).build()
            }

            livePagedUnread = Transformations.switchMap(liveNewestFirst) { newestFirst ->
                LivePagedListBuilder(
                    when {
                        feedId > ID_UNSET -> loadLiveUnreadPreviews(feedId = feedId, newestFirst = newestFirst)
                        feedId == ID_ALL_FEEDS -> loadLiveUnreadPreviews(newestFirst = newestFirst)
                        tag.isNotEmpty() -> loadLiveUnreadPreviews(tag = tag, newestFirst = newestFirst)
                        else -> throw IllegalArgumentException("Tag was empty, but no valid feed id was provided either")
                    },
                    PAGE_SIZE
                ).build()
            }

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

    fun setNewestFirst(newestFirst: Boolean) {
        liveNewestFirst.value = newestFirst
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

    suspend fun loadFeedItemsInFeed(feedId: Long, newestFirst: Boolean): List<FeedItem> {
        return if (newestFirst) dao.loadFeedItemsInFeedDesc(feedId) else dao.loadFeedItemsInFeedAsc(feedId)
    }

    fun loadLivePreviews(feedId: Long, newestFirst: Boolean): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLivePreviewsDesc(feedId) else dao.loadLivePreviewsAsc(feedId)
    }

    fun loadLivePreviews(tag: String, newestFirst: Boolean): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLivePreviewsDesc(tag) else dao.loadLivePreviewsAsc(tag)
    }

    fun loadLivePreviews(newestFirst: Boolean): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLivePreviewsDesc() else dao.loadLivePreviewsAsc()
    }

    fun loadLiveUnreadPreviews(feedId: Long?, unread: Boolean = true, newestFirst: Boolean): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLiveUnreadPreviewsDesc(feedId, unread) else dao.loadLiveUnreadPreviewsAsc(feedId, unread)
    }

    fun loadLiveUnreadPreviews(tag: String, unread: Boolean = true, newestFirst: Boolean): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLiveUnreadPreviewsDesc(tag, unread) else dao.loadLiveUnreadPreviewsAsc(tag, unread)
    }

    fun loadLiveUnreadPreviews(unread: Boolean = true, newestFirst: Boolean): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLiveUnreadPreviewsDesc(unread) else dao.loadLiveUnreadPreviewsAsc(unread)
    }
}
