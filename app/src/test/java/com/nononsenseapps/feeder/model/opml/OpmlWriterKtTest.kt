package com.nononsenseapps.feeder.model.opml

import com.nononsenseapps.feeder.db.DbFeed
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream

class OpmlWriterKtTest {
    @Test
    fun escapeAndUnescape() {
        val original = "A \"feeditem\" with id '9' > 0 & < 10"

        val escaped = escape(original)
        val unescaped = unescape(escaped)

        assertEquals(original, unescaped)
        assertEquals(escaped, escape(unescaped))
        assertEquals(original, unescape(escaped))
    }

    @Test
    fun shouldEscapeStrings() {
        val bos = ByteArrayOutputStream()
        writeOutputStream(bos, listOf("quoted \"tag\""), { tag ->
            val result = ArrayList<DbFeed>()
            val feed = object : DbFeed {
                override fun getId(): Long {
                    return 1L
                }

                override fun getNotify(): Int {
                    return 0
                }

                override fun getTag(): String? {
                    return tag
                }

                override fun getTitle(): String {
                    return "A \"feeditem\" with id '9' > 0 & < 10"
                }

                override fun getCustomTitle(): String {
                    return "A \"feeditem\" with id '9' > 0 & < 10"
                }

                override fun getUnreadCount(): Int {
                    return 2
                }

                override fun getUrl(): String {
                    return "http://somedomain.com/rss.xml?format=feed&type=rss"
                }
            }

            result.add(feed)
            result
        })
        val output = String(bos.toByteArray())
        assertEquals(expected, output)
    }

    private val expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<opml version=\"1.1\">\n" +
            "  <head>\n" +
            "    <title>\n" +
            "      Feeder\n" +
            "    </title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <outline title=\"quoted &quot;tag&quot;\" text=\"quoted &quot;tag&quot;\">\n" +
            "      <outline title=\"A &quot;feeditem&quot; with id &apos;9&apos; &gt; 0 " +
            "&amp; &lt; 10\" text=\"A &quot;feeditem&quot; with id &apos;9&apos; &gt; 0 &amp;" +
            " &lt; 10\" type=\"rss\" " +
            "xmlUrl=\"http://somedomain.com/rss.xml?format=feed&amp;type=rss\">\n" +
            "      </outline>\n" +
            "    </outline>\n" +
            "  </body>\n" +
            "</opml>\n"
}
