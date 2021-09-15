package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.PrefValOpenWith
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.archmodel.ScreenTitle
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerItemWithUnreadCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class FeedScreenViewModel(di: DI, state: SavedStateHandle) : DIAwareViewModel(di) {
    private val repository: Repository by instance()

    val feedId: Long = state["id"]
        ?: throw IllegalArgumentException("Missing feed id in state!")

    val feedTag: String = state["tag"]
        ?: throw IllegalArgumentException("Missing tag in state!")

    val itemOpener: ItemOpener
        get() = repository.itemOpener.value

    suspend fun getLink(itemId: Long): String? = repository.getLink(itemId)

    suspend fun getArticleOpener(itemId: Long): PrefValOpenWith =
        repository.getArticleOpener(itemId)

    suspend fun getFeedDisplayTitle(): String? = repository.getDisplayTitleForFeed(feedId)

    fun setShowOnlyUnread(value: Boolean) = viewModelScope.launch {
        repository.setShowOnlyUnread(value)
    }

    fun deleteFeeds(feedIds: List<Long>) = viewModelScope.launch {
        repository.deleteFeeds(feedIds)
    }

    fun markAllAsRead() = viewModelScope.launch {
        repository.markAllAsReadInFeedOrTag(feedId, feedTag)
    }

    fun markAsUnread(itemId: Long, unread: Boolean) = viewModelScope.launch {
        repository.markAsUnread(itemId, unread)
    }

    fun markBeforeAsRead(itemIndex: Int) = viewModelScope.launch {
        repository.markBeforeAsRead(itemIndex, feedId, feedTag)
    }

    fun markAfterAsRead(itemIndex: Int) = viewModelScope.launch {
        repository.markAfterAsRead(itemIndex, feedId, feedTag)
    }

    fun requestImmediateSyncOfCurrentFeedOrTag() {
        requestFeedSync(
            di = di,
            feedId = feedId,
            feedTag = feedTag,
            ignoreConnectivitySettings = true,
            forceNetwork = true,
            parallell = true
        )
    }

    fun requestImmediateSyncOfAll() {
        requestFeedSync(
            di = di,
            ignoreConnectivitySettings = true,
            forceNetwork = true,
            parallell = true
        )
    }

    val expandedTags: StateFlow<Set<String>> = repository.expandedTags

    fun toggleTagExpansion(tag: String) = repository.toggleTagExpansion(tag)

    val currentFeedListItems: Flow<PagingData<FeedListItem>> =
        repository.getFeedListItems(feedId, feedTag)
            .cachedIn(viewModelScope)

    private val _viewState = MutableStateFlow(FeedScreenViewState())
    val viewState: StateFlow<FeedScreenViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.showOnlyUnread,
                repository.showFab,
                repository.showThumbnails,
                repository.currentTheme,
                repository.isRefreshing,
                repository.getScreenTitleForFeedOrTag(feedId, feedTag),
                repository.getVisibleFeedTitles(feedId, feedTag),
                repository.drawerItemsWithUnreadCounts,
            ) { params: Array<Any> ->
                FeedScreenViewState(
                    onlyUnread = params[0] as Boolean,
                    showFab = params[1] as Boolean,
                    showThumbnails = params[2] as Boolean,
                    currentTheme = params[3] as ThemeOptions,
                    isRefreshing = params[4] as Boolean,
                    screenTitle = params[5] as ScreenTitle,
                    visibleFeeds = params[6] as List<FeedTitle>,
                    drawerItemsWithUnreadCounts = params[7] as List<DrawerItemWithUnreadCount>
                )
            }.collect {
                _viewState.value = it
            }
        }
    }
}

@Immutable
data class FeedScreenViewState(
    val onlyUnread: Boolean = true,
    val showFab: Boolean = true,
    val showThumbnails: Boolean = true,
    val currentTheme: ThemeOptions = ThemeOptions.SYSTEM,
    val isRefreshing: Boolean = false,
    // Defaults to empty string to avoid rendering until loading complete
    val screenTitle: ScreenTitle = ScreenTitle(""),
    val visibleFeeds: List<FeedTitle> = emptyList(),
    val drawerItemsWithUnreadCounts: List<DrawerItemWithUnreadCount> = emptyList(),
)
