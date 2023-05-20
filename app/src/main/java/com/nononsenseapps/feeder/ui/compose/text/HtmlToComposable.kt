package com.nononsenseapps.feeder.ui.compose.text

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.theme.BlockQuoteStyle
import com.nononsenseapps.feeder.ui.compose.theme.CodeBlockBackground
import com.nononsenseapps.feeder.ui.compose.theme.CodeBlockStyle
import com.nononsenseapps.feeder.ui.compose.theme.CodeInlineStyle
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.utils.ProvideScaledText
import com.nononsenseapps.feeder.ui.text.Video
import com.nononsenseapps.feeder.ui.text.getVideo
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.roundToInt
import org.jsoup.Jsoup
import org.jsoup.helper.StringUtil
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

private const val LOG_TAG = "FEEDER_HTMLTOCOM"

fun LazyListScope.htmlFormattedText(
    inputStream: InputStream,
    baseUrl: String,
    onLinkClick: (String) -> Unit,
) {
    try {
        Jsoup.parse(inputStream, null, baseUrl)
            ?.body()
            ?.let { body ->
                formatBody(
                    element = body,
                    baseUrl = baseUrl,
                    onLinkClick = onLinkClick,
                )
            }
    } catch (e: Exception) {
        Log.e(LOG_TAG, "htmlFormattingFailed", e)
    }
}

@Composable
private fun ParagraphText(
    paragraphBuilder: AnnotatedParagraphStringBuilder,
    textStyler: TextStyler?,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
) {
    val paragraph = paragraphBuilder.rememberComposableAnnotatedString()

    // just for debug
//    check(paragraph.text.isNotEmpty()) {
//        "Text is empty - you failed"
//    }

    ProvideScaledText(
        textStyler?.textStyle() ?: MaterialTheme.typography.bodyLarge.merge(
            TextStyle(color = MaterialTheme.colorScheme.onBackground),
        ),
    ) {
        WithBidiDeterminedLayoutDirection(paragraph.text) {
            // ClickableText prevents taps from deselecting selected text
            // So use regular Text if possible
            if (
                paragraph.getStringAnnotations("URL", 0, paragraph.length)
                    .isNotEmpty()
            ) {
                ClickableText(
                    text = paragraph,
                    style = LocalTextStyle.current,
                    modifier = modifier,
                ) { offset ->
                    paragraph.getStringAnnotations("URL", offset, offset)
                        .firstOrNull()
                        ?.let {
                            onLinkClick(it.item)
                        }
                }
            } else {
                Text(
                    text = paragraph,
                    modifier = modifier,
                )
            }
        }
    }
}

private fun LazyListScope.formatBody(
    element: Element,
    baseUrl: String,
    onLinkClick: (String) -> Unit,
) {
    val composer = LazyListComposer(this) { paragraphBuilder, textStyler ->
        val dimens = LocalDimens.current
        ParagraphText(
            paragraphBuilder = paragraphBuilder,
            textStyler = textStyler,
            modifier = Modifier
                .width(dimens.maxContentWidth),
            onLinkClick = onLinkClick,
        )
    }

    composer.appendTextChildren(
        element.childNodes(),
        baseUrl = baseUrl,
        onLinkClick = onLinkClick,
    )

    composer.emitParagraph()
}

