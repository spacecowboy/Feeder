package com.nononsenseapps.feeder.archmodel

import androidx.annotation.StringRes
import com.nononsenseapps.feeder.R

/**
 * Enum representing available translation engines.
 */
enum class TranslationEngine(
    @StringRes val stringRes: Int,
) {
    ML_KIT(R.string.translation_engine_mlkit),
    OPENAI(R.string.translation_engine_openai),
    EXTERNAL_APP(R.string.translation_engine_external),
}
