package com.nononsenseapps.feeder.base

import android.annotation.SuppressLint
import android.view.MenuInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider

/**
 * A fragment which is also Kodein aware and a coroutine scope.
 *
 * All coroutines started in this activity are linked to the Activity's lifecycle. If the activity
 * is destroyed, then all coroutines are cancelled.
 */
@SuppressLint("Registered")
open class CoroutineScopedKodeinAwareActivity : AppCompatActivity(), KodeinAware, CoroutineScope by MainScope() {
    private val parentKodein: Kodein by closestKodein()
    override val kodein: Kodein by Kodein.lazy {
        extend(parentKodein)
        bind<MenuInflater>() with provider { menuInflater }
        bind<FragmentActivity>() with instance(this@CoroutineScopedKodeinAwareActivity)
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}
