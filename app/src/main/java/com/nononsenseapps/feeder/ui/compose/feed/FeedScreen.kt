package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.DrawerValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ImportExport
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.ApplicationState
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.opml.exportOpml
import com.nononsenseapps.feeder.model.opml.importOpml
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerFeed
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerItemWithUnreadCount
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTag
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTop
import com.nononsenseapps.feeder.ui.compose.navdrawer.ListOfFeedsAndTags
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.util.SortingOptions
import com.nononsenseapps.feeder.util.ThemeOptions
import com.nononsenseapps.feeder.util.openGitlabIssues
import kotlinx.coroutines.launch
import org.kodein.di.compose.LocalDI
import org.kodein.di.compose.instance
import org.kodein.di.instance
import org.threeten.bp.LocalDateTime

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
    val onlyUnread by settingsViewModel.showOnlyUnread.collectAsState()
    val currentSorting by settingsViewModel.currentSorting.collectAsState()
    val currentFeedAndTag by settingsViewModel.currentFeedAndTag.collectAsState()
    val showFloatingActionButton by settingsViewModel.showFab.collectAsState()

    val screenTitle by feedItemsViewModel.currentTitle.collectAsState(initial = "")

    val feedsAndTags by feedItemsViewModel.drawerItemsWithUnreadCounts
        .collectAsState(initial = emptyList())

    feedItemsViewModel.feedListArgs = feedItemsViewModel.feedListArgs.copy(
        feedId = currentFeedAndTag.first,
        tag = currentFeedAndTag.second,
        onlyUnread = onlyUnread,
        newestFirst = when (currentSorting) {
            SortingOptions.NEWEST_FIRST -> true
            SortingOptions.OLDEST_FIRST -> false
        }
    )

    val pagedFeedItems = feedItemsViewModel.feedListItems.collectAsLazyPagingItems()

    val visibleFeeds by feedListViewModel.getFeedTitles(
        feedId = currentFeedAndTag.first,
        tag = currentFeedAndTag.second
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

    val context = LocalContext.current
    val onSendFeedback = {
        context.startActivity(openGitlabIssues())
    }

    val di = LocalDI.current

    val opmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        if (uri != null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                exportOpml(di, uri)
            }
        }
    }

    val opmlImporter = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                importOpml(di, uri)
            }
        }
    }

    FeedScreen(
        screenTitle = screenTitle ?: stringResource(id = R.string.all_feeds),
        visibleFeeds = visibleFeeds,
        feedsAndTags = feedsAndTags,
        refreshState = refreshState,
        showFloatingActionButton = showFloatingActionButton,
        onRefreshVisible = {
            applicationState.setRefreshing()
            requestFeedSync(
                di = di,
                feedId = currentFeedAndTag.first,
                feedTag = currentFeedAndTag.second,
                ignoreConnectivitySettings = true,
                forceNetwork = true,
                parallell = true
            )
        },
        onRefreshAll = {
            applicationState.setRefreshing()
            requestFeedSync(
                di = di,
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
        onSettings = onSettings,
        onSendFeedback = onSendFeedback,
        onImport = { opmlImporter.launch(arrayOf("text/plain", "text/xml", "text/opml", "*/*")) },
        onExport = { opmlExporter.launch("feeder-export-${LocalDateTime.now()}") },
        onMarkAllAsRead = {
            feedItemsViewModel.markAllAsReadInBackground(
                feedId = currentFeedAndTag.first,
                tag = currentFeedAndTag.second
            )
        }
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
        ) {
            items(
                pagedFeedItems.itemCount,
                key = { itemIndex ->
                    pagedFeedItems.snapshot().items[itemIndex].id
                }
            ) { itemIndex ->
                val previewItem = pagedFeedItems.getAsState(index = itemIndex).value
                    ?: return@items

                FeedItemPreview(
                    item = previewItem,
                    onMarkAboveAsRead = {
                        if (itemIndex > 0) {
                            feedItemsViewModel.markBeforeAsRead(itemIndex)
                        }
                    },
                    onMarkBelowAsRead = {
                        feedItemsViewModel.markAfterAsRead(itemIndex)
                    },
                    onItemClick = {
                        onItemClick(previewItem.id)
                    }
                )
            }

            when {
                pagedFeedItems.loadState.prepend is LoadState.Loading -> {
                    Log.d("JONAS", "Prepend pager")
                }
                pagedFeedItems.loadState.refresh is LoadState.Loading -> {
                    Log.d("JONAS", "Refreshed pager")
                }
                pagedFeedItems.loadState.append is LoadState.Loading -> {
                    Log.d("JONAS", "Append pager")
                }
                pagedFeedItems.loadState.prepend is LoadState.Error -> {
                    item {
                        Text("pager Prepend Error! TODO")
                    }
                }
                pagedFeedItems.loadState.refresh is LoadState.Error -> {
                    item {
                        Text("pager Refresh Error! TODO")
                    }
                }
                pagedFeedItems.loadState.append is LoadState.Error -> {
                    item {
                        Text("pager Append Error! TODO")
                    }
                }
                pagedFeedItems.loadState.append.endOfPaginationReached -> {
                    // User has reached the end of the list, could insert something here

                    if (pagedFeedItems.itemCount == 0) {
                        Log.d("JONAS", "Pager empty")
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
    screenTitle: String,
    visibleFeeds: List<FeedTitle>,
    feedsAndTags: List<DrawerItemWithUnreadCount>,
    refreshState: SwipeRefreshState,
    onRefreshVisible: () -> Unit,
    onRefreshAll: () -> Unit,
    onlyUnread: Boolean,
    onToggleOnlyUnread: (Boolean) -> Unit,
    onDrawerItemSelected: (Long, String) -> Unit,
    onDelete: (Iterable<Long>) -> Unit,
    onAddFeed: (() -> Unit),
    onEditFeed: (() -> Unit)?,
    onSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    showFloatingActionButton: Boolean,
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
                title = { Text(screenTitle) },
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
                    IconButton(
                        onClick = onRefreshAll
                    ) {
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
                            DropdownMenuItem(onClick = onImport) {
                                Icon(
                                    Icons.Default.ImportExport,
                                    contentDescription = "Import OPML button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.import_feeds_from_opml))
                            }
                            Divider()
                            DropdownMenuItem(onClick = onExport) {
                                Icon(
                                    Icons.Default.ImportExport,
                                    contentDescription = "Export OPML button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.export_feeds_to_opml))
                            }
                            Divider()
                            DropdownMenuItem(onClick = onSettings) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.action_settings))
                            }
                            Divider()
                            DropdownMenuItem(onClick = onSendFeedback) {
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
                        val id = when (item) {
                            is DrawerFeed -> item.id
                            is DrawerTag -> ID_UNSET
                            is DrawerTop -> ID_ALL_FEEDS
                        }
                        val tag = when (item) {
                            is DrawerFeed -> item.tag
                            is DrawerTag -> item.tag
                            is DrawerTop -> ""
                        }
                        onDrawerItemSelected(id, tag)
                        scaffoldState.drawerState.close()
                    }
                }
            )
        },
        floatingActionButton = {
            if (showFloatingActionButton) {
                FloatingActionButton(
                    onClick = onMarkAllAsRead
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Mark all as read button"
                    )
                }
            }
        }
    ) { padding ->
        SwipeRefresh(
            state = refreshState,
            onRefresh = onRefreshVisible
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
            screenTitle = "FeedScreen",
            visibleFeeds = emptyList(),
            feedsAndTags = listOf(),
            refreshState = rememberSwipeRefreshState(false),
            onRefreshVisible = { },
            onRefreshAll = {},
            onlyUnread = false,
            showFloatingActionButton = true,
            onToggleOnlyUnread = {},
            onDrawerItemSelected = { _, _ -> },
            onAddFeed = { },
            onEditFeed = null,
            onDelete = {},
            onSettings = {},
            onSendFeedback = {},
            onImport = {},
            onExport = {},
            onMarkAllAsRead = {}
        ) { modifier, _ ->
            LazyColumn(
                modifier = modifier
            ) {
//                item {
//                    FeedItemPreview(
//                        item = PreviewItem(
//                            id = 1L,
//                            plainTitle = "An interesting story",
//                            plainSnippet = "So this thing happened yesterday",
//                            feedTitle = "The Times"
//                        ),
//                        onItemClick = {}
//                    )
//                }
//                item {
//                    FeedItemPreview(
//                        item = PreviewItem(
//                            id = 2L,
//                            plainTitle = "And this other thing",
//                            plainSnippet = "One two, ".repeat(100),
//                            feedTitle = "The Middle Spread"
//                        ),
//                        onItemClick = {}
//                    )
//                }
//                item {
//                    FeedItemPreview(
//                        item = PreviewItem(
//                            id = 3L,
//                            plainTitle = "Man dies",
//                            plainSnippet = "Got old",
//                            feedTitle = "The Foobar"
//                        ),
//                        onItemClick = {}
//                    )
//                }
            }
        }
    }
}
