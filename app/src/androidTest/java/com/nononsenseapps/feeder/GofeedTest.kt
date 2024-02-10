package com.nononsenseapps.feeder

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nononsenseapps.feeder.model.gofeed.Experiment
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class GofeedTest {
    @Test
    fun test1() {
        val x = feedergofeed.WrappedGoFeed()

        x.toJson()

        assertEquals("[]", x.authorsJson.decodeToString())

        assertEquals("[]", x.categoriesJson?.decodeToString())
    }

    @Test
    fun test() {
        // Struct
        val x = feedergofeed.EGoFeed()

        // Field in struct
        x.title

        val result = feedergofeed.Feedergofeed.plainFunction()

        assertEquals("foo", result)
    }

    @Test
    fun testParse() {
        val j =
            feedergofeed.Feedergofeed.parseBody(
                """
                <rss version="2.0">
                    <channel>
                        <title>Feed Title</title>
                        <link>http://example.com</link>
                        <description>Feed Description</description>
                        <item>
                            <title>Item Title</title>
                            <link>http://example.com/item</link>
                            <description>Item Description</description>
                        </item>
                    </channel>
                </rss>
                """.trimIndent(),
            )

        assertEquals("Feed Title", j.decodeToString())
    }

    @Test
    fun testExperiment() {
        val e = Experiment()

        val f =
            e.parseBody(
                """
                <rss version="2.0">
                    <channel>
                        <title>Feed Title</title>
                        <link>http://example.com</link>
                        <description>Feed Description</description>
                        <item>
                            <title>Item Title</title>
                            <link>http://example.com/item</link>
                            <description>Item Description</description>
                        </item>
                    </channel>
                </rss>
                """.trimIndent(),
            )

        assertEquals("Feed Title", f?.title)
    }
}
