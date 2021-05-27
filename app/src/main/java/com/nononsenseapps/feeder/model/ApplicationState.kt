package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.ID_UNSET
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI

/**
 * State which is tied to the entire application, useful to tie UI together with background tasks
 */
class ApplicationState() {
    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    fun setRefreshing(refreshing: Boolean = true) {
        _isRefreshing.tryEmit(refreshing)
    }
}
