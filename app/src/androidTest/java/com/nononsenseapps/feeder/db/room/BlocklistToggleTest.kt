package com.nononsenseapps.feeder.db.room

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

/**
 * Test to verify that toggling "Apply to summaries" correctly applies/unapplies the blocklist.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class BlocklistToggleTest {
    @get:Rule
    val testDb = TestDatabaseRule(ApplicationProvider.getApplicationContext())

    private lateinit var feedDao: FeedDao
    private lateinit var feedItemDao: FeedItemDao
    private lateinit var blocklistDao: BlocklistDao

    private val testFeedId = 1L

    @Before
    fun setup() {
        feedDao = testDb.db.feedDao()
        feedItemDao = testDb.db.feedItemDao()
        blocklistDao = testDb.db.blocklistDao()

        runBlocking {
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
    fun toggleApplyToSummaries_correctlyBlocksAndUnblocks() =
        runBlocking {
            // Create articles - one with keyword in snippet, one with keyword in link
            val article1Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "test1",
                        feedId = testFeedId,
                        plainTitle = "Some Normal Title",
                        plainSnippet = "This article mentions citizens in the description.",
                        pubDate = ZonedDateTime.now(),
                        link = "https://example.com/article",
                    ),
                )
            val article2Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "test2",
                        feedId = testFeedId,
                        plainTitle = "Some Normal Title",
                        plainSnippet = "Normal snippet",
                        pubDate = ZonedDateTime.now(),
                        link = "https://example.com/citizens",
                    ),
                )

            // Add "citizens" to blocklist
            blocklistDao.insertSafely("citizens")

            // Step 1: Apply blocklist with applyToSummaries = FALSE and applyToLinks = FALSE
            blocklistDao.setItemBlockStatus(Instant.now(), applyToSummaries = false, applyToLinks = false)

            var article1 = feedItemDao.loadFeedItem(article1Id)
            var article2 = feedItemDao.loadFeedItem(article2Id)

            assertNull(
                "Article1 should NOT be blocked when checking title only (toggle OFF)",
                article1?.blockTime,
            )
            assertNull(
                "Article2 should NOT be blocked when checking title only (toggle OFF)",
                article2?.blockTime,
            )

            // Step 2: Apply blocklist with applyToSummaries = TRUE and applyToLinks = TRUE
            blocklistDao.setItemBlockStatus(Instant.now(), applyToSummaries = true, applyToLinks = true)

            article1 = feedItemDao.loadFeedItem(article1Id)
            article2 = feedItemDao.loadFeedItem(article2Id)

            assertNotNull(
                "Article1 SHOULD be blocked when checking summaries (toggle ON)",
                article1?.blockTime,
            )
            assertNotNull(
                "Article2 SHOULD be blocked when checking links (toggle ON)",
                article2?.blockTime,
            )

            // Step 3: Toggle back to FALSE - should unblock
            blocklistDao.setItemBlockStatus(Instant.now(), applyToSummaries = false, applyToLinks = false)

            article1 = feedItemDao.loadFeedItem(article1Id)
            article2 = feedItemDao.loadFeedItem(article2Id)

            assertNull(
                "Article1 should be unblocked when toggle is OFF again",
                article1?.blockTime,
            )
            assertNull(
                "Article2 should be unblocked when toggle is OFF again",
                article2?.blockTime,
            )
        }

    @Test
    fun addWordToBlocklist_withToggleAlreadyOn_blocksImmediately() =
        runBlocking {
            // Create articles - one with keyword in snippet, one with keyword in link
            val article1Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "test1",
                        feedId = testFeedId,
                        plainTitle = "Normal Title",
                        plainSnippet = "This mentions advertisement in the summary.",
                        pubDate = ZonedDateTime.now(),
                        link = "https://example.com/article",
                    ),
                )
            val article2Id =
                feedItemDao.insertFeedItem(
                    FeedItem(
                        guid = "test2",
                        feedId = testFeedId,
                        plainTitle = "Normal Title",
                        plainSnippet = "Normal snippet",
                        pubDate = ZonedDateTime.now(),
                        link = "https://example.com/advertisement",
                    ),
                )

            // First, apply blocklist with empty blocklist but toggle ON
            blocklistDao.setItemBlockStatus(Instant.now(), applyToSummaries = true, applyToLinks = true)

            var article1 = feedItemDao.loadFeedItem(article1Id)
            var article2 = feedItemDao.loadFeedItem(article2Id)

            assertNull("Article1 should not be blocked yet (no patterns)", article1?.blockTime)
            assertNull("Article2 should not be blocked yet (no patterns)", article2?.blockTime)

            // Now add "advertisement" to blocklist
            blocklistDao.insertSafely("advertisement")

            // Re-apply blocklist with toggle still ON
            blocklistDao.setItemBlockStatus(Instant.now(), applyToSummaries = true, applyToLinks = true)

            article1 = feedItemDao.loadFeedItem(article1Id)
            article2 = feedItemDao.loadFeedItem(article2Id)

            assertNotNull(
                "Article1 SHOULD be blocked after adding pattern with toggle ON",
                article1?.blockTime,
            )
            assertNotNull(
                "Article2 SHOULD be blocked after adding pattern with toggle ON",
                article2?.blockTime,
            )
        }
}
