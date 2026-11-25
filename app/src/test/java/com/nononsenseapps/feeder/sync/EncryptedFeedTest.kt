package com.nononsenseapps.feeder.sync

import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.db.room.Feed
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.instance
import java.net.URL
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EncryptedFeedTest : DIAware {
    @MockK
    private lateinit var repository: Repository

    override val di by DI.lazy {
        bind<Repository>() with instance(repository)
    }
    private val moshi = getMoshi()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun encryptedFeedCanBeParsedFromIncompleteAndOddJson() {
        val adapter = moshi.adapter<EncryptedFeed>()

        @Language("JSON")
        val json =
            """
            {
               "url": "https://foo.bar",
               "title": "foo",
               "alternateId": true,
               "notARealField": 1
            }
            """.trimIndent()
        val feed = adapter.fromJson(json)!!

        assertEquals(URL("https://foo.bar"), feed.url)
        assertEquals("foo", feed.title)
        assertTrue(feed.alternateId)
        assertFalse(feed.fullTextByDefault)
        assertFalse(feed.fetchOgImages)
        assertEquals("", feed.blockRules)
        assertEquals("", feed.allowRules)
    }

    @Test
    fun fetchOgImagesRoundTripsThroughEncryptedFeed() {
        coEvery { repository.getFolderPathStringList(5L) } returns listOf("folder1", "folder2")
        coEvery { repository.folderPathStringToFolderId(listOf("folder1", "folder2")) } returns 5L

        val original =
            Feed(
                url = URL("https://foo.bar"),
                title = "foo",
                folderIdNullable = 5L,
                fetchOgImages = false,
                whenModified = Instant.now(),
            )

        val encrypted =
            runBlocking {
                original.toEncryptedFeed(repository)
            }
        val restored =
            runBlocking {
                encrypted.updateFeedCopy(Feed(), repository)
            }

        assertFalse(encrypted.fetchOgImages)
        assertFalse(restored.fetchOgImages)
    }

    @Test
    fun multiLineRulesRoundTripThroughEncryptedFeed() {
        coEvery { repository.getFolderPathStringList(5L) } returns listOf("folder1", "folder2")
        coEvery { repository.folderPathStringToFolderId(listOf("folder1", "folder2")) } returns 5L

        val blockRules = "Title=(?i)sponsored\nURL=example\\.com/ads/"
        val allowRules = "Tag=(?i)linux"

        val original =
            Feed(
                url = URL("https://foo.bar"),
                title = "foo",
                folderIdNullable = 5L,
                blockRules = blockRules,
                allowRules = allowRules,
                whenModified = Instant.now(),
            )

        val encrypted =
            runBlocking {
                original.toEncryptedFeed(repository)
            }
        val restored =
            runBlocking {
                encrypted.updateFeedCopy(Feed(), repository)
            }

        assertEquals(blockRules, encrypted.blockRules)
        assertEquals(allowRules, encrypted.allowRules)
        assertEquals(blockRules, restored.blockRules)
        assertEquals(allowRules, restored.allowRules)
    }
}
