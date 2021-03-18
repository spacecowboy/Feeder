package com.nononsenseapps.feeder.ui.text

import android.app.Application
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
import androidx.core.graphics.drawable.toBitmap
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.AsyncImageLoader
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import kotlinx.coroutines.FlowPreview
import org.ccil.cowan.tagsoup.Parser
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import org.xml.sax.Attributes
import java.io.Reader
import java.net.URL
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

@FlowPreview
class CoilConverter(
    kodein: Kodein,
    source: Reader,
    private val siteUrl: URL,
    parser: Parser,
    maxSize: Point,
    spannableStringBuilder: SensibleSpannableStringBuilder = SensibleSpannableStringBuilder(),
    urlClickListener: UrlClickListener?
) : HtmlToSpannedConverter(source, siteUrl, parser, kodein, maxSize, spannableStringBuilder, urlClickListener = urlClickListener) {

    private val context: Application by instance()
    private val asyncImageLoader: AsyncImageLoader by instance()

    private val densityScale: Float

    init {
        // Get screen density
        densityScale = this.context.resources.displayMetrics.density
    }

    override fun startImg(
        text: SensibleSpannableStringBuilder,
        attributes: Attributes
    ) {
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
        val d = getImgBitmapDrawable(src = attributes.getValue("", "src"))
        if (d == null) {
            super.startImg(text, attributes)
            return
        }

        Log.d("FeederCoil", "Img(${d.intrinsicWidth}, ${d.intrinsicHeight}) vs [$width, $height]")

        val len = text.length
        text.append("\uFFFC")

        val imgLink = relativeLinkIntoAbsolute(siteUrl, attributes.getValue("", "src"))

        text.setSpan(
            ImageSpan(d, imgLink),
            len,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Add a line break
        text.append("\n")
        // If there's an alt text, add it in italics
        val alt = attributes.getValue("", "alt")
        if (alt?.isNotEmpty() == true) {
            val from = text.length
            text.append(alt)
            text.setSpan(
                StyleSpan(Typeface.ITALIC), from,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text.append("\n")
        }
    }

    private fun getImgBitmapDrawable(src: String?): BitmapDrawable? {
        try {
            if (src == null || src.isBlank()) {
                return null
            }

            val imgLink = relativeLinkIntoAbsolute(siteUrl, src)

            // Want to return null on errors
            val imgResult = asyncImageLoader[imgLink]

            return imgResult.drawable?.let { drawable ->
                BitmapDrawable(
                    context.resources,
                    drawable.toBitmap()
                ).also {
                    val (drawableWidth, drawableHeight) = drawable.fitCenterScale(maxSize.x, maxSize.y)
                    it.setBounds(0, 0, drawableWidth, drawableHeight)
                }
            }
        } catch (e: InterruptedException) {
            Log.e("FeederCoil", "Failed to show image", e)
        } catch (e: ExecutionException) {
            Log.e("FeederCoil", "Failed to show image", e)
        }

        return null
    }

    /**
     * @return a drawable of the video thumbnail with a youtube icon overlayed, or a full size
     * youtube icon in case the thumbnail could not be loaded
     */
    override fun getYoutubeThumb(video: Video): Drawable {
        return try {
            val imgLink = relativeLinkIntoAbsolute(siteUrl, video.imageUrl)

            val imgResult = asyncImageLoader[imgLink]

            imgResult.drawable
        } catch (e: Throwable) {
            Log.e("FeederCoil", "Error while getting youtube thumb", e)
            null
        }?.let { drawable ->
            val playIcon = AppCompatResources.getDrawable(context, R.drawable.youtube_icon)!!

            // 20% size, in middle
            var w2 = playIcon.intrinsicWidth
            var h2 = playIcon.intrinsicHeight

            val ratio = h2.toDouble() / w2.toDouble()

            val (drawableWidth, drawableHeight) = drawable.fitCenterScale(maxSize.x, maxSize.y)

            // Start with width which is known
            val relSize = 0.2
            w2 = (relSize * drawableWidth).toInt()
            val left = ((drawableWidth - w2).toDouble() / 2.0).toInt()
            // Then height is simple
            h2 = (ratio * w2).toInt()
            val top = ((drawableHeight - h2).toDouble() / 2.0).toInt()

            // Create layer drawable
            LayerDrawable(arrayOf(drawable, playIcon)).also {
                // Need to set bounds on outer drawable first as it seems to override
                // child bounds
                it.setBounds(0, 0, drawableWidth, drawableHeight)
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

fun Drawable.fitCenterScale(maxWidth: Int, maxHeight: Int): Pair<Int, Int> {
    if (intrinsicWidth < 90 || intrinsicHeight < 90) {
        // Try to catch small Email this images and stuff..
        return intrinsicWidth to intrinsicHeight
    }

    val drawableRatio = intrinsicWidth.toDouble() / intrinsicHeight.toDouble()

    val widthRatio = intrinsicWidth.toDouble() / maxWidth.toDouble()

    val newWidth = (intrinsicWidth.toDouble() / widthRatio)
    val newHeight = (newWidth / drawableRatio).roundToInt()

    Log.d("FeederCoil", "($intrinsicWidth, $intrinsicHeight) -> (${newWidth.toInt()}, $newHeight) [$maxWidth, $maxHeight]")

    return newWidth.toInt() to newHeight
}
