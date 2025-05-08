package com.nononsenseapps.feeder.db.room

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
    private val oldDir = File(feederApplication.cacheDir, "articles")
    private val newDir = File(feederApplication.filesDir, "articles")

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
        oldDir.deleteRecursively()
        oldDir.mkdirs()
        newDir.deleteRecursively()
    }

    @After
    fun tearDown() {
        // Clean up test files
        oldDir.deleteRecursively()
        filePathProvider.articleDir.deleteRecursively()
    }

    @Test
    fun migrate_validFilesAreMoved_invalidFilesAreDeleted() {
        // First, set up the database with some feed item records
        testHelper.createDatabase(dbName, FROM_VERSION).close()

        // Test files here
        val name1 = "1.txt.gz"
        val name2 = "2.txt.gz"

        val oldDir = feederApplication.cacheDir.resolve("articles")

        assertTrue {
            oldDir.resolve(name1).createNewFile()
        }

        assertTrue {
            oldDir.resolve(name2).createNewFile()
        }

        testHelper.runMigrationsAndValidate(
            dbName,
            TO_VERSION,
            true,
            MigrationFrom36To37(di),
        )

        assertTrue {
            newDir.resolve(name1).isFile
        }

        assertTrue {
            newDir.resolve(name2).isFile
        }

        assertFalse {
            oldDir.resolve(name1).isFile
        }

        assertFalse {
            oldDir.resolve(name2).isFile
        }
    }

    companion object {
        private const val FROM_VERSION = 36
        private const val TO_VERSION = 37
    }
}
