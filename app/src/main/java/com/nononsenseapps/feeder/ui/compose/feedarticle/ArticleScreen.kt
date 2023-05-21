package com.nononsenseapps.feeder.ui.compose.feedarticle

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.TextToDisplay
import com.nononsenseapps.feeder.blob.blobFile
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.ui.compose.icons.CustomFilled
import com.nononsenseapps.feeder.ui.compose.icons.TextToSpeech
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableTTSPlayer
import com.nononsenseapps.feeder.ui.compose.reader.ReaderView
import com.nononsenseapps.feeder.ui.compose.reader.dateTimeFormat
import com.nononsenseapps.feeder.ui.compose.reader.onLinkClick
import com.nononsenseapps.feeder.ui.compose.text.htmlFormattedText
import com.nononsenseapps.feeder.ui.compose.theme.SensibleTopAppBar
import com.nononsenseapps.feeder.ui.compose.theme.SetStatusBarColorToMatchScrollableTopAppBar
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import com.nononsenseapps.feeder.util.FilePathProvider
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import com.nononsenseapps.feeder.util.unicodeWrap
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance
import org.threeten.bp.ZonedDateTime

@Composable
fun ArticleScreen(
    onNavigateUp: () -> Unit,
    onNavigateToFeed: (Long) -> Unit,
    viewModel: FeedArticleViewModel,
) {
    BackHandler(onBack = onNavigateUp)
    val viewState: FeedArticleScreenViewState by viewModel.viewState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Each article gets its own scroll state. Persists across device rotations, but is cleared
    // when switching articles.
    val articleListState = key(viewState.articleId) {
        rememberLazyListState()
    }

    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()

    ArticleScreen(
        viewState = viewState,
        onShowToolbarMenu = { visible ->
            viewModel.setToolbarMenuVisible(visible)
        },
        ttsOnPlay = viewModel::ttsPlay,
        ttsOnPause = viewModel::ttsPause,
        ttsOnStop = viewModel::ttsStop,
        ttsOnSkipNext = viewModel::ttsSkipNext,
        ttsOnSelectLanguage = viewModel::ttsOnSelectLanguage,
        onMarkAsUnread = {
            viewModel.markAsUnread(viewState.articleId)
        },
        onToggleFullText = {
            if (viewState.textToDisplay == TextToDisplay.FULLTEXT) {
                viewModel.displayArticleText()
            } else {
                viewModel.displayFullText()
            }
        },
        displayFullText = viewModel::displayFullText,
        onShare = {
            if (viewState.articleId > ID_UNSET) {
                val intent = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        if (viewState.articleLink != null) {
                            putExtra(Intent.EXTRA_TEXT, viewState.articleLink)
                        }
                        putExtra(Intent.EXTRA_TITLE, viewState.articleTitle)
                        type = "text/plain"
                    },
                    null,
                )
                context.startActivity(intent)
            }
        },
        onOpenInCustomTab = {
            viewState.articleLink?.let { link ->
                openLinkInCustomTab(context, link, toolbarColor)
            }
        },
        onFeedTitleClick = {
            onNavigateToFeed(viewState.articleFeedId)
        },
        onNavigateUp = onNavigateUp,
        onTogglePinned = {
            viewModel.setPinned(viewState.articleId, !viewState.isPinned)
        },
        onToggleBookmarked = {
            viewModel.setBookmarked(viewState.articleId, !viewState.isBookmarked)
        },
        articleListState = articleListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    viewState: ArticleScreenViewState,
    onToggleFullText: () -> Unit,
    onMarkAsUnread: () -> Unit,
    onShare: () -> Unit,
    onOpenInCustomTab: () -> Unit,
    onFeedTitleClick: () -> Unit,
    onShowToolbarMenu: (Boolean) -> Unit,
    displayFullText: () -> Unit,
    ttsOnPlay: () -> Unit,
    ttsOnPause: () -> Unit,
    ttsOnStop: () -> Unit,
    ttsOnSkipNext: () -> Unit,
    ttsOnSelectLanguage: (LocaleOverride) -> Unit,
    onTogglePinned: () -> Unit,
    onToggleBookmarked: () -> Unit,
    articleListState: LazyListState,
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    SetStatusBarColorToMatchScrollableTopAppBar(scrollBehavior)

    val bottomBarVisibleState = remember { MutableTransitionState(viewState.isBottomBarVisible) }
    LaunchedEffect(viewState.isBottomBarVisible) {
        bottomBarVisibleState.targetState = viewState.isBottomBarVisible
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            SensibleTopAppBar(
                scrollBehavior = scrollBehavior,
                title = viewState.feedDisplayTitle,
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                },
                actions = {
                    PlainTooltipBox(tooltip = { Text(stringResource(R.string.fetch_full_article)) }) {
                        IconButton(
                            onClick = onToggleFullText,
                            modifier = Modifier.tooltipAnchor(),
                        ) {
                            Icon(
                                Icons.Default.Article,
                                contentDescription = stringResource(R.string.fetch_full_article),
                            )
                        }
                    }

                    PlainTooltipBox(tooltip = { Text(stringResource(id = R.string.open_in_web_view)) }) {
                        IconButton(
                            onClick = onOpenInCustomTab,
                            modifier = Modifier.tooltipAnchor(),
                        ) {
                            Icon(
                                Icons.Default.OpenInBrowser,
                                contentDescription = stringResource(id = R.string.open_in_web_view),
                            )
                        }
                    }

                    PlainTooltipBox(tooltip = { Text(stringResource(id = R.string.open_menu)) }) {
                        Box {
                            IconButton(
                                onClick = { onShowToolbarMenu(true) },
                                modifier = Modifier.tooltipAnchor(),
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(id = R.string.open_menu),
                                )
                            }
                            DropdownMenu(
                                expanded = viewState.showToolbarMenu,
                                onDismissRequest = { onShowToolbarMenu(false) },
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        onShowToolbarMenu(false)
                                        onShare()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = null,
                                        )
                                    },
                                    text = {
                                        Text(stringResource(id = R.string.share))
                                    },
                                )

                                DropdownMenuItem(
                                    onClick = {
                                        onShowToolbarMenu(false)
                                        onMarkAsUnread()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                        )
                                    },
                                    text = {
                                        Text(stringResource(id = R.string.mark_as_unread))
                                    },
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        onShowToolbarMenu(false)
                                        onTogglePinned()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.PushPin,
                                            contentDescription = null,
                                        )
                                    },
                                    text = {
                                        Text(
                                            stringResource(
                                                if (viewState.isPinned) {
                                                    R.string.unpin_article
                                                } else {
                                                    R.string.pin_article
                                                },
                                            ),
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        onShowToolbarMenu(false)
                                        onToggleBookmarked()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Bookmark,
                                            contentDescription = null,
                                        )
                                    },
                                    text = {
                                        Text(
                                            stringResource(
                                                if (viewState.isBookmarked) {
                                                    R.string.remove_bookmark
                                                } else {
                                                    R.string.bookmark_article
                                                },
                                            ),
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        onShowToolbarMenu(false)
                                        ttsOnPlay()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.CustomFilled.TextToSpeech,
                                            contentDescription = null,
                                        )
                                    },
                                    text = {
                                        Text(stringResource(id = R.string.read_article))
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            HideableTTSPlayer(
                visibleState = bottomBarVisibleState,
                currentlyPlaying = viewState.isTTSPlaying,
                onPlay = ttsOnPlay,
                onPause = ttsOnPause,
                onStop = ttsOnStop,
                onSkipNext = ttsOnSkipNext,
                languages = ImmutableHolder(viewState.ttsLanguages),
                onSelectLanguage = ttsOnSelectLanguage,
            )
        },
    ) { padding ->
        ArticleContent(
            viewState = viewState,
            screenType = ScreenType.SINGLE,
            articleListState = articleListState,
            onFeedTitleClick = onFeedTitleClick,
            displayFullText = displayFullText,
            modifier = Modifier
                .padding(padding),
        )
    }
}

