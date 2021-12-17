package com.nononsenseapps.feeder.notifications

import android.app.Application
import android.util.Log
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.model.cancelNotification
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Handles notifying and removing notifications
 */
class NotificationsWorker(override val di: DI) : DIAware {
    private val context: Application by instance()
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()
    private val repository: Repository by instance()
    private var job: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun runForever() {
        job?.cancel("runForever")
        job = applicationCoroutineScope.launch {
            repository.getFeedItemsNeedingNotifying()
                .runningReduce { prev, current ->
                    try {
                        unNotifyForMissingItems(prev, current)
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Error in notifications worker", e)
                    }
                    // Always pass current on
                    current
                }
                .collect {
                    // Notifications are triggered during sync, not here
                }
        }
    }

    fun stopForever() {
        job?.cancel("stopForever")
    }

    internal suspend fun unNotifyForMissingItems(prev: List<Long>, current: List<Long>) {
        prev.filter {
            it !in current
        }.forEach {
            cancelNotification(it)
        }
    }

    // Silly wrapper method to make it testable
    internal suspend fun cancelNotification(id: Long) {
        cancelNotification(context, id)
    }

    companion object {
        private const val LOG_TAG = "FEEDER_NW"
    }
}
