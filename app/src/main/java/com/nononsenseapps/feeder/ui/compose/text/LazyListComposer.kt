package com.nononsenseapps.feeder.ui.compose.text

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable

class LazyListComposer(
    private val lazyListScope: LazyListScope,
    private val paragraphEmitter: @Composable (AnnotatedParagraphStringBuilder, TextStyler?) -> Unit,
) : HtmlComposer() {

    override fun emitParagraph(): Boolean {
        if (builder.isEmpty()) {
            // Nothing to emit, and nothing to reset
            return false
        }

        // Important that we reference the correct builder in the lambda - reset will create a new
        // builder and the lambda will run after that
        val actualBuilder = builder
        val actualTextStyle = textStyleStack.lastOrNull()

        lazyListScope.item {
            paragraphEmitter(actualBuilder, actualTextStyle)
        }
        resetAfterEmit()
        return true
    }

    override fun appendImage(
        link: String?,
        onLinkClick: (String) -> Unit,
        block: @Composable (() -> Unit) -> Unit,
    ) {
        emitParagraph()

        val url = link ?: findClosestLink()
        val onClick: (() -> Unit) = when {
            url?.isNotBlank() == true -> {
                {
                    onLinkClick(url)
                }
            }
            else -> {
                {}
            }
        }

        lazyListScope.item {
            block(onClick)
        }
    }

    fun item(block: @Composable () -> Unit) {
        lazyListScope.item {
            block()
        }
    }

    private fun resetAfterEmit() {
        builder = AnnotatedParagraphStringBuilder()

        for (span in spanStack) {
            when (span) {
                is SpanWithStyle -> builder.pushStyle(span.spanStyle)
                is SpanWithAnnotation -> builder.pushStringAnnotation(
                    tag = span.tag,
                    annotation = span.annotation,
                )
                is SpanWithComposableStyle -> builder.pushComposableStyle(span.spanStyle)
                is SpanWithVerbatim -> builder.pushVerbatimTtsAnnotation(span.verbatim)
            }
        }
    }
}
