package com.nononsenseapps.feeder.ui.compose.feedarticle

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.Article
import com.nononsenseapps.feeder.archmodel.Enclosure
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.archmodel.TextToDisplay
import com.nononsenseapps.feeder.archmodel.TranslationApiSettings
import com.nononsenseapps.feeder.background.runOnceRssSync
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.blob.blobFile
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.blob.blobFullInputStream
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.db.room.FeedItemForFetching
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.ArticleTranslation
import com.nononsenseapps.feeder.model.FeedParserError
import com.nononsenseapps.feeder.model.FullTextParser
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.model.NoBody
import com.nononsenseapps.feeder.model.NoUrl
import com.nononsenseapps.feeder.model.NotHTML
import com.nononsenseapps.feeder.model.PlaybackStatus
import com.nononsenseapps.feeder.model.TTSStateHolder
import com.nononsenseapps.feeder.model.ThumbnailImage
import com.nononsenseapps.feeder.model.TranslationManager
import com.nononsenseapps.feeder.model.UnsupportedContentType
import com.nononsenseapps.feeder.model.html.HtmlLinearizer
import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.openai.OpenAIApi
import com.nononsenseapps.feeder.openai.canSummarize
import com.nononsenseapps.feeder.openai.canUseAsTranslationApi
import com.nononsenseapps.feeder.ui.compose.text.htmlToAnnotatedString
import com.nononsenseapps.feeder.ui.text.MarkdownToHtmlConverter
import com.nononsenseapps.feeder.util.Either
import com.nononsenseapps.feeder.util.FilePathProvider
import com.nononsenseapps.feeder.util.ToastMaker
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.FileNotFoundException
import java.time.ZonedDateTime
import java.util.Locale

