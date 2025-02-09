package com.nononsenseapps.feeder.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FullTextParserKtTest {
    @Test
    fun `detects gbk from html meta content`() {
        val charset = assertNotNull(findMetaCharset(gb2312))

        assertEquals("gb2312", charset)
    }

    @Test
    fun `detects gbk from html meta charset`() {
        val basicHtml = "<html><head><meta charset=\"gbk\"></head><body></body></html>"
        val charset = assertNotNull(findMetaCharset(basicHtml.toByteArray()))

        assertEquals("gbk", charset)
    }

    @Test
    fun `detects gbk from html meta content semicolon`() {
        val basicHtml =
            "<html><head><meta content=\"charset=gbk; text/html\"></head><body></body></html>"
        val charset = assertNotNull(findMetaCharset(basicHtml.toByteArray()))

        assertEquals("gbk", charset)
    }

    @Test
    fun `guesses gb from html`() {
        val charset = assertNotNull(detectCharset(gb2312))

        assertEquals("GB18030", charset)
    }

    @Test
    fun `finds charset in weird broken turkish article`() {
        val charset = assertNotNull(findMetaCharset(muhasebetreArticle))

        assertEquals("windows-1254", charset)
    }

    private val gb2312: ByteArray
        get() =
            javaClass
                .getResourceAsStream("gb2312.html")!!
                .use { it.readBytes() }

    private val muhasebetreArticle: ByteArray
        get() =
            javaClass
                .getResourceAsStream("muhasebetre_article.html")!!
                .use { it.readBytes() }
}
