package com.nononsenseapps.feeder.ui.text;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import com.bumptech.glide.BitmapRequestBuilder;
import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.util.GlideUtils;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;

import java.util.concurrent.ExecutionException;

public class GlideConverter extends HtmlToSpannedConverter {

    private final Context context;
    private final boolean allowDownload;

    private final float densityScale;
    private final Point maxSize;

    public GlideConverter(final String source, final Parser parser, final Context context, final Point maxSize, boolean allowDownload) {
        super(source, parser, context);
        this.context = context.getApplicationContext();
        this.allowDownload = allowDownload;
        // Get screen density
        densityScale = this.context.getResources().getDisplayMetrics().density;
        this.maxSize = maxSize;
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
        final VideoTagHunter.Video video = VideoTagHunter
                .getVideo(attributes.getValue("", "src"),
                        attributes.getValue("", "width"),
                        attributes.getValue("", "height"));

        if (video.src.toLowerCase().contains("youtube")) {
            try {
                ClickableImageSpan span = new ClickableImageSpan(getYoutubeThumb(video)) {
                    @Override
                    public void onClick() {
                        final Intent i =
                                new Intent(Intent.ACTION_VIEW, Uri
                                        .parse(video.link));
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
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

    private Drawable getImgDrawable(final Attributes attributes) {
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
            int w = -1, h = -1;

            // Calculate size first if possible
            if (sWidth != null && sWidth.contains("%") &&
                    sHeight != null && sHeight.contains("%")) {
                hasPercentSize = true;
            }
            if (sWidth != null && !sWidth.contains("%") &&
                    sHeight != null && !sHeight.contains("%")) {
                hasSize = true;
                w = Integer.parseInt(sWidth);
                h = Integer.parseInt(sHeight);
                // This should be parsed away, but just in case...
                if (w < 10 || h < 10) {
                    return null;
                }
                // Scale with screen density
                w = (int) (w * densityScale + 0.5f);
                h = (int) (h * densityScale + 0.5f);
                // Shrink if big (used for picasso downloading)
                // Don't resize if small, since it can be scaled
                // directly in drawable bounds. Need to shrink it to
                // save precious memory however.
                if (w > maxSize.x) {
                    Point newSize = scaleImage(w, h);
                    w = newSize.x;
                    h = newSize.y;
                    shrunk = true;
                }
            }

            final Bitmap b;
            BitmapRequestBuilder<String, Bitmap> g = GlideUtils.glideAsBitmap(context, source, allowDownload);
            if (shrunk || hasSize) {
                b = g.fitCenter().into(w, h).get();
            } else if (hasPercentSize) {
                b = g.fitCenter().into(maxSize.x, maxSize.y).get();
            } else {
                b = g.fitCenter().into(maxSize.x, maxSize.y).get();
            }

            if (w == -1) {
                w = b.getWidth();
                h = b.getHeight();
                // Scale with screen density
                w = (int) (w * densityScale + 0.5f);
                h = (int) (h * densityScale + 0.5f);
            }
            // Enlarge if close, or shrink if big
            if (((float) w) / ((float) maxSize.x) > 0.5) {
                Point newSize = scaleImage(w, h);
                w = newSize.x;
                h = newSize.y;
            }

            d = new BitmapDrawable(context.getResources(), b);
            d.setBounds(0, 0, w, h);
        } catch (InterruptedException | ExecutionException e) {
            Log.e("ImageTextLoader", "" + e.getMessage());
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
    private Point scaleImage(int w, int h) {
        // Which is out of scale the most?
        float ratio = ((float) w) / ((float) maxSize.x);
        // Calculate new size. Maintains aspect ratio.
        int newWidth = (int) ((float) w / ratio);
        int newHeight = (int) ((float) h / ratio);

        return new Point(newWidth, newHeight);
    }

    /**
     * @return a Drawable with a youtube logo in the center
     */
    private Drawable getYoutubeThumb(final VideoTagHunter.Video video) {
        Drawable[] layers = new Drawable[2];

        int w1, h1;
        try {
            final Bitmap b = GlideUtils.glideAsBitmap(context, video.imageurl, allowDownload)
                    .load(video.imageurl).fitCenter().into(maxSize.x, maxSize.y).get();
            //final Point newSize = scaleImage(b.getWidth(), b.getHeight());
            w1 = b.getWidth();
            h1 = b.getHeight();
            final BitmapDrawable d = new BitmapDrawable(context.getResources(), b);
            // Settings bounds later
            //d.setBounds(0, 0, w1, h1);
            // Set in layer
            layers[0] = d;
        } catch (InterruptedException | ExecutionException e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }

        // Add layer with play icon
        final Drawable playicon = context.getResources().getDrawable(R.drawable.youtube_icon);
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

        // And add to layer
        layers[1] = playicon;
        final LayerDrawable ld = new LayerDrawable(layers);
        // Need to set bounds on outer drawable first as it seems to override
        // child bounds
        ld.setBounds(0, 0, w1, h1);
        // Now set smaller bounds on youtube icon
        playicon.setBounds(left, top, left + w2, top + h2);
        return ld;
    }
}
