package com.nononsenseapps.feeder.ui.compose.reader

import android.graphics.Point
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.ui.compose.text.htmlFormattedText
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

        val blob = feedItem?.let { item ->
            // TODO full article fetch action
            when (item.fullTextByDefault) {
                true -> blobFullInputStream(item.id, LocalContext.current.filesDir)
                false -> blobInputStream(item.id, LocalContext.current.filesDir)
            }
        }
        val feedUrl = feedItem?.feedUrl?.toString() ?: ""

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
            blob?.use { inputStream ->
                htmlFormattedText(inputStream = inputStream, baseUrl = feedUrl)
            }
        }
    }
}

@Composable
private fun ReaderView(
    articleTitle: String = "Article title on top",
    feedTitle: String = "Feed Title is here",
    authorDate: String? = "2018-01-02",
    articleBody: LazyListScope.() -> Unit
) {
//    val scrollState = rememberScrollState()
//    Column(modifier = Modifier.verticalScroll(scrollState)) {
    LazyColumn {
        item {
            Text(
                text = articleTitle,
                style = MaterialTheme.typography.h1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = feedTitle,
                style = MaterialTheme.typography.subtitle1
            )
            if (authorDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = authorDate,
                    style = MaterialTheme.typography.subtitle1
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        articleBody()
    }
}
