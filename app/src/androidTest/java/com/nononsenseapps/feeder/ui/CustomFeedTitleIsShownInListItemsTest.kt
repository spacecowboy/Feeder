package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.util.Prefs
import kotlinx.android.synthetic.main.content_navigation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.net.URL

@RunWith(AndroidJUnit4::class)
@LargeTest
class CustomFeedTitleIsShownInListItemsTest {
    @get:Rule
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)

    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private val kodein by closestKodein(getApplicationContext() as Context)
    private val prefs by kodein.instance<Prefs>()

    @Before
    fun keepNavDrawerClosed() {
        prefs.welcomeDone = true
    }

    @Test
    fun feedTitleIsShownIfNoCustomTitle() {
        insertDataAndLaunch("foo", "")
        assertFeedTitleShownIs("foo")
    }

    @Test
    fun customTitleIsShownIfCustomTitle() {
        insertDataAndLaunch("foo", "bar")
        assertFeedTitleShownIs("bar")
    }

    private fun insertDataAndLaunch(title: String, customTitle: String) {
        val feedId = testDb.db.feedDao().insertFeed(Feed(
                title = title,
                customTitle = customTitle,
                url = URL("http://foo")
        ))

        testDb.db.feedItemDao().insertFeedItem(FeedItem(
                guid = "fooitem1",
                feedId = feedId,
                title = "fooitem"
        ))

        activityRule.launchActivity(Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDS, "$feedId")))
    }

    private fun assertFeedTitleShownIs(title: String) {
        runBlocking {
            val recyclerView = activityRule.activity.nav_host_fragment?.view?.findViewById<RecyclerView>(android.R.id.list)!!

            withTimeout(200) {
                while (recyclerView.findViewHolderForAdapterPosition(0) == null) {
                    delay(20)
                }
            }

            onView(withId(R.id.story_author)).check(matches(withText(title)))
        }
    }
}
