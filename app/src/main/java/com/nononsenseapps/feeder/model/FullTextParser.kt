package com.nononsenseapps.feeder.model

import android.util.Log
import com.ibm.icu.text.CharsetDetector
import com.ibm.icu.text.CharsetMatch
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.blob.blobFullOutputStream
import com.nononsenseapps.feeder.db.room.FeedItemForFetching
import com.nononsenseapps.feeder.db.room.estimateWordCount
import com.nononsenseapps.feeder.ui.text.HtmlToPlainTextConverter
import com.nononsenseapps.feeder.util.Either
import com.nononsenseapps.feeder.util.FilePathProvider
import com.nononsenseapps.feeder.util.flatten
import com.nononsenseapps.feeder.util.left
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import net.dankito.readability4j.extended.Readability4JExtended
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.net.URL
import java.nio.charset.Charset

class FullTextParser(
    override val di: DI,
) : DIAware {
    private val repository: Repository by instance()
    private val okHttpClient: OkHttpClient by instance()
    private val filePathProvider: FilePathProvider by instance()

    suspend fun parseFullArticlesForMissing(): Boolean {
        logDebug(LOG_TAG, "Parsing full texts for missing")
        val itemsToSync: List<FeedItemForFetching> =
            repository
                .getFeedsItemsWithDefaultFullTextNeedingDownload()
                .firstOrNull()
                ?: emptyList()

        return itemsToSync
            .map { feedItem ->
                parseFullArticleIfMissing(
                    feedItem = feedItem,
                ).isRight()
            }.fold(true) { acc, value ->
                acc && value
            }
    }

    suspend fun parseFullArticleIfMissing(feedItem: FeedItemForFetching): Either<FeedParserError, Unit> {
        val fullArticleFile =
            blobFullFile(itemId = feedItem.id, filesDir = filePathProvider.fullArticleDir)

        return try {
            if (fullArticleFile.isFile) {
                Either.Right(Unit)
            } else {
                parseFullArticle(feedItem = feedItem)
            }
        } catch (t: Throwable) {
            FullTextDecodingFailure(feedItem.link ?: "", t).left()
        } finally {
            repository.markAsFullTextDownloaded(feedItem.id)
        }.onLeft {
            Log.w(LOG_TAG, "Failed to parse missing full article: $it", it.throwable)
        }
    }

    private suspend fun parseFullArticle(feedItem: FeedItemForFetching): Either<FeedParserError, Unit> =
        withContext(Dispatchers.Default) {
            logDebug(LOG_TAG, "Fetching full page ${feedItem.link}, ${feedItem.id}")
            val url = feedItem.link ?: return@withContext Either.Left(NoUrl())

            okHttpClient
                .curlAndOnResponse(URL(url)) { response ->
                    Either.catching(
                        onCatch = { t ->
                            FullTextDecodingFailure(url, t)
                        },
                    ) {
                        val body = response.body ?: return@catching NoBody(url = url).left()

                        val bytes =
                            body.use {
                                it.bytes()
                            }

                        val contentType =
                            body.contentType()
                                ?: return@catching UnsupportedContentType(
                                    url = url,
                                    mimeType = "null",
                                ).left()

                        if (contentType.type != "text" || contentType.subtype != "html") {
                            return@catching UnsupportedContentType(
                                url = url,
                                mimeType = contentType.toString(),
                            ).left()
                        }

                        var charset: Charset? = contentType.charset()

                        if (charset == null) {
                            charset = findMetaCharset(bytes)?.let { toJavaCharset(it) }
                            logDebug(LOG_TAG, "No charset in content type, meta charset: $charset")
                        }

                        if (charset == null) {
                            charset = detectCharset(bytes)?.let { toJavaCharset(it) }
                            logDebug(LOG_TAG, "No charset in content type, guessing charset: $charset")
                        }

                        logDebug(LOG_TAG, "Full article charset: $charset")

                        val html = String(bytes, charset ?: java.nio.charset.StandardCharsets.UTF_8)
                        logDebug(LOG_TAG, "Parsing article ${feedItem.link}")
                        val article = parseFullArticle(url, html)
                        logDebug(LOG_TAG, "Writing article ${feedItem.link}")
                        withContext(Dispatchers.IO) {
                            article?.let { articleContent ->
                                filePathProvider.fullArticleDir.mkdirs()
                                blobFullOutputStream(feedItem.id, filePathProvider.fullArticleDir)
                                    .bufferedWriter()
                                    .use { writer ->
                                        writer.write(articleContent)
                                    }

                                // Update word count on item
                                val converter = HtmlToPlainTextConverter()
                                val plainText = converter.convert(articleContent)
                                val wordCount = estimateWordCount(plainText)

                                repository.updateWordCountFull(feedItem.id, wordCount)
                            }
                        }

                        Either.Right(Unit)
                    }
                }.flatten()
        }

    fun parseFullArticle(
        uri: String,
        html: String,
    ): String? {
        val article = Readability4JExtended(uri, html).parse()

        val dir = article.dir

        // Ensure dir is set on the outermost element
        return article.contentWithUtf8Encoding?.let { fullHtml ->
            if (dir?.isNotBlank() == true) {
                fullHtml.replaceFirst("<html".toRegex(), "<html dir=\"$dir\"")
            } else {
                fullHtml
            }
        }
    }

    companion object {
        internal const val LOG_TAG = "FEEDER_FULLTEXT"
    }
}

