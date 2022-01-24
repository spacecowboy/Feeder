package com.nononsenseapps.feeder.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavArgumentBuilder
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.NavigationDeepLinkViewModel
import com.nononsenseapps.feeder.ui.compose.editfeed.CreateFeedScreen
import com.nononsenseapps.feeder.ui.compose.editfeed.CreateFeedScreenViewModel
import com.nononsenseapps.feeder.ui.compose.editfeed.EditFeedScreen
import com.nononsenseapps.feeder.ui.compose.editfeed.EditFeedScreenViewModel
import com.nononsenseapps.feeder.ui.compose.feedarticle.FeedArticleScreen
import com.nononsenseapps.feeder.ui.compose.searchfeed.SearchFeedScreen
import com.nononsenseapps.feeder.ui.compose.settings.SettingsScreen
import com.nononsenseapps.feeder.ui.compose.sync.SyncScreen
import com.nononsenseapps.feeder.ui.compose.sync.SyncScreenViewModel
import com.nononsenseapps.feeder.ui.compose.utils.WindowSize
import com.nononsenseapps.feeder.util.DEEP_LINK_BASE_URI
import com.nononsenseapps.feeder.util.urlEncode

sealed class NavigationDestination(
    protected val path: String,
    protected val navArguments: List<NavigationArgument>,
    val deepLinks: List<NavDeepLink>,
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

    fun register(
        navGraphBuilder: NavGraphBuilder,
        navController: NavController,
        windowSize: WindowSize,
    ) {
        navGraphBuilder.composable(
            route = route,
            arguments = arguments,
            deepLinks = deepLinks,
        ) { backStackEntry ->
            registerScreen(
                windowSize = windowSize,
                navController = navController,
                backStackEntry = backStackEntry,
            )
        }
    }

    @Composable
    protected abstract fun registerScreen(
        navController: NavController,
        windowSize: WindowSize,
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
            +("feedUrl" to feedUrl?.urlEncode())
        }

        navController.navigate(path + params)
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        windowSize: WindowSize,
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
            +("feedTitle" to feedTitle.urlEncode())
        }

        navController.navigate("$path/${feedUrl.urlEncode()}${params}")
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        windowSize: WindowSize,
        backStackEntry: NavBackStackEntry
    ) {
        val createFeedScreenViewModel: CreateFeedScreenViewModel = backStackEntry.DIAwareViewModel()

        CreateFeedScreen(
            onNavigateUp = {
                navController.popBackStack()
            },
            createFeedScreenViewModel = createFeedScreenViewModel,
        ) { feedId ->
            createFeedScreenViewModel.setCurrentFeedAndTag(feedId = feedId, tag = "")
            FeedArticleDestination.navigate(navController)
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
        windowSize: WindowSize,
        backStackEntry: NavBackStackEntry
    ) {
        val editFeedScreenViewModel: EditFeedScreenViewModel = backStackEntry.DIAwareViewModel()
        EditFeedScreen(
            onNavigateUp = {
                navController.popBackStack()
            },
            onOk = { feedId ->
                editFeedScreenViewModel.setCurrentFeedAndTag(feedId = feedId, tag = "")
                FeedArticleDestination.navigate(navController)
            },
            editFeedScreenViewModel = editFeedScreenViewModel
        )
    }
}

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
        windowSize: WindowSize,
        backStackEntry: NavBackStackEntry
    ) {
        SettingsScreen(
            onNavigateUp = {
                if (!navController.popBackStack()) {
                    FeedArticleDestination.navigate(navController)
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

object FeedArticleDestination : NavigationDestination(
    path = "feedarticle",
    navArguments = emptyList(),
    deepLinks = emptyList(),
) {
    fun navigate(
        navController: NavController
    ) {
        navController.navigate(path) {
            popUpTo(path)
            launchSingleTop = true
        }
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        windowSize: WindowSize,
        backStackEntry: NavBackStackEntry
    ) {
        FeedArticleScreen(
            windowSize = windowSize,
            navController = navController,
            viewModel = backStackEntry.DIAwareViewModel(),
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

        navController.navigate("$path${params}")
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        windowSize: WindowSize,
        backStackEntry: NavBackStackEntry
    ) {
        val feedId = backStackEntry.arguments?.getLong("id")
            ?: error("Missing mandatory argument: id")
        val tag = backStackEntry.arguments?.getString("tag")
            ?: error("Missing mandatory argument: tag")

        val navigationDeepLinkViewModel: NavigationDeepLinkViewModel =
            backStackEntry.DIAwareViewModel()

        LaunchedEffect(feedId, tag) {
            navigationDeepLinkViewModel.setCurrentFeedAndTag(feedId = feedId, tag = tag)
            FeedArticleDestination.navigate(navController)
        }
    }
}

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
        windowSize: WindowSize,
        backStackEntry: NavBackStackEntry
    ) {
        val itemId = backStackEntry.arguments?.getLong("itemId")
            ?: error("Missing mandatory argument: itemId")

        val navigationDeepLinkViewModel: NavigationDeepLinkViewModel =
            backStackEntry.DIAwareViewModel()

        // TODO should this also set current feed? On tablet it might feel weird that
        // the article is from one feed and the list displays a different feed
        LaunchedEffect(itemId) {
            navigationDeepLinkViewModel.setCurrentArticle(itemId = itemId)
            FeedArticleDestination.navigate(navController)
        }
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

        navController.navigate("$path${params}")
    }

    @Composable
    override fun registerScreen(
        navController: NavController,
        windowSize: WindowSize,
        backStackEntry: NavBackStackEntry
    ) {
        val syncRemoteViewModel = backStackEntry.DIAwareViewModel<SyncScreenViewModel>()

        SyncScreen(
            windowSize = windowSize,
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
