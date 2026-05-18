package com.nononsenseapps.feeder.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.archmodel.AndroidSystemStore
import com.nononsenseapps.feeder.archmodel.FeedItemStore
import com.nononsenseapps.feeder.archmodel.FeedStore
import com.nononsenseapps.feeder.archmodel.FontStore
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.archmodel.SessionStore
import com.nononsenseapps.feeder.archmodel.SettingsStore
import com.nononsenseapps.feeder.archmodel.SyncRemoteStore
import com.nononsenseapps.feeder.crypto.AesCbcWithIntegrity
import com.nononsenseapps.feeder.crypto.SecretKeys
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.BlocklistDao
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.ReadStatusSyncedDao
import com.nononsenseapps.feeder.db.room.RemoteReadMarkDao
import com.nononsenseapps.feeder.db.room.SyncRemote
import com.nononsenseapps.feeder.db.room.SyncRemoteDao
import com.nononsenseapps.feeder.di.networkModule
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import com.nononsenseapps.feeder.util.FilePathProvider
import com.nononsenseapps.feeder.util.filePathProvider
import com.nononsenseapps.jsonfeed.cachingHttpClient
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.net.URL
import java.time.Instant

@RunWith(AndroidJUnit4::class)
@MediumTest
class SyncRestClientTest : DIAware {
    @get:Rule
    val testDb = TestDatabaseRule(getApplicationContext())

    override val di by DI.lazy {
        bind<AppDatabase>() with instance(testDb.db)
        bind<FeedDao>() with singleton { testDb.db.feedDao() }
        bind<FeedItemDao>() with singleton { testDb.db.feedItemDao() }
        bind<BlocklistDao>() with singleton { testDb.db.blocklistDao() }
        bind<RemoteReadMarkDao>() with singleton { testDb.db.remoteReadMarkDao() }
        bind<ReadStatusSyncedDao>() with singleton { testDb.db.readStatusSyncedDao() }
        bind<SyncRemoteDao>() with singleton { testDb.db.syncRemoteDao() }
        bind<FeedStore>() with singleton { FeedStore(di) }
        bind<FeedItemStore>() with singleton { FeedItemStore(di) }
        bind<SettingsStore>() with
            singleton {
                SettingsStore(di).also { it.setAddedFeederNews(true) }
            }
        bind<FontStore>() with singleton { FontStore(di) }
        bind<SessionStore>() with singleton { SessionStore() }
        bind<SyncRemoteStore>() with singleton { SyncRemoteStore(di) }
        bind<OkHttpClient>() with singleton { cachingHttpClient() }
        import(networkModule)
        bind<SharedPreferences>() with
            singleton {
                getApplicationContext<FeederApplication>().getSharedPreferences("synctest", Context.MODE_PRIVATE)
            }
        bind<ApplicationCoroutineScope>() with singleton { ApplicationCoroutineScope() }
        bind<Repository>() with singleton { Repository(di) }
        bind<FilePathProvider>() with
            singleton {
                filePathProvider(
                    cacheDir = getApplicationContext<FeederApplication>().cacheDir,
                    filesDir = getApplicationContext<FeederApplication>().filesDir,
                )
            }
        bind<AndroidSystemStore>() with singleton { AndroidSystemStore(di) }
    }

    private val server = MockWebServer()

    @After
    fun stopServer() {
        server.shutdown()
    }

    @Before
    fun setup() {
        server.start()
    }

    /**
     * Creates a [SyncRestClient] wired to the [MockWebServer].
     *
     * A [SyncRemote] with a valid 64-char [SyncRemote.syncChainId] is inserted into the test
     * database so that [SyncRestClient.safeBlock] passes its [SyncRemote.hasSyncChain] guard.
     * [SyncRestClient.initForTesting] is then called to replace the HTTP client and key with
     * instances that point directly at [server], ensuring no real network traffic occurs.
     *
     * Returns a [Triple] of the configured client, the raw [SecretKeys], and the [SyncRemote]
     * that was stored in the database.
     */
    private suspend fun buildSyncRestClientWithMockServer(): Triple<SyncRestClient, SecretKeys, SyncRemote> {
        val secretKey = AesCbcWithIntegrity.generateKey()
        val syncRemote =
            SyncRemote(
                id = 1L,
                syncChainId = "a".repeat(64),
                secretKey = AesCbcWithIntegrity.encodeKey(secretKey),
                url = server.url("/").toUrl(),
            )
        testDb.db.syncRemoteDao().insert(syncRemote)

        // SyncRestClient.init{} calls initialize() which will find the SyncRemote we just
        // inserted and configure itself.  We then call initForTesting to guarantee our
        // test-local FeederSync instance (pointing at the MockWebServer) and key are used.
        val syncRestClient = SyncRestClient(di)
        val feederSync = getFeederSyncClient(syncRemote, cachingHttpClient())
        syncRestClient.initForTesting(feederSync, secretKey)

        return Triple(syncRestClient, secretKey, syncRemote)
    }

    // -----------------------------------------------------------------------------------------
    // Test 1
    // -----------------------------------------------------------------------------------------

