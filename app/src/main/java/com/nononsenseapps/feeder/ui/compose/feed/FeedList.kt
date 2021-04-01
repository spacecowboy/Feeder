package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.PreviewItem

@Composable
fun FeedList(
    feedItems: LazyPagingItems<PreviewItem>,
    onItemClick: (Long) -> Unit
) {
    LazyColumn {
        items(feedItems) { previewItem ->
            if (previewItem == null) {
                return@items
            }

            FeedItemPreview(item = previewItem, onItemClick = { onItemClick(previewItem.id) })
        }
    }
}

@Composable
@Preview
fun NothingToRead() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // TODO clickable
        Text(stringResource(id = R.string.empty_feed_top))
        Spacer(modifier = Modifier.height(32.dp))
        Text(stringResource(id = R.string.empty_feed_open))
        Spacer(modifier = Modifier.height(32.dp))
        Text(stringResource(id = R.string.empty_feed_add))
    }
}
