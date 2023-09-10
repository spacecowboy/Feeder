package com.nononsenseapps.feeder.ui.text

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import kotlin.test.assertEquals
import org.junit.Test

class KsoupTest {
    @Test
    fun rsquo() {
        val text = "laptop, it&rsquo;s probably"
        val expected = "laptop, it’s probably"

        assertEquals(expected, KsoupEntities.decodeHtml(text))
    }
}
