package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.util.Prefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.net.URL

@RunWith(AndroidJUnit4::class)
@LargeTest
class BadImagePlaceHolderTest {
    @get:Rule
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)

    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private val server = MockWebServer()

    private val kodein by closestKodein(getApplicationContext() as Context)
    private val prefs by kodein.instance<Prefs>()

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Test
    fun placeHolderIsShownOnBadImageNightTheme() = runBlocking {
        prefs.isNightMode = true
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        placeHolderIsShownOnBadImage()
    }

    @Test
    fun placeHolderIsShownOnBadImageDayTheme() = runBlocking {
        prefs.isNightMode = false
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        placeHolderIsShownOnBadImage()
    }

    private suspend fun placeHolderIsShownOnBadImage() {
        server.enqueue(MockResponse().also {
            it.setResponseCode(400)
        })
        server.start()

        val imgUrl = server.url("/img.png")

        val feedId = testDb.db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo")
        ))

        testDb.db.feedItemDao().insertFeedItem(FeedItem(
                guid = "bar",
                feedId = feedId,
                title = "foo",
                imageUrl = "$imgUrl"
        ))

        activityRule.launchActivity(Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDS, "$feedId")))

        runBlocking {
            delay(50)

            val recyclerView = activityRule.activity.findViewById<RecyclerView>(android.R.id.list)!!
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(0)!!
            val imageView = viewHolder.itemView.findViewById<ImageView>(R.id.story_image)!!

            withTimeout(10000) {
                while (true) {
                    if (imageView.visibility == View.VISIBLE && imageView.drawable != null) {
                        break // good
                    }
                    delay(50)
                }
            }
        }

        onView(withId(R.id.story_image)).check(matches(isDisplayed()))
    }
}
