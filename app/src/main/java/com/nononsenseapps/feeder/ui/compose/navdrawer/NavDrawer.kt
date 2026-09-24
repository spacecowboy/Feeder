package com.nononsenseapps.feeder.ui.compose.navdrawer

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_SAVED_ARTICLES
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedIdWithFolderId
import com.nononsenseapps.feeder.model.FeedUnreadCount
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.material3.DismissibleDrawerSheet
import com.nononsenseapps.feeder.ui.compose.material3.DismissibleNavigationDrawer
import com.nononsenseapps.feeder.ui.compose.material3.DrawerState
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.PreviewThemes
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ScreenWithNavDrawer(
    feedsAndFolders: LazyPagingItems<FeedUnreadCount>,
    onToggleFolderExpansion: (Long) -> Unit,
    onDrawerItemSelect: (Long, Long) -> Unit,
    drawerState: DrawerState,
    focusRequester: FocusRequester,
    navDrawerListState: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            focusRequester.requestFocus()
        }
    }

    DismissibleNavigationDrawer(
        modifier =
            modifier
                .onKeyEventLikeEscape {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet {
                ListOfFeedsAndFolders(
                    feedsAndFolders = feedsAndFolders,
                    onToggleFolderExpansion = onToggleFolderExpansion,
                    state = navDrawerListState,
                    { item ->
                        coroutineScope.launch {
                            onDrawerItemSelect(item.feedId, item.folderId)
                            drawerState.close()
                        }
                    },
                    modifier =
                        Modifier
                            .focusRequester(focusRequester),
                )
            }
        },
        content = content,
    )
}

@ExperimentalAnimationApi
@Composable
fun ListOfFeedsAndFolders(
    feedsAndFolders: LazyPagingItems<FeedUnreadCount>,
    onToggleFolderExpansion: (Long) -> Unit,
    state: LazyListState,
    onItemClick: (FeedIdWithFolderId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = state,
        contentPadding = WindowInsets.systemBars.asPaddingValues(),
        modifier =
            modifier
                .fillMaxSize()
                .semantics {
                    testTag = "feedsAndFolders"
                    collectionInfo = CollectionInfo(feedsAndFolders.itemCount, 1)
                },
    ) {
        items(
            count = feedsAndFolders.itemCount,
            key =
                feedsAndFolders.itemKey {
                    when (it.feedId) {
                        ID_ALL_FEEDS -> ID_ALL_FEEDS
                        ID_SAVED_ARTICLES -> ID_SAVED_ARTICLES
                        ID_UNSET -> "folder_${it.folderId}"
                        else -> it.feedId
                    }
                },
            contentType =
                feedsAndFolders.itemContentType {
                    it.contentType
                },
        ) { itemIndex ->
            val item = feedsAndFolders[itemIndex]
            val showFeedsDivider = itemIndex == 2 && item?.contentType?.isFeedListItem == true

            Column {
                if (showFeedsDivider) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                }

                when (item?.contentType) {
                    null -> {
                        Placeholder()
                    }

                    ContentType.AllFeeds -> {
                        AllFeeds(
                            unreadCount = item.unreadCount,
                            title = stringResource(id = R.string.all_feeds),
                            onItemClick = {
                                onItemClick(item)
                            },
                        )
                    }

                    ContentType.SavedArticles -> {
                        SavedArticles(
                            unreadCount = item.unreadCount,
                            title = stringResource(id = R.string.saved_articles),
                            onItemClick = {
                                onItemClick(item)
                            },
                        )
                    }

                    ContentType.Folder -> {
                        Folder(
                            expanded = item.expanded,
                            onToggleExpansion = {
                                onToggleFolderExpansion(item.folderId)
                            },
                            unreadCount = item.unreadCount,
                            title = item.displayTitle,
                            onItemClick = {
                                onItemClick(item)
                            },
                            depth = item.depth,
                        )
                    }

                    ContentType.Feed -> {
                        Feed(
                            unreadCount = item.unreadCount,
                            title = item.displayTitle,
                            imageUrl = item.imageUrl?.toString(),
                            onItemClick = {
                                onItemClick(item)
                            },
                            depth = item.depth,
                        )
                    }
                }
            }
        }
    }
}

