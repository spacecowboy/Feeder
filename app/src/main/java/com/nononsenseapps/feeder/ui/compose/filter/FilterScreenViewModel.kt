package com.nononsenseapps.feeder.ui.compose.filter

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class FilterScreenViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val repository: Repository by instance()

    private val _filter: MutableStateFlow<String> = MutableStateFlow(
        state["filter"] ?: ""
    )
    fun setFilter(value: String) {
        state["filter"] = value
        _filter.update { value }
    }

    private fun isSaveable(filter: String): Boolean {
        return when {
            filter.isBlank() -> false
            else -> false
        }
    }

    suspend fun saveFilter(filter: String) {
        if (isSaveable(filter)) {
            // TODO repository.saveFilter
        }
    }

    private val _viewState = MutableStateFlow(FilterScreenViewState())
    val viewState: StateFlow<FilterScreenViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_filter) { params: Array<Any> ->
                val currentFilter = params[0] as String
                FilterScreenViewState(
                    currentFilter = currentFilter,
                    saveable = isSaveable(currentFilter)
                )
            }
        }
    }
}

@Immutable
data class FilterScreenViewState(
    val matchCount: Int = 0,
    val totalCount: Int = 0,
    val currentFilter: String = "",
    val filterHasError: Boolean = false,
    @StringRes val filterSupportText: Int? = null,
    val saveable: Boolean = false,
    val matchingFeedItems: List<FeedListItem> = emptyList(),
)
