package com.nononsenseapps.feeder.ui.compose.filter

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nononsenseapps.feeder.R

sealed interface ArticleFilter {
    val selected: Boolean
    val showBadge: Boolean
}

sealed interface ArticleStatusFilter : ArticleFilter {
    override val showBadge: Boolean
        get() = false

    data class ArticleStatusSaved(override val selected: Boolean) : ArticleStatusFilter

    data class ArticleStatusRead(override val selected: Boolean) : ArticleStatusFilter

    data class ArticleStatusRecentlyRead(override val selected: Boolean) : ArticleStatusFilter
}

sealed interface ArticleTagFilter : ArticleFilter {
    data class ArticleTagAll(override val selected: Boolean, override val showBadge: Boolean) : ArticleTagFilter

    data class ArticleTagUser(override val selected: Boolean, override val showBadge: Boolean, val tag: String) : ArticleTagFilter
}

sealed interface ArticleFeedFilter : ArticleFilter {
    data class ArticleFeedAll(override val selected: Boolean, override val showBadge: Boolean) : ArticleFeedFilter

    data class ArticleFeedOne(override val selected: Boolean, override val showBadge: Boolean, val title: String, val feedId: Long) : ArticleFeedFilter
}

sealed interface ArticleCategoryFilter : ArticleFilter {
    override val showBadge: Boolean
        get() = false

    data class ArticleCategoryAll(override val selected: Boolean) : ArticleCategoryFilter

    data class ArticleCategoryOne(override val selected: Boolean, val category: String) : ArticleCategoryFilter
}

// TODO string resource
@Composable
fun LabelForFilter(filter: ArticleFilter): String =
    when (filter) {
        is ArticleCategoryFilter.ArticleCategoryAll -> "All"
        is ArticleCategoryFilter.ArticleCategoryOne -> filter.category
        is ArticleStatusFilter.ArticleStatusRead -> stringResource(R.string.read_adjective)
        is ArticleStatusFilter.ArticleStatusRecentlyRead -> stringResource(R.string.recently_read_adjective)
        is ArticleStatusFilter.ArticleStatusSaved -> stringResource(R.string.saved_adjective)
        is ArticleTagFilter.ArticleTagAll -> "All"
        is ArticleTagFilter.ArticleTagUser -> filter.tag
        is ArticleFeedFilter.ArticleFeedAll -> "All"
        is ArticleFeedFilter.ArticleFeedOne -> filter.title
    }
