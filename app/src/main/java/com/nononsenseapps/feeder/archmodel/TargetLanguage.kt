package com.nononsenseapps.feeder.archmodel

import androidx.annotation.StringRes
import com.nononsenseapps.feeder.R

/**
 * Enum representing target languages for translation.
 * SYSTEM will use the device's current locale.
 */
enum class TargetLanguage(
    @StringRes val stringRes: Int,
    val languageCode: String,
) {
    SYSTEM(R.string.target_language_system, ""),
    ENGLISH(R.string.target_language_english, "en"),
    CHINESE(R.string.target_language_chinese, "zh"),
    JAPANESE(R.string.target_language_japanese, "ja"),
    KOREAN(R.string.target_language_korean, "ko"),
    FRENCH(R.string.target_language_french, "fr"),
    GERMAN(R.string.target_language_german, "de"),
    SPANISH(R.string.target_language_spanish, "es"),
    RUSSIAN(R.string.target_language_russian, "ru"),
    PORTUGUESE(R.string.target_language_portuguese, "pt"),
    ARABIC(R.string.target_language_arabic, "ar"),
}
