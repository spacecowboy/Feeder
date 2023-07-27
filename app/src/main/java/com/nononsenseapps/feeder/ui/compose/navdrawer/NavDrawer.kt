package com.nononsenseapps.feeder.ui.compose.navdrawer

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_SAVED_ARTICLES
import com.nononsenseapps.feeder.ui.compose.material3.DismissibleDrawerSheet
import com.nononsenseapps.feeder.ui.compose.material3.DismissibleNavigationDrawer
import com.nononsenseapps.feeder.ui.compose.material3.DrawerState
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.immutableListHolderOf
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
import java.net.URL
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ScreenWithNavDrawer(
    feedsAndTags: ImmutableHolder<List<DrawerItemWithUnreadCount>>,
    expandedTags: ImmutableHolder<Set<String>>,
    unreadBookmarksCount: Int,
    onToggleTagExpansion: (String) -> Unit,
    onDrawerItemSelected: (Long, String) -> Unit,
    drawerState: DrawerState,
    focusRequester: FocusRequester,
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
        modifier = modifier
            .onKeyEventLikeEscape {
                coroutineScope.launch {
                    drawerState.close()
                }
            },
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet {
                ListOfFeedsAndTags(
                    modifier = Modifier
                        .focusRequester(focusRequester),
                    feedsAndTags = feedsAndTags,
                    expandedTags = expandedTags,
                    unreadBookmarksCount = unreadBookmarksCount,
                    onToggleTagExpansion = onToggleTagExpansion,
                    onItemClick = { item ->
                        coroutineScope.launch {
                            onDrawerItemSelected(item.id, item.tag)
                            drawerState.close()
                        }
                    },
                )
            }
        },
        content = content,
    )
}

