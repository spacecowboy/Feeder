package com.nononsenseapps.feeder.ui.compose.text

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

class TextComposer(
    val paragraphEmitter: @Composable (AnnotatedString) -> Unit
) {
    val spanStack: MutableList<Span> = mutableListOf()
    var builder: AnnotatedParagraphStringBuilder = AnnotatedParagraphStringBuilder()

    @Composable
    fun terminateCurrentText() {
        if (builder.isEmpty()) {
            // Nothing to emit, and nothing to reset
            return
        }

        paragraphEmitter(builder.toAnnotatedString())

        builder = AnnotatedParagraphStringBuilder()

        for (span in spanStack) {
            when (span) {
                is SpanWithStyle -> builder.pushStyle(span.spanStyle)
                is SpanWithAnnotation -> builder.pushStringAnnotation(tag = span.tag, annotation = span.annotation)
            }
        }
    }

    fun append(text: String) {
        builder.append(text)
    }

    fun append(char: Char) {
        builder.append(char)
    }

    @Composable
    fun <R> appendTable(block: @Composable () -> R): R {
        builder.ensureDoubleNewline()
        terminateCurrentText()
        return block()
    }

    @Composable
    fun <R> appendImage(block: @Composable (onClick: (() -> Unit)?) -> R): R {
        val url = findClosestLink()
        builder.ensureDoubleNewline()
        terminateCurrentText()
        val onClick: (() -> Unit)? = if (url?.isNotBlank() == true) {
            {
                // TODO handle click
                Log.i("JONAS", "Clicked image with $url")
            }
        } else {
            null
        }
        return block(onClick = onClick)
    }

    private fun findClosestLink(): String? {
        for (span in spanStack.reversed()) {
            if (span is SpanWithAnnotation && span.tag == "URL") {
                return span.annotation
            }
        }
        return null
    }
}

inline fun <R : Any> TextComposer.withParagraph(
    crossinline block: TextComposer.() -> R
): R {
    builder.ensureDoubleNewline()
    return block(this)
}

inline fun <R : Any> TextComposer.withStyle(
    style: SpanStyle,
    crossinline block: TextComposer.() -> R
): R {
    spanStack.add(SpanWithStyle(style))
    return try {
        builder.withStyle(style = style) {
            block()
        }
    } finally {
        spanStack.removeLast()
    }
}

inline fun <R : Any> TextComposer.withAnnotation(
    tag: String,
    annotation: String,
    crossinline block: TextComposer.() -> R
): R {
    spanStack.add(SpanWithAnnotation(tag = tag, annotation = annotation))
    return try {
        builder.withAnnotation(tag = tag, annotation = annotation) {
            block()
        }
    } finally {
        spanStack.removeLast()
    }
}

sealed class Span

data class SpanWithStyle(
    val spanStyle: SpanStyle
): Span()

data class SpanWithAnnotation(
    val tag: String,
    val annotation: String
): Span()
