package com.nononsenseapps.feeder.archmodel

import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.model.FeedUnreadCount
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerFeed
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTag
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTop
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.time.Instant

class FeedStoreTest : DIAware {
    private val store: FeedStore by instance()

    @MockK
    private lateinit var dao: FeedDao

    override val di by DI.lazy {
        bind<FeedDao>() with instance(dao)
        bind<FeedStore>() with singleton { FeedStore(di) }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)
    }

    @Test
    fun getFeed() {
        coEvery { dao.loadFeed(5L) } returns Feed(id = 5L)

        val feed =
            runBlocking {
                store.getFeed(5L)
            }

        assertEquals(
            Feed(id = 5L),
            feed,
        )
    }

    @Test
    fun getDisplayTitle() {
        coEvery { dao.getFeedTitle(5L) } returns FeedTitle(customTitle = "bob")

        assertEquals(
            "bob",
            runBlocking {
                store.getDisplayTitle(5L)
            },
        )
    }

    @Test
    fun deleteFeeds() {
        runBlocking {
            store.deleteFeeds(listOf(3L, 5L))
        }

        coVerify {
            dao.deleteFeeds(listOf(3L, 5L))
        }
    }

    @Test
    fun allTags() {
        every { dao.loadAllTags() } returns flowOf(listOf("one", "two"))

        val tags =
            runBlocking {
                store.allTags.toList().first()
            }

        assertEquals(
            listOf("one", "two"),
            tags,
        )
    }

    @Test
    fun saveNewFeed() {
        coEvery { dao.insertFeed(any()) } returns 5L

        val id =
            runBlocking {
                store.saveFeed(Feed())
            }

        assertEquals(
            5L,
            id,
        )
    }

    @Test
    fun saveExistingFeed() {
        val feedId =
            runBlocking {
                store.saveFeed(Feed(id = 5L))
            }

        assertEquals(5L, feedId)

        coVerify {
            dao.updateFeed(Feed(id = 5L))
        }
    }

    @Test
    fun drawerItemsWithUnreadCounts() {
        every { dao.loadFlowOfFeedsWithUnreadCounts() } returns
            flow {
                emit(
                    listOf(
                        FeedUnreadCount(id = 1, title = "zob", unreadCount = 3, currentlySyncing = true),
                        FeedUnreadCount(id = 2, title = "bob", tag = "zork", unreadCount = 4, currentlySyncing = false),
                        FeedUnreadCount(id = 3, title = "alice", tag = "alpha", unreadCount = 5, currentlySyncing = true),
                        FeedUnreadCount(id = 4, title = "argh", tag = "alpha", unreadCount = 7, currentlySyncing = false),
                    ),
                )
            }
        val drawerItems =
            runBlocking {
                store.drawerItemsWithUnreadCounts.toList().first()
            }

        assertEquals(
            listOf(
                DrawerTop(unreadCount = 19, totalChildren = 4),
                DrawerTag("alpha", 12, -1002, totalChildren = 2),
                DrawerFeed(3, "alpha", "alice", unreadCount = 5),
                DrawerFeed(4, "alpha", "argh", unreadCount = 7),
                DrawerTag("zork", 4, -1001, totalChildren = 1),
                DrawerFeed(2, "zork", "bob", unreadCount = 4),
                DrawerFeed(1, "", "zob", unreadCount = 3),
            ),
            drawerItems,
        )
    }

    @Test
    fun getFeedTitles() {
        every { dao.getFeedTitlesWithId(5L) } returns
            flowOf(
                listOf(
                    FeedTitle(5L, "fejd"),
                ),
            )
        every { dao.getFeedTitlesWithTag("foo") } returns
            flowOf(
                listOf(
                    FeedTitle(5L, "fejd"),
                    FeedTitle(7L, "axv"),
                ),
            )
        every { dao.getAllFeedTitles() } returns
            flowOf(
                listOf(
                    FeedTitle(5L, "fejd"),
                    FeedTitle(7L, "axv"),
                    FeedTitle(8L, "zzz"),
                ),
            )

        assertEquals(
            listOf(FeedTitle(5L, "fejd")),
            runBlocking { store.getFeedTitles(5L, "flopp").toList().first() },
        )

        assertEquals(
            listOf(
                FeedTitle(5L, "fejd"),
                FeedTitle(7L, "axv"),
            ),
            runBlocking { store.getFeedTitles(-1L, "foo").toList().first() },
        )

        assertEquals(
            listOf(
                FeedTitle(5L, "fejd"),
                FeedTitle(7L, "axv"),
                FeedTitle(8L, "zzz"),
            ),
            runBlocking { store.getFeedTitles(-1L, "").toList().first() },
        )
    }

    @Test
    fun getCurrentlySyncingLatestTimestamp() {
        every { dao.getCurrentlySyncingLatestTimestamp() } returns flowOf(null)

        val result =
            runBlocking {
                store.getCurrentlySyncingLatestTimestamp().toList()
            }

        assertEquals(null, result.first())

        verify { dao.getCurrentlySyncingLatestTimestamp() }
    }

    @Test
    fun setCurrentlySyncingOn() {
        val now = Instant.now()
        runBlocking {
            store.setCurrentlySyncingOn(5L, true)
            store.setCurrentlySyncingOn(8L, false, now)
        }

        coVerify {
            dao.setCurrentlySyncingOn(5L, true)
            dao.setCurrentlySyncingOn(8L, false, now)
        }
    }

    @Test
    fun upsertFeed() {
        coEvery { dao.upsert(any()) } returns 6L

        val result =
            runBlocking {
                store.upsertFeed(Feed())
            }

        assertEquals(6L, result)

        coVerify {
            dao.upsert(Feed())
        }
    }
}
