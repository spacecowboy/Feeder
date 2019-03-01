package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.util.PrefUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URI
import java.net.URL
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollingMarksAsRead {
    @get:Rule
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)

    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    var feedId: Long = 1

    @Before
    fun setup() {
        feedId = testDb.db.feedDao().insertFeed(
            Feed(
                title = "foo",
                url = URL("http://foo")
            )
        )

        for (i in 1..100) {
            testDb.db.feedItemDao().insertFeedItem(
                FeedItem(
                    id = i.toLong(),
                    guid = "bar$i",
                    feedId = feedId,
                    title = "bar$i",
                    description = "bar bar bar $i",
                    unread = true
                )
            )
        }

        activityRule.launchActivity(
            Intent(
                Intent.ACTION_VIEW,
                URI_FEEDITEMS.buildUpon().appendPath("$feedId").build()
            )
        )
    }

    @Test
    fun scrollingMarksAsRead() {
        PrefUtils.setMarkAsReadWhenScrolling(getApplicationContext(), true)

        onView(withId(android.R.id.list)).check(matches(isDisplayed()))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(99))

        runBlocking {
            untilEq(false) {
                testDb.db.feedItemDao().loadFeedItem("bar1", feedId)!!.unread
            }
        }
    }

    @Test
    fun scrollingDoesNotMarkAsReadWhenDisabled() {
        PrefUtils.setMarkAsReadWhenScrolling(getApplicationContext(), false)

        onView(withId(android.R.id.list)).check(matches(isDisplayed()))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(99))

        runBlocking {
            delay(200)
            assertTrue("Item should not have been marked as read") {
                testDb.db.feedItemDao().loadFeedItem("bar1", feedId)!!.unread
            }
        }
    }


}