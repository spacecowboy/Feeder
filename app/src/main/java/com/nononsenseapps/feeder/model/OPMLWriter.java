package com.nononsenseapps.feeder.model;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.Util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class OPMLWriter {

    private static final String STARTOPML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<opml version=\"1.1\">\n" +
            "  <head>\n" +
            "    <title>Feeder</title>\n" +
            "  </head>\n" +
            "  <body>\n";
    private static final String ENDOPML = "  </body>\n" +
            "</opml>";
    private static final String ENDTAGFMT = "    </outline>\n";
    private static final String STARTTAGFMT = "    <outline title=\"%1$s\" text=\"%1$s\">\n";
    private static final String FEEDFMT = "    <outline title=\"%1$s\" text=\"%1$s\" type=\"rss\" xmlUrl=\"%2$s\"/>\n";
    private static final String TAG = "OPMLWriter";


    private final Context mContext;

    public OPMLWriter(final Context context) {
        this.mContext = context;
    }

    public void writeFile(final String path) throws FileNotFoundException {
        writeOutputStream(new FileOutputStream(path));
    }

    public void writeOutputStream(final OutputStream os) {
        try {
            // Open file
            BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(os));

            bf.write(STARTOPML);

            for (String tag : getTags()) {
                if (tag != null && !tag.isEmpty()) {
                    bf.write(String.format(STARTTAGFMT, escape(tag)));
                }

                for (FeedSQL feed: getFeedsWithTag(tag)) {
                    if (tag != null && !tag.isEmpty()) {
                        // Indent inside tags
                        bf.write("  ");
                    }
                    bf.write(String.format(FEEDFMT, escape(feed.title), feed.url));
                }

                if (tag != null && !tag.isEmpty()) {
                    bf.write(ENDTAGFMT);
                }
            }

            bf.write(ENDOPML);

            // Close file
            bf.close();
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    /**
     *
     * @param s string to escape
     * @return String with " replaced by \"
     */
    private String escape(final String s) {
        return s.replaceAll("\"", "\\\"");
    }

    private ArrayList<FeedSQL> getFeedsWithTag(final String tag) {
        ArrayList<FeedSQL> feeds = new ArrayList<FeedSQL>();

        final String where = FeedSQL.COL_TAG + " IS ?";
        final String[] args = Util.ToStringArray(tag == null ? "": tag);
        Cursor c = mContext.getContentResolver()
                .query(FeedSQL.URI_FEEDS, FeedSQL.FIELDS,
                        where, args, null);

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

    public ArrayList<String> getTags() {
        ArrayList<String> tags = new ArrayList<String>();

        Cursor c = mContext.getContentResolver()
                .query(FeedSQL.URI_TAGSWITHCOUNTS, Util.ToStringArray(FeedSQL.COL_TAG), null, null, null);

        try {
            while (c.moveToNext()) {
                tags.add(c.getString(0));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return tags;
    }
}
