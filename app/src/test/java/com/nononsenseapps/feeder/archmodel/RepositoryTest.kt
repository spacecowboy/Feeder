package com.nononsenseapps.feeder.archmodel

import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.reader.TextToDisplay
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertTrue
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
    private lateinit var androidSystemStore: AndroidSystemStore

    override val di by DI.lazy {
        bind<Repository>() with singleton { Repository(di) }
        bind<FeedItemStore>() with instance(feedItemStore)
        bind<SettingsStore>() with instance(settingsStore)
        bind<SessionStore>() with instance(sessionStore)
        bind<FeedStore>() with instance(feedStore)
        bind<AndroidSystemStore>() with instance(androidSystemStore)
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)
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
            PrefValOpenWith.OPEN_WITH_CUSTOM_TAB,
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
}
