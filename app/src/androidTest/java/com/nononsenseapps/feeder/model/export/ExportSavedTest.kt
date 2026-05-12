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
                listOf(
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
                    ),
                ),
                result.articles,
            )
        }
    }

    private suspend fun insertTestData(): Long {
        val feedId =
            db.feedDao().insertFeed(
                Feed(
                    title = "Ampersands are & the worst",
                    customTitle = "Ampersands",
                    url = URL("https://example.com/ampersands"),
                    tag = "Characters",
                    fullTextByDefault = true,
                ),
            )

        return db.feedItemDao().insertFeedItem(
            FeedItem(
                guid = "guid anime2you",
                plainTitle = "Item with image",
                plainSnippet = "Snippet with image",
                thumbnailImage =
                    MediaImage(
                        url = "https://example.com/ampersands/image.png",
                        width = 640,
                        height = 320,
                    ),
                enclosureLink = "https://example.com/ampersands/podcast.mp3",
                enclosureType = "audio/mpeg",
                author = "Example Author",
                feedId = feedId,
                link = "https://example.com/ampersands/1",
                pubDate = ZonedDateTime.of(2026, 5, 12, 8, 30, 0, 0, ZoneOffset.UTC),
                primarySortTime = Instant.parse("2026-05-12T08:29:30Z"),
                readTime = Instant.parse("2026-05-12T08:31:00Z"),
                wordCount = 42,
                wordCountFull = 420,
            ),
        )
    }

    @SmallTest
    @Test
    fun testImportSavedArticles() {
        runBlocking {
            val itemId = insertTestData()
            path!!.writeText(
                """
                https://example.com/ampersands
                https://example.com/not-in-db

                https://example.com/ampersands
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
}
