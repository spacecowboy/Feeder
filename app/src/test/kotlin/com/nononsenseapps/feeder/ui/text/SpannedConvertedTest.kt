package com.nononsenseapps.feeder.ui.text

import android.content.Context
import android.content.res.Resources
import android.text.SpannableStringBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.net.URL

class SpannedConverterTest {

    private val mockContext: Context = mock(Context::class.java)
    private val mockResources: Resources = mock(Resources::class.java)
    private val mockBuilder: SpannableStringBuilder = mock(SpannableStringBuilder::class.java)

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
}

class FakeBuilder: SpannableStringBuilder() {
    private val builder: StringBuilder = StringBuilder()

    override fun append(text: CharSequence?): SpannableStringBuilder {
        builder.append(text)
        return this
    }

    override fun get(where: Int): Char {
        return builder[where]
    }

    override val length: Int
        get() = builder.length

    override fun toString(): String {
        return builder.toString()
    }
}
