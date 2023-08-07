package com.nononsenseapps.feeder.archmodel

import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SessionStore {
    private val _resumeTime = MutableStateFlow(Instant.EPOCH)

    /**
     * Observe this value in compose to get actions to happen when the
     * activity returns to the foreground
     */
    val resumeTime: StateFlow<Instant> = _resumeTime.asStateFlow()
    fun setResumeTime(instant: Instant) {
        _resumeTime.update {
            instant
        }
    }

    private val _expandedTags = MutableStateFlow(emptySet<String>())
    val expandedTags: StateFlow<Set<String>> = _expandedTags.asStateFlow()
    fun toggleTagExpansion(tag: String) {
        _expandedTags.update {
            if (tag in expandedTags.value) {
                _expandedTags.value - tag
            } else {
                _expandedTags.value + tag
            }
        }
    }
}
