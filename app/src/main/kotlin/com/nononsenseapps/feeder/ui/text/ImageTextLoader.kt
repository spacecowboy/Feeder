package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.graphics.Point
import android.support.v4.content.AsyncTaskLoader
import android.text.Spanned

class ImageTextLoader(context: Context,
                      private val text: String,
                      private val siteUrl: String,
                      private val maxSize: Point,
                      private val allowDownload: Boolean) : AsyncTaskLoader<Spanned>(context) {

    override fun loadInBackground(): Spanned? =
            toSpannedWithImages(context, text, siteUrl, maxSize, allowDownload)

    override fun onStartLoading() {
        forceLoad()
    }

    override fun onStopLoading() {
        cancelLoad()
    }

    override fun onReset() {
        super.onReset()

        // Ensure the loader is stopped
        onStopLoading()
    }
}
