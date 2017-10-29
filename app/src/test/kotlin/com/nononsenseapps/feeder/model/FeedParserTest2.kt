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
    @Ignore
    fun relativeLinksAreMadeAbsoluteAtom() {

        val feed = FeedParser.parseFeed(atomRelative.byteInputStream())
        assertNotNull(feed)

        assertEquals("http://cowboyprogrammer.org/feed.atom", FeedParser.selfLink(feed))
    }

    @Test
    @Throws(Exception::class)
    @Ignore
    fun relativeLinksAreMadeAbsoluteAtomNoBase() {

        val feed = FeedParser.parseFeed(atomRelativeNoBase.byteInputStream())
        assertNotNull(feed)

        assertEquals("http://cowboyprogrammer.org/feed.atom", FeedParser.selfLink(feed))
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
