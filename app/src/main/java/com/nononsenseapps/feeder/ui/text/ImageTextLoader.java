package com.nononsenseapps.feeder.ui.text;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.content.AsyncTaskLoader;
import android.text.Spanned;

public class ImageTextLoader extends AsyncTaskLoader<Spanned> {

    private final String text;
    private final Point maxSize;

    public ImageTextLoader(Context context, String text, Point maxSize) {
        super(context);
        this.text = text;
        this.maxSize = maxSize;
    }

    @Override
    public Spanned loadInBackground() {
        return HtmlConverter.toSpannedWithImages(text, getContext(), maxSize);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();
    }
}
