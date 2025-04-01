package com.nononsenseapps.feeder.ui.compose.font

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.archmodel.getFontMetadata
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.ui.compose.font.FontSelection.SystemDefault
import com.nononsenseapps.feeder.util.FilePathProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File
import kotlin.getValue

class AddFontViewModel(di: DI) : DIAwareViewModel(di) {
    private val repository: Repository by instance()
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()

    fun onEvent(event: FontSettingsEvent) {
        when (event) {
            is FontSettingsEvent.SetFont -> setFontValues(event.font)
            is FontSettingsEvent.SetFontScale -> repository.setTextScale(event.fontScale)
            is FontSettingsEvent.AddFont -> addFont(event.uri)
            is FontSettingsEvent.SetPreviewBold -> previewBoldState.value = event.value
            is FontSettingsEvent.SetPreviewItalic -> previewItalicState.value = event.value
        }
    }

    private fun setFontValues(value: FontSelection) {
        if (value != viewState.value.font) {
            applicationCoroutineScope.launch {
                repository.setFont(value)
            }
        }
    }

    private fun addFont(uri: Uri) {
        applicationCoroutineScope.launch {
            repository.addFont(uri)
        }
    }

    fun onDeleteFont(font: String) {
        // TODO
    }

    private val previewBoldState = MutableStateFlow(false)
    private val previewItalicState = MutableStateFlow(false)

    private val _viewState = MutableStateFlow(FontSettingsState())
    val viewState: StateFlow<FontSettingsState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.font,
                repository.textScale,
                repository.fontOptions,
                previewBoldState,
                previewItalicState,
            ) { font, textScale, fontOptions, previewBold, previewItalic ->
                FontSettingsState(
                    font = font,
                    fontScale = textScale,
                    fontOptions = fontOptions,
                    previewBold = previewBold,
                    previewItalic = previewItalic,
                )
            }.collect { state ->
                _viewState.value = state

            }
        }
    }
}

data class FontSettingsState(
    val fontOptions: List<FontSelection> = listOf(SystemDefault),
    val font: FontSelection = SystemDefault,
    val fontScale: Float = 1f,
    val previewBold: Boolean = false,
    val previewItalic: Boolean = false,
)

sealed interface FontSettingsEvent {
    data class SetFont(val font: FontSelection) : FontSettingsEvent
    data class SetFontScale(val fontScale: Float) : FontSettingsEvent
    data class AddFont(val uri: Uri) : FontSettingsEvent
    data class SetPreviewBold(val value: Boolean) : FontSettingsEvent
    data class SetPreviewItalic(val value: Boolean) : FontSettingsEvent
}

sealed interface FontSelection {
    val path: String
    val hasWeightVariation: Boolean
    val hasItalicVariation: Boolean
    val minWeightValue: Float
    val maxWeightValue: Float
    val minItalicValue: Float
    val maxItalicValue: Float
    fun serialize(): String {
        return when (this) {
//            is UserFont -> Json.encodeToString(this)
            else -> path
        }
    }

    data object Roboto : FontSelection {
        override val path: String = "bundled/roboto"
        override val hasWeightVariation: Boolean = true
        override val hasItalicVariation: Boolean = true

        override val minWeightValue: Float = 100f
        override val maxWeightValue: Float = 900f

        override val minItalicValue: Float = 0f
        override val maxItalicValue: Float = 1f
    }

    data object AtkinsonHyperLegible : FontSelection {
        override val path: String = "bundled/atkinson_hyper_legible"
        override val hasWeightVariation: Boolean = true
        override val hasItalicVariation: Boolean = true

        override val minWeightValue: Float = 100f
        override val maxWeightValue: Float = 900f

        override val minItalicValue: Float = 0f
        override val maxItalicValue: Float = 1f
    }

    data object SystemDefault : FontSelection {
        override val path: String = "system/default"
        override val hasWeightVariation: Boolean = true
        override val hasItalicVariation: Boolean = true

        override val minWeightValue: Float = 100f
        override val maxWeightValue: Float = 900f

        override val minItalicValue: Float = 0f
        override val maxItalicValue: Float = 1f
    }

//    @Serializable
    data class UserFont(
        override val path: String,
        override val minWeightValue: Float = 0f,
        override val maxWeightValue: Float = 0f,
        override val minItalicValue: Float = 0f,
        override val maxItalicValue: Float = 0f,
    ) : FontSelection {
        override val hasWeightVariation: Boolean
            get() = minWeightValue > 0f && maxWeightValue > minWeightValue

        override val hasItalicVariation: Boolean
            get() = minItalicValue >= 0f && maxItalicValue > minItalicValue

        fun getFile(filePathProvider: FilePathProvider): File {
            return filePathProvider.fontsDir.resolve(path)
        }
    }

//    companion object {
//        private val format = Json { ignoreUnknownKeys = true }
//
//        fun fromString(value: String): FontSelection {
//            return when {
//                Roboto.path == value -> Roboto
//                AtkinsonHyperLegible.path == value -> AtkinsonHyperLegible
//                SystemDefault.path == value -> SystemDefault
//                else -> {
//                    format.decodeFromString<UserFont>(value)
//                }
//            }
//        }
//    }
}

fun getFontSelectionFromPath(filePathProvider: FilePathProvider, path: String): FontSelection? {
    return when (path) {
        FontSelection.Roboto.path -> FontSelection.Roboto
        FontSelection.AtkinsonHyperLegible.path -> FontSelection.AtkinsonHyperLegible
        FontSelection.SystemDefault.path -> FontSelection.SystemDefault
        else -> {
            val fontFile = filePathProvider.fontsDir.resolve(path)
            if (!fontFile.exists()) {
                Log.e("FontSelection", "Font file does not exist: $path")
                return null
            }

            val metadata = getFontMetadata(fontFile)
            if (metadata == null) {
                Log.e("FontSelection", "Error parsing font file: $path")
                return null
            }

            FontSelection.UserFont(
                path = path,
                minWeightValue = metadata.weightVariations?.minValue ?: 0f,
                maxWeightValue = metadata.weightVariations?.maxValue ?: 0f,
                minItalicValue = metadata.italicVariations?.minValue ?: 0f,
                maxItalicValue = metadata.italicVariations?.maxValue ?: 0f,
            )
        }
    }
}
