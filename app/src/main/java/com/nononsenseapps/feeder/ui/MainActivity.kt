package com.nononsenseapps.feeder.ui

import android.graphics.Point
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.maxImageSize
import com.nononsenseapps.feeder.ui.compose.feed.EditFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.toEditableFeed
import com.nononsenseapps.feeder.ui.compose.feed.updateFrom
import com.nononsenseapps.feeder.ui.compose.reader.ReaderScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import kotlinx.coroutines.launch
import org.kodein.di.compose.withDI

@ExperimentalAnimationApi
class MainActivity : DIAwareComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent(maxImageSize())
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun MainActivity.AppContent(maxImageSize: Point) = withDI {
    FeederTheme {
        val navController = rememberNavController()
        val coroutineScope = rememberCoroutineScope()

        NavHost(navController, startDestination = "feed") {
            composable("feed") { backStackEntry ->
                FeedScreen(
                    onItemClick = { itemId ->
                        navController.navigate("reader/$itemId")
                    },
                    onFeedEdit = { feedId ->
                        navController.navigate("edit/feed/$feedId")
                    },
                    feedListViewModel = backStackEntry.DIAwareViewModel(),
                    feedItemsViewModel = backStackEntry.DIAwareViewModel(),
                    settingsViewModel = backStackEntry.DIAwareViewModel()
                )
            }
            composable(
                "reader/{itemId}",
                arguments = listOf(
                    navArgument("itemId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                // Necessary to use the backstackEntry so savedState matches lifecycle
                val feedItemViewModel: FeedItemViewModel = backStackEntry.DIAwareViewModel()

                feedItemViewModel.currentItemId = backStackEntry.arguments?.getLong("itemId")
                    ?: ID_UNSET

                coroutineScope.launch {
                    feedItemViewModel.markCurrentItemAsReadAndNotified()
                }

                ReaderScreen(
                    feedItemViewModel = feedItemViewModel
                ) {
                    navController.popBackStackOrNavigateTo(route = "feed")
                }
            }
            composable(
                "edit/feed/{feedId}",
                arguments = listOf(
                    navArgument("feedId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                // Necessary to use the backstackEntry so savedState matches lifecycle
                val feedViewModel: FeedViewModel = backStackEntry.DIAwareViewModel()

                feedViewModel.currentFeedId = backStackEntry.arguments?.getLong("feedId")
                    ?: ID_UNSET

                EditFeedScreen(
                    onNavigateUp = {
                        navController.popBackStackOrNavigateTo(route = "feed")
                    },
                    feedViewModel = feedViewModel
                )
            }
        }
    }
}

private fun NavController.popBackStackOrNavigateTo(route: String) {
    if (!popBackStack(route = route, inclusive = false)) {
        navigate(route = route) {
            launchSingleTop = true
        }
    }
}
