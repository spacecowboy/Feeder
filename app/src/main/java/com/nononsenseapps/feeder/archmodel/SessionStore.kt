package com.nononsenseapps.feeder.archmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.threeten.bp.Instant

class SessionStore {
    private val _resumeTime = MutableStateFlow(Instant.EPOCH)
    /**
     * Observe this value in compose to get actions to happen when the
     * activity returns to the foreground
     */
    val resumeTime: StateFlow<Instant> = _resumeTime.asStateFlow()
    fun setResumeTime(instant: Instant) {
        _resumeTime.value = instant
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    fun setRefreshing(refreshing: Boolean) {
        _isRefreshing.value = refreshing
    }

    private val _expandedTags = MutableStateFlow(emptySet<String>())
    val expandedTags: StateFlow<Set<String>> = _expandedTags
    fun toggleTagExpansion(tag: String) {
        _expandedTags.value = if (tag in expandedTags.value) {
            _expandedTags.value - tag
        } else {
            _expandedTags.value + tag
        }
    }
}
