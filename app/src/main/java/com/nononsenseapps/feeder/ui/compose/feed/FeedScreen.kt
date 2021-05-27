package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.DrawerValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.painterResource
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
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.model.ApplicationState
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedUnreadCount
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.navdrawer.ListOfFeedsAndTags
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import kotlinx.coroutines.launch
import org.kodein.di.compose.LocalDI
import org.kodein.di.compose.instance

@ExperimentalAnimationApi
@Composable
fun FeedScreen(
    onItemClick: (Long) -> Unit,
    feedListViewModel: FeedListViewModel = DIAwareViewModel(),
    feedItemsViewModel: FeedItemsViewModel = DIAwareViewModel(),
    settingsViewModel: SettingsViewModel = DIAwareViewModel()
) {

    val feedsAndTags by feedListViewModel.liveFeedsAndTagsWithUnreadCounts
        .observeAsState(initial = emptyList())

    val onlyUnread by settingsViewModel.showOnlyUnread.collectAsState()
    val newestFirst by settingsViewModel.liveIsNewestFirst.observeAsState(initial = true)
    val currentFeed by settingsViewModel.currentFeedAndTag.collectAsState()

    val feedItems = feedItemsViewModel.getPreviewPager(
        feedId = currentFeed.first,
        tag = currentFeed.second,
        onlyUnread = onlyUnread,
        newestFirst = newestFirst
    )
        .collectAsLazyPagingItems()

    val applicationState: ApplicationState by instance()
    val isRefreshing by applicationState.isRefreshing.collectAsState()
    val refreshState = rememberSwipeRefreshState(isRefreshing)

    val di = LocalDI.current

    FeedScreen(
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
        }
    ) { openNavDrawer ->
        LazyColumn {
            items(feedItems) { previewItem ->
                if (previewItem == null) {
                    return@items
                }

                FeedItemPreview(item = previewItem, onItemClick = {
                    onItemClick(previewItem.id)
                })
            }

            when {
                feedItems.loadState.prepend is LoadState.Loading -> {
                    Log.d("JONAS", "Prepend")
                }
                feedItems.loadState.refresh is LoadState.Loading -> {
                    Log.d("JONAS", "Refresh")
                }
                feedItems.loadState.append is LoadState.Loading -> {
                    Log.d("JONAS", "Append")
                }
                feedItems.loadState.prepend is LoadState.Error -> {
                    item {
                        Text("Prepend Error! TODO")
                    }
                }
                feedItems.loadState.refresh is LoadState.Error -> {
                    item {
                        Text("Refresh Error! TODO")
                    }
                }
                feedItems.loadState.append is LoadState.Error -> {
                    item {
                        Text("Append Error! TODO")
                    }
                }
                feedItems.loadState.append.endOfPaginationReached -> {
                    // User has reached the end of the list, could insert something here

                    if (feedItems.itemCount == 0) {
                        Log.d("JONAS", "Nothing")
                        item {
                            NothingToRead(openNavDrawer) {}
                        }
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

@ExperimentalAnimationApi
@Composable
fun FeedScreen(
    feedsAndTags: List<FeedUnreadCount>,
    refreshState: SwipeRefreshState,
    onRefresh: () -> Unit,
    onlyUnread: Boolean,
    onToggleOnlyUnread: (Boolean) -> Unit,
    onDrawerItemSelected: (Long, String) -> Unit,
    content: @Composable (suspend () -> Unit) -> Unit
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
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Drawer toggle",
                        modifier = Modifier
                            .clickable {
                                coroutineScope.launch {
                                    scaffoldState.drawerState.open()
                                }
                            }
                    )
                },
                actions = {
                    IconToggleButton(
                        checked = onlyUnread,
                        onCheckedChange = onToggleOnlyUnread
                    ) {
                        if (onlyUnread) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_visibility_off_white_24dp),
                                contentDescription = stringResource(id = R.string.show_all_items)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_visibility_white_24dp),
                                contentDescription = stringResource(id = R.string.show_unread_items)
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Open menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(onClick = { /* Handle refresh! */ }) {
                                Text("Refresh")
                            }
                            DropdownMenuItem(onClick = { showDeleteDialog = true }) {
                                Text(stringResource(id = R.string.delete_feed))
                            }
                            DropdownMenuItem(onClick = { /* Handle settings! */ }) {
                                Text("Settings")
                            }
                            Divider()
                            DropdownMenuItem(onClick = { /* Handle send feedback! */ }) {
                                Text("Send Feedback")
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
    ) {
        SwipeRefresh(
            state = refreshState,
            onRefresh = onRefresh
        ) {
            content {
                scaffoldState.drawerState.open()
            }
        }

        if (showDeleteDialog) {
            DeleteFeedDialog(
                feeds = listOf(/*TODO*/),
                onDismiss = { showDeleteDialog = false }) {
                // TODO
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FeederTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Feeder") },
                    navigationIcon = {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Drawer toggle"
                        )
                    }
                )
            },
            drawerContent = {
                Text("The Drawer")
            }
        ) {
            // A surface container using the 'background' color from the theme
            Surface(color = MaterialTheme.colors.background) {
                Text("Hello Android")
            }
        }
    }
}
