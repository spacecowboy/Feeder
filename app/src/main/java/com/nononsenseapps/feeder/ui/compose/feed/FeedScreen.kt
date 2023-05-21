package com.nononsenseapps.feeder.ui.compose.feed

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconToggleButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.model.opml.exportOpml
import com.nononsenseapps.feeder.model.opml.importOpml
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.empty.NothingToRead
import com.nononsenseapps.feeder.ui.compose.feedarticle.FeedArticleScreenViewState
import com.nononsenseapps.feeder.ui.compose.feedarticle.FeedArticleViewModel
import com.nononsenseapps.feeder.ui.compose.feedarticle.FeedScreenViewState
import com.nononsenseapps.feeder.ui.compose.material3.DrawerState
import com.nononsenseapps.feeder.ui.compose.material3.DrawerValue
import com.nononsenseapps.feeder.ui.compose.material3.rememberDrawerState
import com.nononsenseapps.feeder.ui.compose.navdrawer.ScreenWithNavDrawer
import com.nononsenseapps.feeder.ui.compose.navigation.ArticleDestination
import com.nononsenseapps.feeder.ui.compose.navigation.EditFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.FeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SearchFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SettingsDestination
import com.nononsenseapps.feeder.ui.compose.pullrefresh.PullRefreshIndicator
import com.nononsenseapps.feeder.ui.compose.pullrefresh.pullRefresh
import com.nononsenseapps.feeder.ui.compose.pullrefresh.rememberPullRefreshState
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableTTSPlayer
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.SensibleTopAppBar
import com.nononsenseapps.feeder.ui.compose.theme.SetStatusBarColorToMatchScrollableTopAppBar
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.LocalWindowSize
import com.nononsenseapps.feeder.ui.compose.utils.WindowSize
import com.nononsenseapps.feeder.ui.compose.utils.addMargin
import com.nononsenseapps.feeder.ui.compose.utils.addMarginLayout
import com.nononsenseapps.feeder.util.emailBugReportIntent
import com.nononsenseapps.feeder.util.logDebug
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime

