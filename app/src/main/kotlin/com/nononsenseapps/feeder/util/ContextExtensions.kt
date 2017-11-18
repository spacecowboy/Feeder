package com.nononsenseapps.feeder.util

import android.accounts.AccountManager
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.NotificationManagerCompat
import com.nononsenseapps.feeder.db.AUTHORITY
import com.nononsenseapps.feeder.db.AccountService
import com.nononsenseapps.feeder.util.PrefUtils.PREF_SYNC_ONLY_CHARGING

fun Context.setupSync() {
    val account = AccountService.Account()
    val accountManager: AccountManager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

    if (accountManager.addAccountExplicitly(account, null, null)) {
        // New account was added so...
        // Enable syncing
        ContentResolver.setIsSyncable(account, AUTHORITY, 1)
        // Set sync automatic
        ContentResolver.setSyncAutomatically(account, AUTHORITY, true)
    }

    val extras = Bundle()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_REQUIRE_CHARGING,
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_SYNC_ONLY_CHARGING, false))
    }
    if (PrefUtils.shouldSync(this)) {
        // Once per hour: mins * secs
        ContentResolver.addPeriodicSync(account,
                AUTHORITY,
                extras,
                60L * 60L * PrefUtils.synchronizationFrequency(this))
    } else {
        ContentResolver.getPeriodicSyncs(account, AUTHORITY).map {
            ContentResolver.removePeriodicSync(it.account, it.authority, it.extras)
        }
    }
}

val Context.notificationManager: NotificationManagerCompat
    get() = NotificationManagerCompat.from(this)
