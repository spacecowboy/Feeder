package com.nononsenseapps.text;

import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter;
import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverterKt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlToPlainTextConverterTest {
    @Test
    public void repeated() throws Exception {
        assertEquals("", HtmlToPlainTextConverterKt.repeated("*", 0));
        assertEquals("", HtmlToPlainTextConverterKt.repeated("*", -1));
        assertEquals("*", HtmlToPlainTextConverterKt.repeated("*", 1));
        assertEquals("****", HtmlToPlainTextConverterKt.repeated("*", 4));
    }

    @Test
    public void empty() throws Exception {
        assertEquals("", HtmlToPlainTextConverter.INSTANCE.convert(""));
    }

    @Test
    public void unorderedList() throws Exception {
        assertEquals("* one\n" +
                "* two", HtmlToPlainTextConverter.INSTANCE.convert("<ul><li>one</li><li>two</li></ul>"));
    }

    @Test
    public void orderedList() throws Exception {
        assertEquals("1. one\n" +
                "2. two", HtmlToPlainTextConverter.INSTANCE.convert("<ol><li>one</li><li>two</li></ol>"));
    }

    @Test
    public void nestedList() throws Exception {
        assertEquals("1. sublist:\n" +
                "  * A\n" +
                "  * B\n" +
                "2. two",
                HtmlToPlainTextConverter.INSTANCE.convert("<ol><li>sublist:<ul><li>A</li><li>B</li></ul></li><li>two</li></ol>"));
    }

    @Test
    public void link() throws Exception {
        assertEquals("go to Google and see.",
                HtmlToPlainTextConverter.INSTANCE.convert("go to <a href=\"google.com\">Google</a> and see."));
    }

    @Test
    public void noNewLines() throws Exception {
        assertEquals("one two three", HtmlToPlainTextConverter.INSTANCE.convert("<p>one<br>two<p>three"));
    }

    @Test
    public void noScripts() throws Exception {
        assertEquals("foo bar",
                HtmlToPlainTextConverter.INSTANCE.convert("foo <script>script</script> bar"));
    }

    @Test
    public void noStyles() throws Exception {
        assertEquals("foo bar",
                HtmlToPlainTextConverter.INSTANCE.convert("foo <style>style</style> bar"));
    }
}
