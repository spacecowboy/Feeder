package com.nononsenseapps.feeder.coroutines

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * A ViewModel which is also Coroutine aware.
 *
 * All coroutines started in this view model are linked to the lifecycle. If the view model
 * is destroyed, then all coroutines are cancelled.
 */
open class CoroutineScopedViewModel(application: Application) : AndroidViewModel(application), CoroutineScope by MainScope() {
    override fun onCleared() {
        cancel()
        super.onCleared()
    }
}
