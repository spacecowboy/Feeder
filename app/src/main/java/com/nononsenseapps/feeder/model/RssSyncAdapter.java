/*
 * Copyright (c) 2016 Jonas Kalderstam.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.feeder.model;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.util.PrefUtils;


public class RssSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "FeederRssSyncAdapter";

    public static final String FEED_ADDED_BROADCAST =
            "feeder.nononsenseapps.RSS_FEED_ADDED_BROADCAST";
    public static final String SYNC_BROADCAST =
            "feeder.nononsenseapps.RSS_SYNC_BROADCAST";
    public static final String SYNC_BROADCAST_IS_ACTIVE = "IS_ACTIVE";


    /**
     * Creates an {@link android.content.AbstractThreadedSyncAdapter}.
     *
     * @param context        the {@link android.content.Context} that this is
     *                       running within.
     * @param autoInitialize if true then sync requests that have
     *                       {@link android.content.ContentResolver#SYNC_EXTRAS_INITIALIZE}
     *                       set will be internally handled by
     *                       {@link android.content.AbstractThreadedSyncAdapter}
     *                       by calling
     *                       {@link android.content.ContentResolver#setIsSyncable(android.accounts.Account,
     *                       String, int)} with 1 if it
     */
    public RssSyncAdapter(final Context context, final boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    /**
     * Creates an {@link android.content.AbstractThreadedSyncAdapter}.
     *
     * @param context            the {@link android.content.Context} that this
     *                           is running within.
     * @param autoInitialize     if true then sync requests that have
     *                           {@link android.content.ContentResolver#SYNC_EXTRAS_INITIALIZE}
     *                           set will be internally handled by
     *                           {@link android.content.AbstractThreadedSyncAdapter}
     *                           by calling
     *                           {@link android.content.ContentResolver#setIsSyncable(android.accounts.Account,
     *                           String, int)} with 1 if it is currently set to
     *                           <0.
     * @param allowParallelSyncs if true then allow syncs for different
     *                           accounts to run at the same time, each in
     *                           their own thread.  This must be consistent
     *                           with the setting.
     */
    public RssSyncAdapter(final Context context, final boolean autoInitialize,
                          final boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    /**
     * Perform a sync for this account. SyncAdapter-specific parameters may
     * be specified in extras, which is guaranteed to not be null. Invocations
     * of this method are guaranteed to be serialized.
     *
     * @param account    the account that should be synced
     * @param extras     SyncAdapter-specific parameters
     * @param authority  the authority of this sync request
     * @param provider   a ContentProviderClient that points to the
     *                   ContentProvider for this
     *                   authority
     * @param syncResult SyncAdapter-specific parameters
     */
    @Override
    public void onPerformSync(final Account account, final Bundle extras,
                              final String authority, final ContentProviderClient provider,
                              final SyncResult syncResult) {
        final Intent bcast = new Intent(SYNC_BROADCAST)
                .putExtra(SYNC_BROADCAST_IS_ACTIVE, true);
        if (shouldSync(extras)) {
            // By default, if a sync is performed, we can wait at least an hour
            // to the next one. Unit is seconds
            syncResult.delayUntil = 60L * 60L;

            // Broadcast start of sync
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(bcast);

            RssLocalSync.syncFeeds(getContext(), extras.getLong(FeedSQL.COL_ID, -1), extras.getString(FeedSQL.COL_TAG, ""));
        } else {
            // Delay at least 10 minutes
            syncResult.delayUntil = 60L * 10L;
        }

        // Broadcast end of sync - always
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast
                (bcast.putExtra(SYNC_BROADCAST_IS_ACTIVE, false));
    }

    /**
     * @return true if sync pre-requisites are true
     */
    private boolean shouldSync(Bundle extras) {
        if (!currentlyConnected()) {
            return false;
        }

        if (isManualSync(extras)) {
            // User has actively requested a sync
            return true;
        }

        if (PrefUtils.shouldSyncOnlyWhenCharging(getContext()) && !currentlyCharging()) {
            return false;
        }

        if (currentlyOnWifi()) {
            return PrefUtils.shouldSyncOnHotSpots(getContext()) || !currentlyMetered();
        } else if (PrefUtils.shouldSyncOnlyOnWIfi(getContext())) {
            return false;
        }

        // Mobile connection but nothing indicates we should avoid that
        return true;
    }

    private boolean currentlyMetered() {
        ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.isActiveNetworkMetered();
    }

    private boolean currentlyCharging() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BatteryManager batteryManager = (BatteryManager) getContext().getSystemService(Context.BATTERY_SERVICE);
            return batteryManager.isCharging();
        } else {
            // Sticky intent
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getContext().registerReceiver(null, ifilter);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
        }
    }

    private boolean currentlyOnWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
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

    private boolean currentlyConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
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

    private static boolean isManualSync(Bundle extras) {
        return extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
    }
}
