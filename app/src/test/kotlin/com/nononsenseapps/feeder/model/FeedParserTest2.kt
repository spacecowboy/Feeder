package com.nononsenseapps.feeder.model

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FeedParserTest2 {
    @Rule
    @JvmField
    var tempFolder = TemporaryFolder()

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
