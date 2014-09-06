package com.nononsenseapps.feeder.model;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.nononsenseapps.feeder.model.apis.GoogleFeedAPIClient;

public class RssSearchLoader extends
        AsyncTaskLoader<GoogleFeedAPIClient.FindResponse> {

    private final String searchQuery;
    private final GoogleFeedAPIClient.GoogleFeedAPI googleFeedAPI;

    public RssSearchLoader(final Context context, final String searchQuery) {
        super(context);
        googleFeedAPI = GoogleFeedAPIClient.GetFeedAPI();

        this.searchQuery = searchQuery;
    }

    @Override
    public GoogleFeedAPIClient.FindResponse loadInBackground() {

        GoogleFeedAPIClient.FindResponse result = null;
        Log.d("JONASRSS", "Query " + searchQuery);
        if (searchQuery.startsWith("http://") ||
                searchQuery.startsWith("https://")) {
            // Is a url, load directly
            Log.d("JONASRSS", "Loading feed directly...");
            result = googleFeedAPI.loadFeed("1.0", searchQuery);
        }

        if (result != null) {
            Log.d("JONASRSS", "Status " + result.responseStatus);
        }

        if (result == null || result.responseStatus > 399) {
            Log.d("JONASRSS", "Trying find instead...");
            result = googleFeedAPI.findFeeds("1.0", searchQuery);
        }

        return result;
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();
    }
}
