package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.util.PrefUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
class OpenFeedItemTest {
    @get:Rule
    val activityRule = ActivityTestRule(FeedActivity::class.java, false, false)
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private lateinit var feedItem: FeedItem

    private var feedId: Long? = null

    @Before
    fun keepNavDrawerClosed() {
        PrefUtils.markWelcomeDone(getApplicationContext())
    }

    @Before
    fun setup() {
        feedId = testDb.db.feedDao().insertFeed(Feed(
                title = "ANON",
                url = URL("http://ANON.com/sub")
        ))

        val item = FeedItem(
                feedId = feedId,
                guid = "http://ANON.com/sub/##",
                title = "ANON",
                description = "ANON",
                plainTitle = "ANON",
                plainSnippet = "ANON"
        )

        val feedItemId = testDb.db.feedItemDao().insertFeedItem(item)
        feedItem = item.copy(id = feedItemId)
    }

    @Test
    fun clickingFirstItemOpensReader() {
        activityRule.launchActivity(Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDS, "$feedId")))

        onView(withId(android.R.id.list))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(withId(R.id.story_body)).check(matches(isDisplayed()))
    }
}