val FeedUnreadCount.contentType: ContentType
    get() =
        when (this.feedId) {
            ID_UNSET -> ContentType.Folder
            ID_ALL_FEEDS -> ContentType.AllFeeds
            ID_SAVED_ARTICLES -> ContentType.SavedArticles
            else -> ContentType.Feed
        }

enum class ContentType {
    AllFeeds,
    SavedArticles,
    Folder,
    Feed,
}

private val ContentType.isFeedListItem: Boolean
    get() =
        when (this) {
            ContentType.Folder,
            ContentType.Feed,
            -> true

            ContentType.AllFeeds,
            ContentType.SavedArticles,
            -> false
        }

@ExperimentalAnimationApi
@Composable
private fun Folder(
    title: String,
    unreadCount: Int,
    expanded: Boolean,
    onToggleExpansion: () -> Unit,
    onItemClick: () -> Unit,
    depth: Int = 0,
) {
    val angle: Float by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(),
        label = "angle",
    )

    val indentation = depth.coerceIn(0, 4) * 32.dp
    val interactionSource = remember { MutableInteractionSource() }

    val toggleExpandLabel = stringResource(id = R.string.toggle_folder_expansion)
    val expandedLabel = stringResource(id = R.string.expanded_folder)
    val contractedLabel = stringResource(id = R.string.contracted_folder)

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .padding(top = 2.dp, bottom = 2.dp, end = 16.dp)
                .fillMaxWidth()
                .height(48.dp)
                .safeSemantics(mergeDescendants = true) {
                    stateDescription =
                        if (expanded) {
                            expandedLabel
                        } else {
                            contractedLabel
                        }
                    customActions =
                        listOf(
                            CustomAccessibilityAction(toggleExpandLabel) {
                                onToggleExpansion()
                                true
                            },
                        )
                },
    ) {
        // Box for indentation that also toggles expansion on click
        // It does not give visual touch feedback because that doesn't look good
        Spacer(
            modifier =
                Modifier
                    .clickable(
                        onClick = onToggleExpansion,
                        interactionSource = interactionSource,
                        indication = null,
                    ).fillMaxHeight()
                    .width(indentation),
        )
        // Instead, provide visual touch feedback via MutableInteractionSource shared with ExpandArrow
        ExpandArrow(
            degrees = angle,
            interactionSource = interactionSource,
            onClick = onToggleExpansion,
        )
        Box(
            modifier =
                Modifier
                    .clickable(onClick = onItemClick)
                    .fillMaxHeight()
                    .weight(1.0f, fill = true),
        ) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart),
            )
        }
        if (unreadCount > 0) {
            val unreadLabel =
                LocalContext.current.resources.getQuantityString(
                    R.plurals.n_unread_articles,
                    unreadCount,
                    unreadCount,
                )
            Text(
                text = "%d".format(LocalConfiguration.current.locales[0], unreadCount),
                maxLines = 1,
                modifier =
                    Modifier
                        .semantics {
                            contentDescription = unreadLabel
                        },
            )
        }
    }
}

@Composable
private fun ExpandArrow(
    degrees: Float,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.clearAndSetSemantics { },
    ) {
        Icon(
            Icons.Filled.ExpandLess,
            contentDescription = stringResource(id = R.string.toggle_folder_expansion),
            modifier = Modifier.rotate(degrees = degrees),
        )
    }
}

@Composable
private fun SavedArticles(
    title: String,
    unreadCount: Int,
    onItemClick: () -> Unit,
) {
    Feed(
        title = title,
        unreadCount = unreadCount,
        onItemClick = onItemClick,
        image = {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        },
        depth = 0,
    )
}

@Composable
private fun Placeholder() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp),
    ) {
    }
}

@Composable
private fun AllFeeds(
    title: String,
    unreadCount: Int,
    onItemClick: () -> Unit,
) {
    Feed(
        title = title,
        unreadCount = unreadCount,
        onItemClick = onItemClick,
        image = {
            Icon(
                Icons.AutoMirrored.Filled.Feed,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        },
        depth = 0,
    )
}

@Composable
private fun Feed(
    title: String,
    image: (@Composable () -> Unit),
    unreadCount: Int,
    onItemClick: () -> Unit,
    depth: Int,
) {
    val indentation = depth.coerceIn(0, 4) * 32.dp

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .clickable(onClick = onItemClick)
                .padding(
                    start = 0.dp,
                    end = 16.dp,
                    top = 2.dp,
                    bottom = 2.dp,
                ).fillMaxWidth()
                .height(48.dp),
    ) {
        Spacer(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(indentation),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .minimumInteractiveComponentSize(),
        ) {
            image()
        }
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1.0f, fill = true),
        ) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart),
            )
        }
        if (unreadCount > 0) {
            val unreadLabel =
                LocalContext.current.resources.getQuantityString(
                    R.plurals.n_unread_articles,
                    unreadCount,
                    unreadCount,
                )
            Text(
                text = "%d".format(LocalConfiguration.current.locales[0], unreadCount),
                maxLines = 1,
                modifier =
                    Modifier.semantics {
                        contentDescription = unreadLabel
                    },
            )
        }
    }
}

