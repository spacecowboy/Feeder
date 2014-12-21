/*
 * Copyright (c) 2014 Jonas Kalderstam.
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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Define a Service that returns an IBinder for the sync adapter class,
 * allowing
 * the sync adapter framework to call onPerformSync().
 */
public class LocalRssSyncAdapterService extends Service {
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    // Storage for an instance of the sync adapter
    private static LocalRssSyncAdapter sSyncAdapter = null;

    /*
    * Instantiate the sync adapter object.
    */
    @Override
    public void onCreate() {
/*
* Create the sync adapter as a singleton. Set the sync adapter as
* syncable Disallow parallel syncs
*/
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter =
                        new LocalRssSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
/*
* Get the object that allows external processes to call
* onPerformSync(). The object is created in the base class code when
* the SyncAdapter constructors call super()
*/
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
