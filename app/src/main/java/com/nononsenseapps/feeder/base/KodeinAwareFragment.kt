package com.nononsenseapps.feeder.base

import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

/**
 * A fragment which is also Kodein aware.
 */
open class KodeinAwareFragment : androidx.fragment.app.Fragment(), KodeinAware {
    override val kodein: Kodein by closestKodein()
}
