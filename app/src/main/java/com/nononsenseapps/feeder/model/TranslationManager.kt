package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.openai.OpenAIApi
import com.nononsenseapps.feeder.openai.canTranslate
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
            val settings = repository.openAISettings.value
            val targetLanguage = settings.preferredTranslationLanguage.trim()
            if (!settings.canTranslate || targetLanguage.isBlank()) {
                return@withContext item
            }

            val cache = loadCache(item.id, settings)
            val titleHash = sha256(item.title)
            val snippetHash = sha256(item.snippet)

            val translatedTitle =
                if (item.title.isBlank()) {
                    ""
                } else if (cache.titleHash == titleHash && !cache.translatedTitle.isNullOrBlank()) {
                    cache.translatedTitle
                } else {
                    openAIApi.translate(item.title, targetLanguage).content.ifBlank { item.title }
                }

            val translatedSnippet =
                if (item.snippet.isBlank()) {
                    ""
                } else if (cache.snippetHash == snippetHash && !cache.translatedSnippet.isNullOrBlank()) {
                    cache.translatedSnippet
                } else {
                    openAIApi.translate(item.snippet, targetLanguage).content.ifBlank { item.snippet }
                }

            saveCache(
                item.id,
                settings,
                cache.copy(
                    sourceLanguage = cache.sourceLanguage,
                    titleHash = titleHash,
                    translatedTitle = translatedTitle,
                    snippetHash = snippetHash,
                    translatedSnippet = translatedSnippet,
                ),
            )

            item.copy(
                title = translatedTitle,
                snippet = translatedSnippet,
            )
        }

    suspend fun getOrTranslateArticle(
        itemId: Long,
        title: String,
        html: String,
        isFullText: Boolean,
    ): ArticleTranslation? =
        withContext(Dispatchers.IO) {
            val settings = repository.openAISettings.value
            val targetLanguage = settings.preferredTranslationLanguage.trim()
            if (!settings.canTranslate || targetLanguage.isBlank() || html.isBlank()) {
                return@withContext null
            }

            val cache = loadCache(itemId, settings)
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

            val translatedTitleResult = openAIApi.translate(title, targetLanguage)
            val translatedHtmlResult = openAIApi.translate(html, targetLanguage, preserveHtml = true)
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

            saveCache(itemId, settings, updatedCache)

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
    ): CachedTranslations =
        cacheFile(itemId, settings)
            .takeIf(File::isFile)
            ?.readText()
            ?.let { runCatching { json.decodeFromString<CachedTranslations>(it) }.getOrNull() }
            ?: CachedTranslations()

    private fun saveCache(
        itemId: Long,
        settings: OpenAISettings,
        cache: CachedTranslations,
    ) {
        val file = cacheFile(itemId, settings)
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(CachedTranslations.serializer(), cache))
    }

    private fun cacheFile(
        itemId: Long,
        settings: OpenAISettings,
    ): File {
        val provider =
            when {
                settings.isDeepL -> "deepl"
                settings.isGoogleTranslate -> "google"
                else -> "openai"
            }
        val targetLanguage =
            settings.preferredTranslationLanguage
                .trim()
                .lowercase()
                .replace(Regex("[^a-z0-9._-]+"), "_")
                .ifBlank { "default" }
        return filePathProvider.cacheDir
            .resolve("translations")
            .resolve("$itemId.$provider.$targetLanguage.json")
    }

    private fun sha256(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
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
