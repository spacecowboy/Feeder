package com.nononsenseapps.feeder.ui.compose.reader

import android.content.Context
import android.content.Intent
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import com.nononsenseapps.feeder.model.cancelNotification
import com.nononsenseapps.feeder.model.getPlainTextOfHtmlStream
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableReadAloudPlayer
import com.nononsenseapps.feeder.ui.compose.text.htmlFormattedText
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.keyline1Padding
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import com.nononsenseapps.feeder.util.unicodeWrap
import java.util.*
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

private val dateTimeFormat =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReaderScreen(
    readerScreenViewModel: ReaderScreenViewModel,
    readAloudViewModel: TextToSpeechViewModel,
    onNavigateToFeed: (feedId: Long?) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val viewState by readerScreenViewModel.viewState.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(key1 = readerScreenViewModel.currentItemId) {
        readerScreenViewModel.markAsReadAndNotified()
        cancelNotification(context, readerScreenViewModel.currentItemId)
    }

    val enclosure by remember(viewState.currentItem.enclosureLink, viewState.currentItem.enclosureFilename) {
        derivedStateOf {
            viewState.currentItem.enclosureLink?.let { link ->
                viewState.currentItem.enclosureFilename?.let { name ->
                    Enclosure(
                        link = link,
                        name = name
                    )
                }
            }
        }
    }

    val feedUrl by remember(viewState.currentItem.feedUrl) {
        derivedStateOf {
            viewState.currentItem.feedUrl.toString()
        }
    }

    val onShare = remember(viewState) {
        {
            val intent = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, viewState.currentItem.link.toString())
                    putExtra(Intent.EXTRA_TITLE, viewState.currentItem.plainTitle)
                    type = "text/plain"
                },
                null
            )
            context.startActivity(intent)
        }
    }

    val isLightTheme = MaterialTheme.colors.isLight

    @DrawableRes
    val placeHolder: Int by remember(isLightTheme) {
        derivedStateOf {
            if (isLightTheme) {
                R.drawable.placeholder_image_article_day
            } else {
                R.drawable.placeholder_image_article_night
            }
        }
    }

    val toolbarColor = MaterialTheme.colors.primarySurface.toArgb()

    ReaderScreen(
        articleTitle = viewState.currentItem.plainTitle,
        feedDisplayTitle = viewState.currentItem.feedDisplayTitle,
        author = viewState.currentItem.author,
        pubDate = viewState.currentItem.pubDate,
        enclosure = enclosure,
        onFetchFullText = {
            if (viewState.textToDisplay==TextToDisplay.FULLTEXT) {
                readerScreenViewModel.displayArticleText()
            } else {
                readerScreenViewModel.displayFullText()
            }
        },
        onMarkAsUnread = {
            readerScreenViewModel.markCurrentItemAsUnreadInBackground()
        },
        onShare = onShare,
        onOpenInCustomTab = {
            viewState.currentItem.link?.let { link ->
                openLinkInCustomTab(context, link, toolbarColor)
            }
        },
        onFeedTitleClick = {
            onNavigateToFeed(viewState.currentItem.feedId)
        },
        readAloudPlayer = {
            HideableReadAloudPlayer(readAloudViewModel)
        },
        onReadAloudStart = {
            val fullText = when (viewState.textToDisplay) {
                TextToDisplay.DEFAULT -> {
                    blobInputStream(viewState.currentItem.id, context.filesDir).use {
                        getPlainTextOfHtmlStream(
                            inputStream = it,
                            baseUrl = feedUrl
                        )
                    }
                }
                TextToDisplay.FULLTEXT -> {
                    blobFullInputStream(viewState.currentItem.id, context.filesDir).use {
                        getPlainTextOfHtmlStream(
                            inputStream = it,
                            baseUrl = feedUrl
                        )
                    }
                }
                TextToDisplay.LOADING_FULLTEXT -> null
                TextToDisplay.FAILED_TO_LOAD_FULLTEXT -> null
            }

            if (fullText==null) {
                // TODO show error some message
            } else {
                readAloudViewModel.readAloud(
                    title = viewState.currentItem.plainTitle,
                    fullText = fullText
                )
            }
        },
        onNavigateUp = onNavigateUp
    ) {
        // Can take a composition or two before viewstate is set to its actual values
        if (viewState.currentItem.id > ID_UNSET) {
            when (viewState.textToDisplay) {
                TextToDisplay.DEFAULT -> {
                    blobInputStream(viewState.currentItem.id, context.filesDir).use {
                        htmlFormattedText(
                            inputStream = it,
                            baseUrl = feedUrl,
                            imagePlaceholder = placeHolder,
                            onLinkClick = { link ->
                                onLinkClick(
                                    link = link,
                                    linkOpener = viewState.linkOpener,
                                    context = context,
                                    toolbarColor = toolbarColor
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
                    blobFullInputStream(viewState.currentItem.id, context.filesDir).use {
                        htmlFormattedText(
                            inputStream = it,
                            baseUrl = feedUrl,
                            imagePlaceholder = placeHolder,
                            onLinkClick = { link ->
                                onLinkClick(
                                    link = link,
                                    linkOpener = viewState.linkOpener,
                                    context = context,
                                    toolbarColor = toolbarColor
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
    onFetchFullText: () -> Unit,
    onMarkAsUnread: () -> Unit,
    onShare: () -> Unit,
    onOpenInCustomTab: () -> Unit,
    onNavigateUp: () -> Unit,
    readAloudPlayer: @Composable () -> Unit,
    onReadAloudStart: () -> Unit,
    onFeedTitleClick: () -> Unit,
    articleBody: LazyListScope.() -> Unit,
) {
    var showMenu by rememberSaveable {
        mutableStateOf(false)
    }

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
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.statusBars,
                    applyBottom = false,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onFetchFullText
                    ) {
                        Icon(
                            Icons.Default.Article,
                            contentDescription = stringResource(R.string.fetch_full_article)
                        )
                    }

                    IconButton(onClick = onOpenInCustomTab) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = stringResource(id = R.string.open_in_web_view)
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(id = R.string.open_menu),
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onShare()
                                }
                            ) {
                                Text(stringResource(id = R.string.share))
                            }

                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onMarkAsUnread()
                                }
                            ) {
                                Text(stringResource(id = R.string.mark_as_unread))
                            }
                            DropdownMenuItem(
                                onClick = {
                                    showMenu = false
                                    onReadAloudStart()
                                }
                            ) {
                                Text(stringResource(id = R.string.read_article))
                            }
                        }
                    }
                }
            )
        },
        bottomBar = readAloudPlayer
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
            onFeedTitleClick = onFeedTitleClick,
            authorDate = when {
                author==null && pubDate!=null ->
                    stringResource(
                        R.string.on_date,
                        pubDate.format(dateTimeFormat)
                    )
                author!=null && pubDate!=null ->
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
    onFeedTitleClick: () -> Unit,
    articleBody: LazyListScope.() -> Unit,
) {
    SelectionContainer {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 92.dp),
            modifier = modifier
                .padding(horizontal = keyline1Padding)
        ) {
            item {
                val goToFeedLabel = stringResource(R.string.go_to_feed, feedTitle)
                Column(
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        customActions = listOf(
                            CustomAccessibilityAction(goToFeedLabel) {
                                onFeedTitleClick()
                                true
                            }
                        )
                    }
                ) {
                    Text(
                        text = articleTitle,
                        style = MaterialTheme.typography.h1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = feedTitle,
                        style = MaterialTheme.typography.subtitle1.merge(LinkTextStyle()),
                        modifier = Modifier
                            .clearAndSetSemantics {
                                contentDescription = feedTitle
                            }
                            .clickable {
                                onFeedTitleClick()
                            }
                    )
                    if (authorDate!=null) {
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
            }

            if (enclosureName!=null && enclosureLink!=null) {
                item {
                    val openLabel = stringResource(R.string.open_enclosed_media_file, enclosureName)
                    Text(
                        text = enclosureName,
                        style = MaterialTheme.typography.body1.merge(LinkTextStyle()),
                        modifier = Modifier
                            .clickable {
                                onEnclosureClick()
                            }
                            .clearAndSetSemantics {
                                customActions = listOf(
                                    CustomAccessibilityAction(openLabel) {
                                        onEnclosureClick()
                                        true
                                    }
                                )
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            articleBody()
        }
    }
}

fun onLinkClick(
    link: String,
    linkOpener: LinkOpener,
    context: Context,
    @ColorInt toolbarColor: Int,
) {
    when (linkOpener) {
        LinkOpener.CUSTOM_TAB -> {
            openLinkInCustomTab(context, link, toolbarColor)
        }
        LinkOpener.DEFAULT_BROWSER -> {
            openLinkInBrowser(context, link)
        }
    }
}

@Immutable
data class Enclosure(
    val link: String,
    val name: String,
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
            onFetchFullText = { },
            onMarkAsUnread = { },
            onShare = {},
            onOpenInCustomTab = {},
            readAloudPlayer = {},
            onReadAloudStart = {},
            onFeedTitleClick = {},
            onNavigateUp = { }) {
            item {
                Text("The body")
            }
        }
    }
}
