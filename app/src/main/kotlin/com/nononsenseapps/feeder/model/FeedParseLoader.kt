package com.nononsenseapps.feeder.model

import android.content.AsyncTaskLoader
import android.content.Context
import com.nononsenseapps.feeder.util.LoaderResult
import com.nononsenseapps.feeder.util.d
import com.nononsenseapps.jsonfeed.Feed
import java.io.File

class FeedParseLoader(context: Context,
                      private val searchQuery: String) :
        AsyncTaskLoader<LoaderResult<Feed?>>(context) {

    override fun loadInBackground(): LoaderResult<Feed?> {
        var feed: Feed? = null
        var msg: String? = null
        try {
            var cacheDir: File? = context.externalCacheDir
            // Yes, cacheDir can indeed be null
            if (cacheDir == null) {
                cacheDir = context.filesDir
            }
            feed = FeedParser.parseFeed(searchQuery, cacheDir)
        } catch (feedParsingError: FeedParser.FeedParsingError) {
            d(context, feedParsingError.localizedMessage)
            msg = feedParsingError.localizedMessage
        }

        return LoaderResult(feed, msg)
    }

    /**
     * Handles a request to start the Loader.
     */
    override fun onStartLoading() {
        forceLoad()
    }

    /**
     * Handles a request to stop the Loader.
     */
    override fun onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad()
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    override fun onReset() {
        super.onReset()

        // Ensure the loader is stopped
        onStopLoading()
    }
}
