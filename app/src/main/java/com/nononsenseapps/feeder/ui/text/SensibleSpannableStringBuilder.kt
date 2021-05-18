package com.nononsenseapps.feeder.ui.text

import android.text.SpannableStringBuilder
import android.util.Log

open class SensibleSpannableStringBuilder(
    text: CharSequence = "",
    start: Int = 0,
    end: Int = text.length
) : SpannableStringBuilder(text, start, end) {

    init {
        // TODO why must I access this to prevent a crash?
        // TODO later note: it does not prevent crash at all
        Log.d("FeederSensibleSpannableStringBuilder", "Length is: $length")
    }

    open fun getAllSpans(): List<Any?> =
        getSpans(0, length, Object::class.java).asList()

    inline fun <reified T> getAllSpansWithType(): List<T> =
        getAllSpans().filterIsInstance(T::class.java)
}
