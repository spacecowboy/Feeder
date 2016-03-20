package com.nononsenseapps.feeder.model;

import com.nononsenseapps.feeder.db.FeedSQL;
import com.nononsenseapps.feeder.function.Function;
import com.nononsenseapps.feeder.function.Supplier;
import com.nononsenseapps.feeder.model.OPMLWriter;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;


public class OPMLWriterTest {

    OPMLWriter writer = new OPMLWriter();

    @Test
    public void escapeAndUnescape() throws Exception {
        final String original = "A \"feeditem\" with id '9' > 0 & < 10";

        String escaped = OPMLWriter.escape(original);

        String unescaped = OPMLWriter.unescape(escaped);

        assertEquals(original, unescaped);
        assertEquals(escaped, OPMLWriter.escape(unescaped));
        assertEquals(original, OPMLWriter.unescape(escaped));
    }

    @Test
    public void shouldEscapeStrings() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writer.writeOutputStream(bos,
                new Supplier<Iterable<String>>() {
                    @Override
                    public Iterable<String> get() {
                        return Collections.singletonList("quoted \"tag\"");
                    }
                },
                new Function<String, Iterable<FeedSQL>>() {
                    @Override
                    public Iterable<FeedSQL> apply(String tag) {
                        ArrayList<FeedSQL> result = new ArrayList<>();
                        result.add(new FeedSQL()
                                .withId(1L)
                                .withNotify(0)
                                .withTag(tag)
                                .withTitle("A \"feeditem\" with id '9' > 0 & < 10")
                                .withUnreadCount(2)
                                .withUrl("http://somedomain.com/rss.xml?format=feed&type=rss"));
                        return result;
                    }
                });
        String output = new String(bos.toByteArray());
        assertEquals(expected, output);
    }

    static final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<opml version=\"1.1\">\n" +
            "  <head>\n" +
            "    <title>Feeder</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <outline title=\"quoted &quot;tag&quot;\" text=\"quoted &quot;tag&quot;\">\n" +
            "      <outline title=\"A &quot;feeditem&quot; with id &apos;9&apos; &gt; 0 " +
            "&amp; &lt; 10\" text=\"A &quot;feeditem&quot; with id &apos;9&apos; &gt; 0 &amp;" +
            " &lt; 10\" type=\"rss\" " +
            "xmlUrl=\"http://somedomain.com/rss.xml?format=feed&amp;type=rss\"/>\n" +
            "    </outline>\n" +
            "  </body>\n" +
            "</opml>";
}