package com.nononsenseapps.feeder.ui.compose.push

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.KnownDevice
import com.nononsenseapps.feeder.db.room.ThisDevice
import com.nononsenseapps.feeder.push.Devices
import com.nononsenseapps.feeder.push.toKnownDevice
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class PushScreenViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val context: Application by instance()
    private val repository: Repository by instance()
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()

    private val _screenToShow: MutableStateFlow<PushScreenToShow> = MutableStateFlow(
        state["pushScreen"] ?: PushScreenToShow.SETUP
    )

    fun setScreen(value: PushScreenToShow) {
        state["pushScreen"] = value
        _screenToShow.update { value }
    }

    private val _viewState = MutableStateFlow(PushScreenViewState())
    val viewState: StateFlow<PushScreenViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.thisDevice,
                repository.knownDevices,
                _screenToShow,
                repository.allUnifiedPushDistributors,
                repository.currentUnifiedPushDistributor,
            ) { params ->
                val thisDevice = params[0] as ThisDevice?

                @Suppress("UNCHECKED_CAST")
                val knownDevices = params[1] as List<KnownDevice>
                val screenToShow = params[2] as PushScreenToShow

                @Suppress("UNCHECKED_CAST")
                val allDistributors = params[3] as List<String>
                val currentDistributor = params[4] as String

                PushScreenViewState(
                    thisDevice = thisDevice,
                    knownDevices = knownDevices,
                    singleScreenToShow = screenToShow,
                    allDistributors = allDistributors,
                    currentDistributor = currentDistributor,
                )
            }.collect {
                _viewState.value = it
            }
        }
    }

    fun setThisDeviceEndpoint(endpoint: String) {
        logDebug(LOG_TAG, "resetThisDevice")
        viewModelScope.launch {
            repository.updateThisDeviceEndpoint(endpoint = endpoint)
        }
    }

    fun joinSyncChain(devices: Devices) {
        logDebug(LOG_TAG, "joinSyncChain")
        viewModelScope.launch {
            repository.joinSyncChain(devices.devices.map { it.toKnownDevice() })
        }
    }

    fun deleteDevice(device: KnownDevice) {
        logDebug(LOG_TAG, "deleteDevice: ${device.name}")
        viewModelScope.launch {
            repository.deleteDevices(listOf(device.endpoint))
        }
    }

    // This will also update thisDevice if registration succeeds
    fun setDistributor(value: String) {
        logDebug(LOG_TAG, "setDistributor: $value")
        viewModelScope.launch {
            repository.setDistributor(value)
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_PUSHVIEWMODEL"
    }
}

@Immutable
data class PushScreenViewState(
    val singleScreenToShow: PushScreenToShow = PushScreenToShow.SETUP,
    val thisDevice: ThisDevice? = null,
    val knownDevices: List<KnownDevice> = emptyList(),
    val allDistributors: List<String> = emptyList(),
    val currentDistributor: String = "",
) {
    val leftScreenToShow: LeftScreenToShow
        get() = when (singleScreenToShow) {
            PushScreenToShow.SETUP, PushScreenToShow.JOIN -> LeftScreenToShow.SETUP
            PushScreenToShow.DEVICELIST, PushScreenToShow.ADD_DEVICE -> LeftScreenToShow.DEVICELIST
        }

    val rightScreenToShow: RightScreenToShow
        get() = when (singleScreenToShow) {
            PushScreenToShow.SETUP, PushScreenToShow.JOIN -> RightScreenToShow.JOIN
            PushScreenToShow.DEVICELIST, PushScreenToShow.ADD_DEVICE -> RightScreenToShow.ADD_DEVICE
        }
}

enum class PushScreenToShow {
    SETUP,
    DEVICELIST,
    ADD_DEVICE,
    JOIN,
}

enum class LeftScreenToShow {
    SETUP,
    DEVICELIST,
}

enum class RightScreenToShow {
    ADD_DEVICE,
    JOIN,
}
