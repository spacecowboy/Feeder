package com.nononsenseapps.feeder.truetype

import android.util.Log
import com.nononsenseapps.feeder.util.logDebug
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
private val WGHT_BYTES = byteArrayOf(0x77, 0x67, 0x68, 0x74)
private val ITAL_BYTES = byteArrayOf(0x69, 0x74, 0x61, 0x6C)
private val SLNT_BYTES = byteArrayOf(0x73, 0x6C, 0x6E, 0x74)

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
fun parseTrueTypeFont(inputStream: InputStream): TrueTypeMetadata {
    var weightVariations: FloatVariations? = null
    var italicVariations: FloatVariations? = null
    var slantVariations: FloatVariations? = null

    return inputStream.use { stream ->
        var toSkip = 0L
        var streamOffset = 0L
        val bytes = ByteArray(4)
        var skipped = 0L
        var readLen = stream.read(bytes, 0, 4).also { streamOffset += it }

        // First 4 bytes indicates the scalar type. The values
        // 'true' (0x74727565) and 0x00010000 are recognized by OS X
        // and iOS as referring to TrueType fonts
        if (readLen != 4 || !bytes.contentEquals(TRUE_TYPE_SCALAR_BYTES)) {
            throw IllegalArgumentException("Invalid TrueType font file")
        }

        // Next 2 bytes indicate the number of tables
        readLen = stream.read(bytes, 0, 2).also { streamOffset += it }
        if (readLen != 2) {
            throw IllegalArgumentException("Invalid TrueType font file")
        }

        val numTables = uBytesToUInt(bytes.toUByteArray(), 2)

        logDebug("FEEDER_FONT", "numTables: $numTables")

        // Skip rest of offset subtable
        toSkip = OFFSET_SUBTABLE_LEN - streamOffset
        skipped = stream.skip(toSkip).also { streamOffset += it }
        if (skipped != toSkip) {
            throw IllegalArgumentException("Invalid TrueType font file")
        }

        var fvarOffset = 0L
        var fvarLength = 0L

        for (tableIndex in 0.toUInt() until numTables) {
            // First 4 bytes is the tag
            readLen = stream.read(bytes, 0, 4).also { streamOffset += it }
            if (readLen != 4) {
                throw IllegalArgumentException("Invalid TrueType font file")
            }

            if (bytes.contentEquals(FVAR_BYTES)) {
                logDebug("FEEDER_FONT", "Found fvar table")

                // don't care about checksum (4 bytes)]
                skipped = stream.skip(4).also { streamOffset += it }
                if (skipped != 4L) {
                    throw IllegalArgumentException("Invalid TrueType font file")
                }

                // offset
                readLen = stream.read(bytes, 0, 4).also { streamOffset += it }
                if (readLen != 4) {
                    throw IllegalArgumentException("Invalid TrueType font file")
                }
                fvarOffset = uBytesToUInt(bytes.toUByteArray(), 4).toLong()

                logDebug("FEEDER_FONT", "fvarOffset: $fvarOffset, bytes: ${bytes.toHexString()}")

                // table length
                readLen = stream.read(bytes, 0, 4).also { streamOffset += it }
                if (readLen != 4) {
                    throw IllegalArgumentException("Invalid TrueType font file")
                }
                fvarLength = uBytesToUInt(bytes.toUByteArray(), 4).toLong()

                logDebug("FEEDER_FONT", "fvarLength: $fvarLength")

                // Don't care about the rest of the tables
                break
            } else {
                // skip
                toSkip = TABLE_ENTRY_LEN - 4
                skipped = stream.skip(toSkip).also { streamOffset += it }
                if (skipped != toSkip) {
                    throw IllegalArgumentException("Invalid TrueType font file")
                }
            }
        }

        // read fvar
        if (fvarOffset > 0 && fvarLength > 0) {
            // skip to fvar
            logDebug("FEEDER_FONT", "about to skip to fvar. fvarOffset: $fvarOffset, streamOffset: $streamOffset")
            skipped = stream.skip(fvarOffset - streamOffset).also { streamOffset += it }
            if (streamOffset != fvarOffset) {
                Log.e("FEEDER_FONT", "Failed to skip to fvar. fvarOffset: $fvarOffset, streamOffset: $streamOffset, skipped: $skipped")
                throw IllegalArgumentException("Invalid TrueType font file")
            }
            logDebug("FEEDER_FONT", "Skipped to fvar. fvarOffset: $fvarOffset, streamOffset: $streamOffset, skipped: $skipped")

            /*
            The format of the font variations table header is shown in the following table:

            uint16 	majorVersion 	Set to 1.
            uint16 	minorVersion 	Set to 0.
            uint16 	offsetToData 	Offset in bytes from the beginning of the table to the beginning of the first axis data.
            uint16 	countSizePairs 	Axis + instance = 2.
            uint16 	axisCount 	The number of style axes in this font.
            uint16 	axisSize 	The number of bytes in each gxFontVariationAxis record. Set to 20 bytes.
            uint16 	instanceCount 	The number of named instances for the font found in the sfntInstance array.
            uint16 	instanceSize 	The number of bytes in each sfntInstance. See below.
            sfntVariationAxis 	axis[axisCount] 	The font variation axis array.
            sfntInstance 	instance[instanceCount] 	The instance array.
             */

            // Skip to axisCount
            skipped = stream.skip(8).also { streamOffset += it }
            if (skipped != 8L) {
                Log.e("FEEDER_FONT", "Failed to skip to axisCount. streamOffset: $streamOffset, skipped: $skipped")
                throw IllegalArgumentException("")
            }

            // Read axisCount
            readLen = stream.read(bytes, 0, 2).also { streamOffset += it }
            if (readLen != 2) {
                Log.e("FEEDER_FONT", "Failed to read axisCount. streamOffset: $streamOffset, readLen: $readLen")
                throw IllegalArgumentException("Invalid TrueType font file")
            }
            val axisCount = uBytesToUInt(bytes.toUByteArray(), 2)

            logDebug("FEEDER_FONT", "axisCount: $axisCount")

            // read axisSize
            readLen = stream.read(bytes, 0, 2).also { streamOffset += it }
            if (readLen != 2) {
                Log.e("FEEDER_FONT", "Failed to read axisSize. streamOffset: $streamOffset, readLen: $readLen")
                throw IllegalArgumentException("Invalid TrueType font file")
            }
            val axisSize = uBytesToUInt(bytes.toUByteArray(), 2)

            logDebug("FEEDER_FONT", "axisSize: $axisSize")

            // skip to first axis
            skipped = stream.skip(4L).also { streamOffset += it }
            if (skipped != 4L) {
                Log.e("FEEDER_FONT", "Failed to skip to first axis. streamOffset: $streamOffset, skipped: $skipped")
                throw IllegalArgumentException("Invalid TrueType font file")
            }

            for (axisIndex in 0.toUInt() until axisCount) {
                val axisStartOffset = streamOffset

                // read axis tag (4 bytes)
                readLen = stream.read(bytes, 0, 4).also { streamOffset += it }
                if (readLen != 4) {
                    Log.e("FEEDER_FONT", "Failed to read axis tag. streamOffset: $streamOffset, readLen: $readLen")
                    throw IllegalArgumentException("Invalid TrueType font file")
                }
                val axisTag = bytes.copyOf(4)

                logDebug("FEEDER_FONT", "axisTag: ${String(axisTag)}")

                // read min value (4 bytes)
                readLen = stream.read(bytes, 0, 4).also { streamOffset += it }
                if (readLen != 4) {
                    Log.e("FEEDER_FONT", "Failed to read min value. streamOffset: $streamOffset, readLen: $readLen")
                    throw IllegalArgumentException("Invalid TrueType font file")
                }
                // Some are actually signed
                val minValue = fixedType32ToSignedFloat(bytes.toUByteArray())

                logDebug("FEEDER_FONT", "minValue: $minValue, bytes: ${bytes.toHexString()}")

                // read default value (4 bytes)
                readLen = stream.read(bytes, 0, 4).also { streamOffset += it }
                if (readLen != 4) {
                    Log.e("FEEDER_FONT", "Failed to read default value. streamOffset: $streamOffset, readLen: $readLen")
                    throw IllegalArgumentException("Invalid TrueType font file")
                }
                val defaultValue = fixedType32ToSignedFloat(bytes.toUByteArray())
                logDebug("FEEDER_FONT", "defaultValue: $defaultValue, bytes: ${bytes.toHexString()}")

                // read max value (4 bytes)
                readLen = stream.read(bytes, 0, 4).also { streamOffset += it }
                if (readLen != 4) {
                    Log.e("FEEDER_FONT", "Failed to read max value. streamOffset: $streamOffset, readLen: $readLen")
                    throw IllegalArgumentException("Invalid TrueType font file")
                }
                val maxValue = fixedType32ToSignedFloat(bytes.toUByteArray())
                logDebug("FEEDER_FONT", "maxValue: $maxValue, bytes: ${bytes.toHexString()}")

                if (axisTag.contentEquals(WGHT_BYTES)) {
                    weightVariations = FloatVariations(
                        minValue = minValue,
                        defaultValue = defaultValue,
                        maxValue = maxValue,
                    )
                    logDebug("FEEDER_FONT", "Found weight variations: $weightVariations")
                } else if (axisTag.contentEquals(ITAL_BYTES)) {
                    italicVariations = FloatVariations(
                        minValue = minValue,
                        defaultValue = defaultValue,
                        maxValue = maxValue,
                    )
                    logDebug("FEEDER_FONT", "Found italic variations: $italicVariations")
                } else if (axisTag.contentEquals(SLNT_BYTES)) {
                    slantVariations = FloatVariations(
                        minValue = minValue,
                        defaultValue = defaultValue,
                        maxValue = maxValue,
                    )
                    logDebug("FEEDER_FONT", "Found slant variations: $minValue, $defaultValue, $maxValue")
                }

                // skip rest of axis
                toSkip = axisStartOffset + axisSize.toLong() - streamOffset
                skipped = stream.skip(toSkip).also { streamOffset += it }
                if (skipped != toSkip) {
                    Log.e("FEEDER_FONT", "Failed to skip rest of axis. streamOffset: $streamOffset, skipped: $skipped")
                    throw IllegalArgumentException("Invalid TrueType font file")
                }
            }
        }

        TrueTypeMetadata(
            weightVariations = weightVariations,
            italicVariations = italicVariations,
            slantVariations = slantVariations,
        )
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun uBytesToUInt(bytes: UByteArray, numBytes: Int): UInt {
    var result: UInt = 0.toUInt()
    for (i in 0 until numBytes) {
        result = (result shl 8) or bytes[i].toUInt()
    }
    return result
}

@OptIn(ExperimentalUnsignedTypes::class)
fun fixedType32ToUInt(bytes: UByteArray): UInt = uBytesToUInt(bytes, 2)

@OptIn(ExperimentalUnsignedTypes::class)
fun fixedType32ToSignedFloat(bytes: UByteArray): Float {
    var integer: Short = uBytesToUInt(bytes, 2).toShort()
    val sign = if (integer < 0) -1 else 1

    var decimal: UInt = 0.toUInt()
    for (i in 2 until 4) {
        decimal = (decimal shl 8) or bytes[i].toUInt()
    }

    // Convert to float. Sign logic for decimal is unclear as no examples were found\
    return integer.toFloat() + sign * (decimal.toFloat() / 65536.0f)
}

data class FloatVariations(
    val minValue: Float,
    val defaultValue: Float,
    val maxValue: Float,
)
