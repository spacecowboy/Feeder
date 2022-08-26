package com.nononsenseapps.feeder.ui.compose.feed

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.size.PixelSize
import coil.size.Precision
import coil.size.Scale
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.empty.NothingToRead
import com.nononsenseapps.feeder.ui.compose.feedarticle.FeedScreenViewState
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableTTSPlayer
import com.nononsenseapps.feeder.ui.compose.theme.isLight
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.Instant

@OptIn(ExperimentalCoilApi::class)
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

    val isLightTheme = MaterialTheme.colorScheme.isLight

    @DrawableRes
    val placeHolder: Int by remember(isLightTheme) {
        derivedStateOf {
            if (isLightTheme) {
                R.drawable.placeholder_image_article_day
            } else {
                R.drawable.placeholder_image_article_night
            }
        }
    }

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

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = viewState.haveVisibleFeedItems,
        ) {
            LazyColumn(
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = if (viewState.isBottomBarVisible) PaddingValues(0.dp) else WindowInsets.navigationBars.asPaddingValues(),
                modifier = Modifier.fillMaxSize()
            ) {
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
                        imagePainter = { imageUrl, modifier ->
                            Box {
                                Image(
                                    painter = rememberImagePainter(
                                        data = imageUrl,
                                        builder = {
                                            this.placeholder(placeHolder)
                                                .error(placeHolder)
                                                .scale(Scale.FILL)
                                                .precision(Precision.INEXACT)
                                                .size(PixelSize(1000, 1000))
                                        },
                                    ),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                    modifier = modifier
                                        .run {
                                            when (viewState.feedItemStyle) {
                                                FeedItemStyle.CARD ->
                                                    this
                                                        .fillMaxWidth()
                                                        .aspectRatio(16.0f / 9.0f)
                                                FeedItemStyle.COMPACT,
                                                FeedItemStyle.SUPER_COMPACT,
                                                -> this
                                                    .width(64.dp)
                                                    .fillMaxHeight()
                                            }
                                        }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    val refreshState = rememberSwipeRefreshState(
        viewState.latestSyncTimestamp.isAfter(Instant.now().minusSeconds(20))
    )

    LaunchedEffect(viewState.latestSyncTimestamp) {
        // GUI will only display refresh indicator for 10 seconds at most.
        // Fixes an issue where sync was triggered but no feed needed syncing, meaning no DB updates
        delay(10_000L)
        refreshState.isRefreshing = false
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        rememberTopAppBarState()
    )

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
            SmallTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        viewState.feedScreenTitle.title ?: stringResource(id = R.string.all_feeds),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
    ) { padding ->
        SwipeRefresh(
            state = refreshState,
            onRefresh = onRefreshVisible,
            indicatorPadding = padding,
        ) {
            content(Modifier.padding(padding))
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
