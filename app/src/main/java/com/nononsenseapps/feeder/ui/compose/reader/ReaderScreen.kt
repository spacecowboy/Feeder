package com.nononsenseapps.feeder.ui.compose.reader

import android.graphics.Point
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.ui.compose.text.HtmlFormattedText
import com.nononsenseapps.feeder.ui.compose.theme.Typography
import com.nononsenseapps.feeder.ui.unicodeWrap
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

private val dateTimeFormat =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

@Composable
fun ReaderScreen(
    itemId: Long,
    navController: NavHostController,
    maxImageSize: Point
) {
    val feedItemViewModel: FeedItemViewModel = DIAwareViewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article Title") },
                navigationIcon = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back button",
                        modifier = Modifier
                            .clickable {
                                navController.navigate("feed") {
                                    launchSingleTop = true
                                }
                            }
                    )
                },
                actions = {
                    // todo
                }
            )
        }
    ) {
        val feedItem by feedItemViewModel.getLiveItem(itemId).observeAsState()
//        val articleText by feedItemViewModel.getLiveTextMaybeFull(
//            options = TextOptions(
//                itemId = itemId,
//                maxImageSize = maxImageSize,
//                nightMode = isSystemInDarkTheme() /* TODO should be prefs or something - also in theme */
//            ),
//            urlClickListener = null /* TODO */
//        ).observeAsState(initial = SpannableString("Loading..."))

        val author = feedItem?.author
        val pubDate = feedItem?.pubDate

        ReaderView(
            articleTitle = feedItem?.plainTitle ?: "Could not load",
            feedTitle = feedItem?.feedDisplayTitle ?: "Unknown feed",
            authorDate = when {
                author == null && pubDate != null ->
                    stringResource(
                        R.string.on_date,
                        pubDate.format(dateTimeFormat)
                    )
                author != null && pubDate != null ->
                    stringResource(
                        R.string.by_author_on_date,
                        // Must wrap author in unicode marks to ensure it formats
                        // correctly in RTL
                        LocalContext.current.unicodeWrap(author),
                        pubDate.format(dateTimeFormat)
                    )
                else -> null
            }
        ) {
            feedItem?.let { item ->
                // TODO full article fetch action
                when (item.fullTextByDefault) {
                    true -> blobFullInputStream(item.id, LocalContext.current.filesDir)
                    false -> blobInputStream(item.id, LocalContext.current.filesDir)
                }.use { inputStream ->
                    HtmlFormattedText(inputStream = inputStream, baseUrl = item.feedUrl.toString())
                }
            }
        }
    }
}

@Composable
@Preview
private fun ReaderView(
    articleTitle: String = "Article title on top",
    feedTitle: String = "Feed Title is here",
    authorDate: String? = "2018-01-02",
    articleBody: @Composable () -> Unit = { Text("body goes here") }
) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        Text(
            text = articleTitle,
            style = Typography.h1
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = feedTitle,
            style = Typography.subtitle1
        )
        if (authorDate != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = authorDate,
                style = Typography.subtitle1
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        articleBody()

//        ClickableTextWithInlineContent(
//            text = articleText,
//            inlineContent = mapOf(
//                "IMAGE" to InlineTextContent(
//                    Placeholder(100.sp, 100.sp, PlaceholderVerticalAlign.TextBottom)
//                ) { alternateText ->
//                    // TODO Render image
//                }
//            )
//        ) { offset ->
//            // TODO("on click with offset / index position")
//            articleText.getStringAnnotations("TODO TAG NAME", offset, offset)
//                .firstOrNull()
//                ?.let {
//                    // it.item should have destination
//                    TODO("handle click")
//                }
//        }
        // TODO
//        Text(text = articleText)
//        AndroidView(
//            factory = { context ->
//                LinkedTextView(context, null).also { view ->
//                    view.setTextIsSelectable(true)
//                    // TODO style
//                    // TODO text direction
//                }
//            },
//            modifier = Modifier
//                .defaultMinSize(minHeight = 300.dp)
//                .fillMaxWidth(),
//            update = { view ->
//                view.text = articleText
//            }
//        )
    }
}

//@Composable
//private fun AnnotatedClickableText(
//    text: SpannableString
//) {
//    val annotatedText = buildAnnotatedString {
//        append(text.toString())
//
//        for (span in text.getSpans<Any>()) {
//            when (span) {
//                ClickableSpan -> addStringAnnotation("clickable", "clickable", span.)
//            }
//        }
//    }
//    /*
//
//                ClickableSpan[] link =
//                        buffer.getSpans(off, off, ClickableSpan.class);
//
//                ClickableImageSpan[] image =
//                        buffer.getSpans(off, off, ClickableImageSpan.class);
//
//     */
//    ClickableText(text = annotatedText) { offset ->
//
//    }
//}
