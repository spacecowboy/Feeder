package com.nononsenseapps.feeder.ui.compose.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavArgumentBuilder
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.nononsenseapps.feeder.db.room.ID_UNSET
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
}

object SettingsDestination : NavigationDestination(
    path = "settings",
    navArguments = emptyList(),
    deepLinks = emptyList(),
) {
    fun navigate(navController: NavController) {
        navController.navigate(path)
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
