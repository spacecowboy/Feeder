package com.nononsenseapps.feeder.ui.compose.searchfeed

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.model.FeedParser
import com.nononsenseapps.feeder.model.SiteMetaData
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLOrNull
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import org.kodein.di.DI
import org.kodein.di.instance

class SearchFeedViewModel(di: DI) : DIAwareViewModel(di) {
    private val feedParser: FeedParser by instance()

    private var siteMetaData: SiteMetaData? by mutableStateOf(null)

    fun searchForFeeds(url: URL): Flow<SearchResult> {
        return flow {
            siteMetaData = feedParser.getSiteMetaData(url)
            // Flow collection makes this block concurrent with map below
            val initialSiteMetaData = siteMetaData
            emit(url)

            initialSiteMetaData?.alternateFeedLinks?.forEach {
                emit(it.link)
            }
        }
            .mapNotNull {
                try {
                    feedParser.parseFeedUrl(it)?.let { feed ->
                        if (siteMetaData == null) {
                            feed.home_page_url?.let { pageLink ->
                                sloppyLinkToStrictURLOrNull(pageLink)?.let { pageUrl ->
                                    siteMetaData = feedParser.getSiteMetaData(pageUrl)
                                }
                            }
                        }

                        SearchResult(
                            title = feed.title ?: "",
                            url = feed.feed_url ?: it.toString(),
                            description = feed.description ?: "",
                            isError = false,
                            feedImage = siteMetaData?.feedImage ?: "",
                        )
                    }
                } catch (t: Throwable) {
                    Log.e(LOG_TAG, "Failed to parse", t)
                    SearchResult(
                        title = FAILED_TO_PARSE_PLACEHOLDER,
                        url = it.toString(),
                        description = t.message ?: "",
                        isError = true,
                        feedImage = siteMetaData?.feedImage ?: "",
                    )
                }
            }
            .flowOn(Dispatchers.Default)
    }

    companion object {
        const val FAILED_TO_PARSE_PLACEHOLDER = "failed_to_parse"
        const val LOG_TAG = "FEEDER_SearchFeed"
    }
}
