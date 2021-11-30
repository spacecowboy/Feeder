package com.nononsenseapps.feeder.db.room

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.blob.blobOutputStream
import com.nononsenseapps.feeder.util.contentValues
import com.nononsenseapps.feeder.util.forEach
import com.nononsenseapps.feeder.util.setInt
import com.nononsenseapps.feeder.util.setLong
import com.nononsenseapps.feeder.util.setString

const val DATABASE_NAME = "rssDatabase"
const val ID_UNSET: Long = 0
const val ID_ALL_FEEDS: Long = -10

/**
 * Database versions
 * 4: Was using the RSS Server
 * 5: Added feed url field to feed_item
 * 6: Added feed icon field to feeds
 * 7: Migration to Room
 */
@Database(entities = [Feed::class, FeedItem::class], version = 16)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun feedItemDao(): FeedItemDao

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Use this in tests only
         */
        internal fun setInstance(db: AppDatabase) {
            instance = db
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(*allMigrations)
                .build()
        }
    }
}

val allMigrations = arrayOf(
    MIGRATION_5_7,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11,
    MIGRATION_11_12,
    MIGRATION_12_13,
    MIGRATION_13_14,
    MIGRATION_14_15,
    MIGRATION_15_16,
)

/*
 * 6 represents legacy database
 * 7 represents new Room database
 */
@Suppress("ClassName")
object MIGRATION_15_16 : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE feeds ADD COLUMN currently_syncing INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

@Suppress("ClassName")
object MIGRATION_14_15 : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE feeds ADD COLUMN alternate_id INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

@Suppress("ClassName")
object MIGRATION_13_14 : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
                """
            ALTER TABLE feeds ADD COLUMN open_articles_with TEXT NOT NULL DEFAULT ''
            """.trimIndent()
        )
    }
}

@Suppress("ClassName")
object MIGRATION_12_13 : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE feeds ADD COLUMN fulltext_by_default INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

@Suppress("ClassName")
object MIGRATION_11_12 : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE feed_items ADD COLUMN primary_sort_time INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

@Suppress("ClassName")
object MIGRATION_10_11 : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE feed_items ADD COLUMN first_synced_time INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

