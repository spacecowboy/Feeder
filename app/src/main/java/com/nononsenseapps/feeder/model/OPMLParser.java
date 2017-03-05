package com.nononsenseapps.feeder.model;

import com.nononsenseapps.feeder.db.FeedSQL;
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

import static com.nononsenseapps.feeder.model.OPMLWriter.unescape;

public class OPMLParser implements ContentHandler {

    private static final String TAG = "OPMLParser";
    private final Parser mParser;
    private final OPMLParserToDatabase opmlParserToDatabase;
    private String mCurrentTag;
    private int ignoring = 0;
    private boolean isFeedTag;

    public OPMLParser(final OPMLParserToDatabase opmlParserToDatabase) {
        this.opmlParserToDatabase = opmlParserToDatabase;
        this.mParser = new Parser();
        mParser.setContentHandler(this);

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
    public void startElement(final String uri, final String localName, final String qName,
                             final Attributes attr) throws SAXException {
        // Not allowing nesting below feeds
        if (ignoring > 0) {
            ignoring += 1;
            return;
        }

        if ("outline".equals(localName)) {

            if ("rss".equals(attr.getValue("type"))) {
                isFeedTag = true;
                // Yes, tagsoup seems to make the tags lowercase
                FeedSQL feed = opmlParserToDatabase.getFeed(attr.getValue("xmlurl"))
                        .withTitle(unescape(attr.getValue("title")))
                        .withCustomTitle(unescape(attr.getValue("title")))
                        .withTag(mCurrentTag);

                if (feed.url != null && feed.title != null) {
                    opmlParserToDatabase.saveFeed(feed);
                }
            } else if (mCurrentTag == null) {
                mCurrentTag = unescape(attr.getValue("title"));
            } else {
                ignoring += 1;
            }
        }
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
