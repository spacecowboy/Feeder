package com.nononsenseapps.feeder.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.threeten.bp.Instant
import java.net.URLEncoder

/**
 * State which is tied to the entire application, useful to tie UI together with background tasks
 */
class ApplicationState {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    fun setRefreshing(refreshing: Boolean = true) {
        _isRefreshing.value = refreshing
    }

    private val _resumeTime = MutableStateFlow(Instant.EPOCH)

    /**
     * Observe this value in compose to get actions to happen when the
     * activity returns to the foreground
     */
    val resumeTime: StateFlow<Instant> = _resumeTime.asStateFlow()
    fun setResumeTime(instant: Instant = Instant.now()) {
        _resumeTime.value = instant
    }

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

class NavigationSearch(feedUrl: String): NavigationTarget() {
    override fun navOptions(navOptionsBuilder: NavOptionsBuilder) {
    }

    override val route: String = "search/feed?feedUrl=${URLEncoder.encode(feedUrl, "utf-8")}"
}

class NavigationCurrentFeed : NavigationTarget() {
    override fun navOptions(navOptionsBuilder: NavOptionsBuilder) {
    }

    override val route: String ="feed"
}