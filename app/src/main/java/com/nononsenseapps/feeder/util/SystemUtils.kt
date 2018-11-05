package com.nononsenseapps.feeder.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.BatteryManager
import android.os.Build

/**
 * Note that cellular typically is metered - it is NOT the same as hotspot
 */
private fun currentlyMetered(context: Context): Boolean {
    val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return connManager?.isActiveNetworkMetered ?: false
}

fun currentlyHotSpot(context: Context): Boolean =
        currentlyOnWifi(context) && currentlyMetered(context)

fun currentlyCharging(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
        return batteryManager?.isCharging ?: false
    } else {
        // Sticky intent
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val status = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }
}

fun currentlyOnWifi(context: Context): Boolean {
    val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val net = connManager?.activeNetwork
        val netCapabilities = connManager?.getNetworkCapabilities(net)
        if (netCapabilities != null) {
            return netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        for (net in connManager?.allNetworks ?: emptyArray()) {
            val netCapabilities = connManager?.getNetworkCapabilities(net)
            if (netCapabilities != null) {
                return netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
        }
    }
    @Suppress("DEPRECATION")
    val wifi = connManager?.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    return wifi != null && wifi.isConnected
}

fun currentlyConnected(context: Context): Boolean {
    val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val net = connManager?.activeNetwork
        val netInfo = connManager?.getNetworkInfo(net)
        return netInfo != null && netInfo.isConnected
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        for (net in connManager?.allNetworks ?: emptyArray()) {
            val netInfo = connManager?.getNetworkInfo(net)

            if (true == netInfo?.isConnected) {
                return true
            }
        }
        return false
    }
    @Suppress("DEPRECATION")
    for (netInfo in connManager?.allNetworkInfo ?: emptyArray()) {
        if (netInfo.isConnected) {
            return true
        }
    }
    return false
}
