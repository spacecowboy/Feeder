package com.nononsenseapps.feeder.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;

public class SystemUtils {
    public static boolean currentlyMetered(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.isActiveNetworkMetered();
    }

    public static boolean currentlyCharging(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return batteryManager.isCharging();
        } else {
            // Sticky intent
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
        }
    }

    public static boolean currentlyOnWifi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network net = connManager.getActiveNetwork();
            NetworkInfo netInfo = connManager.getNetworkInfo(net);
            return netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
        } else {
            for (Network net : connManager.getAllNetworks()) {
                NetworkInfo netInfo = connManager.getNetworkInfo(net);

                if (netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean currentlyConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network net = connManager.getActiveNetwork();
            NetworkInfo netInfo = connManager.getNetworkInfo(net);
            return netInfo.isConnected();
        } else {
            for (Network net : connManager.getAllNetworks()) {
                NetworkInfo netInfo = connManager.getNetworkInfo(net);

                if (netInfo.isConnected()) {
                    return true;
                }
            }
            return false;
        }
    }
}
