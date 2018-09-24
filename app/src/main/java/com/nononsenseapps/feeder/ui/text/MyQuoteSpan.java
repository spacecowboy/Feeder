package com.nononsenseapps.feeder.ui.text;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import android.text.Layout;
import android.text.style.QuoteSpan;

/**
 * Configurable properties.
 */
@SuppressLint("ParcelCreator")
public class MyQuoteSpan extends QuoteSpan {

    protected int mColor;
    protected int mStripeWidth;
    protected int mGapWidth;

    /**
     *
     * @param color color of stripe
     */
    public MyQuoteSpan(final int color) {
        // Like parent's defaults
        this(color, 2, 2);
    }

    /**
     * @param color color of stripe
     * @param gapWidth in pixels
     */
    public MyQuoteSpan(final int color, final int gapWidth, final int stripeWidth) {
        super(color);
        mColor = color;
        mGapWidth = gapWidth;
        mStripeWidth = stripeWidth;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return mGapWidth;
    }

    @Override
    public void drawLeadingMargin(@NonNull Canvas c, @NonNull Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(mColor);

        c.drawRect(x, top, x + dir * mStripeWidth, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }
}
