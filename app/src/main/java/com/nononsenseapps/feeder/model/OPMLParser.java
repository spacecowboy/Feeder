package com.nononsenseapps.feeder.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.db.Util;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OPMLParser implements ContentHandler {

    private static final String TAG = "OPMLParser";
    private final Context mContext;
    private final Parser mParser;
    private String mCurrentTag;
    private int ignoring = 0;
    private boolean isFeedTag;

    public OPMLParser(final Context context) {
        this.mContext = context;
        this.mParser = new Parser();
        mParser.setContentHandler(this);

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
                result = new FeedSQL();
                result.url = url;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return result;
    }

    public void parseFile(final String path) throws IOException, SAXException {
        // Open file
        File file = new File(path);

        parseInputStream(new FileInputStream(file));
    }

    public void parseInputStream(final InputStream is) throws IOException, SAXException {
        this.mCurrentTag = null;
        ignoring = 0;
        this.isFeedTag = false;

        InputSource src = new InputSource(is);
        mParser.parse(src);
        is.close();
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
    }

    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {
        // Not allowing nesting below feeds
        if (ignoring > 0) {
            ignoring += 1;
            return;
        }

        if ("outline".equals(localName)) {

            if ("rss".equals(attr.getValue("type"))) {
                isFeedTag = true;
                // Yes, tagsoup seems to make the tags lowercase
                FeedSQL feed = getFeed(mContext, attr.getValue("xmlurl"));
                feed.title = attr.getValue("title");
                feed.tag = mCurrentTag;

                if (feed.url != null && feed.title != null) {
                    saveFeed(feed);
                }
            } else if (mCurrentTag == null) {
                mCurrentTag = attr.getValue("title");
            } else {
                ignoring += 1;
            }
        }
    }

    private void saveFeed(final FeedSQL feed) {
        // Save it
        ContentValues values = feed.getContent();
        if (feed.id < 1) {
            Uri uri = mContext.getContentResolver()
                    .insert(FeedSQL.URI_FEEDS, values);
            feed.id = Long.parseLong(uri.getLastPathSegment());
        } else {
            mContext.getContentResolver().update(Uri.withAppendedPath(
                            FeedSQL.URI_FEEDS,
                            Long.toString(feed.id)), values, null,
                    null);
        }
        // Upload change
        RssSyncHelper.uploadFeedAsync(mContext,
                feed.id,
                feed.title,
                feed.url,
                feed.tag);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        // Ignoring, return
        if (ignoring > 0) {
            ignoring -= 1;
            return;
        }

        if (isFeedTag) {
            isFeedTag = false;
        } else {
            // Must be a tag-tag
            mCurrentTag = null;
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
    }

    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
    }

    @Override
    public void skippedEntity(final String name) throws SAXException {
    }
}
