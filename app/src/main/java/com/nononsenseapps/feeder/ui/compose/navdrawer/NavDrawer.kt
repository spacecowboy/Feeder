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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.immutableListHolderOf
import java.net.URL

const val COLLAPSE_ANIMATION_DURATION = 300

@ExperimentalAnimationApi
@Composable
@Preview(showBackground = true)
private fun ListOfFeedsAndTagsPreview() {
    FeederTheme {
        Surface {
            ListOfFeedsAndTags(
                immutableListHolderOf(
                    DrawerTop(unreadCount = 100, totalChildren = 4),
                    DrawerTag(
                        tag = "News tag",
                        unreadCount = 3,
                        -1111,
                        totalChildren = 2
                    ),
                    DrawerFeed(
                        id = 1,
                        displayTitle = "Times",
                        tag = "News tag",
                        unreadCount = 1
                    ),
                    DrawerFeed(
                        id = 2,
                        displayTitle = "Post",
                        tag = "News tag",
                        unreadCount = 2
                    ),
                    DrawerTag(
                        tag = "Funny tag",
                        unreadCount = 6,
                        -2222,
                        totalChildren = 1
                    ),
                    DrawerFeed(
                        id = 3,
                        displayTitle = "Hidden",
                        tag = "Funny tag",
                        unreadCount = 6
                    ),
                    DrawerFeed(
                        id = 4,
                        displayTitle = "Top Dog",
                        unreadCount = 99,
                        tag = ""
                    )
                ),
                ImmutableHolder(emptySet()),
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
    onToggleTagExpansion: (String) -> Unit,
    onItemClick: (DrawerItemWithUnreadCount) -> Unit,
) {
    LazyColumn(
        contentPadding = WindowInsets.systemBars.asPaddingValues(),
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                testTag = "feedsAndTags"
            }
    ) {
        items(feedsAndTags.item, key = { it.uiId }) { item ->
            when (item) {
                is DrawerTag -> {
                    ExpandableTag(
                        expanded = item.tag in expandedTags.item,
                        onToggleExpansion = onToggleTagExpansion,
                        unreadCount = item.unreadCount,
                        title = item.title(),
                        onItemClick = {
                            onItemClick(item)
                        }
                    )
                }
                is DrawerFeed -> {
                    when {
                        item.tag.isEmpty() -> TopLevelFeed(
                            unreadCount = item.unreadCount,
                            title = item.title(),
                            imageUrl = item.imageUrl,
                            onItemClick = {
                                onItemClick(item)
                            }
                        )
                        else -> {
                            ChildFeed(
                                unreadCount = item.unreadCount,
                                title = item.title(),
                                imageUrl = item.imageUrl,
                                visible = item.tag in expandedTags.item,
                                onItemClick = {
                                    onItemClick(item)
                                }
                            )
                        }
                    }
                }
                is DrawerTop -> AllFeeds(
                    unreadCount = item.unreadCount,
                    title = item.title(),
                    onItemClick = {
                        onItemClick(item)
                    }
                )
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
        animationSpec = tween()
    )

    val toggleExpandLabel = stringResource(id = R.string.toggle_tag_expansion)
    val expandedLabel = stringResource(id = R.string.expanded_tag)
    val contractedLabel = stringResource(id = R.string.contracted_tag)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onItemClick)
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
                        }
                    )
                } catch (e: Exception) {
                    // Observed nullpointer exception when setting customActions
                    // No clue why it could be null
                    Log.e("FeederNavDrawer", "Exception in semantics", e)
                }
            }
    ) {
        ExpandArrow(
            degrees = angle,
            onClick = {
                onToggleExpansion(title)
            }
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.0f, fill = true)
        ) {
            Text(
                text = title,
                maxLines = 1,
                modifier = Modifier
                    .padding(end = 2.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
            )
        }
        val unreadLabel = LocalContext.current.resources
            .getQuantityString(R.plurals.n_unread_articles, unreadCount, unreadCount)
        Text(
            text = unreadCount.toString(),
            maxLines = 1,
            modifier = Modifier
                .padding(start = 2.dp)
                .semantics {
                    contentDescription = unreadLabel
                }
        )
    }
}

@Composable
private fun ExpandArrow(
    degrees: Float,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.clearAndSetSemantics { }
    ) {
        Icon(
            Icons.Filled.ExpandLess,
            contentDescription = stringResource(id = R.string.toggle_tag_expansion),
            modifier = Modifier.rotate(degrees = degrees)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AllFeeds(
    title: String = "Foo",
    unreadCount: Int = 99,
    onItemClick: () -> Unit = {},
) = Feed(
    title = title,
    imageUrl = null,
    unreadCount = unreadCount,
    startPadding = 16.dp,
    onItemClick = onItemClick,
)

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
    startPadding = 16.dp,
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
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Feed(
            title = title,
            imageUrl = imageUrl,
            unreadCount = unreadCount,
            startPadding = 48.dp,
            onItemClick = onItemClick,
        )
    }
}

@Composable
private fun Feed(
    title: String,
    imageUrl: URL?,
    unreadCount: Int,
    startPadding: Dp,
    onItemClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(
                start = startPadding,
                end = 16.dp,
                top = 2.dp,
                bottom = 2.dp
            )
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (imageUrl != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(48.dp)
                    // Taking 4dp spacing into account
                    .width(44.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl.toString())
                        .listener(
                            onError = { a, b ->
                                Log.e("FEEDER_DRAWER", "error ${a.data}", b.throwable)
                            }
                        )
                        .scale(Scale.FIT)
                        .size(64)
                        .precision(Precision.INEXACT)
                        .build(),
                    contentDescription = stringResource(id = R.string.feed_icon),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.0f, fill = true)
        ) {
            Text(
                text = title,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
            )
        }
        val unreadLabel = LocalContext.current.resources
            .getQuantityString(R.plurals.n_unread_articles, unreadCount, unreadCount)
        Text(
            text = unreadCount.toString(),
            maxLines = 1,
            modifier = Modifier.semantics {
                contentDescription = unreadLabel
            }
        )
    }
}
