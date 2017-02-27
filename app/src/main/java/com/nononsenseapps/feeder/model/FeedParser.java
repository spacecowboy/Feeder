package com.nononsenseapps.feeder.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URL;

import static java.lang.Math.min;

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

    public static long timestamp(SyndFeed parsedFeed) {
        if (parsedFeed.getPublishedDate() != null) {
            return parsedFeed.getPublishedDate().getTime();
        } else {
            return DateTime.now().getMillis();
        }
    }

    @Nullable
    public static String firstEnclosure(SyndEntry entry) {
        if (!entry.getEnclosures().isEmpty()) {
            SyndEnclosure enclosure = entry.getEnclosures().get(0);
            if (enclosure.getUrl() != null) {
                return enclosure.getUrl();
            }
        }

        return null;
    }

    @Nullable
    public static String publishDate(SyndEntry entry) {
        if (entry.getPublishedDate() != null) {
            return new DateTime(entry.getPublishedDate().getTime()).toDateTimeISO().toString();
        }
        return null;
    }

    @NonNull
    public static String title(SyndEntry entry) {
        return nonNullString(entry.getTitle());
    }

    @NonNull
    public static String description(SyndEntry entry) {
        if (entry.getDescription() != null) {
            return nonNullString(entry.getDescription().getValue());
        }
        return "";
    }

    @NonNull
    public static String snippet(SyndEntry entry) {
        String text = description(entry);
        return text.substring(0, min(200, text.length()));
    }

    public static class FeedParsingError extends Exception {
        FeedParsingError(Exception e) {
            super(e);
        }
    }

    @NonNull
    private static String nonNullString(@Nullable String text) {
        return text == null ? "" : text;
    }
}
