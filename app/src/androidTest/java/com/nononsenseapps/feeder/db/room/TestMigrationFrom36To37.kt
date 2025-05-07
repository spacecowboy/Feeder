package com.nononsenseapps.feeder.db.room

import android.util.Log
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.util.FilePathProvider
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@LargeTest
class TestMigrationFrom36To37 : DIAware {
    private val dbName = "testDb"
    private val feederApplication: FeederApplication = ApplicationProvider.getApplicationContext()
    override val di: DI by closestDI(feederApplication)
    private val filePathProvider: FilePathProvider by instance()
    private val tempOldArticleDir = File(feederApplication.filesDir, "testOldArticles")

    @Rule
    @JvmField
    val testHelper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java,
            emptyList(),
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Before
    fun setUp() {
        // Create a temporary old article directory
        tempOldArticleDir.mkdirs()

        // Create article directory if it doesn't exist
        filePathProvider.articleDir.mkdirs()

        // Clean any existing test files
        filePathProvider.articleDir.listFiles()?.forEach { it.delete() }
    }

    @After
    fun tearDown() {
        // Clean up test files
        tempOldArticleDir.deleteRecursively()
    }

    @Test
    fun migrate_validFilesAreMoved_invalidFilesAreDeleted() {
        // First, set up the database with some feed item records
        @Suppress("SimpleRedundantLet")
        testHelper.createDatabase(dbName, FROM_VERSION).let { oldDB ->
            // Create a valid feed item
            oldDB.execSQL(
                """
                INSERT INTO feeds(id, title, url, custom_title, tag, notify, last_sync, response_hash, fulltext_by_default, open_articles_with, alternate_id, currently_syncing, when_modified, site_fetched, skip_duplicates, retry_after)
                VALUES(1, 'feed', 'http://url', '', '', 0, 0, 666, 0, '', 0, 0, 0, 0, 0, 0)
                """.trimIndent(),
            )

            oldDB.execSQL(
                """
                INSERT INTO feed_items(id, guid, title, plain_title, plain_snippet, feed_id)
                VALUES(101, 'guid1', 'title1', 'plaintitle1', 'snippet1', 1)
                """.trimIndent(),
            )

            oldDB.execSQL(
                """
                INSERT INTO feed_items(id, guid, title, plain_title, plain_snippet, feed_id)
                VALUES(102, 'guid2', 'title2', 'plaintitle2', 'snippet2', 1)
                """.trimIndent(),
            )

            oldDB.close()
        }

        // Create test files in the old article directory
        // Valid files - should be migrated
        File(tempOldArticleDir, "101.txt.gz").createNewFile()
        File(tempOldArticleDir, "102.txt.gz").createNewFile()

        // Invalid files - should be deleted
        File(tempOldArticleDir, "999.txt.gz").createNewFile() // ID doesn't exist in DB
        File(tempOldArticleDir, "invalid.txt.gz").createNewFile() // Not a valid ID format

        // Patch the migration to use our temp directory instead of the actual oldArticleDir
        val testMigration =
            object : MigrationFrom36To37(di) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    Log.i("TEST_MIGRATION", "Starting test migration")
                    try {
                        // Get all valid feed item IDs from the database
                        val validFeedItemIds = feedItemDao.getAllFeedItemIds()

                        // Ensure target directory exists
                        if (!filePathProvider.articleDir.exists()) {
                            filePathProvider.articleDir.mkdirs()
                        }

                        // Migrate files from the temp directory instead of oldArticleDir
                        migrateFiles(tempOldArticleDir, filePathProvider.articleDir, validFeedItemIds)

                        Log.i("TEST_MIGRATION", "Completed test migration")
                    } catch (e: Exception) {
                        Log.e("TEST_MIGRATION", "Error during test migration", e)
                    }
                }
            }

        // Run the migration
        testHelper.runMigrationsAndValidate(
            dbName,
            TO_VERSION,
            true,
            testMigration,
        )

        // Verify that valid files were migrated to the article directory
        assertTrue(File(filePathProvider.articleDir, "101.txt.gz").exists(), "Valid file 101.txt.gz should be migrated")
        assertTrue(File(filePathProvider.articleDir, "102.txt.gz").exists(), "Valid file 102.txt.gz should be migrated")

        // Verify that invalid files were deleted from the old article directory
        assertFalse(File(tempOldArticleDir, "999.txt.gz").exists(), "Invalid file 999.txt.gz should be deleted")
        assertFalse(File(tempOldArticleDir, "invalid.txt.gz").exists(), "Invalid file invalid.txt.gz should be deleted")
    }

    companion object {
        private const val FROM_VERSION = 36
        private const val TO_VERSION = 37
    }
}