@ExperimentalAnimationApi
@Composable
@Preview(showBackground = true)
private fun ListOfFeedsAndTagsPreview() {
    FeederTheme {
        Surface {
            ListOfFeedsAndTags(
                immutableListHolderOf(
                    DrawerTop(unreadCount = 100, totalChildren = 4),
                    DrawerSavedArticles(unreadCount = 5),
                    DrawerTag(
                        tag = "News tag",
                        unreadCount = 0,
                        -1111,
                        totalChildren = 2,
                    ),
                    DrawerFeed(
                        id = 1,
                        displayTitle = "Times",
                        tag = "News tag",
                        unreadCount = 0,
                    ),
                    DrawerFeed(
                        id = 2,
                        displayTitle = "Post",
                        imageUrl = URL("https://cowboyprogrammer.org/apple-touch-icon.png"),
                        tag = "News tag",
                        unreadCount = 2,
                    ),
                    DrawerTag(
                        tag = "Funny tag",
                        unreadCount = 6,
                        -2222,
                        totalChildren = 1,
                    ),
                    DrawerFeed(
                        id = 3,
                        displayTitle = "Hidden",
                        tag = "Funny tag",
                        unreadCount = 6,
                    ),
                    DrawerFeed(
                        id = 4,
                        displayTitle = "Top Dog",
                        unreadCount = 99,
                        tag = "",
                    ),
                    DrawerFeed(
                        id = 5,
                        imageUrl = URL("https://cowboyprogrammer.org/apple-touch-icon.png"),
                        displayTitle = "Cowboy Programmer",
                        unreadCount = 7,
                        tag = "",
                    ),
                ),
                ImmutableHolder(
                    setOf(
                        "News tag",
                        "Funny tag",
                    ),
                ),
                1,
                {},
            ) {}
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ListOfFeedsAndTags(
    feedsAndTags: ImmutableHolder<List<DrawerItemWithUnreadCount>>,
    expandedTags: ImmutableHolder<Set<String>>,
    unreadBookmarksCount: Int,
    onToggleTagExpansion: (String) -> Unit,
    modifier: Modifier = Modifier,
    onItemClick: (FeedIdTag) -> Unit,
) {
    val firstTopFeed by remember(feedsAndTags) {
        derivedStateOf {
            feedsAndTags.item.asSequence()
                .filterIsInstance<DrawerFeed>()
                .filter { it.tag.isEmpty() }
                .firstOrNull()
        }
    }
    LazyColumn(
        contentPadding = WindowInsets.systemBars.asPaddingValues(),
        modifier = modifier
            .fillMaxSize()
            .semantics {
                testTag = "feedsAndTags"
            },
    ) {
        item(
            key = ID_ALL_FEEDS,
            contentType = ID_ALL_FEEDS,
        ) {
            val item = feedsAndTags.item.firstOrNull() ?: DrawerTop(
                { stringResource(id = R.string.all_feeds) },

                0,
                0,
            )
            AllFeeds(
                unreadCount = item.unreadCount,
                title = stringResource(id = R.string.all_feeds),
                onItemClick = {
                    onItemClick(item)
                },
            )
        }
        item(
            key = ID_SAVED_ARTICLES,
            contentType = ID_SAVED_ARTICLES,
        ) {
            SavedArticles(
                unreadCount = unreadBookmarksCount,
                title = stringResource(id = R.string.saved_articles),
                onItemClick = {
                    onItemClick(DrawerSavedArticles(unreadCount = 1))
                },
            )
        }
        items(
            feedsAndTags.item.drop(1),
            key = { it.uiId },
            contentType = {
                when (it) {
                    is DrawerFeed -> 1L
                    is DrawerTag -> it.id
                    is DrawerSavedArticles -> it.id
                    is DrawerTop -> it.id
                }
            },
        ) { item ->
            when (item) {
                is DrawerTag -> {
                    ExpandableTag(
                        expanded = item.tag in expandedTags.item,
                        onToggleExpansion = onToggleTagExpansion,
                        unreadCount = item.unreadCount,
                        title = item.title(),
                        onItemClick = {
                            onItemClick(item)
                        },
                    )
                }

                is DrawerFeed -> {
                    when {
                        item.tag.isEmpty() -> {
                            if (item.id == firstTopFeed?.id) {
                                Divider(
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            TopLevelFeed(
                                unreadCount = item.unreadCount,
                                title = item.title(),
                                imageUrl = item.imageUrl,
                                onItemClick = {
                                    onItemClick(item)
                                },
                            )
                        }

                        else -> {
                            ChildFeed(
                                unreadCount = item.unreadCount,
                                title = item.title(),
                                imageUrl = item.imageUrl,
                                visible = item.tag in expandedTags.item,
                                onItemClick = {
                                    onItemClick(item)
                                },
                            )
                        }
                    }
                }

                is DrawerTop -> {
                    // Handled at top
                    /*
                    AllFeeds(
                    unreadCount = item.unreadCount,
                    title = item.title(),
                    onItemClick = {
                        onItemClick(item)
                    },
                )
                     */
                }

                is DrawerSavedArticles -> {
                    // Handled at top
                    /*
                    SavedArticles(
                    unreadCount = item.unreadCount,
                    title = item.title(),
                    onItemClick = {
                        onItemClick(item)
                    },
                )
                     */
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
private fun ExpandableTag(
    title: String = "Foo",
    unreadCount: Int = 99,
    expanded: Boolean = true,
    onToggleExpansion: (String) -> Unit = {},
    onItemClick: () -> Unit = {},
) {
    val angle: Float by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(),
    )

    val toggleExpandLabel = stringResource(id = R.string.toggle_tag_expansion)
    val expandedLabel = stringResource(id = R.string.expanded_tag)
    val contractedLabel = stringResource(id = R.string.contracted_tag)

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 2.dp, bottom = 2.dp, end = 16.dp)
            .fillMaxWidth()
            .height(48.dp)
            .semantics(mergeDescendants = true) {
                try {
                    stateDescription = if (expanded) {
                        expandedLabel
                    } else {
                        contractedLabel
                    }
                    customActions = listOf(
                        CustomAccessibilityAction(toggleExpandLabel) {
                            onToggleExpansion(title)
                            true
                        },
                    )
                } catch (e: Exception) {
                    // Observed nullpointer exception when setting customActions
                    // No clue why it could be null
                    Log.e("FeederNavDrawer", "Exception in semantics", e)
                }
            },
    ) {
        ExpandArrow(
            degrees = angle,
            onClick = {
                onToggleExpansion(title)
            },
        )
        Box(
            modifier = Modifier
                .clickable(onClick = onItemClick)
                .fillMaxHeight()
                .weight(1.0f, fill = true),
        ) {
            Text(
                text = title,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
            )
        }
        if (unreadCount > 0) {
            val unreadLabel = LocalContext.current.resources.getQuantityString(
                R.plurals.n_unread_articles,
                unreadCount,
                unreadCount,
            )
            Text(
                text = unreadCount.toString(),
                maxLines = 1,
                modifier = Modifier
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
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.clearAndSetSemantics { }) {
        Icon(
            Icons.Filled.ExpandLess,
            contentDescription = stringResource(id = R.string.toggle_tag_expansion),
            modifier = Modifier.rotate(degrees = degrees),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedArticles(
    title: String = "Bar",
    unreadCount: Int = 10,
    onItemClick: () -> Unit = {},
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
    )
}

@Preview(showBackground = true)
@Composable
private fun TopLevelFeed(
    title: String = "Foo",
    unreadCount: Int = 99,
    onItemClick: () -> Unit = {},
    imageUrl: URL? = null,
) = Feed(
    title = title,
    imageUrl = imageUrl,
    unreadCount = unreadCount,
    onItemClick = onItemClick,
)

@Preview(showBackground = true)
@Composable
private fun ChildFeed(
    title: String = "Foo",
    imageUrl: URL? = null,
    unreadCount: Int = 99,
    visible: Boolean = true,
    onItemClick: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
    ) {
        Feed(
            title = title,
            imageUrl = imageUrl,
            unreadCount = unreadCount,
            onItemClick = onItemClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AllFeeds(
    title: String = "All Feeds",
    unreadCount: Int = 99,
    onItemClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 2.dp,
                bottom = 2.dp,
            )
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.0f, fill = true),
        ) {
            Text(
                text = title,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
            )
        }
        if (unreadCount > 0) {
            val unreadLabel = LocalContext.current.resources.getQuantityString(
                R.plurals.n_unread_articles,
                unreadCount,
                unreadCount,
            )
            Text(
                text = unreadCount.toString(),
                maxLines = 1,
                modifier = Modifier.semantics {
                    contentDescription = unreadLabel
                },
            )
        }
    }
}

@Composable
private fun Feed(
    title: String,
    image: (@Composable () -> Unit),
    unreadCount: Int,
    onItemClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(
                start = 0.dp,
                end = 16.dp,
                top = 2.dp,
                bottom = 2.dp,
            )
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .minimumInteractiveComponentSize(),
//                    .height(48.dp)
//                    // Taking 4dp spacing into account
//                    .width(44.dp),
        ) {
            image()
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.0f, fill = true),
        ) {
            Text(
                text = title,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
            )
        }
        if (unreadCount > 0) {
            val unreadLabel = LocalContext.current.resources.getQuantityString(
                R.plurals.n_unread_articles,
                unreadCount,
                unreadCount,
            )
            Text(
                text = unreadCount.toString(),
                maxLines = 1,
                modifier = Modifier.semantics {
                    contentDescription = unreadLabel
                },
            )
        }
    }
}

@Composable
private fun Feed(
    title: String,
    imageUrl: URL?,
    unreadCount: Int,
    onItemClick: () -> Unit,
) {
    Feed(
        title = title,
        unreadCount = unreadCount,
        onItemClick = onItemClick,
        image = if (imageUrl != null) {
            {
                val pixels = with(LocalDensity.current) {
                    24.dp.roundToPx()
                }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl.toString()).listener(
                            onError = { a, b ->
                                Log.e("FEEDER_DRAWER", "error ${a.data}", b.throwable)
                            },
                        )
                        .scale(Scale.FIT)
                        .size(pixels)
                        .precision(Precision.INEXACT)
                        .build(),
                    contentDescription = stringResource(id = R.string.feed_icon),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(24.dp),
                )
            }
        } else {
            {
                Box(modifier = Modifier.size(24.dp)) {}
            }
        },
    )
}
