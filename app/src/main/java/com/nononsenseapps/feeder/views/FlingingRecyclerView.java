package com.nononsenseapps.feeder.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * RecyclerView's lack of a FLING_SCROLL_STATE forces me to  extend the class.
 * This also allow several scroll-listeners to be attached.
 */
public class FlingingRecyclerView extends RecyclerView {
    private final ArrayList<OnScrollListener> onScrollListeners = new ArrayList<OnScrollListener>();
    private OnFlingListener onFlingListener = null;
    private OnScrollListener onScrollDelegate = null;

    public FlingingRecyclerView(Context context) {
        super(context);
    }

    public FlingingRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlingingRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnFlingListener(OnFlingListener listener) {
        this.onFlingListener = listener;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        boolean flinging = super.fling(velocityX, velocityY);
        if (onFlingListener != null) {
            onFlingListener.flingStateChange(flinging);
        }
        return flinging;
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        onScrollListeners.add(listener);

        if (onScrollDelegate == null) {
            onScrollDelegate = new OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    for (OnScrollListener listener : onScrollListeners) {
                        listener.onScrollStateChanged(recyclerView, newState);
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    for (OnScrollListener listener : onScrollListeners) {
                        listener.onScrolled(recyclerView, dx, dy);
                    }
                }
            };
            super.setOnScrollListener(onScrollDelegate);
        }
    }

    /**
     * A listener to the change in fling state
     */
    public interface OnFlingListener {
        /**
         * Called when a fling action is initiated. Use normal OnScrollListener to detect end.
         */
        public void flingStateChange(boolean flinging);
    }
}
