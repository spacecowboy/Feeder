package com.nononsenseapps.feeder.coroutines

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import kotlinx.coroutines.android.HandlerDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher

private val backgroundHandlerThread: HandlerThread by lazy {
    HandlerThread("BackgroundThread", Process.THREAD_PRIORITY_BACKGROUND)
}
/**
 * Defines a context which runs on a HandlerThread with priority: THREAD_PRIORITY_BACKGROUND.
 * Lazily initialized to avoid allocating Thread when not needed, but cached after that.
 *
 * Use this for real background work
 */
val Background: HandlerDispatcher by lazy {
    backgroundHandlerThread.start()
    Handler(backgroundHandlerThread.looper).asCoroutineDispatcher(name = backgroundHandlerThread.name)
}

private val backgroundUIHandlerThread: HandlerThread by lazy {
    HandlerThread("BackgroundUIThread", Process.THREAD_PRIORITY_LESS_FAVORABLE)
}
/**
 * Defines a context which runs on a HandlerThread with priority: THREAD_PRIORITY_LESS_FAVORABLE.
 * Lazily initialized to avoid allocating Thread when not needed, but cached after that.
 *
 * Use this to run UI related things not on the UI thread.
 */
val BackgroundUI: HandlerDispatcher by lazy {
    backgroundUIHandlerThread.start()
    Handler(backgroundUIHandlerThread.looper).asCoroutineDispatcher(name = backgroundUIHandlerThread.name)
}
