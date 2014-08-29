package com.nononsenseapps.feeder.model;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.xml.sax.XMLReader;

import java.io.IOException;

public class ImageTextLoader extends AsyncTaskLoader<Spanned> {

    private final float mDensityScale;

    // Used in formatting
    private static class Monospace { }
    private static class RelativeSize { }
    private static class Bold { }

    final Html.ImageGetter imgThing;
    final String text;
    final Point maxSize;

    public ImageTextLoader(Context context, String text, Point windowSize) {
        super(context);
        this.text = text;
        this.maxSize = windowSize;
        // Get screen density
        mDensityScale = context.getResources().getDisplayMetrics().density;

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
                    // Scale with screen density
                    w = (int) (w * mDensityScale + 0.5f);
                    h = (int) (h * mDensityScale + 0.5f);
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
        return android.text.Html.fromHtml(text, imgThing, new Html.TagHandler() {
            private boolean ordered = false;
            private int orderCount = 1;

            @Override
            public void handleTag(final boolean opening, final String tag,
                    final Editable output, final XMLReader xmlReader) {
                Log.d("JONAS", "Got tag: " + tag + " opening: " + opening);
                if (tag.equalsIgnoreCase("ul")) {
                    handleUl(output, opening);
                } else if (tag.equalsIgnoreCase("ol")) {
                    handleOl(output, opening);
                } else if (tag.equalsIgnoreCase("li")) {
                    handleLi(output, opening);
                } else if (tag.equalsIgnoreCase("img")) {
                    handleImgEnd(output);
                } else if (tag.equalsIgnoreCase("code")) {
                    handleCode(output, opening);
                } else if (tag.equalsIgnoreCase("pre")) {
                    handlePre(output, opening);
                }
            }

            // Am not notified about starts but ends
            private void handleImgEnd(final Editable text) {
                // Add a line break if not present
                int len = text.length();
                if (len >= 1 && text.charAt(len - 1) == '\n') {
                    return;
                }
                text.append("\n");
            }

            // Start lists with a line break
            private void handleOl(final Editable text, final boolean start) {
                // Remember that we are in an ordered list
                ordered = start;
                int len = text.length();
                if (start) {
                    // Start at one
                    orderCount = 1;
                    if (len >= 1 && text.charAt(len - 1) == '\n') {
                        return;
                    }
                    text.append("\n");
                }
            }

            // Start lists with a line break
            private void handleUl(final Editable text, final boolean start) {
                int len = text.length();
                if (start) {
                    if (len >= 1 && text.charAt(len - 1) == '\n') {
                        return;
                    }
                    text.append("\n");
                }
            }

            // List items
            private void handleLi(final Editable text,
                    final boolean start) {
                int len = text.length();
                if (start) {
                    // Start with blip or number
                    if (ordered) {
                        // Number in bold
                        start(text, new Bold());
                        text.append("" + orderCount + ". ");
                        end(text, Bold.class, new StyleSpan(Typeface.BOLD));
                    } else {
                        // Set a bullet point
                        text.append("\u2022 ");
                    }
                } else {
                    // End line with newline
                    if (len >= 1 && text.charAt(len - 1) == '\n') {
                        return;
                    }
                    text.append("\n");
                    // Increment count
                    orderCount += 1;
                }
            }

            private void handlePre(final Editable text,
                    final boolean start) {
                int len = text.length();
                // Make sure it has spaces before and after
                if (len >= 1 && text.charAt(len - 1) == '\n') {
                    if (len >= 2 && text.charAt(len - 2) != '\n') {
                        text.append("\n");
                    }
                } else if (len != 0) {
                    text.append("\n\n");
                }
                // TODO It also shouldn't wrap, but not sure how to accomplish
                // that..
            }

            // Source code
            private void handleCode(final Editable text,
                    final boolean start) {
                // Should be monospace
                if (start) {
                    start(text, new Monospace());
                    start(text, new RelativeSize());
                } else {
                    end(text, Monospace.class,
                            new TypefaceSpan("monospace"));
                    end(text, RelativeSize.class,
                            new RelativeSizeSpan(0.8f));
                }
            }

            private void start(Editable text, Object mark) {
                int len = text.length();
                text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
            }
            private void end(Editable text, Class kind,
                    Object repl) {
                int len = text.length();
                Object obj = getLast(text, kind);
                int where = text.getSpanStart(obj);

                text.removeSpan(obj);

                if (where != len) {
                    text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            private Object getLast(Spanned text, Class kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
                Object[] objs = text.getSpans(0, text.length(), kind);

                if (objs.length == 0) {
                    return null;
                } else {
                    return objs[objs.length - 1];
                }
            }
        });
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