@Composable
private fun Feed(
    title: String,
    imageUrl: String?,
    unreadCount: Int,
    onItemClick: () -> Unit,
    depth: Int,
) {
    Feed(
        title = title,
        unreadCount = unreadCount,
        onItemClick = onItemClick,
        image =
            if (imageUrl != null) {
                {
                    val pixels =
                        with(LocalDensity.current) {
                            24.dp.roundToPx()
                        }
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(imageUrl.toString())
                                .listener(
                                    onError = { a, b ->
                                        logDebug("FEEDER_DRAWER", "error ${a.data}", b.throwable)
                                    },
                                ).scale(Scale.FIT)
                                .size(pixels)
                                .precision(Precision.INEXACT)
                                .build(),
                        contentDescription = stringResource(id = R.string.feed_icon),
                        fallback = rememberTintedVectorPainter(Icons.Filled.RssFeed),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                {
                    Icon(
                        Icons.Filled.RssFeed,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
        depth = depth,
    )
}

@OptIn(ExperimentalAnimationApi::class)
@PreviewThemes
@Composable
private fun PreviewListOfFeedsAndFolders() {
    val previewItems =
        listOf(
            FeedUnreadCount(depth = 0, feedId = ID_ALL_FEEDS, displayTitle = "", unreadCount = 13),
            FeedUnreadCount(depth = 0, feedId = ID_SAVED_ARTICLES, displayTitle = "", unreadCount = 0),
            FeedUnreadCount(depth = 0, folderId = 1L, displayTitle = "Folder collapsed", unreadCount = 1, expanded = false),
            FeedUnreadCount(depth = 0, folderId = 2L, displayTitle = "Folder with folder and feeds", unreadCount = 12, expanded = true),
            FeedUnreadCount(depth = 1, folderId = 3L, displayTitle = "Folder with feed", unreadCount = 11, expanded = true),
            FeedUnreadCount(depth = 2, feedId = 1L, displayTitle = "Child Feed", unreadCount = 10),
            FeedUnreadCount(depth = 2, feedId = 2L, displayTitle = "Child Feed with indentation and long display name", unreadCount = 1),
            FeedUnreadCount(depth = 1, feedId = 3L, displayTitle = "Another Child Feed", unreadCount = 1),
            FeedUnreadCount(depth = 0, folderId = 4L, displayTitle = "Indentation Test Folder 1", unreadCount = 0, expanded = true),
            FeedUnreadCount(depth = 1, folderId = 5L, displayTitle = "Folder 2", unreadCount = 0, expanded = true),
            FeedUnreadCount(depth = 2, folderId = 6L, displayTitle = "Folder 3", unreadCount = 0, expanded = true),
            FeedUnreadCount(depth = 3, folderId = 7L, displayTitle = "Folder 4", unreadCount = 0, expanded = true),
            FeedUnreadCount(depth = 4, folderId = 8L, displayTitle = "Folder 5", unreadCount = 0, expanded = true),
            FeedUnreadCount(depth = 5, folderId = 9L, displayTitle = "Folder 6", unreadCount = 0, expanded = true),
            FeedUnreadCount(depth = 0, feedId = 4L, displayTitle = "Top Level Feed", unreadCount = 0),
        )
    val pagingData = PagingData.from(previewItems)
    val dataFlow = MutableStateFlow(pagingData)

    PreviewTheme {
        Surface {
            ListOfFeedsAndFolders(
                feedsAndFolders = dataFlow.collectAsLazyPagingItems(),
                onToggleFolderExpansion = {},
                state = rememberLazyListState(),
                onItemClick = {},
            )
        }
    }
}
