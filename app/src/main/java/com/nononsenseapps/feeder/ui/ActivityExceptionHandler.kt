package com.nononsenseapps.feeder.ui

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.nononsenseapps.feeder.util.emailCrashReportIntent
import kotlin.system.exitProcess

fun Activity.installExceptionHandler() {
    val mainHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        try {
            Log.w("FEEDER_PANIC", "Trying to report unhandled exception", throwable)
            val intent = emailCrashReportIntent(throwable)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Necessary to handle the error or the process will freeze
            if (mainHandler != null) {
                mainHandler.uncaughtException(thread, throwable)
            } else {
                exitProcess(1)
            }
        }
    }
}
