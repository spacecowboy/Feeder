package com.nononsenseapps.feeder.coroutines

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * A fragment which is also Coroutine aware.
 *
 * All coroutines started in this fragment are linked to the Activity's lifecycle. If the activity
 * is destroyed, then all coroutines are cancelled.
 */
@SuppressLint("Registered")
open class CoroutineScopedActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}
