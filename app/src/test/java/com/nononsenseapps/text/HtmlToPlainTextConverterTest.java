package com.nononsenseapps.text;

import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlToPlainTextConverterTest {
    @Test
    public void repeated() throws Exception {
        assertEquals("", HtmlToPlainTextConverter.repeated("*", 0));
        assertEquals("", HtmlToPlainTextConverter.repeated("*", -1));
        assertEquals("*", HtmlToPlainTextConverter.repeated("*", 1));
        assertEquals("****", HtmlToPlainTextConverter.repeated("*", 4));
    }

    @Test
    public void empty() throws Exception {
        HtmlToPlainTextConverter converter = new HtmlToPlainTextConverter("");
        assertEquals("", converter.convert());
    }

    @Test
    public void unorderedList() throws Exception {
        HtmlToPlainTextConverter converter = new HtmlToPlainTextConverter("<ul><li>one</li><li>two</li></ul>");
        assertEquals("* one\n" +
                "* two\n", converter.convert());
    }

    @Test
    public void orderedList() throws Exception {
        HtmlToPlainTextConverter converter = new HtmlToPlainTextConverter("<ol><li>one</li><li>two</li></ol>");
        assertEquals("1. one\n" +
                "2. two\n", converter.convert());
    }

    @Test
    public void nestedList() throws Exception {
        HtmlToPlainTextConverter converter = new HtmlToPlainTextConverter("<ol><li>sublist:<ul><li>A</li><li>B</li></ul></li><li>two</li></ol>");
        assertEquals("1. sublist:\n" +
                "  * A\n" +
                "  * B\n" +
                "2. two\n", converter.convert());
    }

    @Test
    public void link() throws Exception {
        HtmlToPlainTextConverter converter = new HtmlToPlainTextConverter("go to <a href=\"google.com\">Google</a> and see.");
        assertEquals("go to [Google](google.com) and see.", converter.convert());
    }
}
