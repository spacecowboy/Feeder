package com.nononsenseapps.feeder.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

import static com.nononsenseapps.feeder.db.RssContentProviderKt.AUTHORITY;
import static com.nononsenseapps.feeder.db.RssContentProviderKt.SCHEME;

/**
 * SQL which handles the feeds
 */
public class FeedSQL implements DbFeed {
    // SQL convention says Table name should be "singular"
    public static final String TABLE_NAME = "Feed";
    // URIs
    public static final Uri URI_FEEDS = Uri.withAppendedPath(
            Uri.parse(SCHEME + AUTHORITY),
            TABLE_NAME);
    // A view which also reports 'unreadcount'
    public static final String VIEWCOUNT_NAME = "WithUnreadCount";
    public static final Uri URI_FEEDSWITHCOUNTS =
            Uri.withAppendedPath(URI_FEEDS, VIEWCOUNT_NAME);
    // A view of distinct tags and their unread counts
    public static final String VIEWTAGS_NAME = "TagsWithUnreadCount";
    public static final Uri URI_TAGSWITHCOUNTS =
            Uri.withAppendedPath(URI_FEEDS, VIEWTAGS_NAME);
    // URI codes, must be unique
    public static final int URICODE = 101;
    public static final int ITEMCODE = 102;
    public static final int VIEWCOUNTCODE = 103;
    public static final int VIEWTAGSCODE = 104;
    // Naming the id column with an underscore is good to be consistent
    // with other Android things. This is ALWAYS needed
    public static final String COL_ID = "_id";
    // These fields can be anything you want.
    public static final String COL_TITLE = "title";
    public static final String COL_CUSTOM_TITLE = "customtitle";
    public static final String COL_URL = "url";
    public static final String COL_TAG = "tag";
    public static final String COL_NOTIFY = "notify";
    // For database projection so order is consistent
    public static final String[] FIELDS = {COL_ID, COL_TITLE, COL_URL,
            COL_TAG, COL_CUSTOM_TITLE, COL_NOTIFY };
    /*
     * The SQL code that creates a Table for storing stuff in.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_TITLE + " TEXT NOT NULL,"
                    + COL_CUSTOM_TITLE + " TEXT NOT NULL,"
                    + COL_URL + " TEXT NOT NULL,"
                    + COL_TAG + " TEXT NOT NULL DEFAULT '',"
                    + COL_NOTIFY + " INTEGER NOT NULL DEFAULT 0,"
                    // Unique constraint
                    + " UNIQUE(" + COL_URL + ") ON CONFLICT REPLACE"
                    + ")";
    // Used on count view
    public static final String COL_UNREADCOUNT = "unreadcount";
    public static final String[] FIELDS_VIEWCOUNT = {COL_ID, COL_TITLE,
            COL_URL, COL_TAG, COL_CUSTOM_TITLE, COL_NOTIFY, COL_UNREADCOUNT};
    public static final String CREATE_COUNT_VIEW =
            "CREATE TEMP VIEW IF NOT EXISTS " + VIEWCOUNT_NAME
                    + " AS SELECT " + Util.arrayToCommaString(FIELDS_VIEWCOUNT)
                    + " FROM " + TABLE_NAME
                    + " LEFT JOIN " + " (SELECT COUNT(1) AS " + COL_UNREADCOUNT
                    + "," + FeedItemSQL.COL_FEED + " FROM " + FeedItemSQL.TABLE_NAME
                    + " WHERE " + FeedItemSQL.COL_UNREAD + " IS 1 " + " GROUP BY "
                    + FeedItemSQL.COL_FEED + ") ON " + TABLE_NAME + "." + COL_ID
                    + " = " + FeedItemSQL.COL_FEED;
    public static final String[] FIELDS_TAGSWITHCOUNT = {COL_ID, COL_TAG,
            COL_UNREADCOUNT};
    public static final String CREATE_TAGS_VIEW =
            "CREATE TEMP VIEW IF NOT EXISTS " + VIEWTAGS_NAME
                    + " AS SELECT " + Util.arrayToCommaString(COL_ID, COL_TAG)
                    //+ ",SUM(" + COL_UNREADCOUNT + ") AS " + COL_UNREADCOUNT
                    + "," + COL_UNREADCOUNT
                    + " FROM " + TABLE_NAME
                    + " LEFT JOIN " + " (SELECT COUNT(1) AS " + COL_UNREADCOUNT
                    + "," + FeedItemSQL.COL_TAG + " AS itemtag "
                    + " FROM " + FeedItemSQL.TABLE_NAME
                    + " WHERE " + FeedItemSQL.COL_UNREAD + " IS 1 "
                    + " GROUP BY " + "itemtag"
                    + ") ON " + TABLE_NAME + "." + COL_TAG
                    + " IS " + "itemtag"
                    + " GROUP BY " + COL_TAG;
    // Fields corresponding to database columns
    public long id = -1;
    public String title = null;
    public String customTitle = null;
    public String url = null;
    public String tag = null;
    public int notify = 0;
    public int unreadCount = -1;

    /**
     * No need to do anything, fields are already set to default values above
     */
    public FeedSQL() {
    }

