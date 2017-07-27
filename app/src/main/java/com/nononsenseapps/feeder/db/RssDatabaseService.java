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

package com.nononsenseapps.feeder.db;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import com.nononsenseapps.feeder.util.ContentResolverExtensionsKt;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class RssDatabaseService extends IntentService {
    private static final String ACTION_SET_NOTIFY = "com.nononsenseapps.feeder.db.action.SET_NOTIFY";
    private static final String ACTION_MARK_FEED_AS_READ = "com.nononsenseapps.feeder.db.action.MARK_FEED_AS_READ";
    private static final String ACTION_MARK_ITEM_AS_READ = "com.nononsenseapps.feeder.db.action.MARK_ITEM_AS_READ";
    private static final String ACTION_MARK_ITEM_AS_UNREAD = "com.nononsenseapps.feeder.db.action.MARK_ITEM_AS_UNREAD";

    private static final String EXTRA_NOTIFY = "com.nononsenseapps.feeder.db.extra.EXTRA_NOTIFY";
    private static final String EXTRA_TAG = "com.nononsenseapps.feeder.db.extra.EXTRA_TAG";
    private static final String EXTRA_ID = "com.nononsenseapps.feeder.db.extra.EXTRA_ID";


    public RssDatabaseService() {
        super("RssDatabaseService");
    }

    /**
     * If id is valid, use that. Else, use tag.
     *
     * @param context
     * @param notify
     * @param id
     * @param tag
     */
    public static void setNotify(Context context, boolean notify, long id, String tag) {
        Intent intent = new Intent(context, RssDatabaseService.class);
        intent.setAction(ACTION_SET_NOTIFY);
        intent.putExtra(EXTRA_NOTIFY, notify);
        intent.putExtra(EXTRA_TAG, tag);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    }

    /**
     *
     * If id is valid, use that. Else, use tag.
     * @param context
     * @param id
     * @param tag
     */
    public static void markFeedAsRead(Context context, long id, String tag) {
        Intent intent = new Intent(context, RssDatabaseService.class);
        intent.setAction(ACTION_MARK_FEED_AS_READ);
        intent.putExtra(EXTRA_TAG, tag);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    }

    public static void markItemAsRead(Context context, long id) {
        Intent intent = new Intent(context, RssDatabaseService.class);
        intent.setAction(ACTION_MARK_ITEM_AS_READ);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    }

    public static void markItemAsUnread(Context context, long id) {
        Intent intent = new Intent(context, RssDatabaseService.class);
        intent.setAction(ACTION_MARK_ITEM_AS_UNREAD);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SET_NOTIFY.equals(action)) {
                final boolean notify = intent.getBooleanExtra(EXTRA_NOTIFY, false);
                final String tag = intent.getStringExtra(EXTRA_TAG);
                final long id = intent.getLongExtra(EXTRA_ID, -1);

                if (id > 0) {
                    ContentResolverExtensionsKt.setNotify(getContentResolver(), id, notify);
                } else {
                    ContentResolverExtensionsKt.setNotify(getContentResolver(), tag, notify);
                }
            } else if (ACTION_MARK_FEED_AS_READ.equals(action)) {
                final String tag = intent.getStringExtra(EXTRA_TAG);
                final long id = intent.getLongExtra(EXTRA_ID, -1);

                if (id > 0) {
                    ContentResolverExtensionsKt.markFeedAsRead(getContentResolver(), id);
                } else if (tag != null) {
                    ContentResolverExtensionsKt.markTagAsRead(getContentResolver(), tag);
                } else {
                    ContentResolverExtensionsKt.markAllAsRead(getContentResolver());
                }
            } else if (ACTION_MARK_ITEM_AS_READ.equals(action)) {
                final long id = intent.getLongExtra(EXTRA_ID, -1);

                if (id > 0) {
                    ContentResolverExtensionsKt.markItemAsRead(getContentResolver(), id, true);
                }
            } else if (ACTION_MARK_ITEM_AS_UNREAD.equals(action)) {
                final long id = intent.getLongExtra(EXTRA_ID, -1);

                if (id > 0) {
                    ContentResolverExtensionsKt.markItemAsUnread(getContentResolver(), id);
                }
            }
        }
    }
}
