package com.nononsenseapps.feeder.model.html

import android.util.Log
import com.nononsenseapps.feeder.ui.compose.text.ancestors
import com.nononsenseapps.feeder.ui.compose.text.attrInHierarchy
import com.nononsenseapps.feeder.ui.compose.text.stripHtml
import com.nononsenseapps.feeder.ui.text.getVideo
import com.nononsenseapps.feeder.util.asUTF8Sequence
import com.nononsenseapps.feeder.util.logDebug
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

typealias IdHolder = (String) -> Unit

class HtmlLinearizer {
    private var linearTextBuilder: LinearTextBuilder = LinearTextBuilder()
    private var idHolder: IdHolder = {
        linearTextBuilder.pushId(it)
    }

    fun linearize(
        html: String,
        baseUrl: String,
    ) = html.byteInputStream().use { linearize(it, baseUrl) }

    fun linearize(
        inputStream: InputStream,
        baseUrl: String,
    ): LinearArticle =
        LinearArticle(
            elements =
                try {
                    Jsoup
                        .parse(inputStream, null, baseUrl)
                        .body()
                        .let { body ->
                            linearizeBody(body, baseUrl)
                        }
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "htmlFormattingFailed", e)
                    emptyList()
                },
        )

    private fun linearizeBody(
        body: Element,
        baseUrl: String,
    ): List<LinearElement> =
        ListBuilderScope {
            asElement(blockStyle = LinearTextBlockStyle.TEXT) {
                linearizeChildren(
                    body.childNodes(),
                    blockStyle = it,
                    baseUrl = baseUrl,
                )
            }
        }.items

    private fun ListBuilderScope<LinearElement>.linearizeChildren(
        nodes: List<Node>,
        baseUrl: String,
        blockStyle: LinearTextBlockStyle,
    ) {
        var node = nodes.firstOrNull()
        while (node != null) {
            when (node) {
                is TextNode -> {
                    if (blockStyle.shouldSoftWrap) {
                        node.appendCorrectlyNormalizedWhiteSpace(
                            linearTextBuilder,
                        )
                    } else {
                        append(node.wholeText)
                    }
                }

                is Element -> {
                    val element = node
                    // Id is used for in-page links
                    pushId(element.id())

                    if (isHiddenByCSS(element)) {
                        // Element is not supposed to be shown because javascript and/or tracking
                        node = node.nextSibling()
                        continue
                    }

                    when (element.tagName()) {
                        "p" -> {
                            // Readability4j inserts p-tags in divs for algorithmic purposes.
                            // They screw up formatting.
                            if (node.hasClass("readability-styled")) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = LinearTextBlockStyle.TEXT,
                                    baseUrl = baseUrl,
                                )
                                // We do need to enforce space after the paragraph though. Sometimes it's missing.
                                appendOneNormalizedWhitespace(linearTextBuilder)
                            } else {
                                asElement(blockStyle) {
                                    linearizeChildren(
                                        element.childNodes(),
                                        blockStyle = LinearTextBlockStyle.TEXT,
                                        baseUrl = baseUrl,
                                    )
                                }
                            }
                        }

                        "br" -> append('\n')

                        "h1" -> {
                            asElement(blockStyle) {
                                withLinearTextAnnotation(LinearTextAnnotationH1) {
                                    element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                        linearTextBuilder,
                                    )
                                }
                            }
                        }

                        "h2" -> {
                            asElement(blockStyle) {
                                withLinearTextAnnotation(LinearTextAnnotationH2) {
                                    element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                        linearTextBuilder,
                                    )
                                }
                            }
                        }

                        "h3" -> {
                            asElement(blockStyle) {
                                withLinearTextAnnotation(LinearTextAnnotationH3) {
                                    element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                        linearTextBuilder,
                                    )
                                }
                            }
                        }

                        "h4" -> {
                            asElement(blockStyle) {
                                withLinearTextAnnotation(LinearTextAnnotationH4) {
                                    element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                        linearTextBuilder,
                                    )
                                }
                            }
                        }

                        "h5" -> {
                            asElement(blockStyle) {
                                withLinearTextAnnotation(LinearTextAnnotationH5) {
                                    element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                        linearTextBuilder,
                                    )
                                }
                            }
                        }

                        "h6" -> {
                            asElement(blockStyle) {
                                withLinearTextAnnotation(LinearTextAnnotationH6) {
                                    element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                        linearTextBuilder,
                                    )
                                }
                            }
                        }

                        "strong", "b" -> {
                            withLinearTextAnnotation(LinearTextAnnotationBold) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "i", "em", "cite", "dfn" -> {
                            withLinearTextAnnotation(LinearTextAnnotationItalic) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "span" -> {
                            val style =
                                element
                                    .attr("style")
                                    .splitToSequence(";")
                                    .map {
                                        it.split(":", limit = 2)
                                    }.filter { it.size == 2 }
                                    .associate {
                                        it[0].trim() to it[1].trim()
                                    }

                            val maybeBold =
                                if (style["font-weight"] == "bold") {
                                    LinearTextAnnotationBold
                                } else {
                                    null
                                }

                            val maybeItalic =
                                if (style["font-style"] in setOf("italic", "oblique")) {
                                    LinearTextAnnotationItalic
                                } else {
                                    null
                                }

                            withLinearTextAnnotation(maybeBold) {
                                withLinearTextAnnotation(maybeItalic) {
                                    linearizeChildren(
                                        element.childNodes(),
                                        blockStyle = blockStyle,
                                        baseUrl = baseUrl,
                                    )
                                }
                            }
                        }

                        "tt" -> {
                            withLinearTextAnnotation(LinearTextAnnotationMonospace) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "u" -> {
                            withLinearTextAnnotation(LinearTextAnnotationUnderline) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "s" -> {
                            withLinearTextAnnotation(LinearTextAnnotationStrikethrough) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "sup" -> {
                            withLinearTextAnnotation(LinearTextAnnotationSuperscript) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "sub" -> {
                            withLinearTextAnnotation(LinearTextAnnotationSubscript) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "font" -> {
                            val face: String? = element.attr("face").ifBlank { null }
                            if (face != null) {
                                withLinearTextAnnotation(LinearTextAnnotationFont(face)) {
                                    this@linearizeChildren.linearizeChildren(
                                        element.childNodes(),
                                        blockStyle = blockStyle,
                                        baseUrl = baseUrl,
                                    )
                                }
                            } else {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "pre" -> {
                            asElement(
                                blockStyle =
                                    if (element.selectFirst("code") != null) {
                                        LinearTextBlockStyle.CODE_BLOCK
                                    } else {
                                        LinearTextBlockStyle.PRE_FORMATTED
                                    },
                            ) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = it,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "code" -> {
                            withLinearTextAnnotation(LinearTextAnnotationCode) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

//                        "q" -> {
                        // TODO
//                            The <q> tag defines a short quotation.
//                            Browsers normally insert quotation marks around the quotation.
//                        }

                        "blockquote" -> {
                            finalizeAndAddCurrentElement(blockStyle)

                            val cite = element.attr("cite").ifBlank { null }
                            // This is not correct. If the blockquote contains elements which will split
                            // the blockquote, then ids will be duplicated. This won't crash or anything
                            // but in-page links will go to the wrong element
                            val ids = element.allIds()

                            // Text should be collected into LinearBlockQuote
                            // Other types should be added separately
                            ListBuilderScope {
                                asElement(blockStyle = LinearTextBlockStyle.TEXT) {
                                    linearizeChildren(
                                        element.childNodes(),
                                        blockStyle = LinearTextBlockStyle.TEXT,
                                        baseUrl = baseUrl,
                                    )
                                }
                            }.items
                                .fold(mutableListOf<LinearElement>()) { acc, item ->
                                    if (item is LinearText) {
                                        acc.add(item)
                                        acc
                                    } else {
                                        if (acc.isNotEmpty()) {
                                            add(
                                                LinearBlockQuote(
                                                    ids = ids,
                                                    cite = cite,
                                                    content = acc,
                                                ),
                                            )
                                        }
                                        add(
                                            item,
                                        )
                                        mutableListOf()
                                    }
                                }.let {
                                    if (it.isNotEmpty()) {
                                        add(
                                            LinearBlockQuote(
                                                ids = ids,
                                                cite = cite,
                                                content = it,
                                            ),
                                        )
                                    }
                                }
                        }

                        "a" -> {
                            // abs:href will be blank for mailto: links
                            withLinearTextAnnotation(LinearTextAnnotationLink(element.attr("abs:href").ifBlank { element.attr("href") })) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "figcaption" -> {
                            // If not inside figure then FullTextParsing just failed
                            if (element.parent()?.tagName() == "figure") {
                                linearizeChildren(
                                    nodes = element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "figure" -> {
                            finalizeAndAddCurrentElement(blockStyle)

                            // Some sites put youtube iframes inside figures
                            val iframes = element.getElementsByTag("iframe")
                            if (iframes.isNotEmpty() && iframes.first() != null) {
                                iframes.first()?.let {
                                    parseIframeVideo(it)
                                }
                            } else {
                                val ids = element.allIds()
                                // Wordpress likes nested figures to get images side by side
                                val imageCandidates =
                                    element.descendantImageCandidates(baseUrl = baseUrl)
                                        // Arstechnica has its own ideas about how to structure things
                                        ?: element.ancestorImageCandidates(baseUrl = baseUrl)

                                if (imageCandidates != null) {
                                    val link = linearTextBuilder.findClosestLink()?.takeIf { it.isNotBlank() }

                                    val caption: LinearText? =
                                        ListBuilderScope {
                                            asElement(blockStyle = LinearTextBlockStyle.TEXT) {
                                                linearizeChildren(
                                                    element.childNodes(),
                                                    blockStyle = it,
                                                    baseUrl = baseUrl,
                                                )
                                            }
                                        }.items.firstOrNull {
                                            // Stuffing non-text inside a caption is not supported
                                            it is LinearText && it.text.isNotBlank()
                                        } as? LinearText

                                    add(
                                        LinearImage(
                                            ids = ids,
                                            sources = imageCandidates,
                                            caption = caption,
                                            link = link,
                                        ),
                                    )
                                }
                            }
                        }

                        "img" -> {
                            finalizeAndAddCurrentElement(blockStyle)

                            val ids = element.allIds()

                            getImageSource(baseUrl, element).let { candidates ->
                                if (candidates.isNotEmpty()) {
                                    val captionText: String? =
                                        stripHtml(element.attr("alt"))
                                            .takeIf { it.isNotBlank() }
                                    add(
                                        LinearImage(
                                            ids = ids,
                                            sources = candidates,
                                            // Parse a LinearText with annotations from element.attr("alt")
                                            caption =
                                                captionText?.let {
                                                    LinearText(
                                                        ids = linearTextBuilder.ids,
                                                        text = it,
                                                        annotations = emptyList(),
                                                        blockStyle = LinearTextBlockStyle.TEXT,
                                                    )
                                                },
                                            link = linearTextBuilder.findClosestLink()?.takeIf { it.isNotBlank() },
                                        ),
                                    )
                                }
                            }
                        }

                        "ul", "ol" -> {
                            finalizeAndAddCurrentElement(blockStyle)

                            val ordered = element.tagName() == "ol"
                            element
                                .children()
                                .filter { it.tagName() == "li" }
                                .forEachIndexed { index, listItem ->
                                    val item =
                                        LinearListItem(
                                            ids = listItem.allIds(),
                                            orderedIndex =
                                                if (ordered) {
                                                    index + 1
                                                } else {
                                                    null
                                                },
                                        ) {
                                            asElement(blockStyle) {
                                                linearizeChildren(
                                                    listItem.childNodes(),
                                                    blockStyle = it,
                                                    baseUrl = baseUrl,
                                                )
                                            }
                                        }

                                    if (item.isNotEmpty()) {
                                        add(item)
                                    }
                                }
                        }

                        "td", "th" -> {
                            // If we end up here, that means the table has been optimized out. Treat as a div.
                            asElement(blockStyle) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            }
                        }

                        "table" -> {
                            finalizeAndAddCurrentElement(blockStyle)

                            // This can also be auto, but for tables that's equivalent to LTR probably
                            val leftToRight = element.attrInHierarchy("dir") != "rtl"
                            val ids = element.allIds()

                            val rowSequence =
                                sequence<Element> {
                                    element
                                        .children()
                                        .asSequence()
                                        .filter { child ->
                                            child.tagName() in setOf("thead", "tbody", "tfoot", "tr")
                                        }.sortedBy { child ->
                                            when (child.tagName()) {
                                                "thead" -> 0
                                                "tbody" -> 1
                                                "tr" -> 2
                                                "tfoot" -> 3
                                                else -> 99
                                            }
                                        }.forEach { child ->
                                            if (child.tagName() == "tr") {
                                                yield(child)
                                            } else {
                                                yieldAll(child.children().filter { it.tagName() == "tr" })
                                            }
                                        }
                                }

                            val colCount =
                                rowSequence
                                    .map { row ->
                                        row.children().count { it.tagName() == "td" || it.tagName() == "th" }
                                    }.maxOrNull()
                                    ?: 0

                            // If there is only a single row, or a single column, then don't bother with a table
                            if (colCount == 1 || rowSequence.count() == 1) {
                                linearizeChildren(
                                    element.childNodes(),
                                    blockStyle = blockStyle,
                                    baseUrl = baseUrl,
                                )
                            } else {
                                add(
                                    LinearTable.build(ids = ids, leftToRight = leftToRight) {
                                        rowSequence
                                            .forEach { row ->
                                                newRow()

                                                row
                                                    .children()
                                                    .filter { it.tagName() == "td" || it.tagName() == "th" }
                                                    .forEach { cell ->
                                                        add(
                                                            LinearTableCellItem(
                                                                colSpan = cell.attr("colspan").toIntOrNull()?.coerceAtLeast(-1) ?: 1,
                                                                rowSpan = cell.attr("rowspan").toIntOrNull()?.coerceAtLeast(-1) ?: 1,
                                                                type =
                                                                    if (cell.tagName() == "th") {
                                                                        LinearTableCellItemType.HEADER
                                                                    } else {
                                                                        LinearTableCellItemType.DATA
                                                                    },
                                                            ) {
                                                                asElement(blockStyle = blockStyle) {
                                                                    linearizeChildren(
                                                                        cell.childNodes(),
                                                                        blockStyle = it,
                                                                        baseUrl = baseUrl,
                                                                    )
                                                                }
                                                            },
                                                        )
                                                    }
                                            }
                                    },
                                )
                            }
                        }

                        "rt", "rp" -> {
                            // Ruby text elements. Not supported.
                        }

                        "audio" -> {
                            val sources =
                                element
                                    .getElementsByTag("source")
                                    .asSequence()
                                    .mapNotNull { source ->
                                        source.attr("abs:src").takeIf { it.isNotBlank() }?.let { src ->
                                            LinearAudioSource(
                                                uri = src,
                                                mimeType = source.attr("type").ifBlank { null },
                                            )
                                        }
                                    }.toList()
                                    .takeIf { it.isNotEmpty() }

                            if (sources != null) {
                                add(LinearAudio(ids = element.allIds(), sources = sources))
                            }
                        }

                        "iframe" -> {
                            finalizeAndAddCurrentElement(blockStyle)
                            parseIframeVideo(element)
                        }

                        "video" -> {
                            val width = element.attr("width").toIntOrNull()
                            val height = element.attr("height").toIntOrNull()
                            val sources =
                                element
                                    .getElementsByTag("source")
                                    .asSequence()
                                    .mapNotNull { source ->
                                        source.attr("abs:src").takeIf { it.isNotBlank() }?.let { src ->
                                            LinearVideoSource(
                                                uri = src,
                                                link = src,
                                                imageThumbnail = null,
                                                mimeType = source.attr("type").ifBlank { null },
                                                widthPx = width?.takeIf { it > 0 },
                                                heightPx = height?.takeIf { it > 0 },
                                            )
                                        }
                                    }.toList()
                                    .takeIf { it.isNotEmpty() }

                            if (sources != null) {
                                add(LinearVideo(ids = element.allIds(), sources = sources))
                            }
                        }

                        else -> {
                            linearizeChildren(
                                nodes = element.childNodes(),
                                blockStyle = blockStyle,
                                baseUrl = baseUrl,
                            )
                        }
                    }
                }
            }

            node = node.nextSibling()
        }
    }

    private fun ListBuilderScope<LinearElement>.parseIframeVideo(element: Element) {
        val width = element.attr("width").toIntOrNull()
        val height = element.attr("height").toIntOrNull()
        getVideo(element.attr("abs:src").ifBlank { null })?.let { video ->
            add(
                LinearVideo(
                    ids = element.allIds(),
                    sources =
                        listOf(
                            LinearVideoSource(
                                uri = video.src,
                                link = video.link,
                                imageThumbnail = video.imageUrl,
                                widthPx = width?.takeIf { it > 0 } ?: video.width.takeIf { it > 0 },
                                heightPx = height?.takeIf { it > 0 } ?: video.height.takeIf { it > 0 },
                                mimeType = null,
                            ),
                        ),
                ),
            )
        }
    }

    private fun append(c: String) {
        linearTextBuilder.append(c)
    }

    private fun pushId(id: String) {
        idHolder(id)
    }

    private fun withIdHolder(
        idHolder: IdHolder,
        block: () -> Unit,
    ) {
        val oldHolder = this.idHolder
        this.idHolder = idHolder
        block()
        this.idHolder = oldHolder
    }

    @Suppress("SameParameterValue")
    private fun append(c: Char) {
        linearTextBuilder.append(c)
    }

    internal fun ListBuilderScope<LinearElement>.finalizeAndAddCurrentElement(blockStyle: LinearTextBlockStyle) {
        if (linearTextBuilder.isNotEmpty()) {
            add(linearTextBuilder.toLinearText(blockStyle = blockStyle))
            linearTextBuilder.clearKeepingSpans()
        }
    }

    private inline fun <R : Any> ListBuilderScope<LinearElement>.asElement(
        blockStyle: LinearTextBlockStyle,
        block: ListBuilderScope<LinearElement>.(blockStyle: LinearTextBlockStyle) -> R,
    ): R {
        finalizeAndAddCurrentElement(blockStyle)
        return this.block(blockStyle).also {
            finalizeAndAddCurrentElement(blockStyle)
        }
    }

    private inline fun <R : Any> ListBuilderScope<LinearElement>.withLinearTextAnnotation(
        annotationData: LinearTextAnnotationData?,
        block: ListBuilderScope<LinearElement>.() -> R,
    ): R {
        // Nullable to handle span styles easier. If null, no annotation is added.
        if (annotationData == null) {
            return this.block()
        }

        val i = linearTextBuilder.push(annotationData)
        return try {
            this.block()
        } finally {
            linearTextBuilder.pop(i)
        }
    }

    private fun isHiddenByCSS(element: Element): Boolean {
        val style = element.attr("style")
        return style.contains("display:") && style.contains("none")
    }

    private fun getImageSource(
        baseUrl: String,
        element: Element,
    ): List<LinearImageSource> {
        val absSrc: String = element.attr("abs:src")
        val dataImgUrl: String = element.attr("data-img-url").ifBlank { element.attr("data-src") }
        val srcSet: String = element.attr("srcset").ifBlank { element.attr("data-responsive") }
        // Can be set on divs - see ArsTechnica
        val backgroundImage =
            element
                .attr("style")
                .ifBlank { null }
                ?.splitToSequence(";")
                ?.map { it.trim() }
                ?.map { it.split(":", limit = 2) }
                ?.mapNotNull { kv ->
                    if (kv.size != 2) {
                        null
                    } else {
                        val (key, value) = kv
                        if (key.trim() == "background-image") {
                            value.trim().removePrefix("url('").removeSuffix("')")
                        } else {
                            null
                        }
                    }
                }?.firstOrNull()
                ?: ""

        val result = mutableListOf<LinearImageSource>()

        try {
            srcSet
                .splitToSequence(", ")
                .map { it.trim() }
                .map { it.split(spaceRegex).take(2).map { x -> x.trim() } }
                .forEach { candidate ->
                    if (candidate.first().isBlank()) {
                        return@forEach
                    }
                    if (candidate.size == 1) {
                        result.add(
                            LinearImageSource(
                                imgUri = resolve(baseUrl, candidate.first()),
                                pixelDensity = null,
                                heightPx = null,
                                widthPx = null,
                                screenWidth = null,
                            ),
                        )
                    } else {
                        val descriptor = candidate.last()
                        when {
                            descriptor.endsWith("w", ignoreCase = true) -> {
                                val width = descriptor.substringBefore("w").toFloat()
                                if (width < 0f) {
                                    return@forEach
                                }

                                result.add(
                                    LinearImageSource(
                                        imgUri = resolve(baseUrl, candidate.first()),
                                        pixelDensity = null,
                                        heightPx = null,
                                        widthPx = null,
                                        screenWidth = width.toInt().takeIf { it > 0 },
                                    ),
                                )
                            }

                            descriptor.endsWith("x", ignoreCase = true) -> {
                                val density = descriptor.substringBefore("x").toFloat()

                                if (density < 0f) {
                                    return@forEach
                                }

                                result.add(
                                    LinearImageSource(
                                        imgUri = resolve(baseUrl, candidate.first()),
                                        pixelDensity = density.takeIf { it > 0 },
                                        heightPx = null,
                                        widthPx = null,
                                        screenWidth = null,
                                    ),
                                )
                            }
                        }
                    }
                }

            val width = element.attr("width").toIntOrNull()
            val height = element.attr("height").toIntOrNull()

            dataImgUrl.takeIf { it.isNotBlank() }?.let {
                val url = resolve(baseUrl, it)
                if (width != null && height != null) {
                    result.add(
                        LinearImageSource(
                            imgUri = url,
                            pixelDensity = null,
                            screenWidth = null,
                            heightPx = height.takeIf { it > 0 },
                            widthPx = width.takeIf { it > 0 },
                        ),
                    )
                } else {
                    result.add(
                        LinearImageSource(
                            imgUri = url,
                            pixelDensity = null,
                            heightPx = null,
                            widthPx = null,
                            screenWidth = null,
                        ),
                    )
                }
            }

            absSrc.takeIf { it.isNotBlank() }?.let {
                // inline data images can't be resolved, the result is an empty string
                val url =
                    if (it.startsWith("data:")) {
                        it
                    } else {
                        resolve(baseUrl, it)
                    }
                if (width != null && height != null) {
                    result.add(
                        LinearImageSource(
                            imgUri = url,
                            pixelDensity = null,
                            screenWidth = null,
                            heightPx = height.takeIf { it > 0 },
                            widthPx = width.takeIf { it > 0 },
                        ),
                    )
                } else {
                    result.add(
                        LinearImageSource(
                            imgUri = url,
                            pixelDensity = null,
                            screenWidth = null,
                            heightPx = null,
                            widthPx = null,
                        ),
                    )
                }
            }

            backgroundImage.takeIf { it.isNotBlank() }?.let {
                val url = resolve(baseUrl, it)
                result.add(
                    LinearImageSource(
                        imgUri = url,
                        pixelDensity = null,
                        screenWidth = null,
                        heightPx = null,
                        widthPx = null,
                    ),
                )
            }
        } catch (e: Throwable) {
            logDebug(LOG_TAG, "Failed to parse image source", e)
        }
        return result
    }

    private fun Element.descendantImageCandidates(baseUrl: String): List<LinearImageSource>? {
        // Arstechnica is weird and has images inside divs inside figures
        return sequence {
            yieldAll(getElementsByTag("img"))
            yieldAll(getElementsByClass("image"))
        }.flatMap { getImageSource(baseUrl, it) }
            .distinctBy { it.imgUri }
            .toList()
            .takeIf { it.isNotEmpty() }
    }

    private fun Element.ancestorImageCandidates(baseUrl: String): List<LinearImageSource>? {
        // Arstechnica is weird and places image details in list items which themselves contain the figure
        return ancestors {
            it.hasAttr("data-src") || it.hasAttr("data-responsive")
        }.flatMap { getImageSource(baseUrl, it) }
            .distinctBy { it.imgUri }
            .toList()
            .takeIf { it.isNotEmpty() }
    }

    companion object {
        private const val LOG_TAG = "FEEDERHtmlLinearizer"
        private val spaceRegex = Regex("\\s+")
    }
}

/**
 * Can't use JSoup's text() method because that strips invisible characters
 * such as ZWNJ which are crucial for several languages.
 */
fun TextNode.appendCorrectlyNormalizedWhiteSpace(builder: LinearTextBuilder) {
    wholeText
        .asUTF8Sequence()
        .dropWhile {
            builder.endsWithWhitespace && isCollapsableWhiteSpace(it)
        }.fold(false) { lastWasWhite, char ->
            if (isCollapsableWhiteSpace(char)) {
                if (!lastWasWhite) {
                    builder.append(' ')
                }
                true
            } else {
                builder.append(char)
                false
            }
        }
}

fun appendOneNormalizedWhitespace(builder: LinearTextBuilder) {
    if (!builder.endsWithWhitespace) {
        builder.append(' ')
    }
}

fun Element.appendCorrectlyNormalizedWhiteSpaceRecursively(builder: LinearTextBuilder) {
    for (child in childNodes()) {
        when (child) {
            is TextNode -> child.appendCorrectlyNormalizedWhiteSpace(builder)
            is Element ->
                child.appendCorrectlyNormalizedWhiteSpaceRecursively(
                    builder,
                )
        }
    }
}

class ListBuilderScope<T>(
    block: ListBuilderScope<T>.() -> Unit,
) {
    val items = mutableListOf<T>()

    init {
        block()
    }

    fun add(item: T) {
        items.add(item)
    }
}

private const val SPACE = ' '
private const val TAB = '\t'
private const val LINE_FEED = '\n'
private const val CARRIAGE_RETURN = '\r'

// 12 is form feed which as no escape in kotlin
private const val FORM_FEED = 12.toChar()

// 160 is &nbsp; (non-breaking space). Not in the spec but expected.
private const val NON_BREAKING_SPACE = 160.toChar()

private fun isCollapsableWhiteSpace(c: String) = c.firstOrNull()?.let { isCollapsableWhiteSpace(it) } ?: false

private fun isCollapsableWhiteSpace(c: Char) = c == SPACE || c == TAB || c == LINE_FEED || c == CARRIAGE_RETURN || c == FORM_FEED || c == NON_BREAKING_SPACE

private fun resolve(
    baseUrl: String,
    relUrl: String,
): String {
    val base: URL
    try {
        try {
            base = URL(baseUrl)
        } catch (e: MalformedURLException) {
            // the base is unsuitable, but the attribute/rel may be abs on its own, so try that
            val abs = URL(relUrl)
            return abs.toExternalForm()
        }
        return resolve(base, relUrl).toExternalForm()
    } catch (e: MalformedURLException) {
        return ""
    }
}

private fun resolve(
    baseUrl: URL,
    relUrl: String,
): URL {
    // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
    var base = baseUrl
    var rel = relUrl
    if (rel.startsWith("?")) {
        rel = base.path + rel
    }
    // workaround: //example.com + ./foo = //example.com/./foo, not //example.com/foo
    if (rel.indexOf('.') == 0 && base.file.indexOf('/') != 0) {
        base = URL(base.protocol, base.host, base.port, "/" + base.file)
    }
    return URL(base, rel)
}

private suspend fun SequenceScope<Element>.yieldDescendantsOf(element: Element) {
    for (child in element.children()) {
        yield(child)
        yieldDescendantsOf(child)
    }
}

private fun Element.allIds(): Set<String> =
    sequence {
        yield(this@allIds)
        yieldDescendantsOf(this@allIds)
    }.map { it.id() }
        .filterNot { it.isEmpty() }
        .toSet()
