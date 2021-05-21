package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.graphics.Point
import android.text.style.BulletSpan
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.android.closestDI
import java.io.StringReader
import java.net.URL

@RunWith(AndroidJUnit4::class)
@MediumTest
class SpannedConverterListTest {
    private val di by closestDI(getApplicationContext() as Context)

    @Test
    @Throws(Exception::class)
    fun nakedLiTagIsBulletized() {
        val builder = FakeBuilder2()
        toSpannedWithNoImages(
            di,
            StringReader("Some <li> bullet </li> text"),
            URL("http://foo.com"),
            Point(100, 100),
            builder,
            null
        )

        assertEquals(1, builder.getAllSpansWithType<BulletSpan>().size)
    }
}
