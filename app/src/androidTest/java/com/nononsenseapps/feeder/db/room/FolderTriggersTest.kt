package com.nononsenseapps.feeder.db.room

import android.database.sqlite.SQLiteConstraintException
import androidx.core.database.getLongOrNull
import androidx.test.core.app.ApplicationProvider
import com.nononsenseapps.feeder.ui.TestDatabaseRule
import org.junit.After
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class FolderTriggersTest {
    @get:Rule
    val testDb = TestDatabaseRule(ApplicationProvider.getApplicationContext())

    @After
    fun tearDown() {
        testDb.db.close()
    }

    @Test
    fun abortInsertWithItselfAsParent() {
        val db = testDb.db.openHelper.writableDatabase

        try {
            db
                .execSQL(
                    """
                    INSERT INTO folders(id, parent_id, name, expanded) VALUES (1, 1, 'folder', 0)
                    """.trimIndent(),
                )
        } catch (_: SQLiteConstraintException) {
        }

        db
            .query(
                """
                SELECT * FROM folders WHERE id = 1
                """.trimIndent(),
            ).use {
                assert(it.count == 0)
            }
    }

    @Test
    fun abortInsertCycle() {
        val db = testDb.db.openHelper.writableDatabase

        try {
            db
                .execSQL(
                    """
                    INSERT INTO folders(id, parent_id, name, expanded) VALUES (1, 2, 'folder1', 0), (2, 1, 'folder2', 0)
                    """.trimIndent(),
                )
        } catch (_: SQLiteConstraintException) {
        }

        db
            .query(
                """
                SELECT * FROM folders WHERE id IN (1, 2)
                """.trimIndent(),
            ).use {
                assert(it.count == 0)
            }
    }

    @Test
    fun abortUpdateWithItselfAsParent() {
        val db = testDb.db.openHelper.writableDatabase

        db
            .execSQL(
                """
                INSERT INTO folders(id, parent_id, name, expanded) VALUES (1, null, 'folder', 0)
                """.trimIndent(),
            )

        try {
            db
                .execSQL(
                    """
                    UPDATE folders SET parent_id = 1 WHERE id = 1
                    """.trimIndent(),
                )
        } catch (_: SQLiteConstraintException) {
        }

        db
            .query(
                """
                SELECT parent_id FROM folders WHERE id = 1
                """.trimIndent(),
            ).use {
                assert(it.count == 1)
                assert(it.moveToFirst())
                assertEquals(null, it.getLongOrNull(0))
            }
    }

    @Test
    fun abortUpdateFoldersToCycle() {
        val db = testDb.db.openHelper.writableDatabase

        db
            .execSQL(
                """
                INSERT INTO folders(id, parent_id, name, expanded) VALUES (1, null, 'folder', 0), (2, 1, 'subfolder', 0)
                """.trimIndent(),
            )

        try {
            db
                .execSQL(
                    """
                    UPDATE folders SET parent_id = 2 WHERE id = 1
                    """.trimIndent(),
                )
        } catch (_: SQLiteConstraintException) {
        }

        db
            .query(
                """
                SELECT parent_id FROM folders WHERE id = 1
                """.trimIndent(),
            ).use {
                assert(it.count == 1)
                assert(it.moveToFirst())
                assertEquals(null, it.getLongOrNull(0))
            }
    }
}
