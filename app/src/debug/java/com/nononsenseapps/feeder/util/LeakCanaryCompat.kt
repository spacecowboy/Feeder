package com.nononsenseapps.feeder.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import leakcanary.LeakCanary

fun updateLeakCanaryNotificationState(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }
    val hasPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    LeakCanary.config = LeakCanary.config.copy(showNotifications = hasPermission)
}
