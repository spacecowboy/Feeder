package com.nononsenseapps.feeder.ui.compose.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

class AnnotatedParagraphStringBuilder(
    val builder: AnnotatedString.Builder = AnnotatedString.Builder()
) {
    val composableStyles = mutableListOf<ComposableStyleWithStartEnd>()
    val lastTwoChars: MutableList<Char> = mutableListOf()

    val endsWithWhitespace: Boolean
        get() {
            if (lastTwoChars.isEmpty()) {
                return true
            }
            lastTwoChars.peekLatest()?.let { latest ->
                // Non-breaking space (160) is not caught by trim or whitespace identification
                if (latest.isWhitespace() || latest.toInt() == 160) {
                    return true
                }
            }

            return false
        }

    fun pushStyle(spanStyle: SpanStyle) {
        builder.pushStyle(style = spanStyle)
    }

    fun pushStringAnnotation(tag: String, annotation: String) {
        builder.pushStringAnnotation(tag = tag, annotation = annotation)
    }

    fun pushComposableStyle(
        style: @Composable () -> SpanStyle
    ): Int {
        composableStyles.add(
            ComposableStyleWithStartEnd(
                style = style,
                start = builder.length
            )
        )
        return composableStyles.lastIndex
    }

    fun popComposableStyle(
        index: Int
    ) {
        composableStyles[index].let {
            composableStyles[index] = it.copy(end = builder.length)
        }
    }

    fun append(text: String) {
        if (text.count() >= 2) {
            lastTwoChars.pushMaxTwo(text.secondToLast())
        }
        if (text.isNotEmpty()) {
            lastTwoChars.pushMaxTwo(text.last())
        }
        builder.append(text)
    }

    fun append(char: Char) {
        lastTwoChars.pushMaxTwo(char)
        builder.append(char)
    }

    @Composable
    fun toAnnotatedString(): AnnotatedString {
        for (composableStyle in composableStyles) {
            builder.addStyle(
                style = composableStyle.style(),
                start = composableStyle.start,
                end = if (composableStyle.end < composableStyle.start) builder.length else composableStyle.end
            )
        }
        return builder.toAnnotatedString()
    }
}

fun AnnotatedParagraphStringBuilder.isEmpty() = lastTwoChars.isEmpty()
fun AnnotatedParagraphStringBuilder.isNotEmpty() = lastTwoChars.isNotEmpty()

fun AnnotatedParagraphStringBuilder.ensureDoubleNewline() {
    when {
        lastTwoChars.isEmpty() -> {
            // Nothing to do
        }
        builder.length == 1 && lastTwoChars.peekLatest()?.isWhitespace() == true -> {
            // Nothing to do
        }
        builder.length == 2 &&
            lastTwoChars.peekLatest()?.isWhitespace() == true &&
            lastTwoChars.peekSecondLatest()?.isWhitespace() == true -> {
            // Nothing to do
        }
        lastTwoChars.peekLatest() == '\n' && lastTwoChars.peekSecondLatest() == '\n' -> {
            // Nothing to do
        }
        lastTwoChars.peekLatest() == '\n' -> {
            append('\n')
        }
        else -> {
            append("\n\n")
        }
    }
}

private fun AnnotatedParagraphStringBuilder.ensureSingleNewline() {
    when {
        lastTwoChars.isEmpty() -> {
            // Nothing to do
        }
        builder.length == 1 && lastTwoChars.peekLatest()?.isWhitespace() == true -> {
            // Nothing to do
        }
        lastTwoChars.peekLatest() == '\n' -> {
            // Nothing to do
        }
        else -> {
            append('\n')
        }
    }
}

inline fun <R : Any> AnnotatedParagraphStringBuilder.withStyle(
    style: SpanStyle,
    crossinline block: AnnotatedParagraphStringBuilder.() -> R
): R {
    val index = builder.pushStyle(style)
    return try {
        block(this)
    } finally {
        builder.pop(index)
    }
}


inline fun <R : Any> AnnotatedParagraphStringBuilder.withComposableStyle(
    noinline style: @Composable () -> SpanStyle,
    crossinline block: AnnotatedParagraphStringBuilder.() -> R
): R {
    val index = pushComposableStyle(style)
    return try {
        block(this)
    } finally {
        popComposableStyle(index)
    }
}

inline fun <R : Any> AnnotatedParagraphStringBuilder.withAnnotation(
    tag: String,
    annotation: String,
    crossinline block: AnnotatedParagraphStringBuilder.() -> R
): R {
    val index = builder.pushStringAnnotation(tag, annotation)
    return try {
        block(this)
    } finally {
        builder.pop(index)
    }
}

private fun CharSequence.secondToLast(): Char {
    if (count() < 2) {
        throw NoSuchElementException("List has less than two items.")
    }
    return this[lastIndex - 1]
}

private fun <T> MutableList<T>.pushMaxTwo(item: T) {
    this.add(0, item)
    if (count() > 2) {
        this.removeLast()
    }
}

private fun <T> List<T>.peekLatest(): T? {
    return this.firstOrNull()
}

private fun <T> List<T>.peekSecondLatest(): T? {
    if (count() < 2) {
        return null
    }
    return this[1]
}

data class ComposableStyleWithStartEnd(
    val style: @Composable () -> SpanStyle,
    val start: Int,
    val end: Int = -1
)
