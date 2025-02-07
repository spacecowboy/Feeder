package com.nononsenseapps.text

import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.nononsenseapps.feeder.ui.text.repeated
import org.junit.Test
import kotlin.test.assertEquals

class HtmlToPlainTextConverterTest {
    @Test
    fun repeated() {
        assertEquals("", repeated("*", 0))
        assertEquals("", repeated("*", -1))
        assertEquals("*", repeated("*", 1))
        assertEquals("****", repeated("*", 4))
    }

    @Test
    fun empty() {
        assertEquals("", HtmlToPlainTextConverter().convert(""))
    }

    @Test
    fun unorderedList() {
        assertEquals(
            """
            * one
            * two
            """.trimIndent(),
            HtmlToPlainTextConverter().convert("<ul><li>one</li><li>two</li></ul>"),
        )
    }

    @Test
    fun orderedList() {
        assertEquals(
            """
            1. one
            2. two
            """.trimIndent(),
            HtmlToPlainTextConverter().convert("<ol><li>one</li><li>two</li></ol>"),
        )
    }

    @Test
    fun nestedList() {
        assertEquals(
            """1. sublist:
  * A
  * B
2. two""",
            HtmlToPlainTextConverter().convert("<ol><li>sublist:<ul><li>A</li><li>B</li></ul></li><li>two</li></ol>"),
        )
    }

    @Test
    fun link() {
        assertEquals(
            "go to Google and see.",
            HtmlToPlainTextConverter().convert("go to <a href=\"google.com\">Google</a> and see."),
        )
    }

    @Test
    fun noNewLines() {
        assertEquals(
            "one two three",
            HtmlToPlainTextConverter().convert("<p>one<br>two<p>three"),
        )
    }

    @Test
    fun noScripts() {
        assertEquals(
            "foo bar",
            HtmlToPlainTextConverter().convert("foo <script>script</script> bar"),
        )
    }

    @Test
    fun noStyles() {
        assertEquals(
            "foo bar",
            HtmlToPlainTextConverter().convert("foo <style>style</style> bar"),
        )
    }

    @Test
    fun imgSkipped() {
        assertEquals(
            "foo bar",
            HtmlToPlainTextConverter().convert("foo <img src=\"meh\" alt=\"meh\"> bar"),
        )
    }

    @Test
    fun noBold() {
        assertEquals("foo", HtmlToPlainTextConverter().convert("<b>foo</b>"))
    }

    @Test
    fun noItalic() {
        assertEquals("foo", HtmlToPlainTextConverter().convert("<i>foo</i>"))
    }

    @Test
    fun noLeadingImages() {
        assertEquals(
            "foo",
            HtmlToPlainTextConverter().convert("<img src='bar' alt='heh'> foo"),
        )
    }
}
