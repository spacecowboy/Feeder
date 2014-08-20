package com.nononsenseapps.feeder.model;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.shirwa.simplistic_rss.RssItem;
import com.shirwa.simplistic_rss.RssReader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class RssLoader extends AsyncTaskLoader<List<RssItem>> {

    public final static String TAG = "RssLoader";
    public final String url;

    public RssLoader(final Context context, final String url) {
        super(context);

        this.url = url;
    }

    @Override
    public List<RssItem> loadInBackground() {
        try {
            return new RssReader(url).getItems();
        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            printWriter.flush();

            Log.e(TAG, e.getLocalizedMessage());
            Log.e(TAG, "" + writer.toString());
            return null;
        }
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
