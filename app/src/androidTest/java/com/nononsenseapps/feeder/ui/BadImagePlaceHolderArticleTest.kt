package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.util.PrefUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
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

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun placeHolderIsShownOnBadImageNightTheme() {
        PrefUtils.setNightMode(getApplicationContext(), true)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        placeHolderIsShownOnBadImage()
    }

    @Test
    fun placeHolderIsShownOnBadImageDayTheme() {
        PrefUtils.setNightMode(getApplicationContext(), false)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        placeHolderIsShownOnBadImage()
    }

    private fun placeHolderIsShownOnBadImage() {
        server.enqueue(MockResponse().also {
            it.setResponseCode(400)
        })
        server.start()

        val imgUrl = server.url("/img.png")

        val feedId = testDb.db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo")
        ))

        val itemId = testDb.db.feedItemDao().insertFeedItem(FeedItem(
                guid = "bar",
                feedId = feedId,
                title = "foo",
                imageUrl = "$imgUrl",
                description = """
                    Image is: <img src="$imgUrl" alt="alt"></img>
                    <p>
                    And that is that
                """.trimIndent()
        ))

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            delay(50)
        }

        activityRule.activity.findViewById<TextView>(R.id.story_body).let {
            assert(it.text.contains("And that is that"))
        }
    }
}
