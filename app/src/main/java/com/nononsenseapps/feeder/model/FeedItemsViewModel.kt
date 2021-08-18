package com.nononsenseapps.feeder.model

import android.util.ArrayMap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerFeed
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerItemWithUnreadCount
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTag
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import java.util.*

const val PAGE_SIZE = 100
private const val KEY_FEED_ID = "FeedItemsViewModel feedid"
private const val KEY_TAG = "FeedItemsViewModel tag"

class FeedItemsViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val dao: FeedItemDao by instance()
    private val feedDao: FeedDao by instance()
    private val liveOnlyUnread = MutableLiveData<Boolean>()
    private val liveNewestFirst = MutableLiveData<Boolean>()

    private val feedListArgsState = MutableStateFlow(
        FeedListArgs(
            feedId = ID_UNSET,
            tag = "",
            onlyUnread = false,
            newestFirst = true
        )
    )
    var feedListArgs
        get() = feedListArgsState.value
        set(value) {
            feedListArgsState.value = value
        }

    init {
        liveOnlyUnread.value = true
        liveNewestFirst.value = true
    }

    private lateinit var livePagedAll: LiveData<PagedList<PreviewItem>>
    private lateinit var livePagedUnread: LiveData<PagedList<PreviewItem>>
    private lateinit var livePreviews: LiveData<PagedList<PreviewItem>>

    @Deprecated("Use compose")
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

    val drawerItemsWithUnreadCounts: Flow<List<DrawerItemWithUnreadCount>> =
        feedDao.loadFlowOfFeedsWithUnreadCounts()
            .flatMapLatest { feeds ->
                var topTag = DrawerTop(unreadCount = 0)
                val tags: MutableMap<String, DrawerTag> = ArrayMap()
                val data: MutableList<DrawerItemWithUnreadCount> = ArrayList()

                for (feedDbo in feeds) {
                    val feed = DrawerFeed(
                        unreadCount = feedDbo.unreadCount,
                        tag = feedDbo.tag,
                        id = feedDbo.id,
                        displayTitle = feedDbo.displayTitle
                    )

                    data.add(feed)
                    topTag = topTag.copy(unreadCount = topTag.unreadCount + feed.unreadCount)

                    if (feed.tag.isNotEmpty()) {
                        val tag = tags[feed.tag] ?: DrawerTag(tag = feed.tag, unreadCount = 0)
                        tags[feed.tag] = tag.copy(unreadCount = tag.unreadCount + feed.unreadCount)
                    }
                }

                data.add(topTag)
                data.addAll(tags.values)

                // done
                flowOf(data.sorted())
            }

    fun markBeforeAsRead(index: Int) {
        viewModelScope.launch {
            val args = feedListArgsState.value
            val offset = 0
            when {
                args.onlyUnread && args.newestFirst -> {
                    when {
                        args.feedId > ID_UNSET -> dao.markAsReadDesc(
                            offset = offset,
                            limit = index,
                            feedId = args.feedId,
                            onlyUnread = 1
                        )
                        args.tag.isNotEmpty() -> dao.markAsReadDesc(
                            offset = offset,
                            limit = index,
                            tag = args.tag,
                            onlyUnread = 1
                        )
                        else -> dao.markAsReadDesc(
                            offset = offset,
                            limit = index,
                            onlyUnread = 1
                        )
                    }
                }
                args.onlyUnread -> {
                    when {
                        args.feedId > ID_UNSET -> dao.markAsReadAsc(
                            offset = offset,
                            limit = index,
                            feedId = args.feedId,
                            onlyUnread = 1
                        )
                        args.tag.isNotEmpty() -> dao.markAsReadAsc(
                            offset = offset,
                            limit = index,
                            tag = args.tag,
                            onlyUnread = 1
                        )
                        else -> dao.markAsReadAsc(
                            offset = offset,
                            limit = index,
                            onlyUnread = 1
                        )
                    }
                }
                args.newestFirst -> {
                    when {
                        args.feedId > ID_UNSET -> dao.markAsReadDesc(
                            offset = offset,
                            limit = index,
                            feedId = args.feedId,
                            onlyUnread = 0
                        )
                        args.tag.isNotEmpty() -> dao.markAsReadDesc(
                            offset = offset,
                            limit = index,
                            tag = args.tag,
                            onlyUnread = 0
                        )
                        else -> dao.markAsReadDesc(
                            offset = offset,
                            limit = index,
                            onlyUnread = 0
                        )
                    }
                }
                else -> {
                    when {
                        args.feedId > ID_UNSET -> dao.markAsReadAsc(
                            offset = offset,
                            limit = index,
                            feedId = args.feedId,
                            onlyUnread = 0
                        )
                        args.tag.isNotEmpty() -> dao.markAsReadAsc(
                            offset = offset,
                            limit = index,
                            tag = args.tag,
                            onlyUnread = 0
                        )
                        else -> dao.markAsReadAsc(
                            offset = offset,
                            limit = index,
                            onlyUnread = 0
                        )
                    }
                }
            }
        }
    }

    fun markAfterAsRead(index: Int) {
        viewModelScope.launch {
            val args = feedListArgsState.value
            val offset = index + 1
            when {
                args.onlyUnread && args.newestFirst -> {
                    when {
                        args.feedId > ID_UNSET -> dao.markAsReadDesc(
                            offset = offset,
                            feedId = args.feedId,
                            onlyUnread = 1
                        )
                        args.tag.isNotEmpty() -> dao.markAsReadDesc(
                            offset = offset,
                            tag = args.tag,
                            onlyUnread = 1
                        )
                        else -> dao.markAsReadDesc(
                            offset = offset,
                            onlyUnread = 1
                        )
                    }
                }
                args.onlyUnread -> {
                    when {
                        args.feedId > ID_UNSET -> dao.markAsReadAsc(
                            offset = offset,
                            feedId = args.feedId,
                            onlyUnread = 1
                        )
                        args.tag.isNotEmpty() -> dao.markAsReadAsc(
                            offset = offset,
                            tag = args.tag,
                            onlyUnread = 1
                        )
                        else -> dao.markAsReadAsc(
                            offset = offset,
                            onlyUnread = 1
                        )
                    }
                }
                args.newestFirst -> {
                    when {
                        args.feedId > ID_UNSET -> dao.markAsReadDesc(
                            offset = offset,
                            feedId = args.feedId,
                            onlyUnread = 0
                        )
                        args.tag.isNotEmpty() -> dao.markAsReadDesc(
                            offset = offset,
                            tag = args.tag,
                            onlyUnread = 0
                        )
                        else -> dao.markAsReadDesc(
                            offset = offset,
                            onlyUnread = 0
                        )
                    }
                }
                else -> {
                    when {
                        args.feedId > ID_UNSET -> dao.markAsReadAsc(
                            offset = offset,
                            feedId = args.feedId,
                            onlyUnread = 0
                        )
                        args.tag.isNotEmpty() -> dao.markAsReadAsc(
                            offset = offset,
                            tag = args.tag,
                            onlyUnread = 0
                        )
                        else -> dao.markAsReadAsc(
                            offset = offset,
                            onlyUnread = 0
                        )
                    }
                }
            }
        }
    }

    val feedListItems: Flow<PagingData<FeedListItem>> =
        feedListArgsState.flatMapLatest { args ->
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    enablePlaceholders = false
                )
            ) {
                when {
                    args.onlyUnread && args.newestFirst -> {
                        when {
                            args.feedId > ID_UNSET -> dao.pagingUnreadPreviewsDesc(feedId = args.feedId)
                            args.tag.isNotEmpty() -> dao.pagingUnreadPreviewsDesc(tag = args.tag)
                            else -> dao.pagingUnreadPreviewsDesc()
                        }
                    }
                    args.onlyUnread -> {
                        when {
                            args.feedId > ID_UNSET -> dao.pagingUnreadPreviewsAsc(feedId = args.feedId)
                            args.tag.isNotEmpty() -> dao.pagingUnreadPreviewsAsc(tag = args.tag)
                            else -> dao.pagingUnreadPreviewsAsc()
                        }
                    }
                    args.newestFirst -> {
                        when {
                            args.feedId > ID_UNSET -> dao.pagingPreviewsDesc(feedId = args.feedId)
                            args.tag.isNotEmpty() -> dao.pagingPreviewsDesc(tag = args.tag)
                            else -> dao.pagingPreviewsDesc()
                        }
                    }
                    else -> {
                        when {
                            args.feedId > ID_UNSET -> dao.pagingPreviewsAsc(feedId = args.feedId)
                            args.tag.isNotEmpty() -> dao.pagingPreviewsAsc(tag = args.tag)
                            else -> dao.pagingPreviewsAsc()
                        }
                    }
                }
            }
                .flow
                .map { pagingData ->
                    pagingData
                        .map { it.toFeedListItem() }
                }
        }
            .cachedIn(viewModelScope)

    val currentTitle: Flow<String?> =
        feedListArgsState.mapLatest { args ->
            when {
                args.feedId > ID_UNSET -> {
                    feedDao.getFeedTitle(args.feedId)
                        .firstOrNull()
                        ?.displayTitle
                }
                args.tag.isNotEmpty() -> args.tag
                else -> null
            }
        }

    suspend fun getFeedDisplayTitle(feedId: Long): String? {
        return feedDao.getFeedTitle(feedId).firstOrNull()?.displayTitle
    }

    fun setOnlyUnread(onlyUnread: Boolean) {
        liveOnlyUnread.value = onlyUnread
    }

    fun setNewestFirst(newestFirst: Boolean) {
        liveNewestFirst.value = newestFirst
    }

    fun markAllAsReadInBackground(feedId: Long, tag: String) = viewModelScope.launch {
        markAllAsRead(feedId, tag)
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

private fun PreviewItem.toFeedListItem() =
    FeedListItem(
        id = id,
        title = plainTitle,
        snippet = plainSnippet,
        feedTitle = feedDisplayTitle,
        unread = unread,
        pubDate = pubDate,
        imageUrl = imageUrl
    )

@Immutable
data class FeedListArgs(
    val feedId: Long,
    val tag: String,
    val newestFirst: Boolean,
    val onlyUnread: Boolean
)
