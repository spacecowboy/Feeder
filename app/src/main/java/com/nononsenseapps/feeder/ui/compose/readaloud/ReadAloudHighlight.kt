@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.runtime.compositionLocalOf
import com.nononsenseapps.feeder.model.SpeechSegment
import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.model.html.LinearBlockQuote
import com.nononsenseapps.feeder.model.html.LinearElement
import com.nononsenseapps.feeder.model.html.LinearListItem
import com.nononsenseapps.feeder.model.html.LinearTable
import com.nononsenseapps.feeder.model.html.LinearText
import java.util.IdentityHashMap

data class SpeechHighlight(
    val text: String,
    val segment: SpeechSegment,
)

// A document-scoped rendering attribute, including nested quotes, lists, and table cells.
@Suppress("ktlint:compose:compositionlocal-allowlist")
internal val LocalSpeechHighlights = compositionLocalOf<Map<LinearText, IntRange>> { emptyMap() }

/** Maps matching text in document order, never highlighting another occurrence of a repeated paragraph. */
internal class SpeechHighlightIndex(
    article: LinearArticle,
    speechText: String,
) {
    private val source = normalizeSpeechText(speechText)
    private val matches = IdentityHashMap<LinearText, Pair<Int, NormalizedSpeechText>>()

    init {
        var cursor = 0

        fun visit(element: LinearElement) {
            when (element) {
                is LinearText -> {
                    val normalized = normalizeSpeechText(element.text)
                    if (normalized.text.isNotBlank()) {
                        val start = source.text.indexOf(normalized.text, cursor)
                        if (start >= 0) {
                            matches[element] = start to normalized
                            cursor = start + normalized.text.length
                        }
                    }
                }
                is LinearBlockQuote -> element.content.forEach(::visit)
                is LinearListItem -> element.content.forEach(::visit)
                is LinearTable ->
                    element.cells.entries
                        .sortedWith(compareBy({ it.key.row }, { it.key.col }))
                        .filterNot { it.value.isFiller }
                        .forEach { it.value.content.forEach(::visit) }
                else -> Unit
            }
        }
        article.elements.forEach(::visit)
    }

    fun highlights(segment: SpeechSegment): Map<LinearText, IntRange> {
        val result = IdentityHashMap<LinearText, IntRange>()
        matches.forEach { (element, match) ->
            val (start, normalized) = match
            val indices = normalized.offsets.indices.filter { source.offsets[start + it] in segment.start until segment.end }
            if (indices.isNotEmpty()) {
                result[element] = normalized.offsets[indices.first()]..normalized.offsets[indices.last()]
            }
        }
        return result
    }
}

internal data class NormalizedSpeechText(
    val text: String,
    val offsets: List<Int>,
)

internal fun normalizeSpeechText(text: String): NormalizedSpeechText {
    val offsets = mutableListOf<Int>()
    val normalized =
        buildString {
            text.forEachIndexed { index, char ->
                if (char.isWhitespace() || char == '\u00a0') {
                    if (isNotEmpty() && last() != ' ') {
                        append(' ')
                        offsets += index
                    }
                } else {
                    append(char)
                    offsets += index
                }
            }
            if (endsWith(' ')) {
                deleteCharAt(length - 1)
                offsets.removeAt(offsets.lastIndex)
            }
        }
    return NormalizedSpeechText(normalized, offsets)
}