private const val LOG_TAG = "FEEDER_FEEDSCREEN"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedArticleViewModel,
) {
    val viewState: FeedArticleScreenViewState by viewModel.viewState.collectAsStateWithLifecycle()
    val pagedFeedItems = viewModel.currentFeedListItems.collectAsLazyPagingItems()

    val di = LocalDI.current
    val opmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/xml"),
    ) { uri ->
        if (uri != null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                exportOpml(di, uri)
            }
        }
    }
    val opmlImporter = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                importOpml(di, uri)
            }
        }
    }

    val context = LocalContext.current

    // Each feed gets its own scroll state. Persists across device rotations, but is cleared when
    // switching feeds
    val feedListState = key(viewState.currentFeedOrTag) {
        pagedFeedItems.rememberLazyListState()
    }

    val feedGridState = key(viewState.currentFeedOrTag) {
        rememberLazyStaggeredGridState()
    }

    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler(
        enabled = drawerState.isOpen,
        onBack = {
            if (drawerState.isOpen) {
                coroutineScope.launch {
                    drawerState.close()
                }
            }
        },
    )

    ScreenWithNavDrawer(
        feedsAndTags = ImmutableHolder(viewState.drawerItemsWithUnreadCounts),
        expandedTags = ImmutableHolder(viewState.expandedTags),
        onToggleTagExpansion = { tag ->
            viewModel.toggleTagExpansion(tag)
        },
        onDrawerItemSelected = { feedId, tag ->
            FeedDestination.navigate(navController, feedId = feedId, tag = tag)
        },
        drawerState = drawerState,
    ) {
        FeedScreen(
            viewState = viewState,
            drawerState = drawerState,
            onRefreshVisible = {
                viewModel.requestImmediateSyncOfCurrentFeedOrTag()
            },
            onRefreshAll = {
                viewModel.requestImmediateSyncOfAll()
                coroutineScope.launch {
                    if (feedListState.firstVisibleItemIndex != 0) {
                        feedListState.animateScrollToItem(0)
                    }
                    if (feedGridState.firstVisibleItemIndex != 0) {
                        feedGridState.animateScrollToItem(0)
                    }
                }
            },
            onToggleOnlyUnread = { value ->
                viewModel.setShowOnlyUnread(value)
            },
            onToggleOnlyBookmarked = { value ->
                viewModel.setShowOnlyBookmarked(value)
            },
            onMarkAllAsRead = {
                viewModel.markAllAsRead()
            },
            onShowToolbarMenu = { visible ->
                viewModel.setToolbarMenuVisible(visible)
            },
            ttsOnPlay = viewModel::ttsPlay,
            ttsOnPause = viewModel::ttsPause,
            ttsOnStop = viewModel::ttsStop,
            ttsOnSkipNext = viewModel::ttsSkipNext,
            ttsOnSelectLanguage = viewModel::ttsOnSelectLanguage,
            onAddFeed = { SearchFeedDestination.navigate(navController) },
            onEditFeed = { feedId ->
                EditFeedDestination.navigate(navController, feedId)
            },
            onShowEditDialog = {
                viewModel.setShowEditDialog(true)
            },
            onDismissEditDialog = {
                viewModel.setShowEditDialog(false)
            },
            onDeleteFeeds = { feedIds ->
                viewModel.deleteFeeds(feedIds.toList())
            },
            onShowDeleteDialog = {
                viewModel.setShowDeleteDialog(true)
            },
            onDismissDeleteDialog = {
                viewModel.setShowDeleteDialog(false)
            },
            onSettings = {
                SettingsDestination.navigate(navController)
            },
            onSendFeedback = {
                context.startActivity(emailBugReportIntent())
            },
            onImport = {
                opmlImporter.launch(
                    arrayOf(
                        "text/plain",
                        "text/xml",
                        "text/opml",
                        "*/*",
                    ),
                )
            },
            onExport = { opmlExporter.launch("feeder-export-${LocalDateTime.now()}.opml") },
            markAsUnread = { itemId, unread ->
                if (unread) {
                    viewModel.markAsUnread(itemId)
                } else {
                    viewModel.markAsRead(itemId)
                }
            },
            markBeforeAsRead = { index ->
                viewModel.markBeforeAsRead(index)
            },
            markAfterAsRead = { index ->
                viewModel.markAfterAsRead(index)
            },
            onOpenFeedItem = { itemId ->
                viewModel.openArticle(
                    itemId = itemId,
                    openInBrowser = { articleLink ->
                        openLinkInBrowser(context, articleLink)
                    },
                    openInCustomTab = { articleLink ->
                        openLinkInCustomTab(context, articleLink, toolbarColor)
                    },
                    navigateToArticle = {
                        ArticleDestination.navigate(navController, itemId)
                    },
                )
            },
            onSetPinned = { itemId, value ->
                viewModel.setPinned(itemId, value)
            },
            onSetBookmarked = { itemId, value ->
                viewModel.setBookmarked(itemId, value)
            },
            feedListState = feedListState,
            feedGridState = feedGridState,
            pagedFeedItems = pagedFeedItems,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    viewState: FeedScreenViewState,
    onRefreshVisible: () -> Unit,
    onRefreshAll: () -> Unit,
    onToggleOnlyUnread: (Boolean) -> Unit,
    onToggleOnlyBookmarked: (Boolean) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onShowToolbarMenu: (Boolean) -> Unit,
    ttsOnPlay: () -> Unit,
    ttsOnPause: () -> Unit,
    ttsOnStop: () -> Unit,
    ttsOnSkipNext: () -> Unit,
    ttsOnSelectLanguage: (LocaleOverride) -> Unit,
    onAddFeed: () -> Unit,
    onEditFeed: (Long) -> Unit,
    onShowEditDialog: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onDeleteFeeds: (Iterable<Long>) -> Unit,
    onShowDeleteDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    drawerState: DrawerState,
    markAsUnread: (Long, Boolean) -> Unit,
    markBeforeAsRead: (Int) -> Unit,
    markAfterAsRead: (Int) -> Unit,
    onOpenFeedItem: (Long) -> Unit,
    onSetPinned: (Long, Boolean) -> Unit,
    onSetBookmarked: (Long, Boolean) -> Unit,
    feedListState: LazyListState,
    feedGridState: LazyStaggeredGridState,
    pagedFeedItems: LazyPagingItems<FeedListItem>,
    modifier: Modifier = Modifier,
) {
    val showingUnreadStateLabel = if (viewState.onlyUnread) {
        stringResource(R.string.showing_only_unread_articles)
    } else {
        stringResource(R.string.showing_all_articles)
    }

    val showingBookmarksStateLabel = if (viewState.onlyBookmarked) {
        stringResource(R.string.showing_only_bookmarked_articles)
    } else {
        stringResource(R.string.showing_all_articles)
    }

    val coroutineScope = rememberCoroutineScope()

    FeedScreen(
        modifier = modifier,
        viewState = viewState,
        onRefreshVisible = onRefreshVisible,
        onOpenNavDrawer = {
            coroutineScope.launch {
                if (drawerState.isOpen) {
                    drawerState.close()
                } else {
                    drawerState.open()
                }
            }
        },
        onMarkAllAsRead = onMarkAllAsRead,
        ttsOnPlay = ttsOnPlay,
        ttsOnPause = ttsOnPause,
        ttsOnStop = ttsOnStop,
        ttsOnSkipNext = ttsOnSkipNext,
        ttsOnSelectLanguage = ttsOnSelectLanguage,
        onDismissDeleteDialog = onDismissDeleteDialog,
        onDismissEditDialog = onDismissEditDialog,
        onDelete = onDeleteFeeds,
        onEditFeed = onEditFeed,
        toolbarActions = {
            PlainTooltipBox(tooltip = { Text(showingUnreadStateLabel) }) {
                IconToggleButton(
                    checked = viewState.onlyUnread,
                    onCheckedChange = onToggleOnlyUnread,
                    modifier = Modifier
                        .tooltipAnchor()
                        .semantics {
                            stateDescription = showingUnreadStateLabel
                        },
                ) {
                    if (viewState.onlyUnread) {
                        Icon(
                            Icons.Default.VisibilityOff,
                            contentDescription = null,
                        )
                    } else {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                        )
                    }
                }
            }
            PlainTooltipBox(tooltip = { Text(showingBookmarksStateLabel) }) {
                IconToggleButton(
                    checked = viewState.onlyBookmarked,
                    onCheckedChange = onToggleOnlyBookmarked,
                    modifier = Modifier
                        .tooltipAnchor()
                        .semantics {
                            stateDescription = showingBookmarksStateLabel
                        },
                ) {
                    if (viewState.onlyBookmarked) {
                        Icon(
                            Icons.Default.BookmarkRemove,
                            contentDescription = null,
                        )
                    } else {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                        )
                    }
                }
            }

            PlainTooltipBox(tooltip = { Text(stringResource(R.string.open_menu)) }) {
                Box {
                    IconButton(
                        onClick = { onShowToolbarMenu(true) },
                        modifier = Modifier.tooltipAnchor(),
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.open_menu),
                        )
                    }
                    DropdownMenu(
                        expanded = viewState.showToolbarMenu,
                        onDismissRequest = { onShowToolbarMenu(false) },
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onMarkAllAsRead()
                                onShowToolbarMenu(false)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.mark_all_as_read))
                            },
                        )
                        Divider()
                        DropdownMenuItem(
                            onClick = {
                                onRefreshAll()
                                onShowToolbarMenu(false)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.synchronize_feeds),
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.synchronize_feeds))
                            },
                        )
                        Divider()
                        DropdownMenuItem(
                            onClick = {
                                onShowToolbarMenu(false)
                                onAddFeed()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.add_feed))
                            },
                        )
                        DropdownMenuItem(
                            onClick = {
                                if (viewState.visibleFeeds.size == 1) {
                                    onEditFeed(viewState.visibleFeeds.first().id)
                                } else {
                                    onShowEditDialog()
                                }
                                onShowToolbarMenu(false)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.edit_feed))
                            },
                        )
                        DropdownMenuItem(
                            onClick = {
                                onShowDeleteDialog()
                                onShowToolbarMenu(false)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.delete_feed))
                            },
                        )
                        Divider()
                        DropdownMenuItem(
                            onClick = {
                                onShowToolbarMenu(false)
                                onImport()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ImportExport,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.import_feeds_from_opml))
                            },
                        )
                        DropdownMenuItem(
                            onClick = {
                                onShowToolbarMenu(false)
                                onExport()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ImportExport,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.export_feeds_to_opml))
                            },
                        )
                        Divider()
                        DropdownMenuItem(
                            onClick = {
                                onShowToolbarMenu(false)
                                onSettings()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.action_settings))
                            },
                        )
                        Divider()
                        DropdownMenuItem(
                            onClick = {
                                onShowToolbarMenu(false)
                                onSendFeedback()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(stringResource(id = R.string.send_bug_report))
                            },
                        )
                    }
                }
            }
        },
    ) { innerModifier ->
        val windowSize = LocalWindowSize()

        val screenType by remember(windowSize) {
            derivedStateOf {
                when (windowSize) {
                    WindowSize.Compact -> FeedScreenType.FeedList
                    WindowSize.CompactWide, WindowSize.Medium, WindowSize.Expanded -> FeedScreenType.FeedGrid
                }
            }
        }

        when (screenType) {
            FeedScreenType.FeedGrid -> FeedGridContent(
                viewState = viewState,
                gridState = feedGridState,
                onOpenNavDrawer = {
                    coroutineScope.launch {
                        if (drawerState.isOpen) {
                            drawerState.close()
                        } else {
                            drawerState.open()
                        }
                    }
                },
                markAsUnread = markAsUnread,
                onAddFeed = onAddFeed,
                markBeforeAsRead = markBeforeAsRead,
                markAfterAsRead = markAfterAsRead,
                onItemClick = onOpenFeedItem,
                onSetPinned = onSetPinned,
                onSetBookmarked = onSetBookmarked,
                pagedFeedItems = pagedFeedItems,
                modifier = innerModifier,
            ).also { logDebug(LOG_TAG, "Showing GRID") }

            FeedScreenType.FeedList -> FeedListContent(
                viewState = viewState,
                onOpenNavDrawer = {
                    coroutineScope.launch {
                        if (drawerState.isOpen) {
                            drawerState.close()
                        } else {
                            drawerState.open()
                        }
                    }
                },
                onAddFeed = onAddFeed,
                markAsUnread = markAsUnread,
                markBeforeAsRead = markBeforeAsRead,
                markAfterAsRead = markAfterAsRead,
                onItemClick = onOpenFeedItem,
                listState = feedListState,
                onSetPinned = onSetPinned,
                onSetBookmarked = onSetBookmarked,
                pagedFeedItems = pagedFeedItems,
                modifier = innerModifier,
            ).also { logDebug(LOG_TAG, "Showing LIST") }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
)
@Composable
fun FeedScreen(
    viewState: FeedScreenViewState,
    onRefreshVisible: () -> Unit,
    onOpenNavDrawer: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    ttsOnPlay: () -> Unit,
    ttsOnPause: () -> Unit,
    ttsOnStop: () -> Unit,
    ttsOnSkipNext: () -> Unit,
    ttsOnSelectLanguage: (LocaleOverride) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onDelete: (Iterable<Long>) -> Unit,
    onEditFeed: (Long) -> Unit,
    toolbarActions: @Composable (RowScope.() -> Unit),
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    var manuallyTriggeredRefresh by rememberSaveable {
        mutableStateOf(false)
    }
    var syncIndicatorMax by rememberSaveable {
        mutableStateOf(Instant.EPOCH)
    }
    val isRefreshing by remember(viewState.latestSyncTimestamp, manuallyTriggeredRefresh) {
        derivedStateOf {
            // latestSync is equal to EPOCH when nothing is syncing
            when {
                manuallyTriggeredRefresh -> true
                viewState.latestSyncTimestamp == Instant.EPOCH -> false
                else -> {
                    // In case an error happens in sync then this might never go back to EPOCH
                    minOf(
                        viewState.latestSyncTimestamp,
                        syncIndicatorMax,
                    )
                        .isAfter(Instant.now().minusSeconds(10))
                }
            }
        }
    }

    LaunchedEffect(viewState.latestSyncTimestamp) {
        if (manuallyTriggeredRefresh && viewState.latestSyncTimestamp.isAfter(Instant.EPOCH)) {
            // A sync has happened so can safely set this to false now
            manuallyTriggeredRefresh = false
        }
    }

    LaunchedEffect(manuallyTriggeredRefresh) {
        // In the event that pulling doesn't trigger a refresh. Say if no feeds are present
        // or all feeds are so recent that no sync is triggered - or an error happens in sync
        // THEN we need to manually disable this variable so we don't get an infinite spinner
        if (manuallyTriggeredRefresh) {
            delay(5_000L)
            manuallyTriggeredRefresh = false
        }
    }

    val floatingActionButton: @Composable () -> Unit = {
        PlainTooltipBox(tooltip = { Text(stringResource(R.string.mark_all_as_read)) }) {
            FloatingActionButton(
                onClick = onMarkAllAsRead,
                modifier = Modifier
                    .navigationBarsPadding()
                    .tooltipAnchor(),
            ) {
                Icon(
                    Icons.Default.DoneAll,
                    contentDescription = stringResource(R.string.mark_all_as_read),
                )
            }
        }
    }
    val bottomBarVisibleState = remember { MutableTransitionState(viewState.isBottomBarVisible) }
    LaunchedEffect(viewState.isBottomBarVisible) {
        bottomBarVisibleState.targetState = viewState.isBottomBarVisible
    }

    val topAppBarState = key(viewState.currentFeedOrTag) {
        rememberTopAppBarState()
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

    SetStatusBarColorToMatchScrollableTopAppBar(scrollBehavior)

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            manuallyTriggeredRefresh = true
            syncIndicatorMax = Instant.now().plusSeconds(10)
            onRefreshVisible()
        },
    )

    Scaffold(
        topBar = {
            SensibleTopAppBar(
                scrollBehavior = scrollBehavior,
                title = viewState.feedScreenTitle.title
                    ?: stringResource(id = R.string.all_feeds),
                navigationIcon = {
                    IconButton(
                        onClick = onOpenNavDrawer,
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = stringResource(R.string.navigation_drawer_open),
                        )
                    }
                },
                actions = toolbarActions,
            )
        },
        bottomBar = {
            HideableTTSPlayer(
                visibleState = bottomBarVisibleState,
                currentlyPlaying = viewState.isTTSPlaying,
                onPlay = ttsOnPlay,
                onPause = ttsOnPause,
                onStop = ttsOnStop,
                onSkipNext = ttsOnSkipNext,
                languages = ImmutableHolder(viewState.ttsLanguages),
                floatingActionButton = when (viewState.showFab) {
                    true -> floatingActionButton
                    false -> null
                },
                onSelectLanguage = ttsOnSelectLanguage,
            )
        },
        floatingActionButton = {
            if (viewState.showFab) {
                AnimatedVisibility(
                    visible = bottomBarVisibleState.isIdle && !bottomBarVisibleState.targetState,
                    enter = scaleIn(animationSpec = tween(256)),
                    exit = scaleOut(animationSpec = tween(256)),
                ) {
                    floatingActionButton()
                }
            }
        },
        modifier = modifier
            // The order is important! PullToRefresh MUST come first
            .pullRefresh(state = pullRefreshState)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding),
        ) {
            content(
                Modifier,
            )

            PullRefreshIndicator(
                isRefreshing,
                pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter),
            )
        }

        if (viewState.showDeleteDialog) {
            DeleteFeedDialog(
                feeds = ImmutableHolder(
                    viewState.visibleFeeds.map {
                        DeletableFeed(it.id, it.displayTitle)
                    },
                ),
                onDismiss = onDismissDeleteDialog,
                onDelete = onDelete,
            )
        }

        if (viewState.showEditDialog) {
            EditFeedDialog(
                feeds = ImmutableHolder(
                    viewState.visibleFeeds.map {
                        DeletableFeed(
                            it.id,
                            it.displayTitle,
                        )
                    },
                ),
                onDismiss = onDismissEditDialog,
                onEdit = onEditFeed,
            )
        }
    }
}

