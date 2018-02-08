package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.text.style.ImageSpan
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*

class SpannedConverterTest {

    private val mockResources: Resources = mockk(relaxed = true)
    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setup() {
        every { mockResources.getColor(any()) } returns 0
        every { mockResources.getColor(any(), any()) } returns 0
        every { mockContext.resources } returns mockResources
        every { mockContext.applicationContext } returns mockContext
    }

    @Test
    @Throws(Exception::class)
    fun testOnePixelImagesAreNotRenderedWithBase() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                mockContext,
                "<img src=\"https://foo.com/bar.gif\" width=\"1\" height=\"1\">",
                URL("http://foo.com"),
                builder
        )

        assertEquals(emptyList<ImageSpan>(), builder.getAllSpansWithType<ImageSpan>())
    }

    @Test
    @Throws(Exception::class)
    fun testOnePixelImagesAreNotRenderedWithGlide() {
        val builder = FakeBuilder()
        toSpannedWithImages(
                mockContext,
                "<img src=\"https://foo.com/bar.gif\" width=\"1\" height=\"1\">",
                URL("http://foo.com"),
                Point(100, 100),
                true,
                builder
        )

        assertEquals(emptyList<ImageSpan>(), builder.getAllSpansWithType<ImageSpan>())
    }

    @Test
    @Throws(Exception::class)
    fun testNotRenderScriptTag() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                mockContext,
                "<p>foo</p><script>script</script><p>bar</p>",
                URL("http://foo.bar"),
                builder
        )

        assertEquals("foo\n\nbar\n\n", builder.toString())
    }

    @Test
    @Throws(Exception::class)
    fun testNotRenderStyleTag() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                mockContext,
                "<p>foo</p><style>style</style><p>bar</p>",
                URL("http://foo.bar"),
                builder
        )

        assertEquals("foo\n\nbar\n\n", builder.toString())
    }

    @Test
    @Throws(Exception::class)
    fun tableColumnsSeparatedNewLinesTest() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                mockContext,
                """
                    <table>
                    <tr>
                        <th>r1c1</th>
                        <th>r1c2</th>
                      </tr>
                      <tr>
                        <td>r2c1</td>
                        <td>r2c2</td>
                      </tr>
                    </table>
                    """,
                URL("http://foo.bar"),
                builder
        )

        assertEquals("r1c1\nr1c2\nr2c1\nr2c2\n\n", builder.toString())
    }

    @Test
    @Throws(Exception::class)
    fun preFormattedTextIsNotDestroyedTest() {
        val builder = FakeBuilder()
        val text = """
            |first  line
            |second        line here
            |don't
            |break
            |me
            """.trimMargin()
        toSpannedWithNoImages(
                mockContext,
                """
                    <pre>$text</pre>
                    """,
                URL("http://foo.bar"),
                builder
        )

        assertEquals(text, builder.toString().trimEnd { it == '\n' })
    }
}

class FakeBuilder : SensibleSpannableStringBuilder() {
    private val builder: StringBuilder = StringBuilder()
    private val spans: ArrayList<Any?> = ArrayList()

    override fun append(text: CharSequence?): SensibleSpannableStringBuilder {
        builder.append(text)
        return this
    }

    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {
        spans.add(what)
    }

    override fun getAllSpans(): List<Any?> = spans

    override fun get(where: Int): Char {
        return builder[where]
    }

    override val length: Int
        get() = builder.length

    override fun toString(): String {
        return builder.toString()
    }
}
