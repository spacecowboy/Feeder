package com.nononsenseapps.feeder.model.opml

import android.content.Context
import android.net.Uri
import android.support.test.InstrumentationRegistry.getContext
import android.support.test.filters.MediumTest
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.nononsenseapps.feeder.db.COL_TAG
import com.nononsenseapps.feeder.db.FeedSQL
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.model.OPMLContenProvider
import com.nononsenseapps.feeder.util.getFeeds
import com.nononsenseapps.feeder.util.insertFeedWith
import com.nononsenseapps.feeder.util.queryTagsWithCounts
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.net.URL


private val sampleFile: List<String> = """<?xml version="1.0" encoding="UTF-8"?>
        |<opml version="1.1">
        |  <head>
        |    <title>
        |      Feeder
        |    </title>
        |  </head>
        |  <body>
        |    <outline title="&quot;0&quot;" text="&quot;0&quot;" type="rss" xmlUrl="http://somedomain0.com/rss.xml"/>
        |    <outline title="&quot;3&quot;" text="&quot;3&quot;" type="rss" xmlUrl="http://somedomain3.com/rss.xml"/>
        |    <outline title="&quot;6&quot;" text="&quot;6&quot;" type="rss" xmlUrl="http://somedomain6.com/rss.xml"/>
        |    <outline title="&quot;9&quot;" text="&quot;9&quot;" type="rss" xmlUrl="http://somedomain9.com/rss.xml"/>
        |    <outline title="tag1" text="tag1">
        |      <outline title="&quot;1&quot;" text="&quot;1&quot;" type="rss" xmlUrl="http://somedomain1.com/rss.xml"/>
        |      <outline title="&quot;4&quot;" text="&quot;4&quot;" type="rss" xmlUrl="http://somedomain4.com/rss.xml"/>
        |      <outline title="&quot;7&quot;" text="&quot;7&quot;" type="rss" xmlUrl="http://somedomain7.com/rss.xml"/>
        |    </outline>
        |    <outline title="tag2" text="tag2">
        |      <outline title="&quot;2&quot;" text="&quot;2&quot;" type="rss" xmlUrl="http://somedomain2.com/rss.xml"/>
        |      <outline title="&quot;5&quot;" text="&quot;5&quot;" type="rss" xmlUrl="http://somedomain5.com/rss.xml"/>
        |      <outline title="&quot;8&quot;" text="&quot;8&quot;" type="rss" xmlUrl="http://somedomain8.com/rss.xml"/>
        |    </outline>
        |  </body>
        |</opml>""".trimMargin().split("\n")

@RunWith(AndroidJUnit4::class)
class OPMLTest {
    private var context: Context? = null

    private var dir: File? = null
    private var path: File? = null

    @Before
    fun setup() {
        // Remove everything in database
        getContext().contentResolver.delete(URI_FEEDS, null, null)
        // Get internal data dir
        dir = createTempDir()
        path = createTempFile()
        assertTrue("Need to be able to write to data dir $dir", dir!!.canWrite())
        context = getContext()
    }

    @After
    fun tearDown() {
        // Remove everything in database
        getContext().contentResolver.delete(URI_FEEDS, null, null)
    }

    @MediumTest
    @Test
    @Throws(IOException::class)
    fun testWrite() {
        // Create some feeds
        createSampleFeeds()

        writeFile(path!!.absolutePath,
                getTags(),
                { tag ->
                    context!!.contentResolver.getFeeds(where = "$COL_TAG IS ?",
                            params = listOf(tag ?: ""))
                })

        //check contents of file
        path!!.bufferedReader().useLines { lines ->
            lines.forEachIndexed { i, line ->
                assertEquals("line $i differed", sampleFile[i], line)
            }
        }
    }

