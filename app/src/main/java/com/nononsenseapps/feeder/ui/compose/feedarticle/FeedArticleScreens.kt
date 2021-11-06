package com.nononsenseapps.feeder.ui.compose.feedarticle

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.primarySurface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.TextToDisplay
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.opml.exportOpml
import com.nononsenseapps.feeder.model.opml.importOpml
import com.nononsenseapps.feeder.ui.compose.feed.FeedListContent
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import com.nononsenseapps.feeder.ui.compose.feed.ScreenWithFeedList
import com.nononsenseapps.feeder.ui.compose.navigation.EditFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SearchFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SettingsDestination
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableReadAloudPlayer
import com.nononsenseapps.feeder.ui.compose.reader.ReaderView
import com.nononsenseapps.feeder.ui.compose.reader.dateTimeFormat
import com.nononsenseapps.feeder.ui.compose.reader.onLinkClick
import com.nononsenseapps.feeder.ui.compose.text.htmlFormattedText
import com.nononsenseapps.feeder.ui.compose.utils.BackHandler
import com.nononsenseapps.feeder.ui.compose.utils.WindowSize
import com.nononsenseapps.feeder.util.openGitlabIssues
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import com.nononsenseapps.feeder.util.unicodeWrap
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZonedDateTime

@Composable
fun FeedArticleScreen(
    windowSize: WindowSize,
    navController: NavController,
    viewModel: FeedArticleViewModel,
) {
    val viewState: FeedArticleScreenViewState by viewModel.viewState.collectAsState()
    val pagedFeedItems = viewModel.currentFeedListItems.collectAsLazyPagingItems()

    val di = LocalDI.current
    val opmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        if (uri != null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                exportOpml(di, uri)
            }
        }
    }
    val opmlImporter = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                importOpml(di, uri)
            }
        }
    }

    val feedArticleScreenType = getFeedArticleScreenType(
        windowSize = windowSize,
        viewState = viewState,
    )

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    // Each feed gets its own scroll state. Persists across device rotations, but is cleared when
    // switching feeds
    val feedListState = key(viewState.currentFeedOrTag) {
        rememberLazyListState()
    }
    // Each article gets its own scroll state. Persists across device rotations, but is cleared
    // when switching articles.
    val articleListState = key(viewState.articleId) {
        rememberLazyListState()
    }

    val toolbarColor = MaterialTheme.colors.primarySurface.toArgb()

    FeedArticleScreen(
        feedArticleScreenType = feedArticleScreenType,
        viewState = viewState,
        onRefreshVisible = {
            viewModel.requestImmediateSyncOfCurrentFeedOrTag()
        },
        onRefreshAll = {
            viewModel.requestImmediateSyncOfAll()
        },
        onToggleOnlyUnread = { value ->
            viewModel.setShowOnlyUnread(value)
        },
        onDrawerItemSelected = { feedId, tag ->
            viewModel.setCurrentFeedAndTag(feedId, tag)
        },
        onMarkAllAsRead = {
            viewModel.markAllAsRead()
        },
        onToggleTagExpansion = { tag ->
            viewModel.toggleTagExpansion(tag)
        },
        onShowToolbarMenu = { visible ->
            viewModel.setToolbarMenuVisible(visible)
        },
        readAloudOnPlay = viewModel::readAloudPlay,
        readAloudOnPause = viewModel::readAloudPause,
        readAloudOnStop = viewModel::readAloudStop,
        onAddFeed = { SearchFeedDestination.navigate(navController) },
        onEditFeed = { feedId ->
            EditFeedDestination.navigate(navController, feedId)
        },
        onShowEditDialog = {
            viewModel.setShowEditDialog(true)
        },
        onDismissEditDialog = {
            viewModel.setShowEditDialog(false)
        },
        onDeleteFeeds = { feedIds ->
            viewModel.deleteFeeds(feedIds.toList())
        },
        onShowDeleteDialog = {
            viewModel.setShowDeleteDialog(true)
        },
        onDismissDeleteDialog = {
            viewModel.setShowDeleteDialog(false)
        },
        onSettings = {
            SettingsDestination.navigate(navController)
        },
        onSendFeedback = {
            context.startActivity(openGitlabIssues())
        },
        onImport = { opmlImporter.launch(arrayOf("text/plain", "text/xml", "text/opml", "*/*")) },
        onExport = { opmlExporter.launch("feeder-export-${LocalDateTime.now()}") },
        onOpenNavDrawer = {
            coroutineScope.launch {
                scaffoldState.drawerState.open()
            }
        },
        markAsUnread = { itemId, unread ->
            viewModel.markAsUnread(itemId, unread)
        },
        markBeforeAsRead = { index ->
            viewModel.markBeforeAsRead(index)
        },
        markAfterAsRead = { index ->
            viewModel.markAfterAsRead(index)
        },
        onOpenFeedItem = { itemId ->
            viewModel.setCurrentArticle(itemId)
            viewModel.markAsUnread(itemId, false)
        },
        onInteractWithList = {
            viewModel.setArticleOpen(false)
        },
        onInteractWithArticle = {
            viewModel.setArticleOpen(true)
        },
        onToggleFullText = {
            if (viewState.textToDisplay == TextToDisplay.FULLTEXT) {
                viewModel.displayArticleText()
            } else {
                viewModel.displayFullText()
            }
        },
        displayFullText = viewModel::displayFullText,
        onMarkAsUnread = {
            viewModel.markAsUnread(
                viewState.articleId,
                unread = true
            )
        },
        onShareArticle = {
            if (viewState.articleId > ID_UNSET) {
                val intent = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        if (viewState.articleLink != null) {
                            putExtra(Intent.EXTRA_TEXT, viewState.articleLink)
                        }
                        putExtra(Intent.EXTRA_TITLE, viewState.articleTitle)
                        type = "text/plain"
                    },
                    null
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
            viewModel.setCurrentFeedAndTag(
                viewState.articleFeedId,
                ""
            )
            viewModel.setArticleOpen(false)
        },
        onNavigateUpFromArticle = {
            viewModel.setArticleOpen(false)
        },
        feedListState = feedListState,
        articleListState = articleListState,
        pagedFeedItems = pagedFeedItems,
        scaffoldState = scaffoldState,
    )
}

