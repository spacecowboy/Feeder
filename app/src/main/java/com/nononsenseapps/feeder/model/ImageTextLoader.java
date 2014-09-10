package com.nononsenseapps.feeder.model;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;

import com.nononsenseapps.feeder.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageTextLoader extends AsyncTaskLoader<Spanned> {

    final float mDensityScale;

    // Used in formatting
    static class Monospace { }
    static class RelativeSize { }
    static class Bold { }
    static class Italic { }

    final Html.ImageGetter imgThing;
    final String mText;
    final Point maxSize;
    final Picasso p;

    final HashMap<String, ImageTagHunter.Image> images;
    final ArrayList<VideoTagHunter.Video> videos;
    int videoIndex = 0;

    // Used to insert alt text for images
    ImageTagHunter.Image lastImg = null;

    public ImageTextLoader(Context context, String text, Point windowSize) {
        super(context);
        images = new HashMap<String, ImageTagHunter.Image>();
        videos = new ArrayList<VideoTagHunter.Video>();
        this.mText = text;
        this.maxSize = windowSize;
        // Get screen density
        mDensityScale = context.getResources().getDisplayMetrics().density;

        final Context appContext = context.getApplicationContext();
        p = Picasso.with(appContext);

        imgThing = new Html.ImageGetter() {

            /**
             * This method is called when the HTML parser encounters an
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
                    // Image size
                    boolean shrunk = false;
                    int w=-1, h=-1;
                    // Get the attributes from the hashmap
                    ImageTagHunter.Image img = images.get(source);
                    lastImg = img;
                    Log.d("JONAS", "Trying to get: " + source);

                    // Calculate size first if possible
                    if (img.hasSize()) {
                        Log.d("JONAS2", "Pixel size present");
                        w = img.getIntWidth();
                        h = img.getIntHeight();
                        // This should be parsed away, but just in case...
                        if (w < 10 || h < 10) {
                            Log.d("JONAS4", "Bullshit image, ignoring...");
                            lastImg = null;
                            return null;
                        }
                        // Scale with screen density
                        w = (int) (w * mDensityScale + 0.5f);
                        h = (int) (h * mDensityScale + 0.5f);
                        // Shrink if big (used for picasso downloading)
                        // Don't resize if small, since it can be scaled
                        // directly in drawable bounds. Need to shrink it to
                        // save precious memory however.
                        if (w > maxSize.x) {
                            Log.d("JONAS2", "Its big, shrinking it");
                            Point newSize = scaleImage(w, h);
                            w = newSize.x;
                            h = newSize.y;
                            shrunk = true;
                        }
                    }

                    final Bitmap b;
                    if (shrunk) {
                        Log.d("JONAS2", "Resizing with picasso");
                        b = p.load(source).resize(w, h).get();
                    } else if (img.hasSize()) {
                        Log.d("JONAS", "Image is small enough, getting");
                        // No resize necessary since we know it is "small"
                        b = p.load(source).get();
                    } else { // if (img.hasPercentSize()) {
                        Log.d("JONAS2", "Percentsize or no size info, " +
                                        "scaling for max");
                        b = p.load(source).resize(maxSize.x,
                                maxSize.y).centerInside().get();
                    }
//                    else {
//                        Log.d("JONAS2", "No size present, just loading..");
//                        b = p.load(source).get();
//                    }

                    if (w == -1) {
                        w = b.getWidth();
                        h = b.getHeight();
                        // Scale with screen density
                        w = (int) (w * mDensityScale + 0.5f);
                        h = (int) (h * mDensityScale + 0.5f);
                    }
                    // Enlarge if close, or shrink if big
                    if (((float) w) / ((float) maxSize.x) > 0.5) {
                        Log.d("JONAS2", "Scaling final image bounds");
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
        //final float yratio = ((float) h) / ((float) maxSize.y);
        float ratio = xratio;
//        if (yratio > xratio) {
//            ratio = yratio;
//        }
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
        // First find all images manually to get their sizes and alt-texts
//        try {
        // Parse the document once and reuse
        final Document doc = Jsoup.parse(mText);
        ImageTagHunter.getImages(doc, this.images);
        VideoTagHunter.getVideos(doc, this.videos);
        Log.d("JONASFULL", mText);
//        } catch (XmlPullParserException e) {
//            Log.e("JONAS2", "" + e.getMessage());
//        } catch (IOException e) {
//            Log.e("JONAS2", "" + e.getMessage());
//        }
        return android.text.Html.fromHtml(mText, imgThing, new Html.TagHandler() {
            private boolean ordered = false;
            private int orderCount = 1;

            @Override
            public void handleTag(final boolean opening, final String tag,
                    final Editable output, final XMLReader xmlReader) {
                //Log.d("JONAS", "Got tag: " + tag + " opening: " + opening);
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
                } else if (tag.equalsIgnoreCase("iframe")) {
                    // TODO handle embed and object also
                    handleIframe(output, opening);
                }
            }

            // Am not notified about starts but ends
            private void handleImgEnd(final Editable text) {
                // Add a line break if not present
                int len = text.length();
                if (len >= 1 && text.charAt(len - 1) == '\n') {

                } else {
                    text.append("\n");
                }
                // If there's an alt text, add it in italics
                if (lastImg != null && lastImg.alt != null) {
                    Log.d("JONAS4", "Last img: " + lastImg.alt);
                    start(text, new Italic());
                    text.append(lastImg.alt);
                    end(text, Italic.class, new StyleSpan(Typeface.ITALIC));
                    text.append("\n");
                }
            }

            // Show thumbnail for video
            private void handleIframe(final Editable text,
                    final boolean start) {
                if (!start) {
                    // Ignore end now
                    return;
                }

                // Fetch the video from list since I am given no information
                // about attributes here...
                final VideoTagHunter.Video video = videos.get(videoIndex);

                // For now only handle youtube videos
                // TODO handle more than youtube
                Log.d("JONASYOUTUBE", "src: " + video.src);
                if (video.src.toLowerCase().contains("youtube")) {
                    ClickableImageSpan span = new ClickableImageSpan
                            (getYoutubeThumb(video)) {
                        @Override
                        public void onClick() {
                            Log.d("YOUTUBECLICK", "Url: " + video.link);
                            final Intent i = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(video.link));
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(i);
                        }
                    };
                    int len = text.length();
                    text.append("\uFFFC");
                    text.setSpan(span, len, text.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // Add newline also
                    text.append("\n");
                    start(text, new Italic());
                    text.append("Touch to play video");
                    end(text, Italic.class, new StyleSpan(Typeface.ITALIC));
                    text.append("\n\n");
                }

                // Do this last
                videoIndex++;
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
     *
     * @return a Drawable with a youtube logo in the center
     */
    protected Drawable getYoutubeThumb(final VideoTagHunter.Video video) {
        Drawable[] layers = new Drawable[2];

        int w1 = 1, h1 = 1;
        try {
            final Bitmap b = p.load(video.imageurl).get();
            final Point newSize = scaleImage(b.getWidth(), b.getHeight());
            w1 = newSize.x;
            h1 = newSize.y;
            final BitmapDrawable d = new BitmapDrawable(getContext()
                    .getResources(), b);
            Log.d("JONASYOUTUBE", "Bounds: " + d.getIntrinsicWidth() + ", " +
                       "" + d.getIntrinsicHeight() + " vs " +
                       w1 + ", " + h1);
            // Settings bounds later
            //d.setBounds(0, 0, w1, h1);
            // Set in layer
            layers[0] = d;
        } catch (IOException e) {
            Log.e("JONASYOUTUBE", "" + e.getMessage());
        }

        // Add layer with play icon
        // TODO
        final Drawable playicon = getContext().getResources().getDrawable(R
                .drawable
                .youtube_icon);
        // 20% size, in middle
        int w2 = playicon.getIntrinsicWidth();
        int h2 = playicon.getIntrinsicHeight();

        final double ratio = ((double) h2) / ((double) w2);

        // Start with width which is known
        final double relSize = 0.2;
        w2 = (int) (relSize * w1);
        final int left = (int) (((double) (w1 - w2)) / 2.0);
        // Then height is simple
        h2 = (int) (ratio * w2);
        final int top = (int) (((double) (h1 - h2)) / 2.0);

        Log.d("JONASYOUTUBE", "l t w h: " + left + " " + top + " " + w2 + " "
                              + h2);

        // And add to layer
        layers[1] = playicon;
        final LayerDrawable ld = new LayerDrawable(layers);
        // Need to set bounds on outer drawable first as it seems to override
        // child bounds
        ld.setBounds(0, 0, w1, h1);
        // Now set smaller bounds on youtube icon
        playicon.setBounds(left, top, left+w2, top+h2);
        return ld;
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
