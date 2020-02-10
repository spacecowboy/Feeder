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
import com.rometools.rome.feed.synd.*
import kotlinx.coroutines.FlowPreview
import org.joda.time.DateTime
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.net.URI
import java.net.URL
import java.util.*
import kotlin.test.assertEquals

@FlowPreview
class RomeExtensionsKtTest {
    @Test
    fun author() {
        assertEquals(Author(name = "Bobby Jane"),
                mockSyndPerson(name = "Bobby Jane").asAuthor())
    }

    @Test
    fun feedLinkButNoLinks() {
        assertEquals(Feed(home_page_url = "$baseUrl/homepage", title = "", items = emptyList()),
                mockSyndFeed(link = "homepage").asFeed(baseUrl))
    }

    @Test
    fun feedLinks() {
        assertEquals(Feed(home_page_url = "$baseUrl/homepage", title = "", items = emptyList()),
                mockSyndFeed(links = listOf(mockSyndLink(href = "homepage",
                        rel = "alternate", type = "text/html"))).asFeed(baseUrl))
    }

    @Test
    fun itemFallsBackToFeedAuthor() {
        assertEquals(
                Feed(author = Author(name = "bob"), title = "",
                        items = listOf(Item(id = "$baseUrl/id", author = Author(name = "bob"), content_text = "",
                                url = "$baseUrl/id",
                                summary = "", title = "", content_html = "", attachments = emptyList()))),
                mockSyndFeed(author = mockSyndPerson(name = "bob"),
                        entries = listOf(mockSyndEntry(uri = "id"))).asFeed(baseUrl)
        )
    }

    // Essentially a test for XKCD
    @Test
    fun descriptionWithOnlyImageDoesNotReturnBlankSummaryAndGetsImageSet() {
        var expectedSummary = "[An image]"
        val html = "  <img src='http://google.com/image.png' alt='An image'/> "

        assertEquals(
                Item(id = "$baseUrl/id", title = "", content_text = expectedSummary, summary = expectedSummary,
                        url = "$baseUrl/id",
                        content_html = html, image = "http://google.com/image.png", attachments = emptyList()),
                mockSyndEntry(uri = "id", description = mockSyndContent(value = html)).asItem(baseUrl)
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
                Item(id = "$baseUrl/id", title = "", content_text = longText, summary = expectedSummary,
                        url = "$baseUrl/id",
                        content_html = longText, attachments = emptyList()),
                mockSyndEntry(uri = "id", description = mockSyndContent(value = longText)).asItem(baseUrl)
        )
    }

    @Test
    fun itemShortTextShouldNotBeIndexOutOfBounds() {
        assertEquals(
                Item(id = "$baseUrl/id", content_text = "abc", summary = "abc", title = "",
                        url = "$baseUrl/id",
                        content_html = "abc", attachments = emptyList()),
                mockSyndEntry(uri = "id", description = mockSyndContent(value = "abc")).asItem(baseUrl)
        )
    }

    @Test
    fun itemLinkButNoLinks() {
        assertEquals(
                Item(id = "$baseUrl/id", content_text = "", summary = "", title = "",
                        content_html = "", attachments = emptyList(), url = "$baseUrl/abc"),
                mockSyndEntry(uri = "id", description = mockSyndContent(value = ""),
                        link = "abc").asItem(baseUrl)
        )
    }

    @Test
    fun itemLinks() {
        assertEquals(
                Item(id = "$baseUrl/id", content_text = "", summary = "", title = "",
                        content_html = "", attachments = emptyList(), url = "$baseUrl/abc"),
                mockSyndEntry(uri = "id", description = mockSyndContent(value = ""),
                        links = listOf(mockSyndLink(href = "abc", rel = "self"),
                                mockSyndLink(href = "bcd"))).asItem(baseUrl)
        )
    }

    @Test
    fun asAttachment() {
        assertEquals(
                Attachment(url = "$baseUrl/uurl", mime_type = "text/html", size_in_bytes = 5),
                mockSyndEnclosure(url = "uurl", type = "text/html", length = 5).asAttachment(baseUrl)
        )
    }

    @Test
    fun contentTextWithPlainAndOthers() {
        assertEquals(
                Item(id = "$baseUrl/id", content_text = "PLAIN", summary = "PLAIN", title = "",
                        url = "$baseUrl/id",
                        content_html = "<b>html</b>", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        contents = listOf(
                                mockSyndContent(value = "PLAIN", type = "text"),
                                mockSyndContent(value = "<b>html</b>", type = "html"),
                                mockSyndContent(value = null, type = "xhtml"),
                                mockSyndContent(value = "bah", type = null)
                        )).asItem(baseUrl)
        )
    }

