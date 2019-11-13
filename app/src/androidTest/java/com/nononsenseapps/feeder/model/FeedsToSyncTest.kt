package com.nononsenseapps.feeder.model

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
class FeedsToSyncTest {
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    @Test
    fun returnsStaleFeed() = runBlocking {
        // with stale feed
        val feed = withFeed()

        // when
        val result = feedsToSync(testDb.db.feedDao(), feedId = feed.id, tag = "")

        // then
        assertEquals(listOf(feed), result)
    }

    @Test
    fun doesNotReturnFreshFeed() = runBlocking {
        val now = DateTime.now(DateTimeZone.UTC)
        val feed = withFeed(lastSync = now.minusMinutes(1))

        // when
        val result = feedsToSync(testDb.db.feedDao(), feedId = feed.id, tag = "",
                staleTime = now.minusMinutes(2).millis)

        // then
        assertEquals(emptyList<Feed>(), result)
    }

    @Test
    fun returnsAllStaleFeeds() = runBlocking {
        val items = listOf(
                withFeed(url = URL("http://one")),
                withFeed(url = URL("http://two"))
        )

        val result = feedsToSync(testDb.db.feedDao(), feedId = ID_UNSET, tag = "")

        assertEquals(items, result)
    }

    @Test
    fun doesNotReturnAllFreshFeeds() = runBlocking {
        val now = DateTime.now(DateTimeZone.UTC)
        val items = listOf(
                withFeed(url = URL("http://one"), lastSync = now.minusMinutes(1)),
                withFeed(url = URL("http://two"), lastSync = now.minusMinutes(3))
        )

        val result = feedsToSync(testDb.db.feedDao(), feedId = ID_UNSET, tag = "", staleTime = now.minusMinutes(2).millis)

        assertEquals(listOf(items[1]), result)
    }

    @Test
    fun returnsTaggedStaleFeeds() = runBlocking {
        val items = listOf(
                withFeed(url = URL("http://one"), tag = "tag"),
                withFeed(url = URL("http://two"), tag = "tag")
        )

        val result = feedsToSync(testDb.db.feedDao(), feedId = ID_UNSET, tag = "")

        assertEquals(items, result)
    }

    @Test
    fun doesNotReturnTaggedFreshFeeds() = runBlocking {
        val now = DateTime.now(DateTimeZone.UTC)
        val items = listOf(
                withFeed(url = URL("http://one"), lastSync = now.minusMinutes(1), tag = "tag"),
                withFeed(url = URL("http://two"), lastSync = now.minusMinutes(3), tag = "tag")
        )

        val result = feedsToSync(testDb.db.feedDao(), feedId = ID_UNSET, tag = "tag", staleTime = now.minusMinutes(2).millis)

        assertEquals(listOf(items[1]), result)
    }

    private suspend fun withFeed(lastSync: DateTime = DateTime(0, DateTimeZone.UTC), url: URL = URL("http://url"), tag: String = ""): Feed {
        val feed = Feed(
                lastSync = lastSync,
                url = url,
                tag = tag
        )

        val id = testDb.db.feedDao().insertFeed(feed)

        return feed.copy(id = id)
    }
}
