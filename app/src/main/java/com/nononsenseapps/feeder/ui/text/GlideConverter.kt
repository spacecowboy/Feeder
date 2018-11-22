package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.text.Spannable
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.GlideUtils
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import org.ccil.cowan.tagsoup.Parser
import org.xml.sax.Attributes
import java.net.URL
import java.util.concurrent.ExecutionException

class GlideConverter(context: Context,
                     source: String,
                     private val siteUrl: URL,
                     parser: Parser,
                     private val maxSize: Point,
                     private val allowDownload: Boolean,
                     spannableStringBuilder: SensibleSpannableStringBuilder = SensibleSpannableStringBuilder()) :
        HtmlToSpannedConverter(source, siteUrl, parser, context, spannableStringBuilder) {

    private val context: Context = context.applicationContext

    private val densityScale: Float

    init {
        // Get screen density
        densityScale = this.context.resources.displayMetrics.density
    }

    override fun startImg(text: SensibleSpannableStringBuilder,
                          attributes: Attributes) {
        val width: String? = attributes.getValue("", "width")
        val height: String? = attributes.getValue("", "height")

        var shouldIgnore = false

        if (width != null) {
            try {
                if (width.toInt() < 2) {
                    shouldIgnore = true
                }
            } catch (_: NumberFormatException) {
                shouldIgnore = true
            }
        }
        if (height != null) {
            try {
                if (height.toInt() < 2) {
                    shouldIgnore = true
                }
            } catch (_: NumberFormatException) {
                shouldIgnore = true
            }
        }

        if (shouldIgnore) {
            super.startImg(text, attributes)
            return
        }

        // Get drawable
        val d = getImgDrawable(attributes)
        if (d == null) {
            super.startImg(text, attributes)
            return
        }

        val len = text.length
        text.append("\uFFFC")

        val imgLink = relativeLinkIntoAbsolute(siteUrl, attributes.getValue("", "src"))

        text.setSpan(ImageSpan(d, imgLink), len,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Add a line break
        text.append("\n")
        // If there's an alt text, add it in italics
        val alt = attributes.getValue("", "alt")
        if (alt != null && !alt.isEmpty()) {
            val from = text.length
            text.append(alt)
            text.setSpan(StyleSpan(Typeface.ITALIC), from,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.append("\n")
        }
    }

    override fun startIframe(text: SensibleSpannableStringBuilder,
                             attributes: Attributes) {
        // Parse information
        val video = VideoTagHunter
                .getVideo(attributes.getValue("", "src"),
                        attributes.getValue("", "width"),
                        attributes.getValue("", "height"))

        if ((video.src ?: "").toLowerCase().contains("youtube")) {
            try {
                video.imageurl?.let { imageUrl ->
                    val span = object : ClickableImageSpan(getYoutubeThumb(imageUrl)) {
                        override fun onClick() {
                            val i = Intent(Intent.ACTION_VIEW, Uri
                                    .parse(video.link))
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(i)
                        }
                    }
                    val len = text.length
                    text.append("\uFFFC")
                    text.setSpan(span, len, text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    // Add newline also
                    text.append("\n")
                    val from = text.length
                    text.append("Touch to play video")
                    text.setSpan(StyleSpan(Typeface.ITALIC), from,
                            text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    text.append("\n\n")
                }
            } catch (e: NullPointerException) {
                // Error is already logged..
            }
        }
    }

    private fun getImgDrawable(attributes: Attributes): Drawable? {
        var d: Drawable? = null
        try {
            // Source
            val src: String? = attributes.getValue("", "src")
            val sWidth: String? = attributes.getValue("", "width")
            val sHeight: String? = attributes.getValue("", "height")

            if (src == null) {
                return null
            }

            // Image size
            var shrunk = false
            var hasSize = false
            var hasPercentSize = false
            var w = -1
            var h = -1

            // Calculate size first if possible
            if (sWidth != null && sWidth.contains("%") &&
                    sHeight != null && sHeight.contains("%")) {
                hasPercentSize = true
            }
            if (sWidth != null && !sWidth.contains("%") &&
                    sHeight != null && !sHeight.contains("%")) {
                hasSize = true
                w = Integer.parseInt(sWidth)
                h = Integer.parseInt(sHeight)
                // This should be parsed away, but just in case...
                if (w < 10 || h < 10) {
                    return null
                }
                // Scale with screen density
                w = (w * densityScale + 0.5f).toInt()
                h = (h * densityScale + 0.5f).toInt()
                // Shrink if big (used for picasso downloading)
                // Don't resize if small, since it can be scaled
                // directly in drawable bounds. Need to shrink it to
                // save precious memory however.
                if (w > maxSize.x) {
                    val newSize = scaleImage(w, h)
                    w = newSize.x
                    h = newSize.y
                    shrunk = true
                }
            }

            val imgLink = relativeLinkIntoAbsolute(siteUrl, src)

            val g = GlideUtils.glideAsBitmap(context, imgLink, allowDownload)
            val b: Bitmap = if (shrunk || hasSize) {
                g.fitCenter().into(w, h).get()
            } else if (hasPercentSize) {
                g.fitCenter().into(maxSize.x, maxSize.y).get()
            } else {
                g.fitCenter().into(maxSize.x, maxSize.y).get()
            }

            if (w == -1) {
                w = b.width
                h = b.height
                // Scale with screen density
                w = (w * densityScale + 0.5f).toInt()
                h = (h * densityScale + 0.5f).toInt()
            }
            // Enlarge if close, or shrink if big
            if (w.toFloat() / maxSize.x.toFloat() > 0.5) {
                val newSize = scaleImage(w, h)
                w = newSize.x
                h = newSize.y
            }

            d = BitmapDrawable(context.resources, b)
            d.setBounds(0, 0, w, h)
        } catch (e: InterruptedException) {
            Log.e("ImageTextLoader", "" + e.message)
        } catch (e: ExecutionException) {
            Log.e("ImageTextLoader", "" + e.message)
        }

        return d
    }


    /**
     * Keeps aspect ratio.
     *
     * @param w current width of image
     * @param h current height of image
     * @return scaled (width, height) of image to fit the intended maxSize
     */
    private fun scaleImage(w: Int, h: Int): Point {
        // Which is out of scale the most?
        val ratio = w.toFloat() / maxSize.x.toFloat()
        // Calculate new size. Maintains aspect ratio.
        val newWidth = (w.toFloat() / ratio).toInt()
        val newHeight = (h.toFloat() / ratio).toInt()

        return Point(newWidth, newHeight)
    }

    /**
     * @return a Drawable with a youtube logo in the center
     */
    private fun getYoutubeThumb(imageurl: String): Drawable {
        val layers = arrayOfNulls<Drawable>(2)

        val w1: Int
        val h1: Int
        try {
            val imgLink = relativeLinkIntoAbsolute(siteUrl, imageurl)
            val b = GlideUtils.glideAsBitmap(context, imgLink, allowDownload)
                    .fitCenter().into(maxSize.x, maxSize.y).get()
            //final Point newSize = scaleImage(b.getWidth(), b.getHeight());
            w1 = b.width
            h1 = b.height
            val d = BitmapDrawable(context.resources, b)
            // Settings bounds later
            //d.setBounds(0, 0, w1, h1);
            // Set in layer
            layers[0] = d
        } catch (e: InterruptedException) {
            throw NullPointerException(e.localizedMessage)
        } catch (e: ExecutionException) {
            throw NullPointerException(e.localizedMessage)
        }

        // Add layer with play icon
        val playicon = AppCompatResources.getDrawable(context, R.drawable.youtube_icon)!!
        // 20% size, in middle
        var w2 = playicon.intrinsicWidth
        var h2 = playicon.intrinsicHeight

        val ratio = h2.toDouble() / w2.toDouble()

        // Start with width which is known
        val relSize = 0.2
        w2 = (relSize * w1).toInt()
        val left = ((w1 - w2).toDouble() / 2.0).toInt()
        // Then height is simple
        h2 = (ratio * w2).toInt()
        val top = ((h1 - h2).toDouble() / 2.0).toInt()

        // And add to layer
        layers[1] = playicon
        val ld = LayerDrawable(layers)
        // Need to set bounds on outer drawable first as it seems to override
        // child bounds
        ld.setBounds(0, 0, w1, h1)
        // Now set smaller bounds on youtube icon
        playicon.setBounds(left, top, left + w2, top + h2)
        return ld
    }
}
