package com.nononsenseapps.feeder.ui.text

import android.graphics.drawable.Drawable
import android.text.style.ImageSpan


/**
 * A clickable image span
 */
abstract class ClickableImageSpan(d: Drawable) : ImageSpan(d) {
    abstract fun onClick()
}
