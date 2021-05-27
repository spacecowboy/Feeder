package com.nononsenseapps.feeder.ui.compose.text

import android.content.res.Resources
import android.text.Annotation
import android.text.SpannedString
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.text.getSpans
import com.nononsenseapps.feeder.ui.compose.theme.FeederTypography

@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}

@Composable
@ReadOnlyComposable
fun annotatedStringResource(@StringRes id: Int): AnnotatedString {
    val resources = resources()
    val text = resources.getText(id) as SpannedString

    return buildAnnotatedString {
        this.append(text.toString())

        for (annotation in text.getSpans<Annotation>()) {
            when (annotation.key) {
                "style" -> {
                    getSpanStyle(annotation.value)?.let { spanStyle ->
                        addStyle(
                            spanStyle,
                            text.getSpanStart(annotation),
                            text.getSpanEnd(annotation)
                        )
                    }
                }
            }
        }
    }
}

private fun getSpanStyle(name: String?): SpanStyle? {
    return when (name) {
        "link" -> FeederTypography.link.toSpanStyle()
        else -> null
    }
}
