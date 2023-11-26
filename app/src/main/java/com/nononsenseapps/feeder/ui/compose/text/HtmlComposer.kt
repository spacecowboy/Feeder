package com.nononsenseapps.feeder.ui.compose.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle

abstract class HtmlComposer : HtmlParser() {
    abstract fun appendImage(
        link: String? = null,
        onLinkClick: (String) -> Unit,
        block: @Composable (() -> Unit) -> Unit,
    )
}

abstract class HtmlParser {
    protected val spanStack: MutableList<Span> = mutableListOf()
    protected val textStyleStack: MutableList<TextStyler> = mutableListOf()

    // The identity of this will change - do not reference it in blocks
    protected var builder: AnnotatedParagraphStringBuilder = AnnotatedParagraphStringBuilder()

    /**
     * returns true if any content was emitted, false otherwise
     */
    abstract fun emitParagraph(): Boolean

    val endsWithWhitespace: Boolean
        get() = builder.endsWithWhitespace

    fun append(text: String) = builder.append(text)

    fun append(char: Char) = builder.append(char)

    fun pop(index: Int) = builder.pop(index)

    fun pushStyle(style: SpanStyle): Int = builder.pushStyle(style)

    fun pushSpan(span: Span) = spanStack.add(span)

    fun pushStringAnnotation(
        tag: String,
        annotation: String,
    ): Int = builder.pushStringAnnotation(tag = tag, annotation = annotation)

    fun pushComposableStyle(style: @Composable () -> SpanStyle): Int = builder.pushComposableStyle(style)

    fun popComposableStyle(index: Int) = builder.popComposableStyle(index)

    fun pushTextStyle(style: TextStyler) = textStyleStack.add(style)

    fun popTextStyle() = textStyleStack.removeLastOrNull()

    fun popSpan() = spanStack.removeLast()

    protected fun findClosestLink(): String? {
        for (span in spanStack.reversed()) {
            if (span is SpanWithAnnotation && span.tag == "URL") {
                return span.annotation
            }
        }
        return null
    }
}

inline fun <R : Any> HtmlComposer.withTextStyle(
    textStyler: TextStyler,
    crossinline block: HtmlComposer.() -> R,
): R {
    emitParagraph()
    pushTextStyle(textStyler)
    return try {
        block()
    } finally {
        emitParagraph()
        popTextStyle()
    }
}

inline fun <R : Any> HtmlParser.withParagraph(crossinline block: HtmlParser.() -> R): R {
    emitParagraph()
    return block(this).also {
        emitParagraph()
    }
}

inline fun <R : Any> HtmlParser.withStyle(
    style: SpanStyle?,
    crossinline block: HtmlParser.() -> R,
): R {
    if (style == null) {
        return block()
    }

    pushSpan(SpanWithStyle(style))
    val index = pushStyle(style)
    return try {
        block()
    } finally {
        pop(index)
        popSpan()
    }
}

inline fun <R : Any> HtmlComposer.withComposableStyle(
    noinline style: @Composable () -> SpanStyle,
    crossinline block: HtmlComposer.() -> R,
): R {
    pushSpan(SpanWithComposableStyle(style))
    val index = pushComposableStyle(style)
    return try {
        block()
    } finally {
        popComposableStyle(index)
        popSpan()
    }
}

inline fun <R : Any> HtmlParser.withAnnotation(
    tag: String,
    annotation: String,
    crossinline block: HtmlParser.() -> R,
): R {
    pushSpan(SpanWithAnnotation(tag = tag, annotation = annotation))
    val index = pushStringAnnotation(tag = tag, annotation = annotation)
    return try {
        block()
    } finally {
        pop(index)
        popSpan()
    }
}

sealed class Span

data class SpanWithStyle(
    val spanStyle: SpanStyle,
) : Span()

data class SpanWithAnnotation(
    val tag: String,
    val annotation: String,
) : Span()

data class SpanWithComposableStyle(
    val spanStyle: @Composable () -> SpanStyle,
) : Span()

data class SpanWithVerbatim(
    val verbatim: String,
) : Span()

interface TextStyler {
    @Composable
    fun textStyle(): TextStyle
}
