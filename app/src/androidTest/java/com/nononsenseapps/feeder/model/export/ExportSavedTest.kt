package com.nononsenseapps.feeder.model.export

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.nononsenseapps.feeder.archmodel.FeedStore
import com.nononsenseapps.feeder.archmodel.SettingsStore
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.BlocklistDao
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.model.MediaImage
import com.nononsenseapps.feeder.model.OPMLParserHandler
import com.nononsenseapps.feeder.model.opml.OPMLImporter
import com.nononsenseapps.feeder.util.ToastMaker
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.io.File
import java.net.URL
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ExportSavedTest : DIAware {
    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var db: AppDatabase
    override val di =
        DI.lazy {
            bind<SharedPreferences>() with
                singleton {
                    PreferenceManager.getDefaultSharedPreferences(
                        this@ExportSavedTest.context,
                    )
                }
            bind<AppDatabase>() with instance(db)
            bind<FeedDao>() with singleton { db.feedDao() }
            bind<FeedItemDao>() with singleton { db.feedItemDao() }
            bind<BlocklistDao>() with singleton { db.blocklistDao() }
            bind<SettingsStore>() with singleton { SettingsStore(di) }
            bind<FeedStore>() with singleton { FeedStore(di) }
            bind<OPMLParserHandler>() with singleton { OPMLImporter(di) }
            bind<ToastMaker>() with
                instance(
                    object : ToastMaker {
                        override suspend fun makeToast(text: String) {}

                        override suspend fun makeToast(resId: Int) {}
                    },
                )
            bind<ContentResolver>() with singleton { this@ExportSavedTest.context.contentResolver }
        }

    private var dir: File? = null
    private var path: File? = null

    @Before
    fun setup() {
        // Get internal data dir
        dir = context.externalCacheDir!!.resolve("${Random.nextInt()}").also { it.mkdir() }
        path = context.externalCacheDir!!.resolve("${Random.nextInt()}").also { it.createNewFile() }
        Assert.assertTrue("Need to be able to write to data dir $dir", dir!!.canWrite())

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        // Remove everything in database
    }

    @SmallTest
    @Test
    fun testExportSavedArticles() {
        runBlocking {
            val itemId = insertTestData()

            db.feedItemDao().setBookmarked(itemId, true)

            assertTrue {
                exportSavedArticles(
                    di,
                    path!!.toUri(),
                ).isRight()
            }

            val result = Json.decodeFromString(SavedArticlesExport.serializer(), path!!.readText())

            assertEquals(SAVED_ARTICLES_EXPORT_FORMAT, result.format)
            assertEquals(SAVED_ARTICLES_EXPORT_VERSION, result.version)
            assertEquals(
                listOf(savedArticleExportItem()),
                result.articles,
            )
        }
    }

    private fun savedArticleExportItem(): SavedArticleExportItem =
        SavedArticleExportItem(
            link = "https://example.com/ampersands/1",
            guid = "guid anime2you",
            title = "Item with image",
            snippet = "Snippet with image",
            pubDate = "2026-05-12T08:30Z",
            primarySortTime = "2026-05-12T08:29:30Z",
            readTime = "2026-05-12T08:31:00Z",
            author = "Example Author",
            thumbnailImage =
                MediaImage(
                    url = "https://example.com/ampersands/image.png",
                    width = 640,
                    height = 320,
                ),
            enclosureLink = "https://example.com/ampersands/podcast.mp3",
            enclosureType = "audio/mpeg",
            wordCount = 42,
            wordCountFull = 420,
            feed =
                SavedArticleFeedExportItem(
                    url = "https://example.com/ampersands",
                    title = "Ampersands are & the worst",
                    customTitle = "Ampersands",
                    tag = "Characters",
                    fullTextByDefault = true,
                ),
        )

    private suspend fun insertTestFeed(): Long {
        val article = savedArticleExportItem()
        return db.feedDao().insertFeed(
            Feed(
                title = article.feed.title,
                customTitle = article.feed.customTitle,
                url = URL(article.feed.url),
                tag = article.feed.tag,
                fullTextByDefault = article.feed.fullTextByDefault,
            ),
        )
    }

    private suspend fun insertTestData(): Long {
        val (itemId, _) = insertTestDataWithFeed()
        return itemId
    }

    private suspend fun insertTestDataWithFeed(): Pair<Long, Long> {
        val article = savedArticleExportItem()
        val feedId = insertTestFeed()
        val itemId =
            db.feedItemDao().insertFeedItem(
                FeedItem(
                    guid = article.guid,
                    plainTitle = article.title,
                    plainSnippet = article.snippet,
                    thumbnailImage = article.thumbnailImage,
                    enclosureLink = article.enclosureLink,
                    enclosureType = article.enclosureType,
                    author = article.author,
                    feedId = feedId,
                    link = article.link,
                    pubDate = ZonedDateTime.of(2026, 5, 12, 8, 30, 0, 0, ZoneOffset.UTC),
                    primarySortTime = Instant.parse("2026-05-12T08:29:30Z"),
                    readTime = Instant.parse("2026-05-12T08:31:00Z"),
                    wordCount = article.wordCount,
                    wordCountFull = article.wordCountFull,
                ),
            )
        return itemId to feedId
    }

    private fun assertSavedArticleRestored(
        article: SavedArticleExportItem,
        imported: FeedItem,
        feedId: Long,
    ) {
        assertTrue(imported.bookmarked)
        assertEquals(article.title, imported.plainTitle)
        assertEquals(article.snippet, imported.plainSnippet)
        assertEquals(article.thumbnailImage, imported.thumbnailImage)
        assertEquals(article.enclosureLink, imported.enclosureLink)
        assertEquals(article.enclosureType, imported.enclosureType)
        assertEquals(article.author, imported.author)
        assertEquals(article.link, imported.link)
        assertEquals(ZonedDateTime.of(2026, 5, 12, 8, 30, 0, 0, ZoneOffset.UTC), imported.pubDate)
        assertEquals(Instant.parse(article.primarySortTime), imported.primarySortTime)
        assertEquals(Instant.parse(article.readTime!!), imported.readTime)
        assertEquals(article.wordCount, imported.wordCount)
        assertEquals(article.wordCountFull, imported.wordCountFull)
        assertEquals(feedId, imported.feedId)
    }

    @SmallTest
    @Test
    fun testExportThenImportSavedArticlesRestoresMissingArticle() {
        runBlocking {
            val article = savedArticleExportItem()
            val (itemId, feedId) = insertTestDataWithFeed()
            db.feedItemDao().setBookmarked(itemId, true)

            assertTrue {
                exportSavedArticles(
                    di,
                    path!!.toUri(),
                ).isRight()
            }

            db.feedItemDao().deleteFeedItems(listOf(itemId))
            Assert.assertNull(db.feedItemDao().loadFeedItem(article.guid, feedId))

            assertTrue {
                importSavedArticles(
                    di,
                    path!!.toUri(),
                ).isRight()
            }

            val imported = assertNotNull(db.feedItemDao().loadFeedItem(article.guid, feedId))
            assertSavedArticleRestored(
                article = article,
                imported = imported,
                feedId = feedId,
            )
        }
    }

    @SmallTest
    @Test
    fun testImportSavedArticles() {
        runBlocking {
            val itemId = insertTestData()
            path!!.writeText(
                """
                https://example.com/ampersands/1
                https://example.com/not-in-db

                https://example.com/ampersands/1
                """.trimIndent(),
            )
            Assert.assertFalse(db.feedItemDao().loadFeedItem(itemId)!!.bookmarked)
            assertTrue {
                importSavedArticles(
                    di,
                    path!!.toUri(),
                ).isRight()
            }
            assertTrue(db.feedItemDao().loadFeedItem(itemId)!!.bookmarked)
        }
    }

    @SmallTest
    @Test
    fun testImportSavedArticlesExportRestoresMissingArticle() {
        runBlocking {
            val article = savedArticleExportItem()
            val feedId = insertTestFeed()
            path!!.writeText(
                Json.encodeToString(
                    SavedArticlesExport.serializer(),
                    SavedArticlesExport(
                        format = SAVED_ARTICLES_EXPORT_FORMAT,
                        version = SAVED_ARTICLES_EXPORT_VERSION,
                        articles = listOf(article),
                    ),
                ),
            )

            Assert.assertNull(db.feedItemDao().loadFeedItem(article.guid, feedId))
            assertTrue {
                importSavedArticles(
                    di,
                    path!!.toUri(),
                ).isRight()
            }

            val imported = assertNotNull(db.feedItemDao().loadFeedItem(article.guid, feedId))
            assertSavedArticleRestored(
                article = article,
                imported = imported,
                feedId = feedId,
            )
        }
    }
}
