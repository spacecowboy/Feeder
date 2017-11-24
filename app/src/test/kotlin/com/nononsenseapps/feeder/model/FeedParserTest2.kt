package com.nononsenseapps.feeder.model

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertNull

class FeedParserTest2 {
    @Rule
    @JvmField
    var tempFolder = TemporaryFolder()

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
                                    baseUrl = "https://www.fz.se/index.html")
                    assertEquals(listOf(
                            "https://www.fz.se/feeds/nyheter" to "application/rss+xml",
                            "https://www.fz.se/feeds/forum" to "application/rss+xml"
                    ), alts)
                }
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksReturnsNullIfNoLink() {
        val rssLink = FeedParser.findFeedLink(atomRelative)
        assertNull(rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksReturnsNullForFeedsWithAlternateLinks() {
        val rssLink = FeedParser.findFeedLink(atomWithAlternateLinks)
        assertNull(rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksPrefersAtomByDefault() {
        val rssLink = FeedParser.findFeedLink(getCowboyHtml())
        assertEquals("https://cowboyprogrammer.org/atom.xml", rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksPreferAtom() {
        val rssLink = FeedParser.findFeedLink(getCowboyHtml(), preferAtom = true)
        assertEquals("https://cowboyprogrammer.org/atom.xml", rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksPreferRss() {
        val rssLink = FeedParser.findFeedLink(getCowboyHtml(), preferRss = true)
        assertEquals("https://cowboyprogrammer.org/index.xml", rssLink)
    }

    @Test
    @Throws(Exception::class)
    fun findsAlternateLinksPreferJSON() {
        val rssLink = FeedParser.findFeedLink(getCowboyHtml(), preferJSON = true)
        assertEquals("https://cowboyprogrammer.org/feed.json", rssLink)
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
