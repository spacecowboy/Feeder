package com.nononsenseapps.feeder.model;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
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
}
