package com.nononsenseapps.feeder.ui.compose.text

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.google.accompanist.coil.rememberCoilPainter
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.theme.Typography
import com.nononsenseapps.feeder.ui.text.Video
import com.nononsenseapps.feeder.ui.text.getVideo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.kodein.di.compose.instance
import java.io.InputStream

@Composable
fun HtmlFormattedText(inputStream: InputStream, baseUrl: String) {
    // TODO some kind of column
    Jsoup.parse(inputStream, null, baseUrl)
        ?.body()
        ?.let { body ->
            FormatBody(element = body)
        }
}

@Composable
fun FormatBody(element: Element) {
    val composer = TextComposer { paragraph ->
        ClickableText(
            text = paragraph,
            style = Typography.body1
        ) { offset ->
            // TODO("on click with offset / index position")
            paragraph.getStringAnnotations("URL", offset, offset)
                .firstOrNull()
                ?.let {
                    // TODO handle click
                    Log.i("JONAS", "Clicked on ${it.item}")
                }
        }
    }

    composer.appendTextChildren(element.childNodes())

    composer.terminateCurrentText()
}

@OptIn(ExperimentalComposeApi::class)
@Composable
fun TextComposer.appendTextChildren(nodes: List<Node>, preFormatted: Boolean = false) {
    var node = nodes.firstOrNull()
    while (node != null) {
        when (node) {
            is TextNode -> append(
                if (preFormatted) {
                    node.wholeText
                } else {
                    node.text()
                }
            )
            is Element -> {
                val element = node
                when (element.tagName()) {
                    "p", "div" -> {
                        withParagraph {
                            appendTextChildren(element.childNodes())
                        }
                    }
                    "br" -> append('\n')
                    "h1" -> {
                        withParagraph {
                            withStyle(Typography.h1.toSpanStyle()) {
                                append(element.text())
                            }
                        }
                    }
                    "h2" -> {
                        withParagraph {
                            withStyle(Typography.h2.toSpanStyle()) {
                                append(element.text())
                            }
                        }
                    }
                    "h3" -> {
                        withParagraph {
                            withStyle(Typography.h3.toSpanStyle()) {
                                append(element.text())
                            }
                        }
                    }
                    "h4" -> {
                        withParagraph {
                            withStyle(Typography.h4.toSpanStyle()) {
                                append(element.text())
                            }
                        }
                    }
                    "h5" -> {
                        withParagraph {
                            withStyle(Typography.h5.toSpanStyle()) {
                                append(element.text())
                            }
                        }
                    }
                    "h6" -> {
                        withParagraph {
                            withStyle(Typography.h6.toSpanStyle()) {
                                append(element.text())
                            }
                        }
                    }
                    "strong", "b" -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            appendTextChildren(element.childNodes())
                        }
                    }
                    "i", "em", "cite", "dfn" -> {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            appendTextChildren(element.childNodes())
                        }
                    }
                    "tt" -> {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                            appendTextChildren(element.childNodes())
                        }
                    }
                    "u" -> {
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            appendTextChildren(element.childNodes())
                        }
                    }
                    "sup" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Superscript)) {
                            appendTextChildren(element.childNodes())
                        }
                    }
                    "sub" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Subscript)) {
                            appendTextChildren(element.childNodes())
                        }
                    }
                    "font" -> {
                        // TODO color
//                        val color: Color = element.attr("color")?.asColor() ?: Color.Unspecified
                        val color = Color.Unspecified
                        val fontFamily: FontFamily? = element.attr("face")?.asFontFamily()
                        withStyle(SpanStyle(color = color, fontFamily = fontFamily)) {
                            appendTextChildren(element.childNodes())
                        }
                    }
                    "pre" -> {
                        appendTextChildren(element.childNodes(), preFormatted = true)
                    }
                    "code" -> {
                        // TODO background
                        withStyle(SpanStyle(background = Color.Magenta)) {
                            appendTextChildren(element.childNodes(), preFormatted = preFormatted)
                        }
                    }
                    "blockquote" -> {
                        withParagraph {
                            // TODO move to typography
                            withStyle(
                                SpanStyle(color = Color.Red, fontWeight = FontWeight.Light)
                            ) {
                                append(element.text())
                            }
                        }
                    }
                    "a" -> {
                        // TODO clickable and colors
                        withStyle(
                            SpanStyle(
                                color = Color.Cyan,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            withAnnotation("URL", element.attr("abs:href") ?: "") {
                                append(element.text())
                            }
                        }
                    }
                    "img" -> {
                        val imageUrl = element.attr("abs:src") ?: ""
                        if (imageUrl.isNotBlank()) {
                            appendImage { onClick ->
                                val imageLoader: ImageLoader by instance()

                                val alt = element.attr("alt") ?: ""
                                Image(
                                    painter = rememberCoilPainter(
                                        request = imageUrl,
                                        imageLoader = imageLoader,
                                        requestBuilder = {
                                            this.error(R.drawable.placeholder_image_article_day)
                                        },
                                        previewPlaceholder = R.drawable.placeholder_image_article_night,
                                        shouldRefetchOnSizeChange = { _, _ -> false },
                                    ),
                                    contentDescription = alt,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .clickable(
                                            enabled = onClick != null
                                        ) {
                                            onClick?.invoke()
                                        }
                                        .fillMaxWidth()
                                )

                                if (alt.isNotBlank()) {
                                    Text(alt, style = Typography.caption)
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
                                    appendTextChildren(listItem.childNodes())
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
                                    appendTextChildren(listItem.childNodes())
                                }
                            }
                    }
                    "table" -> {
                        // TODO
                    }
                    "iframe" -> {
                        // TODO actual video player
                        val video: Video? = getVideo(element.attr("abs:src"))

                        if (video != null) {
                            appendImage {
                                Image(
                                    painter = painterResource(id = R.drawable.youtube_icon),
                                    contentDescription = "Video",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .clickable {
                                            // TODO this is just temporary
                                            Log.i("JONAS", "Clicked on iframe ${video.src}")
                                        }
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                    "video" -> {
                        // TODO
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
fun testIt() {
    val html = """
        <p>In Gimp you go to <em>Image</em> in the top menu bar and select <em>Mode</em> followed by <em>Indexed</em>. Now you see a popup where you can select the number of colors for a generated optimum palette.</p> <p>You&rsquo;ll have to experiment a little because it will depend on your image.</p> <p>I used this approach to shrink the size of the cover image in <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">the_zopfli post</a> from a 37KB (JPG) to just 15KB (PNG, all PNG sizes listed include Zopfli compression btw).</p> <h2 id="straight-jpg-to-png-conversion-124kb">Straight JPG to PNG conversion: 124KB</h2> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things.png" alt="PNG version RGB colors" /></p> <p>First off, I exported the JPG file as a PNG file. This PNG file had a whopping 124KB! Clearly there was some bloat being stored.</p> <h2 id="256-colors-40kb">256 colors: 40KB</h2> <p>Reducing from RGB to only 256 colors has no visible effect to my eyes.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_256.png" alt="256 colors" /></p> <h2 id="128-colors-34kb">128 colors: 34KB</h2> <p>Still no difference.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_128.png" alt="128 colors" /></p> <h2 id="64-colors-25kb">64 colors: 25KB</h2> <p>You can start to see some artifacting in the shadow behind the text.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_64.png" alt="64 colors" /></p> <h2 id="32-colors-15kb">32 colors: 15KB</h2> <p>In my opinion this is the sweet spot. The shadow artifacting is barely noticable but the size is significantly reduced.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_32.png" alt="32 colors" /></p> <h2 id="16-colors-11kb">16 colors: 11KB</h2> <p>Clear artifacting in the text shadow and the yellow (fire?) in the background has developed an outline.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_16.png" alt="16 colors" /></p> <h2 id="8-colors-7-3kb">8 colors: 7.3KB</h2> <p>The broom has shifted in color from a clear brown to almost grey. Text shadow is just a grey blob at this point. Even clearer outline developed on the yellow background.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_8.png" alt="8 colors" /></p> <h2 id="4-colors-4-3kb">4 colors: 4.3KB</h2> <p>Interestingly enough, I think 4 colors looks better than 8 colors. The outline in the background has disappeared because there&rsquo;s not enough color spectrum to render it. The broom is now black and filled areas tend to get a white separator to the outlines.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_4.png" alt="4 colors" /></p> <h2 id="2-colors-2-4kb">2 colors: 2.4KB</h2> <p>Well, at least the silhouette is well defined at this point I guess.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_2.png" alt="2 colors" /></p> <hr/> <p>Other posts in the <b>Migrating from Ghost to Hugo</b> series:</p> <ul class="series"> <li>2016-10-21 &mdash; Reduce the size of images even further by reducing number of colors with Gimp </li> <li>2016-08-26 &mdash; <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">Compress all the images!</a> </li> <li>2016-07-25 &mdash; <a href="https://cowboyprogrammer.org/2016/07/migrating_from_ghost_to_hugo/">Migrating from Ghost to Hugo</a> </li> </ul>
    """.trimIndent()

    html.byteInputStream().use { stream ->
        HtmlFormattedText(inputStream = stream, baseUrl = "https://cowboyprogrammer.org")
    }
}
