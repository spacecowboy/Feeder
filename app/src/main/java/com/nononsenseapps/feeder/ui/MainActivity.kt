package com.nononsenseapps.feeder.ui

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.ApplicationState
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.SearchFeedViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.maxImageSize
import com.nononsenseapps.feeder.ui.compose.feed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.EditFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.reader.ReaderScreen
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import org.kodein.di.compose.withDI
import org.kodein.di.instance

@ExperimentalAnimationApi
class MainActivity : DIAwareComponentActivity() {
    val applicationState: ApplicationState by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent(maxImageSize())
        }
    }

    override fun onResume() {
        super.onResume()

        applicationState.setResumeTime()
    }
}

@ExperimentalAnimationApi
@Composable
fun MainActivity.AppContent(maxImageSize: Point) = withDI {
    val settingsViewModel: SettingsViewModel = DIAwareViewModel()

    val currentTheme by settingsViewModel.currentTheme.collectAsState()

    FeederTheme(
        currentTheme = currentTheme
    ) {
        val navController = rememberNavController()

        // TODO implement intent handling for android.intent.action.MANAGE_NETWORK_USAGE
        // TODO implement intent for adding a feed
        // TODO verify currentFeed is updated on delete

        NavHost(navController, startDestination = "feed") {
            composable(
                "feed"
            ) { backStackEntry ->
                FeedScreen(
                    onItemClick = { itemId ->
                        navController.navigate("reader/$itemId")
                    },
                    onAddFeed = {
                        navController.navigate("search/feed")
                    },
                    onFeedEdit = { feedId ->
                        navController.navigate("edit/feed/$feedId")
                    },
                    onSettings = {
                        navController.navigate("settings")
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

                feedItemViewModel.markCurrentItemAsReadAndNotified()

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

                // TODO change to correct thign in settingsviemodel
                feedViewModel.currentFeedId = backStackEntry.arguments?.getLong("feedId")
                    ?: ID_UNSET

                EditFeedScreen(
                    onNavigateUp = {
                        navController.popBackStackOrNavigateTo(route = "feed")
                    },
                    feedViewModel = feedViewModel
                )
            }
            composable(
                "search/feed?feedUrl={feedUrl}",
                arguments = listOf(
                    navArgument("feedUrl") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                // Necessary to use the backstackEntry so savedState matches lifecycle
                val searchFeedViewModel: SearchFeedViewModel = backStackEntry.DIAwareViewModel()

                SearchFeedScreen(
                    onNavigateUp = {
                        navController.popBackStackOrNavigateTo(route = "feed")
                    },
                    initialFeedUrl = backStackEntry.arguments?.getString("feedUrl"),
                    searchFeedViewModel = searchFeedViewModel
                ) {
                    navController.navigate("add/feed?feedUrl=${it.url}&feedTitle=${it.title}")
                }
            }
            composable(
                "add/feed?feedUrl={feedUrl}&feedTitle={feedTitle}",
                arguments = listOf(
                    navArgument("feedUrl") {
                        type = NavType.StringType
                    },
                    navArgument("feedTitle") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                // Necessary to use the backstackEntry so savedState matches lifecycle
                val feedViewModel: FeedViewModel = backStackEntry.DIAwareViewModel()

                CreateFeedScreen(
                    onNavigateUp = {
                        navController.popBackStackOrNavigateTo(route = "feed")
                    },
                    feedUrl = backStackEntry.arguments?.getString("feedUrl") ?: "",
                    feedTitle = backStackEntry.arguments?.getString("feedTitle") ?: "",
                    feedViewModel = feedViewModel
                ) { feedId ->
                    settingsViewModel.setCurrentFeedAndTag(feedId, "")
                    navController.navigate(
                        "feed"
                    ) {
                        launchSingleTop = true
                    }
                }
            }
            composable(
                "settings"
            ) {
                SettingsScreen(
                    onNavigateUp = {
                        navController.popBackStackOrNavigateTo(route = "feed")
                    },
                    settingsViewModel = settingsViewModel,
                    applicationState = applicationState
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
