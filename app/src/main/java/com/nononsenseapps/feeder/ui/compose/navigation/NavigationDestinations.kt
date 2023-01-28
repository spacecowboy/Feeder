package com.nononsenseapps.feeder.ui.compose.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavArgumentBuilder
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.composable
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.NavigationDeepLinkViewModel
import com.nononsenseapps.feeder.ui.compose.editfeed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.editfeed.CreateFeedScreenViewModel
import com.nononsenseapps.feeder.ui.compose.editfeed.EditFeedScreen
import com.nononsenseapps.feeder.ui.compose.editfeed.EditFeedScreenViewModel
import com.nononsenseapps.feeder.ui.compose.feed.FeedScreen
import com.nononsenseapps.feeder.ui.compose.feedarticle.ArticleScreen
import com.nononsenseapps.feeder.ui.compose.searchfeed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.sync.SyncScreen
import com.nononsenseapps.feeder.ui.compose.sync.SyncScreenViewModel
import com.nononsenseapps.feeder.util.DEEP_LINK_BASE_URI
import com.nononsenseapps.feeder.util.logDebug
import com.nononsenseapps.feeder.util.urlEncode
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.instance

private const val LOG_TAG = "FEEDER_NAV"

@OptIn(ExperimentalAnimationApi::class)
sealed class NavigationDestination(
    protected val path: String,
    protected val navArguments: List<NavigationArgument>,
    val deepLinks: List<NavDeepLink>,
    private val enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = {
        fadeIn()
    },
    private val exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = {
        fadeOut()
    },
    private val popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = {
        fadeIn()
    },
    private val popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = {
        fadeOut()
    },
) {
    val arguments: List<NamedNavArgument> = navArguments.map { it.namedNavArgument }

    val route: String

    init {
        val completePath = (
            listOf(path) + navArguments.asSequence()
                .filterIsInstance<PathParamArgument>()
                .map { "{${it.name}}" }
                .toList()
            ).joinToString(separator = "/")

        val queryParams = navArguments.asSequence()
            .filterIsInstance<QueryParamArgument>()
            .map { "${it.name}={${it.name}}" }
            .joinToString(prefix = "?", separator = "&")

        route = if (queryParams == "?") {
            completePath
        } else {
            completePath + queryParams
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    fun register(
        navGraphBuilder: NavGraphBuilder,
        navController: NavController,
    ) {
        navGraphBuilder.composable(
            route = route,
            arguments = arguments,
            deepLinks = deepLinks,
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
        ) { backStackEntry ->
            registerScreen(
                navController = navController,
                backStackEntry = backStackEntry,
            )
        }
    }

    @Composable
    protected abstract fun registerScreen(
        navController: NavController,
        backStackEntry: NavBackStackEntry,
    )
}

sealed class NavigationArgument(
    val name: String,
    builder: NavArgumentBuilder.() -> Unit,
) {
    val namedNavArgument = navArgument(name, builder)
}

class QueryParamArgument(
    name: String,
    builder: NavArgumentBuilder.() -> Unit,
) : NavigationArgument(name, builder)

class PathParamArgument(
    name: String,
    builder: NavArgumentBuilder.() -> Unit,
) : NavigationArgument(name, builder)

object SearchFeedDestination : NavigationDestination(
    path = "search/feed",
    navArguments = listOf(
        QueryParamArgument("feedUrl") {
            type = NavType.StringType
            defaultValue = null
            nullable = true
        }
    ),
    deepLinks = emptyList(),
) {

    fun navigate(navController: NavController, feedUrl: String? = null) {
        val params = queryParams {
            +("feedUrl" to feedUrl)
        }

        navController.navigate(path + params)
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        backStackEntry: NavBackStackEntry
    ) {
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
}

object AddFeedDestination : NavigationDestination(
    path = "add/feed",
    navArguments = listOf(
        PathParamArgument("feedUrl") {
            type = NavType.StringType
        },
        QueryParamArgument("feedTitle") {
            type = NavType.StringType
            defaultValue = ""
        }
    ),
    deepLinks = emptyList(),
) {

    fun navigate(navController: NavController, feedUrl: String, feedTitle: String = "") {
        val params = queryParams {
            +("feedTitle" to feedTitle)
        }

        navController.navigate("$path/${feedUrl.urlEncode()}$params")
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        backStackEntry: NavBackStackEntry
    ) {
        val createFeedScreenViewModel: CreateFeedScreenViewModel = backStackEntry.DIAwareViewModel()

        CreateFeedScreen(
            onNavigateUp = {
                navController.popBackStack()
            },
            createFeedScreenViewModel = createFeedScreenViewModel,
        ) { feedId ->
            FeedDestination.navigate(navController, feedId = feedId)
        }
    }
}

object EditFeedDestination : NavigationDestination(
    path = "edit/feed",
    navArguments = listOf(
        PathParamArgument("feedId") {
            type = NavType.LongType
        }
    ),
    deepLinks = emptyList(),
) {

    fun navigate(navController: NavController, feedId: Long) {
        navController.navigate("$path/$feedId")
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        backStackEntry: NavBackStackEntry
    ) {
        val editFeedScreenViewModel: EditFeedScreenViewModel = backStackEntry.DIAwareViewModel()
        EditFeedScreen(
            onNavigateUp = {
                navController.popBackStack()
            },
            editFeedScreenViewModel = editFeedScreenViewModel
        ) { feedId ->
            FeedDestination.navigate(navController, feedId = feedId)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
object SettingsDestination : NavigationDestination(
    path = "settings",
    navArguments = emptyList(),
    deepLinks = emptyList(),
) {
    fun navigate(navController: NavController) {
        navController.navigate(path)
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        backStackEntry: NavBackStackEntry
    ) {
        SettingsScreen(
            onNavigateUp = {
                if (!navController.popBackStack()) {
                    FeedDestination.navigate(navController)
                }
            },
            onNavigateToSyncScreen = {
                SyncScreenDestination.navigate(
                    navController = navController,
                    syncCode = "",
                    secretKey = "",
                )
            },
            settingsViewModel = backStackEntry.DIAwareViewModel(),
        )
    }
}

object FeedDestination : NavigationDestination(
    path = "feed",
    navArguments = listOf(
        QueryParamArgument("id") {
            type = NavType.LongType
            defaultValue = ID_UNSET
        },
        QueryParamArgument("tag") {
            type = NavType.StringType
            defaultValue = ""
        },
    ),
    deepLinks = listOf(
        navDeepLink {
            uriPattern = "$DEEP_LINK_BASE_URI/feed?id={id}&tag={tag}"
        }
    ),
) {
    fun navigate(navController: NavController, feedId: Long = ID_UNSET, tag: String = "") {
        val params = queryParams {
            if (feedId != ID_UNSET) {
                +("id" to "$feedId")
            }
            +("tag" to tag)
        }

        logDebug(LOG_TAG, "Current: ${navController.currentDestination?.route}")
        val navigatedQuickly: Boolean = navController.currentBackStackEntry?.let { currentBackStackEntry ->
            if (FeedDestination.route == currentBackStackEntry.destination.route) {
                logDebug(LOG_TAG, "Current matched feed destination, navigating quickly...")
                val di: DI by closestDI(navController.context)
                val repository by di.instance<Repository>()
                repository.setCurrentFeedAndTag(feedId = feedId, tag = tag)
                true
            } else {
                false
            }
        } ?: false

        if (!navigatedQuickly) {
            logDebug(LOG_TAG, "Navigating properly to feed")
            navController.navigate("$path$params") {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        backStackEntry: NavBackStackEntry
    ) {
        val feedId = backStackEntry.arguments?.getLong("id")
            ?: ID_UNSET
        val tag = backStackEntry.arguments?.getString("tag")
            ?: ""

        val navigationDeepLinkViewModel: NavigationDeepLinkViewModel =
            backStackEntry.DIAwareViewModel()

        LaunchedEffect(feedId, tag) {
            if (feedId > ID_UNSET || tag.isNotBlank()) {
                navigationDeepLinkViewModel.setCurrentFeedAndTag(feedId = feedId, tag = tag)
            }
        }
        FeedScreen(
            navController = navController,
            viewModel = backStackEntry.DIAwareViewModel(),
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
object ArticleDestination : NavigationDestination(
    path = "reader",
    navArguments = listOf(
        PathParamArgument("itemId") {
            type = NavType.LongType
        }
    ),
    deepLinks = listOf(
        navDeepLink {
            uriPattern = "$DEEP_LINK_BASE_URI/article/{itemId}"
        }
    ),
) {
    fun navigate(navController: NavController, itemId: Long) {
        navController.navigate("$path/$itemId")
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        backStackEntry: NavBackStackEntry
    ) {
        val itemId = backStackEntry.arguments?.getLong("itemId")
            ?: error("Missing mandatory argument: itemId")

        val navigationDeepLinkViewModel: NavigationDeepLinkViewModel =
            backStackEntry.DIAwareViewModel()

        LaunchedEffect(itemId) {
            navigationDeepLinkViewModel.setCurrentArticle(itemId = itemId)
        }

        ArticleScreen(
            onNavigateUp = {
                if (!navController.popBackStack()) {
                    FeedDestination.navigate(navController)
                }
            },
            onNavigateToFeed = { feedId ->
                FeedDestination.navigate(navController, feedId = feedId)
            },
            viewModel = backStackEntry.DIAwareViewModel(),
        )
    }
}

object SyncScreenDestination : NavigationDestination(
    path = "sync",
    navArguments = listOf(
        QueryParamArgument("syncCode") {
            type = NavType.StringType
            defaultValue = ""
        },
        QueryParamArgument("secretKey") {
            type = NavType.StringType
            defaultValue = ""
        }
    ),
    deepLinks = listOf(
        navDeepLink {
            uriPattern = "$DEEP_LINK_BASE_URI/sync/join?sync_code={syncCode}&key={secretKey}"
        },
    ),
) {
    fun navigate(navController: NavController, syncCode: String, secretKey: String) {
        val params = queryParams {
            if (syncCode.isNotBlank()) {
                +("syncCode" to syncCode)
            }
            if (secretKey.isNotBlank()) {
                +("secretKey" to secretKey)
            }
        }

        navController.navigate("$path$params")
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        backStackEntry: NavBackStackEntry
    ) {
        val syncRemoteViewModel = backStackEntry.DIAwareViewModel<SyncScreenViewModel>()

        SyncScreen(
            onNavigateUp = {
                if (!navController.popBackStack()) {
                    SettingsDestination.navigate(navController)
                }
            },
            viewModel = syncRemoteViewModel,
        )
    }
}

fun queryParams(block: QueryParamsBuilder.() -> Unit): String {
    return QueryParamsBuilder().apply { block() }.toString()
}

class QueryParamsBuilder {
    private val sb = StringBuilder()

    operator fun Pair<String, String?>.unaryPlus() {
        appendIfNotEmpty(first, second)
    }

    private fun appendIfNotEmpty(name: String, value: String?) {
        if (value?.isNotEmpty() != true) {
            return
        }

        when {
            sb.isEmpty() -> sb.append("?")
            else -> sb.append("&")
        }

        sb.append("$name=${value.urlEncode()}")
    }

    override fun toString(): String = sb.toString()
}
