package com.nononsenseapps.feeder.base

import android.annotation.SuppressLint
import android.view.MenuInflater
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

/**
 * A fragment which is also Kodein aware.
 */
@SuppressLint("Registered")
open class DIAwareActivity : AppCompatActivity(), DIAware {
    private val parentDI: DI by closestDI()
    override val di: DI by DI.lazy {
        extend(parentDI)
        bind<MenuInflater>() with provider { menuInflater }
        bind<FragmentActivity>() with instance(this@DIAwareActivity)
    }
}

abstract class DIAwareComponentActivity : ComponentActivity(), DIAware {
    private val parentDI: DI by closestDI()
    override val di: DI by DI.lazy {
        extend(parentDI)
        bind<MenuInflater>() with provider { menuInflater }
        bind<ComponentActivity>() with instance(this@DIAwareComponentActivity)
    }
}
