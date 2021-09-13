package com.nononsenseapps.feeder.model

import android.util.ArrayMap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import com.nononsenseapps.feeder.ui.compose.feed.FeedOrTag
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerFeed
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerItemWithUnreadCount
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTag
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTop
import com.nononsenseapps.feeder.util.SortingOptions
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

const val PAGE_SIZE = 100

class FeedItemsViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()
    private val settingsViewModel: SettingsViewModel by instance(arg = di.direct.instance<DIAwareComponentActivity>())
    private val dao: FeedItemDao by instance()
    private val feedDao: FeedDao by instance()

    private val _currentFeedOrTag = MutableStateFlow(FeedOrTag(-1, ""))

    var feedOrTag: FeedOrTag
        get() = _currentFeedOrTag.value
        set(value) {
            _currentFeedOrTag.value = value
        }

    private val feedListArgsState = combine(
        _currentFeedOrTag,
        settingsViewModel.showOnlyUnread,
        settingsViewModel.currentSorting
    ) { feedOrTag, onlyUnread, sorting ->
        FeedListArgs(
            feedId = feedOrTag.id,
            tag = feedOrTag.tag,
            onlyUnread = onlyUnread,
            newestFirst = sorting==SortingOptions.NEWEST_FIRST,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        FeedListArgs(
            feedId = settingsViewModel.currentFeedAndTag.value.first,
            tag = settingsViewModel.currentFeedAndTag.value.second,
            onlyUnread = true,
            newestFirst = true,
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    fun markAllAsReadInBackground(feedId: Long, tag: String) = viewModelScope.launch {
        markAllAsRead(feedId, tag)
    }

    suspend fun markAllAsRead(feedId: Long, tag: String) {
        when {
            feedId > ID_UNSET -> dao.markAllAsRead(feedId)
            feedId==ID_ALL_FEEDS -> dao.markAllAsRead()
            tag.isNotEmpty() -> dao.markAllAsRead(tag)
        }
    }

    suspend fun toggleReadState(feedItem: PreviewItem) {
        dao.markAsRead(feedItem.id, unread = !feedItem.unread)
        cancelNotification(getApplication(), feedItem.id)
    }

    suspend fun markAsNotified(ids: List<Long>, notified: Boolean = true) =
        dao.markAsNotified(ids = ids, notified = notified)

    fun markAsNotifiedInBackground(ids: List<Long>, notified: Boolean = true) =
        applicationCoroutineScope.launch {
            markAsNotified(ids, notified)
        }

    suspend fun markAsRead(ids: List<Long>, unread: Boolean = false) =
        dao.markAsRead(ids = ids, unread = unread)

    suspend fun markAsRead(id: Long, unread: Boolean = false) =
        dao.markAsRead(id = id, unread = unread)

    suspend fun loadFeedItemsInFeed(feedId: Long, newestFirst: Boolean): List<FeedItem> {
        return if (newestFirst) dao.loadFeedItemsInFeedDesc(feedId) else dao.loadFeedItemsInFeedAsc(
            feedId
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
    val onlyUnread: Boolean,
)