    /**
     * When the server returns two encrypted read-marks, [SyncRestClient.getRead] should decrypt
     * each one and insert the corresponding guids into the `remote_read_mark` table.
     */
    @Test
    fun getReadCallsRemoteMarkAsReadForEachReceivedMark() =
        runBlocking {
            val (syncRestClient, secretKey, _) = buildSyncRestClientWithMockServer()

            val moshi = getMoshi()
            val readMarkAdapter = moshi.adapter<ReadMarkContent>()

            val mark1 = ReadMarkContent(feedUrl = URL("https://example.com/feed"), articleGuid = "guid-1")
            val mark2 = ReadMarkContent(feedUrl = URL("https://example.com/feed"), articleGuid = "guid-2")

            val enc1 = AesCbcWithIntegrity.encryptString(readMarkAdapter.toJson(mark1), secretKey)
            val enc2 = AesCbcWithIntegrity.encryptString(readMarkAdapter.toJson(mark2), secretKey)

            val t1 = Instant.ofEpochMilli(1_704_067_200_000L) // 2024-01-01T00:00:00Z
            val t2 = Instant.ofEpochMilli(1_704_153_600_000L) // 2024-01-02T00:00:00Z

            val responseBody =
                moshi.adapter<GetEncryptedReadMarksResponse>().toJson(
                    GetEncryptedReadMarksResponse(
                        readMarks =
                            listOf(
                                EncryptedReadMark(timestamp = t1, encrypted = enc1),
                                EncryptedReadMark(timestamp = t2, encrypted = enc2),
                            ),
                    ),
                )

            server.enqueue(
                MockResponse()
                    .setBody(responseBody)
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json"),
            )

            syncRestClient.getRead()

            val guids =
                testDb.db
                    .remoteReadMarkDao()
                    .getGuidsWhichAreSyncedAsReadInFeed(URL("https://example.com/feed"))

            assertTrue("guid-1 should be in remote_read_mark", "guid-1" in guids)
            assertTrue("guid-2 should be in remote_read_mark", "guid-2" in guids)
        }

    // -----------------------------------------------------------------------------------------
    // Test 2
    // -----------------------------------------------------------------------------------------

    /**
     * When the server returns a corrupt mark followed by a valid mark, [SyncRestClient.getRead]
     * should advance [SyncRemote.latestMessageTimestamp] past both — including past the corrupt
     * one — so the same mark is not retried on the next sync.
     *
     * The "corrupt" mark is produced by encrypting the JSON literal `"null"` with the correct
     * key.  After decryption Moshi returns `null` for the `ReadMarkContent` adapter, which
     * triggers the null-check fix in [SyncRestClient.getRead] that advances the timestamp and
     * continues to the next mark.
     */
    @Test
    fun getReadAdvancesTimestampPastCorruptMark() =
        runBlocking {
            val (syncRestClient, secretKey, _) = buildSyncRestClientWithMockServer()

            val moshi = getMoshi()

            // Encrypting the JSON literal "null" causes the Moshi adapter to return null
            // (instead of throwing), which exercises the fix that still advances the timestamp.
            val corruptEnc = AesCbcWithIntegrity.encryptString("null", secretKey)

            val validContent =
                ReadMarkContent(feedUrl = URL("https://example.com/feed"), articleGuid = "guid-valid")
            val validEnc =
                AesCbcWithIntegrity.encryptString(
                    moshi.adapter<ReadMarkContent>().toJson(validContent),
                    secretKey,
                )

            val t1 = Instant.ofEpochMilli(1_704_067_200_000L) // 2024-01-01T00:00:00Z
            val t2 = Instant.ofEpochMilli(1_704_153_600_000L) // 2024-01-02T00:00:00Z

            val responseBody =
                moshi.adapter<GetEncryptedReadMarksResponse>().toJson(
                    GetEncryptedReadMarksResponse(
                        readMarks =
                            listOf(
                                EncryptedReadMark(timestamp = t1, encrypted = corruptEnc),
                                EncryptedReadMark(timestamp = t2, encrypted = validEnc),
                            ),
                    ),
                )

            server.enqueue(
                MockResponse()
                    .setBody(responseBody)
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json"),
            )

            syncRestClient.getRead()

            val latestTimestamp =
                testDb.db
                    .syncRemoteDao()
                    .getSyncRemote()!!
                    .latestMessageTimestamp
            assertEquals(
                "latestMessageTimestamp should advance past the corrupt mark to t2",
                t2,
                latestTimestamp,
            )
        }

    // -----------------------------------------------------------------------------------------
    // Test 3
    // -----------------------------------------------------------------------------------------

    /**
     * Given two feed items with a non-null [FeedItem.readTime] that have not yet been synced,
     * [SyncRestClient.markAsRead] should POST them to the server and then record each item in
     * `read_status_synced` so that they are no longer returned by
     * [ReadStatusSyncedDao.getFeedItemsWithoutSyncedReadMark].
     */
    @Test
    fun markAsReadSendsItemsAndMarksThemAsSynced() =
        runBlocking {
            val (syncRestClient, _, _) = buildSyncRestClientWithMockServer()

            val feedId =
                testDb.db.feedDao().insertFeed(
                    Feed(title = "Test Feed", url = URL("https://example.com/feed")),
                )
            testDb.db.feedItemDao().insertFeedItem(
                FeedItem(feedId = feedId, guid = "guid-1", readTime = Instant.now()),
            )
            testDb.db.feedItemDao().insertFeedItem(
                FeedItem(feedId = feedId, guid = "guid-2", readTime = Instant.now()),
            )

            val t = Instant.ofEpochMilli(1_704_067_200_000L)
            val responseBody =
                getMoshi().adapter<SendReadMarkResponse>().toJson(SendReadMarkResponse(timestamp = t))

            server.enqueue(
                MockResponse()
                    .setBody(responseBody)
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json"),
            )

            syncRestClient.markAsRead()

            val unsyncedItems =
                testDb.db.readStatusSyncedDao().getFeedItemsWithoutSyncedReadMark()
            assertTrue(
                "All read items should be recorded in read_status_synced after markAsRead()",
                unsyncedItems.isEmpty(),
            )

            val request = server.takeRequest()
            assertEquals("Expected a POST request", "POST", request.method)
            assertTrue(
                "Request path should contain 'ereadmark'",
                request.path?.contains("ereadmark") == true,
            )
        }
}
