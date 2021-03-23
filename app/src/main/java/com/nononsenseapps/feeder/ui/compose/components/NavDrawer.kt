package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedUnreadCount

const val EXPAND_ANIMATION_DURATION = 300
const val COLLAPSE_ANIMATION_DURATION = 300

@Composable
fun FeedList(feedListViewModel: FeedListViewModel) {
    val feedsAndTags by feedListViewModel.liveFeedsAndTagsWithUnreadCounts.observeAsState(initial = emptyList())
    val expandedTags by feedListViewModel.expandedTags.collectAsState()

    LazyColumn(

    ) {
        items(feedsAndTags) { item ->
            val onItemClick = { feedListViewModel.onItemClicked(item) }
            when {
                item.isTag -> ExpandableTag(
                    item = item,
                    expanded = item.tag in expandedTags,
                    onToggleExpand = { feedListViewModel.toggleExpansion(item.tag) },
                    onItemClick = onItemClick
                )
                item.isTop -> TopLevelFeed(item = item, onItemClick = onItemClick)
                item.tag in expandedTags -> ChildFeed(item = item, onItemClick = onItemClick)
            }
        }
    }
}

@Preview
@Composable
private fun ExpandableTag(
    item: FeedUnreadCount = FeedUnreadCount(tag = "News tag", unreadCount = 1),
    expanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    onItemClick: () -> Unit = {},
) {
    val transitionState = remember {
        MutableTransitionState(expanded).apply {
            targetState = !expanded
        }
    }
    val transition = updateTransition(transitionState)

    val arrowRotationDegree by transition.animateFloat({
        tween(durationMillis = EXPAND_ANIMATION_DURATION)
    }) {
        if (expanded) 0f else 180f
    }
    ConstraintLayout(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(start = 0.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val (expandButton, text, unreadCount) = createRefs()
        Text(
            text = item.unreadCount.toString(),
            maxLines = 1,
            modifier = Modifier
                .padding(start = 2.dp)
                .constrainAs(unreadCount) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    centerVerticallyTo(parent)
                }
        )
        ExpandArrow(
            degrees = arrowRotationDegree,
            onClick = onToggleExpand,
            modifier = Modifier
                .constrainAs(expandButton) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        )
        Text(
            text = item.tag,
            maxLines = 1,
            modifier = Modifier
                .padding(end = 2.dp)
                .constrainAs(text) {
                    top.linkTo(parent.top)
                    start.linkTo(expandButton.end)
                    end.linkTo(unreadCount.start)
                    width = Dimension.fillToConstraints
                    centerVerticallyTo(parent)
                }
        )
    }
}

@Composable
fun ExpandArrow(
    degrees: Float,
    modifier: Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_expand_less_24),
            contentDescription = stringResource(id = R.string.toggle_tag_expansion),
            modifier = Modifier.rotate(degrees = degrees)
        )
    }
}

@Preview
@Composable
fun TopLevelFeed(
    item: FeedUnreadCount = FeedUnreadCount(title = "A feed", unreadCount = 999),
    onItemClick: () -> Unit = {}
) = Feed(
    text = item.displayTitle,
    unreadCount = item.unreadCount,
    startPadding = 16.dp,
    onItemClick = onItemClick
)

@Preview
@Composable
fun ChildFeed(
    item: FeedUnreadCount = FeedUnreadCount(title = "Some feed", unreadCount = 21),
    onItemClick: () -> Unit = {}
) = Feed(
    text = item.displayTitle,
    unreadCount = item.unreadCount,
    startPadding = 48.dp,
    onItemClick = onItemClick
)

@Composable
private fun Feed(
    text: String,
    unreadCount: Int,
    startPadding: Dp,
    onItemClick: () -> Unit
) = Row(
    modifier = Modifier
        .clickable(onClick = onItemClick)
        .padding(start = startPadding, end = 4.dp, top = 2.dp, bottom = 2.dp)
        .fillMaxWidth()
        .height(48.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    Text(
        text = text,
        maxLines = 1,
        modifier = Modifier
            .padding(end = 2.dp)
    )
    Text(
        text = unreadCount.toString(),
        maxLines = 1,
        modifier = Modifier
            .padding(start = 2.dp)
    )
}
