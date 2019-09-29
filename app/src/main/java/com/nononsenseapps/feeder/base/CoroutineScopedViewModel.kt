package com.nononsenseapps.feeder.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.kodein.di.Kodein

/**
 * A ViewModel which is also Kodein aware and a coroutine scope.
 *
 * All coroutines started in this view model are linked to the lifecycle. If the view model
 * is destroyed, then all coroutines are cancelled.
 */
open class CoroutineScopedKodeinAwareViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein), CoroutineScope by MainScope() {
    override fun onCleared() {
        cancel()
        super.onCleared()
    }
}
