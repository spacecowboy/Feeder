package com.nononsenseapps.feeder.ui.compose.reader

import android.graphics.Point
import android.text.SpannableString
import android.text.Spanned
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.TextOptions
import com.nononsenseapps.feeder.ui.compose.theme.Typography
import com.nononsenseapps.feeder.ui.unicodeWrap
import com.nononsenseapps.feeder.views.LinkedTextView
import kotlinx.coroutines.runBlocking
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
    feedItemViewModel: FeedItemViewModel,
    maxImageSize: Point
) {
    val coroutineScope = rememberCoroutineScope()

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
        val liveText = runBlocking(coroutineScope.coroutineContext) {
            feedItemViewModel.getLiveTextMaybeFull(
                options = TextOptions(
                    itemId = itemId,
                    maxImageSize = maxImageSize,
                    nightMode = false /* TODO theme */
                ),
                urlClickListener = null /* TODO */
            )
        }
        val articleText by liveText.observeAsState(initial = SpannableString("Loading..."))

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
            },
            articleText = articleText
        )
    }
}

@Composable
private fun ReaderView(
    articleTitle: String,
    feedTitle: String,
    authorDate: String?,
    articleText: Spanned
) {
    Column {
        Text(
            text = articleTitle,
            style = Typography.h5
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = feedTitle,
            style = Typography.subtitle2
        )
        if (authorDate != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = authorDate,
                style = Typography.subtitle2
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        AndroidView(
            factory = { context ->
                LinkedTextView(context, null).also { view ->
                    view.setTextIsSelectable(true)
                    // TODO style
                    // TODO text direction
                }
            },
            modifier = Modifier
                .defaultMinSize(minHeight = 300.dp)
                .fillMaxWidth(),
            update = { view ->
                view.text = articleText
            }
        )
    }
}
