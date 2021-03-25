package com.nononsenseapps.feeder.base

import android.annotation.SuppressLint
import android.view.MenuInflater
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider

/**
 * A fragment which is also Kodein aware.
 */
@SuppressLint("Registered")
open class KodeinAwareActivity : AppCompatActivity(), KodeinAware {
    private val parentKodein: Kodein by closestKodein()
    override val kodein: Kodein by Kodein.lazy {
        extend(parentKodein)
        bind<MenuInflater>() with provider { menuInflater }
        bind<FragmentActivity>() with instance(this@KodeinAwareActivity)
    }
}

abstract class KodeinAwareComponentActivity : ComponentActivity(), KodeinAware {
    private val parentKodein: Kodein by closestKodein()
    override val kodein: Kodein by Kodein.lazy {
        extend(parentKodein)
        bind<MenuInflater>() with provider { menuInflater }
        bind<ComponentActivity>() with instance(this@KodeinAwareComponentActivity)
    }
}
