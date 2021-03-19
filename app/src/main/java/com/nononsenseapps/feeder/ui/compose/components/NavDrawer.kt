package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R

@Preview
@Composable
private fun ExpandableItem(
    text: String = "Some tag",
    unreadCount: Int = 99
) {
    Row(
        modifier = Modifier
            .padding(start = 0.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO width expandableListPreferredItemPaddingLeft
        Image(
            // TODO R.drawable.tinted_expand_more
            painter = painterResource(id = R.drawable.ic_navigation_expand_more),
            contentDescription = stringResource(id = R.string.toggle_tag_expansion),
            modifier = Modifier
                .clickable(onClick = {})
        )
        Row(
            modifier = Modifier
                .clickable(onClick = {})
                .fillMaxWidth(),
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
    }
}

@Preview
@Composable
fun TopLevelItem(
    text: String = "Some feed",
    unreadCount: Int = 99
) = Feed(
    text = text,
    unreadCount = unreadCount,
    startPadding = 16.dp
)

@Preview
@Composable
fun ChildItem(
    text: String = "Some feed",
    unreadCount: Int = 99
) = Feed(
    text = text,
    unreadCount = unreadCount,
    startPadding = 39.dp // TODO expandableListPreferredChildPaddingLeft
)

@Composable
private fun Feed(
    text: String,
    unreadCount: Int,
    startPadding: Dp
) {
    Row(
        modifier = Modifier
            .padding(start = startPadding, end = 4.dp, top = 2.dp, bottom = 2.dp)
            .clickable(onClick = {})
            .fillMaxWidth(),
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
}
