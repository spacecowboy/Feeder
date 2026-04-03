package com.nononsenseapps.feeder.util

import org.junit.Test
import kotlin.test.assertEquals

class UrlCleanerKtTest {
    @Test
    fun stripsUtmParameters() {
        val url = "https://example.com/article?utm_source=rss&utm_medium=feed&id=123"
        assertEquals("https://example.com/article?id=123", stripTrackingParameters(url))
    }

    @Test
    fun stripsTrafficSource() {
        val url = "https://example.com/article?traffic_source=rss"
        assertEquals("https://example.com/article", stripTrackingParameters(url))
    }

    @Test
    fun preservesNonTrackingParams() {
        val url = "https://example.com/search?q=feeder&page=2"
        assertEquals(url, stripTrackingParameters(url))
    }

    @Test
    fun returnsOriginalWhenNoQueryString() {
        val url = "https://example.com/article"
        assertEquals(url, stripTrackingParameters(url))
    }

    @Test
    fun returnsMalformedUrlUnchanged() {
        val url = "not a url"
        assertEquals(url, stripTrackingParameters(url))
    }
}
