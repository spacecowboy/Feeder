package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
const val FADE_IN_ANIMATION_DURATION = 350
const val FADE_OUT_ANIMATION_DURATION = 300

@ExperimentalAnimationApi
@Composable
fun FeedList(feedListViewModel: FeedListViewModel) {
    val feedsAndTags by feedListViewModel.liveFeedsAndTagsWithUnreadCounts.observeAsState(initial = emptyList())
    val expandedTags by feedListViewModel.expandedTags.collectAsState()

    val onItemClick: (FeedUnreadCount) -> Unit = { item ->
        feedListViewModel.onItemClicked(item)
    }

    Column(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
    ) {
        for (item in feedsAndTags) {
            when {
                item.isTag -> ExpandableTag(
                    item = item,
                    children = item.children,
                    expanded = item.tag in expandedTags,
                    onToggleExpand = { feedListViewModel.toggleExpansion(item.tag) },
                    onItemClick = onItemClick
                )
                item.isTop -> TopLevelFeed(item = item, onItemClick = onItemClick)
                item.tag.isEmpty() -> TopLevelFeed(item = item, onItemClick = onItemClick)
                // Inside objects now
//                item.tag in expandedTags -> ChildFeed(item = item, onItemClick = onItemClick)
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview
@Composable
private fun ExpandableTag(
    item: FeedUnreadCount = FeedUnreadCount(tag = "News tag", unreadCount = 3),
    children: List<FeedUnreadCount> = listOf(
        FeedUnreadCount(title = "foo", unreadCount = 2),
        FeedUnreadCount(title = "bar", unreadCount = 1)
    ),
    expanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    onItemClick: (FeedUnreadCount) -> Unit = {},
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
            .clickable(onClick = { onItemClick(item) })
            .padding(top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
    ) {
        val (expandButton, text, unreadCount, childItems) = createRefs()
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
            text = item.unreadCount.toString(),
            maxLines = 1,
            modifier = Modifier
                .padding(start = 2.dp, end = 4.dp)
                .constrainAs(unreadCount) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    centerVerticallyTo(expandButton)
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
                    centerVerticallyTo(expandButton)
                }
        )
        TagChildren(
            visible = expanded,
            children = children,
            onItemClick = onItemClick,
            modifier = Modifier
                .constrainAs(childItems) {
                    top.linkTo(expandButton.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun TagChildren(
    visible: Boolean,
    children: List<FeedUnreadCount>,
    onItemClick: (FeedUnreadCount) -> Unit = {},
    modifier: Modifier
) {
    val enterFadeIn = remember {
        fadeIn(
            animationSpec = TweenSpec(
                durationMillis = FADE_IN_ANIMATION_DURATION,
                easing = FastOutLinearInEasing
            )
        )
    }
    val enterExpand = remember {
        expandVertically(animationSpec = tween(EXPAND_ANIMATION_DURATION))
    }
    val exitFadeOut = remember {
        fadeOut(
            animationSpec = TweenSpec(
                durationMillis = FADE_OUT_ANIMATION_DURATION,
                easing = LinearOutSlowInEasing
            )
        )
    }
    val exitCollapse = remember {
        shrinkVertically(animationSpec = tween(COLLAPSE_ANIMATION_DURATION))
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enterExpand + enterFadeIn,
        exit = exitCollapse + exitFadeOut
    ) {
        LazyColumn {
            items(children) { feed ->
                ChildFeed(
                    item = feed,
                    onItemClick = onItemClick
                )
            }
        }
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
    onItemClick: (FeedUnreadCount) -> Unit = {}
) = Feed(
    title = item.displayTitle,
    unreadCount = item.unreadCount,
    startPadding = 16.dp,
    onItemClick = { onItemClick(item) }
)

@Preview
@Composable
fun ChildFeed(
    item: FeedUnreadCount = FeedUnreadCount(title = "Some feed", unreadCount = 21),
    onItemClick: (FeedUnreadCount) -> Unit = {}
) = Feed(
    title = item.displayTitle,
    unreadCount = item.unreadCount,
    startPadding = 48.dp,
    onItemClick = { onItemClick(item) }
)

@Composable
private fun Feed(
    title: String,
    unreadCount: Int,
    startPadding: Dp,
    onItemClick: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(start = startPadding, end = 4.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val (refText, refCount) = createRefs()
        Text(
            text = unreadCount.toString(),
            maxLines = 1,
            modifier = Modifier
                .padding(start = 2.dp)
                .constrainAs(refCount) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    centerVerticallyTo(parent)
                }
        )
        Text(
            text = title,
            maxLines = 1,
            modifier = Modifier
                .padding(end = 2.dp)
                .constrainAs(refText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(refCount.start)
                    width = Dimension.fillToConstraints
                    centerVerticallyTo(parent)
                }
        )
    }
}
