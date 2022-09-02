package com.nononsenseapps.feeder.ui.compose.text

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.nononsenseapps.feeder.ui.compose.theme.BlockQuoteStyle
import com.nononsenseapps.feeder.ui.compose.theme.CodeBlockBackground
import com.nononsenseapps.feeder.ui.compose.theme.CodeBlockStyle
import com.nononsenseapps.feeder.ui.compose.theme.CodeInlineStyle
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
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

fun LazyListScope.htmlFormattedText(
    inputStream: InputStream,
    baseUrl: String,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
) {
    Jsoup.parse(inputStream, null, baseUrl)
        ?.body()
        ?.let { body ->
            formatBody(
                element = body,
                imagePlaceholder = imagePlaceholder,
                onLinkClick = onLinkClick,
                baseUrl = baseUrl,
            )
        }
}

private fun LazyListScope.formatBody(
    element: Element,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    val composer = TextComposer { paragraphBuilder ->
        item {
            val dimens = LocalDimens.current
            val paragraph = paragraphBuilder.toComposableAnnotatedString()

            withBidiDeterminedLayoutDirection(paragraph.text) {
                // ClickableText prevents taps from deselecting selected text
                // So use regular Text if possible
                if (
                    paragraph.getStringAnnotations("URL", 0, paragraph.length)
                        .isNotEmpty()
                ) {
                    ClickableText(
                        text = paragraph,
                        style = MaterialTheme.typography.bodyLarge
                            .merge(TextStyle(color = MaterialTheme.colorScheme.onBackground)),
                        modifier = Modifier
                            .width(dimens.maxContentWidth)
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
                        style = MaterialTheme.typography.bodyLarge
                            .merge(TextStyle(color = MaterialTheme.colorScheme.onBackground)),
                        modifier = Modifier
                            .width(dimens.maxContentWidth)
                    )
                }
            }
        }
    }

    composer.appendTextChildren(
        element.childNodes(),
        lazyListScope = this,
        imagePlaceholder = imagePlaceholder,
        onLinkClick = onLinkClick,
        baseUrl = baseUrl,
    )

    composer.terminateCurrentText()
}

