package com.nononsenseapps.feeder

import android.content.Context
import android.support.test.InstrumentationRegistry.getContext
import android.support.test.filters.MediumTest
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.nononsenseapps.feeder.db.FeedSQL
import com.nononsenseapps.feeder.model.OPMLContenProvider
import com.nononsenseapps.feeder.model.OPMLParser
import com.nononsenseapps.feeder.model.opml.writeFile
import com.nononsenseapps.feeder.util.queryFeeds
import com.nononsenseapps.feeder.util.queryTagsWithCounts
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException


private val sampleFile = arrayOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
        "<opml version=\"1.1\">",
        "  <head>",
        "    <title>Feeder</title>",
        "  </head>", "  <body>",
        "    <outline title=\"&quot;0&quot;\" text=\"&quot;0&quot;\" type=\"rss\" xmlUrl=\"http://somedomain0.com/rss.xml\"/>",
        "    <outline title=\"&quot;3&quot;\" text=\"&quot;3&quot;\" type=\"rss\" xmlUrl=\"http://somedomain3.com/rss.xml\"/>",
        "    <outline title=\"&quot;6&quot;\" text=\"&quot;6&quot;\" type=\"rss\" xmlUrl=\"http://somedomain6.com/rss.xml\"/>",
        "    <outline title=\"&quot;9&quot;\" text=\"&quot;9&quot;\" type=\"rss\" xmlUrl=\"http://somedomain9.com/rss.xml\"/>",
        "    <outline title=\"tag1\" text=\"tag1\">",
        "      <outline title=\"&quot;1&quot;\" text=\"&quot;1&quot;\" type=\"rss\" xmlUrl=\"http://somedomain1.com/rss.xml\"/>",
        "      <outline title=\"&quot;4&quot;\" text=\"&quot;4&quot;\" type=\"rss\" xmlUrl=\"http://somedomain4.com/rss.xml\"/>",
        "      <outline title=\"&quot;7&quot;\" text=\"&quot;7&quot;\" type=\"rss\" xmlUrl=\"http://somedomain7.com/rss.xml\"/>",
        "    </outline>", "    <outline title=\"tag2\" text=\"tag2\">",
        "      <outline title=\"&quot;2&quot;\" text=\"&quot;2&quot;\" type=\"rss\" xmlUrl=\"http://somedomain2.com/rss.xml\"/>",
        "      <outline title=\"&quot;5&quot;\" text=\"&quot;5&quot;\" type=\"rss\" xmlUrl=\"http://somedomain5.com/rss.xml\"/>",
        "      <outline title=\"&quot;8&quot;\" text=\"&quot;8&quot;\" type=\"rss\" xmlUrl=\"http://somedomain8.com/rss.xml\"/>",
        "    </outline>",
        "  </body>",
        "</opml>")

private val fmtTitle = "\"%d\""

@RunWith(AndroidJUnit4::class)
class OPMLTest {
    private var context: Context? = null

    private var dir: File? = null

    @Before
    fun setup() {
        // Remove everything in database
        getContext().contentResolver.delete(FeedSQL.URI_FEEDS, null, null)
        // Get internal data dir
        dir = getContext().filesDir
        context = getContext()
    }

    @After
    fun tearDown() {
        // Remove everything in database
        getContext().contentResolver.delete(FeedSQL.URI_FEEDS, null, null)
    }

    @MediumTest
    @Test
    @Throws(IOException::class)
    fun testWrite() {
        // Create some feeds
        createSampleFeeds()
        val path = File(dir, "feeds.opml")

        writeFile(path.absolutePath,
                getTags(),
                { tag ->
                    val feeds = ArrayList<FeedSQL>()

                    context!!.contentResolver.queryFeeds(where = "${FeedSQL.COL_TAG} IS ?",
                            params = listOf(tag ?: "")) {
                        while (it.moveToNext()) {
                            feeds.add(FeedSQL(it))
                        }
                    }

                    feeds
                })

        //check contents of file
        path.bufferedReader().useLines { lines ->
            lines.forEachIndexed { i, line ->
                assertEquals(sampleFile[i], line)
            }
        }
    }

    @MediumTest
    @Test
    @Throws(Exception::class)
    fun testRead() {
        val path = writeSampleFile()

        val parser = OPMLParser(OPMLContenProvider(context))
        parser.parseFile(path)

        // Verify database is correct
        val seen = ArrayList<Int>()
        val feeds = FeedSQL.getFeeds(context, null, null, null)
        assertFalse("No feeds in DB!", feeds.isEmpty())
        for (feed in feeds) {
            val i = Integer.parseInt(feed.title.replace("\"".toRegex(), ""))
            seen.add(i)
            assertEquals("http://somedomain$i.com/rss.xml", feed.url)
            assertEquals("\"" + Integer.toString(i) + "\"", feed.title)
            if (i % 3 == 1) {
                assertEquals("tag1", feed.tag)
            } else if (i % 3 == 2) {
                assertEquals("tag2", feed.tag)
            } else {
                assertEquals("", feed.tag)
            }
        }
        for (i in 0..9) {
            assertTrue("Missing " + i, seen.contains(i))
        }
    }

