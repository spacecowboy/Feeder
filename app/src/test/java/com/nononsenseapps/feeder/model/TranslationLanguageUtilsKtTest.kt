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

    @Test
    fun prepareTextSamplesForLanguageDetectionSamplesLongTextAcrossContent() {
        val content =
            buildString {
                append("A".repeat(4500))
                append("B".repeat(4500))
                append("C".repeat(4500))
            }

        val samples =
            prepareTextSamplesForLanguageDetection(
                content = content,
                preserveHtml = false,
            )

        assertEquals(3, samples.size)
        assertTrue(samples[0].startsWith("AAA"))
        assertTrue(samples[1].contains("BBB"))
        assertTrue(samples[2].endsWith("CCC"))
    }
}
