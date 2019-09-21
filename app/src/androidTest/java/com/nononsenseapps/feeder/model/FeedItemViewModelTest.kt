package com.nononsenseapps.feeder.model

import android.content.Intent
import android.text.Spanned
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.base.CoroutineScopedKodeinAwareActivity
import com.nononsenseapps.feeder.base.getViewModel
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL


@RunWith(AndroidJUnit4::class)
class FeedItemViewModelTest {
    @get:Rule
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    private var feedId: Long = ID_UNSET
    private var itemId: Long = ID_UNSET

    @Before
    fun initDb() {
        feedId = testDb.db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo")
        ))
    }

    @Test
    fun databaseLoadCallsOnChangeTwiceWhenImages() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        itemId = testDb.db.feedItemDao().insertFeedItem(FeedItem(
                feedId = feedId,
                guid = "foobar",
                title = "title",
                description = "description <img src='file://img.png' alt='img here'></img>"
        ))

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

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
    fun databaseLoadCallsOnChangeOnceWhenNoImages() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        itemId = testDb.db.feedItemDao().insertFeedItem(FeedItem(
                feedId = feedId,
                guid = "foobar",
                title = "title",
                description = "description <b>bold</b>"
        ))

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getLiveFeedItemImageText(itemId).observe(activityRule.activity, observer)
            }

            verify(exactly = 1, timeout = 500) {
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
                title = "title",
                description = "description <b>bold</b>"
        )

        itemId = testDb.db.feedItemDao().insertFeedItem(item)
        item = item.copy(id = itemId)

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getLiveFeedItemImageText(itemId).observe(activityRule.activity, observer)
            }

            verify(exactly = 1, timeout = 500) {
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
    fun databaseLoadCallsOnChangeOnceOnSyncWithBodyUpdate() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        var item = FeedItem(
                feedId = feedId,
                guid = "foobar",
                title = "title",
                description = "description <b>bold</b>"
        )

        itemId = testDb.db.feedItemDao().insertFeedItem(item)
        item = item.copy(id = itemId)

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getLiveFeedItemImageText(itemId).observe(activityRule.activity, observer)
            }

            verify(exactly = 1, timeout = 500) {
                observer.onChanged(any())
            }

            clearMocks(observer)

            assertEquals(1, testDb.db.feedItemDao().updateFeedItem(item.copy(description = "updated body")))

            verify(exactly = 1, timeout = 500) {
                observer.onChanged(any())
            }
        }
    }

    @Test
    fun databaseLoadCallsOnChangeOnceOnSyncWithBodyUpdateWithImage() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        val item = FeedItem(
                feedId = feedId,
                guid = "foobar",
                title = "title",
                description = "description <img src='file://img.png' alt='img here'></img>"
        )

        itemId = testDb.db.feedItemDao().insertFeedItem(item)

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getLiveFeedItemImageText(itemId).observe(activityRule.activity, observer)
            }

            verify(exactly = 2, timeout = 500) {
                observer.onChanged(any())
            }

            clearMocks(observer)

            assertEquals(1, testDb.db.feedItemDao().updateFeedItem(item.copy(
                    id = itemId,
                    description = "updated <img src='file://img.png' alt='img here'></img>")))

            verify(exactly = 1, timeout = 500) {
                observer.onChanged(any())
            }
        }
    }
}

fun CoroutineScopedKodeinAwareActivity.getLiveFeedItemImageText(id: Long): MediatorLiveData<Spanned> {
    val viewModel: FeedItemViewModel = getViewModel()
    return viewModel.getLiveImageText(id, maxImageSize(), null)
}
