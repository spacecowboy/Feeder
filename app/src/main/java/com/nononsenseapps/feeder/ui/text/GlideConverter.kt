package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
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
                     maxSize: Point,
                     private val allowDownload: Boolean,
                     spannableStringBuilder: SensibleSpannableStringBuilder = SensibleSpannableStringBuilder()) :
        HtmlToSpannedConverter(source, siteUrl, parser, context, maxSize, spannableStringBuilder) {

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
        val d = getImgDrawable(
                src = attributes.getValue("", "src"),
                sWidth = attributes.getValue("", "width"),
                sHeight = attributes.getValue("", "height")
        )
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

    private fun getImgDrawable(src: String?, sWidth: String?, sHeight: String?): Drawable? {
        var d: Drawable? = null
        try {
            if (src == null || src.isBlank()) {
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
     * @return a drawable of the video thumbnail with a youtube icon overlayed, or a full size
     * youtube icon in case the thumbnail could not be loaded
     */
    override fun getYoutubeThumb(video: Video): Drawable {
        return try {
            val imgLink = relativeLinkIntoAbsolute(siteUrl, video.imageUrl)
            GlideUtils.glideAsBitmap(context, imgLink, allowDownload)
                    .fitCenter().into(maxSize.x, maxSize.y).get()
        } catch (e: Throwable) {
            null
        }?.let {
            SizedDrawable(
                    drawable = BitmapDrawable(context.resources, it),
                    width = it.width,
                    height = it.height
            )
        }?.let { sizedDrawable ->
            val playIcon = AppCompatResources.getDrawable(context, R.drawable.youtube_icon)!!

            // 20% size, in middle
            var w2 = playIcon.intrinsicWidth
            var h2 = playIcon.intrinsicHeight

            val ratio = h2.toDouble() / w2.toDouble()

            // Start with width which is known
            val relSize = 0.2
            w2 = (relSize * sizedDrawable.width).toInt()
            val left = ((sizedDrawable.width - w2).toDouble() / 2.0).toInt()
            // Then height is simple
            h2 = (ratio * w2).toInt()
            val top = ((sizedDrawable.height - h2).toDouble() / 2.0).toInt()

            // Create layer drawable
            LayerDrawable(arrayOf(sizedDrawable.drawable, playIcon)).also {
                // Need to set bounds on outer drawable first as it seems to override
                // child bounds
                it.setBounds(0, 0, sizedDrawable.width, sizedDrawable.height)
                // Now set smaller bounds on youtube icon
                playIcon.setBounds(left, top, left + w2, top + h2)
            }
        }.or {
            super.getYoutubeThumb(video)
        }
    }
}

fun Drawable?.or(callee: () -> Drawable) =
        this ?: callee()

data class SizedDrawable(
        val drawable: Drawable,
        val width: Int,
        val height: Int
)
