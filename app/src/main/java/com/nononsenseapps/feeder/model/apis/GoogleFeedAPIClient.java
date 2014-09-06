package com.nononsenseapps.feeder.model.apis;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * See
 * #link{https://developers.google.com/feed/v1/jsondevguide#basic_query}
 */
public class GoogleFeedAPIClient {
    private static final String API_URL = "https://ajax.googleapis.com";

    public static class FindResponse {
        public int responseStatus;
        public String responseDetails;
        public FindResponseData responseData;
    }

    public static class FindResponseData {
        public String query;
        public List<Entry> entries;
        public Feed feed;
    }

    public static class Entry {
        public String url;
        public String title;
        public String contentSnippet;
        public String link;
    }

    public static class Feed {
        public String feedUrl;
        public String title;
        public String link;
        public String author;
        public String description;
    }

    public interface GoogleFeedAPI {
        /**
         * @param version always give "1.0" for this. Not sure how to hard code
         * @param query search query
         * @return FindResponse
         */
        @GET("/ajax/services/feed/find")
        FindResponse findFeeds(
                @Query("v") String version,
                @Query("q") String query
        );

        /**
         * @param version always give "1.0" for this. Not sure how to hard code
         * @param url RSS/Atom address
         * @return FindResponse
         */
        @GET("/ajax/services/feed/load")
        FindResponse loadFeed(
                @Query("v") String version,
                @Query("q") String url
        );
    }

    /**
     *
     * @return a FeedAPI implementation.
     */
    public static GoogleFeedAPI GetFeedAPI() {
        // Create a very simple REST adapter
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .build();
        // Create an instance of the interface
        GoogleFeedAPI googleFeedApi = restAdapter.create(GoogleFeedAPI.class);

        return googleFeedApi;
    }
}
