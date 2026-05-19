package com.nononsenseapps.feeder.db

import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.model.ParsedArticle
import com.nononsenseapps.feeder.model.ParsedFeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FeedItemTest {
    @Test
    fun getDomain() {
        val fi1 = FeedItem(link = "https://www.cowboyprogrammer.org/some/path.txt")
        assertEquals("www.cowboyprogrammer.org", fi1.domain)

        val fi2 = FeedItem(enclosureLink = "https://www.cowboyprogrammer.org/some/path.txt")
        assertEquals("www.cowboyprogrammer.org", fi2.domain)

        val fi3 = FeedItem(enclosureLink = "asdff\\asdf")
        assertEquals(null, fi3.domain)
    }

    @Test
    fun getEnclosureFilename() {
        val fi1 = FeedItem(enclosureLink = "https://www.cowboyprogrammer.org/some/file.txt")
        assertEquals("file.txt", fi1.enclosureFilename)

        val fi2 = FeedItem(enclosureLink = "https://www.cowboyprogrammer.org/some/file.txt?param=2")
        assertEquals("file.txt", fi2.enclosureFilename)

        val fi3 = FeedItem(enclosureLink = "https://www.cowboyprogrammer.org/some%20file.txt")
        assertEquals("some file.txt", fi3.enclosureFilename)

        val fi4 = FeedItem(enclosureLink = "https://www.cowboyprogrammer.org")
        assertEquals(null, fi4.enclosureFilename)
    }

    @Test
    fun updateFromParsedEntry_usesParsedDateWhenPresent() {
        val feedItem = FeedItem()
        val parsedDate = "2024-01-15T10:00:00Z"
        val irrelevantClock = Clock.fixed(Instant.parse("2000-06-01T00:00:00Z"), ZoneOffset.UTC)

        feedItem.updateFromParsedEntry(
            entry = ParsedArticle(id = "id-1", date_published = parsedDate),
            entryGuid = "guid-1",
            feed = ParsedFeed(title = "Feed", items = emptyList()),
            clock = irrelevantClock,
        )

        val expected = ZonedDateTime.parse(parsedDate)
        assertEquals(expected, feedItem.pubDate)
    }

    @Test
    fun updateFromParsedEntry_usesClockWhenDateIsMissing() {
        val feedItem = FeedItem()
        val fixedInstant = Instant.parse("2024-03-20T12:00:00Z")
        val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

        feedItem.updateFromParsedEntry(
            entry = ParsedArticle(id = "id-1", date_published = null),
            entryGuid = "guid-1",
            feed = ParsedFeed(title = "Feed", items = emptyList()),
            clock = fixedClock,
        )

        val expected = ZonedDateTime.now(fixedClock)
        assertEquals(expected, feedItem.pubDate)
    }

    @Test
    fun updateFromParsedEntry_doesNotOverwriteExistingDateWhenFeedHasNone() {
        val existingDate = ZonedDateTime.parse("2023-05-10T08:00:00Z")
        val feedItem = FeedItem(pubDate = existingDate)
        val fixedClock = Clock.fixed(Instant.parse("2024-03-20T12:00:00Z"), ZoneOffset.UTC)

        feedItem.updateFromParsedEntry(
            entry = ParsedArticle(id = "id-1", date_published = null),
            entryGuid = "guid-1",
            feed = ParsedFeed(title = "Feed", items = emptyList()),
            clock = fixedClock,
        )

        assertEquals(existingDate, feedItem.pubDate)
    }

    @Test
    fun updateFromParsedEntry_distinctClocksProduceDistinctDates() {
        val instantA = Instant.parse("2024-06-01T09:00:00Z")
        val instantB = Instant.parse("2024-06-01T09:00:01Z")
        val clockA = Clock.fixed(instantA, ZoneOffset.UTC)
        val clockB = Clock.fixed(instantB, ZoneOffset.UTC)

        val feedItemA = FeedItem()
        feedItemA.updateFromParsedEntry(
            entry = ParsedArticle(id = "id-a", date_published = null),
            entryGuid = "guid-a",
            feed = ParsedFeed(title = "Feed", items = emptyList()),
            clock = clockA,
        )

        val feedItemB = FeedItem()
        feedItemB.updateFromParsedEntry(
            entry = ParsedArticle(id = "id-b", date_published = null),
            entryGuid = "guid-b",
            feed = ParsedFeed(title = "Feed", items = emptyList()),
            clock = clockB,
        )

        assertNotNull(feedItemA.pubDate)
        assertNotNull(feedItemB.pubDate)
        assertNotEquals(feedItemA.pubDate, feedItemB.pubDate)
        assertTrue(feedItemB.pubDate!! > feedItemA.pubDate!!)
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun magnetLinkGivesNullFilename() {
        val fi =
            FeedItem(
                enclosureLink = "magnet:?xt=urn:btih:E6F5537982306CF703E5016B2BBD36C9B3E3CDD0&dn=Game+of+Thrones+S07E01+PROPER+WEBRip+x264+RARBG&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=http%3A%2F%2Ftracker.trackerfix.com%3A80%2Fannounce",
            )
        assertNull(fi.enclosureFilename)
    }
}
