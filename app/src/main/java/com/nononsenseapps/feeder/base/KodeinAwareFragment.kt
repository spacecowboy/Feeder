package com.nononsenseapps.feeder.base

import androidx.fragment.app.Fragment
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI

/**
 * A fragment which is also Kodein aware.
 */
open class DIAwareFragment : Fragment(), DIAware {
    override val di: DI by closestDI()
}
