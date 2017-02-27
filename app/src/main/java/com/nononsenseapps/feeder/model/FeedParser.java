package com.nononsenseapps.feeder.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

public class FeedParser {

    // Should reuse same instance to have same cache
    private static OkHttpClient _client;

    private static OkHttpClient cachingClient(File cacheDirectory) {
        if (_client != null) {
            return _client;
        }

        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(cacheDirectory, cacheSize);

        _client = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();

        return _client;
    }

    public static SyndFeed parseFeed(String url, File cacheDir) throws FeedParsingError {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = cachingClient(cacheDir).newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            Log.d("RSSLOCAL", "cache response: " + response.cacheResponse());
            Log.d("RSSLOCAL", "network response: " + response.networkResponse());

            return new SyndFeedInput().build(new XmlReader(response.body().source().inputStream()));
        } catch (Throwable e) {
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

        public FeedParsingError(Throwable e) {
            super(e);
        }
    }

    @NonNull
    private static String nonNullString(@Nullable String text) {
        return text == null ? "" : text;
    }
}
