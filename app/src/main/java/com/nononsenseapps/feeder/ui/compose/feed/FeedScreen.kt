package com.nononsenseapps.feeder.ui.compose.feed

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.empty.NothingToRead
import com.nononsenseapps.feeder.ui.compose.feedarticle.FeedScreenViewState
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableTTSPlayer
import com.nononsenseapps.feeder.ui.compose.text.withBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.addMargin
import com.nononsenseapps.feeder.ui.compose.utils.addMarginLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.Instant

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
    modifier: Modifier,
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
                    .verticalScroll(rememberScrollState())
            )
            NothingToRead(
                modifier = modifier,
                onOpenOtherFeed = onOpenNavDrawer,
                onAddFeed = onAddFeed
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
                contentPadding = if (viewState.isBottomBarVisible) PaddingValues(0.dp) else WindowInsets.navigationBars.only(
                    WindowInsetsSides.Bottom
                ).run {
                    when (viewState.feedItemStyle) {
                        FeedItemStyle.CARD -> addMargin(horizontal = LocalDimens.current.margin)
                        FeedItemStyle.COMPACT, FeedItemStyle.SUPER_COMPACT -> addMarginLayout(start = LocalDimens.current.margin)
                    }
                }
                    .asPaddingValues(),
                modifier = Modifier.fillMaxSize()
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
                    }
                ) { itemIndex ->
                    val previewItem = pagedFeedItems[itemIndex]
                        ?: return@items

                    SwipeableFeedItemPreview(
                        onSwipe = { currentState ->
                            markAsUnread(
                                previewItem.id,
                                !currentState
                            )
                        },
                        swipeAsRead = viewState.swipeAsRead,
                        onlyUnread = viewState.onlyUnread,
                        item = previewItem,
                        showThumbnail = viewState.showThumbnails,
                        feedItemStyle = viewState.feedItemStyle,
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
                        onShareItem = {
                            val intent = Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    if (previewItem.link != null) {
                                        putExtra(Intent.EXTRA_TEXT, previewItem.link)
                                    }
                                    putExtra(Intent.EXTRA_TITLE, previewItem.title)
                                    type = "text/plain"
                                },
                                null
                            )
                            context.startActivity(intent)
                        },
                        onItemClick = {
                            onItemClick(previewItem.id)
                        },
                        onTogglePinned = {
                            onSetPinned(previewItem.id, !previewItem.pinned)
                        },
                        onToggleBookmarked = {
                            onSetBookmarked(previewItem.id, !previewItem.bookmarked)
                        },
                    )

                    if (viewState.feedItemStyle != FeedItemStyle.CARD) {
                        if (itemIndex < pagedFeedItems.itemCount - 1) {
                            Divider(
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxWidth()
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
                                .height((56 + 16).dp)
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
    markBeforeAsRead: (Int) -> Unit,
    markAfterAsRead: (Int) -> Unit,
    onItemClick: (Long) -> Unit,
    onSetPinned: (Long, Boolean) -> Unit,
    onSetBookmarked: (Long, Boolean) -> Unit,
    gridState: LazyStaggeredGridState,
    pagedFeedItems: LazyPagingItems<FeedListItem>,
    modifier: Modifier,
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
                    .verticalScroll(rememberScrollState())
            )
            NothingToRead(
                modifier = modifier,
                onOpenOtherFeed = onOpenNavDrawer,
                onAddFeed = onAddFeed
            )
        }

        val arrangement = when (viewState.feedItemStyle) {
            FeedItemStyle.CARD -> Arrangement.spacedBy(LocalDimens.current.gutter)
            FeedItemStyle.COMPACT -> Arrangement.spacedBy(LocalDimens.current.gutter)
            FeedItemStyle.SUPER_COMPACT -> Arrangement.spacedBy(LocalDimens.current.gutter)
        }

        val minItemWidth = when (viewState.feedItemStyle) {
            // 300 - 16 - 16/2 : so that 600dp screens should get two columns
            FeedItemStyle.CARD -> 276.dp
            FeedItemStyle.COMPACT -> 400.dp
            FeedItemStyle.SUPER_COMPACT -> 276.dp
        }

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = viewState.haveVisibleFeedItems,
        ) {
            LazyVerticalStaggeredGrid(
                state = gridState,
                columns = StaggeredGridCells.Adaptive(minItemWidth),
                contentPadding = if (viewState.isBottomBarVisible) PaddingValues(0.dp) else WindowInsets.navigationBars.only(
                    WindowInsetsSides.Bottom
                ).addMargin(LocalDimens.current.margin)
                    .asPaddingValues(),
                verticalArrangement = arrangement,
                horizontalArrangement = arrangement,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    pagedFeedItems.itemCount,
                    key = { itemIndex ->
                        pagedFeedItems.itemSnapshotList.items[itemIndex].id
                    }
                ) { itemIndex ->
                    val previewItem = pagedFeedItems[itemIndex]
                        ?: return@items

                    FixedFeedItemPreview(
                        item = previewItem,
                        showThumbnail = viewState.showThumbnails,
                        feedItemStyle = viewState.feedItemStyle,
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
                        onShareItem = {
                            val intent = Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    if (previewItem.link != null) {
                                        putExtra(Intent.EXTRA_TEXT, previewItem.link)
                                    }
                                    putExtra(Intent.EXTRA_TITLE, previewItem.title)
                                    type = "text/plain"
                                },
                                null
                            )
                            context.startActivity(intent)
                        },
                        onItemClick = {
                            onItemClick(previewItem.id)
                        },
                        onTogglePinned = {
                            onSetPinned(previewItem.id, !previewItem.pinned)
                        },
                        onToggleBookmarked = {
                            onSetBookmarked(previewItem.id, !previewItem.bookmarked)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
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
    toolbarActions: @Composable() (RowScope.() -> Unit),
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    var lastMaxPoint by remember {
        mutableStateOf(Instant.EPOCH)
    }
    val isRefreshing by remember(viewState.latestSyncTimestamp, lastMaxPoint) {
        derivedStateOf {
            viewState.latestSyncTimestamp.isAfter(
                maxOf(
                    lastMaxPoint,
                    Instant.now().minusSeconds(20)
                )
            )
        }
    }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefreshVisible,
    )

    LaunchedEffect(viewState.latestSyncTimestamp) {
        // GUI will only display refresh indicator for 10 seconds at most.
        // Fixes an issue where sync was triggered but no feed needed syncing, meaning no DB updates
        delay(10_000L)
        lastMaxPoint = Instant.now()
    }

    val floatingActionButton: @Composable () -> Unit = {
        FloatingActionButton(
            onClick = onMarkAllAsRead,
            modifier = Modifier.navigationBarsPadding(),
        ) {
            Icon(
                Icons.Default.DoneAll,
                contentDescription = stringResource(R.string.mark_all_as_read)
            )
        }
    }
    val bottomBarVisibleState = remember { MutableTransitionState(viewState.isBottomBarVisible) }
    LaunchedEffect(viewState.isBottomBarVisible) {
        bottomBarVisibleState.targetState = viewState.isBottomBarVisible
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val text = viewState.feedScreenTitle.title
                        ?: stringResource(id = R.string.all_feeds)
                    withBidiDeterminedLayoutDirection(paragraph = text) {
                        Text(
                            text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenNavDrawer
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = stringResource(R.string.navigation_drawer_open)
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
                onSelectLanguage = ttsOnSelectLanguage,
                languages = ImmutableHolder(viewState.ttsLanguages),
                floatingActionButton = when (viewState.showFab) {
                    true -> floatingActionButton
                    false -> null
                }
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
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
        ) {
            content(Modifier.padding(padding))

            PullRefreshIndicator(
                isRefreshing,
                pullRefreshState,
                Modifier
                    .padding(padding)
                    .align(Alignment.TopCenter)
            )
        }

        if (viewState.showDeleteDialog) {
            DeleteFeedDialog(
                feeds = ImmutableHolder(
                    viewState.visibleFeeds.map {
                        DeletableFeed(it.id, it.displayTitle)
                    }
                ),
                onDismiss = onDismissDeleteDialog,
                onDelete = onDelete
            )
        }

        if (viewState.showEditDialog) {
            EditFeedDialog(
                feeds = ImmutableHolder(
                    viewState.visibleFeeds.map {
                        DeletableFeed(
                            it.id,
                            it.displayTitle
                        )
                    }
                ),
                onDismiss = onDismissEditDialog,
                onEdit = onEditFeed
            )
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