@Composable
private fun FeedArticleScreen(
    feedArticleScreenType: FeedArticleScreenType,
    viewState: FeedArticleScreenViewState,
    onRefreshVisible: () -> Unit,
    onRefreshAll: () -> Unit,
    onToggleOnlyUnread: (Boolean) -> Unit,
    onDrawerItemSelected: (Long, String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onToggleTagExpansion: (String) -> Unit,
    onShowToolbarMenu: (Boolean) -> Unit,
    readAloudOnPlay: () -> Unit,
    readAloudOnPause: () -> Unit,
    readAloudOnStop: () -> Unit,
    onAddFeed: () -> Unit,
    onEditFeed: (Long) -> Unit,
    onShowEditDialog: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onDeleteFeeds: (Iterable<Long>) -> Unit,
    onShowDeleteDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onOpenNavDrawer: () -> Unit,
    markAsUnread: (Long, Boolean) -> Unit,
    markBeforeAsRead: (Int) -> Unit,
    markAfterAsRead: (Int) -> Unit,
    onOpenFeedItem: (Long) -> Unit,
    onInteractWithList: () -> Unit,
    onInteractWithArticle: () -> Unit,
    onToggleFullText: () -> Unit,
    displayFullText: () -> Unit,
    onMarkAsUnread: () -> Unit,
    onShareArticle: () -> Unit,
    onOpenInCustomTab: () -> Unit,
    onFeedTitleClick: () -> Unit,
    onNavigateUpFromArticle: () -> Unit,
    feedListState: LazyListState,
    articleListState: LazyListState,
    pagedFeedItems: LazyPagingItems<FeedListItem>,
    scaffoldState: ScaffoldState,
) {
    when (feedArticleScreenType) {
        FeedArticleScreenType.FeedWithArticleDetails -> {
            FeedWithArticleScreen(
                viewState = viewState.copy(showFab = false),
                onRefreshVisible = onRefreshVisible,
                onRefreshAll = onRefreshAll,
                onToggleOnlyUnread = onToggleOnlyUnread,
                onDrawerItemSelected = onDrawerItemSelected,
                onMarkAllAsRead = onMarkAllAsRead,
                onToggleTagExpansion = onToggleTagExpansion,
                onShowToolbarMenu = onShowToolbarMenu,
                readAloudOnPlay = readAloudOnPlay,
                readAloudOnPause = readAloudOnPause,
                readAloudOnStop = readAloudOnStop,
                onOpenInCustomTab = onOpenInCustomTab,
                onAddFeed = onAddFeed,
                onEditFeed = onEditFeed,
                onShowEditDialog = onShowEditDialog,
                onDismissEditDialog = onDismissEditDialog,
                onDeleteFeeds = onDeleteFeeds,
                onShowDeleteDialog = onShowDeleteDialog,
                onDismissDeleteDialog = onDismissDeleteDialog,
                onSettings = onSettings,
                onSendFeedback = onSendFeedback,
                onImport = onImport,
                onExport = onExport,
                onInteractWithList = onInteractWithList,
                onInteractWithArticle = onInteractWithArticle,
                onShareArticle = onShareArticle,
                markAsUnread = markAsUnread,
                markBeforeAsRead = markBeforeAsRead,
                markAfterAsRead = markAfterAsRead,
                onToggleFullText = onToggleFullText,
                displayFullText = displayFullText,
                onOpenFeedItem = onOpenFeedItem,
                onOpenNavDrawer = onOpenNavDrawer,
                feedListState = feedListState,
                articleListState = articleListState,
                onFeedTitleClick = onFeedTitleClick,
                pagedFeedItems = pagedFeedItems,
                scaffoldState = scaffoldState,
            )
        }
        FeedArticleScreenType.Feed -> {
            FeedListScreen(
                viewState = viewState,
                onRefreshVisible = onRefreshVisible,
                onRefreshAll = onRefreshAll,
                onToggleOnlyUnread = onToggleOnlyUnread,
                onDrawerItemSelected = onDrawerItemSelected,
                onMarkAllAsRead = onMarkAllAsRead,
                onToggleTagExpansion = onToggleTagExpansion,
                onShowToolbarMenu = onShowToolbarMenu,
                readAloudOnPlay = readAloudOnPlay,
                readAloudOnPause = readAloudOnPause,
                readAloudOnStop = readAloudOnStop,
                onAddFeed = onAddFeed,
                onEditFeed = onEditFeed,
                onShowEditDialog = onShowEditDialog,
                onDismissEditDialog = onDismissEditDialog,
                onDeleteFeeds = onDeleteFeeds,
                onShowDeleteDialog = onShowDeleteDialog,
                onDismissDeleteDialog = onDismissDeleteDialog,
                onSettings = onSettings,
                onSendFeedback = onSendFeedback,
                onImport = onImport,
                onExport = onExport,
                onOpenNavDrawer = onOpenNavDrawer,
                markAsUnread = markAsUnread,
                markBeforeAsRead = markBeforeAsRead,
                markAfterAsRead = markAfterAsRead,
                onOpenFeedItem = onOpenFeedItem,
                feedListState = feedListState,
                pagedFeedItems = pagedFeedItems,
                scaffoldState = scaffoldState,
            )
        }
        FeedArticleScreenType.ArticleDetails -> {
            ArticleScreen(
                viewState = viewState,
                onToggleFullText = onToggleFullText,
                onMarkAsUnread = onMarkAsUnread,
                onShare = onShareArticle,
                displayFullText = displayFullText,
                onOpenInCustomTab = onOpenInCustomTab,
                onFeedTitleClick = onFeedTitleClick,
                onShowToolbarMenu = onShowToolbarMenu,
                onInteractWithArticle = onInteractWithArticle,
                onNavigateUp = onNavigateUpFromArticle,
                readAloudOnPlay = readAloudOnPlay,
                readAloudOnPause = readAloudOnPause,
                readAloudOnStop = readAloudOnStop,
                articleListState = articleListState,
                scaffoldState = scaffoldState,
            )
        }
    }
}

@Composable
fun FeedWithArticleScreen(
    viewState: FeedArticleScreenViewState,
    onRefreshVisible: () -> Unit,
    onRefreshAll: () -> Unit,
    onToggleOnlyUnread: (Boolean) -> Unit,
    onDrawerItemSelected: (Long, String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onToggleTagExpansion: (String) -> Unit,
    onShowToolbarMenu: (Boolean) -> Unit,
    readAloudOnPlay: () -> Unit,
    readAloudOnPause: () -> Unit,
    readAloudOnStop: () -> Unit,
    onOpenInCustomTab: () -> Unit,
    onAddFeed: () -> Unit,
    onEditFeed: (Long) -> Unit,
    onShowEditDialog: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onDeleteFeeds: (Iterable<Long>) -> Unit,
    onShowDeleteDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onOpenNavDrawer: () -> Unit,
    onShareArticle: () -> Unit,
    markAsUnread: (Long, Boolean) -> Unit,
    markBeforeAsRead: (Int) -> Unit,
    markAfterAsRead: (Int) -> Unit,
    onOpenFeedItem: (Long) -> Unit,
    onInteractWithList: () -> Unit,
    onInteractWithArticle: () -> Unit,
    onFeedTitleClick: () -> Unit,
    onToggleFullText: () -> Unit,
    displayFullText: () -> Unit,
    feedListState: LazyListState,
    articleListState: LazyListState,
    pagedFeedItems: LazyPagingItems<FeedListItem>,
    scaffoldState: ScaffoldState,
) {
    val showingUnreadStateLabel = if (viewState.onlyUnread) {
        stringResource(R.string.showing_only_unread_articles)
    } else {
        stringResource(R.string.showing_all_articles)
    }

    ScreenWithFeedList(
        viewState = viewState,
        onRefreshVisible = onRefreshVisible,
        onDrawerItemSelected = onDrawerItemSelected,
        onMarkAllAsRead = onMarkAllAsRead,
        onToggleTagExpansion = onToggleTagExpansion,
        readAloudOnPlay = readAloudOnPlay,
        readAloudOnPause = readAloudOnPause,
        readAloudOnStop = readAloudOnStop,
        onDismissDeleteDialog = onDismissDeleteDialog,
        onDismissEditDialog = onDismissEditDialog,
        onDelete = onDeleteFeeds,
        onEditFeed = onEditFeed,
        toolbarActions = {
            IconToggleButton(
                checked = viewState.onlyUnread,
                onCheckedChange = onToggleOnlyUnread,
                modifier = Modifier.semantics {
                    stateDescription = showingUnreadStateLabel
                }
            ) {
                if (viewState.onlyUnread) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = null,
                    )
                } else {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                    )
                }
            }
            IconButton(
                onClick = onRefreshAll
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.synchronize_feeds)
                )
            }
            IconButton(
                onClick = onToggleFullText
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
                IconButton(onClick = { onShowToolbarMenu(true) }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.open_menu),
                    )
                }
                DropdownMenu(
                    expanded = viewState.showToolbarMenu,
                    onDismissRequest = { onShowToolbarMenu(false) }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onAddFeed()
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.add_feed))
                    }
                    DropdownMenuItem(
                        onClick = {
                            if (viewState.visibleFeeds.size == 1) {
                                onEditFeed(viewState.visibleFeeds.first().id)
                            } else {
                                onShowEditDialog()
                            }
                            onShowToolbarMenu(false)
                        }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.edit_feed))
                    }
                    DropdownMenuItem(
                        onClick = {
                            onShowDeleteDialog()
                            onShowToolbarMenu(false)
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.delete_feed))
                    }
                    DropdownMenuItem(
                        onClick = {
                            onMarkAllAsRead()
                            onShowToolbarMenu(false)
                        }
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.mark_all_as_read))
                    }
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onShareArticle()
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.share))
                    }

                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            markAsUnread(viewState.articleId, true)
                        }
                    ) {
                        Icon(
                            Icons.Default.VisibilityOff,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.mark_as_unread))
                    }
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            readAloudOnPlay()
                        }
                    ) {
                        Icon(
                            Icons.Default.Speaker,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.read_article))
                    }
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onImport()
                        }
                    ) {
                        Icon(
                            Icons.Default.ImportExport,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.import_feeds_from_opml))
                    }
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onExport()
                        }
                    ) {
                        Icon(
                            Icons.Default.ImportExport,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.export_feeds_to_opml))
                    }
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onSettings()
                        }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.action_settings))
                    }
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onSendFeedback()
                        }
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.send_bug_report))
                    }
                }
            }
        },
        scaffoldState = scaffoldState,
    ) { modifier ->
        Row(modifier) {
            FeedListContent(
                viewState = viewState,
                onOpenNavDrawer = onOpenNavDrawer,
                onAddFeed = onAddFeed,
                markAsUnread = markAsUnread,
                markBeforeAsRead = markBeforeAsRead,
                markAfterAsRead = markAfterAsRead,
                onItemClick = onOpenFeedItem,
                listState = feedListState,
                pagedFeedItems = pagedFeedItems,
                modifier = Modifier
                    .width(334.dp)
                    .notifyInput(onInteractWithList),
            )
            if (viewState.articleId > ID_UNSET) {
                // Avoid state sharing between articles
                key(viewState.articleId) {
                    ArticleContent(
                        viewState = viewState,
                        onFeedTitleClick = onFeedTitleClick,
                        articleListState = articleListState,
                        displayFullText = displayFullText,
                        modifier = Modifier
                            .fillMaxSize()
                            .notifyInput(onInteractWithArticle)
//                            .imePadding() // add padding for the on-screen keyboard
                    )
                }
            }
        }
    }
}

