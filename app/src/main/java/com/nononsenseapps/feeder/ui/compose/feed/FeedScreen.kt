package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.DrawerValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.model.ApplicationState
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedUnreadCount
import com.nononsenseapps.feeder.model.PreviewItem
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.navdrawer.ListOfFeedsAndTags
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.contentHorizontalPadding
import com.nononsenseapps.feeder.util.SortingOptions
import com.nononsenseapps.feeder.util.ThemeOptions
import kotlinx.coroutines.launch
import org.kodein.di.compose.LocalDI
import org.kodein.di.compose.instance

@Composable
fun FeedScreen(
    onItemClick: (Long) -> Unit,
    onAddFeed: (() -> Unit),
    onFeedEdit: (Long) -> Unit,
    onSettings: () -> Unit,
    feedListViewModel: FeedListViewModel,
    feedItemsViewModel: FeedItemsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val feedsAndTags by feedListViewModel.liveFeedsAndTagsWithUnreadCounts
        .observeAsState(initial = emptyList())

    val onlyUnread by settingsViewModel.showOnlyUnread.collectAsState()
    val currentSorting by settingsViewModel.currentSorting.collectAsState()
    val currentFeed by settingsViewModel.currentFeedAndTag.collectAsState()

    // TODO need this to update properly for changes above
    val pagedFeedItems = feedItemsViewModel.getPreviewPager(
        feedId = currentFeed.first,
        tag = currentFeed.second,
        onlyUnread = onlyUnread,
        newestFirst = currentSorting == SortingOptions.NEWEST_FIRST
    )
        .collectAsLazyPagingItems()

    val visibleFeeds by feedListViewModel.getFeedTitles(
        feedId = currentFeed.first,
        tag = currentFeed.second
    ).collectAsState(initial = emptyList())

    val applicationState: ApplicationState by instance()
    val isRefreshing by applicationState.isRefreshing.collectAsState()
    val refreshState = rememberSwipeRefreshState(isRefreshing)

    val onEditFeed = if (visibleFeeds.size == 1) {
        {
            onFeedEdit(visibleFeeds.first().id)
        }
    } else {
        null
    }

    val di = LocalDI.current

    FeedScreen(
        visibleFeeds = visibleFeeds,
        feedsAndTags = feedsAndTags,
        refreshState = refreshState,
        onRefresh = {
            applicationState.setRefreshing()
            requestFeedSync(
                di = di,
                feedId = currentFeed.first,
                feedTag = currentFeed.second,
                ignoreConnectivitySettings = true,
                forceNetwork = true,
                parallell = true
            )
        },
        onlyUnread = onlyUnread,
        onToggleOnlyUnread = { value ->
            settingsViewModel.setShowOnlyUnread(value)
        },
        onDrawerItemSelected = { id, tag ->
            settingsViewModel.setCurrentFeedAndTag(feedId = id, tag = tag)
        },
        onAddFeed = onAddFeed,
        onEditFeed = onEditFeed,
        onDelete = { feeds ->
            feedListViewModel.deleteFeeds(feeds.toList())
        },
        onSettings = onSettings
    ) { modifier, openNavDrawer ->
        if (pagedFeedItems.loadState.append.endOfPaginationReached
            && pagedFeedItems.itemCount == 0
        ) {
            NothingToRead(
                modifier = modifier,
                onOpenOtherFeed = openNavDrawer,
                onAddFeed = onAddFeed
            )
        }

        LazyColumn(
            modifier = modifier
                .padding(start = contentHorizontalPadding)
        ) {
            items(pagedFeedItems) { previewItem ->
                if (previewItem == null) {
                    return@items
                }

                FeedItemPreview(item = previewItem, onItemClick = {
                    onItemClick(previewItem.id)
                })
            }

            when {
                pagedFeedItems.loadState.prepend is LoadState.Loading -> {
                    Log.d("JONAS", "Prepend")
                }
                pagedFeedItems.loadState.refresh is LoadState.Loading -> {
                    Log.d("JONAS", "Refresh")
                }
                pagedFeedItems.loadState.append is LoadState.Loading -> {
                    Log.d("JONAS", "Append")
                }
                pagedFeedItems.loadState.prepend is LoadState.Error -> {
                    item {
                        Text("Prepend Error! TODO")
                    }
                }
                pagedFeedItems.loadState.refresh is LoadState.Error -> {
                    item {
                        Text("Refresh Error! TODO")
                    }
                }
                pagedFeedItems.loadState.append is LoadState.Error -> {
                    item {
                        Text("Append Error! TODO")
                    }
                }
                pagedFeedItems.loadState.append.endOfPaginationReached -> {
                    // User has reached the end of the list, could insert something here

                    if (pagedFeedItems.itemCount == 0) {
                        Log.d("JONAS", "Nothing")
                    } else {
                        item {
                            Spacer(modifier = Modifier.height(92.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FeedScreen(
    visibleFeeds: List<FeedTitle>,
    feedsAndTags: List<FeedUnreadCount>,
    refreshState: SwipeRefreshState,
    onRefresh: () -> Unit,
    onlyUnread: Boolean,
    onToggleOnlyUnread: (Boolean) -> Unit,
    onDrawerItemSelected: (Long, String) -> Unit,
    onDelete: (Iterable<Long>) -> Unit,
    onAddFeed: (() -> Unit),
    onEditFeed: (() -> Unit)?,
    onSettings: () -> Unit,
    content: @Composable (Modifier, suspend () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(
        rememberDrawerState(initialValue = DrawerValue.Closed)
    )
    var showMenu by remember {
        mutableStateOf(false)
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Feeder") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                scaffoldState.drawerState.open()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Drawer toggle button"
                        )
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = onlyUnread,
                        onCheckedChange = onToggleOnlyUnread
                    ) {
                        if (onlyUnread) {
                            Icon(
                                Icons.Default.VisibilityOff,
                                contentDescription = stringResource(id = R.string.show_all_items)
                            )
                        } else {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = stringResource(id = R.string.show_unread_items)
                            )
                        }
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Sync button"
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Open menu")
                        }
                        // TODO make it wider as necessary
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(onClick = onAddFeed) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add feed button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.add_feed))
                            }
                            if (onEditFeed != null) {
                                DropdownMenuItem(onClick = onEditFeed) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit feed button"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.edit_feed))
                                }
                            }
                            DropdownMenuItem(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete feed button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.delete_feed))
                            }
                            Divider()
                            DropdownMenuItem(onClick = { onSettings() }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.action_settings))
                            }
                            Divider()
                            DropdownMenuItem(onClick = { /* TODO Handle send feedback! */ }) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Send bug report button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.send_bug_report))
                            }
                        }
                    }
                }
            )
        },
        drawerContent = {
            ListOfFeedsAndTags(
                feedsAndTags = feedsAndTags,
                onItemClick = { item ->
                    coroutineScope.launch {
                        onDrawerItemSelected(item.id, item.tag)
                        scaffoldState.drawerState.close()
                    }
                }
            )
        }
    ) { padding ->
        SwipeRefresh(
            state = refreshState,
            onRefresh = onRefresh
        ) {
            content(
                Modifier
                    .padding(padding)
            ) {
                scaffoldState.drawerState.open()
            }
        }

        if (showDeleteDialog) {
            DeleteFeedDialog(
                feeds = visibleFeeds.map {
                    DeletableFeed(it.id, it.displayTitle)
                },
                onDismiss = { showDeleteDialog = false },
                onDelete = onDelete
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FeederTheme(
        ThemeOptions.DAY
    ) {
        FeedScreen(
            visibleFeeds = emptyList(),
            feedsAndTags = listOf(),
            refreshState = rememberSwipeRefreshState(false),
            onRefresh = { },
            onlyUnread = false,
            onToggleOnlyUnread = {},
            onDrawerItemSelected = { _, _ -> },
            onAddFeed = { },
            onEditFeed = null,
            onDelete = {},
            onSettings = {}
        ) { modifier, _ ->
            LazyColumn(
                modifier = modifier
                    .padding(start = contentHorizontalPadding)
            ) {
                item {
                    FeedItemPreview(
                        item = PreviewItem(
                            id = 1L,
                            plainTitle = "An interesting story",
                            plainSnippet = "So this thing happened yesterday",
                            feedTitle = "The Times"
                        ),
                        onItemClick = {}
                    )
                }
                item {
                    FeedItemPreview(
                        item = PreviewItem(
                            id = 2L,
                            plainTitle = "And this other thing",
                            plainSnippet = "One two, ".repeat(100),
                            feedTitle = "The Middle Spread"
                        ),
                        onItemClick = {}
                    )
                }
                item {
                    FeedItemPreview(
                        item = PreviewItem(
                            id = 3L,
                            plainTitle = "Man dies",
                            plainSnippet = "Got old",
                            feedTitle = "The Foobar"
                        ),
                        onItemClick = {}
                    )
                }
            }
        }
    }
}