class ArticleViewModel(
    di: DI,
    private val state: SavedStateHandle,
) : DIAwareViewModel(di) {
    private val repository: Repository by instance()
    private val ttsStateHolder: TTSStateHolder by instance()
    private val fullTextParser: FullTextParser by instance()
    private val filePathProvider: FilePathProvider by instance()
    private val openAIApi: OpenAIApi by instance()
    private val toastMaker: ToastMaker by instance()
    private val translationManager: TranslationManager by instance()

    // Use this for actions which should complete even if app goes off screen
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()

    val itemId: Long =
        state["itemId"]
            ?: throw IllegalArgumentException("Missing itemId in savedState")

    private val articleFlow =
        repository
            .getArticleFlow(itemId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    private val textToDisplay = MutableStateFlow(TextToDisplay.CONTENT)
    private val displayFullTextOverride = MutableStateFlow<Boolean?>(null)

    private val articleContentFlow: StateFlow<LinearArticle> =
        combine(
            articleFlow,
            displayFullTextOverride,
        ) { article, fullTextOverride ->
            article?.let {
                it to (fullTextOverride ?: it.fullTextByDefault)
            }
        }.filterNotNull()
            .map { (article, displayFullText) ->
                parseArticleContent(article, displayFullText)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = LinearArticle(emptyList()),
            )

    private val toolbarVisible = MutableStateFlow(state["toolbarMenuVisible"] ?: false)
    private val openAiSummary = MutableStateFlow<OpenAISummaryState>(OpenAISummaryState.Empty)
    private val showTranslatedContent = MutableStateFlow(false)
    private val translatedArticleContent = MutableStateFlow(LinearArticle(emptyList()))
    private val articleTranslationState = MutableStateFlow<ArticleTranslationState>(ArticleTranslationState.Empty)
    private var handledInitialOpenActions = false

    private val isFullText: Boolean
        get() = displayFullTextOverride.value ?: articleFlow.value?.fullTextByDefault ?: false

    val viewState: StateFlow<ArticleScreenViewState> =
        combine(
            articleFlow,
            textToDisplay,
            articleContentFlow,
            translatedArticleContent,
            toolbarVisible,
            repository.linkOpener,
            repository.useDetectLanguage,
            ttsStateHolder.ttsState,
            ttsStateHolder.availableLanguages,
            repository.openAISettings,
            repository.translationApiSettings,
            repository.preferredTranslationLanguage,
            openAiSummary,
            showTranslatedContent,
            articleTranslationState,
        ) { params ->
            val article = params[0] as Article?
            val textToDisplay = params[1] as TextToDisplay
            val articleContent = params[2] as LinearArticle
            val translatedArticleContent = params[3] as LinearArticle
            val toolbarVisible = params[4] as Boolean
            val linkOpener = params[5] as LinkOpener
            val useDetectLanguage = params[6] as Boolean
            val ttsState = params[7] as PlaybackStatus

            @Suppress("UNCHECKED_CAST")
            val ttsLanguages = params[8] as List<Locale>

            val summarySettings = params[9] as OpenAISettings
            val translationSettings = params[10] as TranslationApiSettings
            val preferredTranslationLanguage = (params[11] as String).trim()
            val openAiSummary = params[12] as OpenAISummaryState
            val showTranslated = params[13] as Boolean
            val translationState = params[14] as ArticleTranslationState
            val currentTranslation =
                (translationState as? ArticleTranslationState.Result)
                    ?.takeIf { it.isFullText == isFullText }
            val alreadyInPreferredLanguage =
                (translationState as? ArticleTranslationState.AlreadyInPreferredLanguage)
                    ?.takeIf { it.isFullText == isFullText }
            val isShowingTranslated = showTranslated && currentTranslation != null
            val canRequestTranslation =
                translationSettings.canUseAsTranslationApi &&
                    preferredTranslationLanguage.isNotBlank() &&
                    !article?.link.isNullOrEmpty()

            ArticleState(
                useDetectLanguage = useDetectLanguage,
                isBottomBarVisible = ttsState != PlaybackStatus.STOPPED,
                isTTSPlaying = ttsState == PlaybackStatus.PLAYING,
                ttsLanguages = ttsLanguages,
                articleFeedUrl = article?.feedUrl,
                articleId = itemId,
                articleLink = article?.link,
                articleFeedId = article?.feedId ?: ID_UNSET,
                textToDisplay = textToDisplay,
                linkOpener = linkOpener,
                pubDate = article?.pubDate,
                author = article?.author,
                enclosure = article?.enclosure ?: Enclosure(),
                articleTitle = if (isShowingTranslated) currentTranslation.translatedTitle else article?.title ?: "",
                showToolbarMenu = toolbarVisible,
                feedDisplayTitle = article?.feedDisplayTitle ?: "",
                isBookmarked = article?.bookmarked == true,
                wordCount =
                    if (isFullText) {
                        article?.wordCountFull ?: 0
                    } else {
                        article?.wordCount ?: 0
                    },
                image = article?.image,
                showSummarize = summarySettings.canSummarize && !article?.link.isNullOrEmpty(),
                openAiSummary = openAiSummary,
                showTranslate = isShowingTranslated || (canRequestTranslation && alreadyInPreferredLanguage == null),
                isShowingTranslated = isShowingTranslated,
                isTranslationLoading = translationState is ArticleTranslationState.Loading,
                translationSourceLanguage = currentTranslation?.sourceLanguage ?: alreadyInPreferredLanguage?.sourceLanguage.orEmpty(),
                articleContent = if (isShowingTranslated) translatedArticleContent else articleContent,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ArticleState(),
        )

    init {
        viewModelScope.launch {
            articleFlow.collect { article ->
                val feedId = article?.item?.feedId ?: return@collect
                if (handledInitialOpenActions) {
                    return@collect
                }
                handledInitialOpenActions = true

                val feed = repository.getFeed(feedId)
                if (feed?.summarizeOnOpen == true && repository.openAISettings.value.canSummarize) {
                    summarize()
                }

                if (repository.translateArticlesByDefault.value && canTranslateArticles()) {
                    showTranslatedContent.value = true
                    translateCurrentArticle()
                }
            }
        }

        viewModelScope.launch {
            combine(
                articleFlow,
                displayFullTextOverride,
                repository.translationApiSettings,
                repository.preferredTranslationLanguage,
            ) { article, fullTextOverride, settings, targetLanguage ->
                val currentArticle = article ?: return@combine null
                val fullText = fullTextOverride ?: currentArticle.fullTextByDefault
                if (!settings.canUseAsTranslationApi || targetLanguage.trim().isBlank() || currentArticle.link.isNullOrBlank()) {
                    return@combine null
                }
                ArticleLanguageCheck(
                    article = currentArticle,
                    isFullText = fullText,
                )
            }.collectLatest { request ->
                if (request == null) {
                    clearAlreadyInPreferredLanguageState()
                    return@collectLatest
                }

                val html =
                    loadArticleHtmlForLanguageDetection(
                        article = request.article,
                        fullText = request.isFullText,
                    ) ?: run {
                        clearAlreadyInPreferredLanguageState(request.isFullText)
                        return@collectLatest
                    }

                val sourceLanguage =
                    detectArticleAlreadyInPreferredLanguage(
                        article = request.article,
                        fullText = request.isFullText,
                        html = html,
                    )

                if (sourceLanguage != null) {
                    setAlreadyInPreferredLanguage(
                        sourceLanguage = sourceLanguage,
                        isFullText = request.isFullText,
                    )
                } else {
                    clearAlreadyInPreferredLanguageState(request.isFullText)
                }
            }
        }
    }

    fun toggleFullText() {
        // Using as general loading text
        textToDisplay.update { TextToDisplay.LOADING_FULLTEXT }
        displayFullTextOverride.value = displayFullTextOverride.value?.not() ?: articleFlow.value?.fullTextByDefault?.not() ?: true
        if (showTranslatedContent.value) {
            translateCurrentArticle()
        }
    }

    private suspend fun parseArticleContent(
        article: Article,
        fullText: Boolean,
    ): LinearArticle {
        logDebug(LOG_TAG, "parseArticleContent(${article.id}, $fullText)")
        return try {
            withContext(Dispatchers.IO) {
                val htmlLinearizer = HtmlLinearizer()
                when (fullText) {
                    false -> {
                        if (blobFile(article.id, filePathProvider.articleDir).isFile) {
                            try {
                                blobInputStream(article.id, filePathProvider.articleDir)
                                    .use {
                                        htmlLinearizer.linearize(
                                            inputStream = it,
                                            baseUrl = article.feedUrl ?: "",
                                        )
                                    }.also {
                                        textToDisplay.update { TextToDisplay.CONTENT }
                                    }
                            } catch (e: Exception) {
                                // EOFException is possible
                                Log.e(LOG_TAG, "Could not open blob", e)
                                textToDisplay.update { TextToDisplay.FAILED_TO_LOAD_FULLTEXT }
                                LinearArticle(elements = emptyList())
                            }
                        } else {
                            Log.e(LOG_TAG, "No default file to parse. Attempting to fetch feed again")
                            // Should not happen but keeping this as a fallback
                            runOnceRssSync(
                                di = di,
                                feedId = article.feedId,
                                forceNetwork = true,
                                triggeredByUser = true,
                            )
                            textToDisplay.update { TextToDisplay.CONTENT }
                            htmlLinearizer.linearize(
                                "Sorry, due to a coding oversight, " +
                                    "the full offline version of this article has been permanently deleted, " +
                                    "see <a href=\"https://github.com/spacecowboy/Feeder/issues/443\">feeder#443</a> " +
                                    "for updates on this issue<br/><br/>" +
                                    article.snippet,
                                article.feedUrl ?: "",
                            )
                        }
                    }

                    true -> {
                        if (!blobFullFile(article.id, filePathProvider.fullArticleDir).isFile) {
                            // If the fulltext file is missing, try to fetch it
                            when (retrieveFullText(article.id).leftOrNull()) {
                                null -> {
                                    // Success. Do nothing yet
                                    null
                                }
                                is NoBody -> TextToDisplay.FAILED_MISSING_BODY
                                is NoUrl -> TextToDisplay.FAILED_MISSING_LINK
                                is UnsupportedContentType -> TextToDisplay.FAILED_NOT_HTML
                                is NotHTML -> TextToDisplay.FAILED_NOT_HTML
                                else -> TextToDisplay.FAILED_TO_LOAD_FULLTEXT
                            }?.let { errorText ->
                                textToDisplay.update { errorText }
                            }
                        }
                        if (blobFullFile(article.id, filePathProvider.fullArticleDir).isFile) {
                            try {
                                blobFullInputStream(article.id, filePathProvider.fullArticleDir)
                                    .use {
                                        htmlLinearizer.linearize(
                                            inputStream = it,
                                            baseUrl = article.feedUrl ?: "",
                                        )
                                    }.also {
                                        textToDisplay.update { TextToDisplay.CONTENT }
                                    }
                            } catch (e: Exception) {
                                // EOFException is possible
                                Log.e(LOG_TAG, "Could not open blob", e)
                                textToDisplay.update { TextToDisplay.FAILED_TO_LOAD_FULLTEXT }
                                LinearArticle(elements = emptyList())
                            }
                        } else {
                            // Error text should already be set above
                            LinearArticle(elements = emptyList())
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e(LOG_TAG, "Error parsing article", t)
            LinearArticle(elements = emptyList())
        }
    }

    private suspend fun retrieveFullText(itemId: Long): Either<FeedParserError, Unit> =
        withContext(Dispatchers.IO) {
            logDebug(LOG_TAG, "loadFullTextThenDisplayIt($itemId)")
            if (blobFullFile(itemId, filePathProvider.fullArticleDir).isFile) {
                logDebug(LOG_TAG, "Fulltext file exists")
                return@withContext Either.Right(Unit)
            }

            logDebug(LOG_TAG, "Fulltext file does not exist")
            val link = repository.getLink(itemId)
            return@withContext fullTextParser.parseFullArticleIfMissing(
                object : FeedItemForFetching {
                    override val id = itemId
                    override val link = link
                },
            )
        }

    fun markAsUnread() {
        applicationCoroutineScope.launch {
            repository.markAsUnread(itemId)
        }
    }

    fun setBookmarked(bookmarked: Boolean) =
        applicationCoroutineScope.launch {
            repository.setBookmarked(itemId, bookmarked)
        }

    fun setToolbarMenuVisible(visible: Boolean) {
        state["toolbarMenuVisible"] = visible
        toolbarVisible.update { visible }
    }

    fun ttsPlay() {
        viewModelScope.launch(Dispatchers.IO) {
            val feedItem = repository.getCurrentArticle() ?: return@launch
            val article = Article(feedItem)
            val readFullText = displayFullTextOverride.value ?: feedItem.fullTextByDefault
            val textToRead =
                if (showTranslatedContent.value) {
                    Either.catching<TSSError, List<AnnotatedString>>(
                        onCatch = {
                            when (it) {
                                is FileNotFoundException -> TTSFileNotFound
                                else -> TTSUnknownError
                            }
                        },
                    ) {
                        htmlToAnnotatedString(
                            inputStream = loadTranslatedArticle(article, readFullText).translatedHtml.byteInputStream(),
                            baseUrl = feedItem.feedUrl.toString(),
                        )
                    }
                } else {
                    when (readFullText) {
                        false ->
                            Either.catching<TSSError, List<AnnotatedString>>(
                                onCatch = {
                                    when (it) {
                                        is FileNotFoundException -> TTSFileNotFound
                                        else -> TTSUnknownError
                                    }
                                },
                            ) {
                                blobInputStream(article.id, filePathProvider.articleDir).use {
                                    htmlToAnnotatedString(
                                        inputStream = it,
                                        baseUrl = feedItem.feedUrl.toString(),
                                    )
                                }
                            }

                        true ->
                            Either.catching<TSSError, List<AnnotatedString>>(
                                onCatch = {
                                    when (it) {
                                        is FileNotFoundException -> TTSFileNotFound
                                        else -> TTSUnknownError
                                    }
                                },
                            ) {
                                blobFullInputStream(article.id, filePathProvider.fullArticleDir).use {
                                    htmlToAnnotatedString(
                                        inputStream = it,
                                        baseUrl = feedItem.feedUrl.toString(),
                                    )
                                }
                            }
                    }
                }

            // TODO show error some message
            textToRead.onRight {
                ttsStateHolder.tts(
                    textArray = it,
                    useDetectLanguage = repository.useDetectLanguage.value,
                )
            }
        }
    }

    fun ttsPause() {
        ttsStateHolder.pause()
    }

    fun ttsStop() {
        ttsStateHolder.stop()
    }

    fun ttsSkipNext() {
        ttsStateHolder.skipNext()
    }

    fun ttsOnSelectLanguage(lang: LocaleOverride) {
        ttsStateHolder.setLanguage(lang)
    }

    fun summarize() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                openAiSummary.value = OpenAISummaryState.Loading
                val content = loadArticleContent()
                val summaryResult = openAIApi.summarize(content, repository.openAISettings.value)
                val annotatedStrings = convertSummaryToAnnotatedStrings(summaryResult)
                openAiSummary.value =
                    OpenAISummaryState.Result(
                        value = summaryResult,
                        annotatedStrings = annotatedStrings,
                    )
            } catch (e: Exception) {
                val errorResult = OpenAIApi.SummaryResult.Error(content = e.message ?: "Unknown error")
                val annotatedStrings = convertSummaryToAnnotatedStrings(errorResult)
                openAiSummary.value =
                    OpenAISummaryState.Result(
                        value = errorResult,
                        annotatedStrings = annotatedStrings,
                    )
            }
        }
    }

    fun translate() {
        if (showTranslatedContent.value) {
            showTranslatedContent.value = false
            return
        }
        showTranslatedContent.value = true
        translateCurrentArticle()
    }

    private fun translateCurrentArticle() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val targetLanguage = repository.preferredTranslationLanguage.value.trim()
                if (targetLanguage.isBlank()) {
                    toastMaker.makeToast(R.string.set_translation_language_first)
                    clearTranslatedContent()
                    return@launch
                }

                val article = articleFlow.value ?: return@launch
                val fullText = isFullText
                if (!repository.translationApiSettings.value.canUseAsTranslationApi) {
                    clearTranslatedContent()
                    return@launch
                }

                val html = loadArticleHtml(article, fullText)
                val sourceLanguage =
                    detectArticleAlreadyInPreferredLanguage(
                        article = article,
                        fullText = fullText,
                        html = html,
                    )
                if (sourceLanguage != null) {
                    setAlreadyInPreferredLanguage(
                        sourceLanguage = sourceLanguage,
                        isFullText = fullText,
                    )
                    return@launch
                }

                articleTranslationState.value = ArticleTranslationState.Loading
                val translation =
                    translationManager.getOrTranslateArticle(
                        itemId = article.id,
                        title = article.title,
                        html = html,
                        isFullText = fullText,
                    ) ?: throw IllegalStateException("Translation failed")

                translatedArticleContent.value =
                    HtmlLinearizer().linearize(
                        translation.translatedHtml,
                        article.feedUrl ?: "",
                    )
                articleTranslationState.value =
                    ArticleTranslationState.Result(
                        translatedTitle = translation.translatedTitle,
                        sourceLanguage = translation.sourceLanguage,
                        isFullText = fullText,
                    )
            } catch (e: Exception) {
                clearTranslatedContent()
                toastMaker.makeToast(e.message ?: "Translation failed")
            }
        }
    }

    private suspend fun loadTranslatedArticle(
        article: Article,
        fullText: Boolean,
    ): ArticleTranslation =
        translationManager.getOrTranslateArticle(
            itemId = article.id,
            title = article.title,
            html = loadArticleHtml(article, fullText),
            isFullText = fullText,
        ) ?: throw IllegalStateException("Translation failed")

    private suspend fun convertSummaryToAnnotatedStrings(summaryResult: OpenAIApi.SummaryResult): List<AnnotatedString> =
        withContext(Dispatchers.Default) {
            return@withContext when (summaryResult) {
                is OpenAIApi.SummaryResult.Success -> {
                    val markdownConverter = MarkdownToHtmlConverter()
                    val htmlContent = markdownConverter.convertToHtml(summaryResult.content)
                    htmlToAnnotatedString(htmlContent.byteInputStream(), "")
                }
                is OpenAIApi.SummaryResult.Error -> {
                    // For error messages, create a simple AnnotatedString directly
                    // without going through markdown/HTML conversion
                    listOf(AnnotatedString(summaryResult.content))
                }
            }
        }

    private suspend fun loadArticleContent(): String {
        val viewState = viewState.value
        val blobFile = blobFullFile(viewState.articleId, filePathProvider.fullArticleDir)
        val contentStream =
            if (blobFile.isFile) {
                blobFullInputStream(viewState.articleId, filePathProvider.fullArticleDir)
            } else {
                fullTextParser
                    .parseFullArticleIfMissing(
                        object : FeedItemForFetching {
                            override val id = viewState.articleId
                            override val link = viewState.articleLink
                        },
                    ).let {
                        val error = it.leftOrNull()
                        if (error == null) {
                            blobFullInputStream(viewState.articleId, filePathProvider.fullArticleDir)
                        } else {
                            throw IllegalStateException("Cannot load article: ${error.description}", error.throwable)
                        }
                    }
            }

        return Jsoup.parse(contentStream, null, viewState.articleFeedUrl ?: "").body().text()
    }

    private suspend fun detectArticleAlreadyInPreferredLanguage(
        article: Article,
        fullText: Boolean,
        html: String,
    ): String? =
        translationManager.detectArticleAlreadyInTargetLanguage(
            itemId = article.id,
            title = article.title,
            html = html,
            isFullText = fullText,
        )

    private suspend fun loadArticleHtml(
        article: Article,
        fullText: Boolean,
    ): String =
        withContext(Dispatchers.IO) {
            when (fullText) {
                false -> readArticleBlobIfPresent(article.id) ?: article.snippet

                true -> {
                    val cachedHtml = readFullArticleBlobIfPresent(article.id)
                    if (cachedHtml != null) {
                        return@withContext cachedHtml
                    }

                    val error = retrieveFullText(article.id).leftOrNull()
                    if (error != null) {
                        throw IllegalStateException("Cannot load article: ${error.description}", error.throwable)
                    }
                    readFullArticleBlobIfPresent(article.id)
                        ?: throw IllegalStateException("Cannot load article")
                }
            }
        }

    private suspend fun loadArticleHtmlForLanguageDetection(
        article: Article,
        fullText: Boolean,
    ): String? =
        withContext(Dispatchers.IO) {
            when (fullText) {
                false -> readArticleBlobIfPresent(article.id) ?: article.snippet.takeIf(String::isNotBlank)
                true -> readFullArticleBlobIfPresent(article.id)
            }
        }

    private fun readArticleBlobIfPresent(itemId: Long): String? =
        blobFile(itemId, filePathProvider.articleDir)
            .takeIf { it.isFile }
            ?.let {
                blobInputStream(itemId, filePathProvider.articleDir).bufferedReader().use { reader ->
                    reader.readText()
                }
            }

    private fun readFullArticleBlobIfPresent(itemId: Long): String? =
        blobFullFile(itemId, filePathProvider.fullArticleDir)
            .takeIf { it.isFile }
            ?.let {
                blobFullInputStream(itemId, filePathProvider.fullArticleDir).bufferedReader().use { reader ->
                    reader.readText()
                }
            }

    private fun canTranslateArticles(): Boolean =
        repository.translationApiSettings.value.canUseAsTranslationApi &&
            repository.preferredTranslationLanguage.value
                .trim()
                .isNotBlank()

    private fun setAlreadyInPreferredLanguage(
        sourceLanguage: String,
        isFullText: Boolean,
    ) {
        showTranslatedContent.value = false
        translatedArticleContent.value = LinearArticle(emptyList())
        articleTranslationState.value =
            ArticleTranslationState.AlreadyInPreferredLanguage(
                sourceLanguage = sourceLanguage,
                isFullText = isFullText,
            )
    }

    private fun clearAlreadyInPreferredLanguageState(isFullText: Boolean? = null) {
        val currentState = articleTranslationState.value as? ArticleTranslationState.AlreadyInPreferredLanguage ?: return
        if (isFullText == null || currentState.isFullText == isFullText) {
            articleTranslationState.value = ArticleTranslationState.Empty
        }
    }

    private fun clearTranslatedContent() {
        showTranslatedContent.value = false
        translatedArticleContent.value = LinearArticle(emptyList())
        articleTranslationState.value = ArticleTranslationState.Empty
    }

    companion object {
        private const val LOG_TAG = "FEEDER_ArticleVM"
    }
}

