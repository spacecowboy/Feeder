package com.nononsenseapps.feeder.ui.compose.theme

import androidx.annotation.FontRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.settings.FontSelection
import com.nononsenseapps.feeder.util.FilePathProvider
import org.kodein.di.compose.instance
import java.io.File

class FeederTypography(
    typographySettings: TypographySettings,
) {
    val typography: Typography =
        materialTypography.copy(
            displayLarge = materialTypography.displayLarge.merge(fontFamily = typographySettings.sansFontFamily),
            displayMedium = materialTypography.displayMedium.merge(fontFamily = typographySettings.sansFontFamily),
            displaySmall = materialTypography.displaySmall.merge(fontFamily = typographySettings.sansFontFamily),
            headlineLarge =
                materialTypography.headlineLarge.merge(
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = typographySettings.sansFontFamily,
                ),
            headlineMedium =
                materialTypography.headlineMedium.merge(
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = typographySettings.sansFontFamily,
                ),
            headlineSmall =
                materialTypography.headlineSmall.merge(
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = typographySettings.sansFontFamily,
                ),
            titleLarge = materialTypography.titleLarge.merge(fontFamily = typographySettings.sansFontFamily),
            titleMedium = materialTypography.titleMedium.merge(fontFamily = typographySettings.sansFontFamily),
            titleSmall = materialTypography.titleSmall.merge(fontFamily = typographySettings.sansFontFamily),
            bodyLarge =
                materialTypography.bodyLarge.merge(
                    hyphens = Hyphens.Auto,
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = typographySettings.sansFontFamily,
                ),
            bodyMedium =
                materialTypography.bodyMedium.merge(
                    hyphens = Hyphens.Auto,
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = typographySettings.sansFontFamily,
                ),
            bodySmall =
                materialTypography.bodySmall.merge(
                    hyphens = Hyphens.Auto,
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = typographySettings.sansFontFamily,
                ),
            labelLarge = materialTypography.labelLarge.merge(fontFamily = typographySettings.sansFontFamily),
            labelMedium = materialTypography.labelMedium.merge(fontFamily = typographySettings.sansFontFamily),
            labelSmall = materialTypography.labelSmall.merge(fontFamily = typographySettings.sansFontFamily),
        )

    companion object {
        val materialTypography = Typography()
    }
}

val fontWeights =
    listOf(
        // Weights actually used have comments. Not all fonts have all weights available.
        FontWeight.Thin,
        FontWeight.ExtraLight,
        // Used in BlockQuotes and Empty view
        FontWeight.Light,
        // Normal used in body, title, display
        FontWeight.Normal,
        // Medium used in title, label
        FontWeight.Medium,
        FontWeight.SemiBold,
        // Used whenever bold is used
        FontWeight.Bold,
        FontWeight.ExtraBold,
        // Used in unread title
        FontWeight.Black,
    )

val fontWeightsNormal =
    listOf(
        FontWeight.Normal,
    )

val fontStylesNormalItalic =
    listOf(
        FontStyle.Normal,
        FontStyle.Italic,
    )

val fontStylesNormal =
    listOf(
        FontStyle.Normal,
    )

val fontStylesItalic =
    listOf(
        FontStyle.Italic,
    )

fun userFontFamily(
    file: File,
    font: FontSelection,
): FontFamily {
    val weights =
        if (font.hasWeightVariation) {
            fontWeights.filter { it.weight in (font.minWeightValue.toInt())..(font.maxWeightValue.toInt()) }
        } else {
            fontWeightsNormal
        }

    val italics =
        if (font.hasItalicVariation || font.hasSlantVariation) {
            fontStylesNormalItalic
        } else {
            fontStylesNormal
        }

    return FontFamily(variableFont(file, font, weights, italics).toList())
}

fun robotoSansFontFamily(font: FontSelection.RobotoFlex): FontFamily {
    val weights =
        if (font.hasWeightVariation) {
            fontWeights.filter { it.weight in (font.minWeightValue.toInt())..(font.maxWeightValue.toInt()) }
        } else {
            fontWeightsNormal
        }

    val italics =
        if (font.hasItalicVariation || font.hasSlantVariation) {
            fontStylesNormalItalic
        } else {
            fontStylesNormal
        }

    return FontFamily(variableFont(font.resId, weights, italics).toList())
}

fun robotoMonoFontFamily() = FontFamily(variableFont(R.font.roboto_mono_variable_wght, fontWeights, fontStylesNormal).toList())

fun systemSansSerifFontFamily() = FontFamily.SansSerif

fun systemMonoFontFamily() = FontFamily.Monospace