/**
 * For sites which don't use UTF-8 like http://www.muhasebetr.com/rss/
 */
fun findMetaCharset(html: ByteArray): String? {
    var state = HtmlParserState.INIT
    var tagName = StringBuilder()
    var attrName = StringBuilder()
    val attrValue = StringBuilder()

    html.forEach { b ->
        val char = b.toInt().toChar()
        state = nextHtmlParseState(char, state)

        when (state) {
            HtmlParserState.TAG_NAME -> tagName.append(char)
            HtmlParserState.TAG_ATTR_NAME -> attrName.append(char)
            HtmlParserState.TAG_ATTR_VALUE -> attrValue.append(char)
            HtmlParserState.TAG_START -> {
                tagName.clear()
                attrName.clear()
                attrValue.clear()
            }

            HtmlParserState.TAG_NAME_POST -> {
                // This is the state after a attribute has been parsed (but also before)

                if (tagName.toString().equals("meta", ignoreCase = true)) {
                    val name = attrName.toString()
                    val value = attrValue.toString()
                    when {
                        name.equals("content", ignoreCase = true) && value.contains("charset=") -> {
                            return attrValue.toString().substringAfter("charset=").substringBefore(';')
                        }

                        name.equals("charset", ignoreCase = true) -> {
                            return value
                        }
                    }
                }

                attrName.clear()
                attrValue.clear()
            }

            else -> {
                // Nothing to do
            }
        }
    }
    return null
}

private fun nextHtmlParseState(
    char: Char,
    currentState: HtmlParserState,
): HtmlParserState =
    when (currentState) {
        HtmlParserState.INIT ->
            when (char) {
                '<' -> HtmlParserState.TAG_START
                else -> HtmlParserState.INIT
            }

        HtmlParserState.TAG_START ->
            when (char) {
                ' ' -> HtmlParserState.TAG_START
                '/' -> HtmlParserState.TAG_START
                '>' -> HtmlParserState.INIT
                else -> HtmlParserState.TAG_NAME
            }

        HtmlParserState.TAG_NAME ->
            when (char) {
                ' ' -> HtmlParserState.TAG_NAME_POST
                '/' -> HtmlParserState.TAG_START
                '>' -> HtmlParserState.INIT
                else -> HtmlParserState.TAG_NAME
            }

        HtmlParserState.TAG_NAME_POST ->
            when (char) {
                ' ' -> HtmlParserState.TAG_NAME_POST
                '/' -> HtmlParserState.TAG_START
                '>' -> HtmlParserState.INIT
                else -> HtmlParserState.TAG_ATTR_NAME
            }

        HtmlParserState.TAG_ATTR_NAME ->
            when (char) {
                ' ' -> HtmlParserState.TAG_NAME_POST
                '/' -> HtmlParserState.TAG_START
                '>' -> HtmlParserState.INIT
                '=' -> HtmlParserState.TAG_ATTR_EQUALS
                else -> HtmlParserState.TAG_ATTR_NAME
            }

        HtmlParserState.TAG_ATTR_EQUALS ->
            when (char) {
                ' ' -> HtmlParserState.TAG_ATTR_EQUALS
                '/' -> HtmlParserState.TAG_START
                '>' -> HtmlParserState.INIT
                '"' -> HtmlParserState.TAG_ATTR_QUOTE_START
                else -> HtmlParserState.TAG_START
            }

        HtmlParserState.TAG_ATTR_QUOTE_START ->
            when (char) {
                '"' -> HtmlParserState.TAG_NAME_POST
                else -> HtmlParserState.TAG_ATTR_VALUE
            }

        HtmlParserState.TAG_ATTR_VALUE ->
            when (char) {
                '"' -> HtmlParserState.TAG_NAME_POST
                else -> HtmlParserState.TAG_ATTR_VALUE
            }
    }

enum class HtmlParserState {
    INIT,
    TAG_START,
    TAG_NAME,
    TAG_NAME_POST,
    TAG_ATTR_NAME,
    TAG_ATTR_EQUALS,
    TAG_ATTR_QUOTE_START,
    TAG_ATTR_VALUE,
}

/**
 * For sites which don't use UTF-8 like http://www.muhasebetr.com/rss/
 */
fun detectCharset(html: ByteArray): String? {
    val detector = CharsetDetector()
    detector.setText(html)
    val matches: Array<CharsetMatch> = detector.detectAll()

    return matches.firstOrNull { it.confidence > 50 }?.name
}

private fun toJavaCharset(icuName: String): Charset {
    // Handle ICU's charset name variations
    return when (icuName.uppercase()) {
        "GB2312" -> Charset.forName("GBK") // Android maps GB2312 to GBK
        "BIG5-HKSCS" -> Charset.forName("Big5_HKSCS")
        "Shift-JIS" -> Charset.forName("Shift_JIS")
        else -> Charset.forName(icuName)
    }
}
