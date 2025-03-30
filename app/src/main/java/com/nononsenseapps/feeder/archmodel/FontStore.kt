@file:Suppress("ktlint:standard:property-naming")

package com.nononsenseapps.feeder.archmodel

import com.nononsenseapps.feeder.ui.compose.font.FontSelection
import com.nononsenseapps.feeder.util.FilePathProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.FilenameFilter

class FontStore(
    override val di: DI,
) : DIAware {
    private val filePathProvider: FilePathProvider by instance()

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
}
