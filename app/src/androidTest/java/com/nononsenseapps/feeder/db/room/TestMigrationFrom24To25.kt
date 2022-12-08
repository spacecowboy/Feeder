package com.nononsenseapps.feeder.db.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class TestMigrationFrom24To25 {
    private val dbName = "testDb"

    @Rule
    @JvmField
    val testHelper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate() {
        testHelper.createDatabase(dbName, 24).let { oldDB ->
            // Nothing to do
        }

        val db = testHelper.runMigrationsAndValidate(dbName, 25, true, MIGRATION_24_25)

        db.query(
            """
            SELECT * FROM push_message_queue
            """.trimIndent()
        )!!.use {
            assert(it.count == 0)
        }
        db.query(
            """
            SELECT * FROM push_this_device
            """.trimIndent()
        )!!.use {
            assert(it.count == 0)
        }
        db.query(
            """
            SELECT * FROM push_known_devices
            """.trimIndent()
        )!!.use {
            assert(it.count == 0)
        }
    }
}
