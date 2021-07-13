package com.nononsenseapps.feeder.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavOptionsBuilder
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_UNSET
import org.kodein.di.DI
import java.net.URLEncoder

/**
 * Should only be created with the activity as its lifecycle
 */
class EphemeralState(di: DI) : DIAwareViewModel(di) {
    var lastOpenFeedId: Long = ID_UNSET
        set(value) {
            if (value != lastOpenFeedId) {
                firstVisibleListItem = null
            }
            field = value
        }
    var lastOpenFeedTag: String = ""
        set(value) {
            if (value != lastOpenFeedTag) {
                firstVisibleListItem = null
            }
            field = value
        }
    var firstVisibleListItem: Int? = null

    private val intentNavigationState: MutableState<NavigationTarget> =
        mutableStateOf(NavigationCurrentFeed())

    /**
     * Used as a bridge between activity intents and compose navigation
     */
    val intentNavigationTarget: State<NavigationTarget> = intentNavigationState
    fun setIntentNavigationTarget(target: NavigationTarget) {
        intentNavigationState.value = target
    }
}

sealed class NavigationTarget {
    abstract fun navOptions(navOptionsBuilder: NavOptionsBuilder)
    abstract val route: String
}

class NavigationSettings : NavigationTarget() {
    override fun navOptions(navOptionsBuilder: NavOptionsBuilder) {
    }

    override val route: String = "settings"
}

class NavigationSearch(feedUrl: String) : NavigationTarget() {
    override fun navOptions(navOptionsBuilder: NavOptionsBuilder) {
    }

    override val route: String = "search/feed?feedUrl=${URLEncoder.encode(feedUrl, "utf-8")}"
}

class NavigationCurrentFeed : NavigationTarget() {
    override fun navOptions(navOptionsBuilder: NavOptionsBuilder) {
    }

    override val route: String = "feed"
}
