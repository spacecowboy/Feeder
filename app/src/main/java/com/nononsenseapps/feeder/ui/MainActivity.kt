package com.nononsenseapps.feeder.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.nononsenseapps.feeder.base.KodeinAwareComponentActivity
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.maxImageSize
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreen
import com.nononsenseapps.feeder.ui.compose.reader.ReaderScreen
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import org.kodein.di.direct
import org.kodein.di.generic.instance

@ExperimentalAnimationApi
class MainActivity : KodeinAwareComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FeederTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "feed") {
                    composable("feed") {
                        FeedScreen(navController = navController)
                    }
                    composable(
                        "reader/{itemId}",
                        arguments = listOf(
                            navArgument("itemId") { type = NavType.LongType }
                        )
                    ) { backStackEntry ->
                        ReaderScreen(
                            itemId = backStackEntry.arguments?.getLong("itemId") ?: -1L,
                            navController = navController,
                            maxImageSize = maxImageSize()
                        )
                    }
                }

            }
        }
    }
}
