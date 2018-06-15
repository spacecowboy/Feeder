package com.nononsenseapps.feeder.coroutines

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import kotlinx.coroutines.experimental.android.HandlerContext

private val backgroundHandlerThread: HandlerThread by lazy {
    HandlerThread("BackgroundThread", Process.THREAD_PRIORITY_BACKGROUND)
}
/**
 * Defines a context which runs on a HandlerThread with priority: THREAD_PRIORITY_BACKGROUND.
 * Lazily initialized to avoid allocating Thread when not needed, but cached after that.
 *
 * Use this for real background work
 */
val Background: HandlerContext by lazy {
    backgroundHandlerThread.start()
    HandlerContext(Handler(backgroundHandlerThread.looper), backgroundHandlerThread.name)
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
val BackgroundUI: HandlerContext by lazy {
    backgroundUIHandlerThread.start()
    HandlerContext(Handler(backgroundUIHandlerThread.looper), backgroundUIHandlerThread.name)
}
