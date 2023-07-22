package com.nononsenseapps.feeder.model

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.blob.blobFullOutputStream
import com.nononsenseapps.feeder.db.room.FeedItemForFetching
import com.nononsenseapps.feeder.model.FullTextParser.Companion.LOG_TAG
import com.nononsenseapps.feeder.util.FilePathProvider
import com.nononsenseapps.feeder.util.logDebug
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import net.dankito.readability4j.extended.Readability4JExtended
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

fun scheduleFullTextParse(
    di: DI,
) {
    Log.i(LOG_TAG, "Scheduling a full text parse work")
    val workRequest = OneTimeWorkRequestBuilder<FullTextWorker>()
        .addTag("feeder")
        .keepResultsForAtLeast(1, TimeUnit.MINUTES)

    val workManager by di.instance<WorkManager>()
    workManager.enqueue(workRequest.build())
}

class FullTextWorker(
    val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), DIAware {
    override val di: DI by closestDI(context)
    private val fullTextParser: FullTextParser by instance()

    override suspend fun doWork(): Result {
        Log.i(LOG_TAG, "Performing full text parse work")
        return when (fullTextParser.parseFullArticlesForMissing()) {
            true -> Result.success()
            false -> Result.failure()
        }.also {
            Log.i(LOG_TAG, "Finished full text parse work")
        }
    }
}

class FullTextParser(override val di: DI) : DIAware {
    private val repository: Repository by instance()
    private val okHttpClient: OkHttpClient by instance()
    private val filePathProvider: FilePathProvider by instance()

    suspend fun parseFullArticlesForMissing(): Boolean {
        logDebug(LOG_TAG, "Parsing full texts for missing")
        val itemsToSync: List<FeedItemForFetching> =
            repository.getFeedsItemsWithDefaultFullTextNeedingDownload()
                .firstOrNull()
                ?: emptyList()

        return itemsToSync
            .map { feedItem ->
                parseFullArticleIfMissing(
                    feedItem = feedItem,
                ) is FullTextParsingSuccess
            }
            .fold(true) { acc, value ->
                acc && value
            }
    }

    suspend fun parseFullArticleIfMissing(feedItem: FeedItemForFetching): FullTextParsingResult {
        val fullArticleFile =
            blobFullFile(itemId = feedItem.id, filesDir = filePathProvider.fullArticleDir)

        return try {
            if (fullArticleFile.isFile) {
                FullTextParsingSuccess
            } else {
                parseFullArticle(feedItem = feedItem)
            }
        } finally {
            repository.markAsFullTextDownloaded(feedItem.id)
        }
    }

    private suspend fun parseFullArticle(feedItem: FeedItemForFetching): FullTextParsingResult =
        withContext(Dispatchers.Default) {
            runCatching {
                logDebug(LOG_TAG, "Fetching full page ${feedItem.link}")
                val url = feedItem.link ?: throw MissingLink()

                val html: String = okHttpClient.curlAndOnResponse(URL(url)) { response ->
                    val body = response.body ?: throw MissingBody()

                    val contentType = body.contentType()
                        ?: throw NotHtmlContent("null")

                    val bytes = body.use {
                        it.bytes()
                    }

                    logDebug(LOG_TAG, "contentType: $contentType")

                    if (contentType.type != "text" || contentType.subtype != "html") {
                        throw NotHtmlContent("${contentType.type}/${contentType.subtype}")
                    }

                    val charset = contentType.charset() ?: findMetaCharset(bytes)

                    logDebug(LOG_TAG, "Full article charset: $charset")

                    String(bytes, charset ?: java.nio.charset.StandardCharsets.UTF_8)
                }

                logDebug(LOG_TAG, "Parsing article ${feedItem.link}")
                val article = Readability4JExtended(url, html).parse()

                logDebug(LOG_TAG, "Writing article ${feedItem.link}")
                withContext(Dispatchers.IO) {
                    filePathProvider.fullArticleDir.mkdirs()
                    blobFullOutputStream(feedItem.id, filePathProvider.fullArticleDir)
                        .bufferedWriter().use { writer ->
                            writer.write(article.contentWithUtf8Encoding)
                        }
                }
            }
                .onFailure {
                    Log.w(LOG_TAG, "Failed to get fulltext for ${feedItem.link}", it)
                }
                .fold(
                    onSuccess = { FullTextParsingSuccess },
                    onFailure = {
                        when (it) {
                            is FullTextParsingException -> FullTextParsingExplainableFailure(it)
                            else -> FullTextParsingFailure(it)
                        }
                    },
                )
        }

