package com.nononsenseapps.feeder.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.SearchFeedViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.ui.compose.feed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.feed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import org.kodein.di.compose.withDI

/**
 * This activity should only be started via a Send (share) or Open URL/Text intent.
 */
class AddFeedFromShareActivity : DIAwareComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val initialFeedUrl =
            (intent?.dataString ?: intent?.getStringExtra(Intent.EXTRA_TEXT))?.trim()

        setContent {
            withDI {
                val settingsViewModel: SettingsViewModel = DIAwareViewModel()
                val currentTheme by settingsViewModel.currentTheme.collectAsState()

                FeederTheme(
                    currentTheme = currentTheme
                ) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "search") {
                        composable("search") { backStackEntry ->
                            val searchFeedViewModel: SearchFeedViewModel =
                                backStackEntry.DIAwareViewModel()

                            SearchFeedScreen(
                                onNavigateUp = {
                                    onNavigateUpFromIntentActivities()
                                },
                                initialFeedUrl = initialFeedUrl,
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
                            val feedViewModel: FeedViewModel = backStackEntry.DIAwareViewModel()

                            CreateFeedScreen(
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                feedUrl = backStackEntry.arguments?.getString("feedUrl") ?: "",
                                feedTitle = backStackEntry.arguments?.getString("feedTitle") ?: "",
                                feedViewModel = feedViewModel
                            ) {
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun Activity.onNavigateUpFromIntentActivities() {
    startActivity(
        Intent(
            this,
            MainActivity::class.java
        ).apply {
            // Open existing app task and activity if it exists - otherwise start a new task
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    )
    finish()
}