    @Test
    fun contentTextWithNullAndOthers() {
        assertEquals(
                Item(id = "$baseUrl/id", content_text = "bah", summary = "bah", title = "",
                        url = "$baseUrl/id",
                        content_html = "<b>html</b>", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        contents = listOf(
                                mockSyndContent(value = "<b>html</b>", type = "html"),
                                mockSyndContent(value = null, type = "xhtml"),
                                mockSyndContent(value = "bah", type = null)
                        )).asItem(baseUrl)
        )
    }

    @Test
    fun contentTextWithOthers() {
        assertEquals(
                Item(id = "$baseUrl/id", content_text = "html", summary = "html", title = "",
                        url = "$baseUrl/id",
                        content_html = "<b>html</b>", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        contents = listOf(
                                mockSyndContent(value = "<b>html</b>", type = "html"),
                                mockSyndContent(value = null, type = "xhtml")
                        )).asItem(baseUrl)
        )
    }

    @Test
    fun contentHtmlAtomWithOnlyUnknown() {
        assertEquals(
                Item(id = "$baseUrl/id", title = "", content_text = "foo", summary = "foo",
                        url = "$baseUrl/id",
                        content_html = "foo", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        contents = listOf(
                                mockSyndContent(value = "foo")
                        )).asItem(baseUrl)
        )
    }

    @Test
    fun titleHtmlAtom() {
        assertEquals(
                Item(id = "$baseUrl/id", title = "600 – Email is your electronic memory", content_text = "", summary = "",
                        url = "$baseUrl/id",
                        content_html = "", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        titleEx = mockSyndContent(value = "600 &amp;#8211; Email is your electronic memory", type = "html")
                ).asItem(baseUrl)
        )
    }

    @Test
    fun titleXHtmlAtom() {
        assertEquals(
                Item(id = "$baseUrl/id", title = "600 – Email is your electronic memory", content_text = "", summary = "",
                        url = "$baseUrl/id",
                        content_html = "", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        titleEx = mockSyndContent(value = "600 &#8211; Email is your electronic memory", type = "xhtml")
                ).asItem(baseUrl)
        )
    }

    @Test
    fun titlePlainAtomRss() {
        assertEquals(
                Item(id = "$baseUrl/id", title = "600 – Email is your electronic memory", content_text = "", summary = "",
                        url = "$baseUrl/id",
                        content_html = "", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        title = "600 &#8211; Email is your electronic memory"
                ).asItem(baseUrl)
        )
    }

    @Test
    fun contentHtmlRss() {
        assertEquals(
                Item(id = "$baseUrl/id", content_text = "html", summary = "html", title = "",
                        url = "$baseUrl/id",
                        content_html = "<b>html</b>", attachments = emptyList()),
                mockSyndEntry(uri = "id",
                        description = mockSyndContent(value = "<b>html</b>")
                ).asItem(baseUrl)
        )
    }

    @Test
    fun thumbnailWithThumbnail() {
        assertEquals(
                Item(id = "$baseUrl/id", title = "", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        url = "$baseUrl/id",
                        image = "$baseUrl/img"),
                mockSyndEntry(uri = "id",
                        thumbnails = arrayOf(mockThumbnail(url = URI.create("img")))
                ).asItem(baseUrl)
        )
    }

    @Test
    fun thumbnailWithContent() {
        assertEquals(
                Item(id = "$baseUrl/id", title = "", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        url = "$baseUrl/id",
                        image = "$baseUrl/img"),
                mockSyndEntry(uri = "id",
                        mediaContents = arrayOf(mockMediaContent(url = "img", medium = "image"))
                ).asItem(baseUrl)
        )
    }

