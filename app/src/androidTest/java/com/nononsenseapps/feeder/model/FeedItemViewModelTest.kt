package com.nononsenseapps.feeder.model

import android.content.Intent
import android.text.Spanned
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.ReaderActivity
import com.nononsenseapps.feeder.util.ARG_ID
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL


@RunWith(AndroidJUnit4::class)
class FeedItemViewModelTest {
    @get:Rule
    var activityRule: ActivityTestRule<ReaderActivity> = ActivityTestRule(ReaderActivity::class.java, false, false)

    private lateinit var db: AppDatabase

    private var feedId: Long = ID_UNSET
    private var itemId: Long = ID_UNSET

    @Before
    fun initDb() {
        db = Room.inMemoryDatabaseBuilder(getApplicationContext(),
                AppDatabase::class.java).build()
        // Ensure all classes use test database
        AppDatabase.setInstance(db)

        feedId = db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo")
        ))
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun databaseLoadCallsOnChangeTwiceWhenImages() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        itemId = db.feedItemDao().insertFeedItem(FeedItem(
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
                activityRule.activity.getFeedItemViewModel(itemId).liveImageText.observe(activityRule.activity, observer)
            }

            verify(exactly = 2, timeout = 500) {
                observer.onChanged(any())
            }
        }
    }

    @Test
    fun databaseLoadCallsOnChangeOnceWhenNoImages() {
        val observer = mockk<Observer<Spanned>>(relaxed = true)

        itemId = db.feedItemDao().insertFeedItem(FeedItem(
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
                activityRule.activity.getFeedItemViewModel(itemId).liveImageText.observe(activityRule.activity, observer)
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

        itemId = db.feedItemDao().insertFeedItem(item)
        item = item.copy(id = itemId)

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getFeedItemViewModel(itemId).liveImageText.observe(activityRule.activity, observer)
            }

            verify(exactly = 1, timeout = 500) {
                observer.onChanged(any())
            }

            clearMocks(observer)

            assertEquals(1, db.feedItemDao().updateFeedItem(item.copy(title = "updated title")))

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

        itemId = db.feedItemDao().insertFeedItem(item)
        item = item.copy(id = itemId)

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getFeedItemViewModel(itemId).liveImageText.observe(activityRule.activity, observer)
            }

            verify(exactly = 1, timeout = 500) {
                observer.onChanged(any())
            }

            clearMocks(observer)

            assertEquals(1, db.feedItemDao().updateFeedItem(item.copy(description = "updated body")))

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

        itemId = db.feedItemDao().insertFeedItem(item)

        activityRule.launchActivity(Intent().also {
            it.putExtra(ARG_ID, itemId)
        })

        runBlocking {
            withContext(Dispatchers.Main) {
                activityRule.activity.getFeedItemViewModel(itemId).liveImageText.observe(activityRule.activity, observer)
            }

            verify(exactly = 2, timeout = 500) {
                observer.onChanged(any())
            }

            clearMocks(observer)

            assertEquals(1, db.feedItemDao().updateFeedItem(item.copy(
                    id = itemId,
                    description = "updated <img src='file://img.png' alt='img here'></img>")))

            verify(exactly = 1, timeout = 500) {
                observer.onChanged(any())
            }
        }
    }
}

fun FragmentActivity.getFeedItemViewModel(id: Long): FeedItemViewModel {
    val factory = FeedItemViewModelFactory(application, id, maxImageSize())
    return ViewModelProviders.of(this, factory).get(FeedItemViewModel::class.java)
}
