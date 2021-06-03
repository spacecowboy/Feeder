package com.nononsenseapps.feeder.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

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
}
