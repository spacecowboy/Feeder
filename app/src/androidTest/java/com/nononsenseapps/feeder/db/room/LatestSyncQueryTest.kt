package com.nononsenseapps.feeder.db.room

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
class LatestSyncQueryTest {
    private val lifecycle = LifecycleRegistry(mockk())
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    @Before
    fun setup() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    @Test
    fun latestSyncIsNullWhenEmptyDB() {
        val observer: (t: DateTime?) -> Unit = mockk(relaxed = true)
        runBlocking {
            withContext(Dispatchers.Main) {
                testDb.db.feedDao().getLastSyncTime().observe({ lifecycle }, observer)
            }
        }

        verify(timeout = 200) {
            observer.invoke(null)
        }
    }

    @Test
    fun latestSyncWorksIfFeed() {
        testDb.db.feedDao().insertFeed(Feed(
                title = "bar",
                url = URL("http://bar"),
                lastSync = DateTime(999, DateTimeZone.UTC)
        ))

        val observer: (t: DateTime?) -> Unit = mockk(relaxed = true)
        runBlocking {
            withContext(Dispatchers.Main) {
                testDb.db.feedDao().getLastSyncTime().observe({ lifecycle }, observer)
            }
        }

        verify(timeout = 200) {
            observer.invoke(DateTime(999, DateTimeZone.UTC))
        }
    }

    @Test
    fun latestSyncGetsLatestValue() {
        testDb.db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo"),
                lastSync = DateTime(5, DateTimeZone.UTC)
        ))

        testDb.db.feedDao().insertFeed(Feed(
                title = "bar",
                url = URL("http://bar"),
                lastSync = DateTime(999, DateTimeZone.UTC)
        ))

        val observer: (t: DateTime?) -> Unit = mockk(relaxed = true)
        runBlocking {
            withContext(Dispatchers.Main) {
                testDb.db.feedDao().getLastSyncTime().observe({ lifecycle }, observer)
            }
        }

        verify(timeout = 200) {
            observer.invoke(DateTime(999, DateTimeZone.UTC))
        }
    }
}
