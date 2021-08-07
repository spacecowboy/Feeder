package com.nononsenseapps.feeder.ui.compose.reader

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.TextToDisplay
import com.nononsenseapps.feeder.ui.compose.state.getImagePlaceholder
import com.nononsenseapps.feeder.ui.compose.text.htmlFormattedText
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.keyline1Padding
import com.nononsenseapps.feeder.ui.unicodeWrap
import com.nononsenseapps.feeder.util.LinkOpener
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

private val dateTimeFormat =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

@Composable
fun ReaderScreen(
    feedItemViewModel: FeedItemViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateUp: () -> Unit
) {
    val linkOpener by settingsViewModel.linkOpener.collectAsState()
    val feedItem by feedItemViewModel.currentLiveItem.observeAsState()

    if (feedItem?.fullTextByDefault == true) {
        feedItemViewModel.displayFullText()
    }

    val textToDisplay by feedItemViewModel.textToDisplay.collectAsState()

    val enclosure = feedItem?.enclosureLink?.let { link ->
        feedItem?.enclosureFilename?.let { name ->
            Enclosure(
                link = link,
                name = name
            )
        }
    }

    val feedUrl = feedItem?.feedUrl?.toString() ?: ""
    val context = LocalContext.current

    val onShare = {
        feedItem?.link?.let { link ->
            val intent = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, link)
                    putExtra(Intent.EXTRA_TITLE, feedItem?.plainTitle ?: "")
                    type = "text/plain"
                },
                null
            )
            context.startActivity(intent)
        }
        Unit
    }

    @DrawableRes
    val placeHolder: Int = getImagePlaceholder(settingsViewModel)

    ReaderScreen(
        articleTitle = feedItem?.plainTitle ?: "",
        feedDisplayTitle = feedItem?.feedDisplayTitle ?: "",
        author = feedItem?.author,
        pubDate = feedItem?.pubDate,
        textToDisplay = textToDisplay,
        enclosure = enclosure,
        onFetchFullText = {
            feedItemViewModel.displayFullText()
        },
        onMarkAsUnread = {
            feedItemViewModel.markCurrentItemAsUnread()
        },
        onShare = onShare,
        onOpenInCustomTab = {
            feedItem?.link?.let { link ->
                openLinkInCustomTab(context, link, feedItemViewModel.currentItemId)
            }
        },
        onNavigateUp = onNavigateUp
    ) {
        feedItem?.let { item ->
            when (textToDisplay) {
                TextToDisplay.DEFAULT -> {
                    blobInputStream(item.id, context.filesDir).use {
                        htmlFormattedText(
                            inputStream = it,
                            baseUrl = feedUrl,
                            imagePlaceholder = placeHolder,
                            onLinkClick = { link ->
                                onLinkClick(
                                    link = link,
                                    linkOpener = linkOpener,
                                    context = context
                                )
                            }
                        )
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
                        htmlFormattedText(
                            inputStream = it,
                            baseUrl = feedUrl,
                            imagePlaceholder = placeHolder,
                            onLinkClick = { link ->
                                onLinkClick(
                                    link = link,
                                    linkOpener = linkOpener,
                                    context = context
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReaderScreen(
    articleTitle: String,
    feedDisplayTitle: String,
    author: String?,
    pubDate: ZonedDateTime?,
    enclosure: Enclosure?,
    textToDisplay: TextToDisplay,
    onFetchFullText: () -> Unit,
    onMarkAsUnread: () -> Unit,
    onShare: () -> Unit,
    onOpenInCustomTab: () -> Unit,
    onNavigateUp: () -> Unit,
    articleBody: LazyListScope.() -> Unit
) {
    var showMenu by remember {
        mutableStateOf(false)
    }

    val fetchFullButtonVisible = textToDisplay == TextToDisplay.DEFAULT

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = feedDisplayTitle,
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
                            onClick = onFetchFullText
                        ) {
                            Icon(
                                Icons.Default.Article,
                                contentDescription = "Fetch full article button"
                            )
                        }
                    }

                    IconButton(onClick = onOpenInCustomTab) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = "Switch to browser view button"
                        )
                    }

                    if (!fetchFullButtonVisible) {
                        IconButton(onClick = onShare) {
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
                                DropdownMenuItem(onClick = onShare) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Share button"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(id = R.string.share))
                                }
                            }

                            DropdownMenuItem(
                                onClick = onMarkAsUnread
                            ) {
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
        val context = LocalContext.current

        ReaderView(
            modifier = Modifier.padding(padding),
            articleTitle = articleTitle,
            feedTitle = feedDisplayTitle,
            enclosureName = enclosure?.name,
            enclosureLink = enclosure?.link,
            onEnclosureClick = {
                enclosure?.link?.let { link ->
                    openLinkInBrowser(context, link)
                }
            },
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
                        context.unicodeWrap(author),
                        pubDate.format(dateTimeFormat)
                    )
                else -> null
            },
            articleBody = articleBody
        )
    }
}

@Composable
private fun ReaderView(
    modifier: Modifier = Modifier,
    articleTitle: String = "Article title on top",
    feedTitle: String = "Feed Title is here",
    authorDate: String? = "2018-01-02",
    enclosureName: String? = null,
    enclosureLink: String? = null,
    onEnclosureClick: () -> Unit,
    articleBody: LazyListScope.() -> Unit
) {
    SelectionContainer {
        LazyColumn(
            modifier = modifier
                .padding(horizontal = keyline1Padding)
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
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = authorDate,
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (enclosureName != null && enclosureLink != null) {
                item {
                    Text(
                        text = enclosureName,
                        style = MaterialTheme.typography.body1.merge(LinkTextStyle()),
                        modifier = Modifier
                            .clickable {
                                onEnclosureClick()
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            articleBody()

            item {
                Spacer(modifier = Modifier.height(92.dp))
            }
        }
    }
}

fun onLinkClick(link: String, linkOpener: LinkOpener, context: Context) {
    when (linkOpener) {
        LinkOpener.CUSTOM_TAB -> {
            openLinkInCustomTab(context, link, null)
        }
        LinkOpener.DEFAULT_BROWSER -> {
            openLinkInBrowser(context, link)
        }
    }
}

@Immutable
data class Enclosure(
    val link: String,
    val name: String
)

@Composable
@Preview
private fun PreviewReader() {
    FeederTheme {
        ReaderScreen(
            articleTitle = "A super cool article",
            feedDisplayTitle = "Feed plus plus",
            author = "Bob Marley",
            pubDate = ZonedDateTime.now(),
            enclosure = null,
            textToDisplay = TextToDisplay.DEFAULT,
            onFetchFullText = { },
            onMarkAsUnread = { },
            onShare = {},
            onOpenInCustomTab = {},
            onNavigateUp = { }) {
            item {
                Text("The body")
            }
        }
    }
}
