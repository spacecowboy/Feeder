package com.nononsenseapps.feeder.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HtmlUtilsKtTest {
    @Test
    fun findsImageInSingleLine() {
        val text = "<summary type=\"html\">&lt;img src=\"https://imgs.xkcd.com/comics/interstellar_asteroid.png\" title=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" alt=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" /&gt;</summary>"
        assertEquals("https://imgs.xkcd.com/comics/interstellar_asteroid.png",
                naiveFindImageLink(text))
    }

    @Test
    fun findsImageInSingleLineSrcFarFromImg() {
        val text = "<summary type=\"html\">&lt;img title=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" alt=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" src=\"https://imgs.xkcd.com/comics/interstellar_asteroid.png\" /&gt;</summary>"
        assertEquals("https://imgs.xkcd.com/comics/interstellar_asteroid.png",
                naiveFindImageLink(text))
    }

    @Test
    fun returnsNullWhenNoImageLink() {
        val text = "<summary type=\"html\">&lt;img title=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" alt=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" /&gt;</summary>"
        assertNull(naiveFindImageLink(text))
    }

    @Test
    fun returnsNullForNull() {
        assertNull(naiveFindImageLink(null))
    }
}
