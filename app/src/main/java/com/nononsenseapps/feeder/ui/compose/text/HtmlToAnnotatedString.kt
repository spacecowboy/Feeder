package com.nononsenseapps.feeder.ui.compose.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.BaselineShift
import java.io.InputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * Returns "plain text" with annotations for TTS
 */
fun htmlToAnnotatedString(
    inputStream: InputStream,
    baseUrl: String,
): List<AnnotatedString> =
    Jsoup.parse(inputStream, null, baseUrl)
        ?.body()
        ?.let { body ->
            formatBody(
                element = body,
                baseUrl = baseUrl,
            )
        } ?: emptyList()

private fun formatBody(
    element: Element,
    baseUrl: String,
): List<AnnotatedString> {
    val result = mutableListOf<AnnotatedString>()
    val composer = TextComposer { paragraphBuilder ->
        result.add(paragraphBuilder.toAnnotatedString())
    }

    composer.appendTextChildren(
        nodes = element.childNodes(),
        baseUrl = baseUrl,
    )

    composer.terminateCurrentText()

    return result
}

private fun TextComposer.appendTextChildren(
    nodes: List<Node>,
    preFormatted: Boolean = false,
    baseUrl: String,
) {
    var node = nodes.firstOrNull()
    while (node != null) {
        when (node) {
            is TextNode -> {
                if (preFormatted) {
                    append(node.wholeText)
                } else {
                    if (endsWithWhitespace) {
                        node.text().trimStart().let { trimmed ->
                            if (trimmed.isNotEmpty()) {
                                append(trimmed)
                            }
                        }
                    } else {
                        node.text().let { text ->
                            if (text.isNotEmpty()) {
                                append(text)
                            }
                        }
                    }
                }
            }
            is Element -> {
                val element = node
                when (element.tagName()) {
                    "p" -> {
                        terminateCurrentText()
                        // Readability4j inserts p-tags in divs for algorithmic purposes.
                        // They screw up formatting.
                        if (node.hasClass("readability-styled")) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                            )
                        } else {
                            withParagraph {
                                appendTextChildren(
                                    element.childNodes(),
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        terminateCurrentText()
                    }
                    "br" -> append('\n')
                    "h1" -> {
                        terminateCurrentText()
                        withParagraph {
                            append(element.text())
                        }
                        terminateCurrentText()
                    }
                    "h2" -> {
                        terminateCurrentText()
                        withParagraph {
                            append(element.text())
                        }
                        terminateCurrentText()
                    }
                    "h3" -> {
                        terminateCurrentText()
                        withParagraph {
                            append(element.text())
                        }
                        terminateCurrentText()
                    }
                    "h4" -> {
                        terminateCurrentText()
                        withParagraph {
                            append(element.text())
                        }
                        terminateCurrentText()
                    }
                    "h5" -> {
                        terminateCurrentText()
                        withParagraph {
                            append(element.text())
                        }
                        terminateCurrentText()
                    }
                    "h6" -> {
                        terminateCurrentText()
                        withParagraph {
                            append(element.text())
                        }
                        terminateCurrentText()
                    }
                    "strong", "b" -> {
                        appendTextChildren(
                            element.childNodes(),
                            baseUrl = baseUrl,
                        )
                    }
                    "i", "em", "cite", "dfn" -> {
                        appendTextChildren(
                            element.childNodes(),
                            baseUrl = baseUrl,
                        )
                    }
                    "tt" -> {
                        appendTextChildren(
                            element.childNodes(),
                            baseUrl = baseUrl,
                        )
                    }
                    "u" -> {
                        appendTextChildren(
                            element.childNodes(),
                            baseUrl = baseUrl,
                        )
                    }
                    "sup" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Superscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "sub" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Subscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "font" -> {
                        appendTextChildren(
                            element.childNodes(),
                            baseUrl = baseUrl,
                        )
                    }
                    "pre" -> {
                        terminateCurrentText()
                        // TODO some TTS annotation?
                        appendTextChildren(
                            element.childNodes(),
                            preFormatted = true,
                            baseUrl = baseUrl,
                        )
                        terminateCurrentText()
                    }
                    "code" -> {
                        terminateCurrentText()
                        // TODO some TTS annotation?
                        appendTextChildren(
                            element.childNodes(),
                            preFormatted = preFormatted,
                            baseUrl = baseUrl,
                        )
                        terminateCurrentText()
                    }
                    "blockquote" -> {
                        terminateCurrentText()
                        withParagraph {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                            )
                        }
                        terminateCurrentText()
                    }
                    "a" -> {
                        withAnnotation("URL", element.attr("abs:href") ?: "") {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "img" -> {
                        terminateCurrentText()
                        val alt = element.attr("alt") ?: ""
                        if (alt.isNotEmpty()) {
                            append(alt)
                        }
                        terminateCurrentText()
                    }
                    "ul" -> {
                        element.children()
                            .filter { it.tagName() == "li" }
                            .forEach { listItem ->
                                withParagraph {
                                    // no break space
                                    append("• ")
                                    appendTextChildren(
                                        listItem.childNodes(),
                                        baseUrl = baseUrl,
                                    )
                                }
                            }
                    }
                    "ol" -> {
                        element.children()
                            .filter { it.tagName() == "li" }
                            .forEachIndexed { i, listItem ->
                                withParagraph {
                                    // no break space
                                    append("${i + 1}. ")
                                    appendTextChildren(
                                        listItem.childNodes(),
                                        baseUrl = baseUrl,
                                    )
                                }
                            }
                    }
                    "table" -> {
                        appendTable {
                            /*
                            In this order:
                            optionally a caption element (containing text children for instance),
                            followed by zero or more colgroup elements,
                            followed optionally by a thead element,
                            followed by either zero or more tbody elements
                            or one or more tr elements,
                            followed optionally by a tfoot element
                             */
                            element.children()
                                .filter { it.tagName() == "caption" }
                                .forEach {
                                    appendTextChildren(
                                        it.childNodes(),
                                        baseUrl = baseUrl,
                                    )
                                    ensureDoubleNewline()
                                    terminateCurrentText()
                                }

                            element.children()
                                .filter { it.tagName() == "thead" || it.tagName() == "tbody" || it.tagName() == "tfoot" }
                                .flatMap {
                                    it.children()
                                        .filter { it.tagName() == "tr" }
                                }
                                .forEach { row ->
                                    appendTextChildren(
                                        row.childNodes(),
                                        baseUrl = baseUrl,
                                    )
                                    terminateCurrentText()
                                }

                            append("\n\n")
                        }
                    }
                    "rt", "rp" -> {
                        // Ruby text elements. TTS has no need for furigana and similar
                        // so ignore
                    }
                    "iframe" -> {
                        // not implemented
                    }
                    "video" -> {
                        // not implemented
                    }
                    else -> {
                        appendTextChildren(
                            nodes = element.childNodes(),
                            preFormatted = preFormatted,
                            baseUrl = baseUrl,
                        )
                    }
                }
            }
        }

        node = node.nextSibling()
    }
}
