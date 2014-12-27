package com.nononsenseapps.feeder;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.model.OPMLParser;
import com.nononsenseapps.feeder.model.OPMLWriter;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class OPMLTest extends AndroidTestCase {
    private final String[] sampleFile = new String[]{
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<opml version=\"1.1\">",
            "  <head>",
            "    <title>Feeder</title>",
            "  </head>",
            "  <body>",
            "    <outline title=\"0\" text=\"0\" type=\"rss\" xmlUrl=\"http://somedomain0.com/rss.xml\"/>",
            "    <outline title=\"3\" text=\"3\" type=\"rss\" xmlUrl=\"http://somedomain3.com/rss.xml\"/>",
            "    <outline title=\"6\" text=\"6\" type=\"rss\" xmlUrl=\"http://somedomain6.com/rss.xml\"/>",
            "    <outline title=\"9\" text=\"9\" type=\"rss\" xmlUrl=\"http://somedomain9.com/rss.xml\"/>",
            "    <outline title=\"tag1\" text=\"tag1\">",
            "      <outline title=\"1\" text=\"1\" type=\"rss\" xmlUrl=\"http://somedomain1.com/rss.xml\"/>",
            "      <outline title=\"4\" text=\"4\" type=\"rss\" xmlUrl=\"http://somedomain4.com/rss.xml\"/>",
            "      <outline title=\"7\" text=\"7\" type=\"rss\" xmlUrl=\"http://somedomain7.com/rss.xml\"/>",
            "    </outline>",
            "    <outline title=\"tag2\" text=\"tag2\">",
            "      <outline title=\"2\" text=\"2\" type=\"rss\" xmlUrl=\"http://somedomain2.com/rss.xml\"/>",
            "      <outline title=\"5\" text=\"5\" type=\"rss\" xmlUrl=\"http://somedomain5.com/rss.xml\"/>",
            "      <outline title=\"8\" text=\"8\" type=\"rss\" xmlUrl=\"http://somedomain8.com/rss.xml\"/>",
            "    </outline>",
            "  </body>",
            "</opml>"
    };
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
        ArrayList<FeedSQL> feeds = FeedSQL.getFeeds(context, null, null, null);
        assertFalse("No feeds in DB!", feeds.isEmpty());
        for (FeedSQL feed : feeds) {
            int i = Integer.parseInt(feed.title);
            seen.add(i);
            assertEquals("http://somedomain" + i + ".com/rss.xml", feed.url);
            assertEquals(Integer.toString(i), feed.title);
            if (i % 3 == 1) {
                assertEquals("tag1", feed.tag);
            } else if (i % 3 == 2) {
                assertEquals("tag2", feed.tag);
            } else {
                assertEquals("", feed.tag);
            }
        }
        for (Integer i = 0; i < 10; i++) {
            assertTrue("Missing " + i, seen.contains(i));
        }
    }

    @MediumTest
    public void testReadExisting() throws IOException, SAXException {
        String path = writeSampleFile();

        // Create something that does not exist
        final FeedSQL feednew = new FeedSQL();
        feednew.url = "http://somedomain" + 20 + ".com/rss.xml";
        feednew.title = Integer.toString(20);
        feednew.tag = "kapow";
        Uri uri = context.getContentResolver().insert(FeedSQL.URI_FEEDS, feednew.getContent());
        feednew.id = Long.parseLong(uri.getLastPathSegment());
        // Create something that wil exist
        final FeedSQL feedold = new FeedSQL();
        feedold.url = "http://somedomain" + 0 + ".com/rss.xml";
        feedold.title = Integer.toString(0);
        uri = context.getContentResolver().insert(FeedSQL.URI_FEEDS, feedold.getContent());
        feedold.id = Long.parseLong(uri.getLastPathSegment());

        // Read file
        OPMLParser parser = new OPMLParser(context);
        parser.parseFile(path);

        // should not kill the existing stuff
        ArrayList<Integer> seen = new ArrayList<Integer>();
        ArrayList<FeedSQL> feeds = FeedSQL.getFeeds(context, null, null, null);
        assertFalse("No feeds in DB!", feeds.isEmpty());
        for (FeedSQL feed : feeds) {
            int i = Integer.parseInt(feed.title);
            seen.add(i);
            assertEquals("http://somedomain" + i + ".com/rss.xml", feed.url);
            assertEquals(Integer.toString(i), feed.title);

            if (i == 20) {
                assertEquals("Should not have changed", feednew.id, feed.id);
                assertEquals("Should not have changed", feednew.title, feed.title);
                assertEquals("Should not have changed", feednew.url, feed.url);
                assertEquals("Should not have changed", feednew.tag, feed.tag);
            } else if (i % 3 == 1) {
                assertEquals("tag1", feed.tag);
            } else if (i % 3 == 2) {
                assertEquals("tag2", feed.tag);
            } else {
                assertEquals("", feed.tag);
            }

            if (i == 0) {
                // Make sure id is same as old
                assertEquals("Id should be same still", feedold.id, feed.id);
            }
        }
        assertTrue("Missing 20", seen.contains(20));
        for (Integer i = 0; i < 10; i++) {
            assertTrue("Missing " + i, seen.contains(i));
        }
    }

    @MediumTest
    public void testReadBadFile() throws IOException, SAXException {
        File dir = Environment.getExternalStorageDirectory();
        File path = new File(dir, "feeds.opml");

        BufferedWriter bw = new BufferedWriter(new FileWriter(path));

        bw.write("This is just some bullshit in the file\n");

        bw.close();

        // Read file
        OPMLParser parser = new OPMLParser(context);
        parser.parseFile(path.getAbsolutePath());
    }

    @SmallTest
    public void testReadMissingFile() throws SAXException {
        File dir = Environment.getExternalStorageDirectory();
        File path = new File(dir, "lsadflibaslsdfa.opml");
        // Read file
        OPMLParser parser = new OPMLParser(context);
        boolean raised = false;
        try {
            parser.parseFile(path.getAbsolutePath());
        } catch (IOException e) {
            raised = true;
        }

        assertTrue("Should raise exception", raised);
    }


    @MediumTest
    public void testWrite() throws IOException {
        // Create some feeds
        createSampleFeeds();

        File dir = Environment.getExternalStorageDirectory();
        File path = new File(dir, "feeds.opml");

        OPMLWriter writer = new OPMLWriter(context);
        writer.writeFile(path.getAbsolutePath());

        //check contents of file
        BufferedReader br = new BufferedReader(new FileReader(path));

        int i = -1;
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            i++;

            assertEquals(sampleFile[i], line);
        }

        br.close();
    }

    private String writeSampleFile() throws IOException {
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
