package com.nononsenseapps.feeder.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import org.kodein.di.DI
import org.kodein.di.instance

const val PAGE_SIZE = 100

class FeedItemsViewModel(di: DI) : DIAwareViewModel(di) {
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
    private var previewPager: PreviewPager? = null

    fun getLiveDbPreviews(feedId: Long, tag: String): LiveData<PagedList<PreviewItem>> {
        if (!this::livePreviews.isInitialized) {
            livePagedAll = Transformations.switchMap(liveNewestFirst) { newestFirst ->
                LivePagedListBuilder(
                    when {
                        feedId > ID_UNSET -> loadLivePreviews(
                            feedId = feedId,
                            newestFirst = newestFirst
                        )
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
                        feedId > ID_UNSET -> loadLiveUnreadPreviews(
                            feedId = feedId,
                            newestFirst = newestFirst
                        )
                        feedId == ID_ALL_FEEDS -> loadLiveUnreadPreviews(newestFirst = newestFirst)
                        tag.isNotEmpty() -> loadLiveUnreadPreviews(
                            tag = tag,
                            newestFirst = newestFirst
                        )
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

    fun getPreviewPager(
        feedId: Long,
        tag: String,
        newestFirst: Boolean,
        onlyUnread: Boolean
    ): Flow<PagingData<PreviewItem>> {
        Log.d("JONAS", "Pager with onlyUnread: $onlyUnread")

        val args = PreviewFlowArgs(
            feedId = feedId,
            tag = tag,
            newestFirst = newestFirst,
            onlyUnread = onlyUnread
        )
        previewPager?.let { prev ->
            if (prev.args == args) {
                Log.d("JONAS", "Already have a pager which I'm returning")
                return prev.flow
            }
        }

        val flow = Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            )
        ) {
            when {
                onlyUnread && newestFirst -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingUnreadPreviewsDesc(feedId = feedId)
                        tag.isNotEmpty() -> dao.pagingUnreadPreviewsDesc(tag = tag)
                        else -> dao.pagingUnreadPreviewsDesc()
                    }
                }
                onlyUnread -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingUnreadPreviewsAsc(feedId = feedId)
                        tag.isNotEmpty() -> dao.pagingUnreadPreviewsAsc(tag = tag)
                        else -> dao.pagingUnreadPreviewsAsc()
                    }
                }
                newestFirst -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingPreviewsDesc(feedId = feedId)
                        tag.isNotEmpty() -> dao.pagingPreviewsDesc(tag = tag)
                        else -> dao.pagingPreviewsDesc()
                    }
                }
                else -> {
                    when {
                        feedId > ID_UNSET -> dao.pagingPreviewsAsc(feedId = feedId)
                        tag.isNotEmpty() -> dao.pagingPreviewsAsc(tag = tag)
                        else -> dao.pagingPreviewsAsc()
                    }
                }
            }
        }
            .flow
            .cachedIn(viewModelScope)

        Log.d("JONAS", "Making new pager from scratch")
        previewPager = PreviewPager(
            args = args,
            flow = flow
        )

        return flow
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
        return if (newestFirst) dao.loadFeedItemsInFeedDesc(feedId) else dao.loadFeedItemsInFeedAsc(
            feedId
        )
    }

    private fun loadLivePreviews(
        feedId: Long,
        newestFirst: Boolean
    ): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLivePreviewsDesc(feedId) else dao.loadLivePreviewsAsc(feedId)
    }

    private fun loadLivePreviews(
        tag: String,
        newestFirst: Boolean
    ): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLivePreviewsDesc(tag) else dao.loadLivePreviewsAsc(tag)
    }

    private fun loadLivePreviews(newestFirst: Boolean): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLivePreviewsDesc() else dao.loadLivePreviewsAsc()
    }

    private fun loadLiveUnreadPreviews(
        feedId: Long?,
        unread: Boolean = true,
        newestFirst: Boolean
    ): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLiveUnreadPreviewsDesc(
            feedId,
            unread
        ) else dao.loadLiveUnreadPreviewsAsc(feedId, unread)
    }

    private fun loadLiveUnreadPreviews(
        tag: String,
        unread: Boolean = true,
        newestFirst: Boolean
    ): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLiveUnreadPreviewsDesc(
            tag,
            unread
        ) else dao.loadLiveUnreadPreviewsAsc(tag, unread)
    }

    private fun loadLiveUnreadPreviews(
        unread: Boolean = true,
        newestFirst: Boolean
    ): DataSource.Factory<Int, PreviewItem> {
        return if (newestFirst) dao.loadLiveUnreadPreviewsDesc(unread) else dao.loadLiveUnreadPreviewsAsc(
            unread
        )
    }
}

data class PreviewPager(
    val args: PreviewFlowArgs,
    val flow: Flow<PagingData<PreviewItem>>
)

data class PreviewFlowArgs(
    val feedId: Long,
    val tag: String,
    val newestFirst: Boolean,
    val onlyUnread: Boolean
)