    @MediumTest
    @Test
    @Throws(Exception::class)
    fun testRead() {
        writeSampleFile()

        val parser = OpmlParser(OPMLContenProvider(context))
        parser.parseFile(path!!.canonicalPath)

        // Verify database is correct
        val seen = ArrayList<Int>()
        val feeds = context!!.contentResolver.getFeeds()
        assertFalse("No feeds in DB!", feeds.isEmpty())
        for (feed in feeds) {
            val i = Integer.parseInt(feed.title.replace("\"".toRegex(), ""))
            seen.add(i)
            assertEquals("Title doesn't match", "\"$i\"", feed.title)
            assertEquals("URL doesn't match", "http://somedomain$i.com/rss.xml", feed.url)
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
        writeSampleFile()

        // Create something that does not exist
        var feednew = FeedSQL(
                url = "http://somedomain20.com/rss.xml",
                title = "\"20\"",
                tag = "kapow")
        var id = context!!.contentResolver.insertFeedWith(feednew.asContentValues())
        feednew = feednew.copy(id = id)
        // Create something that will exist
        var feedold = FeedSQL(
                url = "http://somedomain0.com/rss.xml",
                title = "\"0\"")
        id = context!!.contentResolver.insertFeedWith(feedold.asContentValues())

        feedold = feedold.copy(id = id)

        // Read file
        val parser = OpmlParser(OPMLContenProvider(context))
        parser.parseFile(path!!.canonicalPath)

        // should not kill the existing stuff
        val seen = ArrayList<Int>()
        val feeds = context!!.contentResolver.getFeeds()
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
        //val path = File(dir, "feeds.opml")

        path!!.bufferedWriter().use {
            it.write("This is just some bullshit in the file\n")
        }

        // Read file
        val parser = OpmlParser(OPMLContenProvider(context))
        parser.parseFile(path!!.absolutePath)
    }

    @SmallTest
    @Test
    @Throws(Exception::class)
    fun testReadMissingFile() {
        val path = File(dir, "lsadflibaslsdfa.opml")
        // Read file
        val parser = OpmlParser(OPMLContenProvider(context))
        var raised = false
        try {
            parser.parseFile(path.absolutePath)
        } catch (e: IOException) {
            raised = true
        }

        assertTrue("Should raise exception", raised)
    }

    @Throws(IOException::class)
    private fun writeSampleFile() {
        // Use test write to write the sample file
        testWrite()
        // Then delete all feeds again
        context!!.contentResolver.delete(URI_FEEDS, null, null)
    }

    private fun createSampleFeeds() {
        for (i in 0..9) {
            val feed = FeedSQL(
                    url = "http://somedomain$i.com/rss.xml",
                    title = "\"$i\"",
                    tag = when (i % 3) {
                        1 -> "tag1"
                        2 -> "tag2"
                        else -> ""
                    })

            context!!.contentResolver.insertFeedWith(feed.asContentValues())
        }
    }

    private fun getTags(): ArrayList<String> {
        val tags = ArrayList<String>()

        context!!.contentResolver.queryTagsWithCounts(columns = listOf(COL_TAG)) {
            while (it.moveToNext()) {
                tags.add(it.getString(0))
            }
        }

        return tags
    }

    @Test
    @MediumTest
    fun antennaPodOPMLImports() {
        //given
        val opmlStream = javaClass.getResourceAsStream("antennapod-feeds.opml")

        //when
        val parser = OpmlParser(OPMLContenProvider(context))
        parser.parseInputStream(opmlStream)

        //then
        val feeds = context!!.contentResolver.getFeeds()
        val tags = feeds.map { it.tag }.distinct().toList()
        assertEquals("Expecting 8 feeds", 8, feeds.size)
        assertEquals("Expecting 1 tags (incl empty)", 1, tags.size)

        feeds.forEach { feed ->
            assertEquals("Custom title should be empty", "", feed.customTitle)
            assertEquals("No tag expected", "", feed.tag)
            when (feed.url) {
                "http://aliceisntdead.libsyn.com/rss" -> {
                    assertEquals("Alice Isn't Dead", feed.title)
                }
                "http://feeds.soundcloud.com/users/soundcloud:users:154104768/sounds.rss" -> {
                    assertEquals("Invisible City", feed.title)
                }
                "http://feeds.feedburner.com/PodCastle_Main" -> {
                    assertEquals("PodCastle", feed.title)
                }
                "http://www.artofstorytellingshow.com/podcast/storycast.xml" -> {
                    assertEquals("The Art of Storytelling with Brother Wolf", feed.title)
                }
                "http://feeds.feedburner.com/TheCleansed" -> {
                    assertEquals("The Cleansed: A Post-Apocalyptic Saga", feed.title)
                }
                "http://media.signumuniversity.org/tolkienprof/feed" -> {
                    assertEquals("The Tolkien Professor", feed.title)
                }
                "http://nightvale.libsyn.com/rss" -> {
                    assertEquals("Welcome to Night Vale", feed.title)
                }
                "http://withinthewires.libsyn.com/rss" -> {
                    assertEquals("Within the Wires", feed.title)
                }
                else -> fail("Unexpected URI. Feed: $feed")
            }
        }
    }

    @Test
    @MediumTest
    fun FlymOPMLImports() {
        //given
        val opmlStream = javaClass.getResourceAsStream("Flym_auto_backup.opml")

        //when
        val parser = OpmlParser(OPMLContenProvider(context))
        parser.parseInputStream(opmlStream)

        //then
        val feeds = context!!.contentResolver.getFeeds()
        val tags = feeds.map { it.tag }.distinct().toList()
        assertEquals("Expecting 11 feeds", 11, feeds.size)
        assertEquals("Expecting 4 tags (incl empty)", 4, tags.size)

        feeds.forEach { feed ->
            assertEquals("Custom title should be empty", "", feed.customTitle)
            when (feed.url) {
                "http://www.smbc-comics.com/rss.php" -> {
                    assertEquals("black humor", feed.tag)
                    assertEquals("SMBC", feed.title)
                }
                "http://www.deathbulge.com/rss.xml" -> {
                    assertEquals("black humor", feed.tag)
                    assertEquals("Deathbulge", feed.title)
                }
                "http://www.sandraandwoo.com/gaia/feed/" -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Gaia", feed.title)
                }
                "http://replaycomic.com/feed/" -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Replay", feed.title)
                }
                "http://www.cuttimecomic.com/rss.php" -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Cut Time", feed.title)
                }
                "http://www.commitstrip.com/feed/" -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Commit strip", feed.title)
                }
                "http://www.sandraandwoo.com/feed/" -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Sandra and Woo", feed.title)
                }
                "http://www.awakencomic.com/rss.php" -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Awaken", feed.title)
                }
                "http://www.questionablecontent.net/QCRSS.xml" -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Questionable Content", feed.title)
                }
                "https://www.archlinux.org/feeds/news/" -> {
                    assertEquals("Tech", feed.tag)
                    assertEquals("Arch news", feed.title)
                }
                "https://grisebouille.net/feed/" -> {
                    assertEquals("Political humour", feed.tag)
                    assertEquals("Grisebouille", feed.title)
                }
                else -> fail("Unexpected URI. Feed: $feed")
            }
        }
    }
}
