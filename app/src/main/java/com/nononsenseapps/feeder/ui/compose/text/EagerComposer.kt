package com.nononsenseapps.feeder.ui.compose.text

import androidx.compose.runtime.Composable

class EagerComposer(
    private val paragraphEmitter: @Composable (AnnotatedParagraphStringBuilder, TextStyler?) -> Unit,
) : HtmlComposer() {
    private val paragraphs: MutableList<@Composable () -> Unit> = mutableListOf()

    @Composable
    fun render(): Boolean {
        emitParagraph()
        val result = paragraphs.isNotEmpty()
        for (p in paragraphs) {
            p()
        }
        paragraphs.clear()
        return result
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

        paragraphs.add {
            block(onClick)
        }
    }

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

        paragraphs.add {
            paragraphEmitter(actualBuilder, actualTextStyle)
        }
        resetAfterEmit()
        return true
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
