package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.ui.compose.navdrawer.ListOfFeedsAndTags
import com.nononsenseapps.feeder.ui.compose.components.SwipeToRefreshLayout
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun FeedScreen(
    feedListViewModel: FeedListViewModel,
    feedItemsViewModel: FeedItemsViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(
        rememberDrawerState(initialValue = DrawerValue.Open)
    )

    val onlyUnread = remember {
        mutableStateOf(true)
    }

    val newestFirst = true // TODO from prefs

    // TODO prefs here ?
    val currentFeed = remember {
        mutableStateOf(ID_ALL_FEEDS to "")
    }

    val feedItems = feedItemsViewModel
        .getFlowOfDbPreviews(
            feedId = currentFeed.value.first,
            tag = currentFeed.value.second,
            onlyUnread = onlyUnread.value,
            newestFirst = newestFirst
        )
        .collectAsLazyPagingItems()

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
                        checked = onlyUnread.value,
                        onCheckedChange = { onlyUnread.value = !onlyUnread.value }
                    ) {
                        if (onlyUnread.value) {
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
                }
            )
        },
        drawerContent = {
            val feedsAndTags by feedListViewModel.liveFeedsAndTagsWithUnreadCounts.observeAsState(initial = emptyList())
            // TODO replace with remember?
            val expandedTags by feedListViewModel.expandedTags.collectAsState()

            ListOfFeedsAndTags(
                feedsAndTags = feedsAndTags,
                expandedTags = expandedTags,
                onItemClick = { item ->
                    coroutineScope.launch {
                        currentFeed.value = item.id to item.tag
                        scaffoldState.drawerState.close()
                    }
                },
                onToggleExpand = { tag -> feedListViewModel.toggleExpansion(tag) }
            )
        }
    ) {
        SwipeToRefreshLayout(
            refreshingState = false, // TODO live state
            onRefresh = { /*TODO*/ },
            refreshIndicator = { /*TODO*/ }
        ) {
            when (feedItems.loadState.append) {
                is LoadState.NotLoading -> {
                    Crossfade(targetState = feedItems.itemCount) { itemCount ->
                        when {
                            itemCount > 0 -> FeedList(feedItems, feedItemsViewModel)
                            else -> NothingToRead()
                        }
                    }
                }
                LoadState.Loading -> Text("Loading TODO")
                is LoadState.Error -> Text("Error happened TODO")
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