@Composable
fun FeedListScreen(
    viewState: FeedScreenViewState,
    onRefreshVisible: () -> Unit,
    onRefreshAll: () -> Unit,
    onToggleOnlyUnread: (Boolean) -> Unit,
    onDrawerItemSelected: (Long, String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onToggleTagExpansion: (String) -> Unit,
    onShowToolbarMenu: (Boolean) -> Unit,
    readAloudOnPlay: () -> Unit,
    readAloudOnPause: () -> Unit,
    readAloudOnStop: () -> Unit,
    onAddFeed: () -> Unit,
    onEditFeed: (Long) -> Unit,
    onShowEditDialog: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onDeleteFeeds: (Iterable<Long>) -> Unit,
    onShowDeleteDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onSettings: () -> Unit,
    onSendFeedback: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onOpenNavDrawer: () -> Unit,
    markAsUnread: (Long, Boolean) -> Unit,
    markBeforeAsRead: (Int) -> Unit,
    markAfterAsRead: (Int) -> Unit,
    onOpenFeedItem: (Long) -> Unit,
    feedListState: LazyListState,
    pagedFeedItems: LazyPagingItems<FeedListItem>,
    scaffoldState: ScaffoldState,
) {
    val showingUnreadStateLabel = if (viewState.onlyUnread) {
        stringResource(R.string.showing_only_unread_articles)
    } else {
        stringResource(R.string.showing_all_articles)
    }

    ScreenWithFeedList(
        viewState = viewState,
        onRefreshVisible = onRefreshVisible,
        onDrawerItemSelected = onDrawerItemSelected,
        onMarkAllAsRead = onMarkAllAsRead,
        onToggleTagExpansion = onToggleTagExpansion,
        readAloudOnPlay = readAloudOnPlay,
        readAloudOnPause = readAloudOnPause,
        readAloudOnStop = readAloudOnStop,
        onDismissDeleteDialog = onDismissDeleteDialog,
        onDismissEditDialog = onDismissEditDialog,
        onDelete = onDeleteFeeds,
        onEditFeed = onEditFeed,
        toolbarActions = {
            IconToggleButton(
                checked = viewState.onlyUnread,
                onCheckedChange = onToggleOnlyUnread,
                modifier = Modifier.semantics {
                    stateDescription = showingUnreadStateLabel
                }
            ) {
                if (viewState.onlyUnread) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = null,
                    )
                } else {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                    )
                }
            }
            IconButton(
                onClick = onRefreshAll
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.synchronize_feeds)
                )
            }

            Box {
                IconButton(onClick = { onShowToolbarMenu(true) }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.open_menu),
                    )
                }
                DropdownMenu(
                    expanded = viewState.showToolbarMenu,
                    onDismissRequest = { onShowToolbarMenu(false) }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onAddFeed()
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.add_feed))
                    }
                    DropdownMenuItem(
                        onClick = {
                            if (viewState.visibleFeeds.size == 1) {
                                onEditFeed(viewState.visibleFeeds.first().id)
                            } else {
                                onShowEditDialog()
                            }
                            onShowToolbarMenu(false)
                        }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.edit_feed))
                    }
                    DropdownMenuItem(
                        onClick = {
                            onShowDeleteDialog()
                            onShowToolbarMenu(false)
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.delete_feed))
                    }
                    DropdownMenuItem(
                        onClick = {
                            onMarkAllAsRead()
                            onShowToolbarMenu(false)
                        }
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.mark_all_as_read))
                    }
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onImport()
                        }
                    ) {
                        Icon(
                            Icons.Default.ImportExport,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.import_feeds_from_opml))
                    }
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onExport()
                        }
                    ) {
                        Icon(
                            Icons.Default.ImportExport,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.export_feeds_to_opml))
                    }
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onSettings()
                        }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.action_settings))
                    }
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            onShowToolbarMenu(false)
                            onSendFeedback()
                        }
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(id = R.string.send_bug_report))
                    }
                }
            }
        },
        scaffoldState = scaffoldState,
    ) { modifier ->
        FeedListContent(
            viewState = viewState,
            onOpenNavDrawer = onOpenNavDrawer,
            onAddFeed = onAddFeed,
            markAsUnread = markAsUnread,
            markBeforeAsRead = markBeforeAsRead,
            markAfterAsRead = markAfterAsRead,
            onItemClick = onOpenFeedItem,
            listState = feedListState,
            pagedFeedItems = pagedFeedItems,
            modifier = modifier,
        )
    }
}