@Composable
fun FeedListContent(
    viewState: FeedScreenViewState,
    onOpenNavDrawer: () -> Unit,
    onAddFeed: () -> Unit,
    markAsUnread: (Long, Boolean) -> Unit,
    markBeforeAsRead: (Int) -> Unit,
    markAfterAsRead: (Int) -> Unit,
    onItemClick: (Long) -> Unit,
    onSetPinned: (Long, Boolean) -> Unit,
    onSetBookmarked: (Long, Boolean) -> Unit,
    listState: LazyListState,
    pagedFeedItems: LazyPagingItems<FeedListItem>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(modifier = modifier) {
        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = !viewState.haveVisibleFeedItems,
        ) {
            // Keeping the Box behind so the scrollability doesn't override clickable
            // Separate box because scrollable will ignore max size.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            )
            NothingToRead(
                modifier = Modifier,
                onOpenOtherFeed = onOpenNavDrawer,
                onAddFeed = onAddFeed,
            )
        }

        val arrangement = when (viewState.feedItemStyle) {
            FeedItemStyle.CARD -> Arrangement.spacedBy(LocalDimens.current.margin)
            FeedItemStyle.COMPACT -> Arrangement.spacedBy(0.dp)
            FeedItemStyle.SUPER_COMPACT -> Arrangement.spacedBy(0.dp)
        }

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = viewState.haveVisibleFeedItems,
        ) {
            LazyColumn(
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = arrangement,
                contentPadding = if (viewState.isBottomBarVisible) {
                    PaddingValues(0.dp)
                } else {
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom,
                    ).run {
                        when (viewState.feedItemStyle) {
                            FeedItemStyle.CARD -> addMargin(horizontal = LocalDimens.current.margin)
                            FeedItemStyle.COMPACT, FeedItemStyle.SUPER_COMPACT -> addMarginLayout(
                                start = LocalDimens.current.margin,
                            )
                        }
                    }
                        .asPaddingValues()
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                /*
                This is a trick to make the list stay at item 0 when updates come in IF it is
                scrolled to the top.
                 */
                item {
                    Spacer(modifier = Modifier.fillMaxWidth())
                }
                items(
                    pagedFeedItems.itemCount,
                    key = { itemIndex ->
                        pagedFeedItems.itemSnapshotList.items[itemIndex].id
                    },
                ) { itemIndex ->
                    val previewItem = pagedFeedItems[itemIndex]
                        ?: return@items

                    SwipeableFeedItemPreview(
                        onSwipe = { currentState ->
                            markAsUnread(
                                previewItem.id,
                                !currentState,
                            )
                        },
                        onlyUnread = viewState.onlyUnread,
                        item = previewItem,
                        showThumbnail = viewState.showThumbnails,
                        feedItemStyle = viewState.feedItemStyle,
                        swipeAsRead = viewState.swipeAsRead,
                        newIndicator = !viewState.onlyUnread,
                        onMarkAboveAsRead = {
                            if (itemIndex > 0) {
                                markBeforeAsRead(itemIndex)
                                if (viewState.onlyUnread) {
                                    coroutineScope.launch {
                                        listState.scrollToItem(0)
                                    }
                                }
                            }
                        },
                        onMarkBelowAsRead = {
                            markAfterAsRead(itemIndex)
                        },
                        onTogglePinned = {
                            onSetPinned(previewItem.id, !previewItem.pinned)
                        },
                        onToggleBookmarked = {
                            onSetBookmarked(previewItem.id, !previewItem.bookmarked)
                        },
                        onShareItem = {
                            val intent = Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    if (previewItem.link != null) {
                                        putExtra(Intent.EXTRA_TEXT, previewItem.link)
                                    }
                                    putExtra(Intent.EXTRA_TITLE, previewItem.title)
                                    type = "text/plain"
                                },
                                null,
                            )
                            context.startActivity(intent)
                        },
                    ) {
                        onItemClick(previewItem.id)
                    }

                    if (viewState.feedItemStyle != FeedItemStyle.CARD) {
                        if (itemIndex < pagedFeedItems.itemCount - 1) {
                            Divider(
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxWidth(),
                            )
                        }
                    }
                }
                /*
                This item is provide padding for the FAB
                 */
                if (viewState.showFab && !viewState.isBottomBarVisible) {
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((56 + 16).dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedGridContent(
    viewState: FeedScreenViewState,
    onOpenNavDrawer: () -> Unit,
    onAddFeed: () -> Unit,
    markAsUnread: (Long, Boolean) -> Unit,
    markBeforeAsRead: (Int) -> Unit,
    markAfterAsRead: (Int) -> Unit,
    onItemClick: (Long) -> Unit,
    onSetPinned: (Long, Boolean) -> Unit,
    onSetBookmarked: (Long, Boolean) -> Unit,
    gridState: LazyStaggeredGridState,
    pagedFeedItems: LazyPagingItems<FeedListItem>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(modifier = modifier) {
        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = !viewState.haveVisibleFeedItems,
        ) {
            // Keeping the Box behind so the scrollability doesn't override clickable
            // Separate box because scrollable will ignore max size.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            )
            NothingToRead(
                modifier = Modifier,
                onOpenOtherFeed = onOpenNavDrawer,
                onAddFeed = onAddFeed,
            )
        }

        val arrangement = when (viewState.feedItemStyle) {
            FeedItemStyle.CARD -> Arrangement.spacedBy(LocalDimens.current.gutter)
            FeedItemStyle.COMPACT -> Arrangement.spacedBy(LocalDimens.current.gutter)
            FeedItemStyle.SUPER_COMPACT -> Arrangement.spacedBy(LocalDimens.current.gutter)
        }

        // Grid kicks in at 600.dp. So make sure at least 2 columns always in grid mode
        val minItemWidth = (300.dp - LocalDimens.current.margin * 2)

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = viewState.haveVisibleFeedItems,
        ) {
            LazyVerticalStaggeredGrid(
                state = gridState,
                columns = StaggeredGridCells.Adaptive(minItemWidth),
                contentPadding = if (viewState.isBottomBarVisible) {
                    PaddingValues(0.dp)
                } else {
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom,
                    ).addMargin(LocalDimens.current.margin)
                        .asPaddingValues()
                },
                verticalItemSpacing = LocalDimens.current.gutter,
                horizontalArrangement = arrangement,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    pagedFeedItems.itemCount,
                    key = { itemIndex ->
                        pagedFeedItems.itemSnapshotList.items[itemIndex].id
                    },
                ) { itemIndex ->
                    val previewItem = pagedFeedItems[itemIndex]
                        ?: return@items

                    SwipeableFeedItemPreview(
                        onSwipe = { currentState ->
                            markAsUnread(
                                previewItem.id,
                                !currentState,
                            )
                        },
                        onlyUnread = viewState.onlyUnread,
                        item = previewItem,
                        showThumbnail = viewState.showThumbnails,
                        feedItemStyle = viewState.feedItemStyle,
                        swipeAsRead = viewState.swipeAsRead,
                        newIndicator = !viewState.onlyUnread,
                        onMarkAboveAsRead = {
                            if (itemIndex > 0) {
                                markBeforeAsRead(itemIndex)
                                if (viewState.onlyUnread) {
                                    coroutineScope.launch {
                                        gridState.scrollToItem(0)
                                    }
                                }
                            }
                        },
                        onMarkBelowAsRead = {
                            markAfterAsRead(itemIndex)
                        },
                        onTogglePinned = {
                            onSetPinned(previewItem.id, !previewItem.pinned)
                        },
                        onToggleBookmarked = {
                            onSetBookmarked(previewItem.id, !previewItem.bookmarked)
                        },
                        onShareItem = {
                            val intent = Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    if (previewItem.link != null) {
                                        putExtra(Intent.EXTRA_TEXT, previewItem.link)
                                    }
                                    putExtra(Intent.EXTRA_TITLE, previewItem.title)
                                    type = "text/plain"
                                },
                                null,
                            )
                            context.startActivity(intent)
                        },
                    ) {
                        onItemClick(previewItem.id)
                    }
                }
            }
        }
    }
}

@Immutable
data class FeedOrTag(
    val id: Long,
    val tag: String,
)

val FeedOrTag.isFeed
    get() = id > ID_UNSET

enum class FeedScreenType {
    FeedGrid,
    FeedList,
}

// See https://issuetracker.google.com/issues/177245496#comment24
@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyListState(): LazyListState {
    // After recreation, LazyPagingItems first return 0 items, then the cached items.
    // This behavior/issue is resetting the LazyListState scroll position.
    // Below is a workaround. More info: https://issuetracker.google.com/issues/177245496.
    return when (itemCount) {
        // Return a different LazyListState instance.
        0 -> remember(this) { LazyListState(0, 0) }
        // Return rememberLazyListState (normal case).
        else -> androidx.compose.foundation.lazy.rememberLazyListState()
    }
}
