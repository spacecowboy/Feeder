package com.nononsenseapps.feeder.ui.text

import android.text.SpannableStringBuilder

open class SensibleSpannableStringBuilder(text: CharSequence = "",
                                     start: Int = 0,
                                     end: Int = text.length) : SpannableStringBuilder(text, start, end) {

    open fun getAllSpans(): List<Any?> =
            getSpans(0, length, Object::class.java).asList()

    inline fun <reified T> getAllSpansWithType(): List<T> =
            getAllSpans().filterIsInstance(T::class.java)
}
