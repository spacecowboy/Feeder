package com.nononsenseapps.feeder.model.apis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Client class for talking to the backend REST API.
 */
public class BackendAPIClient {
    // TODO
    private static final String API_URL =
            "https://northern-gasket-694.appspot.com/_ah/api/feeder/v1";
            //"http://192.168.1.17:9988/_ah/api/feeder/v1";

    /**
     * @return a FeedAPI implementation.
     */
    public static BackendAPI GetBackendAPI(final String accessToken) {
        // Create a very simple REST adapter, with oauth header
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(API_URL)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Authorization", accessToken);
                    }
                }).build();
        // Create an instance of the interface
        BackendAPI api = restAdapter.create(BackendAPI.class);

        return api;
    }

    public interface BackendAPI {
        @POST("/feeds")
        FeedsResponse getFeeds(@Body FeedsRequest request);

        @POST("/feeds/put")
        Feed putFeed(@Body FeedMessage feedMessage);
        //@POST("/feeds/put")
        //void putFeedAsync(@Body FeedMessage feedMessage, Callback<Feed> cb);

        @POST("/feeds/delete")
        VoidResponse deleteFeed(@Body FeedsRequest request);
    }

    public static class FeedsRequest {
        public String min_timestamp;
        public List<String> urls;
        public String regid;

        public FeedsRequest() {};
        public FeedsRequest(String... links) {
            this.urls = new ArrayList<String>(links.length);
            Collections.addAll(urls, links);
        }
    }

    public static class FeedMessage extends Feed {
        public String regid;
    }

    public static class FeedItem {
        public String title;
        public String title_stripped;
        public String description;
        public String snippet;
        public String link;
        public String imageurl;
        public String published;
        public String author;
        public String comments;
        public List<String> enclosures;
        public List<String> tags;
    }

    public static class Feed {
        public String link;
        public String title;
        public String description;
        public String published;
        public String tag;
        public String timestamp;
        public List<FeedItem> items;
    }

    public static class FeedsResponse {
        public List<Feed> feeds;
    }

    public static class VoidResponse {}
}
