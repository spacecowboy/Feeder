package com.nononsenseapps.feeder.ui.compose.text

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.nononsenseapps.feeder.ui.compose.feedarticle.ArticleItemKeyHolder

class LazyListComposer(
    private val lazyListScope: LazyListScope,
    private val keyHolder: ArticleItemKeyHolder,
    private val paragraphEmitter: @Composable (AnnotatedParagraphStringBuilder, TextStyler?) -> Unit,
) : HtmlComposer() {
    override fun emitParagraph(): Boolean {
        // List items emit dots and non-breaking space. Don't newline after that
        if (builder.isEmpty() || builder.endsWithNonBreakingSpace) {
            // Nothing to emit, and nothing to reset
            return false
        }

        // Important that we reference the correct builder in the lambda - reset will create a new
        // builder and the lambda will run after that
        val actualBuilder = builder
        val actualTextStyle = textStyleStack.lastOrNull()

        item(keyHolder = keyHolder) {
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
        val onClick: (() -> Unit) =
            when {
                url?.isNotBlank() == true -> {
                    {
                        onLinkClick(url)
                    }
                }
                else -> {
                    {}
                }
            }

        item(keyHolder = keyHolder) {
            block(onClick)
        }
    }

    /**
     * Key is necessary or when you switch between default and full text - the initial items
     * will have the same index and will not recompose.
     */
    fun item(
        keyHolder: ArticleItemKeyHolder,
        block: @Composable () -> Unit,
    ) {
        lazyListScope.item(key = keyHolder.getAndIncrementKey()) {
            block()
        }
    }

    private fun resetAfterEmit() {
        builder = AnnotatedParagraphStringBuilder()

        for (span in spanStack) {
            when (span) {
                is SpanWithStyle -> builder.pushStyle(span.spanStyle)
                is SpanWithAnnotation ->
                    builder.pushStringAnnotation(
                        tag = span.tag,
                        annotation = span.annotation,
                    )
                is SpanWithComposableStyle -> builder.pushComposableStyle(span.spanStyle)
                is SpanWithVerbatim -> builder.pushVerbatimTtsAnnotation(span.verbatim)
            }
        }
    }
}
