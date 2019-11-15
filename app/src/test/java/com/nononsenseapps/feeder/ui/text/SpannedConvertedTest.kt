package com.nononsenseapps.feeder.ui.text

import android.app.Application
import android.content.res.Resources
import android.graphics.Point
import android.text.style.ImageSpan
import android.text.style.TextAppearanceSpan
import com.nononsenseapps.feeder.util.Prefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import java.io.StringReader
import java.net.URL
import java.util.*

class SpannedConverterTest: KodeinAware {
    private val mockResources: Resources = mockk(relaxed = true)
    //private val mockContext: Context = mockk(relaxed = true)
    private val mockContext: Application = mockk(relaxed = true)
    private val mockPrefs: Prefs = mockk(relaxed = true)

    override val kodein by Kodein.lazy {
        bind<Application>() with singleton { mockContext }
        bind<Prefs>() with singleton { mockPrefs }
    }

    @Suppress("DEPRECATION")
    @Before
    fun setup() {
        every { mockResources.getColor(any()) } returns 0
        every { mockResources.getColor(any(), any()) } returns 0
        every { mockContext.resources } returns mockResources
        every { mockContext.applicationContext } returns mockContext
    }

    @Test
    @Throws(Exception::class)
    fun testFontWithNoColorDoesntCrash() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                kodein,
                StringReader("<font>No color</font>"),
                URL("http://foo.com"),
                Point(100, 100),
                builder,
                null)

        assertEquals(emptyList<ImageSpan>(), builder.getAllSpansWithType<TextAppearanceSpan>())
        assertEquals("No color", builder.toString())
    }

    @Test
    @Throws(Exception::class)
    fun testOnePixelImagesAreNotRenderedWithBase() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                kodein,
                StringReader("<img src=\"https://foo.com/bar.gif\" width=\"1\" height=\"1\">"),
                URL("http://foo.com"),
                Point(100, 100),
                builder,
                null)

        assertEquals(emptyList<ImageSpan>(), builder.getAllSpansWithType<ImageSpan>())
    }

    @Test
    @Throws(Exception::class)
    fun testOnePixelImagesAreNotRenderedWithGlide() {
        val builder = FakeBuilder()
        toSpannedWithImages(
                kodein,
                StringReader("<img src=\"https://foo.com/bar.gif\" width=\"1\" height=\"1\">"),
                URL("http://foo.com"),
                Point(100, 100),
                true,
                builder,
                null
        )

        assertEquals(emptyList<ImageSpan>(), builder.getAllSpansWithType<ImageSpan>())
    }

    @Test
    @Throws(Exception::class)
    fun testNotRenderScriptTag() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                kodein,
                StringReader("<p>foo</p><script>script</script><p>bar</p>"),
                URL("http://foo.bar"),
                Point(100, 100),
                builder,
                null)

        assertEquals("foo\n\nbar\n\n", builder.toString())
    }

    @Test
    @Throws(Exception::class)
    fun testNotRenderStyleTag() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                kodein,
                StringReader("<p>foo</p><style>style</style><p>bar</p>"),
                URL("http://foo.bar"),
                Point(100, 100),
                builder,
                null)

        assertEquals("foo\n\nbar\n\n", builder.toString())
    }

    @Test
    @Throws(Exception::class)
    fun tableColumnsSeparatedNewLinesTest() {
        val builder = FakeBuilder()
        toSpannedWithNoImages(
                kodein,
                StringReader("""
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
                    """),
                URL("http://foo.bar"),
                Point(100, 100),
                builder,
                null)

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
                kodein,
                StringReader("""
                    <pre>$text</pre>
                    """),
                URL("http://foo.bar"),
                Point(100, 100),
                builder,
                null)

        assertEquals(text, builder.toString().trimEnd { it == '\n' })
    }
}

private class FakeBuilder : SensibleSpannableStringBuilder() {
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
