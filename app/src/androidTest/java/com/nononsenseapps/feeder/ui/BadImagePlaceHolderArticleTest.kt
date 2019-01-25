package com.nononsenseapps.feeder.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
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
import com.nononsenseapps.feeder.util.PrefUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
@LargeTest
class BadImagePlaceHolderArticleTest {
    @get:Rule
    var activityRule: ActivityTestRule<ReaderActivity> = ActivityTestRule(ReaderActivity::class.java, false, false)

    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private val server = MockWebServer()

    @Before
    fun startServer() {
        server.enqueue(MockResponse().also {
            it.setResponseCode(400)
        })
        server.start()
    }

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun placeHolderIsShownOnBadImageNightTheme() {
        PrefUtils.setNightMode(getApplicationContext(), true)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        val imgUrl = server.url("/img.png")

        val itemId = insertData(imgUrl) {
            """
                Image is: <img src="$imgUrl" alt="alt text"></img>
                <p>
                And that is that
                """.trimIndent()
        }

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
                .check(matches(withText(containsString("alt text"))))
    }

    @Test
    fun placeHolderIsShownOnBadImageDayTheme() {
        PrefUtils.setNightMode(getApplicationContext(), false)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)

        val imgUrl = server.url("/img.png")

        val itemId = insertData(imgUrl) {
            """
                Image is: <img src="$imgUrl" alt="alt text"></img>
                <p>
                And that is that
                """.trimIndent()
        }

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
                .check(matches(withText(containsString("alt text"))))
    }

    @Test
    fun imgWithNoSrcIsNotDisplayed() {
        val itemId = insertData {
            """
                Image is: <img src="" alt="alt text"></img>
                <p>
                And that is that
                """.trimIndent()
        }

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
                .check(matches(withText(not(containsString("alt text")))))
    }

    @Test
    fun imgHasAltTextDisplayed() {
        val imgUrl = server.url("/img.png")

        val itemId = insertData {
            """
                Image is: <img src="$imgUrl" alt="alt text"></img>
                <p>
                And that is that
                """.trimIndent()
        }

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
                .check(matches(withText(containsString("alt text"))))
    }

    @Test
    fun imgAppendsNewLineBeforeAndAfter() {
        val imgUrl = server.url("/img.png")

        val itemId = insertData {
            """
                Image is:<img src="$imgUrl" alt="alt text"></img>
                <p>
                And that is that
                """.trimIndent()
        }

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
                .check(matches(withText(containsString("Image is:\n"))))
        onView(withId(R.id.story_body))
                .check(matches(withText(containsString("\nalt text\n"))))
    }

    private fun insertData(imgUrl: HttpUrl? = null, description: () -> String): Long {

        val feedId = testDb.db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo")
        ))

        return testDb.db.feedItemDao().insertFeedItem(FeedItem(
                guid = "bar",
                feedId = feedId,
                title = "foo",
                imageUrl = imgUrl?.let { "$it" },
                description = description()
        ))
    }
}