    /**
     * For sites which don't use UTF-8 like http://www.muhasebetr.com/rss/
     */
    private fun findMetaCharset(html: ByteArray): Charset? {
        val builder = StringBuilder()
        var state = CharsetState.INIT
        html.forEach { b ->
            state = nextCharsetState(b, state)

            when (state) {
                CharsetState.CHARSET -> {
                    builder.append(b.toInt().toChar())
                }

                CharsetState.END -> {
                    try {
                        return if (builder.isNotBlank()) {
                            Charset.forName(builder.toString())
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        // No charset present
                    }
                }

                else -> {
                    // Nothing to do
                }
            }
        }
        return null
    }

    private fun nextCharsetState(byte: Byte, state: CharsetState): CharsetState =
        when (state) {
            CharsetState.END -> CharsetState.END
            CharsetState.INIT -> when (byte.toInt().toChar()) {
                '<' -> CharsetState.TAG_START
                else -> CharsetState.INIT
            }

            CharsetState.TAG_START -> when (byte.toInt().toChar()) {
                'm' -> CharsetState.META_M
                else -> CharsetState.TAG_END
            }

            CharsetState.TAG_END -> when (byte.toInt().toChar()) {
                '>' -> CharsetState.INIT
                else -> CharsetState.TAG_END
            }

            CharsetState.META_M -> when (byte.toInt().toChar()) {
                'e' -> CharsetState.META_E
                else -> CharsetState.TAG_END
            }

            CharsetState.META_E -> when (byte.toInt().toChar()) {
                't' -> CharsetState.META_T
                else -> CharsetState.TAG_END
            }

            CharsetState.META_T -> when (byte.toInt().toChar()) {
                'a' -> CharsetState.META_A
                else -> CharsetState.TAG_END
            }

            CharsetState.META_A -> {
                val c = byte.toInt().toChar()
                when {
                    c.isWhitespace() -> CharsetState.META_SPACE
                    else -> CharsetState.TAG_END
                }
            }

            CharsetState.META_SPACE -> {
                val c = byte.toInt().toChar()
                when {
                    c.isWhitespace() -> CharsetState.META_SPACE
                    c == 'c' -> CharsetState.CHARSET_C
                    else -> CharsetState.TAG_END
                }
            }

            CharsetState.CHARSET_C -> when (byte.toInt().toChar()) {
                'h' -> CharsetState.CHARSET_H
                else -> CharsetState.TAG_END
            }

            CharsetState.CHARSET_H -> when (byte.toInt().toChar()) {
                'a' -> CharsetState.CHARSET_A
                else -> CharsetState.TAG_END
            }

            CharsetState.CHARSET_A -> when (byte.toInt().toChar()) {
                'r' -> CharsetState.CHARSET_R
                else -> CharsetState.TAG_END
            }

            CharsetState.CHARSET_R -> when (byte.toInt().toChar()) {
                's' -> CharsetState.CHARSET_S
                else -> CharsetState.TAG_END
            }

            CharsetState.CHARSET_S -> when (byte.toInt().toChar()) {
                'e' -> CharsetState.CHARSET_E
                else -> CharsetState.TAG_END
            }

            CharsetState.CHARSET_E -> when (byte.toInt().toChar()) {
                't' -> CharsetState.CHARSET_T
                else -> CharsetState.TAG_END
            }

            CharsetState.CHARSET_T -> when (byte.toInt().toChar()) {
                '=' -> CharsetState.CHARSET_EQUALS
                else -> CharsetState.TAG_END
            }

            CharsetState.CHARSET_EQUALS -> when (byte.toInt().toChar()) {
                '"' -> CharsetState.CHARSET_QUOTE
                else -> CharsetState.TAG_END
            }

            CharsetState.CHARSET_QUOTE -> when (byte.toInt().toChar()) {
                '"' -> CharsetState.END
                else -> CharsetState.CHARSET
            }

            CharsetState.CHARSET -> when (byte.toInt().toChar()) {
                '"' -> CharsetState.END
                else -> CharsetState.CHARSET
            }
        }

    companion object {
        internal const val LOG_TAG = "FEEDER_FULLTEXT"
    }
}

enum class CharsetState {
    END,
    INIT,
    TAG_START,
    TAG_END,
    META_M,
    META_E,
    META_T,
    META_A,
    META_SPACE,
    CHARSET_C,
    CHARSET_H,
    CHARSET_A,
    CHARSET_R,
    CHARSET_S,
    CHARSET_E,
    CHARSET_T,
    CHARSET_EQUALS,
    CHARSET_QUOTE,
    CHARSET,
}

sealed class FullTextParsingResult

object FullTextParsingSuccess : FullTextParsingResult()

class FullTextParsingFailure(val exception: Throwable) : FullTextParsingResult()

class FullTextParsingExplainableFailure(val exception: FullTextParsingException) :
    FullTextParsingResult()

sealed class FullTextParsingException : Exception()

class NotHtmlContent(actualContentType: String) : FullTextParsingException()

class MissingBody : FullTextParsingException()

class MissingLink : FullTextParsingException()