@Composable
fun ArticleContent(
    viewState: ArticleScreenViewState,
    screenType: ScreenType,
    onFeedTitleClick: () -> Unit,
    articleListState: LazyListState,
    displayFullText: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filePathProvider by LocalDI.current.instance<FilePathProvider>()

    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()

    val context = LocalContext.current

    if (viewState.articleId > ID_UNSET &&
        viewState.textToDisplay == TextToDisplay.FULLTEXT &&
        !blobFullFile(viewState.articleId, filePathProvider.fullArticleDir).isFile
    ) {
        LaunchedEffect(viewState.articleId, viewState.textToDisplay) {
            // Trigger parse and fetch
            displayFullText()
        }
    }

    ReaderView(
        screenType = screenType,
        onEnclosureClick = {
            if (viewState.enclosure.present) {
                openLinkInBrowser(context, viewState.enclosure.link)
            }
        },
        onFeedTitleClick = onFeedTitleClick,
        modifier = modifier,
        articleListState = articleListState,
        enclosure = viewState.enclosure,
        articleTitle = viewState.articleTitle,
        feedTitle = viewState.feedDisplayTitle,
        authorDate = when {
            viewState.author == null && viewState.pubDate != null ->
                stringResource(
                    R.string.on_date,
                    (viewState.pubDate ?: ZonedDateTime.now()).format(dateTimeFormat),
                )

            viewState.author != null && viewState.pubDate != null ->
                stringResource(
                    R.string.by_author_on_date,
                    // Must wrap author in unicode marks to ensure it formats
                    // correctly in RTL
                    context.unicodeWrap(viewState.author ?: ""),
                    (viewState.pubDate ?: ZonedDateTime.now()).format(dateTimeFormat),
                )

            else -> null
        },
    ) {
        // Can take a composition or two before viewstate is set to its actual values
        // TODO show something in case no article to show
        if (viewState.articleId > ID_UNSET) {
            when (viewState.textToDisplay) {
                TextToDisplay.DEFAULT -> {
                    if (blobFile(viewState.articleId, filePathProvider.articleDir).isFile) {
                        blobInputStream(viewState.articleId, filePathProvider.articleDir).use {
                            htmlFormattedText(
                                inputStream = it,
                                baseUrl = viewState.articleFeedUrl ?: "",
                            ) { link ->
                                onLinkClick(
                                    link = link,
                                    linkOpener = viewState.linkOpener,
                                    context = context,
                                    toolbarColor = toolbarColor,
                                )
                            }
                        }
                    } else {
                        item {
                            Column {
                                Text(text = stringResource(id = R.string.failed_to_open_article))
                                Text(text = stringResource(id = R.string.sync_to_fetch))
                            }
                        }
                    }
                }

                TextToDisplay.FAILED_TO_LOAD_FULLTEXT -> {
                    item {
                        Text(text = stringResource(id = R.string.failed_to_fetch_full_article))
                    }
                }

                TextToDisplay.LOADING_FULLTEXT -> {
                    LoadingItem()
                }

                TextToDisplay.FULLTEXT -> {
                    if (blobFullFile(viewState.articleId, filePathProvider.fullArticleDir).isFile) {
                        blobFullInputStream(
                            viewState.articleId,
                            filePathProvider.fullArticleDir,
                        ).use {
                            htmlFormattedText(
                                inputStream = it,
                                baseUrl = viewState.articleFeedUrl ?: "",
                            ) { link ->
                                onLinkClick(
                                    link = link,
                                    linkOpener = viewState.linkOpener,
                                    context = context,
                                    toolbarColor = toolbarColor,
                                )
                            }
                        }
                    } else {
                        // Already trigger load in effect above
                        LoadingItem()
                    }
                }
            }
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.LoadingItem() {
    item {
        Text(text = stringResource(id = R.string.fetching_full_article))
    }
}
