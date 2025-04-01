package com.nononsenseapps.feeder.truetype

import org.junit.Assert.*
import kotlin.test.Test

@OptIn(ExperimentalUnsignedTypes::class)
class TrueTypeParserKtTest {
//    @Test
//    fun convertsBytesToIntTrailing() {
//        val bytes = byteArrayOf(0x00, 0x00, 0x00, 0x01)
//        val expected = 1
//        val result = bytesToInt(bytes, 4)
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun convertsBytesToIntLeading() {
//        val bytes = byteArrayOf(0x01, 0x00, 0x03, 0x04)
//        val expected = 1
//        val result = bytesToInt(bytes, 1)
//        assertEquals(expected, result)
//    }
//
//    @Test
//    fun convertsBytesToIntBig() {
//        val bytes = byteArrayOf(-0x28, 0x00)
//        val expected = -40
//        val result = bytesToInt(bytes, 1)
//        assertEquals(expected, result)
//    }

//    @Test
//    fun convertsBytesToUIntBig() {
//        val bytes = byteArrayOf(-0x28, 0x00)
//        val expected = 4294967256L
//        val result = bytesToUInt(bytes, 1)
//        assertEquals(expected, result.toLong())
//    }

    @Test
    fun convertsBytesToUIntBiggerThanSignedBig() {
        val bytes = ubyteArrayOf(0x00u, 0x00u, 0x02u, 0xd8u)
        val expected = 728L
        val result = uBytesToUInt(bytes, 4)
        assertEquals(expected, result.toLong())
    }

    @Test
    fun convertBytesToFixedTypeInt() {
        val bytes = ubyteArrayOf(0x02u, 0xd8u, 0x01u, 0x01u)
        val expected = 728u
        val result = fixedType32ToInt(bytes)
        assertEquals(expected, result)
    }

    @Test
    fun convertBytesToFixedTypeFloat() {
        val bytes = ubyteArrayOf(0x02u, 0xd8u, 0x80u, 0x00u)
        val expected = 728.5f
        val result = fixedType32ToFloat(bytes)
        assertEquals(expected, result)
    }
}
