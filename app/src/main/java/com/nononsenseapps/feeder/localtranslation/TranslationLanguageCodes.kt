package com.nononsenseapps.feeder.localtranslation

import java.util.Locale

/**
 * Normalizes common language names and aliases accepted by Feeder's settings UI.
 * This is not the full set of supported model languages. Unknown values are
 * returned as normalized language codes so newly added Bergamot registry pairs
 * can work without changing this mapping.
 */
fun normalizeLanguageCode(language: String): String {
    val normalized =
        language
            .trim()
            .lowercase(Locale.ROOT)
            .replace('_', '-')
            .substringBefore('-')

    return when (normalized) {
        "english", "en" -> "en"
        "german", "de" -> "de"
        "french", "fr" -> "fr"
        "spanish", "es" -> "es"
        "portuguese", "pt" -> "pt"
        "italian", "it" -> "it"
        "dutch", "nl" -> "nl"
        "polish", "pl" -> "pl"
        "russian", "ru" -> "ru"
        "czech", "cs" -> "cs"
        "estonian", "et" -> "et"
        "bulgarian", "bg" -> "bg"
        "icelandic", "is" -> "is"
        "norwegian", "nb", "nn" -> "nb"
        "persian", "fa" -> "fa"
        "ukrainian", "uk" -> "uk"
        else -> normalized
    }
}
