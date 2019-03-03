package com.nononsenseapps.feeder.ui

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.model.getOpenInDefaultActivityIntent
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
class OpenLinkInDefaultActivityTest {
    @get:Rule
    val activityTestRule = ActivityTestRule(OpenLinkInDefaultActivity::class.java, false, false)

    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private lateinit var feedItem: FeedItem

    @Before
    fun setup() {
        val db = testDb.db

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
                    testDb.db.feedItemDao().loadFeedItem(feedItem.id)
                }
            }
            assertEquals(feedItem, item)
        }
    }

    @Test
    fun faultyLinkDoesntCrash() {
        activityTestRule.launchActivity(getOpenInDefaultActivityIntent(getApplicationContext(),
                feedItemId = -252,
                link = "bob"))

        runBlocking {
            val item = withContext(Dispatchers.Default) {
                untilEq(feedItem) {
                    testDb.db.feedItemDao().loadFeedItem(feedItem.id)
                }
            }
            assertEquals(feedItem, item)
        }
    }

    @Test
    fun withIntentItemIsMarkedAsReadAndNotified() {
        activityTestRule.launchActivity(getOpenInDefaultActivityIntent(getApplicationContext(),
                feedItemId = feedItem.id,
                link = feedItem.link!!))

        val expected = feedItem.copy(
                unread = false,
                notified = true
        )

        runBlocking {
            val item = withContext(Dispatchers.Default) {
                untilEq(expected) {
                    testDb.db.feedItemDao().loadFeedItem(feedItem.id)
                }
            }
            assertEquals(expected, item)
        }
    }

    @Test
    fun noLinkButItemIsMarkedAsReadAndNotified() {
        activityTestRule.launchActivity(getOpenInDefaultActivityIntent(getApplicationContext(),
                feedItemId = feedItem.id))

        val expected = feedItem.copy(
                unread = false,
                notified = true
        )

        runBlocking {
            val item = withContext(Dispatchers.Default) {
                untilEq(expected) {
                    testDb.db.feedItemDao().loadFeedItem(feedItem.id)
                }
            }
            assertEquals(expected, item)
        }
    }
}
