package com.nononsenseapps.feeder.data.suggestions

import android.content.res.Resources
import com.nononsenseapps.feeder.R
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.Locale

private val WORD_REGEX = Regex("[\\p{L}\\p{N}']+")
private val SUBSTACK_FEED_REGEX = Regex("^https://([\\p{L}\\p{N}-]+)\\.substack\\.com/feed/?$", RegexOption.IGNORE_CASE)

private fun tokenize(text: String): List<String> =
    WORD_REGEX
        .findAll(text)
        .map { it.value.lowercase(Locale.ROOT) }
        .toList()

private fun normalizeForTextMatch(text: String): String = tokenize(text).joinToString(separator = " ")

private fun String.toSearchableUrlText(): String {
    val match = SUBSTACK_FEED_REGEX.matchEntire(this.trim())
    return match?.groupValues?.getOrNull(1) ?: this
}

@Serializable
data class SuggestedFeed(
    @SerialName("headline")
    val headline: String,
    @SerialName("author_name")
    val authorName: String = "",
    @SerialName("feed_url")
    val feedUrl: String,
    @SerialName("total_subscribers")
    val totalSubscribersRaw: String = "",
) {
    @Transient
    val headlineTokens: List<String> = tokenize(headline)

    @Transient
    val headlineTokenSet: Set<String> = headlineTokens.toSet()

    @Transient
    val authorTokens: List<String> = tokenize(authorName)

    @Transient
    val authorTokenSet: Set<String> = authorTokens.toSet()

    @Transient
    val urlSearchText: String = feedUrl.toSearchableUrlText()

    @Transient
    val urlTokens: List<String> = tokenize(urlSearchText)

    @Transient
    val urlTokenSet: Set<String> = urlTokens.toSet()

    @Transient
    val normalizedHeadlineText: String = normalizeForTextMatch(headline)

    @Transient
    val normalizedAuthorText: String = normalizeForTextMatch(authorName)

    @Transient
    val normalizedUrlText: String = normalizeForTextMatch(urlSearchText)

    @Transient
    val totalSubscribers: Int = totalSubscribersRaw.toIntOrNull() ?: 0
}

private data class ScoredFeed(
    val feed: SuggestedFeed,
    val isExactHeadlineTokensMatch: Boolean,
    val isExactAuthorTokensMatch: Boolean,
    val isExactHeadlineTextMatch: Boolean,
    val isExactAuthorTextMatch: Boolean,
    val containsQueryText: Boolean,
    val headlineContainsQueryText: Boolean,
    val authorContainsQueryText: Boolean,
    val urlContainsQueryText: Boolean,
    val headlineTokenMatches: Int,
    val authorTokenMatches: Int,
    val urlTokenMatches: Int,
    val urlPartialMatches: Int,
)

