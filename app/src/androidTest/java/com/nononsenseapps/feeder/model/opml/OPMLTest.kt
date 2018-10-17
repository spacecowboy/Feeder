package com.nononsenseapps.feeder.model.opml

import android.content.Context
import androidx.room.Room
import androidx.test.InstrumentationRegistry.getContext
import androidx.test.filters.MediumTest
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*


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
    lateinit var db: AppDatabase

    private var dir: File? = null
    private var path: File? = null

    @Before
    fun setup() {
        // Get internal data dir
        dir = createTempDir()
        path = createTempFile()
        assertTrue("Need to be able to write to data dir $dir", dir!!.canWrite())
        context = getContext()

        db = Room.inMemoryDatabaseBuilder(getContext(), AppDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        // Remove everything in database
    }

    @MediumTest
    @Test
    @Throws(IOException::class)
    fun testWrite() {
        // Create some feeds
        createSampleFeeds()

        writeFile(path!!.absolutePath,
                getTags()
        ) { tag ->
            db.feedDao().loadFeeds(tag = tag)
        }

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

        val parser = OpmlParser(OPMLToRoom(db))
        parser.parseFile(path!!.canonicalPath)

        // Verify database is correct
        val seen = ArrayList<Int>()
        val feeds = db.feedDao().loadFeeds()
        assertFalse("No feeds in DB!", feeds.isEmpty())
        for (feed in feeds) {
            val i = Integer.parseInt(feed.title.replace("\"".toRegex(), ""))
            seen.add(i)
            assertEquals("Title doesn't match", "\"$i\"", feed.title)
            assertEquals("URL doesn't match", URL("http://somedomain$i.com/rss.xml"), feed.url)
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
        var feednew = Feed(
                url = URL("http://somedomain20.com/rss.xml"),
                title = "\"20\"",
                tag = "kapow")
        var id = db.feedDao().insertFeed(feednew)
        feednew = feednew.copy(id = id)
        // Create something that will exist
        var feedold = Feed(
                url = URL("http://somedomain0.com/rss.xml"),
                title = "\"0\"")
        id = db.feedDao().insertFeed(feedold)

        feedold = feedold.copy(id = id)

        // Read file
        val parser = OpmlParser(OPMLToRoom(db))
        parser.parseFile(path!!.canonicalPath)

        // should not kill the existing stuff
        val seen = ArrayList<Int>()
        val feeds = db.feedDao().loadFeeds()
        assertFalse("No feeds in DB!", feeds.isEmpty())
        for (feed in feeds) {
            val i = Integer.parseInt(feed.title.replace("\"".toRegex(), ""))
            seen.add(i)
            assertEquals(URL("http://somedomain$i.com/rss.xml"), feed.url)
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
        val parser = OpmlParser(OPMLToRoom(db))
        parser.parseFile(path!!.absolutePath)
    }

    @SmallTest
    @Test
    @Throws(Exception::class)
    fun testReadMissingFile() {
        val path = File(dir, "lsadflibaslsdfa.opml")
        // Read file
        val parser = OpmlParser(OPMLToRoom(db))
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
        db.runInTransaction {
            db.feedDao().loadFeeds().forEach {
                db.feedDao().deleteFeed(it)
            }
        }
    }

    private fun createSampleFeeds() {
        for (i in 0..9) {
            val feed = Feed(
                    url = URL("http://somedomain$i.com/rss.xml"),
                    title = "\"$i\"",
                    tag = when (i % 3) {
                        1 -> "tag1"
                        2 -> "tag2"
                        else -> ""
                    })

            db.feedDao().insertFeed(feed)
        }
    }

    private fun getTags(): List<String> =
            db.feedDao().loadTags()

    @Test
    @MediumTest
    fun antennaPodOPMLImports() {
        //given
        val opmlStream = javaClass.getResourceAsStream("antennapod-feeds.opml")!!

        //when
        val parser = OpmlParser(OPMLToRoom(db))
        parser.parseInputStream(opmlStream)

        //then
        val feeds = db.feedDao().loadFeeds()
        val tags = db.feedDao().loadTags()
        assertEquals("Expecting 8 feeds", 8, feeds.size)
        assertEquals("Expecting 1 tags (incl empty)", 1, tags.size)

        feeds.forEach { feed ->
            assertEquals("Custom title should be empty", "", feed.customTitle)
            assertEquals("No tag expected", "", feed.tag)
            when (feed.url) {
                URL("http://aliceisntdead.libsyn.com/rss") -> {
                    assertEquals("Alice Isn't Dead", feed.title)
                }
                URL("http://feeds.soundcloud.com/users/soundcloud:users:154104768/sounds.rss") -> {
                    assertEquals("Invisible City", feed.title)
                }
                URL("http://feeds.feedburner.com/PodCastle_Main") -> {
                    assertEquals("PodCastle", feed.title)
                }
                URL("http://www.artofstorytellingshow.com/podcast/storycast.xml") -> {
                    assertEquals("The Art of Storytelling with Brother Wolf", feed.title)
                }
                URL("http://feeds.feedburner.com/TheCleansed") -> {
                    assertEquals("The Cleansed: A Post-Apocalyptic Saga", feed.title)
                }
                URL("http://media.signumuniversity.org/tolkienprof/feed") -> {
                    assertEquals("The Tolkien Professor", feed.title)
                }
                URL("http://nightvale.libsyn.com/rss") -> {
                    assertEquals("Welcome to Night Vale", feed.title)
                }
                URL("http://withinthewires.libsyn.com/rss") -> {
                    assertEquals("Within the Wires", feed.title)
                }
                else -> fail("Unexpected URI. Feed: $feed")
            }
        }
    }

    @Test
    @MediumTest
    fun flymOPMLImports() {
        //given
        val opmlStream = javaClass.getResourceAsStream("Flym_auto_backup.opml")

        //when
        val parser = OpmlParser(OPMLToRoom(db))
        parser.parseInputStream(opmlStream)

        //then
        val feeds = db.feedDao().loadFeeds()
        val tags = db.feedDao().loadTags()
        assertEquals("Expecting 11 feeds", 11, feeds.size)
        assertEquals("Expecting 4 tags (incl empty)", 4, tags.size)

        feeds.forEach { feed ->
            assertEquals("Custom title should be empty", "", feed.customTitle)
            when (feed.url) {
                URL("http://www.smbc-comics.com/rss.php") -> {
                    assertEquals("black humor", feed.tag)
                    assertEquals("SMBC", feed.title)
                }
                URL("http://www.deathbulge.com/rss.xml") -> {
                    assertEquals("black humor", feed.tag)
                    assertEquals("Deathbulge", feed.title)
                }
                URL("http://www.sandraandwoo.com/gaia/feed/") -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Gaia", feed.title)
                }
                URL("http://replaycomic.com/feed/") -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Replay", feed.title)
                }
                URL("http://www.cuttimecomic.com/rss.php") -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Cut Time", feed.title)
                }
                URL("http://www.commitstrip.com/feed/") -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Commit strip", feed.title)
                }
                URL("http://www.sandraandwoo.com/feed/") -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Sandra and Woo", feed.title)
                }
                URL("http://www.awakencomic.com/rss.php") -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Awaken", feed.title)
                }
                URL("http://www.questionablecontent.net/QCRSS.xml") -> {
                    assertEquals("comics", feed.tag)
                    assertEquals("Questionable Content", feed.title)
                }
                URL("https://www.archlinux.org/feeds/news/") -> {
                    assertEquals("Tech", feed.tag)
                    assertEquals("Arch news", feed.title)
                }
                URL("https://grisebouille.net/feed/") -> {
                    assertEquals("Political humour", feed.tag)
                    assertEquals("Grisebouille", feed.title)
                }
                else -> fail("Unexpected URI. Feed: $feed")
            }
        }
    }

    @Test
    @MediumTest
    fun rssGuardOPMLImports1() {
        //given
        val opmlStream = javaClass.getResourceAsStream("rssguard_1.opml")!!

        //when
        val parser = OpmlParser(OPMLToRoom(db))
        parser.parseInputStream(opmlStream)

        //then
        val feeds = db.feedDao().loadFeeds()
        val tags = db.feedDao().loadTags()
        assertEquals("Expecting 30 feeds", 30, feeds.size)
        assertEquals("Expecting 6 tags (incl empty)", 6, tags.size)

        feeds.forEach { feed ->
            assertEquals("Custom title should be empty", "", feed.customTitle)
            when (feed.url) {
                URL("http://www.les-trois-sagesses.org/rss-articles.xml") -> {
                    assertEquals("Religion", feed.tag)
                    assertEquals("Les trois sagesses", feed.title)
                }
                URL("http://www.avrildeperthuis.com/feed/") -> {
                    assertEquals("Amis", feed.tag)
                    assertEquals("avril de perthuis", feed.title)
                }
                URL("http://www.fashioningtech.com/profiles/blog/feed?xn_auth=no") -> {
                    assertEquals("Actu Geek", feed.tag)
                    assertEquals("Everyone's Blog Posts - Fashioning Technology", feed.title)
                }
                URL("http://feeds2.feedburner.com/ChartPorn") -> {
                    assertEquals("Graphs", feed.tag)
                    assertEquals("Chart Porn", feed.title)
                }
                URL("http://www.mosqueedeparis.net/index.php?format=feed&amp;type=atom") -> {
                    assertEquals("Religion", feed.tag)
                    assertEquals("Mosquee de Paris", feed.title)
                }
                URL("http://sourceforge.net/projects/stuntrally/rss") -> {
                    assertEquals("Mainstream update", feed.tag)
                    assertEquals("Stunt Rally", feed.title)
                }
                URL("http://www.mairie6.lyon.fr/cs/Satellite?Thematique=&TypeContenu=Actualite&pagename=RSSFeed&site=Mairie6") -> {
                    assertEquals("", feed.tag)
                    assertEquals("Actualités", feed.title)
                }
            }
        }
    }

    @Test
    @MediumTest
    fun rssGuardOPMLImports2() {
        //given
        val opmlStream = javaClass.getResourceAsStream("rssguard_2.opml")

        //when
        val parser = OpmlParser(OPMLToRoom(db))
        parser.parseInputStream(opmlStream)

        //then
        val feeds = db.feedDao().loadFeeds()
        val tags = db.feedDao().loadTags()
        assertEquals("Expecting 30 feeds", 30, feeds.size)
        assertEquals("Expecting 6 tags (incl empty)", 6, tags.size)

        feeds.forEach { feed ->
            assertEquals("Custom title should be empty", "", feed.customTitle)
            when (feed.url) {
                URL("http://www.les-trois-sagesses.org/rss-articles.xml") -> {
                    assertEquals("Religion", feed.tag)
                    assertEquals("Les trois sagesses", feed.title)
                }
                URL("http://www.avrildeperthuis.com/feed/") -> {
                    assertEquals("Amis", feed.tag)
                    assertEquals("avril de perthuis", feed.title)
                }
                URL("http://www.fashioningtech.com/profiles/blog/feed?xn_auth=no") -> {
                    assertEquals("Actu Geek", feed.tag)
                    assertEquals("Everyone's Blog Posts - Fashioning Technology", feed.title)
                }
                URL("http://feeds2.feedburner.com/ChartPorn") -> {
                    assertEquals("Graphs", feed.tag)
                    assertEquals("Chart Porn", feed.title)
                }
                URL("http://www.mosqueedeparis.net/index.php?format=feed&amp;type=atom") -> {
                    assertEquals("Religion", feed.tag)
                    assertEquals("Mosquee de Paris", feed.title)
                }
                URL("http://sourceforge.net/projects/stuntrally/rss") -> {
                    assertEquals("Mainstream update", feed.tag)
                    assertEquals("Stunt Rally", feed.title)
                }
                URL("http://www.mairie6.lyon.fr/cs/Satellite?Thematique=&TypeContenu=Actualite&pagename=RSSFeed&site=Mairie6") -> {
                    assertEquals("", feed.tag)
                    assertEquals("Actualités", feed.title)
                }
            }
        }
    }
}