private data class ArticleState(
    override val useDetectLanguage: Boolean = false,
    override val isBottomBarVisible: Boolean = false,
    override val isTTSPlaying: Boolean = false,
    override val ttsLanguages: List<Locale> = emptyList(),
    override val articleFeedUrl: String? = null,
    override val articleId: Long = ID_UNSET,
    override val articleLink: String? = null,
    override val articleFeedId: Long = ID_UNSET,
    override val textToDisplay: TextToDisplay = TextToDisplay.CONTENT,
    override val linkOpener: LinkOpener = LinkOpener.CUSTOM_TAB,
    override val pubDate: ZonedDateTime? = null,
    override val author: String? = null,
    override val enclosure: Enclosure = Enclosure(),
    override val articleTitle: String = "",
    override val showToolbarMenu: Boolean = false,
    override val feedDisplayTitle: String = "",
    override val isBookmarked: Boolean = false,
    override val keyHolder: ArticleItemKeyHolder = RotatingArticleItemKeyHolder,
    override val wordCount: Int = 0,
    override val image: ThumbnailImage? = null,
    override val showSummarize: Boolean = false,
    override val openAiSummary: OpenAISummaryState = OpenAISummaryState.Empty,
    override val showTranslate: Boolean = false,
    override val isShowingTranslated: Boolean = false,
    override val isTranslationLoading: Boolean = false,
    override val translationSourceLanguage: String = "",
    override val articleContent: LinearArticle = LinearArticle(emptyList()),
) : ArticleScreenViewState

