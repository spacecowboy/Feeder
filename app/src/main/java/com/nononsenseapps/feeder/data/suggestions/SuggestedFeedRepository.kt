package com.nononsenseapps.feeder.data.suggestions

import android.content.res.Resources
import com.nononsenseapps.feeder.R
import java.util.Locale
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@Serializable
data class SuggestedFeed(
    val title: String,
    val website: String,
    @SerialName("feed_url")
    val feedUrl: String,
)

@OptIn(ExperimentalSerializationApi::class)
class SuggestedFeedRepository(
    private val resources: Resources,
    private val json: Json,
) {
    private val suggestedFeeds: List<SuggestedFeed> by lazy { loadFeeds() }

    fun search(query: String, limit: Int = DEFAULT_RESULT_LIMIT): List<SuggestedFeed> {
        if (query.length < MIN_SEARCH_LENGTH) {
            return emptyList()
        }

        val tokens =
            query
                .lowercase(Locale.ROOT)
                .split(WHITESPACE_REGEX)
                .filter { it.length >= MIN_TOKEN_LENGTH }

        if (tokens.isEmpty()) {
            return emptyList()
        }

        return suggestedFeeds
            .asSequence()
            .filter { feed -> feed.matches(tokens) }
            .take(limit)
            .toList()
    }

    private fun loadFeeds(): List<SuggestedFeed> =
        runCatching {
            resources.openRawResource(R.raw.suggested_feeds).use { input ->
                json.decodeFromStream<List<SuggestedFeed>>(input)
            }
        }.getOrElse {
            emptyList()
        }

    private fun SuggestedFeed.matches(tokens: List<String>): Boolean {
        val haystack = searchableText
        return tokens.all { token -> haystack.contains(token) }
    }

    private val SuggestedFeed.searchableText: String
        get() =
            listOf(title, website, feedUrl)
                .joinToString(separator = " ")
                .lowercase(Locale.ROOT)

    companion object {
        private const val MIN_SEARCH_LENGTH = 2
        private const val MIN_TOKEN_LENGTH = 2
        private const val DEFAULT_RESULT_LIMIT = 10
        private val WHITESPACE_REGEX = "\\s+".toRegex()
    }
}
