package com.nononsenseapps.feeder.base

import androidx.fragment.app.DialogFragment
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI

/**
 * A dialog fragment which is also Kodein aware.
 */
open class DIAwareDialogFragment : DialogFragment(), DIAware {
    override val di: DI by closestDI()
}
