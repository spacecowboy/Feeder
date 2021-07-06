package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.content.Intent.ACTION_MANAGE_NETWORK_USAGE
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
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
import com.nononsenseapps.feeder.model.NavigationCurrentFeed
import com.nononsenseapps.feeder.model.NavigationSearch
import com.nononsenseapps.feeder.model.NavigationSettings
import com.nononsenseapps.feeder.model.NavigationTarget
import com.nononsenseapps.feeder.model.SearchFeedViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.ui.compose.feed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.EditFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.reader.ReaderScreen
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.util.ItemOpener
import com.nononsenseapps.feeder.util.PrefValOpenWith
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import kotlinx.coroutines.launch
import org.kodein.di.compose.withDI
import org.kodein.di.instance

@ExperimentalAnimationApi
class MainActivity : DIAwareComponentActivity() {
    private val applicationState: ApplicationState by instance()

    // This reference is only used for intent navigation
    private var navController: NavController? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {
            if (navController?.handleDeepLink(intent) != true) {
                parseIntentToState(it)?.let { target ->
                    applicationState.setIntentNavigationTarget(target)
                    // Navcontroller receives intent automatically on startup but not here
                    // edit: LaunchedEffect should handle this
//                    navController?.navigate(target.route) {
//                        target.navOptions(this)
//                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applicationState.setResumeTime()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            parseIntentToState(it)?.let { target ->
                applicationState.setIntentNavigationTarget(target)
            }
        }

        setContent {
            appContent()
        }
    }

    /**
     * This parses external intents only - deep links are handled directly by navcontroller
     */
    fun parseIntentToState(intent: Intent): NavigationTarget? = when (intent.action) {
        ACTION_MANAGE_NETWORK_USAGE -> NavigationSettings().also {
            Log.d("JONAS66", "Navigation: ${it.route}")
        }
        else -> {
            (intent.dataString ?: intent.getStringExtra(Intent.EXTRA_TEXT))?.trim()
                ?.let { feedUrl ->
                    NavigationSearch(feedUrl).also {
                        Log.d("JONAS66", "Navigation: ${it.route}")
                    }
                }
        }
    }

    @Composable
    fun appContent() {
        withDI {
            val settingsViewModel: SettingsViewModel = DIAwareViewModel()
            val currentTheme by settingsViewModel.currentTheme.collectAsState()

            FeederTheme(
                currentTheme = currentTheme
            ) {
                val navController = rememberNavController().also {
                    this.navController = it
                }

                val intentNavigationTarget by applicationState.intentNavigationTarget

                // TODO implement intent handling for android.intent.action.MANAGE_NETWORK_USAGE
                // TODO implement intent for adding a feed
                // TODO verify currentFeed is updated on delete
                // TODO add a specific navigation target for opening items from intent where navigation is moved to reader/list and correct custom tab etc is opened

                NavHost(navController, startDestination = "feed") {
                    composable(
                        "feed"
                    ) { backStackEntry ->
                        val feedItemViewModel: FeedItemViewModel =
                            backStackEntry.DIAwareViewModel()
                        val context = LocalContext.current
                        val coroutineScope = rememberCoroutineScope()

                        FeedScreen(
                            onItemClick = { itemId ->
                                coroutineScope.launch {
                                    val openArticleWith =
                                        feedItemViewModel.getOpenArticleWith(itemId)
                                    val link = feedItemViewModel.getLink(itemId)
                                    val openItemsByDefaultWith =
                                        settingsViewModel.itemOpener.value

                                    when {
                                        link != null && (openArticleWith == PrefValOpenWith.OPEN_WITH_BROWSER
                                                || openArticleWith == PrefValOpenWith.OPEN_WITH_DEFAULT && openItemsByDefaultWith == ItemOpener.DEFAULT_BROWSER) -> {
                                            openLinkInBrowser(context, link)
                                        }
                                        link != null && (openArticleWith == PrefValOpenWith.OPEN_WITH_CUSTOM_TAB
                                                || openArticleWith == PrefValOpenWith.OPEN_WITH_DEFAULT && openItemsByDefaultWith == ItemOpener.CUSTOM_TAB) -> {
                                            openLinkInCustomTab(context, link, itemId)
                                        }
                                        else -> {
                                            navController.navigate("reader/$itemId")
                                        }
                                    }
                                }
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
                            settingsViewModel = settingsViewModel
                        )
                    }
                    composable(
                        "reader/{itemId}",
                        arguments = listOf(
                            navArgument("itemId") { type = NavType.LongType }
                        )
                    ) { backStackEntry ->
                        // Necessary to use the backstackEntry so savedState matches lifecycle
                        val feedItemViewModel: FeedItemViewModel =
                            backStackEntry.DIAwareViewModel()

                        feedItemViewModel.currentItemId =
                            backStackEntry.arguments?.getLong("itemId")
                                ?: ID_UNSET

                        feedItemViewModel.markCurrentItemAsReadAndNotified()

                        ReaderScreen(
                            feedItemViewModel = feedItemViewModel,
                            settingsViewModel = settingsViewModel
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
                        feedViewModel.currentFeedId =
                            backStackEntry.arguments?.getLong("feedId")
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
                        val searchFeedViewModel: SearchFeedViewModel =
                            backStackEntry.DIAwareViewModel()

                        SearchFeedScreen(
                            onNavigateUp = {
                                navController.popBackStackOrNavigateTo(route = "feed")
                            },
                            initialFeedUrl = backStackEntry.arguments?.getString("feedUrl").also {
                                Log.d("JONAS66", "arg is $it")
                            },
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
                            navController.popBackStackOrNavigateTo("feed")
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

                LaunchedEffect(key1 = intentNavigationTarget.route) {
                    Log.d("JONAS66", "Launched navigate to ${intentNavigationTarget.route}")
                    // This needs to be done after initialization of graph - starting route can't
                    // take arguments??
                    if (intentNavigationTarget !is NavigationCurrentFeed) {
                        // No need to do anything if it's the default route
                        navController.navigate(intentNavigationTarget.route) {
                            intentNavigationTarget.navOptions(this)
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}

private fun NavController.popBackStackOrNavigateTo(route: String) {
    // TODO does not handle the case where an external intent comes
    if (!popBackStack(route = route, inclusive = false)) {
        navigate(route = route) {
            launchSingleTop = true
        }
    }
}