@Suppress("ClassName")
object MIGRATION_9_10 : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `feed_items_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `guid` TEXT NOT NULL, `title` TEXT NOT NULL, `plain_title` TEXT NOT NULL, `plain_snippet` TEXT NOT NULL, `image_url` TEXT, `enclosure_link` TEXT, `author` TEXT, `pub_date` TEXT, `link` TEXT, `unread` INTEGER NOT NULL, `notified` INTEGER NOT NULL, `feed_id` INTEGER, FOREIGN KEY(`feed_id`) REFERENCES `feeds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )
            """.trimIndent()
        )

        database.execSQL(
            """
            INSERT INTO `feed_items_new` (`id`, `guid`, `title`, `plain_title`, `plain_snippet`, `image_url`, `enclosure_link`, `author`, `pub_date`, `link`, `unread`, `notified`, `feed_id`)
            SELECT `id`, `guid`, `title`, `plain_title`, `plain_snippet`, `image_url`, `enclosure_link`, `author`, `pub_date`, `link`, `unread`, `notified`, `feed_id` FROM `feed_items`
            """.trimIndent()
        )

        // Iterate over all items using the minimum query. Also restrict the text field to
        // 1 MB which should be safe enough considering the window size is 2MB large.
        database.query(
            """
            SELECT id, substr(description,0,1000000) FROM feed_items
            """.trimIndent()
        )?.use { cursor ->
            cursor.forEach {
                val feedItemId = cursor.getLong(0)
                val description = cursor.getString(1)

                blobOutputStream(feedItemId, FeederApplication.staticFilesDir).bufferedWriter().use {
                    it.write(description)
                }
            }
        }

        database.execSQL(
            """
            DROP TABLE feed_items
            """.trimIndent()
        )

        database.execSQL(
            """
            ALTER TABLE feed_items_new RENAME TO feed_items
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_feed_items_guid_feed_id` ON `feed_items` (`guid`, `feed_id`)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_feed_items_feed_id` ON `feed_items` (`feed_id`)
            """.trimIndent()
        )

        // And reset response hash on all feeds to trigger parsing of results next sync so items
        // are written disk (in case migration substring was too short)
        database.execSQL(
            """
            UPDATE `feeds` SET `response_hash` = 0
            """.trimIndent()
        )
    }
}

object MIGRATION_8_9 : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE feeds ADD COLUMN response_hash INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

object MIGRATION_7_8 : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE feeds ADD COLUMN last_sync INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

object MIGRATION_6_7 : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        legacyMigration(database, 6)
    }
}

object MIGRATION_5_7 : Migration(5, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        legacyMigration(database, 5)
    }
}

private fun legacyMigration(database: SupportSQLiteDatabase, version: Int) {
    // Create new tables and indices
    // Feeds
    database.execSQL(
        """
            CREATE TABLE IF NOT EXISTS `feeds` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `custom_title` TEXT NOT NULL, `url` TEXT NOT NULL, `tag` TEXT NOT NULL, `notify` INTEGER NOT NULL, `image_url` TEXT)
        """.trimIndent()
    )
    database.execSQL(
        """
            CREATE UNIQUE INDEX `index_Feed_url` ON `feeds` (`url`)
        """.trimIndent()
    )
    database.execSQL(
        """
            CREATE UNIQUE INDEX `index_Feed_id_url_title` ON `feeds` (`id`, `url`, `title`)
        """.trimIndent()
    )

    // Items
    database.execSQL(
        """
            CREATE TABLE IF NOT EXISTS `feed_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `guid` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `plain_title` TEXT NOT NULL, `plain_snippet` TEXT NOT NULL, `image_url` TEXT, `enclosure_link` TEXT, `author` TEXT, `pub_date` TEXT, `link` TEXT, `unread` INTEGER NOT NULL, `notified` INTEGER NOT NULL, `feed_id` INTEGER, FOREIGN KEY(`feed_id`) REFERENCES `feeds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )
        """.trimIndent()
    )
    database.execSQL(
        """
            CREATE UNIQUE INDEX `index_feed_item_guid_feed_id` ON `feed_items` (`guid`, `feed_id`)
        """.trimIndent()
    )
    database.execSQL(
        """
             CREATE  INDEX `index_feed_item_feed_id` ON `feed_items` (`feed_id`)
        """.trimIndent()
    )

    // Migrate to new tables
    database.query(
        """
            SELECT _id, title, url, tag, customtitle, notify ${if (version == 6) ", imageUrl" else ""}
            FROM Feed
        """.trimIndent()
    )?.use { cursor ->
        cursor.forEach { _ ->
            val oldFeedId = cursor.getLong(0)

            val newFeedId = database.insert(
                "feeds",
                SQLiteDatabase.CONFLICT_FAIL,
                contentValues {
                    setString("title" to cursor.getString(1))
                    setString("custom_title" to cursor.getString(4))
                    setString("url" to cursor.getString(2))
                    setString("tag" to cursor.getString(3))
                    setInt("notify" to cursor.getInt(5))
                    if (version == 6) {
                        setString("image_url" to cursor.getString(6))
                    }
                }
            )

            database.query(
                """
                    SELECT title, description, plainTitle, plainSnippet, imageUrl, link, author,
                           pubdate, unread, feed, enclosureLink, notified, guid
                    FROM FeedItem
                    WHERE feed = $oldFeedId
                """.trimIndent()
            )?.use { cursor ->
                database.inTransaction {
                    cursor.forEach { _ ->
                        database.insert(
                            "feed_items",
                            SQLiteDatabase.CONFLICT_FAIL,
                            contentValues {
                                setString("guid" to cursor.getString(12))
                                setString("title" to cursor.getString(0))
                                setString("description" to cursor.getString(1))
                                setString("plain_title" to cursor.getString(2))
                                setString("plain_snippet" to cursor.getString(3))
                                setString("image_url" to cursor.getString(4))
                                setString("enclosure_link" to cursor.getString(10))
                                setString("author" to cursor.getString(6))
                                setString("pub_date" to cursor.getString(7))
                                setString("link" to cursor.getString(5))
                                setInt("unread" to cursor.getInt(8))
                                setInt("notified" to cursor.getInt(11))
                                setLong("feed_id" to newFeedId)
                            }
                        )
                    }
                }
            }
        }
    }

    // Remove all legacy content
    database.execSQL("DROP TRIGGER IF EXISTS trigger_tag_updater")

    database.execSQL("DROP VIEW IF EXISTS WithUnreadCount")
    database.execSQL("DROP VIEW IF EXISTS TagsWithUnreadCount")

    database.execSQL("DROP TABLE IF EXISTS Feed")
    database.execSQL("DROP TABLE IF EXISTS FeedItem")
}

fun SupportSQLiteDatabase.inTransaction(init: (SupportSQLiteDatabase) -> Unit) {
    beginTransaction()
    try {
        init(this)
        setTransactionSuccessful()
    } finally {
        endTransaction()
    }
}
