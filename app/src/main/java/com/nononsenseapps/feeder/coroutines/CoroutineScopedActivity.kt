package com.nononsenseapps.feeder.coroutines

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * A fragment which is also Coroutine aware.
 *
 * All coroutines started in this fragment are linked to the Fragment's lifecycle. If the fragment
 * is destroyed, then all coroutines are cancelled. Likewise, if any of the coroutines encounter
 * an exception, that cancels the other coroutines
 */
@SuppressLint("Registered")
open class CoroutineScopedActivity: AppCompatActivity(), CoroutineScope {
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        job = Job()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}