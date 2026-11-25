package com.nononsenseapps.feeder.db.room

import android.content.SharedPreferences
import androidx.core.database.getLongOrNull
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.nononsenseapps.feeder.FeederApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import kotlin.getValue
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@LargeTest
class TestMigrationFrom41To42 : DIAware {
    private val dbName = "testDb"
    private val feederApplication: FeederApplication = ApplicationProvider.getApplicationContext()
    override val di: DI by closestDI(feederApplication)

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
        @Suppress("SimpleRedundantLet")
        testHelper.createDatabase(dbName, FROM_VERSION).let { oldDB ->
            oldDB.execSQL(
                """
                INSERT INTO feeds(id, title, custom_title, url, tag, notify, image_url, last_sync, response_hash, fulltext_by_default, open_articles_with, alternate_id, currently_syncing, when_modified, site_fetched, skip_duplicates, retry_after, summarize_on_open, fetch_og_images, block_rules, allow_rules, trust_all_certificates)
                VALUES(1, 'feed', '', 'http://url', 'folder', 0, null, 0, 666, 0, '', 0, 0, 0, 0, 0, 0, 0, 0, '', '', 0)
                """.trimIndent(),
            )
        }

        val sharedPrefs by di.instance<SharedPreferences>()

        sharedPrefs
            .edit()
            .putString("pref_last_feed_tag", "folder")
            .putString("pref_widget_last_feed_tag", "folder")
            .apply()

        val db =
            testHelper.runMigrationsAndValidate(
                dbName,
                TO_VERSION,
                true,
                MigrationFrom41To42(di),
            )

        db
            .query(
                """
                SELECT id, parent_id, name, expanded FROM folders
                """.trimIndent(),
            ).use {
                assert(it.count == 1)
                assert(it.moveToFirst())
                assertEquals(1, it.getLong(0))
                assertNull(it.getLongOrNull(1))
                assertEquals("folder", it.getString(2))
                assertEquals(0, it.getInt(3))
            }

        db
            .query(
                """
                SELECT folder_id FROM feeds
                """.trimIndent(),
            ).use {
                assert(it.count == 1)
                assert(it.moveToFirst())
                assertEquals(1, it.getLong(0))
            }

        db
            .query(
                """
                SELECT folder_id FROM feeds_with_items_for_nav_drawer
                """.trimIndent(),
            ).use {
                assert(it.count == 1)
                assert(it.moveToFirst())
                assertEquals(1, it.getLong(0))
            }

        db
            .query(
                """
                SELECT ancestor_id, descendant_id, depth FROM folder_closures
                """.trimIndent(),
            ).use {
                assert(it.count == 1)
                assert(it.moveToFirst())
                assertEquals(1, it.getLong(0))
                assertEquals(1, it.getLong(1))
                assertEquals(0, it.getInt(2))
            }

        // Test if triggers are created
        val triggerNames = mutableMapOf<String, String>()

        db
            .query(
                """
                SELECT name, sql FROM sqlite_master WHERE type = 'trigger'
                """.trimIndent(),
            ).use {
                while (it.moveToNext()) {
                    triggerNames[it.getString(0)] = it.getString(1)
                }
            }

        assertTrue(triggerNames.containsKey("prevent_folder_cycles_after_insert"))
        assertEquals(
            """
            CREATE TRIGGER prevent_folder_cycles_after_insert
            AFTER INSERT ON folders
            WHEN EXISTS(
                SELECT 1 FROM folder_closures
                WHERE ancestor_id = NEW.id AND descendant_id = NEW.parent_id
            )
            BEGIN
                SELECT RAISE(ABORT, 'Cannot insert folder into own subtree');
            END
            """.trimIndent(),
            triggerNames["prevent_folder_cycles_after_insert"],
        )

        assertTrue(triggerNames.containsKey("prevent_folder_cycles_after_update"))
        assertEquals(
            """
            CREATE TRIGGER prevent_folder_cycles_after_update
            AFTER UPDATE OF parent_id ON folders
            WHEN EXISTS(
                SELECT 1 FROM folder_closures
                WHERE ancestor_id = OLD.id AND descendant_id = NEW.parent_id
            )
            BEGIN
                SELECT RAISE(ABORT, 'Cannot move folder into own subtree');
            END
            """.trimIndent(),
            triggerNames["prevent_folder_cycles_after_update"],
        )

        val lastFolderId = sharedPrefs.getLong("pref_last_folder_id", ID_UNSET)
        val lastWidgetFolderId = sharedPrefs.getLong("pref_widget_last_folder_id", ID_UNSET)
        assertEquals(1, lastFolderId)
        assertEquals(1, lastWidgetFolderId)
    }

    companion object {
        private const val FROM_VERSION = 41
        private const val TO_VERSION = 42
    }
}
