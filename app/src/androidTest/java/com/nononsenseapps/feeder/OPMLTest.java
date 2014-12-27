package com.nononsenseapps.feeder;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Environment;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.model.OPMLParser;
import com.nononsenseapps.feeder.model.OPMLWriter;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class OPMLTest extends AndroidTestCase {
    private Context context;
    private ContentResolver resolver;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = getContext();
        resolver = context.getContentResolver();
        // Remove everything in database
        context.getContentResolver().delete(FeedSQL.URI_FEEDS, null, null);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        // Remove everything in database
        context.getContentResolver().delete(FeedSQL.URI_FEEDS, null, null);
    }

    @MediumTest
    public void testRead() throws IOException, SAXException {
        String path = writeSampleFile();

        OPMLParser parser = new OPMLParser(context);
        parser.parseFile(path);

        // Verify database is correct
        ArrayList<Integer> seen = new ArrayList<Integer>();
        for (FeedSQL feed: FeedSQL.getFeeds(context, null, null, null)) {
            int i = Integer.parseInt(feed.title);
            seen.add(i);
            assertEquals(feed.url, "http://somedomain" + i + ".com/rss.xml");
            assertEquals(feed.title, Integer.toString(i));
            if (i % 3 == 1) {
                assertEquals(feed.tag, "tag1");
            } else if (i % 3 == 2) {
                assertEquals(feed.tag, "tag2");
            } else {
                assertNull(feed.tag);
            }
        }
        for (int i = 0; i < 10; i++) {
            assertTrue(seen.contains(i));
        }
    }

    @MediumTest
    public void testReadExisting() throws IOException, SAXException {
        String path = writeSampleFile();

        // TODO should not kill the existing stuff
    }

    @MediumTest
    public void testWrite() {
        // Create some feeds
        createSampleFeeds();

        File dir = Environment.getExternalStorageDirectory();
        File path = new File(dir, "feeds.opml");

        OPMLWriter writer = new OPMLWriter(context);
        writer.writeFile(path.getAbsolutePath());

        // TODO check contents of file
    }

    private String writeSampleFile() {
        File dir = Environment.getExternalStorageDirectory();
        File path = new File(dir, "feeds.opml");

        // Use test write to write the sample file
        testWrite();
        // Then delete all feeds again
        context.getContentResolver().delete(FeedSQL.URI_FEEDS, null, null);

        return path.getAbsolutePath();
    }

    private void createSampleFeeds() {
        for (int i = 0; i < 10; i++) {
            FeedSQL feed = new FeedSQL();
            feed.url = "http://somedomain" + i + ".com/rss.xml";
            feed.title = Integer.toString(i);
            if (i % 3 == 1) {
                feed.tag = "tag1";
            } else if (i % 3 == 2) {
                feed.tag = "tag2";
            }

            context.getContentResolver().insert(FeedSQL.URI_FEEDS, feed.getContent());
        }
    }
}
