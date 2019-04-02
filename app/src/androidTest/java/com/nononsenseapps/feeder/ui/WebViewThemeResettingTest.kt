package com.nononsenseapps.feeder.ui

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.util.PrefUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL


@RunWith(AndroidJUnit4::class)
@LargeTest
class WebViewThemeResettingTest {
    @get:Rule
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)

    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private val server = MockWebServer()

    @Before
    fun setup() {
        server.enqueue(MockResponse().also {
            it.setBody("<html><body>Hello!</body></html>")
        })
        server.start()

        val feedId = testDb.db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http:")
        ))

        val feedItemId = testDb.db.feedItemDao().insertFeedItem(FeedItem(
                guid = "bar",
                feedId = feedId,
                title = "foo",
                imageUrl = null,
                description = "bar",
                link = server.url("/bar.html").url().toString()
        ))

        PrefUtils.setNightMode(getApplicationContext(), true)

        activityRule.launchReader(feedItemId)
    }

    @Test
    fun webViewDoesNotResetTheme() {
        runBlocking {
            assertTextColorIsReadableInNightMode()

            delay(10)

            openWebView()

            delay(10)

            pressBack()

            delay(10)

            assertTextColorIsReadableInNightMode()
        }
    }

    private fun assertTextColorIsReadableInNightMode() {
        onView(withId(com.nononsenseapps.feeder.R.id.story_body))
                .check(ViewAssertions.matches(withTextColorId(com.nononsenseapps.feeder.R.color.white_87)))
    }

    private fun openWebView() =
            onView(withId(com.nononsenseapps.feeder.R.id.action_open_in_webview))
                    .perform(click())

    private fun withTextColorId(@ColorRes colorId: Int): Matcher<View> =
            withTextColor(activityRule.activity.resources.getColor(colorId))
}

fun withTextColor(@ColorInt color: Int): Matcher<View> {
    return object : BoundedMatcher<View, TextView>(TextView::class.java) {
        public override fun matchesSafely(textView: TextView): Boolean {
            return color == textView.currentTextColor
        }

        override fun describeTo(description: Description) {
            description.appendText("TextView with currentTextColor = $color")
        }
    }
}
