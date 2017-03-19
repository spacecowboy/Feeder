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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.db.FeedItemSQL;
import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.Util;
import com.nononsenseapps.feeder.ui.FeedActivity;
import com.nononsenseapps.feeder.ui.ReaderActivity;

import java.util.ArrayList;

/**
 * Handles notifications
 */
public class RssNotifications {

    private final static int mId = 73583;

    /**
     * Notify new items
     *
     * @param context
     */
    public static void notify(final Context context) {
        ArrayList<FeedItemSQL> feedItems = getItemsToNotify(context);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (feedItems.isEmpty()) {
            // Dismiss since it should be empty
            nm.cancel(mId);
            return;
        }

        NotificationCompat.Style notStyle;

        String contentTitle, contentText = "";
        // Many items
        if (feedItems.size() == 1) {
            notStyle = new NotificationCompat.BigTextStyle();
            contentTitle = "1 new RSS-story";
            ((NotificationCompat.BigTextStyle) notStyle).setBigContentTitle(contentTitle);
            contentText = feedItems.get(0).feedtitle + " \u2014 " + feedItems.get(0).plaintitle;
            ((NotificationCompat.BigTextStyle) notStyle).bigText(contentText);
        } else {
            notStyle = new NotificationCompat.InboxStyle();
            contentTitle = feedItems.size() + " new RSS-stories";
            ((NotificationCompat.InboxStyle) notStyle).setBigContentTitle(contentTitle);

            for (FeedItemSQL item : feedItems) {
                ((NotificationCompat.InboxStyle) notStyle).addLine(item.feedtitle + " \u2014 " + item.plaintitle);
                contentText += item.feedtitle + " \u2014 " + item.plaintitle + "\n";
            }
        }

        // Actions: markAsRead, enclosureplay, openlink
        // Priority: Low
        // Category: CATEGORY_SOCIAL
        // Style: INBOX

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        // TODO icon
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_rss)
                .setLargeIcon(bm)
                .setNumber(feedItems.size())
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        if (feedItems.size() == 1) {
            if (feedItems.get(0).description != null && !feedItems.get(0).description.isEmpty()) {
                Intent intent = new Intent(context, ReaderActivity.class);
                ReaderActivity.setRssExtras(intent, feedItems.get(0));
                mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            } else {
                Intent intent = new Intent(context, FeedActivity.class);
                // TODO Set feed arguments
                mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            }

            if (feedItems.get(0).enclosurelink != null) {
                mBuilder.addAction(R.drawable.ic_action_av_play_circle_outline,
                        "Open file", PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_VIEW,
                                Uri.parse(feedItems.get(0).enclosurelink)), PendingIntent.FLAG_UPDATE_CURRENT));
            }
            mBuilder.addAction(R.drawable.ic_action_location_web_site,
                    "Open in browser", PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_VIEW,
                            Uri.parse(feedItems.get(0).link)), PendingIntent.FLAG_UPDATE_CURRENT));
        } else {
            Intent intent = new Intent(context, FeedActivity.class);
            mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
        // TODO?
        // mBuilder.addAction(R.drawable.ic_action_done_all, context.getString(R.string.mark_all_as_read), asreadpe);


        notStyle.setBuilder(mBuilder);

// mId allows you to update the notification later on.
        nm.notify(mId, notStyle.build());
    }

    public static ArrayList<FeedItemSQL> getItemsToNotify(final Context context) {
        ArrayList<FeedItemSQL> feedItems = new ArrayList<>();

        String feeds = getFeedsToNotify(context);

        if (feeds == null)
            return feedItems;

        Cursor c = context.getContentResolver()
                .query(FeedItemSQL.URI_FEED_ITEMS, FeedItemSQL.FIELDS,
                        FeedItemSQL.COL_FEED + " IN (" + feeds + ") AND " + FeedItemSQL.COL_NOTIFIED + " IS 0 AND " + FeedItemSQL.COL_UNREAD + " IS 1",
                        null, null);

        try {
            while (c.moveToNext()) {
                feedItems.add(new FeedItemSQL(c));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return feedItems;
    }

    private static String getFeedsToNotify(Context context) {
        String result = null;

        Cursor c = context.getContentResolver()
                .query(FeedSQL.URI_FEEDS, Util.ToStringArray(FeedSQL.COL_ID),
                        FeedSQL.COL_NOTIFY + " IS 1",
                        null, null);

        try {
            while (c.moveToNext()) {
                if (result == null)
                    result = "";

                result += "" + Long.toString(c.getLong(0)) + ",";
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        if (result != null)
            result = result.substring(0, result.length() - 1);
        return result;
    }
}
