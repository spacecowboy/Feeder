package com.nononsenseapps.feeder.ui

import android.app.Activity
import android.app.LoaderManager
import android.content.*
import android.database.Cursor
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.text.Html.fromHtml
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckedTextView
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.FeedSQL
import com.nononsenseapps.feeder.db.Util
import com.nononsenseapps.feeder.model.OPMLContenProvider
import com.nononsenseapps.feeder.model.OPMLParser
import com.nononsenseapps.feeder.model.RssSyncAdapter
import com.nononsenseapps.feeder.model.opml.writeOutputStream
import com.nononsenseapps.feeder.util.*

const private val EXPORT_OPML_CODE = 101
const private val IMPORT_OPML_CODE = 102

class FeedActivity : BaseActivity() {
    private val fragmentTag = "single_pane"
    private val defaultLoaderId = 2523

    private var fragment: Fragment? = null
    lateinit private var emptyView: View

    private val syncReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Load first feed if nothing is showing (could have been empty and now content has been loaded)
            when (intent.action) {
                RssSyncAdapter.SYNC_BROADCAST -> loadFirstFeedInDB(false)
                RssSyncAdapter.FEED_ADDED_BROADCAST -> {
                    if (fragment == null && intent.getLongExtra(FeedSQL.COL_ID, -1) > 0) {
                        onNavigationDrawerItemSelected(intent.getLongExtra(FeedSQL.COL_ID, -1), "", "", null)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        initializeActionBar()
        overridePendingTransition(0, 0)

        if (savedInstanceState == null) {
            fragment = defaultFragment()
            if (fragment == null) {
                loadFirstFeedInDB(false)
            } else {
                supportFragmentManager.beginTransaction().add(R.id.container, fragment, fragmentTag).commit()
            }
        } else {
            fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        }

        // Empty view
        emptyView = findViewById(android.R.id.empty)
        emptyView.visibility = if (fragment == null) View.VISIBLE else View.GONE

        val emptyAddFeed = findViewById(R.id.empty_add_feed) as TextView
        emptyAddFeed.text = fromHtml(getString(R.string.empty_no_feeds_add))
        emptyAddFeed.setOnClickListener {
            startActivity(Intent(this@FeedActivity, EditFeedActivity::class.java))
        }

        // Night mode
        val nightCheck = findViewById(R.id.nightcheck) as CheckedTextView
        nightCheck.isChecked = PrefUtils.isNightMode(this)
        nightCheck.setOnClickListener {
            // Toggle icon first
            nightCheck.toggle()
            // Toggle prefs second
            PrefUtils.setNightMode(this@FeedActivity, nightCheck.isChecked)
            // Change background
            setNightBackground()
        }
    }

    private fun defaultFragment(): Fragment? {
        val tag = PrefUtils.getLastOpenFeedTag(this)
        val id = PrefUtils.getLastOpenFeedId(this)

        // Will load title and url in fragment
        if (tag != null || id > 0) {
            return FeedFragment.newInstance(id, "", "", tag)
        } else {
            return null
        }
    }

    val firstFeedLoader = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            return CursorLoader(this@FeedActivity, FeedSQL.URI_FEEDS, FeedSQL.FIELDS, null, null, null)
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
            when (loader.id) {
                defaultLoaderId -> {
                    if (data != null && data.moveToFirst()) {
                        val feed = FeedSQL(data)
                        onNavigationDrawerItemSelected(feed.id, feed.title, feed.url, feed.tag)
                    }
                    loaderManager.destroyLoader(defaultLoaderId)
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>?) {}
    }

    fun loadFirstFeedInDB(overrideCurrent: Boolean = false): Unit {
        if (fragment == null || overrideCurrent) {
            loaderManager.restartLoader(defaultLoaderId, Bundle.EMPTY, firstFeedLoader)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        registerHideableHeaderView(findViewById(R.id.headerbar))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.feed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                startActivity(Intent(this, EditFeedActivity::class.java))
                true
            }
            R.id.action_opml_export -> {
                // Choose file, then export
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.type = "text/opml"
                intent.putExtra(Intent.EXTRA_TITLE, "feeder.opml")
                startActivityForResult(intent, EXPORT_OPML_CODE)
                true
            }
            R.id.action_opml_import -> {
                // Choose file
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_MIME_TYPES,
                        Util.ToStringArray("text/plain", "text/xml", "text/opml", "*/*"))
                startActivityForResult(intent, IMPORT_OPML_CODE)
                true
            }
            R.id.action_debug_log -> {
                startActivity(Intent(this, DebugLogActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getSelfNavDrawerItem(): Int {
        return 0
    }

    override fun onNavigationDrawerItemSelected(id: Long, title: String?, url: String?, tag: String?) {
        // update the main content by replacing fragments
        emptyView.visibility = View.GONE
        fragment = FeedFragment.newInstance(id, title, url, tag)
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragmentTag).commit()
        // Remember choice in future
        PrefUtils.setLastOpenFeed(this, id, tag)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(syncReceiver, IntentFilter(RssSyncAdapter.SYNC_BROADCAST))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        // TODO avoid UI-thread
        when (requestCode) {
            EXPORT_OPML_CODE -> {
                if (data != null) {
                    val uri = data.data
                    try {
                        writeOutputStream(contentResolver.openOutputStream(uri), tags(), feedsWithTags())
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
            IMPORT_OPML_CODE -> {
                if (data != null) {
                    val uri = data.data
                    try {
                        val parser = OPMLParser(OPMLContenProvider(this))
                        contentResolver.openInputStream(uri).use {
                            parser.parseInputStream(it)
                        }
                        contentResolver.notifyAllUris()
                        contentResolver.requestFeedSync()
                    } catch (e: Throwable) {
                        // TODO tell user about error
                    }
                }
            }
        }
    }

    private fun tags(): Iterable<String?> {
        val tags = ArrayList<String?>()

        contentResolver.queryTagsWithCounts(columns = listOf(FeedSQL.COL_TAG)) {
            while (it.moveToNext()) {
                tags.add(it.getString(FeedSQL.COL_TAG))
            }
        }

        return tags
    }

    private fun feedsWithTags(): (String?) -> Iterable<FeedSQL> {
        return { tag ->
            val feeds = ArrayList<FeedSQL>()

            contentResolver.queryFeeds(where = "${FeedSQL.COL_TAG} IS ?", params = listOf(tag ?: "")) {
                while (it.moveToNext()) {
                    feeds.add(FeedSQL(it))
                }
            }

            feeds
        }
    }
}
