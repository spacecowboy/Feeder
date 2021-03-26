package com.nononsenseapps.feeder.ui.compose.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.ui.compose.components.FeedList
import com.nononsenseapps.feeder.ui.compose.components.ListOfFeedsAndTags
import com.nononsenseapps.feeder.ui.compose.components.NothingToRead
import com.nononsenseapps.feeder.ui.compose.components.SwipeToRefreshLayout
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun FeedScreen(
    feedId: Long,
    tag: String,
    feedListViewModel: FeedListViewModel,
    feedItemsViewModel: FeedItemsViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(
        rememberDrawerState(initialValue = DrawerValue.Open)
    )
    val feedItems by feedItemsViewModel.getLiveDbPreviews(feedId = feedId, tag = tag).observeAsState()

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
                }
            )
        },
        drawerContent = {
            ListOfFeedsAndTags(feedListViewModel)
        }
    ) {
        SwipeToRefreshLayout(
            refreshingState = false, // TODO live state
            onRefresh = { /*TODO*/ },
            refreshIndicator = { /*TODO*/ }
        ) {
            if (feedItems?.isNotEmpty() == true) {
                FeedList(feedItems!!, feedItemsViewModel)
            } else {
                NothingToRead()
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
