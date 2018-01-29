package com.nononsenseapps.feeder.ui

import android.os.AsyncTask

/**
 * To remove possibility of leaks, remember to call cancel in onDestroy method in Activities, Fragments, Services etc..
 *
 * And, to avoid that you leak stuff via a overridden parameter, set a custom setter which calls cancel too:
 *
 * private var myTask: MyTask? = null
 * set(value) {
 *   field?.cancel(true)
 *   field = value
 * }
 */
abstract class LeakHandlingAsyncTask<A, B, C>(
        private var onProgress: ((B) -> Unit)? = null,
        private var onPost: ((C?) -> Unit)?) : AsyncTask<A, B, C>() {

    override fun onCancelled() {
        onProgress = null
        onPost = null
    }

    override fun onProgressUpdate(vararg items: B?) {
        items.firstOrNull()?.let {
            onProgress?.invoke(it)
        }
    }

    override fun onPostExecute(result: C?) {
        onPost?.invoke(result)
    }
}
