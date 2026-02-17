package com.nononsenseapps.feeder.db.room

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
@MediumTest
class BlocklistDaoTest {
    @get:Rule
    val testDb = TestDatabaseRule(ApplicationProvider.getApplicationContext())

    private lateinit var feedDao: FeedDao
    private lateinit var feedItemDao: FeedItemDao
    private lateinit var blocklistDao: BlocklistDao

    private val testFeedId = 1L
    private val blockTime = Instant.now()

    @Before
    fun setup() {
        feedDao = testDb.db.feedDao()
        feedItemDao = testDb.db.feedItemDao()
        blocklistDao = testDb.db.blocklistDao()

        runBlocking {
            // Create a test feed
            feedDao.insertFeed(
                Feed(
                    id = testFeedId,
                    title = "Test Feed",
                    url = URL("https://example.com/feed"),
                ),
            )
        }
    }

    @After
    fun tearDown() {
        testDb.db.close()
    }

    @Test
    fun blockByTitleOnly_doesNotBlockBySnippet() =
        runBlocking {
            // Add blocklist pattern
            blocklistDao.insertSafely("blocked")

            // Create items - one with blocked title, one with blocked snippet
            val item1Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item1",
                        feedId = testFeedId,
                        plainTitle = "This contains blocked word",
                        plainSnippet = "Normal snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )
            val item2Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item2",
                        feedId = testFeedId,
                        plainTitle = "Normal title",
                        plainSnippet = "This contains blocked word",
                        pubDate = ZonedDateTime.now(),
                    ),
                )
            val item3Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item3",
                        feedId = testFeedId,
                        plainTitle = "Clean title",
                        plainSnippet = "Clean snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )

            // Block with title only (applyToSummaries = false)
            blocklistDao.setItemBlockStatus(blockTime, applyToSummaries = false)

            // Verify: item1 should be blocked, item2 should NOT be blocked, item3 should not be blocked
            val retrievedItem1 = feedItemDao.loadFeedItem(item1Id)
            val retrievedItem2 = feedItemDao.loadFeedItem(item2Id)
            val retrievedItem3 = feedItemDao.loadFeedItem(item3Id)

            assertNotNull("Item1 should be blocked", retrievedItem1?.blockTime)
            assertNull("Item2 should NOT be blocked (title-only mode)", retrievedItem2?.blockTime)
            assertNull("Item3 should not be blocked", retrievedItem3?.blockTime)
        }

    @Test
    fun blockByTitleAndSnippet_blocksFromBoth() =
        runBlocking {
            // Add blocklist pattern
            blocklistDao.insertSafely("blocked")

            // Create items - one with blocked title, one with blocked snippet
            val item1Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item1",
                        feedId = testFeedId,
                        plainTitle = "This contains blocked word",
                        plainSnippet = "Normal snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )
            val item2Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item2",
                        feedId = testFeedId,
                        plainTitle = "Normal title",
                        plainSnippet = "This contains blocked word",
                        pubDate = ZonedDateTime.now(),
                    ),
                )
            val item3Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item3",
                        feedId = testFeedId,
                        plainTitle = "Clean title",
                        plainSnippet = "Clean snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )

            // Block with title AND snippet (applyToSummaries = true)
            blocklistDao.setItemBlockStatus(blockTime, applyToSummaries = true)

            // Verify: both item1 and item2 should be blocked, item3 should not
            val retrievedItem1 = feedItemDao.loadFeedItem(item1Id)
            val retrievedItem2 = feedItemDao.loadFeedItem(item2Id)
            val retrievedItem3 = feedItemDao.loadFeedItem(item3Id)

            assertNotNull("Item1 should be blocked", retrievedItem1?.blockTime)
            assertNotNull("Item2 should be blocked (title+snippet mode)", retrievedItem2?.blockTime)
            assertNull("Item3 should not be blocked", retrievedItem3?.blockTime)
        }

    @Test
    fun blockWhereNull_onlyBlocksUnblockedItems() =
        runBlocking {
            // Add blocklist pattern
            blocklistDao.insertSafely("test")

            // Create items
            val item1Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item1",
                        feedId = testFeedId,
                        plainTitle = "test title",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )
            val item2Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item2",
                        feedId = testFeedId,
                        plainTitle = "another test",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                        blockTime = Instant.now().minusSeconds(3600), // Already blocked
                    ),
                )

            val newBlockTime = Instant.now()

            // Block only where null
            blocklistDao.setItemBlockStatusWhereNull(newBlockTime, applyToSummaries = false)

            // Verify: item1 should have new block time, item2 should keep old block time
            val retrievedItem1 = feedItemDao.loadFeedItem(item1Id)
            val retrievedItem2 = feedItemDao.loadFeedItem(item2Id)

            assertNotNull("Item1 should be blocked", retrievedItem1?.blockTime)
            assertEquals(
                "Item1 should have new block time",
                newBlockTime.epochSecond,
                retrievedItem1?.blockTime?.epochSecond,
            )
            assertNotNull("Item2 should still be blocked", retrievedItem2?.blockTime)
            // Item2's blockTime should NOT be the new one - it should keep its old time
            assert(retrievedItem2!!.blockTime!!.isBefore(newBlockTime.minusSeconds(3000))) {
                "Item2 should keep old block time"
            }
        }

    @Test
    fun blockForNewInFeed_onlyBlocksSpecificFeed() =
        runBlocking {
            // Create second feed
            val secondFeedId = 2L
            feedDao.insertFeed(
                Feed(
                    id = secondFeedId,
                    title = "Second Feed",
                    url = URL("https://example.com/feed2"),
                ),
            )

            // Add blocklist pattern
            blocklistDao.insertSafely("spam")

            // Create items in different feeds
            val item1Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item1",
                        feedId = testFeedId,
                        plainTitle = "spam title",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )
            val item2Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item2",
                        feedId = secondFeedId,
                        plainTitle = "spam title",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )

            // Block only for first feed
            blocklistDao.setItemBlockStatusForNewInFeed(testFeedId, blockTime, applyToSummaries = false)

            // Verify: only item1 should be blocked
            val retrievedItem1 = feedItemDao.loadFeedItem(item1Id)
            val retrievedItem2 = feedItemDao.loadFeedItem(item2Id)

            assertNotNull("Item1 should be blocked", retrievedItem1?.blockTime)
            assertNull("Item2 should NOT be blocked (different feed)", retrievedItem2?.blockTime)
        }

    @Test
    fun unblockWhenPatternRemoved() =
        runBlocking {
            // Add and then remove pattern
            blocklistDao.insertSafely("bad")

            val itemId =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item1",
                        feedId = testFeedId,
                        plainTitle = "bad title",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )

            // Block
            blocklistDao.setItemBlockStatus(blockTime, applyToSummaries = false)
            var retrievedItem = feedItemDao.loadFeedItem(itemId)
            assertNotNull("Item should be blocked", retrievedItem?.blockTime)

            // Remove pattern and re-run blocking
            blocklistDao.deletePattern("bad")
            blocklistDao.setItemBlockStatus(Instant.now(), applyToSummaries = false)

            // Verify: item should be unblocked
            retrievedItem = feedItemDao.loadFeedItem(itemId)
            assertNull("Item should be unblocked after pattern removed", retrievedItem?.blockTime)
        }

    @Test
    fun globPatternMatching() =
        runBlocking {
            // Test wildcard pattern
            blocklistDao.insertSafely("advertise")

            val item1Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item1",
                        feedId = testFeedId,
                        plainTitle = "This is an advertisement",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )
            val item2Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item2",
                        feedId = testFeedId,
                        plainTitle = "Advertiser announcement",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )

            blocklistDao.setItemBlockStatus(blockTime, applyToSummaries = false)

            // Both should be blocked due to wildcard pattern
            val retrievedItem1 = feedItemDao.loadFeedItem(item1Id)
            val retrievedItem2 = feedItemDao.loadFeedItem(item2Id)

            assertNotNull("Item1 should match *advertise*", retrievedItem1?.blockTime)
            assertNotNull("Item2 should match *advertise*", retrievedItem2?.blockTime)
        }

    @Test
    fun caseInsensitiveMatching() =
        runBlocking {
            blocklistDao.insertSafely("blocked")

            val itemId =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item1",
                        feedId = testFeedId,
                        plainTitle = "This has BLOCKED in caps",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )

            blocklistDao.setItemBlockStatus(blockTime, applyToSummaries = false)

            val retrievedItem = feedItemDao.loadFeedItem(itemId)
            assertNotNull("Should match case-insensitively", retrievedItem?.blockTime)
        }

    @Test
    fun multiplePatterns() =
        runBlocking {
            blocklistDao.insertSafely("spam")
            blocklistDao.insertSafely("ads")

            val item1Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item1",
                        feedId = testFeedId,
                        plainTitle = "spam content",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )
            val item2Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item2",
                        feedId = testFeedId,
                        plainTitle = "ads here",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )
            val item3Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "item3",
                        feedId = testFeedId,
                        plainTitle = "clean content",
                        plainSnippet = "snippet",
                        pubDate = ZonedDateTime.now(),
                    ),
                )

            blocklistDao.setItemBlockStatus(blockTime, applyToSummaries = false)

            val retrievedItem1 = feedItemDao.loadFeedItem(item1Id)
            val retrievedItem2 = feedItemDao.loadFeedItem(item2Id)
            val retrievedItem3 = feedItemDao.loadFeedItem(item3Id)

            assertNotNull("Item1 should be blocked by 'spam'", retrievedItem1?.blockTime)
            assertNotNull("Item2 should be blocked by 'ads'", retrievedItem2?.blockTime)
            assertNull("Item3 should not be blocked", retrievedItem3?.blockTime)
        }
}
