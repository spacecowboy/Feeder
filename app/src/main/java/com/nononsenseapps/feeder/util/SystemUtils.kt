package com.nononsenseapps.feeder.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.BatteryManager
import java.net.URLEncoder

/**
 * Note that cellular typically is metered - it is NOT the same as hotspot
 */
private fun currentlyMetered(context: Context): Boolean {
    val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return connManager?.isActiveNetworkMetered ?: false
}

fun currentlyUnmetered(context: Context): Boolean = !currentlyMetered(context)

fun currentlyCharging(context: Context): Boolean {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
    return batteryManager?.isCharging == true
}

fun currentlyConnected(context: Context): Boolean {
    val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    val net = connManager?.activeNetwork

    @Suppress("DEPRECATION")
    val netInfo = connManager?.getNetworkInfo(net)
    @Suppress("DEPRECATION")
    return netInfo != null && netInfo.isConnected
}

fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
