package com.nononsenseapps.feeder.db.room

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class LatestSyncQueryTest {
    private lateinit var db: AppDatabase
    private val lifecycle = LifecycleRegistry(mockk())

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java).build()

        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun latestSyncIsNullWhenEmptyDB() {
        val observer: (t: DateTime?) -> Unit = mockk(relaxed = true)
        runBlocking {
            withContext(Dispatchers.Main) {
                db.feedDao().getLastSyncTime().observe({ lifecycle }, observer)
            }
        }

        verify(timeout = 200) {
            observer.invoke(null)
        }
    }

    @Test
    fun latestSyncWorksIfFeed() {
        val dt = DateTime.now()

        db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo"),
                lastSync = dt
        ))

        val observer: (t: DateTime?) -> Unit = mockk(relaxed = true)
        runBlocking {
            withContext(Dispatchers.Main) {
                db.feedDao().getLastSyncTime().observe({ lifecycle }, observer)
            }
        }

        verify(timeout = 200) {
            observer.invoke(dt.toDateTime(DateTimeZone.UTC))
        }
    }

    @Test
    fun latestSyncGetsLatestValue() {
        db.feedDao().insertFeed(Feed(
                title = "foo",
                url = URL("http://foo"),
                lastSync = DateTime(5, DateTimeZone.UTC)
        ))

        db.feedDao().insertFeed(Feed(
                title = "bar",
                url = URL("http://bar"),
                lastSync = DateTime(999, DateTimeZone.UTC)
        ))

        val observer: (t: DateTime?) -> Unit = mockk(relaxed = true)
        runBlocking {
            withContext(Dispatchers.Main) {
                db.feedDao().getLastSyncTime().observe({ lifecycle }, observer)
            }
        }

        verify(timeout = 200) {
            observer.invoke(DateTime(999, DateTimeZone.UTC))
        }
    }
}