package com.nononsenseapps.feeder.model

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import com.nononsenseapps.feeder.util.feedParser
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
@MediumTest
class RssLocalSyncKtTest {
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private val feederApplication: FeederApplication = getApplicationContext()
    private val feedParser = spyk(feederApplication.feedParser)

    private var cowboyJsonId: Long = -1
    private var cowboyAtomId: Long = -1

    val server = MockWebServer()

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Before
    fun setupTestDb() {
        cowboyJsonId = testDb.db.feedDao().insertFeed(Feed(
                title = "cowboyjson",
                url = URL("https://cowboyprogrammer.org/feed.json"),
                tag = ""
        ))

        cowboyAtomId = testDb.db.feedDao().insertFeed(Feed(
                title = "cowboyatom",
                url = URL("https://cowboyprogrammer.org/atom.xml"),
                tag = ""
        ))
    }

    @Before
    fun setupHttpCache() {
        FeedParser.setup(feederApplication.cacheDir!!)
    }

    @Test
    fun syncCowboyJsonWorks() {
        runBlocking {
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = cowboyJsonId)
        }

        assertEquals(
                "Unexpected number of items in feed",
                10,
                testDb.db.feedItemDao().loadFeedItemsInFeed(cowboyJsonId).size)
    }

    @Test
    fun syncCowboyAtomWorks() {
        runBlocking {
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = cowboyAtomId)
        }

        assertEquals(
                "Unexpected number of items in feed",
                15,
                testDb.db.feedItemDao().loadFeedItemsInFeed(cowboyAtomId).size)
    }

    @Test
    fun syncAllWorks() {
        runBlocking {
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = ID_UNSET, parallel = true)
        }

        assertEquals(
                "Unexpected number of items in feed",
                10,
                testDb.db.feedItemDao().loadFeedItemsInFeed(cowboyJsonId).size)

        assertEquals(
                "Unexpected number of items in feed",
                15,
                testDb.db.feedItemDao().loadFeedItemsInFeed(cowboyAtomId).size)
    }

    @Test
    fun responsesAreNotParsedUnlessFeedHashHasChanged() {
        runBlocking {
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = cowboyJsonId, forceNetwork = true)
            testDb.db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync.millis > 0)
                assertTrue("Feed should have a valid response hash", feed.responseHash > 0)
                // "Long time" ago, but not unset
                testDb.db.feedDao().updateFeed(feed.copy(lastSync = DateTime(999L, DateTimeZone.UTC)))
            }
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = cowboyJsonId, forceNetwork = true)
        }

        verify(exactly = 1) {
            feedParser.parseFeedResponse(any(), any())
        }

        assertNotEquals(
                "Cached response should still have updated feed last sync",
                999L,
                testDb.db.feedDao().loadFeed(cowboyJsonId)!!.lastSync.millis)
    }

    @Test
    fun feedsSyncedWithin15MinAreIgnored() {
        val fourteenMinsAgo = DateTime.now(DateTimeZone.UTC).minusMinutes(14)
        runBlocking {
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = cowboyJsonId, forceNetwork = true)
            testDb.db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync.millis > 0)
                assertTrue("Feed should have a valid response hash", feed.responseHash > 0)

                testDb.db.feedDao().updateFeed(feed.copy(lastSync = fourteenMinsAgo))
            }
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = cowboyJsonId,
                    forceNetwork = false, minFeedAgeMinutes = 15)
        }

        verify(exactly = 1) {
            feedParser.parseFeedResponse(any(), any())
        }

        assertEquals(
                "Last sync should not have changed",
                fourteenMinsAgo,
                testDb.db.feedDao().loadFeed(cowboyJsonId)!!.lastSync)
    }

    @Test
    fun feedsSyncedWithin15MinAreNotIgnoredWhenForcingNetwork() {
        val fourteenMinsAgo = DateTime.now(DateTimeZone.UTC).minusMinutes(14)
        runBlocking {
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = cowboyJsonId, forceNetwork = true)
            testDb.db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync.millis > 0)
                assertTrue("Feed should have a valid response hash", feed.responseHash > 0)

                testDb.db.feedDao().updateFeed(feed.copy(lastSync = fourteenMinsAgo))
            }
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = cowboyJsonId,
                    forceNetwork = true, minFeedAgeMinutes = 15)
        }

        verify(exactly = 1) {
            feedParser.parseFeedResponse(any(), any())
        }

        assertNotEquals(
                "Last sync should have changed",
                fourteenMinsAgo,
                testDb.db.feedDao().loadFeed(cowboyJsonId)!!.lastSync)
    }

    @Test
    fun feedShouldNotBeUpdatedIfRequestFails() {
        val response = MockResponse().also {
            it.setResponseCode(500)
        }
        server.enqueue(response)
        server.start()

        val url = server.url("/feed.json")

        val failingJsonId = testDb.db.feedDao().insertFeed(Feed(
                title = "failJson",
                url = URL("$url"),
                tag = ""
        ))

        runBlocking {
            syncFeeds(db = testDb.db, feedParser = feedParser, feedId = failingJsonId)
        }

        assertEquals(
                "Last sync should not have been updated",
                DateTime(0, DateTimeZone.UTC),
                testDb.db.feedDao().loadFeed(failingJsonId)!!.lastSync
        )

        // Assert the feed was retrieved
        assertEquals("/feed.json", server.takeRequest().path)
    }
}
