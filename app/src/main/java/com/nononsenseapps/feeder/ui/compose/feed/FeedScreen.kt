package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedUnreadCount
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.ui.compose.components.SwipeToRefreshLayout
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.navdrawer.ListOfFeedsAndTags
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.util.Prefs
import kotlinx.coroutines.launch
import org.kodein.di.compose.instance

@ExperimentalAnimationApi
@Composable
fun FeedScreen(
    navController: NavHostController
) {
    val feedListViewModel: FeedListViewModel = DIAwareViewModel()
    val feedItemsViewModel: FeedItemsViewModel = DIAwareViewModel()
    val settingsViewModel: SettingsViewModel = DIAwareViewModel()
    val prefs: Prefs by instance()

    val feedsAndTags by feedListViewModel.liveFeedsAndTagsWithUnreadCounts
        .observeAsState(initial = emptyList())

    var onlyUnread by remember {
        mutableStateOf(prefs.showOnlyUnread)
    }
    val newestFirst by settingsViewModel.liveIsNewestFirst.observeAsState(initial = true)
    var currentFeed by remember {
        mutableStateOf(prefs.lastOpenFeedId to (prefs.lastOpenFeedTag ?: ""))
    }

    val feedItems = feedItemsViewModel
        .getFlowOfDbPreviews(
            feedId = currentFeed.first,
            tag = currentFeed.second,
            onlyUnread = onlyUnread,
            newestFirst = newestFirst
        )
        .collectAsLazyPagingItems()

    FeedScreen(
        feedsAndTags = feedsAndTags,
        refreshing = false, // TODO live state
        onRefresh = { /*TODO*/ },
        onlyUnread = onlyUnread,
        onToggleOnlyUnread = { value ->
            // Remember for later
            prefs.showOnlyUnread = value
            // And trigger recompose
            onlyUnread = value
        },
        onDrawerItemSelected = { id, tag ->
            prefs.lastOpenFeedId = id
            prefs.lastOpenFeedTag = tag
            currentFeed = id to tag
        }
    ) {
        when (feedItems.loadState.append) {
            is LoadState.NotLoading -> {
                Crossfade(targetState = feedItems.itemCount) { itemCount ->
                    when {
                        itemCount > 0 -> FeedList(feedItems) { itemId ->
                            // TODO modify back stack?
                            navController.navigate("reader/$itemId")
                        }
                        else -> NothingToRead()
                    }
                }
            }
            LoadState.Loading -> Text("Loading TODO")
            is LoadState.Error -> Text("Error happened TODO")
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun FeedScreen(
    feedsAndTags: List<FeedUnreadCount>,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onlyUnread: Boolean,
    onToggleOnlyUnread: (Boolean) -> Unit,
    onDrawerItemSelected: (Long, String) -> Unit,
    content: @Composable () -> Unit
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
        SwipeToRefreshLayout(
            refreshingState = refreshing,
            onRefresh = onRefresh,
            refreshIndicator = { /*TODO*/ },
            content = content
        )

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
