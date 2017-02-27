package com.nononsenseapps.feeder.model;

import android.content.AsyncTaskLoader;
import android.content.Context;
import com.nononsenseapps.feeder.util.FileLog;
import com.nononsenseapps.feeder.util.LoaderResult;
import com.rometools.rome.feed.synd.SyndFeed;

public class FeedParseLoader extends
        AsyncTaskLoader<LoaderResult<SyndFeed>> {

    private final String searchQuery;

    public FeedParseLoader(final Context context, final String searchQuery) {
        super(context);
        this.searchQuery = searchQuery;
    }

    @Override
    public LoaderResult<SyndFeed> loadInBackground() {
        SyndFeed feed = null;
        String msg = null;
        try {
            feed = FeedParser.parseFeed(searchQuery);
        } catch (FeedParser.FeedParsingError feedParsingError) {
            FileLog.d(getContext(), feedParsingError.getLocalizedMessage());
            msg = feedParsingError.getLocalizedMessage();
        }

        return new LoaderResult<>(feed, msg);
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
