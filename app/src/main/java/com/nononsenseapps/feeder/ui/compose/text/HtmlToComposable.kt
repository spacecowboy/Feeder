package com.nononsenseapps.feeder.ui.compose.text

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import coil.ImageLoader
import com.google.accompanist.coil.rememberCoilPainter
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.theme.BlockQuoteStyle
import com.nononsenseapps.feeder.ui.compose.theme.CodeBlockBackground
import com.nononsenseapps.feeder.ui.compose.theme.CodeBlockStyle
import com.nononsenseapps.feeder.ui.compose.theme.CodeInlineStyle
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.text.Video
import com.nononsenseapps.feeder.ui.text.getVideo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.kodein.di.compose.instance
import java.io.InputStream

fun LazyListScope.htmlFormattedText(
    inputStream: InputStream,
    baseUrl: String,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit
) {
    Jsoup.parse(inputStream, null, baseUrl)
        ?.body()
        ?.let { body ->
            formatBody(
                element = body,
                imagePlaceholder = imagePlaceholder,
                onLinkClick = onLinkClick
            )
        }
}

private fun LazyListScope.formatBody(
    element: Element,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
) {
    val composer = TextComposer { paragraphBuilder ->
        item {
            val paragraph = paragraphBuilder.toAnnotatedString()
            // TODO compose this prevents taps from deselecting selected text
            ClickableText(
                text = paragraph,
                style = MaterialTheme.typography.body1
                    .merge(TextStyle(color = MaterialTheme.colors.onBackground)),
                modifier = Modifier.fillMaxWidth()
            ) { offset ->
                // TODO("on click with offset / index position")
                paragraph.getStringAnnotations("URL", offset, offset)
                    .firstOrNull()
                    ?.let {
                        onLinkClick(it.item)
                    }
            }
        }
    }

    composer.appendTextChildren(
        element.childNodes(),
        lazyListScope = this,
        imagePlaceholder = imagePlaceholder,
        onLinkClick = onLinkClick,
    )

    composer.terminateCurrentText()
}

private fun LazyListScope.formatCodeBlock(
    element: Element,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
) {
    val composer = TextComposer { paragraphBuilder ->
        item {
            val scrollState = rememberScrollState()
            Surface(
                color = CodeBlockBackground(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .horizontalScroll(
                        state = scrollState
                    )
                    .fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(all = 4.dp)) {
                    Text(
                        text = paragraphBuilder.toAnnotatedString(),
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
    )

    composer.terminateCurrentText()
}

@OptIn(ExperimentalComposeApi::class)
private fun TextComposer.appendTextChildren(
    nodes: List<Node>,
    preFormatted: Boolean = false,
    lazyListScope: LazyListScope,
    @DrawableRes imagePlaceholder: Int,
    onLinkClick: (String) -> Unit,
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
                        // Readability4j inserts p-tags in divs for algorithmic purposes.
                        // They screw up formatting.
                        if (node.hasClass("readability-styled")) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
                            )
                        } else {
                            withParagraph {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onLinkClick = onLinkClick,
                                )
                            }
                        }
                    }
                    "br" -> append('\n')
                    "h1" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.h1.toSpanStyle() }
                            ) {
                                append(element.text())
                            }
                        }
                    }
                    "h2" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.h2.toSpanStyle() }
                            ) {
                                append(element.text())
                            }
                        }
                    }
                    "h3" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.h3.toSpanStyle() }
                            ) {
                                append(element.text())
                            }
                        }
                    }
                    "h4" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.h4.toSpanStyle() }
                            ) {
                                append(element.text())
                            }
                        }
                    }
                    "h5" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.h5.toSpanStyle() }
                            ) {
                                append(element.text())
                            }
                        }
                    }
                    "h6" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { MaterialTheme.typography.h6.toSpanStyle() }
                            ) {
                                append(element.text())
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
                        )
                    }
                    "code" -> {
                        if (element.parent()?.tagName() == "pre") {
                            terminateCurrentText()
                            lazyListScope.formatCodeBlock(
                                element = element,
                                imagePlaceholder = imagePlaceholder,
                                onLinkClick = onLinkClick,
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
                                )
                            }
                        }
                    }
                    "img" -> {
                        val imageUrl = element.attr("abs:src") ?: ""
                        if (imageUrl.isNotBlank()) {
                            val alt = element.attr("alt") ?: ""
                            appendImage(onLinkClick = onLinkClick) { onClick ->
                                lazyListScope.item {
                                    val imageLoader: ImageLoader by instance()
                                    // TODO rememberSaveable to retain this when scrolled off screen
                                    val scale = remember { mutableStateOf(1f) }
                                    DisableSelection {
                                        Box(
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
                                            Image(
                                                painter = rememberCoilPainter(
                                                    request = imageUrl,
                                                    imageLoader = imageLoader,
                                                    requestBuilder = {
                                                        this.error(imagePlaceholder)
                                                    },
                                                    previewPlaceholder = imagePlaceholder,
                                                    shouldRefetchOnSizeChange = { _, _ -> false },
                                                ),
                                                contentDescription = alt,
                                                contentScale = ContentScale.FillWidth,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .graphicsLayer {
                                                        scaleX = scale.value
                                                        scaleY = scale.value
                                                    }

                                            )
                                        }
                                    }

                                    if (alt.isNotBlank()) {
                                        Text(
                                            alt,
                                            style = MaterialTheme.typography.caption,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

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
                                    )
                                    terminateCurrentText()
                                }

                            append("\n\n")
                        }
                    }
                    "iframe" -> {
                        // TODO actual video player
                        val video: Video? = getVideo(element.attr("abs:src"))

                        if (video != null) {
                            appendImage(onLinkClick = onLinkClick) {
                                lazyListScope.item {
                                    DisableSelection {
                                        val imageLoader: ImageLoader by instance()
                                        Image(
                                            painter = rememberCoilPainter(
                                                request = video.imageUrl,
                                                imageLoader = imageLoader,
                                                requestBuilder = {
                                                    this.error(R.drawable.youtube_icon)
                                                },
                                                previewPlaceholder = R.drawable.youtube_icon,
                                            ),
                                            contentDescription = stringResource(R.string.touch_to_play_video),
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier
                                                .clickable {
                                                    onLinkClick(video.link)
                                                }
                                                .fillMaxWidth()
                                        )
                                    }

                                    Text(
                                        text = stringResource(R.string.touch_to_play_video),
                                        style = MaterialTheme.typography.caption,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                    "video" -> {
                        // TODO and remember to disable selection
                    }
                    else -> {
                        appendTextChildren(
                            nodes = element.childNodes(),
                            preFormatted = preFormatted,
                            lazyListScope = lazyListScope,
                            imagePlaceholder = imagePlaceholder,
                            onLinkClick = onLinkClick,
                        )
                    }
                }
            }
        }

        node = node.nextSibling()
    }
}

@OptIn(ExperimentalStdlibApi::class)
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
