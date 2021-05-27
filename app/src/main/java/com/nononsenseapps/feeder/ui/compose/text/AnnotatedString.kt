package com.nononsenseapps.feeder.ui.compose.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

class AnnotatedParagraphStringBuilder(
    val builder: AnnotatedString.Builder = AnnotatedString.Builder()
) {
    val lastTwoChars: MutableList<Char> = mutableListOf()

    fun pushStyle(spanStyle: SpanStyle) {
        builder.pushStyle(style = spanStyle)
    }

    fun pushStringAnnotation(tag: String, annotation: String) {
        builder.pushStringAnnotation(tag = tag, annotation = annotation)
    }

    fun append(text: String) {
        if (text.count() > 2) {
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

    fun toAnnotatedString(): AnnotatedString {
        return builder.toAnnotatedString()
    }
}

fun AnnotatedParagraphStringBuilder.isEmpty() = lastTwoChars.isEmpty()
fun AnnotatedParagraphStringBuilder.isNotEmpty() = lastTwoChars.isNotEmpty()

fun AnnotatedParagraphStringBuilder.ensureDoubleNewline() {
    when {
        lastTwoChars.isEmpty() ||
            lastTwoChars.peekLatest() == '\n' && lastTwoChars.peekSecondLatest() == '\n' -> {
            // Nothing to do
        }
        lastTwoChars.peekLatest() == '\n' -> {
            builder.append('\n')
            lastTwoChars.pushMaxTwo('\n')
        }
        else -> {
            builder.append("\n\n")
            lastTwoChars.pushMaxTwo('\n')
            lastTwoChars.pushMaxTwo('\n')
        }
    }
}

private fun AnnotatedParagraphStringBuilder.ensureSingleNewline() {
    when {
        lastTwoChars.isEmpty() ||
            lastTwoChars.peekLatest() == '\n' -> {
            // Nothing to do
        }
        else -> {
            builder.append('\n')
            lastTwoChars.pushMaxTwo('\n')
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
