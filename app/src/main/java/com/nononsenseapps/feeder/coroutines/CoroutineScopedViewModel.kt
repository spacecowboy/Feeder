package com.nononsenseapps.feeder.coroutines

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * A ViewModel which is also Coroutine aware.
 *
 * All coroutines started in this view model are linked to the lifecycle. If the view model
 * is destroyed, then all coroutines are cancelled. Likewise, if any of the coroutines encounter
 * an exception, that cancels the other coroutines
 */
open class CoroutineScopedViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}