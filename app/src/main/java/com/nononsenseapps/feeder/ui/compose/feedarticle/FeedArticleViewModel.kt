package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.Article
import com.nononsenseapps.feeder.archmodel.Enclosure
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.archmodel.FeedType
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.archmodel.ScreenTitle
import com.nononsenseapps.feeder.archmodel.SwipeAsRead
import com.nononsenseapps.feeder.archmodel.TextToDisplay
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.db.room.FeedItemCursor
import com.nononsenseapps.feeder.db.room.FeedItemForFetching
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FullTextParser
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.model.NoBody
import com.nononsenseapps.feeder.model.NoUrl
import com.nononsenseapps.feeder.model.NotHTML
import com.nononsenseapps.feeder.model.PlaybackStatus
import com.nononsenseapps.feeder.model.TTSStateHolder
import com.nononsenseapps.feeder.model.UnsupportedContentType
import com.nononsenseapps.feeder.model.workmanager.requestFeedSync
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import com.nononsenseapps.feeder.ui.compose.feed.FeedOrTag
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerItemWithUnreadCount
import com.nononsenseapps.feeder.ui.compose.text.htmlToAnnotatedString
import com.nononsenseapps.feeder.util.Either
import com.nononsenseapps.feeder.util.FilePathProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.FileNotFoundException
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale

