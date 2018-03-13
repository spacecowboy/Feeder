package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.text.style.URLSpan
import android.view.View

class URLSpanWithListener(link: String, val listener: ((String, Context) -> Unit)): URLSpan(link) {
    override fun onClick(widget: View?) {
        widget?.context?.let { context ->
            listener(url, context)
        }
    }
}
