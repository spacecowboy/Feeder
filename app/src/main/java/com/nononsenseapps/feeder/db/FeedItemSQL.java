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

package com.nononsenseapps.feeder.db;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import org.joda.time.DateTime;
import org.json.JSONObject;

/**
 * SQL which handles items belonging to a Feed
 */
public class FeedItemSQL {
    // SQL convention says Table name should be "singular"
    public static final String TABLE_NAME = "FeedItem";
    // URIs
    public static final Uri URI_FEED_ITEMS = Uri.withAppendedPath(
            Uri.parse(RssContentProvider.SCHEME + RssContentProvider.AUTHORITY),
            TABLE_NAME);
    // URI codes, must be unique
    public static final int URICODE = 201;
    public static final int ITEMCODE = 202;
    private static final String TAG = "FeedItemSQL";


    public static void addMatcherUris(UriMatcher sURIMatcher) {
        sURIMatcher
                .addURI(RssContentProvider.AUTHORITY, URI_FEED_ITEMS.getPath(),
                        URICODE);
        sURIMatcher.addURI(RssContentProvider.AUTHORITY,
                URI_FEED_ITEMS.getPath() + "/#", ITEMCODE);
    }

    // Naming the id column with an underscore is good to be consistent
    // with other Android things. This is ALWAYS needed
    public static final String COL_ID = "_id";
    // These fields can be anything you want.
    public static final String COL_GUID = "guid";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_PLAINTITLE = "plaintitle";
    public static final String COL_PLAINSNIPPET = "plainsnippet";
    public static final String COL_IMAGEURL = "imageurl";
    public static final String COL_ENCLOSURELINK = "enclosurelink";
    public static final String COL_LINK = "link";
    public static final String COL_AUTHOR = "author";
    public static final String COL_PUBDATE = "pubdate";
    public static final String COL_UNREAD = "unread";
    public static final String COL_NOTIFIED = "notified";
    // These fields corresponds to columns in Feed table
    public static final String COL_FEED = "feed";
    public static final String COL_TAG = "tag";
    public static final String COL_FEEDTITLE = "feedtitle";

    // For database projection so order is consistent
    public static final String[] FIELDS =
            {COL_ID, COL_TITLE, COL_DESCRIPTION, COL_PLAINTITLE, COL_PLAINSNIPPET, COL_IMAGEURL,
                    COL_LINK, COL_AUTHOR,
                    COL_PUBDATE, COL_UNREAD, COL_FEED, COL_TAG,
                    COL_ENCLOSURELINK,
                    COL_FEEDTITLE,
            COL_NOTIFIED, COL_GUID};

    /*
     * The SQL code that creates a Table.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_GUID + " TEXT NOT NULL,"
                    + COL_TITLE + " TEXT NOT NULL,"
                    + COL_DESCRIPTION + " TEXT NOT NULL,"
                    + COL_PLAINTITLE + " TEXT NOT NULL,"
                    + COL_PLAINSNIPPET + " TEXT NOT NULL," +
                    COL_IMAGEURL + " TEXT," +
                    COL_LINK + " TEXT," +
                    COL_ENCLOSURELINK + " TEXT," +
                    COL_AUTHOR + " TEXT," +
                    COL_PUBDATE + " TEXT," +
                    COL_UNREAD + " INTEGER NOT NULL DEFAULT 1," +
                    COL_NOTIFIED + " INTEGER NOT NULL DEFAULT 0," +
                    COL_FEED + " INTEGER NOT NULL," +
                    COL_TAG + " TEXT NOT NULL," +
                    COL_FEEDTITLE + " TEXT NOT NULL," +
                    // Handle foreign key stuff
                    " FOREIGN KEY(" + COL_FEED + ") REFERENCES " + FeedSQL.TABLE_NAME + "(" +
                    FeedSQL.COL_ID + ") ON DELETE CASCADE," +
                    // Handle unique constraint
                    " UNIQUE(" + COL_GUID + "," + COL_FEED + ") ON CONFLICT " +
                    "IGNORE"
                    + ")";
    // Trigger which updates Tags of items when feeds' tags are updated
    public static final String TRIGGER_NAME = "trigger_tag_updater";
    public static final String CREATE_TAG_TRIGGER =
            "CREATE TEMP TRIGGER IF NOT EXISTS " + TRIGGER_NAME
            + " AFTER UPDATE OF " +
            Util.arrayToCommaString(FeedSQL.COL_TAG, FeedSQL.COL_TITLE)
            + " ON " + FeedSQL.TABLE_NAME
            + " WHEN "
            + "new." + FeedSQL.COL_TAG + " IS NOT old." + FeedSQL.COL_TAG
            + " OR "
            + "new." + FeedSQL.COL_TITLE + " IS NOT old." + FeedSQL.COL_TITLE
            + " BEGIN "
            + " UPDATE " + FeedItemSQL.TABLE_NAME + " SET " + COL_TAG + " = "
            + " new." + FeedSQL.COL_TAG + ", " + COL_FEEDTITLE + " = "
            + " new." + FeedSQL.COL_TITLE
            + " WHERE " + COL_FEED + " IS old." + FeedSQL.COL_ID
            + "; END";

    // Fields corresponding to database columns
    public long id = -1;
    public String guid = null;
    public String title = null;
    public String description = null;
    public String plaintitle = null;
    public String plainsnippet = null;
    public String imageurl = null;
    public String enclosurelink = null;
    public String author = null;
    private DateTime pubDate = null;
    public String link = null;
    public String tag = null;
    public String feedtitle = null;
    public int notified = 0;

    // Convenience field for parsing json
    private JSONObject jsonobject = null;

    // Convenience field for list views. Only converted first time
    private String domain;

    /**
     * Given an enclosurelink/link of http://www.bla.com/foo/bar, this method
     * returns bla.com
     * @return the domain of the enclosure link, or if null, the link's domain
     */
    public String getDomain() {
        if (domain == null) {
            domain = enclosurelink != null ? enclosurelink : link;
            // Strip http://
            int start = domain.indexOf("://");
            if (start > 0)
                start += 3;
            else
                start = 0;
            // If www, strip that too
            if (domain.indexOf("www.") == start) {
                start += 4;
            }
            // Strip /foo/bar
            int end = domain.indexOf("/", start);
            if (end < 1)
                domain = domain.substring(start);
            else
                domain = domain.substring(start, end);
        }
        return domain;
    }

