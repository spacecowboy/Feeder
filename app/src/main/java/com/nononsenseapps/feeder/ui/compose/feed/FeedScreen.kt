package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.DrawerValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Text
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
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
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
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
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableReadAloudPlayer
import com.nononsenseapps.feeder.ui.compose.state.getImagePlaceholder
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
    feedOrTag: FeedOrTag,
    onItemClick: (Long) -> Unit,
    onAddFeed: (() -> Unit),
    onFeedEdit: (Long) -> Unit,
    onSettings: () -> Unit,
    onOpenFeedOrTag: (FeedOrTag) -> Unit,
    feedListViewModel: FeedListViewModel,
    feedItemsViewModel: FeedItemsViewModel,
    settingsViewModel: SettingsViewModel,
    readAloudViewModel: TextToSpeechViewModel,
) {
    val onlyUnread by settingsViewModel.showOnlyUnread.collectAsState()
    val currentSorting by settingsViewModel.currentSorting.collectAsState()
    val showFloatingActionButton by settingsViewModel.showFab.collectAsState()

    val screenTitle by feedItemsViewModel.currentTitle.collectAsState(initial = "")

    val context = LocalContext.current

    val feedsAndTags by feedItemsViewModel.drawerItemsWithUnreadCounts
        .collectAsState(initial = emptyList())

    feedItemsViewModel.feedListArgs = feedItemsViewModel.feedListArgs.copy(
        feedId = feedOrTag.id,
        tag = feedOrTag.tag,
        onlyUnread = onlyUnread,
        newestFirst = when (currentSorting) {
            SortingOptions.NEWEST_FIRST -> true
            SortingOptions.OLDEST_FIRST -> false
        }
    )

    val pagedFeedItems = feedItemsViewModel.feedListItems.collectAsLazyPagingItems()

    val visibleFeeds by feedListViewModel.getFeedTitles(
        feedId = feedOrTag.id,
        tag = feedOrTag.tag
    ).collectAsState(initial = emptyList())

    val applicationState: ApplicationState by instance()
    val isRefreshing by applicationState.isRefreshing
    val refreshState = rememberSwipeRefreshState(isRefreshing)

    val onSendFeedback = {
        context.startActivity(openGitlabIssues())
    }

    val showThumbnails by settingsViewModel.showThumbnails.collectAsState()

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
    val imageLoader: ImageLoader by instance()

    @DrawableRes
    val placeHolder: Int = getImagePlaceholder(settingsViewModel)

    FeedScreen(
        screenTitle = screenTitle ?: stringResource(id = R.string.all_feeds),
        visibleFeeds = visibleFeeds,
        feedsAndTags = feedsAndTags,
        refreshState = refreshState,
        showFloatingActionButton = showFloatingActionButton,
        onRefreshVisible = {
            applicationState.setRefreshing(true)
            requestFeedSync(
                di = di,
                feedId = feedOrTag.id,
                feedTag = feedOrTag.tag,
                ignoreConnectivitySettings = true,
                forceNetwork = true,
                parallell = true
            )
        },
        onRefreshAll = {
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
            onOpenFeedOrTag(FeedOrTag(id, tag))
//             TODO set this elsewhere
//            settingsViewModel.setCurrentFeedAndTag(feedId = id, tag = tag)
        },
        onAddFeed = onAddFeed,
        onEditFeed = onFeedEdit,
        onDelete = { feeds ->
            feedListViewModel.deleteFeeds(feeds.toList())
        },
        onSettings = onSettings,
        onSendFeedback = onSendFeedback,
        readAloudPlayer = {
            HideableReadAloudPlayer(readAloudViewModel)
        },
        onImport = { opmlImporter.launch(arrayOf("text/plain", "text/xml", "text/opml", "*/*")) },
        onExport = { opmlExporter.launch("feeder-export-${LocalDateTime.now()}") },
        onMarkAllAsRead = {
            feedItemsViewModel.markAllAsReadInBackground(
                feedId = feedOrTag.id,
                tag = feedOrTag.tag
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
                val previewItem = pagedFeedItems[itemIndex]
                    ?: return@items

                SwipeableFeedItemPreview(
                    onSwipe = {
                        feedItemsViewModel.markAsRead(previewItem.id, unread = !previewItem.unread)
                    },
                    onlyUnread = onlyUnread,
                    item = previewItem,
                    showThumbnail = showThumbnails,
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
                    },
                    imagePainter = { imageUrl ->
                        Image(
                            painter = rememberCoilPainter(
                                request = imageUrl,
                                imageLoader = imageLoader,
                                requestBuilder = {
                                    this.error(placeHolder)
                                },
                                previewPlaceholder = placeHolder,
                                shouldRefetchOnSizeChange = { _, _ -> false },
                            ),
                            contentScale = ContentScale.Crop,
                            contentDescription = "Thumbnail for the article",
                            modifier = Modifier
                                .width(64.dp)
                                .fillMaxHeight()
                                .padding(start = 4.dp)
                        )
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
    onEditFeed: ((Long) -> Unit),
    onSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    showFloatingActionButton: Boolean,
    readAloudPlayer: @Composable () -> Unit,
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
    var showEditDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.statusBars,
                    applyBottom = false,
                ),
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
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onAddFeed()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add feed button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.add_feed))
                            }
                            DropdownMenuItem(
                                onClick = {
                                    if (visibleFeeds.size == 1) {
                                        onEditFeed(visibleFeeds.first().id)
                                    } else {
                                        showEditDialog = true
                                    }
                                    showMenu = false
                                }
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit feed button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.edit_feed))
                            }
                            DropdownMenuItem(
                                onClick = {
                                    showDeleteDialog = true
                                    showMenu = false
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete feed button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.delete_feed))
                            }
                            Divider()
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onImport()
                                }
                            ) {
                                Icon(
                                    Icons.Default.ImportExport,
                                    contentDescription = "Import OPML button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.import_feeds_from_opml))
                            }
                            Divider()
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onExport()
                                }
                            ) {
                                Icon(
                                    Icons.Default.ImportExport,
                                    contentDescription = "Export OPML button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.export_feeds_to_opml))
                            }
                            Divider()
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onSettings()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.action_settings))
                            }
                            Divider()
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onSendFeedback()
                                }
                            ) {
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
        bottomBar = readAloudPlayer,
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
                    onClick = onMarkAllAsRead,
                    modifier = Modifier
                        .navigationBarsPadding()
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
            onRefresh = onRefreshVisible,
            indicatorPadding = padding,
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

        if (showEditDialog) {
            EditFeedDialog2(
                feeds = visibleFeeds.map { DeletableFeed(it.id, it.displayTitle) },
                onDismiss = { showEditDialog = false },
                onEdit = onEditFeed
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
            onEditFeed = {},
            onDelete = {},
            onSettings = {},
            onSendFeedback = {},
            readAloudPlayer = {},
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

@Immutable
data class FeedOrTag(
    val id: Long,
    val tag: String
)

val FeedOrTag.isFeed
    get() = id > ID_UNSET
