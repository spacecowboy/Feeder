package com.nononsenseapps.feeder.util

import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.URL
import java.net.UnknownHostException
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
    fun resolvedHostnamesPointingAtLanAddressesAreLanLocal() =
        runBlocking {
            val lookups = mutableListOf<String>()
            val resolve = { host: String ->
                lookups += host
                when (host) {
                    "nas" -> arrayOf(InetAddress.getByName("192.168.1.10"))
                    "feeds.example.com" -> arrayOf(InetAddress.getByName("2001:db8::1"), InetAddress.getByName("10.0.0.5"))
                    "example.com" -> arrayOf(InetAddress.getByName("93.184.215.14"))
                    else -> throw UnknownHostException(host)
                }
            }
            assertTrue(URL("http://nas/feed.xml").resolvesToLanLocalAddress(resolve))
            assertTrue(URL("https://feeds.example.com/rss").resolvesToLanLocalAddress(resolve))
            assertFalse(URL("https://example.com/feed").resolvesToLanLocalAddress(resolve))
            assertFalse(URL("https://missing.example/feed").resolvesToLanLocalAddress(resolve))
            // Literal LAN hosts and IP literals never need a lookup
            assertTrue(URL("http://router.lan/feed").resolvesToLanLocalAddress(resolve))
            assertFalse(URL("http://8.8.8.8/").resolvesToLanLocalAddress(resolve))
            assertEquals(listOf("nas", "feeds.example.com", "example.com", "missing.example"), lookups)
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
