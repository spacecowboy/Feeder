package com.nononsenseapps.feeder.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.nononsenseapps.feeder.model.OPMLParserToDatabase
import com.nononsenseapps.feeder.model.opml.OpmlParser
import com.nononsenseapps.feeder.model.opml.writeFile
import com.nononsenseapps.feeder.util.firstOrNull
import com.nononsenseapps.feeder.util.forEach
import com.nononsenseapps.feeder.util.getString
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import java.io.File
import java.util.*

val DATABASE_VERSION = 5
private val DATABASE_NAME = "rssDatabase"

class DatabaseHandler constructor(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val context = context.applicationContext

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_FEED_TABLE)
        db.execSQL(CREATE_FEED_ITEM_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            // Same for oldVersion < 4
            if (oldVersion < 5) {
                createViewsAndTriggers(db)
                // Export to OPML
                val tempFile = File(context.externalCacheDir, "upgrade.opml")

                writeFile(tempFile.absolutePath,
                        tags(db), feedsWithTag(db))

                // Delete database
                deleteEverything(db)

                // Create database
                onCreate(db)

                // Import OMPL
                val parser = OpmlParser(OPMLDatabaseHandler(db))
                parser.parseFile(tempFile.absolutePath)
            }
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }

    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            // Enable foreign key constraints
            db.setForeignKeyConstraintsEnabled(true)
        }
        createViewsAndTriggers(db)
    }

    private fun deleteEverything(db: SQLiteDatabase) {
        db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_NAME)

        db.execSQL("DROP VIEW IF EXISTS " + VIEWCOUNT_NAME)
        db.execSQL("DROP VIEW IF EXISTS " + VIEWTAGS_NAME)

        db.execSQL("DROP TABLE IF EXISTS " + FEED_TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + FEED_ITEM_TABLE_NAME)
    }

    private fun createViewsAndTriggers(db: SQLiteDatabase) {
        // Create triggers
        db.execSQL(CREATE_TAG_TRIGGER)
        // Create views if not exists
        db.execSQL(CREATE_COUNT_VIEW)
        db.execSQL(CREATE_TAGS_VIEW)
    }

    private fun tags(db: SQLiteDatabase): List<String?> {
        val tags = ArrayList<String?>()

        db.query(VIEWTAGS_NAME, arrayOf(COL_TAG), null, null, null, null, null).use { cursor ->
            cursor.forEach {
                tags.add(it.getString(COL_TAG))
            }
        }

        return tags
    }

    private fun feedsWithTag(db: SQLiteDatabase): (String?) -> List<FeedSQL> {
        return { tag ->
            val feeds = ArrayList<FeedSQL>()

            db.query(FEED_TABLE_NAME, FEED_FIELDS, "$COL_TAG IS ?",
                    arrayOf(tag ?: ""), null, null, null).use { cursor ->
                cursor.forEach {
                    feeds.add(it.asFeed())
                }
            }

            feeds
        }
    }
}

class OPMLDatabaseHandler(val db: SQLiteDatabase) : OPMLParserToDatabase {
    override fun getFeed(url: String): FeedSQL {
        db.query(FEED_TABLE_NAME,
                FEED_FIELDS, "$COL_URL IS ?",
                arrayOf(url), null, null, null).use { cursor ->
            return cursor.firstOrNull()?.asFeed() ?: FeedSQL(url = sloppyLinkToStrictURL(url))
        }
    }

    override fun saveFeed(feed: FeedSQL) {
        if (feed.id < 1) {
            db.insert(FEED_TABLE_NAME, null, feed.asContentValues())
        } else {
            db.update(FEED_TABLE_NAME, feed.asContentValues(), "$COL_ID IS ?", arrayOf(feed.id.toString()))
        }
    }
}
