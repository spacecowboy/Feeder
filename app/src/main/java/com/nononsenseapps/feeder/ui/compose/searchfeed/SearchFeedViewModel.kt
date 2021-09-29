package com.nononsenseapps.feeder.ui.compose.searchfeed

import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.model.FeedParser
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import org.kodein.di.DI
import org.kodein.di.instance

class SearchFeedViewModel(di: DI) : DIAwareViewModel(di) {
    private val feedParser: FeedParser by instance()

    fun searchForFeeds(url: URL) =
        flow {
            emit(url)
            feedParser.getAlternateFeedLinksAtUrl(url)
                .forEach {
                    emit(sloppyLinkToStrictURL(it.first))
                }
        }
            .mapNotNull {
                try {
                    feedParser.parseFeedUrl(it)?.let { feed ->
                        SearchResult(
                            title = feed.title ?: "",
                            url = feed.feed_url ?: it.toString(),
                            description = feed.description ?: "",
                            isError = false
                        )
                    }
                } catch (t: Throwable) {
                    SearchResult(
                        title = FAILED_TO_PARSE_PLACEHOLDER,
                        url = it.toString(),
                        description = t.message ?: "",
                        isError = true
                    )
                }
            }
            .flowOn(Dispatchers.Default)

    companion object {
        const val FAILED_TO_PARSE_PLACEHOLDER = "failed_to_parse"
    }
}