    @MediumTest
    @Test
    @Throws(Exception::class)
    fun testReadExisting() {
        val path = writeSampleFile()

        // Create something that does not exist
        val feednew = FeedSQL()
        feednew.url = "http://somedomain" + 20 + ".com/rss.xml"
        feednew.title = String.format(fmtTitle, 20)
        feednew.tag = "kapow"
        var uri = context!!.contentResolver.insert(FeedSQL.URI_FEEDS, feednew.content)
        feednew.id = java.lang.Long.parseLong(uri.lastPathSegment)
        // Create something that wil exist
        val feedold = FeedSQL()
        feedold.url = "http://somedomain" + 0 + ".com/rss.xml"
        feedold.title = Integer.toString(0)
        uri = context!!.contentResolver.insert(FeedSQL.URI_FEEDS, feedold.content)
        feedold.id = java.lang.Long.parseLong(uri.lastPathSegment)

        // Read file
        val parser = OPMLParser(OPMLContenProvider(context))
        parser.parseFile(path)

        // should not kill the existing stuff
        val seen = ArrayList<Int>()
        val feeds = FeedSQL.getFeeds(context, null, null, null)
        assertFalse("No feeds in DB!", feeds.isEmpty())
        for (feed in feeds) {
            val i = Integer.parseInt(feed.title.replace("\"".toRegex(), ""))
            seen.add(i)
            assertEquals("http://somedomain$i.com/rss.xml", feed.url)
            assertEquals("\"" + Integer.toString(i) + "\"", feed.title)

            if (i == 20) {
                assertEquals("Should not have changed", feednew.id, feed.id)
                assertEquals("Should not have changed", feednew.title, feed.title)
                assertEquals("Should not have changed", feednew.url, feed.url)
                assertEquals("Should not have changed", feednew.tag, feed.tag)
            } else if (i % 3 == 1) {
                assertEquals("tag1", feed.tag)
            } else if (i % 3 == 2) {
                assertEquals("tag2", feed.tag)
            } else {
                assertEquals("", feed.tag)
            }

            if (i == 0) {
                // Make sure id is same as old
                assertEquals("Id should be same still", feedold.id, feed.id)
            }
        }
        assertTrue("Missing 20", seen.contains(20))
        for (i in 0..9) {
            assertTrue("Missing " + i, seen.contains(i))
        }
    }

    @MediumTest
    @Test
    @Throws(Exception::class)
    fun testReadBadFile() {
        val path = File(dir, "feeds.opml")

        path.bufferedWriter().use {
            it.write("This is just some bullshit in the file\n")
        }

        // Read file
        val parser = OPMLParser(OPMLContenProvider(context))
        parser.parseFile(path.absolutePath)
    }

    @SmallTest
    @Test
    @Throws(Exception::class)
    fun testReadMissingFile() {
        val path = File(dir, "lsadflibaslsdfa.opml")
        // Read file
        val parser = OPMLParser(OPMLContenProvider(context))
        var raised = false
        try {
            parser.parseFile(path.absolutePath)
        } catch (e: IOException) {
            raised = true
        }

        assertTrue("Should raise exception", raised)
    }

    @Throws(IOException::class)
    private fun writeSampleFile(): String {
        val path = File(dir, "feeds.opml")

        // Use test write to write the sample file
        testWrite()
        // Then delete all feeds again
        context!!.contentResolver.delete(FeedSQL.URI_FEEDS, null, null)

        return path.absolutePath
    }

    private fun createSampleFeeds() {
        for (i in 0..9) {
            val feed = FeedSQL()
            feed.url = "http://somedomain$i.com/rss.xml"
            feed.title = "\"$i\""
            if (i % 3 == 1) {
                feed.tag = "tag1"
            } else if (i % 3 == 2) {
                feed.tag = "tag2"
            }

            context!!.contentResolver.insert(FeedSQL.URI_FEEDS, feed.content)
        }
    }

    private fun getTags(): ArrayList<String> {
        val tags = ArrayList<String>()

        context!!.contentResolver.queryTagsWithCounts(columns = listOf(FeedSQL.COL_TAG)) {
            while (it.moveToNext()) {
                tags.add(it.getString(0))
            }
        }

        return tags
    }
}
