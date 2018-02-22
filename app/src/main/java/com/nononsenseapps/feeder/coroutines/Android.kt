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
 */
val Background: HandlerContext by lazy {
    backgroundHandlerThread.start()
    HandlerContext(Handler(backgroundHandlerThread.looper), backgroundHandlerThread.name)
}