@Composable
fun ArticleScreen(
    viewState: ArticleScreenViewState,
    onToggleFullText: () -> Unit,
    onMarkAsUnread: () -> Unit,
    onShare: () -> Unit,
    onOpenInCustomTab: () -> Unit,
    onFeedTitleClick: () -> Unit,
    onShowToolbarMenu: (Boolean) -> Unit,
    onInteractWithArticle: () -> Unit,
    displayFullText: () -> Unit,
    readAloudOnPlay: () -> Unit,
    readAloudOnPause: () -> Unit,
    readAloudOnStop: () -> Unit,
    scaffoldState: ScaffoldState,
    articleListState: LazyListState,
    onNavigateUp: () -> Unit,
) {
    BackHandler(onBack = onNavigateUp)
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewState.feedDisplayTitle,
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
                        onClick = onToggleFullText
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
                        IconButton(onClick = { onShowToolbarMenu(true) }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(id = R.string.open_menu),
                            )
                        }
                        DropdownMenu(
                            expanded = viewState.showToolbarMenu,
                            onDismissRequest = { onShowToolbarMenu(false) }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onShowToolbarMenu(false)
                                    onShare()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.share))
                            }

                            DropdownMenuItem(
                                onClick = {
                                    onShowToolbarMenu(false)
                                    onMarkAsUnread()
                                }
                            ) {
                                Icon(
                                    Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.mark_as_unread))
                            }
                            DropdownMenuItem(
                                onClick = {
                                    onShowToolbarMenu(false)
                                    readAloudOnPlay()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Speaker,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(id = R.string.read_article))
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            HideableReadAloudPlayer(
                visible = viewState.isReadAloudVisible,
                currentlyPlaying = viewState.isReadAloudPlaying,
                title = viewState.readAloudTitle,
                onPlay = readAloudOnPlay,
                onPause = readAloudOnPause,
                onStop = readAloudOnStop,
            )
        },
    ) { padding ->
        ArticleContent(
            viewState = viewState,
            articleListState = articleListState,
            onFeedTitleClick = onFeedTitleClick,
            displayFullText = displayFullText,
            modifier = Modifier
                .padding(padding)
                .notifyInput(onInteractWithArticle)
        )
    }
}

