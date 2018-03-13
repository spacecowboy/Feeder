package com.nononsenseapps.feeder.views;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import com.nononsenseapps.feeder.ui.text.ClickableImageSpan;

/**
 * A textview containing clickable links.
 */
public class LinkedTextView extends TextView {
    public LinkedTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TextView widget = this;
        Object text = widget.getText();
        if (text instanceof Spanned) {
            Spanned buffer = (Spanned) text;

            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP || action == MotionEvent
                    .ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link =
                        buffer.getSpans(off, off, ClickableSpan.class);

                ClickableImageSpan[] image =
                        buffer.getSpans(off, off, ClickableImageSpan.class);

                // Cant click to the right of a span, if the line ends with the span!
                try {
                    if (x > layout.getLineRight(line)) {
                        // Don't call the span
                    } else if (link.length != 0) {
                        if (action == MotionEvent.ACTION_UP) {
                            link[0].onClick(widget);
                        }
                        return true;
                    } else if (image.length != 0) {
                        if (action == MotionEvent.ACTION_UP) {
                            image[0].onClick();
                        }
                        // Don't allow selections on this so always return true
                        return true;
                    }
                } catch(ActivityNotFoundException exception) {
                    // No such activity
                    Log.e("LinkedTextView", exception.getLocalizedMessage());
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
