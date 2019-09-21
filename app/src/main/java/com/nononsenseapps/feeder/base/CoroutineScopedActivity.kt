package com.nononsenseapps.feeder.base

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein

/**
 * A fragment which is also Kodein aware and a coroutine scope.
 *
 * All coroutines started in this activity are linked to the Activity's lifecycle. If the activity
 * is destroyed, then all coroutines are cancelled.
 */
@SuppressLint("Registered")
open class CoroutineScopedKodeinAwareActivity : AppCompatActivity(), KodeinAware, CoroutineScope by MainScope() {
    override val kodein: Kodein by closestKodein()

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}
