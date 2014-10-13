package com.nononsenseapps.feeder.model.apis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Client class for talking to the backend REST API.
 */
public class BackendAPIClient {
    // TODO
    private static final String API_URL =
            //"https://northern-gasket-694.appspot.com/_ah/api/feeder/v1";
            //"http://192.168.1.17:5000";
            "https://feeder.nononsenseapps.com";

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
        @GET("/feeds")
        FeedsResponse getFeeds(@Query("min_timestamp") String min_timestamp);

        @POST("/feeds")
        Feed putFeed(@Body FeedMessage feedMessage);

        @POST("/feeds/delete")
        VoidResponse deleteFeed(@Body DeleteMessage deleteMessage);
    }

  public static class FeedsResponse {
    public List<Feed> feeds;
    public List<Delete> deletes;
  }

    public static class FeedMessage extends Feed {
        public String regid;
    }

  public static class DeleteMessage {
    public String link;
  }

    public static class FeedItem {
        public String title;
        public String title_stripped;
        public String description;
        public String snippet;
        public String link;
        public String image;
        public String published;
        public String author;
        public String comments;
        public String enclosure;
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

  public static class Delete {
    public String link;
    public String timestamp;
  }

    public static class VoidResponse {}
}
