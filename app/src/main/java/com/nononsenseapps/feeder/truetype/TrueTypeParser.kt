package com.nononsenseapps.feeder.truetype

import java.io.InputStream

/**
 * Provides functions for parsing TrueType font files to determine if it
 * supports variations such as weight and italic.
 *
 * @throws IllegalArgumentException if the input stream is not a valid TrueType font file.
 */

private const val TRUE_TYPE_HEADER = 0x00010000
private val TRUE_TYPE_SCALAR_BYTES = byteArrayOf(0x00, 0x01, 0x00, 0x00)
private const val OFFSET_SUBTABLE_LEN = 12L
private const val TABLE_ENTRY_LEN = 16L
private val FVAR_BYTES = byteArrayOf(0x66, 0x76, 0x61, 0x72)

fun parseTrueTypeFont(inputStream: InputStream): TrueTypeMetadata {
    inputStream.use { stream ->
        var streamOffset = 0L
        val bytes = ByteArray(4)
        var readLen = stream.readNBytes(bytes, 0, 4).also { streamOffset += it }

        // First 4 bytes indicates the scalar type. The values
        // 'true' (0x74727565) and 0x00010000 are recognized by OS X
        // and iOS as referring to TrueType fonts
        if (readLen != 4 || !bytes.contentEquals(TRUE_TYPE_SCALAR_BYTES)) {
            throw IllegalArgumentException("Invalid TrueType font file")
        }

        // Next 2 bytes indicate the number of tables
        readLen = stream.readNBytes(bytes, 0, 2).also { streamOffset += it }
        if (readLen != 2) {
            throw IllegalArgumentException("Invalid TrueType font file")
        }

        // Skip rest of offset subtable
        stream.skip(OFFSET_SUBTABLE_LEN - readLen).also { streamOffset += it }

        val numTables = bytesToInt(bytes, 2)

        var fvarOffset = 0
        var fvarLength = 0

        for (tableIndex in 0 until numTables) {
            // First 4 bytes is the tag
            readLen = stream.readNBytes(bytes, 0, 4).also { streamOffset += it }
            if (readLen != 4) {
                throw IllegalArgumentException("Invalid TrueType font file")
            }

            if (bytes.contentEquals(FVAR_BYTES)) {
                // don't care about checksum (4 bytes)]
                stream.skip(4).also { streamOffset += it }
                // go straight to offset
                readLen = stream.readNBytes(bytes, 0, 4).also { streamOffset += it }
                if (readLen != 4) {
                    throw IllegalArgumentException("Invalid TrueType font file")
                }

                fvarOffset = bytesToInt(bytes, 4)

                // table length
                readLen = stream.readNBytes(bytes, 0, 4).also { streamOffset += it }
                if (readLen != 4) {
                    throw IllegalArgumentException("Invalid TrueType font file")
                }

                fvarLength = bytesToInt(bytes, 4)
            } else {
                // skip
                stream.skip(TABLE_ENTRY_LEN - readLen).also { streamOffset += it }
            }
        }

        // read fvar
        if (fvarOffset > 0 && fvarLength > 0) {

        } else {
            return TrueTypeMetadata("", "", false, false)
        }
    }
}

fun bytesToInt(bytes: ByteArray, numBytes: Int): Int {
    var result = 0
    for (i in 0 until numBytes) {
        result = (result shl 8) or bytes[i].toInt()
    }
    return result
}