@OptIn(ExperimentalSerializationApi::class)
class SuggestedFeedRepository(
    private val resources: Resources,
    private val json: Json,
) {
    private val suggestedFeeds: List<SuggestedFeed> by lazy { loadFeeds() }

    fun preload() {
        // Accessing the lazy property forces the dataset to load.
        suggestedFeeds
    }

    fun search(
        query: String,
        limit: Int = DEFAULT_RESULT_LIMIT,
    ): List<SuggestedFeed> {
        if (query.length < MIN_SEARCH_LENGTH) {
            return emptyList()
        }

        val tokens =
            tokenize(query)
                .filter { it.length >= MIN_TOKEN_LENGTH }

        if (tokens.isEmpty()) {
            return emptyList()
        }

        val normalizedQueryText = normalizeForTextMatch(query)

        return suggestedFeeds
            .asSequence()
            .map { feed -> scoreFeed(feed, tokens, normalizedQueryText) }
            .filter { scored ->
                scored.containsQueryText
            }.sortedByDescending { it.feed.totalSubscribers }
            .take(limit)
            .map { it.feed }
            .toList()
    }

    private fun loadFeeds(): List<SuggestedFeed> =
        runCatching {
            resources.openRawResource(R.raw.feeds_mapped).use { input ->
                json.decodeFromStream<List<SuggestedFeed>>(input)
            }
        }.getOrElse {
            emptyList()
        }

    private fun scoreFeed(
        feed: SuggestedFeed,
        queryTokens: List<String>,
        normalizedQueryText: String,
    ): ScoredFeed {
        val cleanedQueryTokens =
            queryTokens.mapNotNull { token ->
                token.trim().takeIf { it.isNotEmpty() }
            }

        if (cleanedQueryTokens.isEmpty()) {
            return ScoredFeed(
                feed = feed,
                isExactHeadlineTokensMatch = false,
                isExactAuthorTokensMatch = false,
                isExactHeadlineTextMatch = false,
                isExactAuthorTextMatch = false,
                containsQueryText = false,
                headlineContainsQueryText = false,
                authorContainsQueryText = false,
                urlContainsQueryText = false,
                headlineTokenMatches = 0,
                authorTokenMatches = 0,
                urlTokenMatches = 0,
                urlPartialMatches = 0,
            )
        }

        val queryTokenList = cleanedQueryTokens
        val queryTokenSet = queryTokenList.toSet()
        val queryText = queryTokenList.joinToString(separator = " ")
        val isExactHeadlineTokensMatch = feed.headlineTokens == queryTokenList
        val isExactAuthorTokensMatch = feed.authorTokens == queryTokenList
        val isExactHeadlineTextMatch = feed.normalizedHeadlineText == queryText
        val isExactAuthorTextMatch = feed.normalizedAuthorText == queryText
        val headlineContainsQueryText =
            normalizedQueryText.isNotEmpty() &&
                feed.normalizedHeadlineText.contains(normalizedQueryText)
        val authorContainsQueryText =
            normalizedQueryText.isNotEmpty() &&
                feed.normalizedAuthorText.contains(normalizedQueryText)
        val urlContainsQueryText =
            normalizedQueryText.isNotEmpty() &&
                feed.normalizedUrlText.contains(normalizedQueryText)
        val containsQueryText = headlineContainsQueryText || authorContainsQueryText || urlContainsQueryText

        if (!containsQueryText) {
            return ScoredFeed(
                feed = feed,
                isExactHeadlineTokensMatch = false,
                isExactAuthorTokensMatch = false,
                isExactHeadlineTextMatch = false,
                isExactAuthorTextMatch = false,
                containsQueryText = false,
                headlineContainsQueryText = false,
                authorContainsQueryText = false,
                urlContainsQueryText = false,
                headlineTokenMatches = 0,
                authorTokenMatches = 0,
                urlTokenMatches = 0,
                urlPartialMatches = 0,
            )
        }

        val headlineTokenMatches = countMatches(feed.headlineTokenSet, queryTokenSet)
        val authorTokenMatches = countMatches(feed.authorTokenSet, queryTokenSet)
        val urlTokenMatches = countMatches(feed.urlTokenSet, queryTokenSet)

        val urlPartialMatches =
            queryTokenList.count { token ->
                token !in feed.urlTokenSet && feed.normalizedUrlText.contains(token)
            }

        return ScoredFeed(
            feed = feed,
            isExactHeadlineTokensMatch = isExactHeadlineTokensMatch,
            isExactAuthorTokensMatch = isExactAuthorTokensMatch,
            isExactHeadlineTextMatch = isExactHeadlineTextMatch,
            isExactAuthorTextMatch = isExactAuthorTextMatch,
            containsQueryText = containsQueryText,
            headlineContainsQueryText = headlineContainsQueryText,
            authorContainsQueryText = authorContainsQueryText,
            urlContainsQueryText = urlContainsQueryText,
            headlineTokenMatches = headlineTokenMatches,
            authorTokenMatches = authorTokenMatches,
            urlTokenMatches = urlTokenMatches,
            urlPartialMatches = urlPartialMatches,
        )
    }

    private fun countMatches(
        tokenSet: Set<String>,
        queryTokens: Set<String>,
    ): Int = queryTokens.count { it in tokenSet }

    companion object {
        private const val MIN_SEARCH_LENGTH = 2
        private const val MIN_TOKEN_LENGTH = 2
        private const val DEFAULT_RESULT_LIMIT = 10
        private val WORD_REGEX = Regex("[\\p{L}\\p{N}']+")
    }
}
