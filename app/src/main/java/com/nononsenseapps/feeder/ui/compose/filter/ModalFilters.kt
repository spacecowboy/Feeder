package com.nononsenseapps.feeder.ui.compose.filter

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nononsenseapps.feeder.ui.compose.utils.WithAllPreviewProviders

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalFilters(
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.PartiallyExpanded),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.then(modifier),
    ) {
        FilterFoo(
            statusFilters = listOf(ArticleStatusFilter.ArticleStatusSaved(true), ArticleStatusFilter.ArticleStatusRecentlyRead(true), ArticleStatusFilter.ArticleStatusRead(false)),
            tagFilters =
                listOf(
                    ArticleTagFilter.ArticleTagAll(true, true),
                    ArticleTagFilter.ArticleTagUser(false, false, "Don't Miss"),
                    ArticleTagFilter.ArticleTagUser(false, true, "Daily"),
                    ArticleTagFilter.ArticleTagUser(false, true, "Bored"),
                ),
            feedFilters =
                listOf(
                    ArticleFeedFilter.ArticleFeedAll(false, true),
                    ArticleFeedFilter.ArticleFeedOne(true, true, "Cornucopia", 1L),
                    ArticleFeedFilter.ArticleFeedOne(false, true, "Fastmail Status - Incident history", 1L),
                    ArticleFeedFilter.ArticleFeedOne(true, false, "Richard Stallman's Political Notes", 1L),
                    ArticleFeedFilter.ArticleFeedOne(false, false, "xkcd", 1L),
                    ArticleFeedFilter.ArticleFeedOne(true, false, "Feeder news", 1L),
                    ArticleFeedFilter.ArticleFeedOne(false, true, "Ars Technica", 1L),
                ),
            categoryFilters =
                listOf(
                    ArticleCategoryFilter.ArticleCategoryAll(false),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Tech"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Media"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "News"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Defense"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Environment"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Tech"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Media"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "News"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Defense"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Environment"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Tech"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Media"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "News"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Defense"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Environment"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Tech"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Media"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "News"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Defense"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Environment"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Tech"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Media"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "News"),
                    ArticleCategoryFilter.ArticleCategoryOne(false, "Defense"),
                    ArticleCategoryFilter.ArticleCategoryOne(true, "Environment"),
                ),
            onFilterClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewModalFilters() {
    WithAllPreviewProviders {
        Surface {
            ModalFilters(
                onDismiss = {},
            )
        }
    }
}
