package com.nononsenseapps.feeder.model;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;

public class FeedParser {

    public static SyndFeed parseFeed(String url) throws FeedParsingError {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            return new SyndFeedInput().build(new XmlReader(new URL(url)));
        } catch (FeedException | IOException e) {
            throw new FeedParsingError(e);
        }
    }

    public static class FeedParsingError extends Exception {
        FeedParsingError(Exception e) {
            super(e);
        }
    }
}
