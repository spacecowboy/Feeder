package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.archmodel.OpenAISettings
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TranslationLanguageUtilsKtTest {
    private val deepLSettings = OpenAISettings(baseUrl = "https://api.deepl.com")

    @Test
    fun deepLAllowsGenericEnglishToMatchRegionalVariant() {
        assertTrue(
            detectedLanguageMatchesTranslationTarget(
                detectedLanguage = "en-GB",
                targetLanguage = "English",
                settings = deepLSettings,
            ),
        )
    }

    @Test
    fun deepLRequiresExactRegionalMatchWhenTargetIsRegional() {
        assertFalse(
            detectedLanguageMatchesTranslationTarget(
                detectedLanguage = "en-GB",
                targetLanguage = "EN_US",
                settings = deepLSettings,
            ),
        )
    }

    @Test
    fun norwegianBokmalMatchesGenericNorwegianTarget() {
        assertTrue(
            detectedLanguageMatchesTranslationTarget(
                detectedLanguage = "nb-NO",
                targetLanguage = "Norwegian",
                settings = deepLSettings,
            ),
        )
    }

    @Test
    fun prepareTextForLanguageDetectionStripsHtmlAndCollapsesWhitespace() {
        assertEquals(
            "Headline Hello world",
            prepareTextForLanguageDetection(
                content = "Headline\n<div>Hello <b>world</b></div>",
                preserveHtml = true,
            ),
        )
    }
}
