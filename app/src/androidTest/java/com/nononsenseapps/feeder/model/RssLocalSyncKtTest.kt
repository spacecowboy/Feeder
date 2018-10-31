package com.nononsenseapps.feeder.model

import androidx.room.Room
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.filters.MediumTest
import androidx.test.runner.AndroidJUnit4
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
@MediumTest
class RssLocalSyncKtTest {

    private val db = Room.inMemoryDatabaseBuilder(getInstrumentation().context, AppDatabase::class.java).build()

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
            syncFeeds(getInstrumentation().context, cowboyJsonId, "", forceNetwork = false)
        }

        assertEquals(
                "Unexpected number of items in feed",
                10,
                db.feedItemDao().loadFeedItemsInFeed(cowboyJsonId).size)
    }

    @Test
    fun syncCowboyAtomWorks() {
        runBlocking {
            syncFeeds(getInstrumentation().context, cowboyAtomId, "", forceNetwork = false)
        }

        assertEquals(
                "Unexpected number of items in feed",
                15,
                db.feedItemDao().loadFeedItemsInFeed(cowboyAtomId).size)
    }

    @Test
    fun syncAllWorks() {
        runBlocking {
            syncFeeds(getInstrumentation().context, ID_UNSET, "", forceNetwork = false)
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
    fun cachedResponsesAreNotParsedUnlessFeedIsNew() {
        FeedParser.setup(getInstrumentation().targetContext.cacheDir!!)
        runBlocking {
            syncFeeds(getInstrumentation().targetContext, cowboyJsonId, "", forceNetwork = true)
            db.feedDao().loadFeed(cowboyJsonId)!!.let { feed ->
                assertTrue("Feed should have been synced", feed.lastSync > 0)
                // "Long time" ago, but not unset
                db.feedDao().updateFeed(feed.copy(lastSync = 999L))
            }
            syncFeeds(getInstrumentation().context, cowboyJsonId, "", forceNetwork = true)
        }

        assertEquals(
                "Cached response should not have updated feed",
                999L,
                db.feedDao().loadFeed(cowboyJsonId)!!.lastSync)
    }
}