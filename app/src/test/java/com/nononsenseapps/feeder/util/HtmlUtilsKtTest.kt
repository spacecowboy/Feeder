package com.nononsenseapps.feeder.util

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class HtmlUtilsKtTest {
    @Test
    fun ignoresBase64InlineImages() {
        val text = """<summary type="html">
&lt;img src="data:image/png;base64,iVBORw0KGgoAAA
ANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4
//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU
5ErkJggg==" alt="Red dot" /&gt;</summary>"""
        assertNull(naiveFindImageLink(text))
    }

    @Test
    fun ignoresBase64InlineImagesSingleLine() {
        val text = """<summary type="html">&lt;img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==" alt="Red dot" /&gt;</summary>""".trimMargin()
        assertNull(naiveFindImageLink(text))
    }

    @Test
    fun findsImageInSingleLine() {
        val text = "<summary type=\"html\">&lt;img src=\"https://imgs.xkcd.com/comics/interstellar_asteroid.png\" title=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" alt=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" /&gt;</summary>"
        assertEquals(
            "https://imgs.xkcd.com/comics/interstellar_asteroid.png",
            naiveFindImageLink(text)
        )
    }

    @Test
    fun findsImageInSingleLineSrcFarFromImg() {
        val text = "<summary type=\"html\">&lt;img title=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" alt=\"Every time we detect an asteroid from outside the Solar System, we should immediately launch a mission to fling one of our asteroids back in the direction it came from.\" src=\"https://imgs.xkcd.com/comics/interstellar_asteroid.png\" /&gt;</summary>"
        assertEquals(
            "https://imgs.xkcd.com/comics/interstellar_asteroid.png",
            naiveFindImageLink(text)
        )
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

    @Test
    fun correctlyMatchesKindOfQuote() {
        assertEquals(
            """a'quote""",
            naiveFindImageLink("""<img src="a'quote">""")
        )

        assertEquals(
            """a"quote""",
            naiveFindImageLink("""<img src='a"quote'>""")
        )
    }
}
