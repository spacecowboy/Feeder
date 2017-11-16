package com.nononsenseapps.feeder.util

import com.nononsenseapps.jsonfeed.Attachment
import com.nononsenseapps.jsonfeed.Author
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.Item
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.modules.mediarss.MediaModule
import com.rometools.modules.mediarss.types.MediaContent
import com.rometools.modules.mediarss.types.Reference
import com.rometools.modules.mediarss.types.Thumbnail
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndLink
import com.rometools.rome.feed.synd.SyndPerson
import org.joda.time.DateTime
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.net.URI
import java.util.*
import kotlin.test.assertEquals

class RomeExtensionsKtTest {
    @Test
    fun author() {
        assertEquals(Author(name = "Bobby Jane"),
                mockSyndPerson(name = "Bobby Jane").asAuthor())
    }

    @Test
    fun feedLinkButNoLinks() {
        assertEquals(Feed(home_page_url = "homepage", title = null, items = emptyList()),
                mockSyndFeed(link = "homepage").asFeed())
    }

    @Test
    fun feedLinks() {
        assertEquals(Feed(home_page_url = "homepage", title = null, items = emptyList()),
                mockSyndFeed(links = listOf(mockSyndLink(href = "homepage",
                        rel = "alternate", type = "text/html"))).asFeed())
    }

    @Test
    fun itemFallsBackToFeedAuthor() {
        assertEquals(
                Feed(author = Author(name = "bob"), title = null,
                        items = listOf(Item(id = "id", author = Author(name = "bob"), content_text = "",
                                summary = "", content_html = "", attachments = emptyList()))),
                mockSyndFeed(author = mockSyndPerson(name = "bob"),
                        entries = listOf(mockSyndEntry(uri = "id"))).asFeed()
        )
    }

    @Test
    fun itemSummary() {
        val rand = Random()
        var expectedSummary = ""
        while (expectedSummary.length < 200) {
            expectedSummary += "${rand.nextInt(10)}"
        }
        val longText = "$expectedSummary and some additional text"

        assertEquals(
                Item(id = "id", content_text = longText, summary = expectedSummary,
                        content_html = longText, attachments = emptyList()),
                mockSyndEntry(uri = "id", description = mockSyndContent(value = longText)).asItem()
        )
    }

    @Test
    fun itemShortTextShouldNotBeIndexOutOfBounds() {
        assertEquals(
                Item(id = "id", content_text = "abc", summary = "abc",
                        content_html = "abc", attachments = emptyList()),
                mockSyndEntry(uri = "id", description = mockSyndContent(value = "abc")).asItem()
        )
    }

    @Test
    fun itemLinkButNoLinks() {
        assertEquals(
                Item(id = "id", content_text = "", summary = "",
                        content_html = "", attachments = emptyList(), url = "abc"),
                mockSyndEntry(uri = "id", description = mockSyndContent(value = ""),
                        link = "abc").asItem()
        )
    }

    @Test
    fun itemLinks() {
        assertEquals(
                Item(id = "id", content_text = "", summary = "",
                        content_html = "", attachments = emptyList(), url = "abc"),
                mockSyndEntry(uri = "id", description = mockSyndContent(value = ""),
                        links = listOf(mockSyndLink(href = "abc", rel = "self"),
                                mockSyndLink(href = "bcd"))).asItem()
        )
    }

    @Test
    fun asAttachment() {
        assertEquals(
                Attachment(url = "uurl", mime_type = "text/html", size_in_bytes = 5),
                mockSyndEnclosure(url = "uurl", type = "text/html", length = 5).asAttachment()
        )
    }

    @Test
    fun contentTextWithPlainAndOthers() {
        assertEquals(
                Item(id = "id", content_text = "PLAIN", summary = "PLAIN",
                        content_html = "<b>html</b>", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        contents = listOf(
                                mockSyndContent(value = "PLAIN", type = "text"),
                                mockSyndContent(value = "<b>html</b>", type = "html"),
                                mockSyndContent(value = null, type = "xhtml"),
                                mockSyndContent(value = "bah", type = null)
                        )).asItem()
        )
    }

