package com.nononsenseapps.feeder.ui.compose.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.MarkAsUnread
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.TextToDisplay
import com.nononsenseapps.feeder.ui.compose.text.htmlFormattedText
import com.nononsenseapps.feeder.ui.compose.theme.contentHorizontalPadding
import com.nononsenseapps.feeder.ui.unicodeWrap
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

private val dateTimeFormat =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

@Composable
fun ReaderScreen(
    feedItemViewModel: FeedItemViewModel,
    onNavigateUp: () -> Unit
) {
    val feedItem by feedItemViewModel.currentLiveItem.observeAsState()

    var showMenu by remember {
        mutableStateOf(false)
    }

    if (feedItem?.fullTextByDefault == true) {
        feedItemViewModel.displayFullText()
    }

    val textToDisplay by feedItemViewModel.textToDisplay.collectAsState()

    val fetchFullButtonVisible = textToDisplay == TextToDisplay.DEFAULT

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = feedItem?.feedDisplayTitle ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back button"
                        )
                    }
                },
                actions = {
                    if (fetchFullButtonVisible) {
                        IconButton(
                            onClick = {
                                feedItemViewModel.displayFullText()
                            }
                        ) {
                            Icon(
                                Icons.Default.Article,
                                contentDescription = "Fetch full article button"
                            )
                        }
                    }

                    IconButton(onClick = { /*TODO open in custom tab*/ }) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = "Switch to browser view button"
                        )
                    }

                    if (!fetchFullButtonVisible) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share button"
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Open menu")
                        }
                        // TODO make it wider as necessary
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (fetchFullButtonVisible) {
                                DropdownMenuItem(onClick = {/* TODO */ }) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Share button"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.share))
                                }
                            }

                            DropdownMenuItem(onClick = {/* TODO */ }) {
                                Icon(
                                    Icons.Default.MarkAsUnread,
                                    contentDescription = "Mark as unread button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.mark_as_unread))
                            }

                            // TODO different icon depending on state
                            DropdownMenuItem(onClick = {/* TODO */ }) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Read aloud button"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.read_article))
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        val author = feedItem?.author
        val pubDate = feedItem?.pubDate
        val feedUrl = feedItem?.feedUrl?.toString() ?: ""
        val context = LocalContext.current

        ReaderView(
            modifier = Modifier.padding(padding),
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
                when (textToDisplay) {
                    TextToDisplay.DEFAULT -> {
                        blobInputStream(item.id, context.filesDir).use {
                            htmlFormattedText(inputStream = it, baseUrl = feedUrl)
                        }
                    }
                    TextToDisplay.LOADING_FULLTEXT -> {
                        item {
                            Text(text = stringResource(id = R.string.fetching_full_article))
                        }
                    }
                    TextToDisplay.FAILED_TO_LOAD_FULLTEXT -> {
                        item {
                            Text(text = stringResource(id = R.string.failed_to_fetch_full_article))
                        }
                    }
                    TextToDisplay.FULLTEXT -> {
                        blobFullInputStream(item.id, context.filesDir).use {
                            htmlFormattedText(inputStream = it, baseUrl = feedUrl)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderView(
    modifier: Modifier = Modifier,
    articleTitle: String = "Article title on top",
    feedTitle: String = "Feed Title is here",
    authorDate: String? = "2018-01-02",
    articleBody: LazyListScope.() -> Unit
) {
//    val scrollState = rememberScrollState()
//    Column(modifier = Modifier.verticalScroll(scrollState)) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = contentHorizontalPadding)
    ) {
        item {
            Text(
                text = articleTitle,
                style = MaterialTheme.typography.h1
            )
            Spacer(modifier = Modifier.height(8.dp))
            // TODO clickable so you can go direct to the feed
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

        // TODO if any enclosure then show link or something here

        articleBody()

        item {
            Spacer(modifier = Modifier.height(92.dp))
        }
    }
}
