package com.nononsenseapps.feeder.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import com.nononsenseapps.feeder.util.minusMinutes
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import org.threeten.bp.Instant
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue
import org.junit.Ignore

@RunWith(AndroidJUnit4::class)
@MediumTest
@Ignore("Needs some love")
class RssLocalSyncKtTest {
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private val filesDir = getApplicationContext<FeederApplication>().filesDir

    private val di by closestDI(getApplicationContext() as Context)
    private val feedParser: FeedParser by di.instance()

    val server = MockWebServer()

    val responses = mutableMapOf<URL?, MockResponse>()

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Before
    fun setup() {
        server.start()
    }

    private suspend fun insertFeed(title: String, url: URL, raw: String, isJson: Boolean = true): Long {
        val id = testDb.db.feedDao().insertFeed(
            Feed(
                title = title,
                url = url,
                tag = ""
            )
        )

        server.dispatcher = object: Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return responses.getOrDefault(request.requestUrl?.toUrl(), MockResponse().setResponseCode(404))
            }
        }

        responses[url] = MockResponse().apply {
            setResponseCode(200)
            if (isJson) {
                setHeader("Content-Type", "application/json")
            }
            setBody(raw)
        }

        return id
    }

    @Test
    fun syncCowboyJsonWorks() = runBlocking {
        val cowboyJsonId = insertFeed(
            "cowboyjson", server.url("/feed.json").toUrl(),
            cowboyJson
        )

        runBlocking {
            syncFeeds(
                di = di,
                filesDir = filesDir,
                feedId = cowboyJsonId
            )
        }

        assertEquals(
            "Unexpected number of items in feed",
            10,
            testDb.db.feedItemDao().loadFeedItemsInFeedDesc(cowboyJsonId).size
        )
    }

    @Test
    fun syncCowboyAtomWorks() = runBlocking {
        val cowboyAtomId = insertFeed(
            "cowboyatom", server.url("/atom.xml").toUrl(),
            cowboyAtom, isJson = false
        )

        runBlocking {
            syncFeeds(
                di = di,
                filesDir = filesDir,
                feedId = cowboyAtomId
            )
        }

        assertEquals(
            "Unexpected number of items in feed",
            15,
            testDb.db.feedItemDao().loadFeedItemsInFeedDesc(cowboyAtomId).size
        )
    }

    @Test
    fun syncAllWorks() = runBlocking {
        val cowboyJsonId = insertFeed(
            "cowboyjson", server.url("/feed.json").toUrl(),
            cowboyJson
        )
        val cowboyAtomId = insertFeed(
            "cowboyatom", server.url("/atom.xml").toUrl(),
            cowboyAtom, isJson = false
        )

        runBlocking {
            syncFeeds(
                di = di,
                filesDir = filesDir,
                feedId = ID_UNSET,
                parallel = true
            )
        }

        assertEquals(
            "Unexpected number of items in feed",
            10,
            testDb.db.feedItemDao().loadFeedItemsInFeedDesc(cowboyJsonId).size
        )

        assertEquals(
            "Unexpected number of items in feed",
            15,
            testDb.db.feedItemDao().loadFeedItemsInFeedDesc(cowboyAtomId).size
        )
    }

    @Test
    fun responsesAreNotParsedUnlessFeedHashHasChanged() = runBlocking {
        val cowboyJsonId = insertFeed(
            "cowboyjson", server.url("/feed.json").toUrl(),
            cowboyJson
        )

        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = cowboyJsonId, forceNetwork = true)
            testDb.db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync.toEpochMilli() > 0)
                assertTrue("Feed should have a valid response hash", feed.responseHash > 0)
                // "Long time" ago, but not unset
                testDb.db.feedDao().updateFeed(feed.copy(lastSync = Instant.ofEpochMilli(999L)))
            }
            syncFeeds(di = di, filesDir = filesDir, feedId = cowboyJsonId, forceNetwork = true)
        }

        assertEquals("Feed should have been fetched twice", 2, server.requestCount)

        assertNotEquals(
            "Cached response should still have updated feed last sync",
            999L,
            testDb.db.feedDao().loadFeed(cowboyJsonId)!!.lastSync.toEpochMilli()
        )
    }

    @Test
    fun feedsSyncedWithin15MinAreIgnored() = runBlocking {
        val cowboyJsonId = insertFeed(
            "cowboyjson", server.url("/feed.json").toUrl(),
            cowboyJson
        )

        val fourteenMinsAgo = Instant.now().minusMinutes(14)
        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = cowboyJsonId, forceNetwork = true)
            testDb.db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync.toEpochMilli() > 0)
                assertTrue("Feed should have a valid response hash", feed.responseHash > 0)

                testDb.db.feedDao().updateFeed(feed.copy(lastSync = fourteenMinsAgo))
            }
            syncFeeds(
                di = di, filesDir = filesDir,
                feedId = cowboyJsonId, forceNetwork = false, minFeedAgeMinutes = 15
            )
        }

        assertEquals(
            "Recently synced feed should not get a second network request",
            1, server.requestCount
        )

        assertEquals(
            "Last sync should not have changed",
            fourteenMinsAgo,
            testDb.db.feedDao().loadFeed(cowboyJsonId)!!.lastSync
        )
    }

    @Test
    fun feedsSyncedWithin15MinAreNotIgnoredWhenForcingNetwork() = runBlocking {
        val cowboyJsonId = insertFeed(
            "cowboyjson", server.url("/feed.json").toUrl(),
            cowboyJson
        )

        val fourteenMinsAgo = Instant.now().minusMinutes(14)
        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = cowboyJsonId, forceNetwork = true)
            testDb.db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync.toEpochMilli() > 0)
                assertTrue("Feed should have a valid response hash", feed.responseHash > 0)

                testDb.db.feedDao().updateFeed(feed.copy(lastSync = fourteenMinsAgo))
            }
            syncFeeds(
                di = di, filesDir = filesDir,
                feedId = cowboyJsonId, forceNetwork = true, minFeedAgeMinutes = 15
            )
        }

        assertEquals("Request should have been sent due to forced network", 2, server.requestCount)

        assertNotEquals(
            "Last sync should have changed",
            fourteenMinsAgo,
            testDb.db.feedDao().loadFeed(cowboyJsonId)!!.lastSync
        )
    }

    @Test
    fun feedShouldNotBeUpdatedIfRequestFails() = runBlocking {
        val response = MockResponse().also {
            it.setResponseCode(500)
        }
        server.enqueue(response)

        val url = server.url("/feed.json")

        val failingJsonId = testDb.db.feedDao().insertFeed(
            Feed(
                title = "failJson",
                url = URL("$url"),
                tag = ""
            )
        )

        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = failingJsonId)
        }

        assertEquals(
            "Last sync should not have been updated",
            Instant.EPOCH,
            testDb.db.feedDao().loadFeed(failingJsonId)!!.lastSync
        )

        // Assert the feed was retrieved
        assertEquals("/feed.json", server.takeRequest().path)
    }

    @Test
    fun feedWithNoUniqueLinksGetsSomeGeneratedGUIDsFromTitles() = runBlocking {
        val response = MockResponse().also {
            it.setResponseCode(200)
            it.setBody(String(nixosRss.readBytes()))
        }
        server.enqueue(response)

        val url = server.url("/news-rss.xml")

        val feedId = testDb.db.feedDao().insertFeed(
            Feed(
                title = "NixOS",
                url = URL("$url"),
                tag = ""
            )
        )

        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = feedId)
        }

        // Assert the feed was retrieved
        assertEquals("/news-rss.xml", server.takeRequest().path)

        val items = testDb.db.feedItemDao().loadFeedItemsInFeedDesc(feedId)
        assertEquals(
            "Unique IDs should have been generated for items",
            99, items.size
        )

        // Should be unique to item so that it stays the same after updates
        assertEquals(
            "Unexpected ID",
            "NixOS 18.09 released-NixOS 18.09 “Jellyfish” has been released, the tenth stable release branch. See the release notes for details. You can get NixOS 18.09 ISOs and VirtualBox appliances from the download page. For inform",
            items.first().guid
        )
    }

    @Test
    fun feedWithNoDatesShouldGetSomeGenerated() = runBlocking {
        val response = MockResponse().also {
            it.setResponseCode(200)
            it.setBody(fooRss(2))
        }
        server.enqueue(response)

        val url = server.url("/rss")

        val feedId = testDb.db.feedDao().insertFeed(
            Feed(
                url = URL("$url")
            )
        )

        val beforeSyncTime = Instant.now()

        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = feedId)
        }

        // Assert the feed was retrieved
        assertEquals("/rss", server.takeRequest().path)

        val items = testDb.db.feedItemDao().loadFeedItemsInFeedDesc(feedId)

        assertNotNull(
            "Item should have gotten a pubDate generated",
            items[0].pubDate
        )

        assertNotEquals(
            "Items should have distinct pubDates",
            items[0].pubDate, items[1].pubDate
        )

        assertTrue(
            "The pubDate should be after 'before sync time'",
            items[0].pubDate!!.toInstant() > beforeSyncTime
        )

        // Compare ID to compare insertion order (and thus pubdate compared to raw feed)
        assertTrue("The pubDates' magnitude should match descending iteration order") {
            items[0].guid == "https://foo.bar/1" &&
                items[1].guid == "https://foo.bar/2" &&
                items[0].pubDate!! > items[1].pubDate!!
        }
    }

    @Test
    fun feedWithNoDatesShouldNotGetOverriddenDatesNextSync() = runBlocking {
        server.enqueue(
            MockResponse().also {
                it.setResponseCode(200)
                it.setBody(fooRss(1))
            }
        )
        server.enqueue(
            MockResponse().also {
                it.setResponseCode(200)
                it.setBody(fooRss(2))
            }
        )

        val url = server.url("/rss")

        val feedId = testDb.db.feedDao().insertFeed(
            Feed(
                url = URL("$url")
            )
        )

        // Sync first time
        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = feedId)
        }

        // Assert the feed was retrieved
        assertEquals("/rss", server.takeRequest(100, TimeUnit.MILLISECONDS)!!.path)

        val firstItem = testDb.db.feedItemDao().loadFeedItemsInFeedDesc(feedId).let { items ->
            assertNotNull(
                "Item should have gotten a pubDate generated",
                items[0].pubDate
            )

            items[0]
        }

        // Sync second time
        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = feedId, forceNetwork = true)
        }

        // Assert the feed was retrieved
        assertEquals("/rss", server.takeRequest(100, TimeUnit.MILLISECONDS)!!.path)

        testDb.db.feedItemDao().loadFeedItemsInFeedDesc(feedId).let { items ->
            assertEquals(
                "Should be 2 items in feed",
                2, items.size
            )

            val item = items.last()

            assertEquals(
                "Making sure we are comparing the same item",
                firstItem.id, item.id
            )

            assertEquals(
                "Pubdate should not have changed",
                firstItem.pubDate, item.pubDate
            )
        }
    }

    @Test
    fun feedShouldNotBeCleanedToHaveLessItemsThanActualFeed() = runBlocking {
        val feedItemCount = 9
        server.enqueue(
            MockResponse().also {
                it.setResponseCode(200)
                it.setBody(fooRss(feedItemCount))
            }
        )

        val url = server.url("/rss")

        val feedId = testDb.db.feedDao().insertFeed(
            Feed(
                url = URL("$url")
            )
        )

        val maxFeedItemCount = 5

        // Sync first time
        runBlocking {
            syncFeeds(
                di = di, filesDir = filesDir,
                feedId = feedId,
                maxFeedItemCount = maxFeedItemCount
            )
        }

        // Assert the feed was retrieved
        assertEquals("/rss", server.takeRequest(100, TimeUnit.MILLISECONDS)!!.path)

        testDb.db.feedItemDao().loadFeedItemsInFeedDesc(feedId).let { items ->
            assertEquals(
                "Feed should have no less items than in the raw feed even if that's more than cleanup count",
                feedItemCount, items.size
            )
        }
    }

    @Test
    fun slowResponseShouldBeOk() = runBlocking {
        val url = server.url("/atom.xml").toUrl()
        val cowboyAtomId = insertFeed("cowboy", url, cowboyAtom, isJson = false)
        responses[url]!!.throttleBody(1024 * 100, 29, TimeUnit.SECONDS)

        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = cowboyAtomId)
        }

        assertEquals(
            "Feed should have been parsed from slow response",
            15,
            testDb.db.feedItemDao().loadFeedItemsInFeedDesc(cowboyAtomId).size
        )
    }

    @Test
    fun verySlowResponseShouldBeCancelled() = runBlocking {
        val url = server.url("/atom.xml").toUrl()
        val cowboyAtomId = insertFeed("cowboy", url, cowboyAtom, isJson = false)
        responses[url]!!.throttleBody(1024 * 100, 31, TimeUnit.SECONDS)

        runBlocking {
            syncFeeds(di = di, filesDir = filesDir, feedId = cowboyAtomId)
        }

        assertEquals(
            "Feed should not have been parsed from extremely slow response",
            0,
            testDb.db.feedItemDao().loadFeedItemsInFeedDesc(cowboyAtomId).size
        )
    }

    val nixosRss: InputStream
        get() = javaClass.getResourceAsStream("rss_nixos.xml")!!

    val cowboyJson: String
        get() = String(javaClass.getResourceAsStream("cowboyprogrammer_feed.json")!!.use { it.readBytes() })

    val cowboyAtom: String
        get() = String(javaClass.getResourceAsStream("cowboyprogrammer_atom.xml")!!.use { it.readBytes() })

    fun fooRss(itemsCount: Int = 1): String {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                <channel>
                <title>Foo Feed</title>
                <link>https://foo.bar</link>
                ${
        (1..itemsCount).map {
            """
                <item>
                  <title>Foo Item $it</title>
                  <link>https://foo.bar/$it</link>
                  <description>Woop woop $it</description>
                </item>
            """.trimIndent()
        }.fold("") { acc, s ->
            "$acc\n$s"
        }
        }
                </channel>
                </rss>
        """.trimIndent()
    }
}
