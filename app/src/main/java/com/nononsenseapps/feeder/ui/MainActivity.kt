package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import coil.ImageLoader
import coil.compose.LocalImageLoader
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.PrefValOpenWith
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import com.nononsenseapps.feeder.model.isOkToSyncAutomatically
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.ui.compose.editfeed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.editfeed.EditFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreenViewModel
import com.nononsenseapps.feeder.ui.compose.feed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.isFeed
import com.nononsenseapps.feeder.ui.compose.reader.ReaderScreen
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.util.DEEP_LINK_BASE_URI
import com.nononsenseapps.feeder.util.addDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import com.nononsenseapps.feeder.util.reportShortcutToFeedUsed
import kotlinx.coroutines.launch
import org.kodein.di.compose.withDI
import org.kodein.di.instance

class MainActivity : DIAwareComponentActivity() {
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()
    private val mainActivityViewModel: MainActivityViewModel by instance(arg = this)

    // This reference is only used for intent navigation
    private var navController: NavController? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {
            if (navController?.handleDeepLink(intent)!=true) {
                Log.e("FeederMainActivity", "In onNewIntent, navController rejected the intent")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivityViewModel.setResumeTime()
        maybeRequestSync()
    }

    private fun maybeRequestSync() = lifecycleScope.launch {
        if (mainActivityViewModel.shouldSyncOnResume) {
            if (isOkToSyncAutomatically(applicationContext)) {
                requestFeedSync(
                    di = di,
                    ignoreConnectivitySettings = false,
                    forceNetwork = false,
                    parallell = true
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val currentTheme by mainActivityViewModel.currentTheme.collectAsState()

            FeederTheme(
                currentTheme = currentTheme
            ) {
                withDI {
                    val imageLoader: ImageLoader by instance()
                    LocalImageLoader.provides(imageLoader)
                    appContent()
                }
            }
        }
    }

    @Composable
    fun appContent() {
        val readAloudViewModel: TextToSpeechViewModel = DIAwareViewModel()

        val navController = rememberNavController().also {
            this.navController = it
        }

        // TODO deleting the currently open feed should open all feeds
        // TODO handle deep link for item where default is open with custom tab or browser

        NavHost(navController, startDestination = "lastfeed") {
            composable("lastfeed") { backStackEntry ->
                navController.popBackStack()
                navController.navigate("feed?id=${mainActivityViewModel.currentFeedId}&tag=${mainActivityViewModel.currentFeedTag}")
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
                LaunchedEffect(
                    key1 = backStackEntry.arguments?.getLong("id"),
                    key2 = backStackEntry.arguments?.getString("tag")
                ) {
                    val feedId = backStackEntry.arguments?.getLong("id")
                        ?: mainActivityViewModel.currentFeedId
                    val tag = backStackEntry.arguments?.getString("tag")
                        ?: mainActivityViewModel.currentFeedTag

                    mainActivityViewModel.setCurrentFeedAndTag(feedId, tag)
                }

                FeedScreen(
                    backStackEntry = backStackEntry,
                    textToSpeechViewModel = readAloudViewModel
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
                ReaderScreen(
                    readerScreenViewModel = backStackEntry.DIAwareViewModel(),
                    readAloudViewModel = readAloudViewModel,
                    onNavigateToFeed = { feedId ->
                        navController.clearBackstack()
                        if (feedId!=null) {
                            navController.navigate("feed?id=$feedId")
                        } else {
                            navController.navigate("feed")
                        }
                    },
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
                EditFeedScreen(
                    onNavigateUp = {
                        navController.popBackStack()
                    },
                    onOk = { feedId ->
                        navController.clearBackstack()
                        navController.navigate("feed?id=$feedId")
                    },
                    editFeedScreenViewModel = backStackEntry.DIAwareViewModel()
                )
            }
            composable(
                "search/feed?feedUrl={feedUrl}",
                arguments = listOf(
                    navArgument("feedUrl") {
                        type = NavType.StringType
                        defaultValue = null
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                SearchFeedScreen(
                    onNavigateUp = {
                        navController.popBackStack()
                    },
                    initialFeedUrl = backStackEntry.arguments?.getString("feedUrl"),
                    searchFeedViewModel = backStackEntry.DIAwareViewModel()
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
                CreateFeedScreen(
                    onNavigateUp = {
                        navController.popBackStack()
                    },
                    createFeedScreenViewModel = backStackEntry.DIAwareViewModel(),
                ) { feedId ->
                    navController.clearBackstack()
                    navController.navigate("feed?id=$feedId")
                }
            }
            composable(
                "settings"
            ) { backStackEntry ->
                SettingsScreen(
                    onNavigateUp = {
                        navController.popBackStack()
                    },
                    settingsViewModel = backStackEntry.DIAwareViewModel(),
                )
            }
        }
    }

    @Composable
    fun FeedScreen(
        backStackEntry: NavBackStackEntry,
        textToSpeechViewModel: TextToSpeechViewModel,
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val feedScreenViewModel: FeedScreenViewModel = backStackEntry.DIAwareViewModel()
        val toolbarColor = MaterialTheme.colors.primarySurface.toArgb()

        FeedScreen(
            onItemClick = { itemId ->
                coroutineScope.launch {
                    val openArticleWith = feedScreenViewModel.getArticleOpener(itemId)
                    val link = feedScreenViewModel.getLink(itemId)
                    val openItemsByDefaultWith = feedScreenViewModel.itemOpener

                    when {
                        link!=null && (openArticleWith==PrefValOpenWith.OPEN_WITH_BROWSER
                                || openArticleWith==PrefValOpenWith.OPEN_WITH_DEFAULT && openItemsByDefaultWith==ItemOpener.DEFAULT_BROWSER) -> {
                            openLinkInBrowser(context, link)
                        }
                        link!=null && (openArticleWith==PrefValOpenWith.OPEN_WITH_CUSTOM_TAB
                                || openArticleWith==PrefValOpenWith.OPEN_WITH_DEFAULT && openItemsByDefaultWith==ItemOpener.CUSTOM_TAB) -> {
                            openLinkInCustomTab(context, link, toolbarColor)
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
                    if (feedOrTag.isFeed) {
                        addDynamicShortcutToFeed(
                            feedScreenViewModel.getFeedDisplayTitle() ?: "",
                            feedOrTag.id,
                            null
                        )
                        // Report shortcut usage
                        reportShortcutToFeedUsed(feedOrTag.id)
                    }
                }
            },
            feedScreenViewModel = feedScreenViewModel,
            textToSpeechViewModel = textToSpeechViewModel,
        )
    }
}

private fun NavController.clearBackstack() {
    var popped = true
    while (popped) {
        popped = popBackStack()
    }
}