    public FeedSQL withUnreadCount(int count) {
        this.unreadCount = count;
        return this;
    }

    public FeedSQL withNotify(int flag) {
        this.notify = flag;
        return this;
    }

    public FeedSQL withTag(String tag) {
        this.tag = tag;
        return this;
    }

    public FeedSQL withUrl(String url) {
        this.url = url;
        return this;
    }

    public FeedSQL withTitle(String title) {
        this.title = title;
        return this;
    }

    public FeedSQL withCustomTitle(String title) {
        this.customTitle = title;
        return this;
    }

    public FeedSQL withId(long id) {
        this.id = id;
        return this;
    }

    /**
     * Convert information from the database into a Feed object.
     */
    public FeedSQL(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.id = cursor.getLong(0);
        this.title = cursor.getString(1);
        this.url = cursor.getString(2);
        this.tag = cursor.getString(3);
        this.customTitle = cursor.getString(4);
        this.notify = cursor.getInt(5);
        if (cursor.getColumnCount() >= 7) {
            this.unreadCount = cursor.getInt(6);
        }

    }

    public static void addMatcherUris(UriMatcher sURIMatcher) {
        sURIMatcher.addURI(AUTHORITY, URI_FEEDS.getPath(),
                URICODE);
        sURIMatcher.addURI(AUTHORITY,
                URI_FEEDS.getPath() + "/#", ITEMCODE);
        sURIMatcher.addURI(AUTHORITY,
                URI_FEEDSWITHCOUNTS.getPath(), VIEWCOUNTCODE);
        sURIMatcher.addURI(AUTHORITY,
                URI_TAGSWITHCOUNTS.getPath(), VIEWTAGSCODE);
    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_TITLE, title);
        values.put(COL_CUSTOM_TITLE, customTitle);
        values.put(COL_URL, url);
        if (tag == null)
            tag = "";
        values.put(COL_TAG, tag);
        values.put(COL_NOTIFY, notify);

        return values;
    }

    /**
     * Given a url of http://www.bla.com/foo/bar, this method
     * returns bla.com
     *
     * @return the domain of the url or null
     */
    public String getDomain() {
        if (url == null)
            return null;

        String domain = url;
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

        return domain;
    }



    public static ArrayList<FeedSQL> getFeeds(final Context context, final String where,
                                               final String[] args, final String sort) {
        ArrayList<FeedSQL> feeds = new ArrayList<FeedSQL>();

        Cursor c = context.getContentResolver()
                .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS, where, args, sort);

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

    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        return obj instanceof FeedSQL && Long.valueOf(id).equals(((FeedSQL) obj).id);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getNotify() {
        return notify;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getCustomTitle() {
        return customTitle;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }

    @Override
    public String getUrl() {
        return url;
    }

}
