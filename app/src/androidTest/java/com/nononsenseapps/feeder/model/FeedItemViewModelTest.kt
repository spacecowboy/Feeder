package com.nononsenseapps.feeder.model

import android.content.Intent
import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.base.KodeinAwareActivity
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.FeedActivity
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import com.nononsenseapps.feeder.util.ARG_ID
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.generic.instance
import java.net.URL
import kotlin.test.fail

@RunWith(AndroidJUnit4::class)
class FeedItemViewModelTest {
    @get:Rule
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private var feedId: Long = ID_UNSET
    private var itemId: Long = ID_UNSET

    @Before
    fun initDb() = runBlocking {
        feedId = testDb.db.feedDao().insertFeed(
            Feed(
                title = "foo",
                url = URL("http://foo")
            )
        )
    }

    @Test
    fun databaseLoadCallsOnChangeTwiceWhenImages() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        itemId = runBlocking {
            testDb.insertFeedItemWithBlob(
                FeedItem(
                    feedId = feedId,
                    guid = "foobar",
                    title = "title"
                ),
                description = "description <img src='file://img.png' alt='img here'></img>"
            )
        }

        activityRule.launchActivity(
            Intent().also {
                it.putExtra(ARG_ID, itemId)
            }
        )

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getLiveFeedItemImageText(itemId).observe(activityRule.activity, observer)
            }

            verify(exactly = 3, timeout = 500) {
                observer.onChanged(any())
            }
        }
    }

    @Test
    fun databaseLoadCallsOnChangeOnceWhenNoImages() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        itemId = runBlocking {
            testDb.insertFeedItemWithBlob(
                FeedItem(
                    feedId = feedId,
                    guid = "foobar",
                    title = "title"
                ),
                description = "description <b>bold</b>"
            )
        }

        activityRule.launchActivity(
            Intent().also {
                it.putExtra(ARG_ID, itemId)
            }
        )

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getLiveFeedItemImageText(itemId).observe(activityRule.activity, observer)
            }

            verify(exactly = 2, timeout = 500) {
                observer.onChanged(any())
            }
        }
    }

    @Test
    fun databaseLoadCallsOnChangeNeverOnSyncAndNoUpdateOnBody() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        var item = FeedItem(
            feedId = feedId,
            guid = "foobar",
            title = "title"
        )
        val description = "description <b>bold</b>"

        itemId = runBlocking {
            testDb.insertFeedItemWithBlob(item, description)
        }
        item = item.copy(id = itemId)

        activityRule.launchActivity(
            Intent().also {
                it.putExtra(ARG_ID, itemId)
            }
        )

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getLiveFeedItemImageText(itemId).observe(activityRule.activity, observer)
            }

            verify(exactly = 2, timeout = 500) {
                observer.onChanged(any())
            }

            clearMocks(observer)

            assertEquals(1, testDb.db.feedItemDao().updateFeedItem(item.copy(title = "updated title")))

            verify(exactly = 0, timeout = 500) {
                observer.onChanged(any())
            }
        }
    }

    @Test
    @Ignore("Not monitoring file")
    fun databaseLoadCallsOnChangeOnceOnSyncWithBodyUpdate() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        var item = FeedItem(
            feedId = feedId,
            guid = "foobar",
            title = "title"
        )
        val description = "description <b>bold</b>"

        itemId = runBlocking {
            testDb.insertFeedItemWithBlob(item, description)
        }
        item = item.copy(id = itemId)

        activityRule.launchActivity(
            Intent().also {
                it.putExtra(ARG_ID, itemId)
            }
        )

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getLiveFeedItemImageText(itemId).observe(activityRule.activity, observer)
            }

            verify(exactly = 2, timeout = 500) {
                observer.onChanged(any())
            }

            clearMocks(observer)

            fail("Not monitoring changes to file")
            // assertEquals(1, testDb.db.feedItemDao().updateFeedItem(item.copy(description = "updated body")))

//            verify(exactly = 1, timeout = 500) {
//                observer.onChanged(any())
//            }
        }
    }

    @Test
    @Ignore("Not monitoring file")
    fun databaseLoadCallsOnChangeOnceOnSyncWithBodyUpdateWithImage() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        val item = FeedItem(
            feedId = feedId,
            guid = "foobar",
            title = "title"
        )
        val description = "description <img src='file://img.png' alt='img here'></img>"

        itemId = runBlocking {
            testDb.insertFeedItemWithBlob(item, description)
        }

        activityRule.launchActivity(
            Intent().also {
                it.putExtra(ARG_ID, itemId)
            }
        )

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getLiveFeedItemImageText(itemId).observe(activityRule.activity, observer)
            }

            verify(exactly = 2, timeout = 500) {
                observer.onChanged(any())
            }

            clearMocks(observer)

            fail("Not monitoring file")
            /*assertEquals(1, testDb.db.feedItemDao().updateFeedItem(item.copy(
                    id = itemId,
                    description = "updated <img src='file://img.png' alt='img here'></img>")))*/

//            verify(exactly = 1, timeout = 500) {
//                observer.onChanged(any())
//            }
        }
    }
}

suspend fun KodeinAwareActivity.getLiveFeedItemImageText(id: Long): LiveData<Spanned> {
    val viewModel: FeedItemViewModel by instance()
    return viewModel.getLiveDefaultText(
        TextOptions(
            id,
            maxImageSize(),
            false
        ),
        null
    )
}
