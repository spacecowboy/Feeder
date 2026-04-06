package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.openai.isDeepL
import org.jsoup.Jsoup
import java.util.Locale

private const val MAX_LANGUAGE_DETECTION_TEXT_LENGTH = 4000
private const val MIN_LANGUAGE_DETECTION_LETTERS = 20

private data class ComparableTranslationLanguage(
    val language: String,
    val region: String? = null,
)

internal fun prepareTextForLanguageDetection(
    content: String,
    preserveHtml: Boolean,
): String =
    (if (preserveHtml) {
        Jsoup.parse(content).text()
    } else {
        content
    }).replace(Regex("\\s+"), " ")
        .trim()
        .take(MAX_LANGUAGE_DETECTION_TEXT_LENGTH)

internal fun hasEnoughTextForLanguageDetection(content: String): Boolean =
    content.count(Char::isLetter) >= MIN_LANGUAGE_DETECTION_LETTERS

internal fun detectedLanguageMatchesTranslationTarget(
    detectedLanguage: String,
    targetLanguage: String,
    settings: OpenAISettings,
): Boolean {
    val detected = detectedLanguage.asComparableDetectedLanguage() ?: return false
    val target = targetLanguage.asComparableTranslationTarget(settings) ?: return false

    return detected.language == target.language &&
        (target.region == null || detected.region == target.region)
}

private fun String.asComparableDetectedLanguage(): ComparableTranslationLanguage? {
    val normalized = trim().replace('_', '-')
    if (normalized.isBlank()) {
        return null
    }

    val locale = Locale.forLanguageTag(normalized)
    val language =
        locale.language
            .takeUnless { it.isBlank() || it == "und" }
            ?: normalized.substringBefore('-').takeIf { it.isNotBlank() }
            ?: return null

    val region =
        locale.country
            .takeIf { it.isNotBlank() }
            ?: normalized
                .split('-')
                .drop(1)
                .firstOrNull { it.length == 2 || it.length == 3 }

    return ComparableTranslationLanguage(
        language = language.toCanonicalLanguageCode(),
        region = region?.uppercase(Locale.ROOT),
    )
}

private fun String.asComparableTranslationTarget(settings: OpenAISettings): ComparableTranslationLanguage? {
    val normalized = trim().replace('-', '_').uppercase(Locale.ROOT)
    if (normalized.isBlank()) {
        return null
    }

    return when {
        settings.isDeepL -> normalized.asDeepLComparableTranslationTarget()
        else -> normalized.asGenericComparableTranslationTarget()
    }
}

private fun String.asDeepLComparableTranslationTarget(): ComparableTranslationLanguage? =
    when (this) {
        "ENGLISH", "EN" -> ComparableTranslationLanguage(language = "en")
        "EN_GB", "ENGLISH_UK", "ENGLISH_GB", "BRITISH_ENGLISH" ->
            ComparableTranslationLanguage(language = "en", region = "GB")

        "EN_US", "ENGLISH_US", "AMERICAN_ENGLISH" ->
            ComparableTranslationLanguage(language = "en", region = "US")

        "GERMAN", "DE" -> ComparableTranslationLanguage(language = "de")
        "FRENCH", "FR" -> ComparableTranslationLanguage(language = "fr")
        "SPANISH", "ES" -> ComparableTranslationLanguage(language = "es")
        "PORTUGUESE", "PT" -> ComparableTranslationLanguage(language = "pt")
        "PT_BR", "PORTUGUESE_BR", "BRAZILIAN_PORTUGUESE" ->
            ComparableTranslationLanguage(language = "pt", region = "BR")

        "PT_PT", "PORTUGUESE_PT", "EUROPEAN_PORTUGUESE" ->
            ComparableTranslationLanguage(language = "pt", region = "PT")

        "ITALIAN", "IT" -> ComparableTranslationLanguage(language = "it")
        "DUTCH", "NL" -> ComparableTranslationLanguage(language = "nl")
        "POLISH", "PL" -> ComparableTranslationLanguage(language = "pl")
        "RUSSIAN", "RU" -> ComparableTranslationLanguage(language = "ru")
        "JAPANESE", "JA" -> ComparableTranslationLanguage(language = "ja")
        "CHINESE", "ZH" -> ComparableTranslationLanguage(language = "zh")
        "CZECH", "CS" -> ComparableTranslationLanguage(language = "cs")
        "DANISH", "DA" -> ComparableTranslationLanguage(language = "da")
        "GREEK", "EL" -> ComparableTranslationLanguage(language = "el")
        "FINNISH", "FI" -> ComparableTranslationLanguage(language = "fi")
        "HUNGARIAN", "HU" -> ComparableTranslationLanguage(language = "hu")
        "INDONESIAN", "ID" -> ComparableTranslationLanguage(language = "id")
        "KOREAN", "KO" -> ComparableTranslationLanguage(language = "ko")
        "LITHUANIAN", "LT" -> ComparableTranslationLanguage(language = "lt")
        "LATVIAN", "LV" -> ComparableTranslationLanguage(language = "lv")
        "NORWEGIAN", "NB", "NORWEGIAN_BOKMAL" -> ComparableTranslationLanguage(language = "no")
        "ROMANIAN", "RO" -> ComparableTranslationLanguage(language = "ro")
        "SLOVAK", "SK" -> ComparableTranslationLanguage(language = "sk")
        "SLOVENIAN", "SL" -> ComparableTranslationLanguage(language = "sl")
        "SWEDISH", "SV" -> ComparableTranslationLanguage(language = "sv")
        "TURKISH", "TR" -> ComparableTranslationLanguage(language = "tr")
        "UKRAINIAN", "UK" -> ComparableTranslationLanguage(language = "uk")
        else -> asFallbackComparableTranslationTarget(preserveRegion = true)
    }

private fun String.asGenericComparableTranslationTarget(): ComparableTranslationLanguage? =
    asDeepLComparableTranslationTarget() ?: asFallbackComparableTranslationTarget(preserveRegion = true)

private fun String.asFallbackComparableTranslationTarget(preserveRegion: Boolean): ComparableTranslationLanguage? {
    val normalized = lowercase(Locale.ROOT).replace('_', '-')
    if (normalized.isBlank()) {
        return null
    }

    val locale = Locale.forLanguageTag(normalized)
    val language =
        locale.language
            .takeUnless { it.isBlank() || it == "und" }
            ?: normalized.substringBefore('-').takeIf { it.isNotBlank() }
            ?: return null

    val region =
        if (preserveRegion) {
            locale.country
                .takeIf { it.isNotBlank() }
                ?: normalized
                    .split('-')
                    .drop(1)
                    .firstOrNull { it.length == 2 || it.length == 3 }
        } else {
            null
        }

    return ComparableTranslationLanguage(
        language = language.toCanonicalLanguageCode(),
        region = region?.uppercase(Locale.ROOT),
    )
}

private fun String.toCanonicalLanguageCode(): String =
    when (lowercase(Locale.ROOT)) {
        "nb" -> "no"
        else -> lowercase(Locale.ROOT)
    }
