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
    public void cowboy() throws Exception {
        SyndFeed feed = FeedParser.parseFeed(getCowboy());
        assertNotNull(feed);

        assertNull(FeedParser.selfLink(feed));

        assertEquals(15, feed.getEntries().size());

        SyndEntry entry = feed.getEntries().get(1);

        assertEquals("https://cowboyprogrammer.org/images/zopfli_all_the_things.jpg",
                FeedParser.thumbnail(entry));

        // Snippet should not contain images
        entry = feed.getEntries().get(4);
        assertEquals("Fixing the up button in Python shell history", entry.getTitle());
        assertEquals("In case your python/ipython shell doesn’t have a working history, e.g. pressing ↑ only prints some nonsensical ^[[A, then you are missing either the readline or ncurses library.\n" +
                        "\n" +
                        "Ipython is more descr",
                FeedParser.snippet(entry));
        // Snippet should not contain links
        entry = feed.getEntries().get(1);
        assertEquals("Compress all the images!", entry.getTitle());
        assertEquals("*Update 2016-11-22: Made the Makefile compatible with BSD sed (MacOS)*\n" +
                        "\n" +
                        "One advantage that static sites, such as those built by Hugo, provide is fast loading times. Because there is no processing to b",
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

    private InputStream getCowboy() {
        return getClass().getResourceAsStream("rss_cowboy.xml");
    }

    private InputStream getMorningPaper() {
        return getClass().getResourceAsStream("rss_morningpaper.xml");
    }

    private InputStream getFz() {
        return getClass().getResourceAsStream("rss_fz.xml");
    }
}
