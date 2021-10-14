package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.LocalImageLoader
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.PrefValOpenWith
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import com.nononsenseapps.feeder.model.isOkToSyncAutomatically
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.ui.compose.editfeed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.editfeed.EditFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreenViewModel
import com.nononsenseapps.feeder.ui.compose.feed.isFeed
import com.nononsenseapps.feeder.ui.compose.navigation.AddFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.EditFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.FeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.ReaderDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SearchFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SettingsDestination
import com.nononsenseapps.feeder.ui.compose.reader.ReaderScreen
import com.nononsenseapps.feeder.ui.compose.searchfeed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
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
            val darkThemePreference by mainActivityViewModel.darkThemePreference.collectAsState()

            FeederTheme(
                currentTheme = currentTheme,
                darkThemePreference = darkThemePreference
            ) {
                withDI {
                    val imageLoader: ImageLoader by instance()
                    CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                        appContent()
                    }
                }
            }
        }
    }

    @Composable
    fun appContent() {
        val readAloudViewModel: TextToSpeechViewModel = DIAwareViewModel()

        val navController = rememberNavController().also {
            if (this.navController==null) {
                this.navController = it
            }
        }

        // TODO handle deep link for item where default is open with custom tab or browser

        val currentFeedAndTag by mainActivityViewModel.currentFeedAndTag.collectAsState()

        NavHost(navController, startDestination = "lastfeed") {
            composable("lastfeed") {
                LaunchedEffect(Unit) {
                    navController.popEntireBackStack()
                    FeedDestination.navigate(
                        navController,
                        currentFeedAndTag.first,
                        currentFeedAndTag.second
                    )
                }
            }
            composable(
                route = FeedDestination.route,
                arguments = FeedDestination.arguments,
                deepLinks = FeedDestination.deepLinks,
            ) { backStackEntry ->
                val feedId = backStackEntry.arguments?.getLong("id")
                    ?: error("Missing mandatory argument: id")
                val tag = backStackEntry.arguments?.getString("tag")
                    ?: error("Missing mandatory argument: tag")

                LaunchedEffect(feedId, tag) {
                    Log.d("JONAS56", "Received tag: '$tag'")
                    mainActivityViewModel.setCurrentFeedAndTag(feedId, tag)
                }

                FeedScreen(
                    navController = navController,
                    backStackEntry = backStackEntry,
                    textToSpeechViewModel = readAloudViewModel
                )
            }
            composable(
                route = ReaderDestination.route,
                arguments = ReaderDestination.arguments,
                deepLinks = ReaderDestination.deepLinks,
            ) { backStackEntry ->
                ReaderScreen(
                    readerScreenViewModel = backStackEntry.DIAwareViewModel(),
                    readAloudViewModel = readAloudViewModel,
                    onNavigateToFeed = { feedId ->
                        if (feedId!=null) {
                            navController.popEntireBackStack()
                            FeedDestination.navigate(
                                navController,
                                feedId
                            )
                        } else {
                            navController.popEntireBackStack()
                            FeedDestination.navigate(
                                navController,
                                currentFeedAndTag.first,
                                currentFeedAndTag.second
                            )
                        }
                    },
                ) {
                    navController.popEntireBackStack()
                    FeedDestination.navigate(
                        navController,
                        currentFeedAndTag.first,
                        currentFeedAndTag.second
                    )
                }
            }
            composable(
                route = EditFeedDestination.route,
                arguments = EditFeedDestination.arguments,
                deepLinks = EditFeedDestination.deepLinks,
            ) { backStackEntry ->
                EditFeedScreen(
                    onNavigateUp = {
                        navController.popBackStack()
                    },
                    onOk = { feedId ->
                        navController.popEntireBackStack()
                        FeedDestination.navigate(
                            navController,
                            feedId
                        )
                    },
                    editFeedScreenViewModel = backStackEntry.DIAwareViewModel()
                )
            }
            composable(
                route = SearchFeedDestination.route,
                arguments = SearchFeedDestination.arguments,
                deepLinks = SearchFeedDestination.deepLinks,
            ) { backStackEntry ->
                SearchFeedScreen(
                    onNavigateUp = {
                        navController.popBackStack()
                    },
                    initialFeedUrl = backStackEntry.arguments?.getString("feedUrl"),
                    searchFeedViewModel = backStackEntry.DIAwareViewModel()
                ) {
                    AddFeedDestination.navigate(
                        navController,
                        feedUrl = it.url,
                        feedTitle = it.title
                    )
                }
            }
            composable(
                route = AddFeedDestination.route,
                arguments = AddFeedDestination.arguments,
                deepLinks = AddFeedDestination.deepLinks,
            ) { backStackEntry ->
                CreateFeedScreen(
                    onNavigateUp = {
                        navController.popBackStack()
                    },
                    createFeedScreenViewModel = backStackEntry.DIAwareViewModel(),
                ) { feedId ->
                    navController.popEntireBackStack()
                    FeedDestination.navigate(
                        navController,
                        feedId
                    )
                }
            }
            composable(
                route = SettingsDestination.route,
                arguments = SettingsDestination.arguments,
                deepLinks = SettingsDestination.deepLinks,
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
        navController: NavController,
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

                    feedScreenViewModel.markAsReadAndNotified(itemId)

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
                            ReaderDestination.navigate(navController, itemId)
                        }
                    }
                }
            },
            onAddFeed = {
                SearchFeedDestination.navigate(navController)
            },
            onFeedEdit = { feedId ->
                EditFeedDestination.navigate(navController, feedId)
            },
            onSettings = {
                SettingsDestination.navigate(navController)
            },
            onOpenFeedOrTag = { feedOrTag ->
                navController.popEntireBackStack()
                FeedDestination.navigate(
                    navController,
                    feedOrTag.id,
                    feedOrTag.tag
                )
                applicationCoroutineScope.launch {
                    if (feedOrTag.isFeed) {
                        addDynamicShortcutToFeed(
                            feedScreenViewModel.getFeedDisplayTitle(feedOrTag.id) ?: "",
                            feedOrTag.id,
                            null
                        )
                        // Report shortcut usage
                        reportShortcutToFeedUsed(feedOrTag.id)
                    }
                }
            },
            onDelete = { feeds ->
                feedScreenViewModel.deleteFeeds(feeds.toList())
                navController.popEntireBackStack()
                FeedDestination.navigate(
                    navController,
                    ID_ALL_FEEDS
                )
            },
            feedScreenViewModel = feedScreenViewModel,
            textToSpeechViewModel = textToSpeechViewModel,
        )
    }
}

private fun NavController.popEntireBackStack() {
    var popped = true
    while (popped) {
        popped = popBackStack()
    }
}
