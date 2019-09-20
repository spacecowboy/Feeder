package com.nononsenseapps.feeder.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * A fragment which is also Coroutine aware.
 *
 * All coroutines started in this fragment are linked to the Fragment's lifecycle. If the fragment
 * is destroyed, then all coroutines are cancelled.
 */
open class CoroutineScopedFragment : androidx.fragment.app.Fragment(), CoroutineScope by MainScope() {
    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}