@Composable
fun ArticleContent(
    viewState: ArticleScreenViewState,
    onFeedTitleClick: () -> Unit,
    articleListState: LazyListState,
    displayFullText: () -> Unit,
    modifier: Modifier,
) {
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

    val context = LocalContext.current

    if (viewState.articleId > ID_UNSET
        && viewState.textToDisplay == TextToDisplay.FULLTEXT
        && !blobFullFile(viewState.articleId, context.filesDir).isFile
    ) {
        LaunchedEffect(viewState.articleId, viewState.textToDisplay) {
            // Trigger parse and fetch
            displayFullText()
        }
    }

    ReaderView(
        modifier = modifier,
        articleListState = articleListState,
        articleTitle = viewState.articleTitle,
        feedTitle = viewState.feedDisplayTitle,
        enclosure = viewState.enclosure,
        onEnclosureClick = {
            if (viewState.enclosure.present) {
                openLinkInBrowser(context, viewState.enclosure.link)
            }
        },
        onFeedTitleClick = onFeedTitleClick,
        authorDate = when {
            viewState.author == null && viewState.pubDate != null ->
                stringResource(
                    R.string.on_date,
                    (viewState.pubDate ?: ZonedDateTime.now()).format(dateTimeFormat)
                )
            viewState.author != null && viewState.pubDate != null ->
                stringResource(
                    R.string.by_author_on_date,
                    // Must wrap author in unicode marks to ensure it formats
                    // correctly in RTL
                    context.unicodeWrap(viewState.author ?: ""),
                    (viewState.pubDate ?: ZonedDateTime.now()).format(dateTimeFormat)
                )
            else -> null
        },
    ) {
        // Can take a composition or two before viewstate is set to its actual values
        // TODO show something in case no article to show
        if (viewState.articleId > ID_UNSET) {
            when (viewState.textToDisplay) {
                TextToDisplay.DEFAULT -> {
                    blobInputStream(viewState.articleId, context.filesDir).use {
                        htmlFormattedText(
                            inputStream = it,
                            baseUrl = viewState.articleFeedUrl ?: "",
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
                TextToDisplay.FAILED_TO_LOAD_FULLTEXT -> {
                    item {
                        Text(text = stringResource(id = R.string.failed_to_fetch_full_article))
                    }
                }
                TextToDisplay.LOADING_FULLTEXT -> {
                    LoadingItem()
                }

                TextToDisplay.FULLTEXT -> {
                    if (blobFullFile(viewState.articleId, context.filesDir).isFile) {
                        blobFullInputStream(viewState.articleId, context.filesDir).use {
                            htmlFormattedText(
                                inputStream = it,
                                baseUrl = viewState.articleFeedUrl ?: "",
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
                    } else {
                        // Already trigger load in effect above
                        LoadingItem()
                    }
                }
            }
        }
    }
}

private fun LazyListScope.LoadingItem() {
    item {
        Text(text = stringResource(id = R.string.fetching_full_article))
    }
}

private enum class FeedArticleScreenType {
    FeedWithArticleDetails,
    Feed,
    ArticleDetails,
}

private fun getFeedArticleScreenType(
    windowSize: WindowSize,
    viewState: FeedArticleScreenViewState,
): FeedArticleScreenType = when (windowSize) {
    WindowSize.Compact, WindowSize.Medium -> {
        when {
            viewState.isArticleOpen -> FeedArticleScreenType.ArticleDetails
            else -> FeedArticleScreenType.Feed
        }
    }
    WindowSize.Expanded -> FeedArticleScreenType.FeedWithArticleDetails
}

/**
 * A [Modifier] that tracks all input, and calls [block] every time input is received.
 */
private fun Modifier.notifyInput(block: () -> Unit): Modifier =
    composed {
        val blockState = rememberUpdatedState(block)
        pointerInput(Unit) {
            while (currentCoroutineContext().isActive) {
                awaitPointerEventScope {
                    awaitPointerEvent(PointerEventPass.Initial)
                    blockState.value()
                }
            }
        }
    }
