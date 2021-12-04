package com.nononsenseapps.feeder.archmodel

import android.app.Application
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.RemoteReadMarkReadyToBeApplied
import com.nononsenseapps.feeder.util.addDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.reportShortcutToFeedUsed
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.verify
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.threeten.bp.Instant

class RepositoryTest : DIAware {
    private val repository: Repository by instance()

    @MockK
    private lateinit var feedItemStore: FeedItemStore

    @MockK
    private lateinit var settingsStore: SettingsStore

    @MockK
    private lateinit var sessionStore: SessionStore

    @MockK
    private lateinit var feedStore: FeedStore

    @MockK
    private lateinit var syncRemoteStore: SyncRemoteStore

    @MockK
    private lateinit var androidSystemStore: AndroidSystemStore

    @MockK
    private lateinit var application: FeederApplication

    override val di by DI.lazy {
        bind<Repository>() with singleton { Repository(di) }
        bind<FeedItemStore>() with instance(feedItemStore)
        bind<SettingsStore>() with instance(settingsStore)
        bind<SessionStore>() with instance(sessionStore)
        bind<SyncRemoteStore>() with instance(syncRemoteStore)
        bind<FeedStore>() with instance(feedStore)
        bind<AndroidSystemStore>() with instance(androidSystemStore)
        bind<Application>() with instance(application)
        bind<ApplicationCoroutineScope>() with singleton { ApplicationCoroutineScope() }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)
    }

    @Test
    fun setCurrentFeedAndTagTagDoesNotReportFeedShortcut() {
        mockkStatic("com.nononsenseapps.feeder.util.ContextExtensionsKt")

        repository.setCurrentFeedAndTag(ID_UNSET, "foo")

        coVerify(timeout = 500L, exactly = 0) {
            application.addDynamicShortcutToFeed(
                "fooFeed",
                10L,
                null
            )
            application.reportShortcutToFeedUsed(10L)
        }
    }

    @Test
    fun setCurrentFeedAndTagFeedReportsShortcut() {
        coEvery { feedStore.getDisplayTitle(10L) } returns "fooFeed"
        coEvery { settingsStore.setCurrentFeedAndTag(any(), any()) } just Runs

        mockkStatic("com.nononsenseapps.feeder.util.ContextExtensionsKt")

        repository.setCurrentFeedAndTag(10L, "")

        coVerify(timeout = 500L) {
            application.addDynamicShortcutToFeed(
                "fooFeed",
                10L,
                null
            )
            application.reportShortcutToFeedUsed(10L)
        }
    }

    @Test
    fun getTextToDisplayForItem() {
        coEvery { feedItemStore.getFullTextByDefault(5L) } returns true
        coEvery { feedItemStore.getFullTextByDefault(6L) } returns false

        assertEquals(
            TextToDisplay.FULLTEXT,
            runBlocking {
                repository.getTextToDisplayForItem(5L)
            }
        )


        assertEquals(
            TextToDisplay.DEFAULT,
            runBlocking {
                repository.getTextToDisplayForItem(6L)
            }
        )
    }

    @Test
    fun getArticleOpener() {
        coEvery { feedItemStore.getArticleOpener(5L) } returns PREF_VAL_OPEN_WITH_CUSTOM_TAB

        assertEquals(
            ItemOpener.CUSTOM_TAB,
            runBlocking {
                repository.getArticleOpener(5L)
            }
        )
    }

    @Test
    fun getArticleOpenerDefaultFallback() {
        coEvery { feedItemStore.getArticleOpener(5L) } returns ""
        every { settingsStore.itemOpener } returns MutableStateFlow(ItemOpener.DEFAULT_BROWSER)

        assertEquals(
            ItemOpener.DEFAULT_BROWSER,
            runBlocking {
                repository.getArticleOpener(5L)
            }
        )
    }

    @Test
    fun markAllAsReadInCurrentFeed() {
        runBlocking {
            repository.markAllAsReadInFeedOrTag(5L, "foo")
        }

        coVerify {
            feedItemStore.markAllAsReadInFeed(5L)
        }

    }

    @Test
    fun markAllAsReadInCurrentTag() {
        runBlocking {
            repository.markAllAsReadInFeedOrTag(ID_UNSET, "foo")
        }

        coVerify {
            feedItemStore.markAllAsReadInTag("foo")
        }
    }

    @Test
    fun markAllAsReadInCurrentAll() {
        runBlocking {
            repository.markAllAsReadInFeedOrTag(ID_ALL_FEEDS, "")
        }

        coVerify {
            feedItemStore.markAllAsRead()
        }
    }

    @Test
    fun getScreenTitleForCurrentFeedOrTagAll() {
        val result = runBlocking {
            repository.getScreenTitleForFeedOrTag(ID_UNSET, "").toList().first()
        }

        assertEquals(ScreenTitle(title = null), result)
    }

    @Test
    fun getScreenTitleForCurrentFeedOrTagTag() {
        val result = runBlocking {
            repository.getScreenTitleForFeedOrTag(ID_UNSET, "fwr").toList().first()
        }

        assertEquals(ScreenTitle(title = "fwr"), result)
    }

    @Test
    fun getScreenTitleForCurrentFeedOrTagFeed() {
        coEvery { feedStore.getDisplayTitle(5L) } returns "floppa"

        val result = runBlocking {
            repository.getScreenTitleForFeedOrTag(5L, "fwr").toList().first()
        }

        assertEquals(ScreenTitle(title = "floppa"), result)

        coVerify {
            feedStore.getDisplayTitle(5L)
        }
    }

    @Test
    fun deleteFeeds() {
        coEvery { feedStore.deleteFeeds(any()) } just Runs

        runBlocking {
            repository.deleteFeeds(listOf(1, 2))
        }

        coVerify {
            feedStore.deleteFeeds(listOf(1, 2))
        }

        verify {
            androidSystemStore.removeDynamicShortcuts(listOf(1, 2))
        }

    }

    @Test
    fun ensurePeriodicSyncConfigured() {
        coEvery { settingsStore.configurePeriodicSync(any()) } just Runs

        runBlocking {
            repository.ensurePeriodicSyncConfigured()
        }

        coVerify {
            settingsStore.configurePeriodicSync(false)
        }
    }

    @Test
    fun getFeedsItemsWithDefaultFullTextParse() {
        coEvery { feedItemStore.getFeedsItemsWithDefaultFullTextParse() } returns flowOf(emptyList())

        val result = runBlocking {
            repository.getFeedsItemsWithDefaultFullTextParse().first()
        }

        assertTrue {
            result.isEmpty()
        }

        coVerify {
            feedItemStore.getFeedsItemsWithDefaultFullTextParse()
        }
    }

    @Test
    fun currentlySyncingLatestTimestamp() {
        every { feedStore.getCurrentlySyncingLatestTimestamp() } returns flowOf(null)

        val result = runBlocking {
            repository.currentlySyncingLatestTimestamp.toList()
        }

        assertEquals(1, result.size)
        assertEquals(Instant.EPOCH, result.first())

        verify {
            feedStore.getCurrentlySyncingLatestTimestamp()
        }
    }

    @Test
    fun applyRemoteReadMarks() {
        coEvery { syncRemoteStore.getRemoteReadMarksReadyToBeApplied() } returns listOf(
            RemoteReadMarkReadyToBeApplied(1L, 2L),
            RemoteReadMarkReadyToBeApplied(3L, 4L)
        )

        runBlocking {
            repository.applyRemoteReadMarks()
        }

        coVerify {
            syncRemoteStore.getRemoteReadMarksReadyToBeApplied()
            feedItemStore.markAsRead(listOf(2L, 4L))
            syncRemoteStore.setSynced(2L)
            syncRemoteStore.setSynced(4L)
            syncRemoteStore.deleteReadStatusSyncs(listOf(1L, 3L))
        }
        confirmVerified(feedItemStore, syncRemoteStore)
    }

    @Test
    fun remoteMarkAsReadExistingItem() {
        coEvery { feedItemStore.getFeedItemId(URL("https://foo"), "guid") } returns 5L

        runBlocking {
            repository.remoteMarkAsRead(URL("https://foo"), "guid")
        }

        coVerify {
            feedItemStore.getFeedItemId(URL("https://foo"), "guid")
            syncRemoteStore.setSynced(5L)
            feedItemStore.markAsReadAndNotified(5L)
        }
        confirmVerified(feedItemStore, syncRemoteStore)
    }

    @Test
    fun remoteMarkAsReadNonExistingItem() {
        coEvery { feedItemStore.getFeedItemId(any(), any()) } returns null
        coEvery { syncRemoteStore.addRemoteReadMark(any(), any()) } just Runs

        runBlocking {
            repository.remoteMarkAsRead(URL("https://foo"), "guid")
        }

        coVerify {
            feedItemStore.getFeedItemId(URL("https://foo"), "guid")
            syncRemoteStore.addRemoteReadMark(URL("https://foo"), "guid")
        }
        confirmVerified(feedItemStore, syncRemoteStore)
    }
}
