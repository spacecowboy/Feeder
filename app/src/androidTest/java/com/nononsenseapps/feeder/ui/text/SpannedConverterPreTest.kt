package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.graphics.Point
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.android.closestKodein
import java.io.StringReader
import java.net.URL


@RunWith(AndroidJUnit4::class)
@MediumTest
class SpannedConverterPreTest {
    private val kodein by closestKodein(getApplicationContext() as Context)

    @Test
    @Throws(Exception::class)
    fun preIsMonospaced() {
        val builder = FakeBuilder2()
        toSpannedWithNoImages(
                kodein,
                StringReader("Some <pre>pre  formatted</pre> text"),
                URL("http://foo.com"),
                Point(100, 100),
                builder,
                null
        )

        assertEquals(1, builder.getAllSpansWithType<TypefaceSpan>().size)
        assertEquals(0, builder.getAllSpansWithType<RelativeSizeSpan>().size)
        assertEquals(0, builder.getAllSpansWithType<BackgroundColorSpan>().size)

        assertTrue(builder.toString().contains("pre  formatted"))
    }

    @Test
    @Throws(Exception::class)
    fun codeIsMonospacedAndMore() {
        val builder = FakeBuilder2()
        toSpannedWithNoImages(
                kodein,
                StringReader("Some <code>code  formatted</code> text"),
                URL("http://foo.com"),
                Point(100, 100),
                builder,
                null
        )

        assertEquals(1, builder.getAllSpansWithType<TypefaceSpan>().size)
        assertEquals(1, builder.getAllSpansWithType<RelativeSizeSpan>().size)
        assertEquals(1, builder.getAllSpansWithType<BackgroundColorSpan>().size)

        assertTrue(builder.toString().contains("code formatted"))
    }

    @Test
    @Throws(Exception::class)
    fun preCodeIsMonospacedAndMore() {
        val builder = FakeBuilder2()
        toSpannedWithNoImages(
                kodein,
                StringReader("Some <pre><code>pre  code  formatted</code></pre> text"),
                URL("http://foo.com"),
                Point(100, 100),
                builder,
                null
        )

        assertEquals(2, builder.getAllSpansWithType<TypefaceSpan>().size)
        assertEquals(1, builder.getAllSpansWithType<RelativeSizeSpan>().size)
        assertEquals(1, builder.getAllSpansWithType<BackgroundColorSpan>().size)

        assertTrue(builder.toString().contains("pre  code  formatted"))
    }
}
