package com.nononsenseapps.feeder.model

import android.app.Application
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.openai.OpenAIApi.TranslationResult
import com.nononsenseapps.feeder.openai.OpenAIApi
import com.nononsenseapps.feeder.openai.canTranslate
import com.nononsenseapps.feeder.openai.canUseAsTranslationApi
import com.nononsenseapps.feeder.openai.isDeepL
import com.nononsenseapps.feeder.openai.isGoogleTranslate
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import com.nononsenseapps.feeder.util.FilePathProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.File
import java.security.MessageDigest

class TranslationManager(
    override val di: DI,
) : DIAware {
    private val application: Application by instance()
    private val repository: Repository by instance()
    private val openAIApi: OpenAIApi by instance()
    private val filePathProvider: FilePathProvider by instance()
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    suspend fun getCachedTranslatedFeedListItem(
        item: FeedListItem,
        settings: OpenAISettings,
        targetLanguage: String,
    ): CachedFeedListItemTranslation =
        withContext(Dispatchers.IO) {
            if (!settings.canTranslate || targetLanguage.isBlank()) {
                return@withContext CachedFeedListItemTranslation(
                    item = item,
                    hasCachedTranslation = false,
                    isFullyCached = false,
                )
            }

            val cache = loadCache(item.id, settings, targetLanguage)
            val titleHash = sha256(item.title)
            val snippetHash = sha256(item.snippet)

            val hasCachedTitle =
                item.title.isNotBlank() &&
                    cache.titleHash == titleHash &&
                    !cache.translatedTitle.isNullOrBlank()
            val hasCachedSnippet =
                item.snippet.isNotBlank() &&
                    cache.snippetHash == snippetHash &&
                    !cache.translatedSnippet.isNullOrBlank()
            val titleReady = item.title.isBlank() || hasCachedTitle
            val snippetReady = item.snippet.isBlank() || hasCachedSnippet

            CachedFeedListItemTranslation(
                item =
                    item.copy(
                        title = cache.translatedTitle.takeIf { hasCachedTitle } ?: item.title,
                        snippet = cache.translatedSnippet.takeIf { hasCachedSnippet } ?: item.snippet,
                    ),
                hasCachedTranslation = hasCachedTitle || hasCachedSnippet,
                isFullyCached = titleReady && snippetReady,
            )
        }

    suspend fun translateFeedListItem(item: FeedListItem): FeedListItem =
        withContext(Dispatchers.IO) {
            val settings = translationSettings()
            val targetLanguage = repository.preferredTranslationLanguage.value.trim()
            if (!settings.canTranslate || targetLanguage.isBlank()) {
                return@withContext item
            }

            val cache = loadCache(item.id, settings, targetLanguage)
            val titleHash = sha256(item.title)
            val snippetHash = sha256(item.snippet)
            val cachedTitle =
                cache.translatedTitle.takeIf {
                    cache.titleHash == titleHash &&
                        !cache.translatedTitle.isNullOrBlank()
                }
            val cachedSnippet =
                cache.translatedSnippet.takeIf {
                    cache.snippetHash == snippetHash &&
                        !cache.translatedSnippet.isNullOrBlank()
                }

            val titleReady = item.title.isBlank() || cachedTitle != null
            val snippetReady = item.snippet.isBlank() || cachedSnippet != null
            if (titleReady && snippetReady) {
                return@withContext item.copy(
                    title = cachedTitle ?: item.title,
                    snippet = cachedSnippet ?: item.snippet,
                )
            }

            val detectedSameLanguage =
                detectSourceLanguageIfAlreadyTargetLanguage(
                    content =
                        listOf(item.title, item.snippet)
                            .filter(String::isNotBlank)
                            .joinToString(separator = "\n\n"),
                    targetLanguage = targetLanguage,
                    settings = settings,
                )
            if (detectedSameLanguage != null) {
                val updatedCache =
                    cache.copy(
                        sourceLanguage = detectedSameLanguage,
                        titleHash = if (item.title.isBlank()) null else titleHash,
                        translatedTitle = if (item.title.isBlank()) null else item.title,
                        snippetHash = if (item.snippet.isBlank()) null else snippetHash,
                        translatedSnippet = if (item.snippet.isBlank()) null else item.snippet,
                    )
                if (updatedCache != cache) {
                    saveCache(
                        item.id,
                        settings,
                        targetLanguage,
                        updatedCache,
                    )
                }
                return@withContext item
            }

            val translatedTitleResult =
                if (item.title.isBlank() || cachedTitle != null) {
                    null
                } else {
                    translateOrNull(
                        content = item.title,
                        targetLanguage = targetLanguage,
                        settings = settings,
                    )
                }
            val translatedTitle = cachedTitle ?: translatedTitleResult?.content.orEmpty()

            val translatedSnippetResult =
                if (item.snippet.isBlank() || cachedSnippet != null) {
                    null
                } else {
                    translateOrNull(
                        content = item.snippet,
                        targetLanguage = targetLanguage,
                        settings = settings,
                    )
                }
            val translatedSnippet = cachedSnippet ?: translatedSnippetResult?.content.orEmpty()
            val cachedSourceLanguage =
                if (cachedTitle != null || cachedSnippet != null) {
                    cache.sourceLanguage
                } else {
                    ""
                }
            val sourceLanguage =
                translatedSnippetResult?.detectedLanguageOrBlank().orEmpty()
                    .ifBlank { translatedTitleResult?.detectedLanguageOrBlank().orEmpty() }
                    .ifBlank { cachedSourceLanguage }

            val updatedCache =
                cache.copy(
                    sourceLanguage = sourceLanguage.ifBlank { cachedSourceLanguage },
                    titleHash =
                        when {
                            item.title.isBlank() -> null
                            translatedTitle.isNotBlank() -> titleHash
                            else -> cache.titleHash
                        },
                    translatedTitle =
                        when {
                            item.title.isBlank() -> null
                            translatedTitle.isNotBlank() -> translatedTitle
                            else -> cache.translatedTitle
                        },
                    snippetHash =
                        when {
                            item.snippet.isBlank() -> null
                            translatedSnippet.isNotBlank() -> snippetHash
                            else -> cache.snippetHash
                        },
                    translatedSnippet =
                        when {
                            item.snippet.isBlank() -> null
                            translatedSnippet.isNotBlank() -> translatedSnippet
                            else -> cache.translatedSnippet
                        },
                )

            if (updatedCache != cache) {
                saveCache(
                    item.id,
                    settings,
                    targetLanguage,
                    updatedCache,
                )
            }

            item.copy(
                title = translatedTitle.ifBlank { item.title },
                snippet = translatedSnippet.ifBlank { item.snippet },
            )
        }

    suspend fun getOrTranslateArticle(
        itemId: Long,
        title: String,
        html: String,
        isFullText: Boolean,
    ): ArticleTranslation? =
        withContext(Dispatchers.IO) {
            val settings = translationSettings()
            val targetLanguage = repository.preferredTranslationLanguage.value.trim()
            if (!settings.canTranslate || targetLanguage.isBlank() || html.isBlank()) {
                return@withContext null
            }

            val cache = loadCache(itemId, settings, targetLanguage)
            val titleHash = sha256(title)
            val htmlHash = sha256(html)

            val cachedHtml =
                if (isFullText) {
                    cache.translatedFullArticleHtml.takeIf { cache.fullArticleHtmlHash == htmlHash }
                } else {
                    cache.translatedArticleHtml.takeIf { cache.articleHtmlHash == htmlHash }
                }
            val cachedTitle =
                cache.translatedTitle.takeIf {
                    cache.titleHash == titleHash &&
                        !cache.translatedTitle.isNullOrBlank()
                }

            if (cachedHtml != null && cachedTitle != null) {
                return@withContext ArticleTranslation(
                    translatedTitle = cachedTitle,
                    translatedHtml = cachedHtml,
                    sourceLanguage = cache.sourceLanguage,
                )
            }

            val detectedSameLanguage =
                detectArticleAlreadyInTargetLanguage(
                    itemId = itemId,
                    title = title,
                    html = html,
                    isFullText = isFullText,
                    settings = settings,
                    targetLanguage = targetLanguage,
                    existingCache = cache,
                )
            if (detectedSameLanguage != null) {
                return@withContext ArticleTranslation(
                    translatedTitle = title,
                    translatedHtml = html,
                    sourceLanguage = detectedSameLanguage,
                )
            }

            val translatedTitleResult =
                if (cachedTitle != null) {
                    null
                } else {
                    translateOrThrow(
                        content = title,
                        targetLanguage = targetLanguage,
                        settings = settings,
                    )
                }
            val translatedHtmlResult =
                if (cachedHtml != null) {
                    null
                } else {
                    translateOrThrow(
                        content = html,
                        targetLanguage = targetLanguage,
                        settings = settings,
                        preserveHtml = true,
                    )
                }
            val translatedTitle = cachedTitle ?: translatedTitleResult?.content.orEmpty().ifBlank { title }
            val translatedHtml = cachedHtml ?: translatedHtmlResult?.content.orEmpty().ifBlank { html }
            val cachedSourceLanguage =
                if (cachedTitle != null || cachedHtml != null) {
                    cache.sourceLanguage
                } else {
                    ""
                }
            val sourceLanguage =
                translatedHtmlResult?.detectedLanguageOrBlank().orEmpty()
                    .ifBlank { translatedTitleResult?.detectedLanguageOrBlank().orEmpty() }
                    .ifBlank { cachedSourceLanguage }

            val updatedCache =
                cache.copy(
                    sourceLanguage = sourceLanguage.ifBlank { cachedSourceLanguage },
                    titleHash = titleHash,
                    translatedTitle = translatedTitle,
                    articleHtmlHash = if (!isFullText) htmlHash else cache.articleHtmlHash,
                    translatedArticleHtml = if (!isFullText) translatedHtml else cache.translatedArticleHtml,
                    fullArticleHtmlHash = if (isFullText) htmlHash else cache.fullArticleHtmlHash,
                    translatedFullArticleHtml = if (isFullText) translatedHtml else cache.translatedFullArticleHtml,
                )

            saveCache(itemId, settings, targetLanguage, updatedCache)

            ArticleTranslation(
                translatedTitle = translatedTitle,
                translatedHtml = translatedHtml,
                sourceLanguage = sourceLanguage,
            )
        }

    suspend fun detectArticleAlreadyInTargetLanguage(
        itemId: Long,
        title: String,
        html: String,
        isFullText: Boolean,
    ): String? =
        withContext(Dispatchers.IO) {
            val settings = translationSettings()
            val targetLanguage = repository.preferredTranslationLanguage.value.trim()
            detectArticleAlreadyInTargetLanguage(
                itemId = itemId,
                title = title,
                html = html,
                isFullText = isFullText,
                settings = settings,
                targetLanguage = targetLanguage,
                existingCache = null,
            )
        }

    private fun loadCache(
        itemId: Long,
        settings: OpenAISettings,
        targetLanguage: String,
    ): CachedTranslations =
        cacheFile(itemId, settings, targetLanguage)
            .takeIf(File::isFile)
            ?.readText()
            ?.let { runCatching { json.decodeFromString<CachedTranslations>(it) }.getOrNull() }
            ?: CachedTranslations()

    private fun saveCache(
        itemId: Long,
        settings: OpenAISettings,
        targetLanguage: String,
        cache: CachedTranslations,
    ) {
        val file = cacheFile(itemId, settings, targetLanguage)
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(CachedTranslations.serializer(), cache))
    }

    private fun cacheFile(
        itemId: Long,
        settings: OpenAISettings,
        targetLanguage: String,
    ): File {
        val provider =
            when {
                settings.isDeepL -> "deepl"
                settings.isGoogleTranslate -> "google"
                else -> "openai"
            }
        return filePathProvider.cacheDir
            .resolve("translations")
            .resolve(
                "$itemId.$provider.${
                    targetLanguage
                        .trim()
                        .lowercase()
                        .replace(Regex("[^a-z0-9._-]+"), "_")
                        .ifBlank { "default" }
                }.json",
            )
    }

    private fun sha256(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private fun translationSettings(): OpenAISettings =
        repository.translationOpenAISettings.value.takeIf { it.canUseAsTranslationApi } ?: OpenAISettings()

    private suspend fun translateOrNull(
        content: String,
        targetLanguage: String,
        settings: OpenAISettings,
        preserveHtml: Boolean = false,
    ): TranslationResult.Success? =
        when (
            val result =
                openAIApi.translate(
                    content = content,
                    targetLanguage = targetLanguage,
                    settings = settings,
                    preserveHtml = preserveHtml,
                )
        ) {
            is TranslationResult.Success -> result.takeIf { it.content.isNotBlank() }
            is TranslationResult.Error -> null
        }

    private suspend fun translateOrThrow(
        content: String,
        targetLanguage: String,
        settings: OpenAISettings,
        preserveHtml: Boolean = false,
    ): TranslationResult.Success =
        when (
            val result =
                openAIApi.translate(
                    content = content,
                    targetLanguage = targetLanguage,
                    settings = settings,
                    preserveHtml = preserveHtml,
                )
        ) {
            is TranslationResult.Success -> result.takeIf { it.content.isNotBlank() }
                ?: throw IllegalStateException("Translation failed")
            is TranslationResult.Error -> throw IllegalStateException(result.content.ifBlank { "Translation failed" })
        }

    private fun detectSourceLanguageIfAlreadyTargetLanguage(
        content: String,
        targetLanguage: String,
        settings: OpenAISettings,
        preserveHtml: Boolean = false,
    ): String? =
        runCatching {
            val detectionText = prepareTextForLanguageDetection(content, preserveHtml)
            if (!hasEnoughTextForLanguageDetection(detectionText)) {
                return@runCatching null
            }

            application
                .detectLocaleFromText(
                    text = detectionText,
                    minConfidence = 95.0f,
                ).firstOrNull()
                ?.locale
                ?.toLanguageTag()
                ?.takeIf {
                    detectedLanguageMatchesTranslationTarget(
                        detectedLanguage = it,
                        targetLanguage = targetLanguage,
                        settings = settings,
                    )
                }
        }.getOrNull()

    private fun detectArticleAlreadyInTargetLanguage(
        itemId: Long,
        title: String,
        html: String,
        isFullText: Boolean,
        settings: OpenAISettings,
        targetLanguage: String,
        existingCache: CachedTranslations?,
    ): String? {
        if (!settings.canTranslate || targetLanguage.isBlank() || html.isBlank()) {
            return null
        }

        val cache = existingCache ?: loadCache(itemId, settings, targetLanguage)
        val titleHash = sha256(title)
        val htmlHash = sha256(html)
        val currentHtmlHash =
            if (isFullText) {
                cache.fullArticleHtmlHash
            } else {
                cache.articleHtmlHash
            }
        val cachedSameLanguage =
            cache.titleHash == titleHash &&
                currentHtmlHash == htmlHash &&
                cache.sourceLanguage.isNotBlank() &&
                detectedLanguageMatchesTranslationTarget(
                    detectedLanguage = cache.sourceLanguage,
                    targetLanguage = targetLanguage,
                    settings = settings,
                )
        if (cachedSameLanguage) {
            return cache.sourceLanguage
        }

        val detectedSameLanguage =
            detectSourceLanguageIfAlreadyTargetLanguage(
                content =
                    buildString {
                        if (title.isNotBlank()) {
                            append(title)
                        }
                        if (html.isNotBlank()) {
                            if (isNotEmpty()) {
                                append("\n\n")
                            }
                            append(html)
                        }
                    },
                targetLanguage = targetLanguage,
                settings = settings,
                preserveHtml = true,
            ) ?: return null

        saveCache(
            itemId = itemId,
            settings = settings,
            targetLanguage = targetLanguage,
            cache =
                cache.copy(
                    sourceLanguage = detectedSameLanguage,
                    titleHash = if (title.isBlank()) null else titleHash,
                    translatedTitle = if (title.isBlank()) null else title,
                    articleHtmlHash = if (!isFullText) htmlHash else cache.articleHtmlHash,
                    translatedArticleHtml = if (!isFullText) html else cache.translatedArticleHtml,
                    fullArticleHtmlHash = if (isFullText) htmlHash else cache.fullArticleHtmlHash,
                    translatedFullArticleHtml = if (isFullText) html else cache.translatedFullArticleHtml,
                ),
        )
        return detectedSameLanguage
    }
}

data class ArticleTranslation(
    val translatedTitle: String,
    val translatedHtml: String,
    val sourceLanguage: String,
)

@Serializable
private data class CachedTranslations(
    val sourceLanguage: String = "",
    val titleHash: String? = null,
    val translatedTitle: String? = null,
    val snippetHash: String? = null,
    val translatedSnippet: String? = null,
    val articleHtmlHash: String? = null,
    val translatedArticleHtml: String? = null,
    val fullArticleHtmlHash: String? = null,
    val translatedFullArticleHtml: String? = null,
)

private fun OpenAIApi.TranslationResult.detectedLanguageOrBlank(): String =
    when (this) {
        is OpenAIApi.TranslationResult.Success -> detectedLanguage
        is OpenAIApi.TranslationResult.Error -> ""
    }

data class CachedFeedListItemTranslation(
    val item: FeedListItem,
    val hasCachedTranslation: Boolean,
    val isFullyCached: Boolean,
)