    @Test
    fun contentTextWithNullAndOthers() {
        assertEquals(
                Item(id = "id", content_text = "bah", summary = "bah",
                        content_html = "<b>html</b>", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        contents = listOf(
                                mockSyndContent(value = "<b>html</b>", type = "html"),
                                mockSyndContent(value = null, type = "xhtml"),
                                mockSyndContent(value = "bah", type = null)
                        )).asItem()
        )
    }

    @Test
    fun contentTextWithOthers() {
        assertEquals(
                Item(id = "id", content_text = "**html**", summary = "**html**",
                        content_html = "<b>html</b>", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        contents = listOf(
                                mockSyndContent(value = "<b>html</b>", type = "html"),
                                mockSyndContent(value = null, type = "xhtml")
                        )).asItem()
        )
    }

    @Test
    fun contentHtmlAtomWithOnlyUnknown() {
        assertEquals(
                Item(id = "id", content_text = "foo", summary = "foo",
                        content_html = "foo", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        contents = listOf(
                                mockSyndContent(value = "foo")
                        )).asItem()
        )
    }

    @Test
    fun contentHtmlRss() {
        assertEquals(
                Item(id = "id", content_text = "**html**", summary = "**html**",
                        content_html = "<b>html</b>", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        description = mockSyndContent(value = "<b>html</b>")
                        ).asItem()
        )
    }

    @Test
    fun thumbnailWithThumbnail() {
        assertEquals(
                Item(id = "id", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        image = "img"),
                mockSyndEntry(uri = "id",
                        thumbnails = arrayOf(mockThumbnail(url = URI.create("img")))
                ).asItem()
        )
    }

    @Test
    fun thumbnailWithContent() {
        assertEquals(
                Item(id = "id", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        image = "img"),
                mockSyndEntry(uri = "id",
                        mediaContents = arrayOf(mockMediaContent(url = "img", medium = "image"))
                ).asItem()
        )
    }

    @Test
    fun publishedRFC3339Date() {
        // Need to convert it so timezone is correct for test
        val romeDate = DateTime.parse("2017-11-15T22:36:36+00:00").toDate()
        val dateTime = DateTime(romeDate.time)
        assertEquals(
                Item(id = "id", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        date_published = dateTime.toDateTimeISO().toString()),
                mockSyndEntry(uri = "id",
                        publishedDate = romeDate
                ).asItem()
        )
    }

    @Test
    fun publishedRFC3339DateFallsBackToModified() {
        // Need to convert it so timezone is correct for test
        val romeDate = DateTime.parse("2017-11-15T22:36:36+00:00").toDate()
        val dateTime = DateTime(romeDate.time)
        assertEquals(
                Item(id = "id", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        date_modified = dateTime.toDateTimeISO().toString(),
                        date_published = dateTime.toDateTimeISO().toString()),
                mockSyndEntry(uri = "id",
                        updatedDate = romeDate
                ).asItem()
        )
    }

    @Test
    fun modifiedRFC3339Date() {
        // Need to convert it so timezone is correct for test
        val romePubDate = DateTime.parse("2017-11-15T22:36:36+00:00").toDate()
        val romeModDate = DateTime.parse("2017-11-10T22:36:36+00:00").toDate()
        val pubDate = DateTime(romePubDate.time)
        val modDate = DateTime(romeModDate.time)
        assertEquals(
                Item(id = "id", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        date_modified = modDate.toDateTimeISO().toString(),
                        date_published = pubDate.toDateTimeISO().toString()),
                mockSyndEntry(uri = "id",
                        updatedDate = romeModDate,
                        publishedDate = romePubDate
                ).asItem()
        )
    }

