package com.nononsenseapps.feeder.ui

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.model.getOpenInBrowserIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class OpenInWebBrowserActivityTest {
    @get:Rule
    val activityTestRule = ActivityTestRule(OpenInWebBrowserActivity::class.java, false, false)

    private lateinit var db: AppDatabase

    private lateinit var feedItem: FeedItem

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(getApplicationContext(),
                AppDatabase::class.java).build()
        // Ensure all classes use test database
        AppDatabase.setInstance(db)

        val feedId = db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo")
        ))

        val item = FeedItem(
                feedId = feedId,
                guid = "foobar",
                title = "bla",
                link = "http://foo",
                notified = false,
                unread = true
        )

        val feedItemId = db.feedItemDao().insertFeedItem(item)

        feedItem = item.copy(id = feedItemId)
    }

    @After
    fun pressHome() {
        UiDevice.getInstance(getInstrumentation()).pressBack()
    }

    @Test
    fun noIntentDoesNothing() {
        activityTestRule.launchActivity(null)

        runBlocking {
            val item = withContext(Dispatchers.Default) {
                untilEq(feedItem) {
                    db.feedItemDao().loadFeedItem(feedItem.id)
                }
            }
            assertEquals(feedItem, item)
        }
    }

    @Test
    fun faultyLinkDoesntCrash() {
        activityTestRule.launchActivity(getOpenInBrowserIntent(getApplicationContext(),
                feedItemId = -252,
                link = "bob"))

        runBlocking {
            val item = withContext(Dispatchers.Default) {
                untilEq(feedItem) {
                    db.feedItemDao().loadFeedItem(feedItem.id)
                }
            }
            assertEquals(feedItem, item)
        }
    }

    @Test
    fun withIntentItemIsMarkedAsReadAndNotified() {
        activityTestRule.launchActivity(getOpenInBrowserIntent(getApplicationContext(),
                feedItemId = feedItem.id,
                link = feedItem.link!!))

        val expected = feedItem.copy(
                unread = false,
                notified = true
        )

        runBlocking {
            val item = withContext(Dispatchers.Default) {
                untilEq(expected) {
                    db.feedItemDao().loadFeedItem(feedItem.id)
                }
            }
            assertEquals(expected, item)
        }
    }
}
