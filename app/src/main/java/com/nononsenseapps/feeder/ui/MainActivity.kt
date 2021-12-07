package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.LocalImageLoader
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.model.isOkToSyncAutomatically
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.ui.compose.editfeed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.editfeed.EditFeedScreen
import com.nononsenseapps.feeder.ui.compose.feedarticle.FeedArticleScreen
import com.nononsenseapps.feeder.ui.compose.navigation.AddFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.ArticleDestination
import com.nononsenseapps.feeder.ui.compose.navigation.EditFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.FeedArticleDestination
import com.nononsenseapps.feeder.ui.compose.navigation.FeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SearchFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SettingsDestination
import com.nononsenseapps.feeder.ui.compose.searchfeed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.utils.rememberWindowSizeClass
import kotlinx.coroutines.launch
import org.kodein.di.compose.withDI
import org.kodein.di.instance

class MainActivity : DIAwareComponentActivity() {
    private val mainActivityViewModel: MainActivityViewModel by instance(arg = this)

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
        mainActivityViewModel.setResumeTime()
        maybeRequestSync()
    }

    private fun maybeRequestSync() = lifecycleScope.launch {
        if (mainActivityViewModel.shouldSyncOnResume) {
            if (isOkToSyncAutomatically(applicationContext)) {
                requestFeedSync(
                    di = di,
                    forceNetwork = false,
                    parallel = true
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityViewModel.ensurePeriodicSyncConfigured()

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
        val windowSize = rememberWindowSizeClass()

        val navController = rememberNavController().also {
            if (this.navController == null) {
                this.navController = it
            }
        }

        NavHost(navController, startDestination = FeedArticleDestination.route) {
            composable(
                route = FeedArticleDestination.route,
                arguments = FeedArticleDestination.arguments,
                deepLinks = FeedArticleDestination.deepLinks,
            ) { backStackEntry ->
                FeedArticleScreen(
                    windowSize = windowSize,
                    navController = navController,
                    viewModel = backStackEntry.DIAwareViewModel(),
                )
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
                    mainActivityViewModel.setCurrentFeedAndTag(feedId = feedId, tag = tag)
                    FeedArticleDestination.navigate(navController)
                }
            }
            composable(
                route = ArticleDestination.route,
                arguments = ArticleDestination.arguments,
                deepLinks = ArticleDestination.deepLinks,
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId")
                    ?: error("Missing mandatory argument: itemId")

                // TODO should this also set current feed? On tablet it might feel weird that
                // the article is from one feed and the list displays a different feed
                LaunchedEffect(itemId) {
                    mainActivityViewModel.setCurrentArticle(itemId = itemId)
                    FeedArticleDestination.navigate(navController)
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
                        mainActivityViewModel.setCurrentFeedAndTag(feedId = feedId, tag = "")
                        FeedArticleDestination.navigate(navController)
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
                    mainActivityViewModel.setCurrentFeedAndTag(feedId = feedId, tag = "")
                    FeedArticleDestination.navigate(navController)
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
}
