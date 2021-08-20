package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.ApplicationState
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.SearchFeedViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import com.nononsenseapps.feeder.model.cancelNotification
import com.nononsenseapps.feeder.ui.compose.feed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.EditFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.FeedOrTag
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.isFeed
import com.nononsenseapps.feeder.ui.compose.reader.ReaderScreen
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.util.DEEP_LINK_BASE_URI
import com.nononsenseapps.feeder.util.ItemOpener
import com.nononsenseapps.feeder.util.PrefValOpenWith
import com.nononsenseapps.feeder.util.addDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import com.nononsenseapps.feeder.util.reportShortcutToFeedUsed
import kotlinx.coroutines.launch
import org.kodein.di.compose.withDI
import org.kodein.di.instance

class MainActivity : DIAwareComponentActivity() {
    private val applicationState: ApplicationState by instance()
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()

    // This reference is only used for intent navigation
    private var navController: NavController? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {
            if (navController?.handleDeepLink(intent) != true) {
                Log.e("FeederMainActivity", "In onNewIntent, navController rejected the intent")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applicationState.setResumeTime()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            appContent()
        }
    }

    @Composable
    fun appContent() {
        withDI {
            val settingsViewModel: SettingsViewModel = DIAwareViewModel()
            val readAloudViewModel: TextToSpeechViewModel = DIAwareViewModel()
            val currentTheme by settingsViewModel.currentTheme.collectAsState()

            FeederTheme(
                currentTheme = currentTheme
            ) {
                val navController = rememberNavController().also {
                    this.navController = it
                }

                // TODO deleting the currently open feed should open all feeds
                // TODO handle deep link for item where default is open with custom tab or browser

                NavHost(navController, startDestination = "lastfeed") {
                    composable("lastfeed") { backStackEntry ->
                        val feedOrTag: FeedOrTag = remember {
                            FeedOrTag(
                                settingsViewModel.currentFeedAndTag.value.first,
                                settingsViewModel.currentFeedAndTag.value.second
                            )
                        }

                        FeedScreen(
                            feedOrTag = feedOrTag,
                            backStackEntry = backStackEntry,
                            settingsViewModel = settingsViewModel,
                            readAloudViewModel = readAloudViewModel
                        )
                    }
                    composable(
                        "feed?id={id}&tag={tag}",
                        arguments = listOf(
                            navArgument("id") {
                                type = NavType.LongType
                                defaultValue = ID_UNSET
                            },
                            navArgument("tag") {
                                type = NavType.StringType
                                defaultValue = ""
                            }
                        ),
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = "$DEEP_LINK_BASE_URI/feed?id={id}&tag={tag}"
                            }
                        )
                    ) { backStackEntry ->
                        val feedOrTag: FeedOrTag = remember(
                            key1 = backStackEntry.arguments?.getLong("id"),
                            key2 = backStackEntry.arguments?.getString("tag")
                        ) {
                            val feedId = (backStackEntry.arguments?.getLong("id")
                                ?: ID_UNSET).let { feedId ->
                                if (feedId == ID_UNSET) {
                                    settingsViewModel.currentFeedAndTag.value.first
                                } else {
                                    feedId
                                }
                            }

                            val tag =
                                (backStackEntry.arguments?.getString("tag") ?: "").let { tag ->
                                    if (tag.isEmpty()) {
                                        settingsViewModel.currentFeedAndTag.value.second
                                    } else {
                                        tag
                                    }
                                }

                            settingsViewModel.setCurrentFeedAndTag(feedId, tag)
                            FeedOrTag(feedId, tag)
                        }



                        FeedScreen(
                            feedOrTag = feedOrTag,
                            backStackEntry = backStackEntry,
                            settingsViewModel = settingsViewModel,
                            readAloudViewModel = readAloudViewModel
                        )
                    }
                    composable(
                        "reader/{itemId}",
                        arguments = listOf(
                            navArgument("itemId") { type = NavType.LongType }
                        ),
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = "$DEEP_LINK_BASE_URI/article/{itemId}"
                            }
                        )
                    ) { backStackEntry ->
                        // Necessary to use the backstackEntry so savedState matches lifecycle
                        val feedItemViewModel: FeedItemViewModel =
                            backStackEntry.DIAwareViewModel()

                        val itemId = backStackEntry.arguments?.getLong("itemId")
                            ?: ID_UNSET

                        feedItemViewModel.currentItemId = itemId

                        val context = LocalContext.current

                        LaunchedEffect(key1 = itemId) {
                            feedItemViewModel.markAsReadAndNotified(itemId)
                            cancelNotification(context, itemId)
                        }

                        ReaderScreen(
                            feedItemViewModel = feedItemViewModel,
                            settingsViewModel = settingsViewModel,
                            readAloudViewModel = readAloudViewModel,
                        ) {
                            if (!navController.popBackStack()) {
                                navController.navigate("lastfeed")
                            }
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

                        val feedId = backStackEntry.arguments?.getLong("feedId")
                            ?: ID_UNSET

                        if (feedId <= ID_UNSET) {
                            error("Can't edit a feed with no id")
                        }

                        feedViewModel.currentFeedId = feedId

                        val feed by feedViewModel.currentLiveFeed.observeAsState()

                        feed?.let { feed ->
                            EditFeedScreen(
                                feed = feed,
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                onOk = { feedId ->
                                    navController.clearBackstack()
                                    navController.navigate("feed?id=$feedId")
                                },
                                feedViewModel = feedViewModel
                            )
                        }
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
                                navController.popBackStack()
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
                                navController.popBackStack()
                            },
                            feedUrl = backStackEntry.arguments?.getString("feedUrl") ?: "",
                            feedTitle = backStackEntry.arguments?.getString("feedTitle") ?: "",
                            feedViewModel = feedViewModel
                        ) { feedId ->
                            navController.clearBackstack()
                            navController.navigate("feed?id=$feedId")
                        }
                    }
                    composable(
                        "settings"
                    ) {
                        SettingsScreen(
                            onNavigateUp = {
                                navController.popBackStack()
                            },
                            settingsViewModel = settingsViewModel,
                            applicationState = applicationState
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun FeedScreen(
        feedOrTag: FeedOrTag,
        backStackEntry: NavBackStackEntry,
        settingsViewModel: SettingsViewModel,
        readAloudViewModel: TextToSpeechViewModel,
    ) {
        val feedItemViewModel: FeedItemViewModel =
            backStackEntry.DIAwareViewModel()
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val feedItemsViewModel: FeedItemsViewModel = backStackEntry.DIAwareViewModel()

        FeedScreen(
            feedOrTag = feedOrTag,
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
                            navController?.navigate("reader/$itemId")
                        }
                    }
                }
            },
            onAddFeed = {
                navController?.navigate("search/feed")
            },
            onFeedEdit = { feedId ->
                navController?.navigate("edit/feed/$feedId")
            },
            onSettings = {
                navController?.navigate("settings")
            },
            onOpenFeedOrTag = { feedOrTag ->
                navController?.popBackStack()
                navController?.navigate("feed?id=${feedOrTag.id}&tag=${feedOrTag.tag}") {
                    launchSingleTop = true
                }
                applicationCoroutineScope.launch {
                    settingsViewModel.setCurrentFeedAndTag(feedOrTag.id, feedOrTag.tag)

                    if (feedOrTag.isFeed) {
                        addDynamicShortcutToFeed(
                            feedItemsViewModel.getFeedDisplayTitle(feedOrTag.id) ?: "",
                            feedOrTag.id,
                            null
                        )
                        // Report shortcut usage
                        reportShortcutToFeedUsed(feedOrTag.id)
                    }
                }
            },
            feedListViewModel = backStackEntry.DIAwareViewModel(),
            feedItemsViewModel = feedItemsViewModel,
            settingsViewModel = settingsViewModel,
            readAloudViewModel = readAloudViewModel,
        )
    }
}

private fun NavController.clearBackstack() {
    var popped = true
    while (popped) {
        popped = popBackStack()
    }
}
