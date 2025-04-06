package com.nononsenseapps.feeder.truetype

import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalUnsignedTypes::class)
class TrueTypeParserKtTest {
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
        val result = fixedType32ToUInt(bytes)
        assertEquals(expected, result)
    }

    @Test
    fun convertBytesToFixedTypeUSignedFloat() {
        val bytes = ubyteArrayOf(0x02u, 0xd8u, 0x80u, 0x00u)
        val expected = 728.5f
        val result = fixedType32ToSignedFloat(bytes)
        assertEquals(expected, result)
    }

    @Test
    fun convertBytesToFixedTypeSignedFloat() {
        val bytes = ubyteArrayOf(0xffu, 0xf6u, 0x00u, 0x00u)
        val expected = -10f
        val result = fixedType32ToSignedFloat(bytes)
        assertEquals(expected, result)
    }

    @Test
    fun convertBytesToFixedTypeSignedFloatWithDecimal() {
        val bytes = ubyteArrayOf(0xffu, 0xf6u, 0x80u, 0x00u)
        val expected = -10.5f
        val result = fixedType32ToSignedFloat(bytes)
        assertEquals(expected, result)
    }
}
