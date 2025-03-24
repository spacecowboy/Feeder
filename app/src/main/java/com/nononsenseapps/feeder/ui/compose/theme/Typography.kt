package com.nononsenseapps.feeder.ui.compose.theme

import androidx.annotation.FontRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
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
import com.nononsenseapps.feeder.archmodel.FontOptions

class FeederTypography(val font: FontOptions) {
    val sansFontFamily by lazy {
        when (font) {
            FontOptions.ATKINSON_HYPERLEGIBLE -> atkinsonHyperlegibleNextVariableFamily()
            FontOptions.ROBOTO -> robotoFontFamily()
        }
    }

    val typography: Typography =
        materialTypography.copy(
            displayLarge = materialTypography.displayLarge.merge(fontFamily = sansFontFamily),
            displayMedium = materialTypography.displayMedium.merge(fontFamily = sansFontFamily),
            displaySmall = materialTypography.displaySmall.merge(fontFamily = sansFontFamily),
            headlineLarge =
                materialTypography.headlineLarge.merge(
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = sansFontFamily,
                ),
            headlineMedium =
                materialTypography.headlineMedium.merge(
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = sansFontFamily,
                ),
            headlineSmall =
                materialTypography.headlineSmall.merge(
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = sansFontFamily,
                ),
            titleLarge = materialTypography.titleLarge.merge(fontFamily = sansFontFamily),
            titleMedium = materialTypography.titleMedium.merge(fontFamily = sansFontFamily),
            titleSmall = materialTypography.titleSmall.merge(fontFamily = sansFontFamily),
            bodyLarge =
                materialTypography.bodyLarge.merge(
                    hyphens = Hyphens.Auto,
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = sansFontFamily,
                ),
            bodyMedium =
                materialTypography.bodyMedium.merge(
                    hyphens = Hyphens.Auto,
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = sansFontFamily,
                ),
            bodySmall =
                materialTypography.bodySmall.merge(
                    hyphens = Hyphens.Auto,
                    lineBreak = LineBreak.Paragraph,
                    fontFamily = sansFontFamily,
                ),
            labelLarge = materialTypography.labelLarge.merge(fontFamily = sansFontFamily),
            labelMedium = materialTypography.labelMedium.merge(fontFamily = sansFontFamily),
            labelSmall = materialTypography.labelSmall.merge(fontFamily = sansFontFamily),
        )

    // TODO options?
    fun monoFontFamily() = atkinsonHyperlegibleMonoFamily

    // TODO options?
    fun serifFontFamily() = FontFamily.Serif

    companion object {
        private val materialTypography = Typography()
    }
}

val fontWeights = listOf(
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

val fontStylesNormalItalic = listOf(
    FontStyle.Normal,
    FontStyle.Italic,
)

val fontStylesNormal = listOf(
    FontStyle.Normal,
)

val fontStylesItalic = listOf(
    FontStyle.Italic,
)

fun atkinsonHyperlegibleNextVariableFamily() = FontFamily(
    variableFont(R.font.atkinson_hyperlegible_next_variable, fontWeights, fontStylesNormalItalic).toList()
)

fun robotoFontFamily() = FontFamily(
    (variableFont(R.font.roboto_wdth_wght, fontWeights, fontStylesNormal) +
        variableFont(R.font.roboto_italic_wdth_wght, fontWeights, fontStylesItalic)).toList(),
)

val atkinsonHyperlegibleMonoFamily = FontFamily(
    // extra light, light, regular, medium, semi bold, bold, extra bold
    Font(R.font.atkinson_hyperlegible_mono_extra_light, weight = FontWeight.ExtraLight),
    Font(R.font.atkinson_hyperlegible_mono_extra_light_italic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
    Font(R.font.atkinson_hyperlegible_mono_light, weight = FontWeight.Light),
    Font(R.font.atkinson_hyperlegible_mono_light_italic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.atkinson_hyperlegible_mono_regular),
    Font(R.font.atkinson_hyperlegible_mono_regular_italic, style = FontStyle.Italic),
    Font(R.font.atkinson_hyperlegible_mono_medium, weight = FontWeight.Medium),
    Font(R.font.atkinson_hyperlegible_mono_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.atkinson_hyperlegible_mono_semi_bold, weight = FontWeight.SemiBold),
    Font(R.font.atkinson_hyperlegible_mono_semi_bold_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(R.font.atkinson_hyperlegible_mono_bold, weight = FontWeight.Bold),
    Font(R.font.atkinson_hyperlegible_mono_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.atkinson_hyperlegible_mono_extra_bold, weight = FontWeight.ExtraBold),
    Font(R.font.atkinson_hyperlegible_mono_extra_bold_italic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
)

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
): Sequence<Font> = cartesianProduct(fontWeights, fontStyles)
    .map { (weight, style) ->
        Font(
            resId,
            weight = weight,
            style = style,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight.weight),
                FontVariation.italic(style.value.toFloat())
            )
        )
    }

@Composable
fun LinkTextStyle(): TextStyle =
    TextStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
//        fontFamily = LocalFeederTypography.current.sansFontFamily,
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
        fontFamily = LocalFeederTypography.current.monoFontFamily(),
    )

/**
 * Has no background because it is meant to be put over a Surface which has the proper background.
 */
@Composable
fun CodeBlockStyle(): TextStyle =
    MaterialTheme.typography.bodyMedium.merge(
        SpanStyle(
            fontFamily = LocalFeederTypography.current.monoFontFamily(),
        ),
    )

@Composable
fun CodeBlockBackground(): Color = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun OnCodeBlockBackground(): Color = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun BlockQuoteStyle(): SpanStyle =
    MaterialTheme.typography.bodyLarge.toSpanStyle().merge(
        SpanStyle(
            fontWeight = FontWeight.Light,
        ),
    )

// TODO resuse this for font?
@Immutable
data class TypographySettings(
    val fontScale: Float = 1.0f,
)

val LocalTypographySettings =
    staticCompositionLocalOf {
        TypographySettings()
    }

@Composable
fun ProvideFontScale(
    fontScale: Float,
    content: @Composable () -> Unit,
) {
    val typographySettings =
        TypographySettings(
            fontScale = fontScale,
        )
    CompositionLocalProvider(LocalTypographySettings provides typographySettings, content = content)
}
