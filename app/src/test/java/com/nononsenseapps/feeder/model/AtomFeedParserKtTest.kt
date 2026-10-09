package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.model.gofeed.FeederGoItem
import com.nononsenseapps.feeder.model.gofeed.GoEnclosure
import com.nononsenseapps.feeder.model.gofeed.makeGoItem
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Unit tests for xml:base resolution in Atom feed parsing (issue #883).
 *
 * These tests cover the four required scenarios:
 *  1. Feed-level xml:base with relative entry links.
 *  2. Nested entry-level xml:base (relative, resolved against the feed base).
 *  3. Element-level xml:base on a <link> element within an entry.
 *  4. Absolute links are unchanged.
 *
 * The tests operate at two levels:
 *  - [extractAtomXmlBases]: parses xml:base attributes from raw XML fixtures.
 *  - [FeederGoItem]: applies the extracted bases when resolving URLs.
 */
class AtomFeedParserKtTest {
    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun loadFixture(name: String): ByteArray =
        javaClass
            .getResourceAsStream("/com/nononsenseapps/feeder/model/$name")!!
            .use { it.readBytes() }

    // -------------------------------------------------------------------------
    // 1. Feed-level xml:base with relative entry links
    // -------------------------------------------------------------------------

    @Test
    fun feedLevelXmlBaseIsDetected() {
        val feedUrl = URL("http://proxy.example.com/feeds/blog.atom")
        val body = loadFixture("atom_xml_base_feed_level.xml")

        val xmlBases = extractAtomXmlBases(body, feedUrl)

        assertEquals("https://blog.example.com/", xmlBases.feedBase.toString())
    }

    @Test
    fun feedLevelXmlBaseYieldsOneEntryBasePerEntry() {
        val feedUrl = URL("http://proxy.example.com/feeds/blog.atom")
        val body = loadFixture("atom_xml_base_feed_level.xml")

        val xmlBases = extractAtomXmlBases(body, feedUrl)

        assertEquals(3, xmlBases.entries.size)
        xmlBases.entries.forEach { entry ->
            assertEquals("https://blog.example.com/", entry.base.toString())
        }
    }

    @Test
    fun feederGoItemResolvesRelativeLinkAgainstFeedLevelXmlBase() {
        val feedUrl = URL("http://proxy.example.com/feeds/blog.atom")
        val body = loadFixture("atom_xml_base_feed_level.xml")
        val xmlBases = extractAtomXmlBases(body, feedUrl)

        val entryInfo = xmlBases.entries[0]
        val item =
            FeederGoItem(
                goItem = makeGoItem(link = "/posts/hello-world"),
                feedAuthor = null,
                feedBaseUrl = feedUrl,
                atomEntryInfo = entryInfo,
            )

        assertEquals("https://blog.example.com/posts/hello-world", item.link)
    }

    /** Regression baseline: without the fix the proxy URL is used instead of the canonical base. */
    @Test
    fun feederGoItemWithoutXmlBaseInfoResolvesAgainstProxyUrl() {
        val feedUrl = URL("http://proxy.example.com/feeds/blog.atom")

        val item =
            FeederGoItem(
                goItem = makeGoItem(link = "/posts/hello-world"),
                feedAuthor = null,
                feedBaseUrl = feedUrl,
                atomEntryInfo = null,
            )

        // Without xml:base info the raw link is resolved against the proxy fetch URL – wrong.
        assertEquals("http://proxy.example.com/posts/hello-world", item.link)
    }

    // -------------------------------------------------------------------------
    // 2. Nested entry-level xml:base (relative, resolved against feed base)
    // -------------------------------------------------------------------------

    @Test
    fun relativeEntryLevelXmlBaseIsResolvedAgainstFeedBase() {
        val feedUrl = URL("http://proxy.example.com/feeds/blog.atom")
        val body = loadFixture("atom_xml_base_entry_level.xml")

        val xmlBases = extractAtomXmlBases(body, feedUrl)

        assertEquals("https://blog.example.com/", xmlBases.feedBase.toString())
        assertEquals(2, xmlBases.entries.size)
        // Entry 0: xml:base="posts/" relative to feed base → https://blog.example.com/posts/
        assertEquals("https://blog.example.com/posts/", xmlBases.entries[0].base.toString())
        // Entry 1: xml:base="https://other.example.com/articles/" (absolute override)
        assertEquals("https://other.example.com/articles/", xmlBases.entries[1].base.toString())
    }

    @Test
    fun feederGoItemResolvesRelativeLinkUsingNestedEntryLevelXmlBase() {
        val feedUrl = URL("http://proxy.example.com/feeds/blog.atom")
        val body = loadFixture("atom_xml_base_entry_level.xml")
        val xmlBases = extractAtomXmlBases(body, feedUrl)

        // Entry 0: base = https://blog.example.com/posts/, link href = "1"
        val item =
            FeederGoItem(
                goItem = makeGoItem(link = "1"),
                feedAuthor = null,
                feedBaseUrl = feedUrl,
                atomEntryInfo = xmlBases.entries[0],
            )
        assertEquals("https://blog.example.com/posts/1", item.link)
    }

    @Test
    fun feederGoItemResolvesRelativeLinkUsingAbsoluteEntryLevelXmlBaseOverride() {
        val feedUrl = URL("http://proxy.example.com/feeds/blog.atom")
        val body = loadFixture("atom_xml_base_entry_level.xml")
        val xmlBases = extractAtomXmlBases(body, feedUrl)

        // Entry 1: base = https://other.example.com/articles/, link href = "my-article"
        val item =
            FeederGoItem(
                goItem = makeGoItem(link = "my-article"),
                feedAuthor = null,
                feedBaseUrl = feedUrl,
                atomEntryInfo = xmlBases.entries[1],
            )
        assertEquals("https://other.example.com/articles/my-article", item.link)
    }

    // -------------------------------------------------------------------------
    // 3. Element-level xml:base on a <link> element
    // -------------------------------------------------------------------------

    @Test
    fun saxExtractorCapturesResolvedHrefWhenLinkHasItsOwnXmlBase() {
        val feedUrl = URL("http://proxy.example.com/feed.atom")
        val body = loadFixture("atom_xml_base_element_level.xml")

        val xmlBases = extractAtomXmlBases(body, feedUrl)

        assertEquals(1, xmlBases.entries.size)
        // The SAX handler resolves the href against the link element's own xml:base.
        assertNotNull(xmlBases.entries[0].resolvedAlternateHref)
        assertEquals(
            "https://canonical.example.com/articles/first-post",
            xmlBases.entries[0].resolvedAlternateHref,
        )
    }

    @Test
    fun feederGoItemUsesResolvedHrefToHonourElementLevelXmlBaseOnLink() {
        val feedUrl = URL("http://proxy.example.com/feed.atom")
        val body = loadFixture("atom_xml_base_element_level.xml")
        val xmlBases = extractAtomXmlBases(body, feedUrl)

        // The Go parser returns the raw href ("first-post"); the SAX-resolved href in
        // atomEntryInfo takes precedence and gives the correct absolute URL.
        val item =
            FeederGoItem(
                goItem = makeGoItem(link = "first-post"),
                feedAuthor = null,
                feedBaseUrl = feedUrl,
                atomEntryInfo = xmlBases.entries[0],
            )

        assertEquals("https://canonical.example.com/articles/first-post", item.link)
    }

    /** Regression baseline: without the fix the proxy URL is used for the element-level case. */
    @Test
    fun feederGoItemWithoutXmlBaseInfoUsesProxyUrlForElementLevelCase() {
        val feedUrl = URL("http://proxy.example.com/feed.atom")

        val item =
            FeederGoItem(
                goItem = makeGoItem(link = "first-post"),
                feedAuthor = null,
                feedBaseUrl = feedUrl,
                atomEntryInfo = null,
            )

        assertEquals("http://proxy.example.com/first-post", item.link)
    }

    // -------------------------------------------------------------------------
    // 4. Absolute links are unchanged regardless of xml:base
    // -------------------------------------------------------------------------

    @Test
    fun absoluteEntryLinkIsReturnedUnchangedRegardlessOfFeedXmlBase() {
        val feedUrl = URL("http://proxy.example.com/feeds/blog.atom")
        val body = loadFixture("atom_xml_base_feed_level.xml")
        val xmlBases = extractAtomXmlBases(body, feedUrl)

        // Entry 2 in the fixture has an absolute href; resolving it against feedBase is a no-op.
        assertEquals(
            "https://other.example.com/external",
            xmlBases.entries[2].resolvedAlternateHref,
        )
    }

    @Test
    fun feederGoItemAbsoluteLinkIsUnchangedWhenFeedLevelXmlBaseIsSet() {
        val feedUrl = URL("http://proxy.example.com/feeds/blog.atom")
        val xmlBases =
            AtomXmlBases(
                feedBase = URL("https://blog.example.com/"),
                entries =
                    listOf(
                        AtomEntryInfo(
                            base = URL("https://blog.example.com/"),
                            resolvedAlternateHref = null,
                        ),
                    ),
            )

        val item =
            FeederGoItem(
                goItem = makeGoItem(link = "https://other.example.com/external"),
                feedAuthor = null,
                feedBaseUrl = feedUrl,
                atomEntryInfo = xmlBases.entries[0],
            )

        assertEquals("https://other.example.com/external", item.link)
    }

    // -------------------------------------------------------------------------
    // Enclosure URL resolution via effective base
    // -------------------------------------------------------------------------

    @Test
    fun feederGoItemResolvesEnclosureUrlAgainstEffectiveEntryBase() {
        val feedUrl = URL("http://proxy.example.com/feeds/podcast.atom")
        val entryBase = URL("https://podcast.example.com/episodes/")
        val entryInfo = AtomEntryInfo(base = entryBase, resolvedAlternateHref = null)

        val item =
            FeederGoItem(
                goItem =
                    makeGoItem(
                        link = "https://podcast.example.com/episodes/1",
                        enclosures =
                            listOf(
                                GoEnclosure(
                                    url = "cover.jpg",
                                    length = "99999",
                                    type = "image/jpeg",
                                ),
                            ),
                    ),
                feedAuthor = null,
                feedBaseUrl = feedUrl,
                atomEntryInfo = entryInfo,
            )

        // The image-enclosure URL "cover.jpg" is relative; feedThumbnail resolves it with
        // effectiveBaseUrl = https://podcast.example.com/episodes/.
        assertNotNull(item.feedThumbnail)
        assertEquals(
            "https://podcast.example.com/episodes/cover.jpg",
            item.feedThumbnail!!.url,
        )
    }

    // -------------------------------------------------------------------------
    // No xml:base declared: behaviour must be identical to pre-fix
    // -------------------------------------------------------------------------

    @Test
    fun noXmlBasePresentUsesFeedUrlAsBase() {
        val feedUrl = URL("https://lineageos.org/feed.xml")
        val xml =
            """<?xml version='1.0' encoding='UTF-8'?>
<feed xmlns='http://www.w3.org/2005/Atom'>
  <id>https://lineageos.org/</id>
  <title>No Base Feed</title>
  <updated>2024-01-01T00:00:00Z</updated>
  <entry>
    <id>/entry/1</id>
    <title>Entry 1</title>
    <link href='/entry/1'/>
    <updated>2024-01-01T00:00:00Z</updated>
  </entry>
</feed>""".toByteArray()

        val xmlBases = extractAtomXmlBases(xml, feedUrl)

        assertEquals(feedUrl.toString(), xmlBases.feedBase.toString())
        assertEquals(1, xmlBases.entries.size)
        assertEquals(feedUrl.toString(), xmlBases.entries[0].base.toString())
        // SAX-resolved href uses feedUrl as the base for the relative path.
        assertEquals("https://lineageos.org/entry/1", xmlBases.entries[0].resolvedAlternateHref)
    }

    // -------------------------------------------------------------------------
    // Robustness: malformed / non-Atom input must not throw
    // -------------------------------------------------------------------------

    @Test
    fun brokenXmlFallsBackToFeedUrlWithoutThrowing() {
        val feedUrl = URL("https://example.com/feed.xml")
        val brokenXml = "<<<not valid XML at all>>>".toByteArray()

        val xmlBases = extractAtomXmlBases(brokenXml, feedUrl)

        assertEquals(feedUrl.toString(), xmlBases.feedBase.toString())
        assertEquals(0, xmlBases.entries.size)
    }

    @Test
    fun rssInputFallsBackToFeedUrlWithoutThrowing() {
        val feedUrl = URL("https://example.com/rss.xml")
        val rssXml =
            """<?xml version='1.0' encoding='UTF-8'?>
<rss version="2.0">
  <channel>
    <title>RSS Feed</title>
    <link>https://example.com</link>
    <item><title>Item</title><link>https://example.com/item1</link></item>
  </channel>
</rss>""".toByteArray()

        val xmlBases = extractAtomXmlBases(rssXml, feedUrl)

        assertEquals(feedUrl.toString(), xmlBases.feedBase.toString())
    }

    @Test
    fun jsonInputFallsBackToFeedUrlWithoutThrowing() {
        val feedUrl = URL("https://example.com/feed.json")
        val jsonFeed = """{"version":"https://jsonfeed.org/version/1","title":"Test"}""".toByteArray()

        val xmlBases = extractAtomXmlBases(jsonFeed, feedUrl)

        assertEquals(feedUrl.toString(), xmlBases.feedBase.toString())
        assertEquals(0, xmlBases.entries.size)
    }

    // -------------------------------------------------------------------------
    // Security: XXE and entity-expansion hardening
    // -------------------------------------------------------------------------

    @Test
    fun xxeViaSYSTEMEntityDoesNotLeakLocalFileContent() {
        val secret = tempFolder.newFile("secret.txt").also { it.writeText("TOP_SECRET_9a3f") }
        val feedUrl = URL("https://example.com/feed.atom")
        val xxeXml =
            """<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE feed [<!ENTITY xxe SYSTEM "file://${secret.absolutePath}">]>
<feed xmlns='http://www.w3.org/2005/Atom' xml:base="&xxe;">
  <entry>
    <id>1</id><title>t</title>
    <link rel='alternate' href='&xxe;'/>
    <updated>2024-01-01T00:00:00Z</updated>
  </entry>
</feed>""".toByteArray()

        // Must not throw; file content must never appear in any resolved URL.
        val xmlBases = extractAtomXmlBases(xxeXml, feedUrl)

        assertFalse(
            xmlBases.feedBase.toString().contains("TOP_SECRET_9a3f"),
            "File content leaked into feedBase",
        )
        xmlBases.entries.forEach { entry ->
            assertFalse(
                entry.base.toString().contains("TOP_SECRET_9a3f"),
                "File content leaked into entry base",
            )
            assertFalse(
                entry.resolvedAlternateHref?.contains("TOP_SECRET_9a3f") == true,
                "File content leaked into resolvedAlternateHref",
            )
        }
    }

    @Test
    fun billionLaughsInternalEntityExpansionFallsBackToFeedUrl() {
        val feedUrl = URL("https://example.com/feed.atom")
        // Classic billion-laughs: exponential entity expansion via internal references.
        // With disallow-doctype-decl=true the parser throws immediately on the DOCTYPE
        // line, so this returns promptly rather than hanging or OOMing.
        val billionLaughs =
            """<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE lolz [
  <!ENTITY lol  "lol">
  <!ENTITY lol2 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
  <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
  <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
  <!ENTITY lol5 "&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;">
  <!ENTITY lol6 "&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;">
  <!ENTITY lol7 "&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;">
  <!ENTITY lol8 "&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;">
  <!ENTITY lol9 "&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;">
]>
<feed xmlns='http://www.w3.org/2005/Atom' xml:base="&lol9;"></feed>""".toByteArray()

        val xmlBases = extractAtomXmlBases(billionLaughs, feedUrl)

        assertEquals(feedUrl.toString(), xmlBases.feedBase.toString())
        assertEquals(0, xmlBases.entries.size)
    }
}
