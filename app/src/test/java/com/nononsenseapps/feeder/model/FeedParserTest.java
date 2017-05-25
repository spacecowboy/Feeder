package com.nononsenseapps.feeder.model;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class FeedParserTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void noStyles() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getResearchRsc());
        assertNotNull(feed);

        assertEquals("http://research.swtch.com/feed.atom", FeedParser.selfLink(feed));

        assertEquals(17, feed.getEntries().size());

        SyndEntry entry = feed.getEntries().get(9);

        assertNull(FeedParser.thumbnail(entry));

        assertEquals("QArt Codes",
                FeedParser.plainTitle(entry));

        // Style tags should be ignored
        assertEquals("QR codes are 2-dimensional bar codes that encode arbitrary text strings. A common use of QR codes is to encode URLs so that people can scan a QR code (for example, on an advertising poster, building r",
                FeedParser.snippet(entry));
    }

    @Test
    public void feedAuthorIsUsedAsFallback() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getResearchRsc());
        assertNotNull(feed);

        assertEquals("http://research.swtch.com/feed.atom", FeedParser.selfLink(feed));

        assertEquals(17, feed.getEntries().size());

        SyndEntry entry = feed.getEntries().get(9);

        assertEquals("Russ Cox", feed.getAuthors().get(0).getName());
        assertEquals(feed.getAuthors(), entry.getAuthors());
        assertEquals(feed.getAuthors().get(0).getName(), entry.getAuthor());
    }

    @Test
    public void cyklist() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getCyklistBloggen());
        assertNotNull(feed);

        assertNull(FeedParser.selfLink(feed));

        assertEquals(10, feed.getEntries().size());

        SyndEntry entry = feed.getEntries().get(0);

        assertNull(FeedParser.thumbnail(entry));

        assertEquals("Ingen ombyggning av Danvikstull",
                FeedParser.plainTitle(entry));

        // Make sure character 160 (non-breaking space) is trimmed
        assertEquals("För mer än tre år sedan aviserade dåvarande Allians-styrda Stockholms Stad att man äntligen skulle bredda den extremt smala passagen på pendlingsstråket vid Danvikstull: I smalaste passagen är gångdel",
                FeedParser.snippet(entry));
    }

    @Test
    public void cowboy() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getCowboyRss());
        assertNotNull(feed);

        assertNull(FeedParser.selfLink(feed));

        assertEquals(15, feed.getEntries().size());

        SyndEntry entry = feed.getEntries().get(1);

        assertEquals("https://cowboyprogrammer.org/images/zopfli_all_the_things.jpg",
                FeedParser.thumbnail(entry));

        // Snippet should not contain images
        entry = feed.getEntries().get(4);
        assertEquals("Fixing the up button in Python shell history", entry.getTitle());
        assertEquals("In case your python/ipython shell doesn’t have a working history, e.g. pressing ↑ only prints some nonsensical ^[[A, then you are missing either the readline or ncurses library." +
                        " Ipython is more descri",
                FeedParser.snippet(entry));
        // Snippet should not contain links
        entry = feed.getEntries().get(1);
        assertEquals("Compress all the images!", entry.getTitle());
        assertEquals("*Update 2016-11-22: Made the Makefile compatible with BSD sed (MacOS)* One advantage that static sites, such as those built by Hugo, provide is fast loading times. Because there is no processing to be",
                FeedParser.snippet(entry));
    }

    @Test
    public void rss() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getRSS());

        assertEquals("http://cornucopia.cornubot.se/", feed.getLink());
        assertNull(FeedParser.selfLink(feed));

        assertEquals(25, feed.getEntries().size());
        SyndEntry entry = feed.getEntries().get(0);

        assertEquals("Tredje månaden med överhettad svensk ekonomi - tydlig säljsignal för börsen",
                FeedParser.plainTitle(entry));
        assertEquals("Tredje månaden med överhettad svensk ekonomi - tydlig säljsignal för börsen",
                FeedParser.title(entry));

        assertEquals("För tredje månaden på raken ligger Konjunkturinsitutets barometerindikator (\"konjunkturbarometern\") kvar i överhettat läge. Det råder alltså en klart och tydligt långsiktig säljsignal i enlighet med k",
                FeedParser.snippet(entry));
        assertTrue(FeedParser.description(entry).startsWith("För tredje månaden på raken"));
        assertEquals("https://1.bp.blogspot.com/-hD_mqKJx-XY/WLwTIKSEt6I/AAAAAAAAqfI/sztWEjwSYAoN22y_YfnZ-yotKjQsypZHACLcB/s72-c/konj.png",
                FeedParser.thumbnail(entry));

        assertEquals(null, FeedParser.firstEnclosure(entry));
    }

    @Test
    public void atom() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getAtom());

        assertEquals("http://cornucopia.cornubot.se/", feed.getLink());
        assertEquals("http://www.blogger.com/feeds/8354057230547055221/posts/default", FeedParser.selfLink(feed));

        assertEquals(25, feed.getEntries().size());
        SyndEntry entry = feed.getEntries().get(0);

        assertEquals("Tredje månaden med överhettad svensk ekonomi - tydlig säljsignal för börsen",
                FeedParser.plainTitle(entry));
        assertEquals("Tredje månaden med överhettad svensk ekonomi - tydlig säljsignal för börsen",
                FeedParser.title(entry));

        assertEquals("För tredje månaden på raken ligger Konjunkturinsitutets barometerindikator (\"konjunkturbarometern\") kvar i överhettat läge. Det råder alltså en klart och tydligt långsiktig säljsignal i enlighet med k",
                FeedParser.snippet(entry));
        assertTrue(FeedParser.description(entry).startsWith("För tredje månaden på raken"));
        assertEquals("https://1.bp.blogspot.com/-hD_mqKJx-XY/WLwTIKSEt6I/AAAAAAAAqfI/sztWEjwSYAoN22y_YfnZ-yotKjQsypZHACLcB/s72-c/konj.png",
                FeedParser.thumbnail(entry));

        assertEquals(null, FeedParser.firstEnclosure(entry));
    }

    @Test
    public void atomCowboy() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getCowboyAtom());

        assertEquals(15, feed.getEntries().size());
        SyndEntry entry = feed.getEntries().get(1);

        assertEquals("dummy-id-to-distinguis-from-alternate-link", entry.getUri());
        assertTrue("Should take the updated timestamp", FeedParser.publishDate(entry).contains("2016"));
        assertEquals("http://localhost:1313/images/zopfli_all_the_things.jpg",
                FeedParser.thumbnail(entry));
    }

    @Test
    public void morningPaper() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getMorningPaper());

        assertEquals("https://blog.acolyer.org", feed.getLink());
        assertNull(FeedParser.selfLink(feed));

        assertEquals(10, feed.getEntries().size());
        SyndEntry entry = feed.getEntries().get(0);

        assertEquals("Thou shalt not depend on me: analysing the use of outdated JavaScript libraries on the web",
                FeedParser.plainTitle(entry));

        assertEquals("http://1.gravatar.com/avatar/a795b4f89a6d096f314fc0a2c80479c1?s=96&d=identicon&r=G",
                FeedParser.thumbnail(entry));
    }

    @Test
    @Ignore
    public void fz() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getFz());

        assertEquals("http://www.fz.se/nyheter/", feed.getLink());
        assertNull(FeedParser.selfLink(feed));

        assertEquals(20, feed.getEntries().size());
        SyndEntry entry = feed.getEntries().get(0);

        assertEquals("Nier: Automata bjuder på maffig lanseringstrailer",
                FeedParser.plainTitle(entry));

        assertEquals("http://d2ihp3fq52ho68.cloudfront.net/YTo2OntzOjI6ImlkIjtpOjEzOTI3OTM7czoxOiJ3IjtpOjUwMDtzOjE6ImgiO2k6OTk5OTtzOjE6ImMiO2k6MDtzOjE6InMiO2k6MDtzOjE6ImsiO3M6NDA6IjU5YjA2YjgyZjkyY2IxZjBiMDZjZmI5MmE3NTk5NjMzMjIyMmU4NGMiO30=",
                FeedParser.thumbnail(entry));
    }

    private InputStream getAtom() {
        return getClass().getResourceAsStream("atom_cornucopia.xml");
    }

    private InputStream getRSS() {
        return getClass().getResourceAsStream("rss_cornucopia.xml");
    }

    private InputStream getCowboyRss() {
        return getClass().getResourceAsStream("rss_cowboy.xml");
    }

    private InputStream getCowboyAtom() {
        return getClass().getResourceAsStream("atom_cowboy.xml");
    }

    private InputStream getCyklistBloggen() {
        return getClass().getResourceAsStream("rss_cyklistbloggen.xml");
    }

    private InputStream getResearchRsc() {
        return getClass().getResourceAsStream("atom_research_rsc.xml");
    }

    private InputStream getMorningPaper() {
        return getClass().getResourceAsStream("rss_morningpaper.xml");
    }

    private InputStream getFz() {
        return getClass().getResourceAsStream("rss_fz.xml");
    }
}
