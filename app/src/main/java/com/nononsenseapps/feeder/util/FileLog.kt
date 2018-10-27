package com.nononsenseapps.feeder.util

import android.content.Context
import android.util.Log
import com.nononsenseapps.feeder.coroutines.Background
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


fun ensureDebugLogDeleted(context: Context)  {
    try {
        val logFile = File(context.filesDir, "feeder.log")
        if (logFile.exists()) {
            logFile.delete()
        }
    } catch (exception: Exception) {
        Log.e("FileLog", "Failed to delete legacy debug log: $exception")
    }
}