class FeedArticleViewModel(
    di: DI,
    private val state: SavedStateHandle,
) : DIAwareViewModel(di), FeedListFilterCallback {
    private val repository: Repository by instance()
    private val ttsStateHolder: TTSStateHolder by instance()
    private val fullTextParser: FullTextParser by instance()
    private val filePathProvider: FilePathProvider by instance()

    // Use this for actions which should complete even if app goes off screen
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()

    val currentFeedListItems: Flow<PagingData<FeedListItem>> =
        repository.getCurrentFeedListItems()
            .cachedIn(viewModelScope)

    private val visibleFeedItemCount: StateFlow<Int> =
        repository.getCurrentFeedListVisibleItemCount()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                1, // So we display an empty screen before data is loaded (less flicker)
            )

    private val screenTitleForCurrentFeedOrTag: StateFlow<ScreenTitle> =
        repository.getScreenTitleForCurrentFeedOrTag()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                ScreenTitle("", FeedType.ALL_FEEDS),
            )

    private val visibleFeeds: StateFlow<List<FeedTitle>> =
        repository.getCurrentlyVisibleFeedTitles()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList(),
            )

    fun deleteFeeds(feedIds: List<Long>) =
        applicationCoroutineScope.launch {
            repository.deleteFeeds(feedIds)
        }

    fun markAllAsRead() =
        applicationCoroutineScope.launch {
            val (feedId, feedTag) = repository.currentFeedAndTag.value
            repository.markAllAsReadInFeedOrTag(feedId, feedTag)
        }

    fun markAsUnread(itemId: Long) =
        applicationCoroutineScope.launch {
            repository.markAsUnread(itemId)
        }

    fun markAsRead(
        itemId: Long,
        feedOrTag: FeedOrTag?,
    ) = applicationCoroutineScope.launch {
        val (feedId, tag) = repository.currentFeedAndTag.value
        // Ensure mark as read on scroll doesn't fire when navigating between feeds
        if (feedOrTag == null || feedId == feedOrTag.id && tag == feedOrTag.tag) {
            repository.markAsReadAndNotified(itemId)
        }
    }

    fun markAsReadOnSwipe(itemId: Long) =
        applicationCoroutineScope.launch {
            repository.markAsReadAndNotified(itemId = itemId, readTimeBeforeMinReadTime = true)
        }

    fun markBeforeAsRead(cursor: FeedItemCursor) =
        applicationCoroutineScope.launch {
            val (feedId, feedTag) = repository.currentFeedAndTag.value
            repository.markBeforeAsRead(cursor, feedId, feedTag)
        }

    fun markAfterAsRead(cursor: FeedItemCursor) =
        applicationCoroutineScope.launch {
            val (feedId, feedTag) = repository.currentFeedAndTag.value
            repository.markAfterAsRead(cursor, feedId, feedTag)
        }

    fun setBookmarked(
        itemId: Long,
        bookmarked: Boolean,
    ) = applicationCoroutineScope.launch {
        repository.setBookmarked(itemId, bookmarked)
    }

    fun requestImmediateSyncOfCurrentFeedOrTag() {
        val (feedId, feedTag) = repository.currentFeedAndTag.value
        requestFeedSync(
            di = di,
            feedId = feedId,
            feedTag = feedTag,
            forceNetwork = true,
        )
    }

    fun requestImmediateSyncOfAll() {
        requestFeedSync(
            di = di,
            forceNetwork = true,
        )
    }

    private val toolbarVisible: MutableStateFlow<Boolean> =
        MutableStateFlow(state["toolbarMenuVisible"] ?: false)

    fun setToolbarMenuVisible(visible: Boolean) {
        state["toolbarMenuVisible"] = visible
        toolbarVisible.update { visible }
    }

    private val filterMenuVisible: MutableStateFlow<Boolean> =
        MutableStateFlow(state["filterMenuVisible"] ?: false)

    fun setFilterMenuVisible(visible: Boolean) {
        state["filterMenuVisible"] = visible
        filterMenuVisible.update { visible }
    }

    val filterCallback: FeedListFilterCallback = this

    fun toggleTagExpansion(tag: String) = repository.toggleTagExpansion(tag)

    private val editDialogVisible = MutableStateFlow(false)

    fun setShowEditDialog(visible: Boolean) {
        editDialogVisible.update { visible }
    }

    private val deleteDialogVisible = MutableStateFlow(false)

    fun setShowDeleteDialog(visible: Boolean) {
        deleteDialogVisible.update { visible }
    }

    /**
     * This determines if the main screen is article or list on small screens - it does not impact
     * visibility of article on large landscape screens
     */
    fun setArticleOpen(value: Boolean) {
        repository.setIsArticleOpen(value)
    }

    suspend fun setCurrentArticle(itemId: Long) {
        repository.setCurrentArticle(itemId)

        // Now wait until article has been loaded until opening the article view
        // This is so the reader doesn't open with the previous article briefly visible
        // until the new one has loaded

        // Naturally, don't let infinite loops be possible even though it shouldn't be infinite
        withTimeout(100_000L) {
            while (viewState.value.articleId != itemId) {
                delay(10)
            }
        }

        setArticleOpen(true)
    }

    fun openArticle(
        itemId: Long,
        openInCustomTab: (String) -> Unit,
        openInBrowser: (String) -> Unit,
        navigateToArticle: () -> Unit,
    ) = viewModelScope.launch {
        val itemOpener = repository.getArticleOpener(itemId = itemId)
        val articleLink = repository.getLink(itemId)
        when {
            ItemOpener.CUSTOM_TAB == itemOpener && articleLink != null -> {
                openInCustomTab(articleLink)
            }

            ItemOpener.DEFAULT_BROWSER == itemOpener && articleLink != null -> {
                openInBrowser(articleLink)
            }

            else -> {
                setCurrentArticle(itemId)
                navigateToArticle()
            }
        }
        markAsRead(itemId, null)
    }

    // Used to trigger state update
    private val textToDisplayTrigger: MutableStateFlow<Int> = MutableStateFlow(0)

    private suspend fun getTextToDisplayFor(itemId: Long): TextToDisplay =
        state["textToDisplayFor$itemId"]
            ?: repository.getTextToDisplayForItem(itemId)

    // Only affect the state by design, settings is done in EditFeed
    private fun setTextToDisplayFor(
        itemId: Long,
        value: TextToDisplay,
    ) {
        state["textToDisplayFor$itemId"] = value
        textToDisplayTrigger.update {
            textToDisplayTrigger.value + 1
        }
    }

    val viewState: StateFlow<FeedArticleScreenViewState> =
        combine(
            repository.showFab,
            repository.showThumbnails,
            repository.currentTheme,
            repository.currentlySyncingLatestTimestamp,
            repository.drawerItemsWithUnreadCounts,
            repository.feedItemStyle,
            repository.expandedTags,
            toolbarVisible,
            visibleFeedItemCount,
            screenTitleForCurrentFeedOrTag,
            editDialogVisible,
            deleteDialogVisible,
            visibleFeeds,
            repository.isArticleOpen,
            repository.linkOpener,
            repository.currentFeedAndTag.map { (feedId, tag) -> FeedOrTag(feedId, tag) },
            repository.currentArticle,
            ttsStateHolder.ttsState,
            repository.swipeAsRead,
            textToDisplayTrigger, // Never actually read, only used as trigger
            repository.useDetectLanguage,
            ttsStateHolder.availableLanguages,
            repository.getUnreadBookmarksCount,
            repository.isMarkAsReadOnScroll,
            repository.maxLines,
            filterMenuVisible,
            repository.feedListFilter,
            repository.showOnlyTitle,
            repository.showReadingTime,
        ) { params: Array<Any> ->
            val article = params[16] as Article

            val ttsState = params[17] as PlaybackStatus

            val haveVisibleFeedItems = (params[8] as Int) > 0

            val currentFeedOrTag = params[15] as FeedOrTag

            val textToDisplay = getTextToDisplayFor(article.id)

            @Suppress("UNCHECKED_CAST")
            FeedArticleScreenViewState(
                showFab = haveVisibleFeedItems && (params[0] as Boolean),
                showThumbnails = params[1] as Boolean,
                currentTheme = params[2] as ThemeOptions,
                latestSyncTimestamp = params[3] as Instant,
                drawerItemsWithUnreadCounts = params[4] as List<DrawerItemWithUnreadCount>,
                feedItemStyle = params[5] as FeedItemStyle,
                expandedTags = params[6] as Set<String>,
                showToolbarMenu = params[7] as Boolean,
                haveVisibleFeedItems = haveVisibleFeedItems,
                feedScreenTitle = params[9] as ScreenTitle,
                showEditDialog = params[10] as Boolean,
                showDeleteDialog = params[11] as Boolean,
                visibleFeeds = params[12] as List<FeedTitle>,
                articleFeedUrl = article.feedUrl,
                articleFeedId = article.feedId,
                linkOpener = params[14] as LinkOpener,
                pubDate = article.pubDate,
                author = article.author,
                enclosure = article.enclosure,
                articleTitle = article.title,
                feedDisplayTitle = article.feedDisplayTitle,
                currentFeedOrTag = currentFeedOrTag,
                articleLink = article.link,
                textToDisplay = textToDisplay,
                isTTSPlaying = ttsState == PlaybackStatus.PLAYING,
                isBottomBarVisible = ttsState != PlaybackStatus.STOPPED,
                articleId = article.id,
                isArticleOpen = params[13] as Boolean,
                swipeAsRead = params[18] as SwipeAsRead,
                isBookmarked = article.bookmarked,
                useDetectLanguage = params[20] as Boolean,
                ttsLanguages = params[21] as List<Locale>,
                unreadBookmarksCount = params[22] as Int,
                markAsReadOnScroll = params[23] as Boolean,
                maxLines = params[24] as Int,
                showFilterMenu = params[25] as Boolean,
                filter = params[26] as FeedListFilter,
                showOnlyTitle = params[27] as Boolean,
                showReadingTime = params[28] as Boolean,
                wordCount =
                    when (textToDisplay) {
                        TextToDisplay.DEFAULT -> article.wordCount

                        TextToDisplay.FULLTEXT,
                        TextToDisplay.LOADING_FULLTEXT,
                        -> article.wordCountFull

                        TextToDisplay.FAILED_TO_LOAD_FULLTEXT,
                        TextToDisplay.FAILED_MISSING_BODY,
                        TextToDisplay.FAILED_MISSING_LINK,
                        TextToDisplay.FAILED_NOT_HTML,
                        -> 0
                    },
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                FeedArticleScreenViewState(),
            )

    fun displayArticleText() {
        setTextToDisplayFor(viewState.value.articleId, TextToDisplay.DEFAULT)
    }

    fun displayFullText() {
        val itemId = viewState.value.articleId
        viewModelScope.launch {
            loadFullTextThenDisplayIt(itemId)
        }
    }

    private suspend fun loadFullTextThenDisplayIt(itemId: Long) {
        if (blobFullFile(viewState.value.articleId, filePathProvider.fullArticleDir).isFile) {
            setTextToDisplayFor(itemId, TextToDisplay.FULLTEXT)
            return
        }

        setTextToDisplayFor(itemId, TextToDisplay.LOADING_FULLTEXT)
        val link = viewState.value.articleLink
        val result =
            fullTextParser.parseFullArticleIfMissing(
                object : FeedItemForFetching {
                    override val id = viewState.value.articleId
                    override val link = link
                },
            )

        setTextToDisplayFor(
            itemId,
            when {
                result.isRight() -> TextToDisplay.FULLTEXT
                else -> {
                    when (result.leftOrNull()) {
                        is NoBody -> TextToDisplay.FAILED_MISSING_BODY
                        is NoUrl -> TextToDisplay.FAILED_MISSING_LINK
                        is UnsupportedContentType -> TextToDisplay.FAILED_NOT_HTML
                        is NotHTML -> TextToDisplay.FAILED_NOT_HTML
                        else -> TextToDisplay.FAILED_TO_LOAD_FULLTEXT
                    }
                }
            },
        )
    }

    fun ttsStop() {
        ttsStateHolder.stop()
    }

    fun ttsPause() {
        ttsStateHolder.pause()
    }

    fun ttsSkipNext() {
        ttsStateHolder.skipNext()
    }

    fun ttsOnSelectLanguage(lang: LocaleOverride) {
        ttsStateHolder.setLanguage(lang)
    }

    fun ttsPlay() {
        viewModelScope.launch(Dispatchers.IO) {
            val fullText =
                when (viewState.value.textToDisplay) {
                    TextToDisplay.DEFAULT ->
                        Either.catching(
                            onCatch = {
                                when (it) {
                                    is FileNotFoundException -> TTSFileNotFound
                                    else -> TTSUnknownError
                                }
                            },
                        ) {
                            blobInputStream(viewState.value.articleId, filePathProvider.articleDir).use {
                                htmlToAnnotatedString(
                                    inputStream = it,
                                    baseUrl = viewState.value.articleFeedUrl ?: "",
                                )
                            }
                        }

                    TextToDisplay.FULLTEXT ->
                        Either.catching(
                            onCatch = {
                                when (it) {
                                    is FileNotFoundException -> TTSFileNotFound
                                    else -> TTSUnknownError
                                }
                            },
                        ) {
                            blobFullInputStream(
                                viewState.value.articleId,
                                filePathProvider.fullArticleDir,
                            ).use {
                                htmlToAnnotatedString(
                                    inputStream = it,
                                    baseUrl = viewState.value.articleFeedUrl ?: "",
                                )
                            }
                        }

                    TextToDisplay.LOADING_FULLTEXT,
                    TextToDisplay.FAILED_TO_LOAD_FULLTEXT,
                    TextToDisplay.FAILED_MISSING_BODY,
                    TextToDisplay.FAILED_MISSING_LINK,
                    TextToDisplay.FAILED_NOT_HTML,
                    -> Either.Left(TTSUnknownError)
                }

            // TODO show error some message
            fullText.onRight {
                ttsStateHolder.tts(
                    textArray = it,
                    useDetectLanguage = viewState.value.useDetectLanguage,
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsStateHolder.shutdown()
    }

    override fun setSaved(value: Boolean) {
        repository.setFeedListFilterSaved(value)
    }

    override fun setRecentlyRead(value: Boolean) {
        repository.setFeedListFilterRecentlyRead(value)
    }

    override fun setRead(value: Boolean) {
        repository.setFeedListFilterRead(value)
    }
}

interface FeedScreenViewState {
    val currentFeedOrTag: FeedOrTag
    val showFab: Boolean
    val showThumbnails: Boolean
    val currentTheme: ThemeOptions
    val latestSyncTimestamp: Instant
    val feedScreenTitle: ScreenTitle
    val visibleFeeds: List<FeedTitle>
    val drawerItemsWithUnreadCounts: List<DrawerItemWithUnreadCount>
    val unreadBookmarksCount: Int
    val feedItemStyle: FeedItemStyle
    val expandedTags: Set<String>
    val isBottomBarVisible: Boolean
    val isTTSPlaying: Boolean
    val ttsLanguages: List<Locale>
    val showToolbarMenu: Boolean
    val showDeleteDialog: Boolean
    val showEditDialog: Boolean
    val haveVisibleFeedItems: Boolean
    val swipeAsRead: SwipeAsRead
    val markAsReadOnScroll: Boolean
    val maxLines: Int
    val showOnlyTitle: Boolean
    val showReadingTime: Boolean
    val filter: FeedListFilter
    val showFilterMenu: Boolean
}

interface ArticleScreenViewState {
    val useDetectLanguage: Boolean
    val isBottomBarVisible: Boolean
    val isTTSPlaying: Boolean
    val ttsLanguages: List<Locale>
    val articleFeedUrl: String?
    val articleId: Long
    val articleLink: String?
    val articleFeedId: Long
    val textToDisplay: TextToDisplay
    val linkOpener: LinkOpener
    val pubDate: ZonedDateTime?
    val author: String?
    val enclosure: Enclosure
    val articleTitle: String
    val showToolbarMenu: Boolean
    val feedDisplayTitle: String
    val isBookmarked: Boolean
    val keyHolder: ArticleItemKeyHolder
    val wordCount: Int
}

interface ArticleItemKeyHolder {
    fun getAndIncrementKey(): Long
}

interface FeedListFilter {
    val unread: Boolean
    val saved: Boolean
    val recentlyRead: Boolean
    val read: Boolean
}

val emptyFeedListFilter =
    object : FeedListFilter {
        override val unread: Boolean = true
        override val saved: Boolean = false
        override val recentlyRead: Boolean = false
        override val read: Boolean = false
    }

interface FeedListFilterCallback {
    fun setSaved(value: Boolean)

    fun setRecentlyRead(value: Boolean)

    fun setRead(value: Boolean)
}

val FeedListFilter.onlyUnread: Boolean
    get() = unread && !saved && !recentlyRead && !read

val FeedListFilter.onlyUnreadAndSaved: Boolean
    get() = unread && saved && !recentlyRead && !read

object RotatingArticleItemKeyHolder : ArticleItemKeyHolder {
    private var key: Long = 0L

    override fun getAndIncrementKey(): Long {
        return key++
    }
}

@Immutable
data class FeedArticleScreenViewState(
    override val currentFeedOrTag: FeedOrTag = FeedOrTag(ID_UNSET, ""),
    override val showFab: Boolean = true,
    override val showThumbnails: Boolean = true,
    override val currentTheme: ThemeOptions = ThemeOptions.SYSTEM,
    override val latestSyncTimestamp: Instant = Instant.EPOCH,
    // Defaults to empty string to avoid rendering until loading complete
    override val feedScreenTitle: ScreenTitle = ScreenTitle("", FeedType.FEED),
    override val visibleFeeds: List<FeedTitle> = emptyList(),
    override val drawerItemsWithUnreadCounts: List<DrawerItemWithUnreadCount> = emptyList(),
    override val unreadBookmarksCount: Int = 0,
    override val feedItemStyle: FeedItemStyle = FeedItemStyle.CARD,
    override val expandedTags: Set<String> = emptySet(),
    override val isBottomBarVisible: Boolean = false,
    override val isTTSPlaying: Boolean = false,
    override val ttsLanguages: List<Locale> = emptyList(),
    override val showToolbarMenu: Boolean = false,
    override val showDeleteDialog: Boolean = false,
    override val showEditDialog: Boolean = false,
    // Defaults to true so empty screen doesn't appear before load
    override val haveVisibleFeedItems: Boolean = true,
    override val articleFeedUrl: String? = null,
    override val articleFeedId: Long = ID_UNSET,
    override val textToDisplay: TextToDisplay = TextToDisplay.DEFAULT,
    override val linkOpener: LinkOpener = LinkOpener.CUSTOM_TAB,
    override val pubDate: ZonedDateTime? = null,
    override val author: String? = null,
    override val enclosure: Enclosure = Enclosure(),
    override val articleTitle: String = "",
    override val articleLink: String? = null,
    override val feedDisplayTitle: String = "",
    override val articleId: Long = ID_UNSET,
    override val swipeAsRead: SwipeAsRead = SwipeAsRead.ONLY_FROM_END,
    override val isBookmarked: Boolean = false,
    override val useDetectLanguage: Boolean = false,
    override val markAsReadOnScroll: Boolean = false,
    override val keyHolder: ArticleItemKeyHolder = RotatingArticleItemKeyHolder,
    override val maxLines: Int = 2,
    override val showOnlyTitle: Boolean = false,
    override val showReadingTime: Boolean = false,
    override val showFilterMenu: Boolean = false,
    override val filter: FeedListFilter = emptyFeedListFilter,
    val isArticleOpen: Boolean = false,
    override val wordCount: Int = 0,
) : FeedScreenViewState, ArticleScreenViewState

sealed class TSSError

object TTSFileNotFound : TSSError()

object TTSUnknownError : TSSError()
