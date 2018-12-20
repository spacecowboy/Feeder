package com.nononsenseapps.feeder.ui

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.MockResponses.cowboy_feed_json_body
import com.nononsenseapps.feeder.ui.MockResponses.cowboyprogrammer_feed_json_headers
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL


@RunWith(AndroidJUnit4::class)
@LargeTest
class EditFeedTest {
    @get:Rule
    var activityRule: ActivityTestRule<EditFeedActivity> = ActivityTestRule(EditFeedActivity::class.java)
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    val server = MockWebServer()

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun activityStarts() {
        assertNotNull(activityRule.activity)
    }

    @Test
    @Ignore("Not sure how to make a bad URL")
    fun badUrlDisplaysEmptyView() {
        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())))
        onView(withId(R.id.search_view))
                .perform(
                        typeText("abc123-_.\\"),
                        pressImeActionButton()
                )
        runBlocking {
            untilEq(true) {
                activityRule.activity?.searchJob?.isCompleted
            }
        }
        onView(withId(android.R.id.empty)).check(matches(isDisplayed()))
    }

    @Test
    fun badResponseShowsEmptyView() {
        server.enqueue(MockResponse().also {
            it.setBody("NOT VALID XML")
        })
        server.start()

        val url = server.url("/rss.xml")

        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())))
        onView(withId(R.id.search_view))
                .perform(
                        typeText("$url"),
                        pressImeActionButton()
                )
        runBlocking {
            untilEq(true) {
                activityRule.activity?.searchJob?.isCompleted
            }
        }
        onView(withId(android.R.id.empty)).check(matches(isDisplayed()))
    }

    @Test
    fun endToEnd() {
        val response = MockResponse().also {
            it.setBody(cowboy_feed_json_body)
            cowboyprogrammer_feed_json_headers.entries.forEach { entry ->
                it.setHeader(entry.key, entry.value)
            }
        }
        server.enqueue(response)
        server.enqueue(response)
        server.start()

        val url = server.url("/feed.json")

        onView(withId(android.R.id.empty)).check(matches(not(isDisplayed())))
        onView(withId(R.id.search_view))
                .perform(
                        typeText("$url"),
                        pressImeActionButton()
                )

        val recyclerView: RecyclerView = activityRule.activity.findViewById(R.id.results_listview)

        runBlocking {
            // Wait for search to be done
            untilEq(true) {
                activityRule.activity?.searchJob?.isCompleted
            }
            // Then wait for recyclerView to update
            untilNotEq(null) {
                recyclerView.findViewHolderForAdapterPosition(0)
            }
        }

        // Assert the feed was retrieved
        val request = server.takeRequest()
        assertEquals("/feed.json", request.path)

        val viewHolder = recyclerView.findViewHolderForAdapterPosition(0)!!
        assertEquals("https://cowboyprogrammer.org/feed.json",
                viewHolder.itemView.findViewById<TextView>(R.id.feed_url)!!.text)

        onView(withId(R.id.results_listview)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
        )

        onView(withId(R.id.feed_details_frame)).check(matches(isDisplayed()))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.add_button)).perform(click())

        runBlocking {
            untilEq(true) {
                activityRule.activity?.job?.isCompleted
            }

            val feed = untilNotEq(null) {
                testDb.db.feedDao().loadFeedWithUrl(URL("https://cowboyprogrammer.org/feed.json"))
            }
            assertEquals(
                    "Cowboy Programmer",
                    feed?.title
            )
        }
    }
}
