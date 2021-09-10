package com.nononsenseapps.feeder.model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.threeten.bp.Instant

/**
 * State which is tied to the entire application, useful to tie UI together with background tasks
 */
class ApplicationState {
    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: State<Boolean> = _isRefreshing
    fun setRefreshing(refreshing: Boolean) {
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
