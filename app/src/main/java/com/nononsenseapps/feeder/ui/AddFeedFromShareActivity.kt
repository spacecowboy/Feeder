package com.nononsenseapps.feeder.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.ui.compose.editfeed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.navigation.AddFeedDestination
import com.nononsenseapps.feeder.ui.compose.searchfeed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.utils.rememberWindowSizeClass
import org.kodein.di.compose.withDI

/**
 * This activity should only be started via a Send (share) or Open URL/Text intent.
 */
class AddFeedFromShareActivity : DIAwareComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val initialFeedUrl =
            (intent?.dataString ?: intent?.getStringExtra(Intent.EXTRA_TEXT))?.trim()

        setContent {
            withDI {
                val viewModel: AddFeedFromShareActivityViewModel = DIAwareViewModel()
                val currentTheme by viewModel.currentTheme.collectAsState()
                val darkThemePreference by viewModel.darkThemePreference.collectAsState()
                val dynamicColors by viewModel.dynamicColors.collectAsState()

                FeederTheme(
                    currentTheme = currentTheme,
                    darkThemePreference = darkThemePreference,
                    dynamicColors = dynamicColors,
                ) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "search") {
                        composable("search") { backStackEntry ->
                            SearchFeedScreen(
                                onNavigateUp = {
                                    onNavigateUpFromIntentActivities()
                                },
                                initialFeedUrl = initialFeedUrl,
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
                            val windowSize = rememberWindowSizeClass()

                            CreateFeedScreen(
                                windowSize = windowSize,
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                createFeedScreenViewModel = backStackEntry.DIAwareViewModel(),
                                {
                                    finish()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Activity.onNavigateUpFromIntentActivities() {
    startActivity(
        Intent(
            this,
            MainActivity::class.java
        )
    )
    finish()
}