    /**
     *
     * @return Last bit of a URL. Example, bob/sam/floff will return floff
     */
    public String getEnclosureFilename() {
        if (enclosurelink == null)
            return null;

        String[] parts = enclosurelink.split("/");
        if (parts.length == 0)
            return null;

        // Return last bit
        return parts[parts.length - 1];
    }

    public boolean isUnread() {
        return unread == 1;
    }

    public void setUnread(boolean unread) {
        this.unread = unread ? 1 : 0;
    }

    private int unread = 1;
    public long feed_id = -1;

    /**
     * No need to do anything, fields are already set to default values above
     */
    public FeedItemSQL() {
    }

    /**
     * Convert information from the database into a FeedItem object.
     */
    public FeedItemSQL(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.id = cursor.getLong(0);
        this.title = cursor.getString(1);
        this.description = cursor.getString(2);
        this.plaintitle = cursor.getString(3);
        this.plainsnippet = cursor.getString(4);
        this.imageurl = cursor.getString(5);
        this.link = cursor.getString(6);
        this.author = cursor.getString(7);
        setPubDate(cursor.getString(8));
        this.unread = cursor.getInt(9);
        this.feed_id = cursor.getLong(10);
        this.tag = cursor.getString(11);
        this.enclosurelink = cursor.getString(12);
        this.feedtitle = cursor.getString(13);
        this.notified = cursor.getInt(14);
        this.guid = cursor.getString(15);
    }

    public DateTime getPubDate() {
        return pubDate;
    }

    public void setPubDate(DateTime datetime) {
        pubDate = datetime;
    }

    /**
     * Output the date time in ISO8601 format (yyyy-MM-ddTHH:mm:ss.SSSZZ).
     *
     * @return ISO8601 time formatted string.
     */
    public String getPubDateString() {
        if (pubDate == null)
            return null;

        return pubDate.toString();
    }

    /**
     * Set the date time in ISO8601 format (yyyy-MM-ddTHH:mm:ss.SSSZZ).
     */
    public void setPubDate(String datetime) {
        if (datetime == null) {
            pubDate = null;
        } else {
            try {
                pubDate = DateTime.parse(datetime);
            } catch (Throwable e) {
                Log.e(tag, "Couldn't parse date: " + datetime + ";" + e);
                pubDate = null;
            }
        }
    }

    /**
     * Parse a timestamp and return what should be set on the database item.
     * @param datetime a timestamp to parse. Allowed to be null
     * @return null, or valid timestamp
     */
    public static String getPubDateFromString(String datetime) {
        if (datetime == null) {
            return null;
        }

        try {
            return DateTime.parse(datetime).toString();
        } catch (Exception e) {
            return DateTime.now().toString();
        }
    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_PLAINTITLE, plaintitle);
        values.put(COL_PLAINSNIPPET, plainsnippet);
        values.put(COL_FEED, feed_id);
        values.put(COL_UNREAD, unread);
        values.put(COL_FEEDTITLE, feedtitle);
        values.put(COL_NOTIFIED, notified);

        Util.PutNullable(values, COL_IMAGEURL, imageurl);
        Util.PutNullable(values, COL_LINK, link);
        Util.PutNullable(values, COL_GUID, guid);
        Util.PutNullable(values, COL_AUTHOR, author);
        Util.PutNullable(values, COL_PUBDATE, getPubDateString());
        Util.PutNullable(values, COL_TAG, tag);
        Util.PutNullable(values, COL_ENCLOSURELINK, enclosurelink);

        return values;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        return obj instanceof FeedItemSQL && Long.valueOf(id).equals(((FeedItemSQL) obj).id);

    }
}
