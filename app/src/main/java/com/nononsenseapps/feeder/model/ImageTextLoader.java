package com.nononsenseapps.feeder.model;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by jonas on 8/24/14.
 */
public class ImageTextLoader extends AsyncTaskLoader<Spanned> {

    final Html.ImageGetter imgThing;
    final String text;
    final Point maxSize;

    public ImageTextLoader(Context context, String text, Point windowSize) {
        super(context);
        this.text = text;
        this.maxSize = windowSize;

        final Context appContext = context.getApplicationContext();

        imgThing = new Html.ImageGetter() {

            /**
             * This methos is called when the HTML parser encounters an
             * &lt;img&gt; tag.  The <code>source</code> argument is the
             * string from the "src" attribute; the return value should be
             * a Drawable representation of the image or <code>null</code>
             * for a generic replacement image.  Make sure you call
             * setBounds() on your Drawable if it doesn't already have
             * its bounds set.
             *
             * @param source
             */
            @Override
            public Drawable getDrawable(final String source) {
                Drawable d = null;
                try {
                    Log.d("JONAS", "Trying to get: " + source);
                    final Bitmap b = Picasso.with(appContext).load(source)
                            //.resize(size.x, size.y).centerInside()
                            .get();
                    Log.d("JONAS", "Got it!");
                    int w = b.getWidth();
                    int h = b.getHeight();
                    // Shrink if big
                    if (w > maxSize.x || h > maxSize.y) {
                        Point newSize = scaleImage(w, h);
                        w = newSize.x;
                        h = newSize.y;
                    }

                    d = new BitmapDrawable(appContext.getResources(), b);
                    Log.d("JONAS", "Bounds: " + d.getIntrinsicWidth() + ", " +
                                   "" + d.getIntrinsicHeight() + " vs " +
                                   w + ", " + h);
                    d.setBounds(0, 0, w, h);
                } catch (IOException e) {
                    Log.e("JONAS", "" + e.getMessage());
                }
                return d;
            }
        };
    }


    /**
     * Keeps aspect ratio.
     *
     * @param w current width of image
     * @param h current height of image
     * @return scaled (width, height) of image to fit the intended maxSize
     */
    Point scaleImage(int w, int h) {
        // Which is out of scale the most?
        final float xratio = ((float) w) / ((float) maxSize.x);
        final float yratio = ((float) h) / ((float) maxSize.y);
        float ratio = xratio;
        if (yratio > xratio) {
            ratio = yratio;
        }
        // Calculate new size. Maintains aspect ratio.
        int newWidth = (int) ((float) w / ratio);
        int newHeight = (int) ((float) h / ratio);

        return new Point(newWidth, newHeight);
    }

    /**
     * Do shit
     */
    @Override
    public Spanned loadInBackground() {
        return android.text.Html.fromHtml(text, imgThing, null);
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
