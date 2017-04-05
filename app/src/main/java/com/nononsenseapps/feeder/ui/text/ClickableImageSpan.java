package com.nononsenseapps.feeder.ui.text;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;


/**
 * A clickable image span
 */
public abstract class ClickableImageSpan extends ImageSpan {
    public ClickableImageSpan(final Drawable d) {
        super(d);
    }

    public abstract void onClick();
}