@Immutable
interface ArticleScreenViewState {
    val useDetectLanguage: Boolean
    val isBottomBarVisible: Boolean
    val isTTSPlaying: Boolean
    val ttsLanguages: List<Locale>
    val articleFeedUrl: String?
    val articleId: Long
    val articleLink: String?
    val articleFeedId: Long
    val textToDisplay: TextToDisplay
    val linkOpener: LinkOpener
    val pubDate: ZonedDateTime?
    val author: String?
    val enclosure: Enclosure
    val articleTitle: String
    val showToolbarMenu: Boolean
    val feedDisplayTitle: String
    val isBookmarked: Boolean
    val keyHolder: ArticleItemKeyHolder
    val wordCount: Int
    val image: ThumbnailImage?
    val showSummarize: Boolean
    val openAiSummary: OpenAISummaryState
    val showTranslate: Boolean
    val isShowingTranslated: Boolean
    val isTranslationLoading: Boolean
    val translationSourceLanguage: String
    val articleContent: LinearArticle
}

sealed interface OpenAISummaryState {
    data object Empty : OpenAISummaryState

    data object Loading : OpenAISummaryState

    data class Result(
        val value: OpenAIApi.SummaryResult,
        val annotatedStrings: List<AnnotatedString>,
    ) : OpenAISummaryState
}

sealed interface ArticleTranslationState {
    data object Empty : ArticleTranslationState

    data object Loading : ArticleTranslationState

    data class AlreadyInPreferredLanguage(
        val sourceLanguage: String,
        val isFullText: Boolean,
    ) : ArticleTranslationState

    data class Result(
        val translatedTitle: String,
        val sourceLanguage: String,
        val isFullText: Boolean,
    ) : ArticleTranslationState
}

private data class ArticleLanguageCheck(
    val article: Article,
    val isFullText: Boolean,
)

interface ArticleItemKeyHolder {
    fun getAndIncrementKey(): Any
}

object RotatingArticleItemKeyHolder : ArticleItemKeyHolder {
    private var key: Long = 0L

    override fun getAndIncrementKey(): Long = key++
}

sealed class TSSError

data object TTSFileNotFound : TSSError()

data object TTSUnknownError : TSSError()
