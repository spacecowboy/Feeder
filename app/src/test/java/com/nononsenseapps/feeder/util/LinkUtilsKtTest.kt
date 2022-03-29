package com.nononsenseapps.feeder.util

import java.net.URL
import kotlin.test.assertEquals
import org.junit.Test

class LinkUtilsKtTest {
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
        assertEquals(URL("http://"), sloppyLinkToStrictURL(""))
    }

    @Test
    fun testRelativeToAbsoluteWithFeedLinkAsBase() {
        assertEquals(
            URL("http://cowboy.com/bob.jpg"),
            relativeLinkIntoAbsoluteOrThrow(URL("http://cowboy.com/feed.xml"), "/bob.jpg")
        )
    }
}
