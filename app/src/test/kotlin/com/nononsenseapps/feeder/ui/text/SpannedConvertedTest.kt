package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.content.res.Resources
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.net.URL
import java.util.*

class SpannedConverterTest {

    private val mockContext: Context = mock(Context::class.java)
    private val mockResources: Resources = mock(Resources::class.java)

    @Before
    fun setup() {
        `when`(mockResources.getColor(ArgumentMatchers.anyInt())).thenReturn(0)
        `when`(mockResources.getDimension(ArgumentMatchers.anyInt())).thenReturn(10.0f)
        `when`(mockContext.resources).thenReturn(mockResources)
    }

    @Test
    @Throws(Exception::class)
    fun testNotRenderScriptTag() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                "<p>foo</p><script>script</script><p>bar</p>",
                URL("http://foo.bar"),
                mockContext,
                builder
        )

        assertEquals("foo\n\nbar\n\n", builder.toString())
    }

    @Test
    @Throws(Exception::class)
    fun testNotRenderStyleTag() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                "<p>foo</p><style>style</style><p>bar</p>",
                URL("http://foo.bar"),
                mockContext,
                builder
        )

        assertEquals("foo\n\nbar\n\n", builder.toString())
    }

    @Test
    @Throws(Exception::class)
    fun tableColumnsSeparatedNewLinesTest() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
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
                mockContext,
                builder
        )

        assertEquals("r1c1\nr1c2\nr2c1\nr2c2\n\n", builder.toString())
    }

    @Test
    @Throws(Exception::class)
    fun preFormattedTextIsNotDestroyedTest() {
        val builder = FakeBuilder()
        val text = """
            first  line
            second        line here
            don't
            break
            me
            """.trimMargin()
        toSpannedWithNoImages(
                """
                    <pre>$text</pre>
                    """,
                URL("http://foo.bar"),
                mockContext,
                builder
        )

        assertEquals(text, builder.toString())
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
