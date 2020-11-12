package com.nononsenseapps.feeder.ui.text

import android.text.style.URLSpan
import android.view.View

class URLSpanWithListener(link: String, private val listener: UrlClickListener?): URLSpan(link) {
    override fun onClick(widget: View) {
        listener?.invoke(url)
    }
}
