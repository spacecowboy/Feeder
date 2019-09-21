package com.nononsenseapps.feeder.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

/**
 * A fragment which is also Kodein aware and a coroutine scope.
 *
 * All coroutines started in this fragment are linked to the Fragment's lifecycle. If the fragment
 * is destroyed, then all coroutines are cancelled.
 */
open class CoroutineScopedKodeinAwareFragment : androidx.fragment.app.Fragment(), KodeinAware, CoroutineScope by MainScope() {
    override val kodein: Kodein by closestKodein()

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}
