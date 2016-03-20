package com.nononsenseapps.feeder.model;


import android.util.Log;

import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.function.Function;
import com.nononsenseapps.feeder.function.Supplier;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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


    public OPMLWriter() {
    }

    public void writeFile(final String path,
                          Supplier<Iterable<String>> tagSupplier,
                          Function<String, Iterable<FeedSQL>> feedsWithTag)
            throws FileNotFoundException {
        writeOutputStream(new FileOutputStream(path), tagSupplier, feedsWithTag);
    }

    public void writeOutputStream(final OutputStream os,
                                  Supplier<Iterable<String>> tagSupplier,
                                  Function<String, Iterable<FeedSQL>> feedsWithTag) {
        try {
            // Open file
            BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(os));

            bf.write(STARTOPML);

            for (String tag : tagSupplier.get()) {
                if (tag != null && !tag.isEmpty()) {
                    bf.write(String.format(STARTTAGFMT, escape(tag)));
                }

                for (FeedSQL feed: feedsWithTag.apply(tag)) {
                    if (tag != null && !tag.isEmpty()) {
                        // Indent inside tags
                        bf.write("  ");
                    }
                    bf.write(String.format(FEEDFMT, escape(feed.title), escape(feed.url)));
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
     * @return String with xml stuff escaped
     */
    static String escape(final String s) {
        return s.replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&apos;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    /**
     *
     * @param s string to unescape
     * @return String with xml stuff unescaped
     */
    static String unescape(final String s) {
        return s.replaceAll("&quot;", "\"")
                .replaceAll("&apos;", "'")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&");
    }
}
