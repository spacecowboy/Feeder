package com.nononsenseapps.feeder.model

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
    private val repository: Repository by instance()
    private val openAIApi: OpenAIApi by instance()
    private val filePathProvider: FilePathProvider by instance()
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

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

            val translatedTitle =
                if (item.title.isBlank()) {
                    ""
                } else if (cache.titleHash == titleHash && !cache.translatedTitle.isNullOrBlank()) {
                    cache.translatedTitle
                } else {
                    translateOrNull(
                        content = item.title,
                        targetLanguage = targetLanguage,
                        settings = settings,
                    )?.content.orEmpty()
                }

            val translatedSnippet =
                if (item.snippet.isBlank()) {
                    ""
                } else if (cache.snippetHash == snippetHash && !cache.translatedSnippet.isNullOrBlank()) {
                    cache.translatedSnippet
                } else {
                    translateOrNull(
                        content = item.snippet,
                        targetLanguage = targetLanguage,
                        settings = settings,
                    )?.content.orEmpty()
                }

            val updatedCache =
                cache.copy(
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

            val translatedTitleResult =
                translateOrThrow(
                    content = title,
                    targetLanguage = targetLanguage,
                    settings = settings,
                )
            val translatedHtmlResult =
                translateOrThrow(
                    content = html,
                    targetLanguage = targetLanguage,
                    settings = settings,
                    preserveHtml = true,
                )
            val sourceLanguage =
                translatedHtmlResult.detectedLanguageOrBlank()
                    .ifBlank { translatedTitleResult.detectedLanguageOrBlank() }

            val updatedCache =
                cache.copy(
                    sourceLanguage = sourceLanguage,
                    titleHash = titleHash,
                    translatedTitle = translatedTitleResult.content.ifBlank { title },
                    articleHtmlHash = if (!isFullText) htmlHash else cache.articleHtmlHash,
                    translatedArticleHtml = if (!isFullText) translatedHtmlResult.content.ifBlank { html } else cache.translatedArticleHtml,
                    fullArticleHtmlHash = if (isFullText) htmlHash else cache.fullArticleHtmlHash,
                    translatedFullArticleHtml = if (isFullText) translatedHtmlResult.content.ifBlank { html } else cache.translatedFullArticleHtml,
                )

            saveCache(itemId, settings, targetLanguage, updatedCache)

            ArticleTranslation(
                translatedTitle = updatedCache.translatedTitle ?: title,
                translatedHtml =
                    if (isFullText) {
                        updatedCache.translatedFullArticleHtml ?: html
                    } else {
                        updatedCache.translatedArticleHtml ?: html
                    },
                sourceLanguage = sourceLanguage,
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
