package com.nononsenseapps.feeder.db.room

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.InstrumentationRegistry
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import com.nononsenseapps.feeder.db.legacy.COL_AUTHOR
import com.nononsenseapps.feeder.db.legacy.COL_CUSTOM_TITLE
import com.nononsenseapps.feeder.db.legacy.COL_DESCRIPTION
import com.nononsenseapps.feeder.db.legacy.COL_ENCLOSURELINK
import com.nononsenseapps.feeder.db.legacy.COL_FEED
import com.nononsenseapps.feeder.db.legacy.COL_FEEDTITLE
import com.nononsenseapps.feeder.db.legacy.COL_FEEDURL
import com.nononsenseapps.feeder.db.legacy.COL_GUID
import com.nononsenseapps.feeder.db.legacy.COL_ID
import com.nononsenseapps.feeder.db.legacy.COL_IMAGEURL
import com.nononsenseapps.feeder.db.legacy.COL_LINK
import com.nononsenseapps.feeder.db.legacy.COL_NOTIFIED
import com.nononsenseapps.feeder.db.legacy.COL_NOTIFY
import com.nononsenseapps.feeder.db.legacy.COL_PLAINSNIPPET
import com.nononsenseapps.feeder.db.legacy.COL_PLAINTITLE
import com.nononsenseapps.feeder.db.legacy.COL_PUBDATE
import com.nononsenseapps.feeder.db.legacy.COL_TAG
import com.nononsenseapps.feeder.db.legacy.COL_TITLE
import com.nononsenseapps.feeder.db.legacy.COL_UNREAD
import com.nononsenseapps.feeder.db.legacy.COL_URL
import com.nononsenseapps.feeder.db.legacy.CREATE_FEED_ITEM_TABLE
import com.nononsenseapps.feeder.db.legacy.CREATE_TAGS_VIEW
import com.nononsenseapps.feeder.db.legacy.CREATE_TAG_TRIGGER
import com.nononsenseapps.feeder.db.legacy.FEED_ITEM_TABLE_NAME
import com.nononsenseapps.feeder.db.legacy.FEED_TABLE_NAME
import com.nononsenseapps.feeder.db.legacy.LegacyDatabaseHandler
import com.nononsenseapps.feeder.util.contentValues
import com.nononsenseapps.feeder.util.setInt
import com.nononsenseapps.feeder.util.setLong
import com.nononsenseapps.feeder.util.setString
import org.joda.time.DateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
@LargeTest
class MigrationFromLegacy5ToLatest {

