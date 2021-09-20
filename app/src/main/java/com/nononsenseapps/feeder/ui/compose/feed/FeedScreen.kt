package com.nononsenseapps.feeder.ui.compose.feed

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DrawerValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberImagePainter
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
import com.nononsenseapps.feeder.archmodel.ScreenTitle
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import com.nononsenseapps.feeder.model.opml.exportOpml
import com.nononsenseapps.feeder.model.opml.importOpml
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerFeed
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerItemWithUnreadCount
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTag
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTop
import com.nononsenseapps.feeder.ui.compose.navdrawer.ListOfFeedsAndTags
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableReadAloudPlayer
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.util.openGitlabIssues
import kotlinx.coroutines.launch
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance
import org.threeten.bp.LocalDateTime

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FeedScreen(
    onItemClick: (Long) -> Unit,
    onAddFeed: (() -> Unit),
    onFeedEdit: (Long) -> Unit,
    onSettings: () -> Unit,
    onOpenFeedOrTag: (FeedOrTag) -> Unit,
    onDelete: (Iterable<Long>) -> Unit,
    feedScreenViewModel: FeedScreenViewModel,
    textToSpeechViewModel: TextToSpeechViewModel,
) {
    // Start collecting all flows
    val viewState: FeedScreenViewState? by feedScreenViewModel.viewState.collectAsState(initial = null)
    val pagedFeedItems = feedScreenViewModel.currentFeedListItems.collectAsLazyPagingItems()
    val expandedTags by feedScreenViewModel.expandedTags.collectAsState()

    // But don't do anything else unless we have a state to render
    if (viewState==null) {
        return
    }

    val nothingToRead by remember(pagedFeedItems) {
        derivedStateOf {
            pagedFeedItems.loadState.append.endOfPaginationReached
                    && pagedFeedItems.itemCount==0
        }
    }

    val refreshState = rememberSwipeRefreshState(viewState?.isRefreshing ?: false)

    val context = LocalContext.current
    val onSendFeedback = {
        context.startActivity(openGitlabIssues())
    }

    val di = LocalDI.current

    val opmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        if (uri!=null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                exportOpml(di, uri)
            }
        }
    }

    val opmlImporter = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri!=null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                importOpml(di, uri)
            }
        }
    }

    val isLightTheme = MaterialTheme.colors.isLight

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

    FeedScreen(
        // Avoiding rendering a title if no state yet, that's why screentitle must never be null
        screenTitle = (viewState?.screenTitle ?: ScreenTitle("")).title
            ?: stringResource(id = R.string.all_feeds),
        visibleFeeds = viewState?.visibleFeeds ?: emptyList(),
        feedsAndTags = viewState?.drawerItemsWithUnreadCounts ?: emptyList(),
        refreshState = refreshState,
        onRefreshVisible = {
            feedScreenViewModel.requestImmediateSyncOfCurrentFeedOrTag()
        },
        onRefreshAll = {
            feedScreenViewModel.requestImmediateSyncOfAll()
        },
        onlyUnread = viewState?.onlyUnread ?: true,
        onToggleOnlyUnread = { value ->
            feedScreenViewModel.setShowOnlyUnread(value)
        },
        onDrawerItemSelected = { id, tag ->
            onOpenFeedOrTag(FeedOrTag(id, tag))
        },
        onDelete = onDelete,
        onAddFeed = onAddFeed,
        onEditFeed = onFeedEdit,
        onSettings = onSettings,
        onSendFeedback = onSendFeedback,
        onImport = { opmlImporter.launch(arrayOf("text/plain", "text/xml", "text/opml", "*/*")) },
        onExport = { opmlExporter.launch("feeder-export-${LocalDateTime.now()}") },
        onMarkAllAsRead = {
            feedScreenViewModel.markAllAsRead()
        },
        showFloatingActionButton = viewState?.showFab ?: true,
        bottomBarVisible = textToSpeechViewModel.notStopped.value,
        expandedTags = expandedTags,
        onToggleTagExpansion = {
            feedScreenViewModel.toggleTagExpansion(it)
        },
        readAloudPlayer = {
            HideableReadAloudPlayer(textToSpeechViewModel)
        }
    ) { modifier, openNavDrawer ->
        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = nothingToRead,
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
                onOpenOtherFeed = openNavDrawer,
                onAddFeed = onAddFeed
            )
        }

        val bottomPadding by remember(textToSpeechViewModel.notStopped.value) {
            derivedStateOf {
                if (textToSpeechViewModel.notStopped.value) {
                    80.dp
                } else {
                    // Navigation bar is 48dp high
                    (80 + 48).dp
                }
            }
        }

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = !nothingToRead,
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = bottomPadding),
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
                            feedScreenViewModel.markAsUnread(
                                previewItem.id,
                                unread = !previewItem.unread
                            )
                        },
                        onlyUnread = viewState?.onlyUnread ?: true,
                        item = previewItem,
                        showThumbnail = viewState?.showThumbnails ?: true,
                        onMarkAboveAsRead = {
                            if (itemIndex > 0) {
                                feedScreenViewModel.markBeforeAsRead(itemIndex)
                            }
                        },
                        onMarkBelowAsRead = {
                            feedScreenViewModel.markAfterAsRead(itemIndex)
                        },
                        onItemClick = {
                            onItemClick(previewItem.id)
                        },
                        imagePainter = { imageUrl ->
                            Image(
                                painter = rememberImagePainter(
                                    data = imageUrl,
                                    builder = {
                                        this.placeholder(placeHolder)
                                            .error(placeHolder)
                                    },
                                ),
                                contentScale = ContentScale.Crop,
                                contentDescription = null,
                                modifier = Modifier
//                                    .width(64.dp)
//                                    .fillMaxHeight()
//                                    .padding(start = 4.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(16.0f / 9.0f)
                            )
                        }
                    )
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
    onAddFeed: () -> Unit,
    onEditFeed: (Long) -> Unit,
    onSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    showFloatingActionButton: Boolean,
    bottomBarVisible: Boolean,
    expandedTags: Set<String>,
    onToggleTagExpansion: (String) -> Unit,
    readAloudPlayer: @Composable () -> Unit,
    content: @Composable (Modifier, suspend () -> Unit) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(
        rememberDrawerState(initialValue = DrawerValue.Closed)
    )
    var showMenu by rememberSaveable {
        mutableStateOf(false)
    }
    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showEditDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val showingUnreadStateLabel = if (onlyUnread) {
        stringResource(R.string.showing_only_unread_articles)
    } else {
        stringResource(R.string.showing_all_articles)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        screenTitle,
                        maxLines = 2,
                    )
                },
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
                            contentDescription = stringResource(R.string.navigation_drawer_open)
                        )
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = onlyUnread,
                        onCheckedChange = onToggleOnlyUnread,
                        modifier = Modifier.semantics {
                            stateDescription = showingUnreadStateLabel
                        }
                    ) {
                        if (onlyUnread) {
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
                    IconButton(
                        onClick = onRefreshAll
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.synchronize_feeds)
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.open_menu),
                            )
                        }
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
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.add_feed))
                            }
                            DropdownMenuItem(
                                onClick = {
                                    if (visibleFeeds.size==1) {
                                        onEditFeed(visibleFeeds.first().id)
                                    } else {
                                        showEditDialog = true
                                    }
                                    showMenu = false
                                }
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
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
                                    contentDescription = null,
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
                                    contentDescription = null,
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
                                    contentDescription = null,
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
                                    contentDescription = null,
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
                                    contentDescription = null,
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
                expandedTags = expandedTags,
                onToggleTagExpansion = onToggleTagExpansion,
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
                        .let {
                            if (bottomBarVisible) {
                                it
                            } else {
                                it.navigationBarsPadding()
                            }
                        }
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
            onToggleOnlyUnread = {},
            onDrawerItemSelected = { _, _ -> },
            onDelete = {},
            onAddFeed = { },
            onEditFeed = {},
            onSettings = {},
            onSendFeedback = {},
            onImport = {},
            onExport = {},
            onMarkAllAsRead = {},
            showFloatingActionButton = true,
            bottomBarVisible = false,
            expandedTags = emptySet(),
            onToggleTagExpansion = {},
            readAloudPlayer = {}
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
    val tag: String,
)

val FeedOrTag.isFeed
    get() = id > ID_UNSET

