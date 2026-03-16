package com.nononsenseapps.feeder.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.util.currentlyCharging
import com.nononsenseapps.feeder.util.currentlyConnected
import com.nononsenseapps.feeder.util.currentlyUnmetered
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import java.time.Instant

enum class ScrollDirection {
    UP,
    DOWN,
}

class MainActivityViewModel(
    di: DI,
) : DIAwareViewModel(di) {
    private val repository: Repository by instance()
    private val context: Application by instance()

    private val _scrollCommand = MutableSharedFlow<ScrollDirection>()
    val scrollCommand: SharedFlow<ScrollDirection> = _scrollCommand.asSharedFlow()

    fun emitScrollCommand(direction: ScrollDirection) {
        viewModelScope.launch {
            _scrollCommand.emit(direction)
        }
    }

    val isPagingMode: StateFlow<Boolean> = repository.isPagingMode
    val isAnimatedPaging: StateFlow<Boolean> = repository.isAnimatedPaging

    fun setResumeTime() {
        repository.setResumeTime(Instant.now())
    }

    val shouldSyncOnResume: Boolean =
        repository.syncOnResume.value

    fun ensurePeriodicSyncConfigured() =
        viewModelScope.launch {
            repository.ensurePeriodicSyncConfigured()
        }

    fun isOkToSyncAutomatically(): Boolean =
        currentlyConnected(context) &&
            (!repository.syncOnlyWhenCharging.value || currentlyCharging(context)) &&
            (!repository.syncOnlyOnWifi.value || currentlyUnmetered(context))
}
