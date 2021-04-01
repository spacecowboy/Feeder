package com.nononsenseapps.feeder.ui

import android.content.Context
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
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.model.insertFeedItemWithBlob
import com.nononsenseapps.feeder.util.Prefs
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
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.net.URL

@RunWith(AndroidJUnit4::class)
@LargeTest
class BadImagePlaceHolderArticleTest {
    @get:Rule
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)

    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private val server = MockWebServer()

    private val di by closestDI(getApplicationContext() as Context)
    private val prefs by di.instance<Prefs>()

    @Before
    fun startServer() {
        server.enqueue(
            MockResponse().also {
                it.setResponseCode(400)
            }
        )
        server.start()
    }

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun placeHolderIsShownOnBadImageNightTheme() = runBlocking {
        prefs.isNightMode = true
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        val imgUrl = server.url("/img.png")

        val itemId = insertData(imgUrl) {
            """
                Image is: <img src="$imgUrl" alt="alt text"></img>
                <p>
                And that is that
            """.trimIndent()
        }

        activityRule.launchReader(itemId)

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
            .check(matches(withText(containsString("alt text"))))
    }

    @Test
    fun placeHolderIsShownOnBadImageDayTheme() = runBlocking {
        prefs.isNightMode = false
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)

        val imgUrl = server.url("/img.png")

        val itemId = insertData(imgUrl) {
            """
                Image is: <img src="$imgUrl" alt="alt text"></img>
                <p>
                And that is that
            """.trimIndent()
        }

        activityRule.launchReader(itemId)

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
            .check(matches(withText(containsString("alt text"))))
    }

    @Test
    fun imgWithNoSrcIsNotDisplayed() = runBlocking {
        val itemId = insertData {
            """
                Image is: <img src="" alt="alt text"></img>
                <p>
                And that is that
            """.trimIndent()
        }

        activityRule.launchReader(itemId)

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
            .check(matches(withText(not(containsString("alt text")))))
    }

    @Test
    fun imgHasAltTextDisplayed() = runBlocking {
        val imgUrl = server.url("/img.png")

        val itemId = insertData {
            """
                Image is: <img src="$imgUrl" alt="alt text"></img>
                <p>
                And that is that
            """.trimIndent()
        }

        activityRule.launchReader(itemId)

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
            .check(matches(withText(containsString("alt text"))))
    }

    @Test
    fun imgAppendsNewLineBeforeAndAfter() = runBlocking {
        val imgUrl = server.url("/img.png")

        val itemId = insertData {
            """
                Image is:<img src="$imgUrl" alt="alt text"></img>
                <p>
                And that is that
            """.trimIndent()
        }

        activityRule.launchReader(itemId)

        runBlocking {
            delay(50)
        }

        onView(withId(R.id.story_body))
            .check(matches(withText(containsString("Image is:\n"))))
        onView(withId(R.id.story_body))
            .check(matches(withText(containsString("\nalt text\n"))))
    }

    private suspend fun insertData(imgUrl: HttpUrl? = null, description: () -> String): Long {

        val feedId = testDb.db.feedDao().insertFeed(
            Feed(
                title = "foo",
                url = URL("http://foo")
            )
        )

        return testDb.insertFeedItemWithBlob(
            FeedItem(
                guid = "bar",
                feedId = feedId,
                title = "foo",
                imageUrl = imgUrl?.let { "$it" }
            ),
            description = description()
        )
    }
}

fun ActivityTestRule<FeedActivity>.launchReader(itemId: Long) =
    launchActivity(
        Intent().also {
            it.data = URI_FEEDITEMS.buildUpon().appendPath("$itemId").build()
        }
    )
