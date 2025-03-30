@file:Suppress("ktlint:standard:property-naming")

package com.nononsenseapps.feeder.archmodel

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile
import com.nononsenseapps.feeder.ui.compose.font.FontSelection
import com.nononsenseapps.feeder.util.FilePathProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.FileOutputStream
import java.io.FilenameFilter

class FontStore(
    override val di: DI,
) : DIAware {
    private val filePathProvider: FilePathProvider by instance()
    private val contentResolver: ContentResolver by instance()

    private val _fontOptions = MutableStateFlow(getAllFonts())
    val fontOptions: StateFlow<List<FontSelection>> = _fontOptions.asStateFlow()

    private fun getAllFonts(): List<FontSelection> {
        return sequence {
            for (file in filePathProvider.fontsDir.listFiles() ?: emptyArray()) {
                if (file.isFile && file.name.endsWith(".json")) {
                    val json = file.readText()
                    yield(FontSelection.fromString(json))
                }
            }

            yield(FontSelection.SystemDefault)
            yield(FontSelection.Roboto)
            yield(FontSelection.AtkinsonHyperLegible)
        }.toList()
    }

    suspend fun addFont(uri: Uri): FontSelection.UserFont {
        // TODO JONAS exceptions

        // Get filename from uri
        var filename: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    filename = cursor.getString(nameIndex)
                }
            }
        }

        if (filename == null) {
            // TODO Exception
            filename = "fallback.ttf"
        }

        // Copy the file to the fonts directory
        val fontFile = filePathProvider.fontsDir.resolve(filename)

        // Check if the file already exists

        if (fontFile.exists()) {
            // TODO Exception
            throw RuntimeException("File already exists")
        }

        return withContext(Dispatchers.IO) {
            fontFile.parentFile?.mkdirs()
            fontFile.createNewFile()

            val outputStream = FileOutputStream(fontFile)

            val inputStream = contentResolver.openInputStream(uri)

            inputStream?.use {
                outputStream.use {
                    inputStream.transferTo(outputStream)
                }
            }

            // Then write JSON file
            val userFont = FontSelection.UserFont(
                path = filename,
                hasWeightVariation = false,
                hasItalicVariation = false,
            )

            val jsonFilename = filename.replace(".ttf", ".json")
            val jsonFile = filePathProvider.fontsDir.resolve(jsonFilename)

            val jsonStream = FileOutputStream(jsonFile)

            jsonStream.use {
                jsonStream.write(userFont.serialize().toByteArray())
            }

            userFont
        }.also {
            _fontOptions.value = getAllFonts()
        }
    }
}
