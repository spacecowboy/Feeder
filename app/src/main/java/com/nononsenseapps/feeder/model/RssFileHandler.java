package com.nononsenseapps.feeder.model;


import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read and write feeds backup file
 */
public class RssFileHandler {

    public static void writeFile(final Context context, final String path) {
        // Get feeds
        ArrayList<FeedSQL> feeds = getFeeds(context);

        try {
            // Open file
            BufferedWriter bf = new BufferedWriter(new FileWriter(path));

            // For feed, write line
            for (FeedSQL feed : feeds) {
                // TODO title and tag
                bf.write(String.format("%1$s%n", feed.url));
            }

            // Close file
            bf.close();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

    }


    public static void readFile(final Context context, final String path) throws FileNotFoundException {
        // Open file
        File file = new File(path);

        if (!file.isFile()) {
            throw new FileNotFoundException("Specified path was not a file");
        }

        final ArrayList<ContentProviderOperation> operations =
                new ArrayList<ContentProviderOperation>();

        // TODO decide on pattern
        Pattern pattern = Pattern.compile("", 0);

        try {
            BufferedReader bf = new BufferedReader(new FileReader(file));
            // For line in file
            for (String line = bf.readLine(); line != null; line = bf.readLine()) {
                // Parse line
                Matcher m = pattern.matcher(line.trim());
                if (m.find()) {
                    // Add/replace feed in DB
                    // Get existing or new feed with url
                    FeedSQL feed = getFeed(context, m.group(1));
                    // Title
                    if (m.groupCount() > 1 && !m.group(2).isEmpty()) {
                        feed.title = m.group(2);
                    } else if (feed.title == null) {
                        feed.title = feed.getDomain();
                    }
                    // Tag
                    if (m.groupCount() > 2 && !m.group(3).isEmpty()) {
                        feed.tag = m.group(3);
                    }
                    if (feed.tag == null) {
                        feed.tag = "";
                    }
                    // Save it
                    ContentValues values = feed.getContent();
                    if (feed.id < 1) {
                        Uri uri = context.getContentResolver()
                                .insert(FeedSQL.URI_FEEDS, values);
                        feed.id = Long.parseLong(uri.getLastPathSegment());
                    } else {
                        context.getContentResolver().update(Uri.withAppendedPath(
                                        FeedSQL.URI_FEEDS,
                                        Long.toString(feed.id)), values, null,
                                null);
                    }
                    // Upload change
                    RssSyncHelper.uploadFeedAsync(context,
                            feed.id,
                            feed.title,
                            feed.url,
                            feed.tag);
                }
            }
            // Close file
            bf.close();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    private static FeedSQL getFeed(final Context context, final String url) {
        FeedSQL result = null;

        Cursor c = context.getContentResolver()
                .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS, FeedSQL.COL_URL + " IS ?",
                        Util.ToStringArray(url), null);

        try {
            if (c.moveToNext()) {
                result = new FeedSQL(c);
            } else {
                result = new FeedSQL(c);
                result.url = url;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return result;
    }

    private static ArrayList<FeedSQL> getFeeds(final Context context) {
        ArrayList<FeedSQL> feeds = new ArrayList<FeedSQL>();

        Cursor c = context.getContentResolver()
                .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS, null, null, null);

        try {
            while (c.moveToNext()) {
                feeds.add(new FeedSQL(c));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return feeds;
    }
}