private fun LazyListScope.formatCodeBlock(
    element: Element,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    val composer = TextComposer { paragraphBuilder ->
        item {
            val dimens = LocalDimens.current
            val scrollState = rememberScrollState()
            Surface(
                color = CodeBlockBackground(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .horizontalScroll(
                        state = scrollState
                    )
                    .width(dimens.maxContentWidth)
            ) {
                Box(modifier = Modifier.padding(all = 4.dp)) {
                    Text(
                        text = paragraphBuilder.toComposableAnnotatedString(),
                        style = CodeBlockStyle(),
                        softWrap = false
                    )
                }
            }
        }
    }

    composer.appendTextChildren(
        element.childNodes(), preFormatted = true,
        lazyListScope = this,
        imagePlaceholder = imagePlaceholder,
        onLinkClick = onLinkClick,
        baseUrl = baseUrl,
    )

    composer.terminateCurrentText()
}

private fun TextComposer.appendTextChildren(
    nodes: List<Node>,
    preFormatted: Boolean = false,
    lazyListScope: LazyListScope,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
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
                        stripLeading = endsWithWhitespace
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
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        } else {
                            withParagraph {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }
                    "br" -> append('\n')
                    "h1" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }
                    "h2" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }
                    "h3" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }
                    "h4" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }
                    "h5" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }
                    "h6" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.headlineSmall.toSpanStyle() }
                            ) {
                                element.appendCorrectlyNormalizedWhiteSpaceRecursively(
                                    this,
                                    stripLeading = endsWithWhitespace
                                )
                            }
                        }
                    }
                    "strong", "b" -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "i", "em", "cite", "dfn" -> {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "tt" -> {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "u" -> {
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "sup" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Superscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "sub" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Subscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "font" -> {
                        val fontFamily: FontFamily? = element.attr("face")?.asFontFamily()
                        withStyle(SpanStyle(fontFamily = fontFamily)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }
                    "pre" -> {
                        appendTextChildren(
                            element.childNodes(),
                            preFormatted = true,
                            lazyListScope = lazyListScope,
                            imagePlaceholder = imagePlaceholder,
                            onLinkClick = onLinkClick,
                            baseUrl = baseUrl,
                        )
                    }
                    "code" -> {
                        if (element.parent()?.tagName() == "pre") {
                            terminateCurrentText()
                            lazyListScope.formatCodeBlock(
                                element = element,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        } else {
                            // inline code
                            withComposableStyle(
                                style = { CodeInlineStyle() }
                            ) {
                                appendTextChildren(
                                    element.childNodes(),
                                    preFormatted = preFormatted,
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }
                    "blockquote" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { BlockQuoteStyle() }
                            ) {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }
                    "a" -> {
                        withComposableStyle(
                            style = { LinkTextStyle().toSpanStyle() }
                        ) {
                            withAnnotation("URL", element.attr("abs:href") ?: "") {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }
                    "img" -> {
                        val imageCandidates = getImageSource(baseUrl, element)
                        if (imageCandidates.hasImage) {
                            val alt = element.attr("alt") ?: ""
                            appendImage(onLinkClick = onLinkClick) { onClick ->
                                lazyListScope.item {
                                    val dimens = LocalDimens.current
//                                    val scale = remember { mutableStateOf(1f) }
                                    Column(
                                        modifier = Modifier
                                            .width(dimens.maxContentWidth)
                                    ) {
                                        DisableSelection {
                                            BoxWithConstraints(
                                                modifier = Modifier
                                                    .clip(RectangleShape)
                                                    .clickable(
                                                        enabled = onClick != null
                                                    ) {
                                                        onClick?.invoke()
                                                    }
                                                    .fillMaxWidth()
                                                // This makes scrolling a pain, find a way to solve that
//                                            .pointerInput("imgzoom") {
//                                                detectTransformGestures { centroid, pan, zoom, rotation ->
//                                                    val z = zoom * scale.value
//                                                    scale.value = when {
//                                                        z < 1f -> 1f
//                                                        z > 3f -> 3f
//                                                        else -> z
//                                                    }
//                                                }
//                                            }
                                            ) {
                                                val imageWidth = maxImageWidth()
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(
                                                            imageCandidates.getBestImageForMaxSize(
                                                                pixelDensity = pixelDensity(),
                                                                maxWidth = imageWidth,
                                                            )
                                                        )
                                                        .placeholder(imagePlaceholder)
                                                        .error(imagePlaceholder)
                                                        .scale(Scale.FIT)
                                                        .size(imageWidth)
                                                        .precision(Precision.INEXACT)
                                                        .build(),
                                                    contentScale = ContentScale.FillWidth,
                                                    contentDescription = alt,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                )
                                            }
                                        }

                                        if (alt.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(16.dp))

                                            Text(
                                                alt,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
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
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
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
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
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
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
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
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
                                        baseUrl = baseUrl,
                                    )
                                    terminateCurrentText()
                                }

                            append("\n\n")
                        }
                    }
                    "iframe" -> {
                        val video: Video? = getVideo(element.attr("abs:src"))

                        if (video != null) {
                            appendImage(onLinkClick = onLinkClick) {
                                lazyListScope.item {
                                    val dimens = LocalDimens.current
                                    Column(
                                        modifier = Modifier
                                            .width(dimens.maxContentWidth)
                                    ) {
                                        DisableSelection {
                                            BoxWithConstraints(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                val imageWidth = maxImageWidth()
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
                                                        .fillMaxWidth()
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = stringResource(R.string.touch_to_play_video),
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
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
                            lazyListScope = lazyListScope,
                            imagePlaceholder = imagePlaceholder,
                            onLinkClick = onLinkClick,
                            baseUrl = baseUrl,
                        )
                    }
                }
            }
        }

        node = node.nextSibling()
    }
}

private fun String.asFontFamily(): FontFamily? = when (this.lowercase()) {
    "monospace" -> FontFamily.Monospace
    "serif" -> FontFamily.Serif
    "sans-serif" -> FontFamily.SansSerif
    else -> null
}

@Preview
@Composable
private fun testIt() {
    val html = """
        <p>In Gimp you go to <em>Image</em> in the top menu bar and select <em>Mode</em> followed by <em>Indexed</em>. Now you see a popup where you can select the number of colors for a generated optimum palette.</p> <p>You&rsquo;ll have to experiment a little because it will depend on your image.</p> <p>I used this approach to shrink the size of the cover image in <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">the_zopfli post</a> from a 37KB (JPG) to just 15KB (PNG, all PNG sizes listed include Zopfli compression btw).</p> <h2 id="straight-jpg-to-png-conversion-124kb">Straight JPG to PNG conversion: 124KB</h2> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things.png" alt="PNG version RGB colors" /></p> <p>First off, I exported the JPG file as a PNG file. This PNG file had a whopping 124KB! Clearly there was some bloat being stored.</p> <h2 id="256-colors-40kb">256 colors: 40KB</h2> <p>Reducing from RGB to only 256 colors has no visible effect to my eyes.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_256.png" alt="256 colors" /></p> <h2 id="128-colors-34kb">128 colors: 34KB</h2> <p>Still no difference.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_128.png" alt="128 colors" /></p> <h2 id="64-colors-25kb">64 colors: 25KB</h2> <p>You can start to see some artifacting in the shadow behind the text.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_64.png" alt="64 colors" /></p> <h2 id="32-colors-15kb">32 colors: 15KB</h2> <p>In my opinion this is the sweet spot. The shadow artifacting is barely noticable but the size is significantly reduced.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_32.png" alt="32 colors" /></p> <h2 id="16-colors-11kb">16 colors: 11KB</h2> <p>Clear artifacting in the text shadow and the yellow (fire?) in the background has developed an outline.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_16.png" alt="16 colors" /></p> <h2 id="8-colors-7-3kb">8 colors: 7.3KB</h2> <p>The broom has shifted in color from a clear brown to almost grey. Text shadow is just a grey blob at this point. Even clearer outline developed on the yellow background.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_8.png" alt="8 colors" /></p> <h2 id="4-colors-4-3kb">4 colors: 4.3KB</h2> <p>Interestingly enough, I think 4 colors looks better than 8 colors. The outline in the background has disappeared because there&rsquo;s not enough color spectrum to render it. The broom is now black and filled areas tend to get a white separator to the outlines.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_4.png" alt="4 colors" /></p> <h2 id="2-colors-2-4kb">2 colors: 2.4KB</h2> <p>Well, at least the silhouette is well defined at this point I guess.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_2.png" alt="2 colors" /></p> <hr/> <p>Other posts in the <b>Migrating from Ghost to Hugo</b> series:</p> <ul class="series"> <li>2016-10-21 &mdash; Reduce the size of images even further by reducing number of colors with Gimp </li> <li>2016-08-26 &mdash; <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">Compress all the images!</a> </li> <li>2016-07-25 &mdash; <a href="https://cowboyprogrammer.org/2016/07/migrating_from_ghost_to_hugo/">Migrating from Ghost to Hugo</a> </li> </ul>
    """.trimIndent()

    html.byteInputStream().use { stream ->
        LazyColumn {
            htmlFormattedText(
                inputStream = stream,
                baseUrl = "https://cowboyprogrammer.org",
                imagePlaceholder = R.drawable.placeholder_image_article_night,
                onLinkClick = {}
            )
        }
    }
}

@Composable
private fun pixelDensity() = with(LocalDensity.current) {
    density
}

@Composable
fun BoxWithConstraintsScope.maxImageWidth() = with(LocalDensity.current) {
    maxWidth.toPx().roundToInt().coerceAtMost(2000)
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
    val absSrc: String
) {
    val hasImage: Boolean = srcSet.isNotBlank() || absSrc.isNotBlank()

    /**
     * Might throw if hasImage returns false
     */
    fun getBestImageForMaxSize(maxWidth: Int, pixelDensity: Float): String {
        val setCandidate = srcSet.splitToSequence(",")
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

        if (setCandidate.isNotBlank()) {
            return StringUtil.resolve(baseUrl, setCandidate)
        }
        return StringUtil.resolve(baseUrl, absSrc)
    }
}

private val SpaceRegex = Regex("\\s+")

/**
 * Can't use JSoup's text() method because that strips invisible characters
 * such as ZWNJ which are crucial for several languages.
 */
fun TextNode.appendCorrectlyNormalizedWhiteSpace(
    builder: TextComposer,
    stripLeading: Boolean
) {
    val string = wholeText

    var reachedNonWhite = false
    var lastWasWhite = false
    var i = 0
    while (i < string.length) {
        val code = string.codePointAt(i)
        val char = code.toChar()
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
    builder: TextComposer,
    stripLeading: Boolean
) {
    for (child in childNodes()) {
        when (child) {
            is TextNode -> child.appendCorrectlyNormalizedWhiteSpace(builder, stripLeading)
            is Element -> child.appendCorrectlyNormalizedWhiteSpaceRecursively(
                builder,
                stripLeading
            )
        }
    }
}

// 12 is form feed which as no escape in kotlin
// 160 is &nbsp; (non-breaking space). Not in the spec but expected.
private fun isCollapsableWhiteSpace(c: Char) =
    c == ' ' || c == '\t' || c == '\n' || c == 12.toChar() || c == '\r' || c == 160.toChar()
