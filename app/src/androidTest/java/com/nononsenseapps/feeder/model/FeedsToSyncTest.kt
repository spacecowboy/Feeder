package com.nononsenseapps.feeder.model

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
class FeedsToSyncTest {

    private lateinit var db: AppDatabase

    @Before
    fun initDb() {
        db = Room.inMemoryDatabaseBuilder(getApplicationContext(),
                AppDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun returnsStaleFeed() {
        // with stale feed
        val feed = withFeed()

        // when
        val result = feedsToSync(db, feedId = feed.id, tag = "")

        // then
        assertEquals(listOf(feed), result)
    }

    @Test
    fun doesNotReturnFreshFeed() {
        val now = DateTime.now(DateTimeZone.UTC)
        val feed = withFeed(lastSync = now.minusMinutes(1).millis)

        // when
        val result = feedsToSync(db, feedId = feed.id, tag = "",
                staleTime = now.minusMinutes(2).millis)

        // then
        assertEquals(emptyList<Feed>(), result)
    }

    @Test
    fun returnsAllStaleFeeds() {
        val items = listOf(
                withFeed(url = URL("http://one")),
                withFeed(url = URL("http://two"))
        )

        val result = feedsToSync(db, feedId = ID_UNSET, tag = "")

        assertEquals(items, result)
    }

    @Test
    fun doesNotReturnAllFreshFeeds() {
        val now = DateTime.now(DateTimeZone.UTC)
        val items = listOf(
                withFeed(url = URL("http://one"), lastSync = now.minusMinutes(1).millis),
                withFeed(url = URL("http://two"), lastSync = now.minusMinutes(3).millis)
        )

        val result = feedsToSync(db, feedId = ID_UNSET, tag = "", staleTime = now.minusMinutes(2).millis)

        assertEquals(listOf(items[1]), result)
    }

    @Test
    fun returnsTaggedStaleFeeds() {
        val items = listOf(
                withFeed(url = URL("http://one"), tag = "tag"),
                withFeed(url = URL("http://two"), tag = "tag")
        )

        val result = feedsToSync(db, feedId = ID_UNSET, tag = "")

        assertEquals(items, result)
    }

    @Test
    fun doesNotReturnTaggedFreshFeeds() {
        val now = DateTime.now(DateTimeZone.UTC)
        val items = listOf(
                withFeed(url = URL("http://one"), lastSync = now.minusMinutes(1).millis, tag = "tag"),
                withFeed(url = URL("http://two"), lastSync = now.minusMinutes(3).millis, tag = "tag")
        )

        val result = feedsToSync(db, feedId = ID_UNSET, tag = "tag", staleTime = now.minusMinutes(2).millis)

        assertEquals(listOf(items[1]), result)
    }

    private fun withFeed(lastSync: Long = 0, url: URL = URL("http://url"), tag: String = ""): Feed {
        val feed = Feed(
                lastSync = lastSync,
                url = url,
                tag = tag
        )

        val id = db.feedDao().insertFeed(feed)

        return feed.copy(id = id)
    }
}
