package com.nononsenseapps.feeder.db.room

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Database(entities = [Feed::class, FeedItem::class], version = 7)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun feedItemDao(): FeedItemDao

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_5_7, MIGRATION_6_7)
                    .build()
        }
    }
}

@Suppress("ClassName")
/**
 * 6 represents legacy database
 * 7 represents new Room database
 */
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
    database.execSQL("""
            CREATE TABLE IF NOT EXISTS `feeds` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `custom_title` TEXT NOT NULL, `url` TEXT NOT NULL, `tag` TEXT NOT NULL, `notify` INTEGER NOT NULL, `image_url` TEXT)
        """.trimIndent())
    database.execSQL("""
            CREATE UNIQUE INDEX `index_Feed_url` ON `feeds` (`url`)
        """.trimIndent())
    database.execSQL("""
            CREATE UNIQUE INDEX `index_Feed_id_url_title` ON `feeds` (`id`, `url`, `title`)
        """.trimIndent())

    // Items
    database.execSQL("""
            CREATE TABLE IF NOT EXISTS `feed_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `guid` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `plain_title` TEXT NOT NULL, `plain_snippet` TEXT NOT NULL, `image_url` TEXT, `enclosure_link` TEXT, `author` TEXT, `pub_date` TEXT, `link` TEXT, `unread` INTEGER NOT NULL, `notified` INTEGER NOT NULL, `feed_id` INTEGER, FOREIGN KEY(`feed_id`) REFERENCES `feeds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )
            """.trimIndent())
    database.execSQL("""
            CREATE UNIQUE INDEX `index_feed_item_guid_feed_id` ON `feed_items` (`guid`, `feed_id`)
        """.trimIndent())
    database.execSQL("""
             CREATE  INDEX `index_feed_item_feed_id` ON `feed_items` (`feed_id`)
        """.trimIndent())

    // Migrate to new tables
    database.query("""
            SELECT _id, title, url, tag, customtitle, notify ${if (version == 6) ", imageUrl" else ""}
            FROM Feed
        """.trimIndent())?.use { cursor ->
        cursor.forEach { _ ->
            val oldFeedId = cursor.getLong(0)

            val newFeedId = database.insert("feeds",
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
                    })

            database.query("""
                    SELECT title, description, plainTitle, plainSnippet, imageUrl, link, author,
                           pubdate, unread, feed, enclosureLink, notified, guid
                    FROM FeedItem
                    WHERE feed = $oldFeedId
                """.trimIndent())?.use { cursor ->
                database.inTransaction {
                    cursor.forEach { _ ->
                        database.insert("feed_items",
                                SQLiteDatabase.CONFLICT_FAIL,
                                contentValues {
                                    setString("guid" to cursor.getString(12))
                                    setString("title" to cursor.getString(0))
                                    setString("description" to cursor.getString(1))
                                    setString("plain_title" to cursor.getString(2))
                                    setString("plain_snippet" to cursor.getString(3))
                                    setString("image_url" to cursor.getString(4))
                                    setString("enclosure_link" to cursor.getString(10))
                                    setString("author" to  cursor.getString(6))
                                    setString("pub_date" to cursor.getString(7))
                                    setString("link" to  cursor.getString(5))
                                    setInt("unread" to cursor.getInt(8))
                                    setInt("notified" to cursor.getInt(11))
                                    setLong("feed_id" to newFeedId)
                                })
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