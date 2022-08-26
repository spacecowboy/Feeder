package com.nononsenseapps.feeder.archmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.paging.PagingData
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.FeedItemForReadMark
import com.nononsenseapps.feeder.db.room.FeedItemIdWithLink
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.RemoteFeed
import com.nononsenseapps.feeder.db.room.SyncDevice
import com.nononsenseapps.feeder.db.room.SyncRemote
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerItemWithUnreadCount
import com.nononsenseapps.feeder.util.addDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.reportShortcutToFeedUsed
import java.net.URL
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class Repository(override val di: DI) : DIAware {
    private val settingsStore: SettingsStore by instance()
    private val sessionStore: SessionStore by instance()
    private val feedItemStore: FeedItemStore by instance()
    private val feedStore: FeedStore by instance()
    private val androidSystemStore: AndroidSystemStore by instance()
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()
    private val application: Application by instance()
    private val syncRemoteStore: SyncRemoteStore by instance()

    val showOnlyUnread: StateFlow<Boolean> = settingsStore.showOnlyUnread
    fun setShowOnlyUnread(value: Boolean) = settingsStore.setShowOnlyUnread(value)
    val showOnlyBookmarked: StateFlow<Boolean> = settingsStore.showOnlyBookmarked
    fun setShowOnlyBookmarked(value: Boolean) = settingsStore.setShowOnlyBookmarked(value)

    val currentFeedAndTag: StateFlow<Pair<Long, String>> = settingsStore.currentFeedAndTag
    fun setCurrentFeedAndTag(feedId: Long, tag: String) {
        if (feedId > ID_UNSET) {
            applicationCoroutineScope.launch {
                application.apply {
                    addDynamicShortcutToFeed(
                        feedStore.getDisplayTitle(feedId) ?: "",
                        feedId,
                        null
                    )
                    // Report shortcut usage
                    reportShortcutToFeedUsed(feedId)
                }
            }
        }
        settingsStore.setCurrentFeedAndTag(feedId, tag)
    }

    val isArticleOpen: StateFlow<Boolean> = settingsStore.isArticleOpen
    fun setIsArticleOpen(open: Boolean) {
        settingsStore.setIsArticleOpen(open)
    }

    val currentArticleId: StateFlow<Long> = settingsStore.currentArticleId
    fun setCurrentArticle(articleId: Long) =
        settingsStore.setCurrentArticle(articleId)

    val currentTheme: StateFlow<ThemeOptions> = settingsStore.currentTheme
    fun setCurrentTheme(value: ThemeOptions) = settingsStore.setCurrentTheme(value)

    val preferredDarkTheme: StateFlow<DarkThemePreferences> = settingsStore.darkThemePreference
    fun setPreferredDarkTheme(value: DarkThemePreferences) =
        settingsStore.setDarkThemePreference(value)

    val blockList: StateFlow<Set<String>> = settingsStore.blockListPreference
    fun setBlockList(value: Iterable<String>) =
        settingsStore.setBlockListPreference(value)

    val currentSorting: StateFlow<SortingOptions> = settingsStore.currentSorting
    fun setCurrentSorting(value: SortingOptions) = settingsStore.setCurrentSorting(value)

    val showFab: StateFlow<Boolean> = settingsStore.showFab
    fun setShowFab(value: Boolean) = settingsStore.setShowFab(value)

    val feedItemStyle: StateFlow<FeedItemStyle> = settingsStore.feedItemStyle
    fun setFeedItemStyle(value: FeedItemStyle) = settingsStore.setFeedItemStyle(value)

    val swipeAsRead: StateFlow<SwipeAsRead> = settingsStore.swipeAsRead
    fun setSwipeAsRead(value: SwipeAsRead) = settingsStore.setSwipeAsRead(value)

    val syncOnResume: StateFlow<Boolean> = settingsStore.syncOnResume
    fun setSyncOnResume(value: Boolean) = settingsStore.setSyncOnResume(value)

    val syncOnlyOnWifi: StateFlow<Boolean> = settingsStore.syncOnlyOnWifi
    suspend fun setSyncOnlyOnWifi(value: Boolean) = settingsStore.setSyncOnlyOnWifi(value)

    val syncOnlyWhenCharging: StateFlow<Boolean> = settingsStore.syncOnlyWhenCharging
    suspend fun setSyncOnlyWhenCharging(value: Boolean) =
        settingsStore.setSyncOnlyWhenCharging(value)

    val loadImageOnlyOnWifi = settingsStore.loadImageOnlyOnWifi
    fun setLoadImageOnlyOnWifi(value: Boolean) = settingsStore.setLoadImageOnlyOnWifi(value)

    val showThumbnails = settingsStore.showThumbnails
    fun setShowThumbnails(value: Boolean) = settingsStore.setShowThumbnails(value)

    val useDetectLanguage = settingsStore.useDetectLanguage
    fun setUseDetectLanguage(value: Boolean) = settingsStore.setUseDetectLanguage(value)

    val useDynamicTheme = settingsStore.useDynamicTheme
    fun setUseDynamicTheme(value: Boolean) = settingsStore.setUseDynamicTheme(value)

    val maximumCountPerFeed = settingsStore.maximumCountPerFeed
    fun setMaxCountPerFeed(value: Int) = settingsStore.setMaxCountPerFeed(value)

    val itemOpener = settingsStore.itemOpener
    fun setItemOpener(value: ItemOpener) = settingsStore.setItemOpener(value)

    val linkOpener = settingsStore.linkOpener
    fun setLinkOpener(value: LinkOpener) = settingsStore.setLinkOpener(value)

    val syncFrequency = settingsStore.syncFrequency
    suspend fun setSyncFrequency(value: SyncFrequency) = settingsStore.setSyncFrequency(value)

    val resumeTime: StateFlow<Instant> = sessionStore.resumeTime
    fun setResumeTime(value: Instant) {
        sessionStore.setResumeTime(value)
    }

    val currentlySyncingLatestTimestamp: Flow<Instant> =
        feedStore.getCurrentlySyncingLatestTimestamp()
            .map { value ->
                value ?: Instant.EPOCH
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFeedListItems(feedId: Long, tag: String): Flow<PagingData<FeedListItem>> = combine(
        showOnlyUnread,
        currentSorting,
        showOnlyBookmarked,
    ) { showOnlyUnread, currentSorting, showOnlyBookmarked ->
        FeedListArgs(
            feedId = feedId,
            tag = tag,
            onlyUnread = showOnlyUnread,
            newestFirst = currentSorting == SortingOptions.NEWEST_FIRST,
            onlyBookmarks = showOnlyBookmarked
        )
    }.flatMapLatest {
        feedItemStore.getPagedFeedItems(
            feedId = it.feedId,
            tag = it.tag,
            onlyUnread = it.onlyUnread,
            newestFirst = it.newestFirst,
            onlyBookmarks = it.onlyBookmarks,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentFeedListItems(): Flow<PagingData<FeedListItem>> = combine(
        currentFeedAndTag,
        showOnlyUnread,
        currentSorting,
        showOnlyBookmarked,
    ) { feedAndTag, showOnlyUnread, currentSorting, showOnlyBookmarked ->
        val (feedId, tag) = feedAndTag
        FeedListArgs(
            feedId = feedId,
            tag = tag,
            onlyUnread = showOnlyUnread,
            newestFirst = currentSorting == SortingOptions.NEWEST_FIRST,
            onlyBookmarks = showOnlyBookmarked
        )
    }.flatMapLatest {
        feedItemStore.getPagedFeedItems(
            feedId = it.feedId,
            tag = it.tag,
            onlyUnread = it.onlyUnread,
            newestFirst = it.newestFirst,
            onlyBookmarks = it.onlyBookmarks,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentFeedListVisibleItemCount(): Flow<Int> = combine(
        currentFeedAndTag,
        showOnlyUnread,
        currentSorting,
    ) { feedAndTag, showOnlyUnread, _ ->
        val (feedId, tag) = feedAndTag
        FeedListArgs(
            feedId = feedId,
            tag = tag,
            onlyUnread = showOnlyUnread,
            newestFirst = false,
            onlyBookmarks = false
        )
    }.flatMapLatest {
        feedItemStore.getVisibleFeedItemCount(
            feedId = it.feedId,
            tag = it.tag,
            onlyUnread = it.onlyUnread
        )
    }

    val currentArticle: Flow<Article> = currentArticleId
        .flatMapLatest { itemId ->
            feedItemStore.getFeedItem(itemId)
        }
        .mapLatest { item ->
            Article(item = item)
        }

    suspend fun getFeed(feedId: Long): Feed? = feedStore.getFeed(feedId)
    suspend fun getFeed(url: URL): Feed? = feedStore.getFeed(url)

    suspend fun saveFeed(feed: Feed): Long = feedStore.saveFeed(feed)

    suspend fun setPinned(itemId: Long, pinned: Boolean) = feedItemStore.setPinned(itemId = itemId, pinned = pinned)
    suspend fun setBookmarked(itemId: Long, bookmarked: Boolean) = feedItemStore.setBookmarked(itemId = itemId, bookmarked = bookmarked)
    suspend fun markAsNotified(itemIds: List<Long>) = feedItemStore.markAsNotified(itemIds)
    suspend fun markAsReadAndNotified(itemId: Long) = feedItemStore.markAsReadAndNotified(itemId)
    suspend fun markAsUnread(itemId: Long, unread: Boolean = true) =
        feedItemStore.markAsUnread(itemId, unread)

    suspend fun getTextToDisplayForItem(itemId: Long): TextToDisplay =
        when (feedItemStore.getFullTextByDefault(itemId)) {
            true -> TextToDisplay.FULLTEXT
            false -> TextToDisplay.DEFAULT
        }

    suspend fun getLink(itemId: Long): String? = feedItemStore.getLink(itemId)
    suspend fun getArticleOpener(itemId: Long): ItemOpener =
        when (feedItemStore.getArticleOpener(itemId)) {
            PREF_VAL_OPEN_WITH_BROWSER -> ItemOpener.DEFAULT_BROWSER
            PREF_VAL_OPEN_WITH_CUSTOM_TAB -> ItemOpener.CUSTOM_TAB
            PREF_VAL_OPEN_WITH_READER -> ItemOpener.READER
            else -> itemOpener.value // Global default
        }

    suspend fun getDisplayTitleForFeed(feedId: Long): String? =
        feedStore.getDisplayTitle(feedId)

    fun getScreenTitleForFeedOrTag(feedId: Long, tag: String) = flow {
        emit(
            ScreenTitle(
                title = when {
                    feedId > ID_UNSET -> feedStore.getDisplayTitle(feedId)
                    tag.isNotBlank() -> tag
                    else -> null
                }
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getScreenTitleForCurrentFeedOrTag(): Flow<ScreenTitle> =
        currentFeedAndTag.mapLatest { (feedId, tag) ->
            ScreenTitle(
                title = when {
                    feedId > ID_UNSET -> feedStore.getDisplayTitle(feedId)
                    tag.isNotBlank() -> tag
                    else -> null
                }
            )
        }

    suspend fun deleteFeeds(feedIds: List<Long>) {
        feedStore.deleteFeeds(feedIds)
        androidSystemStore.removeDynamicShortcuts(feedIds)
    }

    suspend fun markAllAsReadInFeedOrTag(feedId: Long, tag: String) {
        when {
            feedId > ID_UNSET -> feedItemStore.markAllAsReadInFeed(feedId)
            tag.isNotBlank() -> feedItemStore.markAllAsReadInTag(tag)
            else -> feedItemStore.markAllAsRead()
        }
    }

    suspend fun markBeforeAsRead(itemIndex: Int, feedId: Long, tag: String) {
        feedItemStore.markBeforeAsRead(
            index = itemIndex,
            feedId = feedId,
            tag = tag,
            onlyUnread = showOnlyUnread.value,
            newestFirst = SortingOptions.NEWEST_FIRST == currentSorting.value,
        )
    }

    suspend fun markAfterAsRead(itemIndex: Int, feedId: Long, tag: String) {
        feedItemStore.markAfterAsRead(
            index = itemIndex,
            feedId = feedId,
            tag = tag,
            onlyUnread = showOnlyUnread.value,
            newestFirst = SortingOptions.NEWEST_FIRST == currentSorting.value,
        )
    }

    val allTags: Flow<List<String>> = feedStore.allTags

    val drawerItemsWithUnreadCounts: Flow<List<DrawerItemWithUnreadCount>> =
        feedStore.drawerItemsWithUnreadCounts

    fun getVisibleFeedTitles(feedId: Long, tag: String): Flow<List<FeedTitle>> =
        feedStore.getFeedTitles(feedId, tag).buffer(1)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentlyVisibleFeedTitles(): Flow<List<FeedTitle>> =
        currentFeedAndTag.flatMapLatest { (feedId, tag) ->
            feedStore.getFeedTitles(feedId, tag)
        }

    val expandedTags: StateFlow<Set<String>> = sessionStore.expandedTags

    fun toggleTagExpansion(tag: String) = sessionStore.toggleTagExpansion(tag)

    suspend fun ensurePeriodicSyncConfigured() =
        settingsStore.configurePeriodicSync(replace = false)

    fun getFeedsItemsWithDefaultFullTextParse(): Flow<List<FeedItemIdWithLink>> =
        feedItemStore.getFeedsItemsWithDefaultFullTextParse()

    fun getFeedItemsNeedingNotifying(): Flow<List<Long>> {
        return feedItemStore.getFeedItemsNeedingNotifying()
    }

    suspend fun remoteMarkAsRead(feedUrl: URL, articleGuid: String) {
        // First try to get an existing ID
        val itemId = feedItemStore.getFeedItemId(feedUrl = feedUrl, articleGuid = articleGuid)
        if (itemId != null) {
            syncRemoteStore.setSynced(itemId)
            feedItemStore.markAsReadAndNotified(itemId = itemId)
        } else {
            syncRemoteStore.addRemoteReadMark(feedUrl = feedUrl, articleGuid = articleGuid)
        }
    }

    fun getSyncRemoteFlow(): Flow<SyncRemote?> {
        return syncRemoteStore.getSyncRemoteFlow()
    }

    suspend fun getSyncRemote(): SyncRemote {
        return syncRemoteStore.getSyncRemote()
    }

    suspend fun updateSyncRemote(syncRemote: SyncRemote) {
        syncRemoteStore.updateSyncRemote(syncRemote)
    }

    suspend fun updateSyncRemoteMessageTimestamp(timestamp: Instant) {
        syncRemoteStore.updateSyncRemoteMessageTimestamp(timestamp)
    }

    suspend fun deleteAllReadStatusSyncs() {
        syncRemoteStore.deleteAllReadStatusSyncs()
    }

    fun getNextFeedItemWithoutSyncedReadMark(): Flow<FeedItemForReadMark?> {
        return syncRemoteStore.getNextFeedItemWithoutSyncedReadMark()
    }

    fun getFlowOfFeedItemsWithoutSyncedReadMark(): Flow<List<FeedItemForReadMark>> {
        return syncRemoteStore.getFlowOfFeedItemsWithoutSyncedReadMark()
    }

    suspend fun getFeedItemsWithoutSyncedReadMark(): List<FeedItemForReadMark> {
        return syncRemoteStore.getFeedItemsWithoutSyncedReadMark()
    }

    suspend fun setSynced(feedItemId: Long) {
        syncRemoteStore.setSynced(feedItemId)
    }

    suspend fun upsertFeed(feedSql: Feed) =
        feedStore.upsertFeed(feedSql)

    suspend fun loadFeedItem(guid: String, feedId: Long): FeedItem? =
        feedItemStore.loadFeedItem(guid = guid, feedId = feedId)

    suspend fun upsertFeedItems(
        itemsWithText: List<Pair<FeedItem, String>>,
        block: suspend (FeedItem, String) -> Unit
    ) {
        feedItemStore.upsertFeedItems(itemsWithText, block)
    }

    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, keepCount: Int) =
        feedItemStore.getItemsToBeCleanedFromFeed(feedId = feedId, keepCount = keepCount)

    suspend fun deleteFeedItems(ids: List<Long>) {
        feedItemStore.deleteFeedItems(ids)
    }

    suspend fun deleteStaleRemoteReadMarks() {
        syncRemoteStore.deleteStaleRemoteReadMarks(Instant.now())
    }

    suspend fun applyRemoteReadMarks() {
        val toBeApplied = syncRemoteStore.getRemoteReadMarksReadyToBeApplied()
        val itemIds = toBeApplied.map { it.feedItemId }
        feedItemStore.markAsRead(itemIds)
        for (itemId in itemIds) {
            syncRemoteStore.setSynced(itemId)
        }
        syncRemoteStore.deleteReadStatusSyncs(toBeApplied.map { it.id })
    }

    suspend fun replaceWithDefaultSyncRemote() {
        syncRemoteStore.replaceWithDefaultSyncRemote()
    }

    fun getDevices(): Flow<List<SyncDevice>> {
        return syncRemoteStore.getDevices()
    }

    suspend fun replaceDevices(devices: List<SyncDevice>) {
        syncRemoteStore.replaceDevices(devices)
    }

    suspend fun getFeedsOrderedByUrl(): List<Feed> {
        return feedStore.getFeedsOrderedByUrl()
    }

    fun getFlowOfFeedsOrderedByUrl(): Flow<List<Feed>> {
        return feedStore.getFlowOfFeedsOrderedByUrl()
    }

    suspend fun getRemotelySeenFeeds(): List<URL> {
        return syncRemoteStore.getRemotelySeenFeeds()
    }

    suspend fun deleteFeed(url: URL) {
        feedStore.deleteFeed(url)
    }

    suspend fun replaceRemoteFeedsWith(remoteFeeds: List<RemoteFeed>) {
        syncRemoteStore.replaceRemoteFeedsWith(remoteFeeds)
    }
}

private data class FeedListArgs(
    val feedId: Long,
    val tag: String,
    val newestFirst: Boolean,
    val onlyUnread: Boolean,
    val onlyBookmarks: Boolean,
)

// Wrapper class because flow combine doesn't like nulls
@Immutable
data class ScreenTitle(
    val title: String?,
)

@Immutable
data class Enclosure(
    val present: Boolean = false,
    val link: String = "",
    val name: String = "",
)

@Immutable
data class Article(
    val item: FeedItemWithFeed?
) {
    val id: Long = item?.id ?: ID_UNSET
    val link: String? = item?.link
    val feedDisplayTitle: String = item?.feedDisplayTitle ?: ""
    val title: String = item?.plainTitle ?: ""
    val enclosure: Enclosure = item?.enclosureLink?.let { link ->
        Enclosure(
            present = true,
            link = link,
            name = item.enclosureFilename ?: ""
        )
    } ?: Enclosure(
        present = false,
    )
    val author: String? = item?.author
    val pubDate: ZonedDateTime? = item?.pubDate
    val feedId: Long = item?.feedId ?: ID_UNSET
    val feedUrl: String? = item?.feedUrl?.toString()
    val pinned: Boolean = item?.pinned ?: false
    val bookmarked: Boolean = item?.bookmarked ?: false
}

enum class TextToDisplay {
    DEFAULT,
    LOADING_FULLTEXT,
    FAILED_TO_LOAD_FULLTEXT,
    FULLTEXT,
}