    @Test
    fun thumbnailFromHtmlFlaggedContentIsUnescaped() {
        val description = mockSyndContent(
                value = """
                            <![CDATA[
						<img src="https://o.aolcdn.com/images/dims?crop=1200%2C627%2C0%2C0&amp;quality=85&amp;format=jpg&amp;resize=1600%2C836&amp;image_uri=https%3A%2F%2Fs.yimg.com%2Fos%2Fcreatr-uploaded-images%2F2019-03%2Ffa057c20-5050-11e9-bfef-d1614983d7cc&amp;client=a1acac3e1b3290917d92&amp;signature=351348aa11c53a569d5ad40f3a7ef697471b645a" />Google didn&#039;t completely scrap its robotic dreams after it sold off Boston Dynamics and shuttered the other robotic start-ups it acquired over the past decade. Now, the tech giant has given us a glimpse of how the program has changed in a blog post a...
				]]>
                        """.trimIndent(),
                type = "html")

        val item = mockSyndEntry(uri = "id",
                contents = listOf(description)
        ).asItem(baseUrl)

        assertEquals(
                "https://o.aolcdn.com/images/dims?crop=1200%2C627%2C0%2C0&quality=85&format=jpg&resize=1600%2C836&image_uri=https%3A%2F%2Fs.yimg.com%2Fos%2Fcreatr-uploaded-images%2F2019-03%2Ffa057c20-5050-11e9-bfef-d1614983d7cc&client=a1acac3e1b3290917d92&signature=351348aa11c53a569d5ad40f3a7ef697471b645a",
                item.image
        )
    }

    @Test
    fun thumbnailFromDescriptionIsUnescaped() {
        val description = mockSyndContent(
                value = """
                            <![CDATA[
						<img src="https://o.aolcdn.com/images/dims?crop=1200%2C627%2C0%2C0&amp;quality=85&amp;format=jpg&amp;resize=1600%2C836&amp;image_uri=https%3A%2F%2Fs.yimg.com%2Fos%2Fcreatr-uploaded-images%2F2019-03%2Ffa057c20-5050-11e9-bfef-d1614983d7cc&amp;client=a1acac3e1b3290917d92&amp;signature=351348aa11c53a569d5ad40f3a7ef697471b645a" />Google didn&#039;t completely scrap its robotic dreams after it sold off Boston Dynamics and shuttered the other robotic start-ups it acquired over the past decade. Now, the tech giant has given us a glimpse of how the program has changed in a blog post a...
				]]>
                        """.trimIndent(),
                type = "html")

        val item = mockSyndEntry(uri = "id",
                description = description
        ).asItem(baseUrl)

        assertEquals(
                "https://o.aolcdn.com/images/dims?crop=1200%2C627%2C0%2C0&quality=85&format=jpg&resize=1600%2C836&image_uri=https%3A%2F%2Fs.yimg.com%2Fos%2Fcreatr-uploaded-images%2F2019-03%2Ffa057c20-5050-11e9-bfef-d1614983d7cc&client=a1acac3e1b3290917d92&signature=351348aa11c53a569d5ad40f3a7ef697471b645a",
                item.image
        )
    }

    @Test
    fun publishedRFC3339Date() {
        // Need to convert it so timezone is correct for test
        val romeDate = DateTime.parse("2017-11-15T22:36:36+00:00").toDate()
        val dateTime = DateTime(romeDate.time)
        assertEquals(
                Item(id = "$baseUrl/id", title = "", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        url = "$baseUrl/id",
                        date_published = dateTime.toDateTimeISO().toString()),
                mockSyndEntry(uri = "id",
                        publishedDate = romeDate
                ).asItem(baseUrl)
        )
    }

    @Test
    fun publishedRFC3339DateFallsBackToModified() {
        // Need to convert it so timezone is correct for test
        val romeDate = DateTime.parse("2017-11-15T22:36:36+00:00").toDate()
        val dateTime = DateTime(romeDate.time)
        assertEquals(
                Item(id = "$baseUrl/id", title = "", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        url = "$baseUrl/id",
                        date_modified = dateTime.toDateTimeISO().toString(),
                        date_published = dateTime.toDateTimeISO().toString()),
                mockSyndEntry(uri = "id",
                        updatedDate = romeDate
                ).asItem(baseUrl)
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
                Item(id = "$baseUrl/id", title = "", content_html = "", content_text = "", summary = "", attachments = emptyList(),
                        url = "$baseUrl/id",
                        date_modified = modDate.toDateTimeISO().toString(),
                        date_published = pubDate.toDateTimeISO().toString()),
                mockSyndEntry(uri = "id",
                        updatedDate = romeModDate,
                        publishedDate = romePubDate
                ).asItem(baseUrl)
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
                              mediaContents: Array<MediaContent>? = null,
                              title: String? = null,
                              titleEx: SyndContent? = null): SyndEntry {
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
        `when`(mock.title).thenReturn(title)
        `when`(mock.titleEx).thenReturn(titleEx)

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

    private val baseUrl = URL("http://test.com")
}
