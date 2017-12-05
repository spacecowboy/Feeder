package com.nononsenseapps.feeder.model;

import com.nononsenseapps.jsonfeed.Feed;
import com.nononsenseapps.jsonfeed.Item;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;

import static java.util.Collections.emptyList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class FeedParserTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void noStyles() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getResearchRsc());
        assertNotNull(feed);

        assertEquals("http://research.swtch.com/feed.atom", feed.getFeed_url());

        assertEquals(17, feed.getItems().size());

        Item entry = feed.getItems().get(9);

        assertEquals("http://research.swtch.com/qr-bbc.png", entry.getImage());

        assertEquals("QArt Codes",
                entry.getTitle());

        // Style tags should be ignored
        assertEquals("QR codes are 2-dimensional bar codes that encode arbitrary text strings. A common use of QR codes is to encode URLs so that people can scan a QR code (for example, on an advertising poster, building r",
                entry.getSummary());
    }

    @Test
    public void feedAuthorIsUsedAsFallback() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getResearchRsc());
        assertNotNull(feed);

        assertEquals("http://research.swtch.com/feed.atom", feed.getFeed_url());

        assertEquals(17, feed.getItems().size());

        Item entry = feed.getItems().get(9);

        assertEquals("Russ Cox", feed.getAuthor().getName());
        assertEquals(feed.getAuthor(), entry.getAuthor());
    }

    @Test
    public void cyklist() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getCyklistBloggen());
        assertNotNull(feed);

        assertNull(feed.getFeed_url());

        assertEquals(10, feed.getItems().size());

        Item entry = feed.getItems().get(0);

        assertEquals("http://www.cyklistbloggen.se/wp-content/uploads/2014/01/Danviksklippan-skyltad.jpg", entry.getImage());

        assertEquals("Ingen ombyggning av Danvikstull",
                entry.getTitle());

        // Make sure character 160 (non-breaking space) is trimmed
        assertEquals("För mer än tre år sedan aviserade dåvarande Allians-styrda Stockholms Stad att man äntligen skulle bredda den extremt smala passagen på pendlingsstråket vid Danvikstull: <image: > I smalaste passagen ",
                entry.getSummary());
    }

    @Test
    public void cowboy() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getCowboyRss());
        assertNotNull(feed);

        assertNull(feed.getFeed_url());

        assertEquals(15, feed.getItems().size());

        Item entry = feed.getItems().get(1);

        assertEquals("https://cowboyprogrammer.org/images/zopfli_all_the_things.jpg",
                entry.getImage());

        // Snippet should not contain images
        entry = feed.getItems().get(4);
        assertEquals("Fixing the up button in Python shell history", entry.getTitle());
        assertEquals("In case your python/ipython shell doesn’t have a working history, e.g. pressing ↑ only prints some nonsensical ^[[A, then you are missing either the readline or ncurses library. <image: Python shell w",
                entry.getSummary());
        // Snippet should not contain links
        entry = feed.getItems().get(1);
        assertEquals("Compress all the images!", entry.getTitle());
        assertEquals("*Update 2016-11-22: Made the Makefile compatible with BSD sed (MacOS)* One advantage that static sites, such as those built by Hugo, provide is fast loading times. Because there is no processing to be",
                entry.getSummary());
    }

    @Test
    public void rss() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getRSS());

        assertEquals("http://cornucopia.cornubot.se/", feed.getHome_page_url());
        assertNull(feed.getFeed_url());

        assertEquals(25, feed.getItems().size());
        Item entry = feed.getItems().get(0);

        assertEquals("Tredje månaden med överhettad svensk ekonomi - tydlig säljsignal för börsen",
                entry.getTitle());
        assertEquals("Tredje månaden med överhettad svensk ekonomi - tydlig säljsignal för börsen",
                entry.getTitle());

        assertEquals("För tredje månaden på raken ligger Konjunkturinsitutets barometerindikator (\"konjunkturbarometern\") kvar i överhettat läge. Det råder alltså en klart och tydligt långsiktig säljsignal i enlighet med k",
                entry.getSummary());
        assertTrue(entry.getContent_html().startsWith("För tredje månaden på raken"));
        assertEquals("https://1.bp.blogspot.com/-hD_mqKJx-XY/WLwTIKSEt6I/AAAAAAAAqfI/sztWEjwSYAoN22y_YfnZ-yotKjQsypZHACLcB/s72-c/konj.png",
                entry.getImage());

        assertEquals(emptyList(), entry.getAttachments());
    }

    @Test
    public void atom() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getAtom());

        assertEquals("http://cornucopia.cornubot.se/", feed.getHome_page_url());
        assertEquals("http://www.blogger.com/feeds/8354057230547055221/posts/default", feed.getFeed_url());

        assertEquals(25, feed.getItems().size());
        Item entry = feed.getItems().get(0);

        assertEquals("Tredje månaden med överhettad svensk ekonomi - tydlig säljsignal för börsen",
                entry.getTitle());
        assertEquals("Tredje månaden med överhettad svensk ekonomi - tydlig säljsignal för börsen",
                entry.getTitle());

        assertEquals("För tredje månaden på raken ligger Konjunkturinsitutets barometerindikator (\"konjunkturbarometern\") kvar i överhettat läge. Det råder alltså en klart och tydligt långsiktig säljsignal i enlighet med k",
                entry.getSummary());
        assertTrue(entry.getContent_html().startsWith("För tredje månaden på raken"));
        assertEquals("https://1.bp.blogspot.com/-hD_mqKJx-XY/WLwTIKSEt6I/AAAAAAAAqfI/sztWEjwSYAoN22y_YfnZ-yotKjQsypZHACLcB/s72-c/konj.png",
                entry.getImage());

        assertEquals(emptyList(), entry.getAttachments());
    }

    @Test
    public void atomCowboy() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getCowboyAtom());

        assertEquals(15, feed.getItems().size());
        Item entry = feed.getItems().get(1);

        assertEquals("dummy-id-to-distinguis-from-alternate-link", entry.getId());
        assertTrue("Should take the updated timestamp", entry.getDate_published().contains("2016"));
        assertEquals("http://localhost:1313/images/zopfli_all_the_things.jpg",
                entry.getImage());

        assertEquals("http://localhost:1313/css/images/logo.png", feed.getIcon());
    }

    @Test
    public void morningPaper() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getMorningPaper());

        assertEquals("https://blog.acolyer.org", feed.getHome_page_url());
        assertNull(feed.getFeed_url());

        assertEquals(10, feed.getItems().size());
        Item entry = feed.getItems().get(0);

        assertEquals("Thou shalt not depend on me: analysing the use of outdated JavaScript libraries on the web",
                entry.getTitle());

        assertEquals("http://1.gravatar.com/avatar/a795b4f89a6d096f314fc0a2c80479c1?s=96&d=identicon&r=G",
                entry.getImage());
    }

    @Test
    public void londoner() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getLondoner());

        assertEquals("http://londonist.com/", feed.getHome_page_url());
        assertNull(feed.getFeed_url());

        assertEquals(40, feed.getItems().size());
        Item entry = feed.getItems().get(0);

        assertEquals("Make The Most Of London's Offerings With Chip",
                entry.getTitle());

        assertEquals("https://assets.londonist.com/uploads/2017/06/i300x150/chip_2.jpg",
                entry.getImage());
    }

    @Test
    @Ignore
    public void fz() throws Exception {
        Feed feed = FeedParser.INSTANCE.parseFeedInputStream(getFz());

        assertEquals("http://www.fz.se/nyheter/", feed.getHome_page_url());
        assertNull(feed.getFeed_url());

        assertEquals(20, feed.getItems().size());
        Item entry = feed.getItems().get(0);

        assertEquals("Nier: Automata bjuder på maffig lanseringstrailer",
                entry.getTitle());

        assertEquals("http://d2ihp3fq52ho68.cloudfront.net/YTo2OntzOjI6ImlkIjtpOjEzOTI3OTM7czoxOiJ3IjtpOjUwMDtzOjE6ImgiO2k6OTk5OTtzOjE6ImMiO2k6MDtzOjE6InMiO2k6MDtzOjE6ImsiO3M6NDA6IjU5YjA2YjgyZjkyY2IxZjBiMDZjZmI5MmE3NTk5NjMzMjIyMmU4NGMiO30=",
                entry.getImage());
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

    private InputStream getLondoner() {
        return getClass().getResourceAsStream("rss_londoner.xml");
    }
}
