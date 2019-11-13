package com.nononsenseapps.feeder.ui

import android.widget.TextView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
@LargeTest
class CustomFeedTitleIsShownInReaderTest {
    @get:Rule
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    @Test
    fun feedTitleIsShownIfNoCustomTitle() = runBlocking {
        insertDataAndLaunch("foo", "")
        assertFeedTitleShownIs("foo")
    }

    @Test
    fun customTitleIsShownIfCustomTitle() = runBlocking {
        insertDataAndLaunch("foo", "bar")
        assertFeedTitleShownIs("bar")
    }

    private suspend fun insertDataAndLaunch(title: String, customTitle: String) {
        val feedId = testDb.db.feedDao().insertFeed(Feed(
                title = title,
                customTitle = customTitle,
                url = URL("http://foo")
        ))

        val feedItemId = testDb.db.feedItemDao().insertFeedItem(FeedItem(
                guid = "fooitem1",
                feedId = feedId,
                title = "fooitem"
        ))

        activityRule.launchReader(feedItemId)
    }

    private fun assertFeedTitleShownIs(title: String) {
        runBlocking {
            val feedTitle = activityRule.activity.findViewById<TextView>(R.id.story_feedtitle)!!

            withTimeout(200) {
                while (feedTitle.text.toString() != title) {
                    delay(20)
                }
            }

            onView(withId(R.id.story_feedtitle)).check(matches(withText(title)))
        }
    }
}
