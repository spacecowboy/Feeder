@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
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
    val focusRequestId: Long = 0,
    val followSpokenText: Boolean = false,
)

@OptIn(ExperimentalFoundationApi::class)
internal object CenteredSpeechBringIntoViewSpec : BringIntoViewSpec {
    override fun calculateScrollDistance(
        offset: Float,
        size: Float,
        containerSize: Float,
    ): Float = offset + size / 2f - containerSize / 2f
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SpeechFollowViewport(
    centerSpeech: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val spec = if (centerSpeech) CenteredSpeechBringIntoViewSpec else LocalBringIntoViewSpec.current
    CompositionLocalProvider(LocalBringIntoViewSpec provides spec) {
        Box(modifier, content = content)
    }
}

/** Programmatic bring-into-view scrolling must not disable following; user input must. */
@Composable
internal fun Modifier.stopSpeechFollowingOnUserScroll(onUserScroll: () -> Unit): Modifier {
    val callback = rememberUpdatedState(onUserScroll)
    return pointerInput(Unit) {
        awaitPointerEventScope {
            var origin: Offset? = null
            var dragging = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type == PointerEventType.Scroll && event.changes.any { it.scrollDelta != Offset.Zero }) {
                    callback.value()
                }
                val pressed = event.changes.firstOrNull { it.pressed }
                if (pressed == null) {
                    origin = null
                    dragging = false
                } else if (origin == null) {
                    origin = pressed.position
                } else if (!dragging && (pressed.position - checkNotNull(origin)).getDistance() > viewConfiguration.touchSlop) {
                    dragging = true
                    callback.value()
                }
            }
        }
    }
}

/** Neutral emphasis, independent of dynamic accent colors, without sacrificing text contrast. */
internal fun speechHighlightColor(
    background: Color,
    foreground: Color,
    link: Color,
): Color {
    fun contrast(
        a: Color,
        b: Color,
    ): Float {
        val x = a.luminance()
        val y = b.luminance()
        return (maxOf(x, y) + 0.05f) / (minOf(x, y) + 0.05f)
    }
    val neutral = if (background.luminance() > 0.5f) Color.Black else Color.White
    val targetAlpha = if (neutral == Color.Black) 0.10f else 0.14f
    val colors = listOf(foreground, link)
    val minimums = colors.map { minOf(4.5f, contrast(it, background)) }
    for (step in 10 downTo 0) {
        val candidate = neutral.copy(alpha = targetAlpha * step / 10)
        val highlighted = candidate.compositeOver(background)
        if (colors.indices.all { contrast(colors[it], highlighted) + 0.001f >= minimums[it] }) return candidate
    }
    return Color.Transparent
}

// A document-scoped rendering attribute, including nested quotes, lists, and table cells.
@Suppress("ktlint:compose:compositionlocal-allowlist")
internal val LocalSpeechHighlights = compositionLocalOf<Map<LinearText, IntRange>> { emptyMap() }

internal class SpeechFocusTarget(
    val text: LinearText,
    val range: IntRange,
    val requestId: Long,
)

@Suppress("ktlint:compose:compositionlocal-allowlist")
internal val LocalSpeechFocusTarget = compositionLocalOf<SpeechFocusTarget?> { null }

/** Maps matching text in document order, never highlighting another occurrence of a repeated paragraph. */
internal class SpeechHighlightIndex(
    article: LinearArticle,
    speechText: String,
) {
    private val source = normalizeSpeechText(speechText)
    private val matches = IdentityHashMap<LinearText, Pair<Int, NormalizedSpeechText>>()
    private val documentOrder = mutableListOf<LinearText>()

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
                            documentOrder += element
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

    fun focusTarget(
        segment: SpeechSegment,
        requestId: Long,
    ): SpeechFocusTarget? {
        if (requestId == 0L) return null
        val ranges = highlights(segment)
        val first = documentOrder.firstOrNull { ranges.containsKey(it) } ?: return null
        return SpeechFocusTarget(first, ranges.getValue(first), requestId)
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
