package com.nononsenseapps.feeder.ui.compose.navdrawer

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerFeed
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerItemWithUnreadCount
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTag
import com.nononsenseapps.feeder.ui.compose.navdrawer.DrawerTop

const val COLLAPSE_ANIMATION_DURATION = 300

@ExperimentalAnimationApi
@Composable
@Preview(showBackground = true)
private fun ListOfFeedsAndTagsPreview() {
    ListOfFeedsAndTags(
        listOf(
            DrawerTop(unreadCount = 100),
            DrawerTag(tag = "News tag", unreadCount = 3),
            DrawerFeed(id = 1, displayTitle = "Times", tag = "News tag", unreadCount = 1),
            DrawerFeed(id = 2, displayTitle = "Post", tag = "News tag", unreadCount = 2),
            DrawerTag(tag = "Funny tag", unreadCount = 6),
            DrawerFeed(id = 3, displayTitle = "Hidden", tag = "Funny tag", unreadCount = 6),
            DrawerFeed(id = 4, displayTitle = "Top Dog", unreadCount = 99, tag = "")
        )
    ) {}
}

@ExperimentalAnimationApi
@Composable
fun ListOfFeedsAndTags(
    feedsAndTags: List<DrawerItemWithUnreadCount>,
    onItemClick: (DrawerItemWithUnreadCount) -> Unit
) {
    var expandedTags by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(feedsAndTags, key = { it.uiId }) { item ->
            when (item) {
                is DrawerTag -> {
                    ExpandableTag(
                        expanded = item.tag in expandedTags,
                        onExpand = { expandedTags = expandedTags + item.tag },
                        onContract = { expandedTags = expandedTags - item.tag },
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
                            onItemClick = {
                                onItemClick(item)
                            }
                        )
                        else -> {
                            ChildFeed(
                                unreadCount = item.unreadCount,
                                title = item.title(),
                                visible = item.tag in expandedTags,
                                onItemClick = {
                                    onItemClick(item)
                                }
                            )
                        }
                    }
                }
                is DrawerTop -> TopLevelFeed(
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
    onExpand: (String) -> Unit = {},
    onContract: (String) -> Unit = {},
    onItemClick: () -> Unit = {},
) {
    val angle: Float by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween()
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(top = 2.dp, bottom = 2.dp, end = 16.dp)
            .fillMaxWidth()
    ) {
        ExpandArrow(
            degrees = angle,
            onClick = {
                if (expanded) {
                    onContract(title)
                } else {
                    onExpand(title)
                }
            }
        )
        Text(
            text = title,
            maxLines = 1,
            modifier = Modifier
                .padding(end = 2.dp)
                .weight(1.0f, fill = true)
        )
        Text(
            text = unreadCount.toString(),
            maxLines = 1,
            modifier = Modifier
                .padding(start = 2.dp)
        )
    }
}

@Composable
private fun ExpandArrow(
    degrees: Float,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
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
private fun TopLevelFeed(
    title: String = "Foo",
    unreadCount: Int = 99,
    onItemClick: () -> Unit = {}
) = Feed(
    title = title,
    unreadCount = unreadCount,
    startPadding = 16.dp,
    onItemClick = onItemClick
)

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
private fun ChildFeed(
    title: String = "Foo",
    unreadCount: Int = 99,
    visible: Boolean = true,
    onItemClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Feed(
            title = title,
            unreadCount = unreadCount,
            startPadding = 48.dp,
            onItemClick = onItemClick
        )
    }
}

@Composable
private fun Feed(
    title: String,
    unreadCount: Int,
    startPadding: Dp,
    onItemClick: () -> Unit
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
        Text(
            text = title,
            maxLines = 1,
            modifier = Modifier
                .weight(1.0f, fill = true)
        )
        Text(
            text = unreadCount.toString(),
            maxLines = 1
        )
    }
}
