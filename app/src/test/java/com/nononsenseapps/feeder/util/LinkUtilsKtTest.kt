package com.nononsenseapps.feeder.util

import org.junit.Test
import java.net.MalformedURLException
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinkUtilsKtTest {
    @Test
    fun urlHasNoQueryParamsReturnsTrueWhenNoQuery() {
        val url = URL("https://example.com/path")
        assertTrue(urlHasNoQueryParams(url))
    }

    @Test
    fun urlHasNoQueryParamsReturnsFalseWhenQueryPresent() {
        val url = URL("https://example.com/path?foo=bar")
        assertFalse(urlHasNoQueryParams(url))
    }

    @Test
    fun urlHasNoQueryParamsReturnsTrueWhenQueryIsEmpty() {
        val url = URL("https://example.com/path?")
        assertTrue(urlHasNoQueryParams(url))
    }

    @Test
    fun urlHasNoAuthParamsReturnsTrueWhenNoUserInfo() {
        val url = URL("https://example.com/path")
        assertTrue(urlHasNoAuthParams(url))
    }

    @Test
    fun urlHasNoAuthParamsReturnsFalseWhenUserInfoPresent() {
        val url = URL("https://user:pass@example.com/path")
        assertFalse(urlHasNoAuthParams(url))
    }

    @Test
    fun urlHasNoAuthParamsReturnsTrueWhenUserInfoIsEmpty() {
        val url = URL("https://@example.com/path")
        assertTrue(urlHasNoAuthParams(url))
    }

    @Test
    fun lanLocalAddressMatchesPrivateIpsAndLanHostsOnly() {
        assertTrue(URL("http://192.168.1.20/feed").isLanLocalAddress())
        assertTrue(URL("http://10.1.2.3/a").isLanLocalAddress())
        assertTrue(URL("http://172.16.0.1/").isLanLocalAddress())
        assertTrue(URL("http://172.31.255.255/").isLanLocalAddress())
        assertTrue(URL("http://127.0.0.1/").isLanLocalAddress())
        assertTrue(URL("http://169.254.1.1/").isLanLocalAddress())
        assertTrue(URL("http://100.64.1.2/").isLanLocalAddress())
        assertTrue(URL("http://[fd12:3456::1]/x").isLanLocalAddress())
        assertTrue(URL("http://[fe80::1]/feed").isLanLocalAddress())
        assertTrue(URL("http://localhost:1313/feed").isLanLocalAddress())
        assertTrue(URL("https://nas.local/rss").isLanLocalAddress())
        assertTrue(URL("http://router.lan/feed.xml").isLanLocalAddress())
        assertTrue(URL("http://homeassistant.home/rss").isLanLocalAddress())
        assertFalse(URL("https://example.com/feed").isLanLocalAddress())
        assertFalse(URL("http://8.8.8.8/").isLanLocalAddress())
        assertFalse(URL("http://172.32.0.1/").isLanLocalAddress())
        assertFalse(URL("http://100.128.0.1/").isLanLocalAddress())
        assertFalse(URL("http://[2001:db8::1]/").isLanLocalAddress())
    }

    @Test
    fun testSloppyToStrictAddsRespectsUnknownProtocols() {
        assertFails {
            sloppyLinkToStrictURL("gemini://google.com")
        }
    }

    @Test
    fun testSloppyToStrictAddsHttp() {
        assertEquals(URL("http://google.com"), sloppyLinkToStrictURL("google.com"))
    }

    @Test
    fun testSloppyToStrictWithAlreadyValidLink() {
        assertEquals(URL("https://google.com"), sloppyLinkToStrictURL("https://google.com"))
    }

    @Test
    fun testSloppyToStrictWithEmptyString() {
        assertFailsWith<MalformedURLException> {
            sloppyLinkToStrictURL("")
        }
    }

    @Test
    fun testRelativeToAbsoluteWithFeedLinkAsBase() {
        assertEquals(
            URL("http://cowboy.com/bob.jpg"),
            relativeLinkIntoAbsoluteOrThrow(URL("http://cowboy.com/feed.xml"), "/bob.jpg"),
        )
    }
}
