package com.nononsenseapps.feeder.push

import android.content.Context
import com.nononsenseapps.feeder.db.room.KnownDevice
import com.nononsenseapps.feeder.db.room.KnownDevicesDao
import com.nononsenseapps.feeder.db.room.MessageQueueDao
import com.nononsenseapps.feeder.db.room.QueuedMessage
import com.nononsenseapps.feeder.db.room.ThisDevice
import com.nononsenseapps.feeder.db.room.ThisDeviceDao
import com.nononsenseapps.feeder.db.room.upsert
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.threeten.bp.Instant
import org.unifiedpush.android.connector.UnifiedPush

class PushStore(override val di: DI) : DIAware {
    private val context by instance<Context>()
    private val messageQueueDao by instance<MessageQueueDao>()
    private val knownDevicesDao by instance<KnownDevicesDao>()
    private val thisDeviceDao by instance<ThisDeviceDao>()

    val allDistributors: StateFlow<List<String>>
        get() {
            // This is a getter because I want every time the screen in entered to fetch them anew
            return MutableStateFlow(getAvailableDistributors()).asStateFlow()
        }

    private val _distributor = MutableStateFlow(getDistributor())
    val distributor: StateFlow<String> = _distributor.asStateFlow()

    // Used by UI
    fun setDistributor(value: String) {
        when {
            value.isNotEmpty() -> registerWithDistributor(value)
            else -> unregisterApp()
        }
        _distributor.value = value
    }

    // Used by BroadcastReceiver
    @Suppress("FunctionName")
    internal fun _setDistributorValue(value: String) {
        _distributor.value = value
    }

    private val _registrationFailed = MutableStateFlow(false)
    val registrationFailed: StateFlow<Boolean> = _registrationFailed.asStateFlow()
    internal fun setRegistrationFailed(value: Boolean) {
        _registrationFailed.value = value
    }

    fun resetRegistrationFailed() {
        _registrationFailed.value = false
    }

    private fun getDistributor(): String {
        return UnifiedPush.getDistributor(context)
            .also {
                logDebug(PushMaker.LOG_TAG, "getDistributor: $it")
            }
    }

    private fun registerWithDistributor(distributor: String) {
        logDebug(PushMaker.LOG_TAG, "registerWithDistributor: $distributor")
        UnifiedPush.saveDistributor(context, distributor)
        UnifiedPush.registerApp(context, features = arrayListOf(UnifiedPush.FEATURE_BYTES_MESSAGE))
    }

    private fun unregisterApp() {
        logDebug(PushMaker.LOG_TAG, "unregisterWithDistributor")
        UnifiedPush.unregisterApp(context)
    }

    private fun getAvailableDistributors(): List<String> {
        return UnifiedPush.getDistributors(
            context,
            features = arrayListOf(UnifiedPush.FEATURE_BYTES_MESSAGE)
        ).also {
            logDebug(PushMaker.LOG_TAG, "getAvailableDistributors: ${it.joinToString(", ")}")
        }
    }

    suspend fun deleteAllDevices() {
        knownDevicesDao.deleteAllDevices()
    }

    suspend fun deleteDevices(endpoints: List<String>): Int {
        return knownDevicesDao.deleteDevices(endpoints)
    }

    suspend fun addMessageToQueue(message: QueuedMessage) {
        messageQueueDao.insert(message)
    }

    suspend fun getMessagesInQueue(): List<QueuedMessage> {
        return messageQueueDao.getMessages()
    }

    suspend fun getKnownDevice(endpoint: String): KnownDevice? {
        return knownDevicesDao.getKnownDevice(endpoint = endpoint)
    }

    suspend fun getKnownDevices(): List<KnownDevice> {
        return knownDevicesDao.getKnownDevices()
    }

    fun getKnownDevicesFlow(): Flow<List<KnownDevice>> {
        return knownDevicesDao.getKnownDevicesFlow()
    }

    suspend fun saveKnownDevices(devices: List<KnownDevice>) {
        for (device in devices) {
            saveKnownDevice(device)
        }
    }

    suspend fun saveKnownDevice(knownDevice: KnownDevice) {
        knownDevicesDao.upsert(knownDevice)
    }

    suspend fun deleteMessagesInQueue(ids: List<Long>) {
        messageQueueDao.deleteMessages(ids)
    }

    suspend fun getThisDevice(): ThisDevice? {
        return thisDeviceDao.getThisDevice()
    }

    fun getThisDeviceFlow(): Flow<ThisDevice?> {
        return thisDeviceDao.getThisDeviceFlow()
    }

    suspend fun saveThisDevice(thisDevice: ThisDevice) {
        // Replaces on conflict
        thisDeviceDao.insert(thisDevice)
    }

    suspend fun deleteThisDevice() {
        thisDeviceDao.delete()
    }

    suspend fun getThisDeviceEndpoint(): String? {
        return thisDeviceDao.getThisDeviceEndpoint()
    }
}

fun Device.toKnownDevice() = KnownDevice(
    endpoint = endpoint,
    name = name ?: "",
    lastSeen = Instant.EPOCH,
)

fun KnownDevice.toProto() = Device(
    endpoint = endpoint,
    name = name,
)
