package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.util.Prefs
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.net.URL

@RunWith(AndroidJUnit4::class)
class OpenFeedFromTitleTest {
    @get:Rule
    val activityRule = ActivityTestRule(FeedActivity::class.java, false, false)
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private lateinit var feedItem: FeedItem

    private var feedId: Long? = null

    private val kodein by closestKodein(getApplicationContext() as Context)
    private val prefs by kodein.instance<Prefs>()

    @Before
    fun keepNavDrawerClosed() {
        prefs.welcomeDone = true
    }

    @Before
    fun setup() = runBlocking {
        feedId = testDb.db.feedDao().insertFeed(
            Feed(
                title = "ANON",
                url = URL("http://ANON.com/sub")
            )
        )

        val item = FeedItem(
            feedId = feedId,
            guid = "http://ANON.com/sub/##",
            title = "ANON",
            plainTitle = "ANON",
            plainSnippet = "ANON"
        )

        val feedItemId = testDb.db.feedItemDao().insertFeedItem(item)
        feedItem = item.copy(id = feedItemId)
    }

    @Test
    fun clickingFirstItemOpensReader() {
        activityRule.launchActivity(Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDITEMS, "${feedItem.id}")))

        onView(withId(R.id.story_feedtitle))
            .perform(click())

        onView(withId(android.R.id.list)).check(matches(isDisplayed()))
    }
}
