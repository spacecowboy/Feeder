package com.nononsenseapps.feeder.ui.compose.theme

import android.graphics.Typeface
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
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.FontOptions

class FeederTypography(typographySettings: TypographySettings) {
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
        private val materialTypography = Typography()
    }
}

val fontWeights =
    listOf(
        FontWeight.Thin,
        FontWeight.ExtraLight,
        FontWeight.Light,
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
        FontWeight.ExtraBold,
        FontWeight.Black,
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

fun atkinsonHyperlegibleNextVariableFamily() =
    FontFamily(
        variableFont(R.font.atkinson_hyperlegible_next_variable, fontWeights, fontStylesNormalItalic).toList(),
    )

fun atkinsonHyperlegibleMonoVariableFamily() =
    FontFamily(
        variableFont(R.font.atkinson_hyperlegible_mono_variable, fontWeights, fontStylesNormalItalic).toList(),
    )

fun robotoFontFamily() =
    FontFamily(
        (
                variableFont(R.font.roboto_wdth_wght, fontWeights, fontStylesNormal) +
                        variableFont(R.font.roboto_italic_wdth_wght, fontWeights, fontStylesItalic)
                ).toList(),
    )

fun robotoMonoFontFamily() =
    FontFamily(
        (
                variableFont(R.font.roboto_mono_variable_wght, fontWeights, fontStylesNormal) +
                        variableFont(R.font.roboto_mono_italic_variable_wght, fontWeights, fontStylesItalic)
                ).toList(),
    )

fun systemSansSerifFontFamily() = FontFamily.SansSerif
fun systemMonoFontFamily() = FontFamily.Monospace

fun <A, B> cartesianProduct(
    list1: List<A>,
    list2: List<B>,
): Sequence<Pair<A, B>> =
    list1.asSequence()
        .flatMap { fontWeight ->
            list2.asSequence()
                .map { fontStyle ->
                    fontWeight to fontStyle
                }
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

@Immutable
data class TypographySettings(
    val fontScale: Float,
    val font: FontOptions,
) {
    val sansFontFamily by lazy {
        when (font) {
            FontOptions.ATKINSON_HYPERLEGIBLE -> atkinsonHyperlegibleNextVariableFamily()
            FontOptions.ROBOTO -> robotoFontFamily()
            FontOptions.SYSTEM_DEFAULT -> systemSansSerifFontFamily()
        }
    }

    val monoFontFamily by lazy {
        when (font) {
            FontOptions.ATKINSON_HYPERLEGIBLE -> atkinsonHyperlegibleMonoVariableFamily()
            FontOptions.ROBOTO -> robotoMonoFontFamily()
            FontOptions.SYSTEM_DEFAULT -> systemMonoFontFamily()
        }
    }

    val serifFontFamily by lazy {
        FontFamily.Serif
    }
}

val LocalTypographySettings: ProvidableCompositionLocal<TypographySettings> = compositionLocalOf { error("Missing TypographySettings!") }

@Composable
fun ProvideTypographySettings(
    fontScale: Float,
    font: FontOptions,
    content: @Composable () -> Unit,
) {
    val typographySettings =
        TypographySettings(
            fontScale = fontScale,
            font = font,
        )
    CompositionLocalProvider(LocalTypographySettings provides typographySettings, content = content)
}