    @Rule
    @JvmField
    val testHelper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory())

    private val testDbName = "TestingDatabase"

    private val legacyDb: LegacyDatabaseHandler
        get() = LegacyDatabaseHandler(context = InstrumentationRegistry.getTargetContext(),
                name = testDbName,
                version = 5)

    private val roomDb: AppDatabase
        get() =
            Room.databaseBuilder(InstrumentationRegistry.getTargetContext(),
                    AppDatabase::class.java,
                    testDbName)
                    .addMigrations(MIGRATION_5_7, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .build().also { testHelper.closeWhenFinished(it) }

    @Before
    fun setup() {
        legacyDb.writableDatabase.use { db ->
            db.execSQL("""
                CREATE TABLE $FEED_TABLE_NAME (
                  $COL_ID INTEGER PRIMARY KEY,
                  $COL_TITLE TEXT NOT NULL,
                  $COL_CUSTOM_TITLE TEXT NOT NULL,
                  $COL_URL TEXT NOT NULL,
                  $COL_TAG TEXT NOT NULL DEFAULT '',
                  $COL_NOTIFY INTEGER NOT NULL DEFAULT 0,
                  UNIQUE($COL_URL) ON CONFLICT REPLACE
                )""")
            db.execSQL(CREATE_FEED_ITEM_TABLE)
            db.execSQL(CREATE_TAG_TRIGGER)
            db.execSQL("""
                CREATE TEMP VIEW IF NOT EXISTS WithUnreadCount
                AS SELECT $COL_ID, $COL_TITLE, $COL_URL, $COL_TAG, $COL_CUSTOM_TITLE, $COL_NOTIFY, "unreadcount"
                   FROM $FEED_TABLE_NAME
                   LEFT JOIN (SELECT COUNT(1) AS ${"unreadcount"}, $COL_FEED
                     FROM $FEED_ITEM_TABLE_NAME
                     WHERE $COL_UNREAD IS 1
                     GROUP BY $COL_FEED)
                   ON $FEED_TABLE_NAME.$COL_ID = $COL_FEED""")
            db.execSQL(CREATE_TAGS_VIEW)

            // Bare minimum non-null feeds
            val idA = db.insert(FEED_TABLE_NAME, null, contentValues {
                setString(COL_TITLE to "feedA")
                setString(COL_CUSTOM_TITLE to "feedACustom")
                setString(COL_URL to "https://feedA")
                setString(COL_TAG to "")
            })

            // All fields filled
            val idB = db.insert(FEED_TABLE_NAME, null, contentValues {
                setString(COL_TITLE to "feedB")
                setString(COL_CUSTOM_TITLE to "feedBCustom")
                setString(COL_URL to "https://feedB")
                setString(COL_TAG to "tag")
                setInt(COL_NOTIFY to 1)
            })

            IntRange(0, 1).forEach { index ->
                db.insert(FEED_ITEM_TABLE_NAME, null, contentValues {
                    setLong(COL_FEED to idA)
                    setString(COL_GUID to "guid$index")
                    setString(COL_TITLE to "title$index")
                    setString(COL_DESCRIPTION to "desc$index")
                    setString(COL_PLAINTITLE to "plain$index")
                    setString(COL_PLAINSNIPPET to "snippet$index")
                    setString(COL_FEEDTITLE to "feedA")
                    setString(COL_FEEDURL to "https://feedA")
                    setString(COL_TAG to "")
                })

                db.insert(FEED_ITEM_TABLE_NAME, null, contentValues {
                    setLong(COL_FEED to idB)
                    setString(COL_GUID to "guid$index")
                    setString(COL_TITLE to "title$index")
                    setString(COL_DESCRIPTION to "desc$index")
                    setString(COL_PLAINTITLE to "plain$index")
                    setString(COL_PLAINSNIPPET to "snippet$index")
                    setString(COL_FEEDTITLE to "feedB")
                    setString(COL_FEEDURL to "https://feedB")
                    setString(COL_TAG to "tag")
                    setInt(COL_NOTIFIED to 1)
                    setInt(COL_UNREAD to 0)
                    setString(COL_AUTHOR to "author$index")
                    setString(COL_ENCLOSURELINK to "https://enclosure$index")
                    setString(COL_IMAGEURL to "https://image$index")
                    setString(COL_PUBDATE to DateTime(2018, 2, 3, 4, 5).toString())
                    setString(COL_LINK to "https://link$index")
                })
            }
        }
    }

    @After
    fun tearDown() {
        assertTrue(InstrumentationRegistry.getTargetContext().deleteDatabase(testDbName))
    }

    @Test
    fun legacyMigrationTo7MinimalFeed() {
        testHelper.runMigrationsAndValidate(testDbName, 7, true,
                MIGRATION_5_7, MIGRATION_7_8)

        roomDb.let { db ->
            val feeds = db.feedDao().loadFeeds()

            assertEquals("Wrong number of feeds", 2, feeds.size)

            val feedA = feeds[0]

            assertEquals("feedA", feedA.title)
            assertEquals("feedACustom", feedA.customTitle)
            assertEquals(URL("https://feedA"), feedA.url)
            assertEquals("", feedA.tag)
            assertEquals(0, feedA.lastSync)
            assertFalse(feedA.notify)
            assertNull(feedA.imageUrl)
        }
    }

    @Test
    fun legacyMigrationTo7CompleteFeed() {
        testHelper.runMigrationsAndValidate(testDbName, 7, true,
                MIGRATION_5_7, MIGRATION_7_8)

        roomDb.let { db ->
            val feeds = db.feedDao().loadFeeds()

            assertEquals("Wrong number of feeds", 2, feeds.size)

            val feedB = feeds[1]

            assertEquals("feedB", feedB.title)
            assertEquals("feedBCustom", feedB.customTitle)
            assertEquals(URL("https://feedB"), feedB.url)
            assertEquals("tag", feedB.tag)
            assertEquals(0, feedB.lastSync)
            assertTrue(feedB.notify)
            assertNull(feedB.imageUrl)
        }
    }

    @Test
    fun legacyMigrationTo7MinimalFeedItem() {
        testHelper.runMigrationsAndValidate(testDbName, 7, true,
                MIGRATION_5_7, MIGRATION_7_8)

        roomDb.let { db ->
            val feed = db.feedDao().loadFeeds()[0]
            assertEquals("feedA", feed.title)
            val items = db.feedItemDao().loadFeedItemsInFeed(feedId = feed.id)

            assertEquals(2, items.size)

            items.forEachIndexed { index, it ->
                assertEquals(feed.id, it.feedId)
                assertEquals("guid$index", it.guid)
                assertEquals("title$index", it.title)
                assertEquals("desc$index", it.description)
                assertEquals("plain$index", it.plainTitle)
                assertEquals("snippet$index", it.plainSnippet)
                assertTrue(it.unread)
                assertNull(it.author)
                assertNull(it.enclosureLink)
                assertNull(it.imageUrl)
                assertNull(it.pubDate)
                assertNull(it.link)
                assertFalse(it.notified)
            }
        }
    }

    @Test
    fun legacyMigrationTo7CompleteFeedItem() {
        testHelper.runMigrationsAndValidate(testDbName, 7, true,
                MIGRATION_5_7, MIGRATION_7_8)

        roomDb.let { db ->
            val feed = db.feedDao().loadFeeds()[1]
            assertEquals("feedB", feed.title)
            val items = db.feedItemDao().loadFeedItemsInFeed(feedId = feed.id)

            assertEquals(2, items.size)

            items.forEachIndexed { index, it ->
                assertEquals(feed.id, it.feedId)
                assertEquals("guid$index", it.guid)
                assertEquals("title$index", it.title)
                assertEquals("desc$index", it.description)
                assertEquals("plain$index", it.plainTitle)
                assertEquals("snippet$index", it.plainSnippet)
                assertFalse(it.unread)
                assertEquals("author$index", it.author)
                assertEquals("https://enclosure$index", it.enclosureLink)
                assertEquals("https://image$index", it.imageUrl)
                assertEquals(DateTime(2018, 2, 3, 4, 5).toString(), it.pubDate.toString())
                assertEquals("https://link$index", it.link)
                assertTrue(it.notified)
            }
        }
    }
}
