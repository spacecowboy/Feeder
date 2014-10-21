package com.nononsenseapps.feeder.util;

import android.content.Context;

import com.nononsenseapps.feeder.R;

/**
 * Simple class to check things regarding phone/tablet size.
 */
public class TabletUtils {

    public static boolean isTablet(final Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static int numberOfFeedColumns(final Context context) {
        return context.getResources().getInteger(R.integer.feedColumns);
    }
}