    private fun mockSyndPerson(name: String? = null,
                               uri: String? = null,
                               email: String? = null): SyndPerson {
        val mock = mock(SyndPerson::class.java)

        `when`(mock.uri).thenReturn(uri)
        `when`(mock.name).thenReturn(name)
        `when`(mock.email).thenReturn(email)

        return mock
    }

    private fun mockSyndFeed(link: String? = null,
                             author: SyndPerson? = null,
                             links: List<SyndLink> = emptyList(),
                             entries: List<SyndEntry>? = null): SyndFeed {
        val mock = mock(SyndFeed::class.java)

        `when`(mock.link).thenReturn(link)
        `when`(mock.links).thenReturn(links)
        if (author != null) {
            `when`(mock.authors).thenReturn(listOf(author))
        }
        if (entries != null) {
            `when`(mock.entries).thenReturn(entries)
        }

        return mock
    }

    private fun mockSyndLink(href: String, rel: String? = null, type: String? = null): SyndLink {
        val mock = mock(SyndLink::class.java)

        `when`(mock.href).thenReturn(href)
        `when`(mock.rel).thenReturn(rel)
        `when`(mock.type).thenReturn(type)

        return mock
    }

    private fun mockSyndEntry(uri: String?, author: SyndPerson? = null,
                              description: SyndContent? = null,
                              link: String? = null,
                              links: List<SyndLink>? = null,
                              publishedDate: Date? = null,
                              updatedDate: Date? = null,
                              contents: List<SyndContent>? = null,
                              thumbnails: Array<Thumbnail>? = null,
                              mediaContents: Array<MediaContent>? = null): SyndEntry {
        val mock = mock(SyndEntry::class.java)

        `when`(mock.uri).thenReturn(uri)
        if (author != null) {
            `when`(mock.authors).thenReturn(listOf(author))
        }
        `when`(mock.description).thenReturn(description)
        `when`(mock.link).thenReturn(link)
        `when`(mock.links).thenReturn(links)
        `when`(mock.contents).thenReturn(contents)
        `when`(mock.publishedDate).thenReturn(publishedDate)
        `when`(mock.updatedDate).thenReturn(updatedDate)

        val mockMedia = mock(MediaEntryModule::class.java)
        val mockMetadata = mock(com.rometools.modules.mediarss.types.Metadata::class.java)

        `when`(mock.getModule(MediaModule.URI)).thenReturn(mockMedia)
        `when`(mockMedia.metadata).thenReturn(mockMetadata)
        `when`(mockMedia.mediaContents).thenReturn(mediaContents)
        `when`(mockMetadata.thumbnail).thenReturn(thumbnails)

        return mock
    }

    private fun mockSyndContent(value: String? = null, type: String? = null): SyndContent {
        val mock = mock(SyndContent::class.java)

        `when`(mock.value).thenReturn(value)
        `when`(mock.type).thenReturn(type)

        return mock
    }

    private fun mockSyndEnclosure(url: String? = null,
                                  length: Long? = 0,
                                  type: String? = null): SyndEnclosure {
        val mock = mock(SyndEnclosure::class.java)

        `when`(mock.url).thenReturn(url)
        `when`(mock.length).thenReturn(length)
        `when`(mock.type).thenReturn(type)

        return mock
    }

    private fun mockThumbnail(url: URI? = null): Thumbnail {
        val mock = mock(Thumbnail::class.java)

        `when`(mock.url).thenReturn(url)

        return mock
    }

    private fun mockMediaContent(url: String? = null, medium: String? = null): MediaContent {
        val mock = mock(MediaContent::class.java)
        var mockRef: Reference? = null

        if (url != null) {
            mockRef = mock(Reference::class.java)
            `when`(mockRef.toString()).thenReturn(url)
        }

        `when`(mock.reference).thenReturn(mockRef)
        `when`(mock.medium).thenReturn(medium)

        return mock
    }
}
