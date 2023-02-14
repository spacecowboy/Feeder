package com.nononsenseapps.feeder.ui.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

// Set of Material typography styles to start with
val Typography = Typography()

@Composable
fun LinkTextStyle(): TextStyle =
    TextStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
    )

fun titleFontWeight(unread: Boolean) =
    if (unread) {
        FontWeight.Bold
    } else {
        FontWeight.Normal
    }

@Composable
fun FeedListItemTitleStyle(): SpanStyle =
    FeedListItemTitleTextStyle().toSpanStyle()

@Composable
fun FeedListItemTitleTextStyle(): TextStyle =
    MaterialTheme.typography.titleMedium

@Composable
fun FeedListItemStyle(): TextStyle =
    MaterialTheme.typography.bodyLarge

@Composable
fun FeedListItemFeedTitleStyle(): TextStyle =
    FeedListItemDateStyle()

@Composable
fun FeedListItemDateStyle(): TextStyle =
    MaterialTheme.typography.labelMedium

@Composable
fun TTSPlayerStyle(): TextStyle =
    MaterialTheme.typography.titleMedium

@Composable
fun CodeInlineStyle(): SpanStyle =
    SpanStyle(
        background = CodeBlockBackground(),
        fontFamily = FontFamily.Monospace,
    )

/**
 * Has no background because it is meant to be put over a Surface which has the proper background.
 */
@Composable
fun CodeBlockStyle(): TextStyle =
    MaterialTheme.typography.bodyMedium.merge(
        SpanStyle(
            fontFamily = FontFamily.Monospace,
        ),
    )

@Composable
fun CodeBlockBackground(): Color =
    MaterialTheme.colorScheme.surfaceVariant

@Composable
fun BlockQuoteStyle(): SpanStyle =
    MaterialTheme.typography.bodyLarge.toSpanStyle().merge(
        SpanStyle(
            fontWeight = FontWeight.Light,
        ),
    )

@Immutable
data class TypographySettings(
    val fontScale: Float = 1.0f,
)

val LocalTypographySettings = staticCompositionLocalOf {
    TypographySettings()
}

@Composable
fun ProvideFontScale(
    fontScale: Float,
    content: @Composable () -> Unit,
) {
    val typographySettings = TypographySettings(
        fontScale = fontScale,
    )
    CompositionLocalProvider(LocalTypographySettings provides typographySettings, content = content)
}
