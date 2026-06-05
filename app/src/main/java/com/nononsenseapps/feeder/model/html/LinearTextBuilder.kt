package com.nononsenseapps.feeder.model.html

import com.nononsenseapps.feeder.util.logDebug

class LinearTextBuilder : Appendable {
    private data class MutableRange<T>(
        val item: T,
        var start: Int,
        var end: Int? = null,
    )

    private val text: StringBuilder = StringBuilder(16)
    private val annotations: MutableList<MutableRange<LinearTextAnnotationData>> = mutableListOf()
    private val annotationsStack: MutableList<MutableRange<LinearTextAnnotationData>> = mutableListOf()
    private var lastCodePoint: Int? = null

    private val _ids: MutableSet<String> = mutableSetOf()
    val ids: Set<String>
        get() = _ids

    val length: Int
        get() = text.length

    val endsWithWhitespace: Boolean
        get() {
            return isCollapsableWhiteSpaceCode(lastCodePoint ?: SPACE_CODE)
        }

    fun pushId(id: String) {
        if (id.isNotBlank()) {
            _ids.add(id)
        }
    }

    fun append(text: String) {
        if (text.isNotEmpty()) {
            lastCodePoint = text.codePointBefore(text.length)
        }
        this.text.append(text)
    }

    override fun append(char: Char): LinearTextBuilder {
        lastCodePoint = char.code
        text.append(char)
        return this
    }

    fun appendCodePoint(codePoint: Int): LinearTextBuilder {
        lastCodePoint = codePoint
        text.appendCodePoint(codePoint)
        return this
    }

    override fun append(csq: CharSequence?): LinearTextBuilder {
        if (csq == null) {
            return this
        }

        if (csq.isNotEmpty()) {
            lastCodePoint = csq.last().code
        }
        text.append(csq)
        return this
    }

    override fun append(
        csq: CharSequence?,
        start: Int,
        end: Int,
    ): java.lang.Appendable {
        if (csq == null) {
            return this
        }

        if (end - start > 0) {
            lastCodePoint = csq[start + end - 1].code
        }
        text.append(csq, start, end)
        return this
    }

    /**
     * Applies the given [LinearTextAnnotationData] to any appended text until a corresponding [pop] is called.
     *
     * @return the index of the pushed annotation
     */
    fun push(annotation: LinearTextAnnotationData): Int {
        MutableRange(item = annotation, start = text.length).also {
            annotations.add(it)
            annotationsStack.add(it)
        }
        return annotationsStack.lastIndex
    }

    /**
     * Ends the style or annotation that was added via a push operation before.
     */
    fun pop() {
        check(annotationsStack.isNotEmpty()) { "No annotation to pop" }
        // pop the last element
        val item = annotationsStack.removeAt(annotationsStack.lastIndex)
        item.end = text.lastIndex
    }

    /**
     * Ends the annotation up to and including the pushLinearTextAnnotationData that returned the given index.
     *
     * @param index - the result of the a previous push in order to pop to
     */
    fun pop(index: Int) {
        check(index in annotationsStack.indices) { "No annotation at index $index: annotations size ${annotationsStack.size}" }
        while (annotationsStack.lastIndex >= index) {
            pop()
        }
    }

    fun toLinearText(blockStyle: LinearTextBlockStyle): LinearText {
        // Chop of possible ending whitespace - looks bad in code blocks for instance
        val trimmed = text.toString().trimEnd()
        return LinearText(
            // Copy the set to avoid modifications
            ids = ids.toSet(),
            text = trimmed,
            blockStyle = blockStyle,
            annotations =
                annotations.mapNotNull {
                    val start = it.start.coerceAtMost(trimmed.lastIndex)
                    val end = (it.end ?: text.lastIndex).coerceAtMost(trimmed.lastIndex)

                    if (start < 0 || end < 0 || start > end) {
                        // This can happen if the link encloses an image for example
                        logDebug(LOG_TAG, "Ignoring ${it.item} start: $start, end: $end")
                        null
                    } else {
                        LinearTextAnnotation(
                            data = it.item,
                            start = start,
                            end = end,
                        )
                    }
                },
        )
    }

    /**
     * Clears the text and resets annotations to start at the beginning
     */
    fun clearKeepingSpans() {
        _ids.clear()
        text.clear()
        lastCodePoint = null
        // Get rid of completed annotations
        annotations.clear()

        annotationsStack.forEach {
            it.start = 0
            it.end = null
            annotations.add(it)
        }
    }

    fun findClosestLink(): String? {
        for (annotation in annotationsStack.reversed()) {
            if (annotation.item is LinearTextAnnotationLink) {
                return annotation.item.href
            }
        }
        return null
    }

    fun isEmpty(): Boolean = lastCodePoint == null

    fun isNotEmpty(): Boolean = !isEmpty()

    companion object {
        private const val LOG_TAG = "FEEDER_LINEARTB"
    }
}
