package com.nononsenseapps.feeder.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.util.Log;


/**
 * A clickable image span
 */
public abstract class ClickableImageSpan extends ImageSpan {
    public ClickableImageSpan(final Drawable d) {
        super(d);
    }

    public abstract void onClick();
}
