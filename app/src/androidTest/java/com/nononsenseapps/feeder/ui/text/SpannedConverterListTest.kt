package com.nononsenseapps.feeder.ui.text

import android.graphics.Point
import android.text.style.BulletSpan
import android.text.style.ImageSpan
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL


@RunWith(AndroidJUnit4::class)
@MediumTest
class SpannedConverterListTest {

    @Test
    @Throws(Exception::class)
    fun nakedLiTagIsBulletized() {
        val builder = FakeBuilder2()
        toSpannedWithNoImages(
                getApplicationContext(),
                "Some <li> bullet </li> text",
                URL("http://foo.com"),
                Point(100, 100),
                builder,
                null
        )

        assertEquals(1, builder.getAllSpansWithType<BulletSpan>().size)
    }

}
