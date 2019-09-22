package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.graphics.Point
import android.text.style.ImageSpan
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.android.closestKodein
import java.net.URL


@RunWith(AndroidJUnit4::class)
@MediumTest
class SpannedConverterImageTest {
    private val kodein by closestKodein(getApplicationContext() as Context)

    @Test
    @Throws(Exception::class)
    fun imgGetsPlaceHolderInserted() {
        val builder = FakeBuilder2()
        toSpannedWithNoImages(
                kodein,
                "<img src=\"https://foo.com/bar.gif\">",
                URL("http://foo.com"),
                Point(100, 100),
                builder,
                null
        )

        assertEquals(1, builder.getAllSpansWithType<ImageSpan>().size)
    }

    @Test
    @Throws(Exception::class)
    fun imgWithNoSrcGetsNoPlaceHolder() {
        val builder = FakeBuilder2()
        toSpannedWithNoImages(
                kodein,
                "<img src=\"\">",
                URL("http://foo.com"),
                Point(100, 100),
                builder,
                null
        )

        assertEquals(emptyList<ImageSpan>(), builder.getAllSpansWithType<ImageSpan>())
    }

}

internal class FakeBuilder2 : SensibleSpannableStringBuilder() {
    private val builder: StringBuilder = StringBuilder()
    private val spans: ArrayList<Any?> = ArrayList()

    override fun append(text: CharSequence?): SensibleSpannableStringBuilder {
        builder.append(text)
        return this
    }

    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {
        spans.add(what)
    }

    override fun getAllSpans(): List<Any?> = spans

    override fun get(where: Int): Char {
        return builder[where]
    }

    override val length: Int
        get() = builder.length

    override fun toString(): String {
        return builder.toString()
    }
}
