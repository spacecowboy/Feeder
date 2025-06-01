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
