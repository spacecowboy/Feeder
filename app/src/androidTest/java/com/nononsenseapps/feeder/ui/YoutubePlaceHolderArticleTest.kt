package com.nononsenseapps.feeder.ui

import android.content.Context
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
import com.nononsenseapps.feeder.model.insertFeedItemWithBlob
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
@LargeTest
class YoutubePlaceHolderArticleTest {
    @get:Rule
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)

    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private val server = MockWebServer()

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun placeHolderIsShownForYoutubeIframes() = runBlocking {
        val itemId = setup("youtube.com/embed/foo") { imgUrl ->
            """
            Video is: <iframe src="$imgUrl"></iframe>
            <p>
            And that is that
            """.trimIndent()
        }

        activityRule.launchReader(itemId)

        onView(withId(R.id.story_body))
                .check(matches(withText(containsString(getApplicationContext<Context>()
                        .getString(R.string.touch_to_play_video)))))
    }

    @Test
    fun placeHolderIsNotShownForBadIframes() = runBlocking {
        val itemId = setup("badsite.com/foo") { imgUrl ->
            """
            Video is: <iframe src="$imgUrl"></iframe>
            <p>
            And that is that
            """.trimIndent()
        }

        activityRule.launchReader(itemId)

        onView(withId(R.id.story_body))
                .check(matches(withText(not(containsString(getApplicationContext<Context>()
                        .getString(R.string.touch_to_play_video))))))
    }

    private suspend fun setup(urlSuffix: String, description: (HttpUrl) -> String): Long {
        server.enqueue(MockResponse().also {
            it.setResponseCode(400)
        })
        server.start()

        val imgUrl = server.url(urlSuffix)
        val feedId = testDb.db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo")
        ))

        return testDb.insertFeedItemWithBlob(
                FeedItem(
                        guid = "bar",
                        feedId = feedId,
                        title = "foo",
                        imageUrl = "$imgUrl"
                ),
                description = description(imgUrl)
        )
    }

}
