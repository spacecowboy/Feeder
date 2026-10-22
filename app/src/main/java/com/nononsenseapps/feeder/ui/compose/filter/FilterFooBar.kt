package com.nononsenseapps.feeder.ui.compose.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.utils.WithAllPreviewProviders

@Composable
fun FilterFoo(
    statusFilters: List<ArticleStatusFilter>,
    tagFilters: List<ArticleTagFilter>,
    feedFilters: List<ArticleFeedFilter>,
    categoryFilters: List<ArticleCategoryFilter>,
    onFilterClick: (ArticleFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp), // TODO spacing
        modifier =
            Modifier
                .verticalScroll(scrollState)
                .then(modifier),
    ) {
        FilterSectionHeading(
            text = "Article Status",
            icon = {
                Icon(
                    Icons.AutoMirrored.Outlined.Article,
                    contentDescription = null,
                )
            },
        )
        FilterChips(
            filters = statusFilters,
            onClick = onFilterClick,
            modifier = Modifier.padding(start = 12.dp), // Matches padding of the icon box itself
        )
        // only show tags if tags exist, "all" is always present so there must be more than 1 item
        if (tagFilters.size > 1) {
            HorizontalDivider()
            FilterSectionHeading(
                text = stringResource(id = R.string.tag),
                icon = {
                    Icon(
                        Icons.Outlined.Folder,
                        contentDescription = null,
                    )
                },
            )
            FilterChips(
                filters = tagFilters,
                onClick = onFilterClick,
                modifier = Modifier.padding(start = 12.dp), // Matches padding of the icon box itself
            )
        }
        // only show feeds if feeds exist, "all" is always present so there must be more than 1 item
        if (feedFilters.size > 1) {
            HorizontalDivider()
            FilterSectionHeading(
                // TODO stringresource
                text = "Feed",
                icon = {
                    Icon(
                        Icons.Outlined.RssFeed,
                        contentDescription = null,
                    )
                },
            )
            FilterChips(
                filters = feedFilters,
                onClick = onFilterClick,
                modifier = Modifier.padding(start = 12.dp), // Matches padding of the icon box itself
            )
        }
        // only show categories if categories exist, "all" is always present so there must be more than 1 item
        if (categoryFilters.size > 1) {
            HorizontalDivider()
            FilterSectionHeading(
                text = "Categories",
                icon = {
                    Icon(
                        Icons.Outlined.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            )
            FilterChips(
                filters = categoryFilters,
                onClick = onFilterClick,
                modifier = Modifier.padding(start = 12.dp), // Matches padding of the icon box itself
            )
        }
    }
}

@Composable
fun FilterChips(
    filters: List<ArticleFilter>,
    modifier: Modifier = Modifier,
    onClick: (ArticleFilter) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.then(modifier),
    ) {
        for (filter in filters) {
            // Using a box to get the badge properly aligned inside the FilterChip
            // BadgeBox will place it outside and does not allow padding to be defined
            Box(
                contentAlignment = Alignment.TopEnd,
            ) {
                FilterChip(
                    selected = filter.selected,
                    leadingIcon =
                        if (filter.selected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null,
                                    // 18dp specified at https://m3.material.io/components/chips/specs
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        },
                    onClick = {
                        onClick(filter)
                    },
                    label = {
                        Text(
                            LabelForFilter(filter),
                            // Highlight items with unread items with a bit bolder text to further
                            // the idea of "click me"
                            fontWeight =
                                if (filter.showBadge) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                        )
                    },
                )
                if (filter.showBadge) {
                    // items with unread items get a very explicit red badge to indicate new items available
                    Badge(
                        modifier =
                            Modifier
                                // Padding to make the badge aligned with the corner radius
                                .padding(top = 12.dp, end = 4.dp)
                                .semantics { contentDescription = "Unread articles" }, // TODO string resource
                    )
                }
            }
        }
    }
}

@Composable
fun FilterSectionHeading(
    text: String,
    icon: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            // TODO alpha
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                icon()
            }
        }
        Text(
            text,
            // TODO style
            style =
                MaterialTheme.typography.bodyLarge.merge(
                    TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    ),
                ),
        )
    }
}

@Preview
@Composable
fun PreviewFilterFoo() {
    WithAllPreviewProviders {
        Surface {
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
                    ),
                onFilterClick = {},
            )
        }
    }
}
