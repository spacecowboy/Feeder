package com.nononsenseapps.feeder.ui.compose.font

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.Repository
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
            is FontSettingsEvent.SetWeightVariation -> setWeightVariation(event.value)
            is FontSettingsEvent.SetItalicVariation -> setItalicVariation(event.value)
            is FontSettingsEvent.AddFont -> addFont(event.uri)
        }
    }

    private fun setWeightVariation(value: Boolean) {
        val userFont = viewState.value.font as? FontSelection.UserFont ?: return

        setFontValues(userFont.copy(hasWeightVariation = value))
    }

    private fun setItalicVariation(value: Boolean) {
        val userFont = viewState.value.font as? FontSelection.UserFont ?: return

        setFontValues(userFont.copy(hasItalicVariation = value))
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

    private val _viewState = MutableStateFlow(FontSettingsState())
    val viewState: StateFlow<FontSettingsState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.font,
                repository.textScale,
                repository.fontOptions,
            ) { font, textScale, fontOptions ->
                FontSettingsState(
                    font = font,
                    fontScale = textScale,
                    fontOptions = fontOptions,
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
)

sealed interface FontSettingsEvent {
    data class SetFont(val font: FontSelection) : FontSettingsEvent
    data class SetFontScale(val fontScale: Float) : FontSettingsEvent
    data class SetWeightVariation(val value: Boolean) : FontSettingsEvent
    data class SetItalicVariation(val value: Boolean) : FontSettingsEvent
    data class AddFont(val uri: Uri) : FontSettingsEvent
}

sealed interface FontSelection {
    val path: String
    val hasWeightVariation: Boolean
    val hasItalicVariation: Boolean
    fun serialize(): String {
        return when (this) {
            is UserFont -> Json.encodeToString(this)
            else -> path
        }
    }

    data object Roboto : FontSelection {
        override val path: String = "bundled/roboto"
        override val hasWeightVariation: Boolean = true
        override val hasItalicVariation: Boolean = true
    }

    data object AtkinsonHyperLegible : FontSelection {
        override val path: String = "bundled/atkinson_hyper_legible"
        override val hasWeightVariation: Boolean = true
        override val hasItalicVariation: Boolean = true
    }

    data object SystemDefault : FontSelection {
        override val path: String = "system/default"
        override val hasWeightVariation: Boolean = true
        override val hasItalicVariation: Boolean = true
    }

    @Serializable
    data class UserFont(
        override val path: String,
        override val hasWeightVariation: Boolean,
        override val hasItalicVariation: Boolean,
    ) : FontSelection {
        fun getFile(filePathProvider: FilePathProvider): File {
            return filePathProvider.fontsDir.resolve(path)
        }
    }

    companion object {
        fun fromString(value: String): FontSelection {
            return when {
                Roboto.path == value -> Roboto
                AtkinsonHyperLegible.path == value -> AtkinsonHyperLegible
                SystemDefault.path == value -> SystemDefault
                else -> {
                    Json.decodeFromString<UserFont>(value)
                }
            }
        }
    }
}
