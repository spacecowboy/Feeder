package com.nononsenseapps.feeder.db.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
@LargeTest
class TestMigrationFrom40To41 {
    private val dbName = "testDb"

    @Rule
    @JvmField
    val testHelper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java,
            emptyList(),
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun migrate() {
        testHelper.createDatabase(dbName, FROM_VERSION).let { oldDB ->
            oldDB.execSQL(
                """
                INSERT INTO feeds(id, title, custom_title, url, tag, notify, image_url, last_sync, response_hash, fulltext_by_default, open_articles_with, alternate_id, currently_syncing, when_modified, site_fetched, skip_duplicates, retry_after, summarize_on_open, fetch_og_images, block_rules, allow_rules)
                VALUES(1, 'feed', '', 'http://url', '', 0, null, 0, 666, 0, '', 0, 0, 0, 0, 0, 0, 0, 0, '', '')
                """.trimIndent(),
            )
            oldDB.execSQL(
                """
                INSERT INTO feed_items(id, guid, title, plain_title, plain_snippet, image_url, image_from_body, enclosure_link, enclosure_type, author, pub_date, link, unread, notified, feed_id, first_synced_time, primary_sort_time, pinned, bookmarked, fulltext_downloaded, read_time, word_count, word_count_full, block_time)
                VALUES(8, 'item1', 'title', 'ptitle', 'psnippet', 'http://image', 0, '', '', 'author', 0, 'http://item1', 1, 0, 1, 0, 0, 0, 0, 0, null, 5, 900, null)
                """.trimIndent(),
            )
        }

        val db =
            testHelper.runMigrationsAndValidate(
                dbName,
                TO_VERSION,
                true,
                MIGRATION_40_41,
            )

        db
            .query(
                """
                SELECT article_tags FROM feed_items
                """.trimIndent(),
            ).use {
                assert(it.count == 1)
                assert(it.moveToFirst())
                assertNull(it.getString(0))
            }
    }

    companion object {
        private const val FROM_VERSION = 40
        private const val TO_VERSION = 41
    }
}
