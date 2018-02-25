package com.nononsenseapps.feeder.model

import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FeedParserTest2 {
    @Rule
    @JvmField
    var tempFolder = TemporaryFolder()

    @Test
    @Throws(Exception::class)
    fun getAlternateLinksHandlesYoutube() {
        // I want this to be an Online test to make sure that I notice if/when Youtube changes something which breaks it
        val feeds: List<Pair<String, String>> =
                FeedParser.getAlternateFeedLinksAtUrl(URL("https://www.youtube.com/watch?v=-m5I_5Vnh6A"))
        assertEquals(listOf("https://www.youtube.com/feeds/videos.xml?channel_id=UCG1h-Wqjtwz7uUANw6gazRw" to "atom"),
                feeds)
    }

    @Test
    @Throws(Exception::class)
    fun htmlAtomContentGetsUnescaped() {
        javaClass.getResourceAsStream("atom_hnapp.xml")
                .use {
                    val feed = FeedParser.parseFeedInputStream(it)

                    val item = feed.items!![0]
                    assertEquals("37 – Spectre Mitigations in Microsoft's C/C++ Compiler",
                            item.title)
                    assertEquals("37 points, 1 comment",
                            item.content_text)
                    assertEquals("<p>37 points, <a href=\"https://news.ycombinator.com/item?id=16381978\">1 comment</a></p>",
                            item.content_html)
                }
    }

    @Test
    @Throws(Exception::class)
    fun enclosedImageIsUsedAsThumbnail() {
        javaClass.getResourceAsStream("rss_lemonde.xml")
                .use {
                    val feed = FeedParser.parseFeedInputStream(it)

                    val item = feed.items!![0]
                    assertEquals("http://s1.lemde.fr/image/2018/02/11/644x322/5255112_3_a8dc_martin-fourcade_02be61d126b2da39d977b2e1902c819a.jpg",
                            item.image)
                }
    }

    @Test
    @Throws(Exception::class)
    fun getAlternateFeedLinksDoesNotReturnRelativeLinks() {
        javaClass.getResourceAsStream("fz.html")
                .bufferedReader()
                .use {
                    val alts: List<Pair<String, String>> = FeedParser.getAlternateFeedLinksInHtml(it.readText())
                    assertEquals(emptyList<Pair<String, String>>(), alts)
                }
    }

    @Test
    @Throws(Exception::class)
    fun getAlternateFeedLinksResolvesRelativeLinksGivenBaseUrl() {
        javaClass.getResourceAsStream("fz.html")
                .bufferedReader()
                .use {
                    val alts: List<Pair<String, String>> =
                            FeedParser.getAlternateFeedLinksInHtml(it.readText(),
                                    baseUrl = URL("https://www.fz.se/index.html"))
                    assertEquals(listOf(
                            "https://www.fz.se/feeds/nyheter" to "application/rss+xml",
                            "https://www.fz.se/feeds/forum" to "application/rss+xml"
                    ), alts)
                }
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksReturnsNullIfNoLink() {
        val rssLink = FeedParser.findFeedUrl(atomRelative)
        assertNull(rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksReturnsNullForFeedsWithAlternateLinks() {
        val rssLink = FeedParser.findFeedUrl(atomWithAlternateLinks)
        assertNull(rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksPrefersAtomByDefault() {
        val rssLink = FeedParser.findFeedUrl(getCowboyHtml())
        assertEquals(URL("https://cowboyprogrammer.org/atom.xml"), rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksPreferAtom() {
        val rssLink = FeedParser.findFeedUrl(getCowboyHtml(), preferAtom = true)
        assertEquals(URL("https://cowboyprogrammer.org/atom.xml"), rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksPreferRss() {
        val rssLink = FeedParser.findFeedUrl(getCowboyHtml(), preferRss = true)
        assertEquals(URL("https://cowboyprogrammer.org/index.xml"), rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksPreferJSON() {
        val rssLink = FeedParser.findFeedUrl(getCowboyHtml(), preferJSON = true)
        assertEquals(URL("https://cowboyprogrammer.org/feed.json"), rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun encodingIsHandledInAtomRss() {
        val responseBody: ResponseBody = ResponseBody.create(MediaType.parse("application/xml"), getGolemDe())

        val response: Response = Response.Builder()
                .body(responseBody)
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("Test")
                .request(Request.Builder()
                        .url("https://rss.golem.de/rss.php?feed=RSS2.0")
                        .build())
                .build()

        val feed = FeedParser.parseFeedResponse(response)

        assertEquals(true, feed.items?.get(0)?.content_text?.contains("größte"))
    }

    @Test
    @Throws(Exception::class)
    fun correctAlternateLinkInAtomIsUsedForUrl() {
        val feed = FeedParser.parseRssAtomBytes(getAtomUtdelningsSeglaren())

        assertEquals("http://utdelningsseglaren.blogspot.com/2017/12/tips-pa-6-podcasts.html",
                feed.items?.get(0)?.url)
    }

    @Test
    @Throws(Exception::class)
    @Ignore
    fun relativeLinksAreMadeAbsoluteAtom() {

        val feed = FeedParser.parseFeedInputStream(atomRelative.byteInputStream())
        assertNotNull(feed)

        assertEquals("http://cowboyprogrammer.org/feed.atom", feed.feed_url)
    }

    @Test
    @Throws(Exception::class)
    @Ignore
    fun relativeLinksAreMadeAbsoluteAtomNoBase() {

        val feed = FeedParser.parseFeedInputStream(atomRelativeNoBase.byteInputStream())
        assertNotNull(feed)

        assertEquals("http://cowboyprogrammer.org/feed.atom", feed.feed_url)
    }

    private fun getCowboyHtml(): String =
            javaClass.getResourceAsStream("cowboyprogrammer.html")
                    .bufferedReader()
                    .use {
                        it.readText()
                    }

    private fun getGolemDe(): ByteArray =
            javaClass.getResourceAsStream("golem-de.xml").readBytes()

    private fun getAtomUtdelningsSeglaren(): ByteArray =
            javaClass.getResourceAsStream("atom_utdelningsseglaren.xml").readBytes()
}

val atomRelative = """
<?xml version='1.0' encoding='UTF-8'?>
<feed xmlns='http://www.w3.org/2005/Atom' xml:base='http://cowboyprogrammer.org'>
  <id>http://cowboyprogrammer.org</id>
  <title>Relative links</title>
  <updated>2003-12-13T18:30:02Z</updated>
  <link rel="self" href="/feed.atom"/>
</feed>
"""

val atomRelativeNoBase = """
<?xml version='1.0' encoding='UTF-8'?>
<feed xmlns='http://www.w3.org/2005/Atom'>
  <id>http://cowboyprogrammer.org</id>
  <title>Relative links</title>
  <updated>2003-12-13T18:30:02Z</updated>
  <link rel="self" href="/feed.atom"/>
</feed>
"""

val atomWithAlternateLinks = """
<?xml version='1.0' encoding='UTF-8'?>
<feed xmlns='http://www.w3.org/2005/Atom'>
  <id>http://cowboyprogrammer.org</id>
  <title>Relative links</title>
  <updated>2003-12-13T18:30:02Z</updated>
  <link rel="self" href="/feed.atom"/>
  <link rel="alternate" type="text/html" href="http://localhost:1313/" />
  <link rel="alternate" type="application/rss" href="http://localhost:1313/index.xml" />
  <link rel="alternate" type="application/json" href="http://localhost:1313/feed.json" />
</feed>
"""
