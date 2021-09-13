package com.nononsenseapps.feeder.base

import android.view.MenuInflater
import androidx.activity.ComponentActivity
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

abstract class DIAwareComponentActivity : ComponentActivity(), DIAware {
    private val parentDI: DI by closestDI()
    override val di: DI by DI.lazy {
        extend(parentDI)
        bind<MenuInflater>() with provider { menuInflater }
        bind<DIAwareComponentActivity>() with instance(this@DIAwareComponentActivity)
    }
}
