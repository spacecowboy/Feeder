package com.nononsenseapps.feeder.base

import androidx.fragment.app.DialogFragment
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

/**
 * A dialog fragment which is also Kodein aware.
 */
open class KodeinAwareDialogFragment : DialogFragment(), KodeinAware {
    override val kodein: Kodein by closestKodein()
}