private fun HtmlComposer.appendTextChildren(
    nodes: List<Node>,
    preFormatted: Boolean = false,
    baseUrl: String,
    onLinkClick: (String) -> Unit,
) {
    var node = nodes.firstOrNull()
    while (node != null) {
        when (node) {
            is TextNode -> {
                if (preFormatted) {
                    append(node.wholeText)
                } else {
                    node.appendCorrectlyNormalizedWhiteSpace(
                        this,
                        stripLeading = endsWithWhitespace,
                    )
                }
            }

            is Element -> {
                val element = node
                when (element.tagName()) {
                    "p" -> {
                        // Readability4j inserts p-tags in divs for algorithmic purposes.
                        // They screw up formatting.
                        if (node.hasClass("readability-styled")) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                            )
                        } else {
                            withParagraph {
                                appendTextChildren(
                                    element.childNodes(),
                                    baseUrl = baseUrl,
                                    onLinkClick = onLinkClick,
                                )
                            }
                        }
                    }

                    "br" -> append('\n')
                    "h1" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() },
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace,
                                )
                            }
                        }
                    }

                    "h2" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() },
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace,
                                )
                            }
                        }
                    }

                    "h3" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() },
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace,
                                )
                            }
                        }
                    }

                    "h4" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() },
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace,
                                )
                            }
                        }
                    }

                    "h5" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() },
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace,
                                )
                            }
                        }
                    }

                    "h6" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() },
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace,
                                )
                            }
                        }
                    }

                    "strong", "b" -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }

                    "i", "em", "cite", "dfn" -> {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }

                    "tt" -> {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }

                    "u" -> {
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }

                    "sup" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Superscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }

                    "sub" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Subscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }

                    "font" -> {
                        val fontFamily: FontFamily? = element.attr("face")?.asFontFamily()
                        withStyle(SpanStyle(fontFamily = fontFamily)) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }

                    "pre" -> {
                        appendTextChildren(
                            element.childNodes(),
                            preFormatted = true,
                            baseUrl = baseUrl,
                            onLinkClick = onLinkClick,
                        )
                    }

                    "code" -> {
                        if (element.parent()?.tagName() == "pre") {
                            emitParagraph()

                            when (this) {
                                is LazyListComposer -> {
                                    val composer = EagerComposer { paragraphBuilder, textStyler ->
                                        val dimens = LocalDimens.current
                                        val scrollState = rememberScrollState()
                                        Surface(
                                            color = CodeBlockBackground(),
                                            shape = MaterialTheme.shapes.medium,
                                            modifier = Modifier
                                                .horizontalScroll(
                                                    state = scrollState,
                                                )
                                                .width(dimens.maxContentWidth),
                                        ) {
                                            Box(modifier = Modifier.padding(all = 4.dp)) {
                                                Text(
                                                    text = paragraphBuilder.rememberComposableAnnotatedString(),
                                                    style = textStyler?.textStyle()
                                                        ?: CodeBlockStyle(),
                                                    softWrap = false,
                                                )
                                            }
                                        }
                                    }

                                    with(composer) {
                                        item {
                                            appendTextChildren(
                                                element.childNodes(),
                                                preFormatted = true,
                                                baseUrl = baseUrl,
                                                onLinkClick = onLinkClick,
                                            )
                                            emitParagraph()
                                            render()
                                        }
                                    }
                                }

                                is EagerComposer -> {
                                    // Should never happen as far as I know. But render text just in
                                    // case
                                    appendTextChildren(
                                        element.childNodes(),
                                        preFormatted = true,
                                        baseUrl = baseUrl,
                                        onLinkClick = onLinkClick,
                                    )
                                    emitParagraph()
                                }
                            }
                        } else {
                            // inline code
                            withComposableStyle(
                                style = { CodeInlineStyle() },
                            ) {
                                appendTextChildren(
                                    element.childNodes(),
                                    preFormatted = preFormatted,
                                    baseUrl = baseUrl,
                                    onLinkClick = onLinkClick,
                                )
                            }
                        }
                    }

                    "blockquote" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { BlockQuoteStyle() },
                            ) {
                                appendTextChildren(
                                    element.childNodes(),
                                    baseUrl = baseUrl,
                                    onLinkClick = onLinkClick,
                                )
                            }
                        }
                    }

                    "a" -> {
                        withComposableStyle(
                            style = { LinkTextStyle().toSpanStyle() },
                        ) {
                            withAnnotation("URL", element.attr("abs:href") ?: "") {
                                appendTextChildren(
                                    element.childNodes(),
                                    baseUrl = baseUrl,
                                    onLinkClick = onLinkClick,
                                )
                            }
                        }
                    }

                    "figure" -> {
                        emitParagraph()

                        // Wordpress likes nested figures to get images side by side
                        if (this is LazyListComposer) {
                            val imgElement = element.firstDescendant(tagName = "img")

                            if (imgElement != null) {
                                val composer = EagerComposer { paragraphBuilder, textStyler ->
                                    val dimens = LocalDimens.current
                                    ParagraphText(
                                        paragraphBuilder = paragraphBuilder,
                                        textStyler = textStyler,
                                        modifier = Modifier
                                            .width(dimens.maxContentWidth),
                                        onLinkClick = onLinkClick,
                                    )
                                }

                                item {
                                    with(composer) {
                                        val dimens = LocalDimens.current
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .width(dimens.maxContentWidth),
                                        ) {
                                            withTextStyle(NestedTextStyle.CAPTION) {
                                                appendTextChildren(
                                                    element.childNodes(),
                                                    baseUrl = baseUrl,
                                                    onLinkClick = onLinkClick,
                                                )
                                            }
                                            render()
                                        }
                                    }
                                }
                            }
                        } else if (this is EagerComposer) {
                            appendTextChildren(
                                element.childNodes(),
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                            )
                        }
                    }

                    "img" -> {
                        appendImage(onLinkClick = onLinkClick) { onClick ->
                            val dimens = LocalDimens.current
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .width(dimens.maxContentWidth),
                            ) {
                                renderImage(
                                    baseUrl = baseUrl,
                                    onClick = onClick,
                                    element = element,
                                )
                            }
                        }
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
                                        onLinkClick = onLinkClick,
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
                                        onLinkClick = onLinkClick,
                                    )
                                }
                            }
                    }

                    "table" -> {
                        if (this is LazyListComposer) {
                            appendTable(
                                baseUrl = baseUrl,
                                onLinkClick = onLinkClick,
                                element = element,
                            )
                        }
                    }

                    "iframe" -> {
                        val video: Video? = getVideo(element.attr("abs:src"))

                        if (video != null) {
                            appendImage(onLinkClick = onLinkClick) {
                                val dimens = LocalDimens.current
                                Column(
                                    modifier = Modifier
                                        .width(dimens.maxContentWidth),
                                ) {
                                    DisableSelection {
                                        BoxWithConstraints(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            val imageWidth by rememberMaxImageWidth()
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .placeholder(R.drawable.youtube_icon)
                                                    .error(R.drawable.youtube_icon)
                                                    .scale(Scale.FIT)
                                                    .size(imageWidth)
                                                    .precision(Precision.INEXACT)
                                                    .build(),
                                                contentScale = ContentScale.FillWidth,
                                                contentDescription = stringResource(R.string.touch_to_play_video),
                                                modifier = Modifier
                                                    .clickable {
                                                        onLinkClick(video.link)
                                                    }
                                                    .fillMaxWidth(),
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    ProvideScaledText(
                                        MaterialTheme.typography.labelMedium.merge(
                                            TextStyle(color = MaterialTheme.colorScheme.onBackground),
                                        ),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.touch_to_play_video),
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "rt", "rp" -> {
                        // Ruby text elements. Not rendering them might be better than not
                        // handling them well
                    }

                    "video" -> {
                        // not implemented yet. remember to disable selection
                    }

                    else -> {
                        appendTextChildren(
                            nodes = element.childNodes(),
                            preFormatted = preFormatted,
                            baseUrl = baseUrl,
                            onLinkClick = onLinkClick,
                        )
                    }
                }
            }
        }

        node = node.nextSibling()
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.renderImage(
    baseUrl: String,
    onClick: (() -> Unit)?,
    element: Element,
) {
    val imageCandidates by remember {
        derivedStateOf {
            getImageSource(baseUrl, element)
        }
    }

    // Some sites are silly and insert formatting in alt text
    val alt by remember {
        derivedStateOf {
            stripHtml(element.attr("alt") ?: "")
        }
    }

    DisableSelection {
        BoxWithConstraints(
            modifier = Modifier
                .clip(RectangleShape)
                .clickable(
                    enabled = onClick != null,
                ) {
                    onClick?.invoke()
                }
                .fillMaxWidth(),
        ) {
            val imageWidth by rememberMaxImageWidth()
            WithTooltipIfNotBlank(tooltip = alt) { modifier ->
                if (imageCandidates.notHasImage) {
                    Image(
                        rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                        contentDescription = alt,
                        contentScale = ContentScale.FillWidth,
                        modifier = modifier
                            .fillMaxWidth(),
                    )
                } else {
                    val pixelDensity = LocalDensity.current.density
                    val bestImage by remember {
                        derivedStateOf {
                            imageCandidates.getBestImageForMaxSize(
                                pixelDensity = pixelDensity,
                                maxWidth = imageWidth,
                            )
                        }
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bestImage)
                            .scale(Scale.FIT)
                            .size(imageWidth)
                            .precision(Precision.INEXACT)
                            .build(),
                        contentDescription = alt,
                        placeholder = rememberTintedVectorPainter(
                            Icons.Outlined.Terrain,
                        ),
                        error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                        contentScale = ContentScale.FillWidth,
                        modifier = modifier
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }

    // Figure has own caption so don't use alt text as caption there
    val notFigureAncestor by remember {
        derivedStateOf {
            (element.notAncestorOf("figure"))
        }
    }
    if (notFigureAncestor) {
        if (alt.isNotBlank()) {
            ProvideScaledText(
                MaterialTheme.typography.labelMedium.merge(
                    TextStyle(color = MaterialTheme.colorScheme.onBackground),
                ),
            ) {
                Text(
                    alt,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun LazyListComposer.appendTable(
    baseUrl: String,
    onLinkClick: (String) -> Unit,
    element: Element,
) {
    emitParagraph()

    val imgDescendant = element.hasDescendant("img")

    if (imgDescendant) {
        appendTextChildren(element.childNodes(), baseUrl = baseUrl, onLinkClick = onLinkClick)
    } else {
        item {
            val composer = EagerComposer { paragraphBuilder, textStyler ->
                ParagraphText(
                    paragraphBuilder = paragraphBuilder,
                    textStyler = textStyler,
                    modifier = Modifier,
                    onLinkClick = onLinkClick,
                )
            }
            with(composer) {
                tableColFirst(baseUrl = baseUrl, onLinkClick = onLinkClick, element = element)
            }
        }
    }
}

@Composable
private fun EagerComposer.tableColFirst(
    baseUrl: String,
    onLinkClick: (String) -> Unit,
    element: Element,
) {
    val rowCount by remember {
        derivedStateOf {
            element.descendants("tr").count()
        }
    }
    val colCount by remember {
        derivedStateOf {
            element.descendants("tr")
                .map { row ->
                    row.descendants()
                        .filter {
                            it.tagName() in setOf("th", "td")
                        }.count()
                }.max()
        }
    }

    /*
    In this order:
    optionally a caption element (containing text children for instance),
    followed by zero or more colgroup elements,
    followed optionally by a thead element,
    followed by either zero or more tbody elements
    or one or more tr elements,
    followed optionally by a tfoot element
     */
    val dimens = LocalDimens.current
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(dimens.maxContentWidth),
    ) {
        key(element, baseUrl, onLinkClick) {
            element.children()
                .filter { it.tagName() == "caption" }
                .forEach {
                    withTextStyle(NestedTextStyle.CAPTION) {
                        appendTextChildren(
                            it.childNodes(),
                            baseUrl = baseUrl,
                            onLinkClick = onLinkClick,
                        )
                    }
                    render()
                }
        }

        val rowData by remember {
            derivedStateOf {
                element.children()
                    .filter {
                        it.tagName() in setOf(
                            "thead",
                            "tbody",
                            "tfoot",
                        )
                    }
                    .sortedBy {
                        when (it.tagName()) {
                            "thead" -> 0
                            "tbody" -> 1
                            "tfoot" -> 10
                            else -> 2
                        }
                    }
                    .flatMap {
                        it.children()
                            .filter { child -> child.tagName() == "tr" }
                            .map { child ->
                                it.tagName() to child
                            }
                    }
            }
        }

        key(rowCount, colCount, rowData, baseUrl, onLinkClick) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .width(dimens.maxContentWidth),
            ) {
                items(
                    count = colCount,
                ) { colIndex ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier,
                    ) {
                        for (rowIndex in 0 until rowCount) {
                            val (section, rowElement) = rowData.getOrNull(rowIndex) ?: break
                            var emptyCell = false
                            Surface(
                                tonalElevation = when (section) {
                                    "thead" -> 3.dp
                                    "tbody" -> 0.dp
                                    "tfoot" -> 1.dp
                                    else -> 0.dp
                                },
                            ) {
                                rowElement.children()
                                    .filter { it.tagName() in setOf("th", "td") }
                                    .elementAtOrNullWithSpans(colIndex)
                                    ?.let { colElement ->
                                        withParagraph {
                                            withStyle(
                                                if (colElement.tagName() == "th") {
                                                    SpanStyle(fontWeight = FontWeight.Bold)
                                                } else {
                                                    null
                                                },
                                            ) {
                                                appendTextChildren(
                                                    colElement.childNodes(),
                                                    baseUrl = baseUrl,
                                                    onLinkClick = onLinkClick,
                                                )
                                            }
                                        }
                                    }
                                emptyCell = !render()
                            }
                            if (emptyCell) {
                                // An empty cell looks better if it has some height - but don't want
                                // the surface because having one space wide surface is weird
                                append(' ')
                                render()
                            }
                        }
                    }
                }
            }
        }
    }
}

// Just ensures that columns coming after a spanned entry ends up in the right column
fun Iterable<Element>.elementAtOrNullWithSpans(index: Int): Element? {
    var currentColumn = 0
    forEach {
        if (currentColumn > index) {
            // Span over this column
            return null
        }
        if (currentColumn == index) {
            return it
        }
        val spans = it.attr("colspan") ?: "1"
        currentColumn += when (val spanCount = spans.toIntOrNull()) {
            null, 1 -> (spanCount ?: 1)
            0 -> return null // Firefox special - spans to end
            else -> spanCount.coerceAtLeast(1)
        }
    }
    return null
}

private fun Element.descendants(tagName: String): Sequence<Element> {
    return descendants().filter { it.tagName() == tagName }
}

private fun Element.descendants(): Sequence<Element> {
    return sequence {
        children().forEach {
            recursiveSequence(it)
        }
    }
}

private suspend fun SequenceScope<Element>.recursiveSequence(element: Element) {
    yield(element)

    element.children().forEach {
        recursiveSequence(it)
    }
}

private fun Element.hasDescendant(tagName: String): Boolean {
    return descendants(tagName).any()
}

private fun Element.firstDescendant(tagName: String): Element? {
    return descendants(tagName).firstOrNull()
}

private fun Element.notAncestorOf(tagName: String): Boolean {
    var current: Element? = this

    while (current != null) {
        val parent = current.parent()

        current = when {
            parent == null || parent.tagName() == "#root" -> {
                null
            }

            parent.tagName() == tagName -> {
                return false
            }

            else -> {
                parent
            }
        }
    }

    return true
}

private enum class NestedTextStyle : TextStyler {
    CAPTION {
        @Composable
        override fun textStyle() = MaterialTheme.typography.labelMedium.merge(
            TextStyle(color = MaterialTheme.colorScheme.onBackground),
        )
    },
}

private fun String.asFontFamily(): FontFamily? = when (this.lowercase()) {
    "monospace" -> FontFamily.Monospace
    "serif" -> FontFamily.Serif
    "sans-serif" -> FontFamily.SansSerif
    else -> null
}

@Preview
@Composable
private fun TestIt() {
    val html = """
        <p>In Gimp you go to <em>Image</em> in the top menu bar and select <em>Mode</em> followed by <em>Indexed</em>. Now you see a popup where you can select the number of colors for a generated optimum palette.</p> <p>You&rsquo;ll have to experiment a little because it will depend on your image.</p> <p>I used this approach to shrink the size of the cover image in <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">the_zopfli post</a> from a 37KB (JPG) to just 15KB (PNG, all PNG sizes listed include Zopfli compression btw).</p> <h2 id="straight-jpg-to-png-conversion-124kb">Straight JPG to PNG conversion: 124KB</h2> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things.png" alt="PNG version RGB colors" /></p> <p>First off, I exported the JPG file as a PNG file. This PNG file had a whopping 124KB! Clearly there was some bloat being stored.</p> <h2 id="256-colors-40kb">256 colors: 40KB</h2> <p>Reducing from RGB to only 256 colors has no visible effect to my eyes.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_256.png" alt="256 colors" /></p> <h2 id="128-colors-34kb">128 colors: 34KB</h2> <p>Still no difference.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_128.png" alt="128 colors" /></p> <h2 id="64-colors-25kb">64 colors: 25KB</h2> <p>You can start to see some artifacting in the shadow behind the text.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_64.png" alt="64 colors" /></p> <h2 id="32-colors-15kb">32 colors: 15KB</h2> <p>In my opinion this is the sweet spot. The shadow artifacting is barely noticable but the size is significantly reduced.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_32.png" alt="32 colors" /></p> <h2 id="16-colors-11kb">16 colors: 11KB</h2> <p>Clear artifacting in the text shadow and the yellow (fire?) in the background has developed an outline.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_16.png" alt="16 colors" /></p> <h2 id="8-colors-7-3kb">8 colors: 7.3KB</h2> <p>The broom has shifted in color from a clear brown to almost grey. Text shadow is just a grey blob at this point. Even clearer outline developed on the yellow background.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_8.png" alt="8 colors" /></p> <h2 id="4-colors-4-3kb">4 colors: 4.3KB</h2> <p>Interestingly enough, I think 4 colors looks better than 8 colors. The outline in the background has disappeared because there&rsquo;s not enough color spectrum to render it. The broom is now black and filled areas tend to get a white separator to the outlines.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_4.png" alt="4 colors" /></p> <h2 id="2-colors-2-4kb">2 colors: 2.4KB</h2> <p>Well, at least the silhouette is well defined at this point I guess.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_2.png" alt="2 colors" /></p> <hr/> <p>Other posts in the <b>Migrating from Ghost to Hugo</b> series:</p> <ul class="series"> <li>2016-10-21 &mdash; Reduce the size of images even further by reducing number of colors with Gimp </li> <li>2016-08-26 &mdash; <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">Compress all the images!</a> </li> <li>2016-07-25 &mdash; <a href="https://cowboyprogrammer.org/2016/07/migrating_from_ghost_to_hugo/">Migrating from Ghost to Hugo</a> </li> </ul>
    """.trimIndent()

    html.byteInputStream().use { stream ->
        LazyColumn {
            htmlFormattedText(
                inputStream = stream,
                baseUrl = "https://cowboyprogrammer.org",
            ) {}
        }
    }
}

@Composable
fun BoxWithConstraintsScope.rememberMaxImageWidth() = with(LocalDensity.current) {
    remember {
        derivedStateOf {
            maxWidth.toPx().roundToInt().coerceAtMost(2000)
        }
    }
}

/**
 * Gets the url to the image in the <img> tag - could be from srcset or from src
 */
internal fun getImageSource(baseUrl: String, element: Element) = ImageCandidates(
    baseUrl = baseUrl,
    srcSet = element.attr("srcset") ?: "",
    absSrc = element.attr("abs:src") ?: "",
)

internal class ImageCandidates(
    val baseUrl: String,
    val srcSet: String,
    val absSrc: String,
) {
    val hasImage: Boolean = srcSet.isNotBlank() || absSrc.isNotBlank()
    val notHasImage: Boolean = !hasImage

    /**
     * Might throw if hasImage returns false
     */
    fun getBestImageForMaxSize(maxWidth: Int, pixelDensity: Float): String {
        val setCandidate = srcSet.splitToSequence(", ")
            .map { it.trim() }
            .map { it.split(SpaceRegex).take(2).map { x -> x.trim() } }
            .fold(100f to "") { acc, candidate ->
                val candidateSize = if (candidate.size == 1) {
                    // Assume it corresponds to 1x pixel density
                    1.0f / pixelDensity
                } else {
                    val descriptor = candidate.last()
                    when {
                        descriptor.endsWith("w", ignoreCase = true) -> {
                            descriptor.substringBefore("w").toFloat() / maxWidth
                                .toFloat()
                        }

                        descriptor.endsWith("x", ignoreCase = true) -> {
                            descriptor.substringBefore("x").toFloat() / pixelDensity
                        }

                        else -> {
                            return@fold acc
                        }
                    }
                }

                if (abs(candidateSize - 1.0f) < abs(acc.first - 1.0f)) {
                    candidateSize to candidate.first()
                } else {
                    acc
                }
            }
            .second

        return StringUtil.resolve(
            baseUrl,
            setCandidate.takeIf { it.isNotBlank() } ?: absSrc,
        )
    }
}

private val SpaceRegex = Regex("\\s+")

/**
 * Can't use JSoup's text() method because that strips invisible characters
 * such as ZWNJ which are crucial for several languages.
 */
fun TextNode.appendCorrectlyNormalizedWhiteSpace(
    builder: HtmlParser,
    stripLeading: Boolean,
) {
    val string = wholeText

    var reachedNonWhite = false
    var lastWasWhite = false
    var i = 0
    while (i < string.length) {
        val code = string.codePointAt(i)

        // Unicode smileys are an example of where toChar() won't work. Needs to be String.
        val char = String(intArrayOf(code), 0, 1)
        i += Character.charCount(code)

        lastWasWhite = if (isCollapsableWhiteSpace(char)) {
            if (!(stripLeading && !reachedNonWhite || lastWasWhite)) {
                builder.append(' ')
            }
            true
        } else {
            reachedNonWhite = true
            builder.append(char)
            false
        }
    }
}

fun Element.appendCorrectlyNormalizedWhiteSpaceRecursively(
    builder: HtmlParser,
    stripLeading: Boolean,
) {
    for (child in childNodes()) {
        when (child) {
            is TextNode -> child.appendCorrectlyNormalizedWhiteSpace(builder, stripLeading)
            is Element -> child.appendCorrectlyNormalizedWhiteSpaceRecursively(
                builder,
                stripLeading,
            )
        }
    }
}

private const val space = ' '
private const val tab = '\t'
private const val linefeed = '\n'
private const val carriageReturn = '\r'

// 12 is form feed which as no escape in kotlin
private const val formFeed = 12.toChar()

// 160 is &nbsp; (non-breaking space). Not in the spec but expected.
private const val nonBreakableSpace = 160.toChar()

private fun isCollapsableWhiteSpace(c: String) =
    c.firstOrNull()?.let { isCollapsableWhiteSpace(it) } ?: false

private fun isCollapsableWhiteSpace(c: Char) =
    c == space || c == tab || c == linefeed || c == carriageReturn || c == formFeed || c == nonBreakableSpace

/**
 * Super basic function to strip html formatting from alt-texts.
 */
fun stripHtml(html: String): String {
    val result = StringBuilder()

    var skipping = false

    for (char in html) {
        if (!skipping) {
            if (char == '<') {
                skipping = true
            } else {
                result.append(char)
            }
        } else {
            if (char == '>') {
                skipping = false
            } else {
                // Skipping char
            }
        }
    }

    return result.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithTooltipIfNotBlank(
    tooltip: String,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    if (tooltip.isNotBlank()) {
        PlainTooltipBox(tooltip = { Text(tooltip) }) {
            content(modifier.tooltipAnchor())
        }
    } else {
        content(modifier)
    }
}
