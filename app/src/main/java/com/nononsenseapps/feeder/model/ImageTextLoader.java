/*
 * Copyright (c) 2014 Jonas Kalderstam.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.feeder.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.text.HtmlToSpannedConverter;
import com.squareup.picasso.Picasso;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;

import java.io.IOException;

public class ImageTextLoader extends AsyncTaskLoader<Spanned> {
    final Context appContext;
    final float mDensityScale;

    final String mText;
    final Point maxSize;
    final Picasso p;

    // Used to insert alt text for images
    ImageTagHunter.Image lastImg = null;

    public ImageTextLoader(Context context, String text, Point windowSize) {
        super(context);
        this.mText = text;
        this.maxSize = windowSize;
        // Get screen density
        mDensityScale = context.getResources().getDisplayMetrics().density;

        appContext = context.getApplicationContext();
        p = Picasso.with(appContext);
    }

    Drawable getImgDrawable(final Attributes attributes) {
        Drawable d = null;
        try {
            // Source
            final String source = attributes.getValue("", "src");
            final String sWidth = attributes.getValue("", "width");
            final String sHeight = attributes.getValue("", "height");
            // Image size
            boolean shrunk = false;
            boolean hasSize = false;
            boolean hasPercentSize = false;
            int w=-1, h=-1;

            Log.d("JONAS", "Trying to get: " + source);

            // Calculate size first if possible
            if (sWidth != null && sWidth.contains("%") &&
                    sHeight != null && sHeight.contains("%")) {
                hasPercentSize = true;
            }
            if (sWidth != null && !sWidth.contains("%") &&
                    sHeight != null && !sHeight.contains("%")) {
                hasSize = true;
                Log.d("JONAS2", "Pixel size present");
                w = Integer.parseInt(sWidth);
                h = Integer.parseInt(sHeight);
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
                b = p.load(source).resize(w, h).tag(ImageTextLoader.this).get();
            } else if (hasSize) {
                Log.d("JONAS", "Image is small enough, getting");
                // No resize necessary since we know it is "small"
                b = p.load(source).tag(ImageTextLoader.this).get();
            } else if (hasPercentSize) {
                Log.d("JONAS2", "Percentsize, " +
                        "scaling for max");
                b = p.load(source).resize(maxSize.x,
                        maxSize.y).centerInside().tag(ImageTextLoader.this).get();
            } else {
                Log.d("JONAS2", "no size info, " +
                        "using intrinsic");
                b = p.load(source).tag(ImageTextLoader.this).get();
            }

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


    /**
     * Keeps aspect ratio.
     *
     * @param w current width of image
     * @param h current height of image
     * @return scaled (width, height) of image to fit the intended maxSize
     */
    Point scaleImage(int w, int h) {
        // Which is out of scale the most?
        float ratio = ((float) w) / ((float) maxSize.x);
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
//        Log.d("JONASFULL", mText);
//        } catch (XmlPullParserException e) {
//            Log.e("JONAS2", "" + e.getMessage());
//        } catch (IOException e) {
//            Log.e("JONAS2", "" + e.getMessage());
//        }
        MyHtml html = new MyHtml();
        return html.fromHtml(mText, getContext());
    }

    /**
     *
     * @return a Drawable with a youtube logo in the center
     */
    protected Drawable getYoutubeThumb(final com.nononsenseapps.text.VideoTagHunter.Video video) {
        Drawable[] layers = new Drawable[2];

        int w1, h1;
        try {
            final Bitmap b = p.load(video.imageurl).tag(ImageTextLoader.this).get();
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
            throw new NullPointerException(e.getLocalizedMessage());
        }

        // Add layer with play icon
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
        p.cancelTag(ImageTextLoader.this);
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

    class MyHtml extends com.nononsenseapps.text.Html {
        public HtmlToSpannedConverter getConverter(String source,
                Parser parser, Context context) {
            return new PicassoConverter(source, parser, context);
        }
    }

    class PicassoConverter extends HtmlToSpannedConverter {

        public PicassoConverter(final String source, final Parser parser, final Context context) {
            super(source, parser, context);
        }

        @Override
        protected void startImg(final SpannableStringBuilder text,
                final Attributes attributes) {
            // Get drawable
            Drawable d = getImgDrawable(attributes);
            if (d == null) {
                super.startImg(text, attributes);
                return;
            }

            int len = text.length();
            text.append("\uFFFC");

            text.setSpan(new ImageSpan(d, attributes.getValue("", "src")), len,
                    text.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Add a line break
            text.append("\n");
            // If there's an alt text, add it in italics
            final String alt = attributes.getValue("", "alt");
            if (alt != null && !alt.isEmpty()) {
                Log.d("JONAS4", "Last img: " + alt);
                int from = text.length();
                text.append(alt);
                text.setSpan(new StyleSpan(Typeface.ITALIC), from,
                        text.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.append("\n");
            }
        }

        @Override
        protected void startIframe(final SpannableStringBuilder text,
                final Attributes attributes) {
            // Parse information
            final com.nononsenseapps.text.VideoTagHunter.Video video = com
                    .nononsenseapps.text.VideoTagHunter
                    .getVideo(attributes.getValue("", "src"),
                            attributes.getValue("", "width"),
                            attributes.getValue("", "height"));

            if (video.src.toLowerCase().contains("youtube")) {
                try {
                    ClickableImageSpan span = new ClickableImageSpan(getYoutubeThumb(video)) {
                        @Override
                        public void onClick() {
                            Log.d("YOUTUBECLICK", "Url: " + video.link);
                            final Intent i =
                                    new Intent(Intent.ACTION_VIEW, Uri
                                            .parse(video.link));
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
                    int from = text.length();
                    text.append("Touch to play video");
                    text.setSpan(new StyleSpan(Typeface.ITALIC), from,
                            text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    text.append("\n\n");
                } catch (NullPointerException e) {
                    // Error is already logged..
                }
            }
        }
    }
}