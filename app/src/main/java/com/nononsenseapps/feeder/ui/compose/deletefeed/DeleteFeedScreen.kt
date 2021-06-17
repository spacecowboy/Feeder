package com.nononsenseapps.feeder.ui.compose.deletefeed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize

@Composable
fun DeleteFeedDialog(
    feeds: List<DeletableFeed>,
    onDismiss: () -> Unit,
    onDelete: (Iterable<Long>) -> Unit
) {
    // TODO use remembersaveable
    val feedsToDelete = remember {
        feeds.map { feed -> feed.id to false }.toMutableStateMap()
    }

    DeleteFeedDialog(
        feeds = feeds,
        isChecked = { feedId ->
            feedsToDelete[feedId] ?: false
        },
        onDismiss = onDismiss,
        onOk = {
            onDelete(feedsToDelete.filterValues { it }.keys)
            onDismiss()
        },
        onToggleFeed = { feedId, checked ->
            feedsToDelete[feedId] = checked ?: !feedsToDelete.contains(feedId)
        }
    )
}

@Composable
fun DeleteFeedDialog(
    feeds: List<DeletableFeed>,
    isChecked: (Long) -> Boolean,
    onDismiss: () -> Unit,
    onOk: () -> Unit,
    onToggleFeed: (Long, Boolean?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onOk) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            stringResource(id = R.string.delete_feed)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(feeds) { feed ->
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeightIn(min = minimumTouchSize)
                            .clickable {
                                onToggleFeed(feed.id, !isChecked(feed.id))
                            }
                    ) {
                        Checkbox(
                            checked = isChecked(feed.id),
                            onCheckedChange = { checked ->
                                onToggleFeed(feed.id, checked)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feed.title,
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                }
            }
        }
    )
}

@Immutable
data class DeletableFeed(
    val id: Long,
    val title: String
)

@Composable
@Preview
private fun preview() =
    DeleteFeedDialog(
        feeds = listOf(
            DeletableFeed(1, "A Feed"),
            DeletableFeed(2, "Another Feed")
        ),
        onDismiss = {},
        onDelete = {}
    )
