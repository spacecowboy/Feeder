package com.nononsenseapps.feeder.sync

import com.nononsenseapps.feeder.db.room.Feed
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.net.URL
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EncryptedFeedTest {
    private val moshi = getMoshi()

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
    }

    @Test
    fun fetchOgImagesRoundTripsThroughEncryptedFeed() {
        val original =
            Feed(
                url = URL("https://foo.bar"),
                title = "foo",
                fetchOgImages = false,
                whenModified = Instant.now(),
            )

        val encrypted = original.toEncryptedFeed()
        val restored = encrypted.updateFeedCopy(Feed())

        assertFalse(encrypted.fetchOgImages)
        assertFalse(restored.fetchOgImages)
    }
}
