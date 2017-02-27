package com.nononsenseapps.feeder.model;

import com.rometools.rome.feed.synd.SyndFeed;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class FeedParserTest {

    @Test
    public void basic() throws Exception {
        SyndFeed feed = FeedParser.parseFeed("https://cowboyprogrammer.org/index.xml");
        assertNotNull(feed);
        // TODO this test should not rely on internet
        assertEquals(15, feed.getEntries().size());
    }
}
