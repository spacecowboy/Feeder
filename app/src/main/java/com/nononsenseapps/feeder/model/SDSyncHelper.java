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

import android.content.Context;
import android.os.Environment;

import com.nononsenseapps.feeder.db.FeedSQL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Synchronize feeds with SD card
 */
public class SDSyncHelper {
    public static final String FEEDS_FILENAME = "feeds.txt";
    public static final String DELETES_FILENAME = ".feeds_deleted.txt";
    public static final String DEFAULT_DIR = Environment
            .getExternalStorageDirectory().toString() + "/Feeder";

    public static void ensureDir() {
        File d = new File(DEFAULT_DIR);
        if (!d.isDirectory()) {
            d.mkdir();
        }
    }

    public static String feedToStringEntry(FeedSQL feed) {
        return feed.url + "\n";
    }

    public static String readFeedsFile(final Context context) {
        // TODO
        return null;
    }

    public static void addFeedToFile(final Context context, final FeedSQL feed) {
        String s = readFeedsFile(context);
        s += feedToStringEntry(feed);
        // TODO
    }

    public static void writeFeedsToFile(final Context context) {
        ensureDir();

        final StringBuilder contents = new StringBuilder();
        final ArrayList<FeedSQL> feeds = RssSyncHelper.getFeeds(context, null);
        for (FeedSQL feed: feeds) {
            contents.append(feedToStringEntry(feed));
        }

        final File file = new File(DEFAULT_DIR, FEEDS_FILENAME);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(contents.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null)
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
