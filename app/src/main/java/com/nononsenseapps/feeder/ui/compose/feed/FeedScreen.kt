package com.nononsenseapps.feeder.ui.compose.feed

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconToggleButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.size.PixelSize
import coil.size.Precision
import coil.size.Scale
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.empty.NothingToRead
import com.nononsenseapps.feeder.ui.compose.feedarticle.FeedScreenViewState
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableReadAloudPlayer
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
                contentPadding = PaddingValues(
                    bottom = if (viewState.bottomBarVisible) (80 + 40).dp else 80.dp
                ),
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
                        imagePainter = { imageUrl ->
                            val alpha: Float = if (previewItem.shouldBeShownAsUnread) {
                                1.0f
                            } else {
                                0.5f
                            }
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
                                    modifier = Modifier
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
                                        .alpha(alpha)
                                )
                                if (previewItem.pinned) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = Color.Red.copy(alpha = 0.7f)
                                    )
                                }
                                if (previewItem.bookmarked) {
                                    Icon(
                                        Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = Color.Yellow.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewState: FeedScreenViewState,
    refreshState: SwipeRefreshState,
    onRefreshVisible: () -> Unit,
    onRefreshAll: () -> Unit,
    onToggleOnlyUnread: (Boolean) -> Unit,
    onToggleOnlyBookmarked: (Boolean) -> Unit,
    onOpenNavDrawer: () -> Unit,
    onDelete: (Iterable<Long>) -> Unit,
    onAddFeed: () -> Unit,
    onEditFeed: (Long) -> Unit,
    onSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    readAloudOnPlay: () -> Unit,
    readAloudOnPause: () -> Unit,
    readAloudOnStop: () -> Unit,
    content: @Composable (Modifier, () -> Unit) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var showMenu by rememberSaveable {
        mutableStateOf(false)
    }
    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showEditDialog by rememberSaveable {
        mutableStateOf(false)
    }

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

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            .systemBarsPadding(),
        topBar = {
            SmallTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        viewState.feedScreenTitle.title ?: stringResource(id = R.string.all_feeds),
                        maxLines = 2,
                        modifier = Modifier
                            .safeSemantics {
                                testTag = "appBarTitle"
                            }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                // TODO drawer
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = stringResource(R.string.navigation_drawer_open)
                        )
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = viewState.onlyUnread,
                        onCheckedChange = onToggleOnlyUnread,
                        modifier = Modifier.semantics {
                            stateDescription = showingUnreadStateLabel
                        }
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
                    IconToggleButton(
                        checked = viewState.onlyBookmarked,
                        onCheckedChange = onToggleOnlyBookmarked,
                        modifier = Modifier.semantics {
                            stateDescription = showingBookmarksStateLabel
                        }
                    ) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                        )
                    }
                    IconButton(
                        onClick = onRefreshAll
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.synchronize_feeds)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .safeSemantics {
                                    testTag = "menuButton"
                                }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.open_menu),
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .safeSemantics {
                                    testTag = "menu"
                                }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onAddFeed()
                                },
                                modifier = Modifier
                                    .safeSemantics {
                                        testTag = "menuAddFeed"
                                    },
                                text = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.add_feed))
                                },
                            )
                            DropdownMenuItem(
                                onClick = {
                                    if (viewState.visibleFeeds.size == 1) {
                                        onEditFeed(viewState.visibleFeeds.first().id)
                                    } else {
                                        showEditDialog = true
                                    }
                                    showMenu = false
                                },
                                modifier = Modifier
                                    .safeSemantics {
                                        testTag = "menuEditFeed"
                                    },
                                text = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.edit_feed))
                                },
                            )
                            DropdownMenuItem(
                                onClick = {
                                    showDeleteDialog = true
                                    showMenu = false
                                },
                                modifier = Modifier
                                    .safeSemantics {
                                        testTag = "menuDeleteFeed"
                                    },
                                text = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.delete_feed))
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onImport()
                                },
                                modifier = Modifier
                                    .safeSemantics {
                                        testTag = "menuImportFeeds"
                                    },
                                text = {
                                    Icon(
                                        Icons.Default.ImportExport,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.import_feeds_from_opml))
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onExport()
                                },
                                modifier = Modifier
                                    .safeSemantics {
                                        testTag = "menuExportFeeds"
                                    },
                                text = {
                                    Icon(
                                        Icons.Default.ImportExport,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.export_feeds_to_opml))
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onSettings()
                                },
                                modifier = Modifier
                                    .safeSemantics {
                                        testTag = "menuSettings"
                                    },
                                text = {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.action_settings))
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onSendFeedback()
                                },
                                modifier = Modifier
                                    .safeSemantics {
                                        testTag = "menuSendBugReport"
                                    },
                                text = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.send_bug_report))
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            HideableReadAloudPlayer(
                visible = viewState.isReadAloudVisible,
                currentlyPlaying = viewState.isReadAloudPlaying,
                title = viewState.readAloudTitle,
                onPlay = readAloudOnPlay,
                onPause = readAloudOnPause,
                onStop = readAloudOnStop,
            )
        },
        floatingActionButton = {
            if (viewState.showFab) {
                FloatingActionButton(
                    onClick = onMarkAllAsRead,
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.mark_all_as_read)
                    )
                }
            }
        }
    ) { padding ->
        SwipeRefresh(
            state = refreshState,
            onRefresh = onRefreshVisible,
            indicatorPadding = padding,
        ) {
            content(
                Modifier
                    .padding(padding)
            ) {
                onOpenNavDrawer()
            }
        }

        if (showDeleteDialog) {
            DeleteFeedDialog(
                feeds = ImmutableHolder(
                    viewState.visibleFeeds.map {
                        DeletableFeed(it.id, it.displayTitle)
                    }
                ),
                onDismiss = { showDeleteDialog = false },
                onDelete = onDelete
            )
        }

        if (showEditDialog) {
            EditFeedDialog(
                feeds = ImmutableHolder(viewState.visibleFeeds.map { DeletableFeed(it.id, it.displayTitle) }),
                onDismiss = { showEditDialog = false },
                onEdit = onEditFeed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWithFeedList(
    viewState: FeedScreenViewState,
    onRefreshVisible: () -> Unit,
    onOpenNavDrawer: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    readAloudOnPlay: () -> Unit,
    readAloudOnPause: () -> Unit,
    readAloudOnStop: () -> Unit,
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

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarState()
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        viewState.feedScreenTitle.title ?: stringResource(id = R.string.all_feeds),
                        maxLines = 2,
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
            HideableReadAloudPlayer(
                visible = viewState.isReadAloudVisible,
                currentlyPlaying = viewState.isReadAloudPlaying,
                title = viewState.readAloudTitle,
                onPlay = readAloudOnPlay,
                onPause = readAloudOnPause,
                onStop = readAloudOnStop,
            )
        },
        // todo drawer
//        drawerContent = {
//            ListOfFeedsAndTags(
//                feedsAndTags = ImmutableHolder(viewState.drawerItemsWithUnreadCounts),
//                expandedTags = ImmutableHolder(viewState.expandedTags),
//                onToggleTagExpansion = onToggleTagExpansion,
//                onItemClick = { item ->
//                    coroutineScope.launch {
//                        val id = when (item) {
//                            is DrawerFeed -> item.id
//                            is DrawerTag -> ID_UNSET
//                            is DrawerTop -> ID_ALL_FEEDS
//                        }
//                        val tag = when (item) {
//                            is DrawerFeed -> item.tag
//                            is DrawerTag -> item.tag
//                            is DrawerTop -> ""
//                        }
//                        onDrawerItemSelected(id, tag)
//                        scaffoldState.drawerState.close()
//                    }
//                }
//            )
//        },
        floatingActionButton = {
            if (viewState.showFab) {
                FloatingActionButton(
                    onClick = onMarkAllAsRead,
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.mark_all_as_read)
                    )
                }
            }
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            .systemBarsPadding(),
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
                feeds = ImmutableHolder(viewState.visibleFeeds.map { DeletableFeed(it.id, it.displayTitle) }),
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