fun <A, B> cartesianProduct(
    list1: List<A>,
    list2: List<B>,
): Sequence<Pair<A, B>> =
    list1
        .asSequence()
        .flatMap { fontWeight ->
            list2
                .asSequence()
                .map { fontStyle ->
                    fontWeight to fontStyle
                }
        }

fun variableFont(
    file: File,
    font: FontSelection,
    fontWeights: List<FontWeight>,
    fontStyles: List<FontStyle>,
): Sequence<Font> =
    cartesianProduct(fontWeights, fontStyles)
        .map { (weight, style) ->
            val variations = mutableListOf<FontVariation.Setting>()

            if (font.hasWeightVariation) {
                variations.add(FontVariation.weight(weight.weight))
            }

            if (font.hasSlantVariation) {
                variations.add(
                    if (style == FontStyle.Italic) {
                        // Slant has a negative value for "forward" lean
                        FontVariation.slant(font.minSlantValue)
                    } else {
                        FontVariation.slant(0f)
                    },
                )
            } else if (font.hasItalicVariation) {
                variations.add(
                    if (style == FontStyle.Italic) {
                        FontVariation.italic(font.maxItalicValue)
                    } else {
                        FontVariation.italic(0f)
                    },
                )
            }

            Font(
                file = file,
                weight = weight,
                style = style,
                variationSettings = FontVariation.Settings(*variations.toTypedArray()),
            )
        }

@OptIn(ExperimentalTextApi::class)
fun variableFont(
    @FontRes resId: Int,
    fontWeights: List<FontWeight>,
    fontStyles: List<FontStyle>,
): Sequence<Font> =
    cartesianProduct(fontWeights, fontStyles)
        .map { (weight, style) ->
            Font(
                resId,
                weight = weight,
                style = style,
                variationSettings =
                    FontVariation.Settings(
                        FontVariation.weight(weight.weight),
                        FontVariation.italic(style.value.toFloat()),
                    ),
            )
        }

@Composable
fun LinkTextStyle(): TextStyle =
    TextStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
    )

fun titleFontWeight(unread: Boolean) =
    if (unread) {
        FontWeight.Black
    } else {
        FontWeight.Normal
    }

@Composable
fun FeedListItemTitleTextStyle(): TextStyle =
    MaterialTheme.typography.titleMedium.merge(
        TextStyle(
            lineBreak = LineBreak.Paragraph,
            hyphens = Hyphens.Auto,
        ),
    )

@Composable
fun FeedListItemSnippetTextStyle(): TextStyle =
    MaterialTheme.typography.titleSmall.merge(
        TextStyle(
            lineBreak = LineBreak.Paragraph,
            hyphens = Hyphens.Auto,
        ),
    )

@Composable
fun FeedListItemFeedTitleStyle(): TextStyle = FeedListItemDateStyle()

@Composable
fun FeedListItemDateStyle(): TextStyle = MaterialTheme.typography.labelMedium

@Composable
fun TTSPlayerStyle(): TextStyle = MaterialTheme.typography.titleMedium

@Composable
fun CodeInlineStyle(): SpanStyle =
    SpanStyle(
        background = CodeBlockBackground(),
        fontFamily = LocalTypographySettings.current.monoFontFamily,
    )

@Composable
fun CodeBlockBackground(): Color = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun OnCodeBlockBackground(): Color = MaterialTheme.colorScheme.onSurfaceVariant

fun getMonoFontFamily(font: FontSelection) =
    when (font) {
        FontSelection.RobotoFlex -> robotoMonoFontFamily()
        else -> systemMonoFontFamily()
    }

@Immutable
data class TypographySettings(
    val fontScale: Float,
    val sansFontFamily: FontFamily,
    val monoFontFamily: FontFamily,
    val serifFontFamily: FontFamily = FontFamily.Serif,
)

val LocalTypographySettings: ProvidableCompositionLocal<TypographySettings> = compositionLocalOf { error("Missing TypographySettings!") }

@Composable
fun ProvideTypographySettings(
    fontScale: Float,
    font: FontSelection,
    content: @Composable () -> Unit,
) {
    val sansFont =
        when (font) {
            is FontSelection.RobotoFlex -> robotoSansFontFamily(font)
            is FontSelection.SystemDefault -> systemSansSerifFontFamily()
            is FontSelection.UserFont -> {
                val filePathProvider: FilePathProvider by instance()
                val file = font.getFile(filePathProvider)
                userFontFamily(file, font)
            }
        }

    val typographySettings =
        TypographySettings(
            fontScale = fontScale,
            sansFontFamily = sansFont,
            monoFontFamily = getMonoFontFamily(font),
        )
    CompositionLocalProvider(LocalTypographySettings provides typographySettings, content = content)
}
