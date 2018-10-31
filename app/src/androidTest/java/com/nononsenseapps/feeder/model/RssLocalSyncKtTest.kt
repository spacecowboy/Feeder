package com.nononsenseapps.feeder.model

import androidx.room.Room
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.filters.MediumTest
import androidx.test.runner.AndroidJUnit4
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.util.feedParser
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
@MediumTest
class RssLocalSyncKtTest {

    private val db = Room.inMemoryDatabaseBuilder(getInstrumentation().context, AppDatabase::class.java).build()
    private val feedParser = spyk(getInstrumentation().targetContext.feedParser)

    private var cowboyJsonId: Long = -1
    private var cowboyAtomId: Long = -1

    @Before
    fun setupTestDb() {
        AppDatabase.setInstance(db)

        cowboyJsonId = db.feedDao().insertFeed(Feed(
                title = "cowboyjson",
                url = URL("https://cowboyprogrammer.org/feed.json"),
                tag = ""
        ))

        cowboyAtomId = db.feedDao().insertFeed(Feed(
                title = "cowboyatom",
                url = URL("https://cowboyprogrammer.org/atom.xml"),
                tag = ""
        ))
    }

    @Test
    fun syncCowboyJsonWorks() {
        runBlocking {
            syncFeeds(db = db, feedParser = feedParser, feedId = cowboyJsonId)
        }

        assertEquals(
                "Unexpected number of items in feed",
                10,
                db.feedItemDao().loadFeedItemsInFeed(cowboyJsonId).size)
    }

    @Test
    fun syncCowboyAtomWorks() {
        runBlocking {
            syncFeeds(db = db, feedParser = feedParser, feedId = cowboyAtomId)
        }

        assertEquals(
                "Unexpected number of items in feed",
                15,
                db.feedItemDao().loadFeedItemsInFeed(cowboyAtomId).size)
    }

    @Test
    fun syncAllWorks() {
        runBlocking {
            syncFeeds(db = db, feedParser = feedParser, feedId = ID_UNSET, parallel = true)
        }

        assertEquals(
                "Unexpected number of items in feed",
                10,
                db.feedItemDao().loadFeedItemsInFeed(cowboyJsonId).size)

        assertEquals(
                "Unexpected number of items in feed",
                15,
                db.feedItemDao().loadFeedItemsInFeed(cowboyAtomId).size)
    }

    @Test
    fun responsesAreNotParsedUnlessFeedHashHasChanged() {
        FeedParser.setup(getInstrumentation().targetContext.cacheDir!!)
        runBlocking {
            syncFeeds(db = db, feedParser = feedParser, feedId = cowboyJsonId, forceNetwork = true)
            db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync > 0)
                assertTrue("Feed should have a valid response hash", feed.responseHash > 0)
                // "Long time" ago, but not unset
                db.feedDao().updateFeed(feed.copy(lastSync = 999L))
            }
            syncFeeds(db = db, feedParser = feedParser, feedId = cowboyJsonId, forceNetwork = true)
        }

        verify(exactly = 1) {
            feedParser.parseFeedResponse( any(), any())
        }

        assertNotEquals(
                "Cached response should still have updated feed last sync",
                999L,
                db.feedDao().loadFeed(cowboyJsonId)!!.lastSync)
    }

    @Test
    fun feedsSyncedWithin15MinAreIgnored() {
        val fourteenMinsAgo = DateTime.now().minusMinutes(14).millis
        FeedParser.setup(getInstrumentation().targetContext.cacheDir!!)
        runBlocking {
            syncFeeds(db = db, feedParser = feedParser, feedId = cowboyJsonId, forceNetwork = true)
            db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync > 0)
                assertTrue("Feed should have a valid response hash", feed.responseHash > 0)

                db.feedDao().updateFeed(feed.copy(lastSync = fourteenMinsAgo))
            }
            syncFeeds(db = db, feedParser = feedParser, feedId = cowboyJsonId,
                    forceNetwork = false, minFeedAgeMinutes = 15)
        }

        verify(exactly = 1) {
            feedParser.parseFeedResponse( any(), any())
        }

        assertEquals(
                "Last sync should not have changed",
                fourteenMinsAgo,
                db.feedDao().loadFeed(cowboyJsonId)!!.lastSync)
    }

    @Test
    fun feedsSyncedWithin15MinAreNotIgnoredWhenForcingNetwork() {
        val fourteenMinsAgo = DateTime.now().minusMinutes(14).millis
        FeedParser.setup(getInstrumentation().targetContext.cacheDir!!)
        runBlocking {
            syncFeeds(db = db, feedParser = feedParser, feedId = cowboyJsonId, forceNetwork = true)
            db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync > 0)
                assertTrue("Feed should have a valid response hash", feed.responseHash > 0)

                db.feedDao().updateFeed(feed.copy(lastSync = fourteenMinsAgo))
            }
            syncFeeds(db = db, feedParser = feedParser, feedId = cowboyJsonId,
                    forceNetwork = true, minFeedAgeMinutes = 15)
        }

        verify(exactly = 1) {
            feedParser.parseFeedResponse( any(), any())
        }

        assertNotEquals(
                "Last sync should have changed",
                fourteenMinsAgo,
                db.feedDao().loadFeed(cowboyJsonId)!!.lastSync)
    }
}