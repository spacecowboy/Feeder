@file:Suppress("ktlint:standard:property-naming")

package com.nononsenseapps.feeder.archmodel

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.nononsenseapps.feeder.truetype.TrueTypeMetadata
import com.nononsenseapps.feeder.truetype.parseTrueTypeFont
import com.nononsenseapps.feeder.ui.compose.settings.FontSelection
import com.nononsenseapps.feeder.util.FilePathProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class FontStore(
    override val di: DI,
) : DIAware {
    private val filePathProvider: FilePathProvider by instance()
    private val contentResolver: ContentResolver by instance()

    private val _fontOptions = MutableStateFlow(getAllFonts())
    val fontOptions: StateFlow<List<FontSelection>> = _fontOptions.asStateFlow()

    private fun getAllFonts(): List<FontSelection> =
        sequence {
            for (file in filePathProvider.fontsDir.listFiles() ?: emptyArray()) {
                if (file.isFile && file.name.endsWith(".ttf")) {
                    val metadata = getFontMetadata(file) ?: continue

                    val font =
                        FontSelection.UserFont(
                            path = file.name,
                            minWeightValue = metadata.weightVariations?.minValue ?: 0f,
                            maxWeightValue = metadata.weightVariations?.maxValue ?: 0f,
                            minItalicValue = metadata.italicVariations?.minValue ?: 0f,
                            maxItalicValue = metadata.italicVariations?.maxValue ?: 0f,
                        )

                    yield(font)
                }
            }

            yield(FontSelection.SystemDefault)
            yield(FontSelection.Roboto)
            yield(FontSelection.AtkinsonHyperLegible)
        }.toList()

    suspend fun addFont(uri: Uri): FontSelection.UserFont {
        // Get filename from uri
        val filename = getFilename(uri) ?: throw RuntimeException("No filename")

        // Copy the file to the fonts directory
        val fontFile = filePathProvider.fontsDir.resolve(filename)

        if (fontFile.exists()) {
            Log.e("FEEDER_FONT", "File already exists: $filename")
            throw RuntimeException("File already exists")
        }

        return withContext(Dispatchers.IO) {
            // First copy to cache
            val cacheFile = filePathProvider.cacheDir.resolve(filename)
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
            cacheFile.createNewFile()

            contentResolver.openInputStream(uri)?.use { inputStream ->
                val outputStream = FileOutputStream(cacheFile)
                outputStream.use {
                    transferTo(inputStream, outputStream)
                }
            }

            // Then verify font metadata
            val fontMetadata = getFontMetadata(cacheFile)
            if (fontMetadata == null) {
                Log.e("FEEDER_FONT", "Error parsing font file: $filename")
                cacheFile.delete()
                throw RuntimeException("Error parsing font file")
            }

            // File is valid, copy to fonts directory
            fontFile.parentFile?.mkdirs()
            // Copy the file to the fonts directory
            cacheFile.copyTo(fontFile)

            // Then write JSON file
            val userFont =
                FontSelection.UserFont(
                    path = filename,
                    minWeightValue = fontMetadata.weightVariations?.minValue ?: 0f,
                    maxWeightValue = fontMetadata.weightVariations?.maxValue ?: 0f,
                    minItalicValue = fontMetadata.italicVariations?.minValue ?: 0f,
                    maxItalicValue = fontMetadata.italicVariations?.maxValue ?: 0f,
                )

            userFont
        }.also {
            withContext(Dispatchers.IO) {
                _fontOptions.value = getAllFonts()
            }
        }
    }

    suspend fun removeFont(font: FontSelection.UserFont) {
        withContext(Dispatchers.IO) {
            val file = filePathProvider.fontsDir.resolve(font.path)
            if (file.exists()) {
                file.delete()
            }

            _fontOptions.value = getAllFonts()
        }
    }

    private fun getFilename(uri: Uri): String? {
        var filename: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex)
                }
            }
        }

        return filename
    }
}

fun getFontMetadata(file: File): TrueTypeMetadata? =
    try {
        file.inputStream().use {
            parseTrueTypeFont(it)
        }
    } catch (e: Exception) {
        Log.e("FEEDER_FONT", "Error parsing font file", e)
        null
    }

fun transferTo(
    inputStream: InputStream,
    outputStream: OutputStream,
) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var read: Int
    while ((inputStream.read(buffer, 0, DEFAULT_BUFFER_SIZE).also { read = it }) >= 0) {
        outputStream.write(buffer, 0, read)
    }
}

private const val DEFAULT_BUFFER_SIZE = 8192
