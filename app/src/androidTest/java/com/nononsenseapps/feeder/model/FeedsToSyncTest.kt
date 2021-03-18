package com.nononsenseapps.feeder.model

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import com.nononsenseapps.feeder.util.minusMinutes
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Instant
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
        val now = Instant.now()
        val feed = withFeed(lastSync = now.minusMinutes(1))

        // when
        val result = feedsToSync(
            testDb.db.feedDao(), feedId = feed.id, tag = "",
            staleTime = now.minusMinutes(2).toEpochMilli()
        )

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
        val now = Instant.now()
        val items = listOf(
            withFeed(url = URL("http://one"), lastSync = now.minusMinutes(1)),
            withFeed(url = URL("http://two"), lastSync = now.minusMinutes(3))
        )

        val result = feedsToSync(testDb.db.feedDao(), feedId = ID_UNSET, tag = "", staleTime = now.minusMinutes(2).toEpochMilli())

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
        val now = Instant.now()
        val items = listOf(
            withFeed(url = URL("http://one"), lastSync = now.minusMinutes(1), tag = "tag"),
            withFeed(url = URL("http://two"), lastSync = now.minusMinutes(3), tag = "tag")
        )

        val result = feedsToSync(testDb.db.feedDao(), feedId = ID_UNSET, tag = "tag", staleTime = now.minusMinutes(2).toEpochMilli())

        assertEquals(listOf(items[1]), result)
    }

    private suspend fun withFeed(lastSync: Instant = Instant.ofEpochMilli(0), url: URL = URL("http://url"), tag: String = ""): Feed {
        val feed = Feed(
            lastSync = lastSync,
            url = url,
            tag = tag
        )

        val id = testDb.db.feedDao().insertFeed(feed)

        return feed.copy(id = id)
    }
}
