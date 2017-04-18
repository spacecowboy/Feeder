package com.nononsenseapps.feeder.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.nononsenseapps.feeder.model.OPMLDatabaseHandler
import com.nononsenseapps.feeder.model.OPMLParser
import com.nononsenseapps.feeder.model.opml.writeFile
import com.nononsenseapps.feeder.util.getString
import java.io.File

private val DATABASE_VERSION = 4
private val DATABASE_NAME = "rssDatabase"

class DatabaseHandler private constructor(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object Singleton {
        private var instance: DatabaseHandler? = null

        @Synchronized
        fun getInstance(context: Context): DatabaseHandler {
            if (instance == null) {
                instance = DatabaseHandler(context)
            }
            return instance!!
        }
    }

    private val context = context.applicationContext

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(FeedSQL.CREATE_TABLE)
        db.execSQL(FeedItemSQL.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
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
            val parser = OPMLParser(OPMLDatabaseHandler(db))
            parser.parseFile(tempFile.absolutePath)
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
        db.execSQL("DROP TRIGGER IF EXISTS " + FeedItemSQL.TRIGGER_NAME)

        db.execSQL("DROP VIEW IF EXISTS " + FeedSQL.VIEWCOUNT_NAME)
        db.execSQL("DROP VIEW IF EXISTS " + FeedSQL.VIEWTAGS_NAME)

        db.execSQL("DROP TABLE IF EXISTS " + FeedSQL.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + FeedItemSQL.TABLE_NAME)
    }

    private fun createViewsAndTriggers(db: SQLiteDatabase) {
        // Create triggers
        db.execSQL(FeedItemSQL.CREATE_TAG_TRIGGER)
        // Create views if not exists
        db.execSQL(FeedSQL.CREATE_COUNT_VIEW)
        db.execSQL(FeedSQL.CREATE_TAGS_VIEW)
    }

    private fun tags(db: SQLiteDatabase): List<String?> {
        val tags = ArrayList<String?>()

        db.query(FeedSQL.VIEWTAGS_NAME, arrayOf(FeedSQL.COL_TAG), null, null, null, null, null).use {
            while (it.moveToNext()) {
                tags.add(it.getString(FeedSQL.COL_TAG))
            }
        }

        return tags
    }

    private fun feedsWithTag(db: SQLiteDatabase): (String?) -> List<FeedSQL> {
        return { tag ->
            val feeds = ArrayList<FeedSQL>()

            db.query(FeedSQL.TABLE_NAME, FeedSQL.FIELDS, "${FeedSQL.COL_TAG} IS ?",
                    arrayOf(tag ?: ""), null, null, null).use {
                while (it.moveToNext()) {
                    feeds.add(FeedSQL(it))
                }
            }

            feeds
        }
    }
}

