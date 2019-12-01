package com.nononsenseapps.feeder.ui

import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.model.getOpenInDefaultActivityIntent
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import kotlin.test.assertFalse


@RunWith(AndroidJUnit4::class)
class OpenLinkInDefaultActivityTaskTest {
    @get:Rule
    val activityTestRule = ActivityTestRule(OpenLinkInDefaultActivity::class.java, false, false)
    @get:Rule
    val mainTaskTestRule = ActivityTestRule(FeedActivity::class.java, false, false)

    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private lateinit var feedItem: FeedItem

    @Before
    fun setup() = runBlocking {
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
        UiDevice.getInstance(getInstrumentation()).pressHome()
    }


    @Test
    fun openInBrowserThenGoingBackDoesNotGoToMainTask() {
        mainTaskTestRule.launchActivity(Intent(
                Intent.ACTION_VIEW,
                URI_FEEDITEMS.buildUpon().appendPath("${feedItem.id}").build())
        )

        UiDevice.getInstance(getInstrumentation()).pressHome()

        activityTestRule.launchActivity(getOpenInDefaultActivityIntent(getApplicationContext(),
                feedItemId = feedItem.id,
                link = feedItem.link!!))

        // Hack - first back exits browser, second back exits main task if it is shown after the first back
        // if it's not shown, then pressing back will not finish it
        UiDevice.getInstance(getInstrumentation()).pressBack()
        UiDevice.getInstance(getInstrumentation()).pressBack()

        assertFalse(mainTaskTestRule.activity.isFinishing,
                message = "Main activity should not be on screen after pressing back")
    }
}
