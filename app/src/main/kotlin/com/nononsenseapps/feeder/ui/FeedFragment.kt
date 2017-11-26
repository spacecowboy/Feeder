/*
 * Copyright (c) 2017 Jonas Kalderstam.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.feeder.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.COL_FEED
import com.nononsenseapps.feeder.db.COL_NOTIFY
import com.nononsenseapps.feeder.db.COL_PUBDATE
import com.nononsenseapps.feeder.db.COL_TAG
import com.nononsenseapps.feeder.db.COL_UNREAD
import com.nononsenseapps.feeder.db.FEED_FIELDS
import com.nononsenseapps.feeder.db.FEED_ITEM_FIELDS_FOR_LIST
import com.nononsenseapps.feeder.db.FeedItemSQL
import com.nononsenseapps.feeder.db.QUERY_PARAM_LIMIT
import com.nononsenseapps.feeder.db.RssDatabaseService
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.Util
import com.nononsenseapps.feeder.db.asFeed
import com.nononsenseapps.feeder.model.RssSyncAdapter
import com.nononsenseapps.feeder.util.FeedItemDeltaCursorLoader
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.TabletUtils
import com.nononsenseapps.feeder.util.addDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.bundle
import com.nononsenseapps.feeder.util.notifyAllUris
import com.nononsenseapps.feeder.util.removeDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.reportShortcutToFeedUsed
import com.nononsenseapps.feeder.util.requestFeedSync
import com.nononsenseapps.feeder.util.setLong
import com.nononsenseapps.feeder.util.setString
import org.joda.time.format.DateTimeFormat
import java.util.*

const val FEEDITEMS_LOADER = 1
const val FEED_LOADER = 2
const val FEED_SETTINGS_LOADER = 3

const val ARG_FEED_ID = "feed_id"
const val ARG_FEED_TITLE = "feed_title"
const val ARG_FEED_URL = "feed_url"
const val ARG_FEED_TAG = "feed_tag"
// Filter for database loader
const val ONLY_UNREAD = COL_UNREAD + " IS 1 "
const val AND_UNREAD = " AND " + ONLY_UNREAD

class FeedFragment : Fragment(), LoaderManager.LoaderCallbacks<Any> {

    private val TAG = "FeedFragment"

    private var adapter: FeedAdapter? = null
    private var recyclerView: RecyclerView? = null
    internal var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var emptyView: View? = null
    private var emptyAddFeed: View? = null
    private var emptyOpenFeeds: View? = null

    private val syncReceiver: BroadcastReceiver

    private var id: Long = -1
    private var title: String? = ""
    private var url: String? = ""
    private var feedTag: String? = ""
    private var customTitle = ""
    private var layoutManager: LinearLayoutManager? = null
    private var checkAllButton: View? = null
    private var notify = 0
    private var notifyCheck: CheckedTextView? = null
    internal var actionMode: ActionMode? = null
    internal var selectedItem: FeedItemSQL? = null
    internal val actionModeCallback = object : ActionMode.Callback {

        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.contextmenu_feedfragment, menu)

            // Show/Hide enclosure
            menu.findItem(R.id.action_open_enclosure).isVisible = selectedItem!!.enclosurelink != null
            // Add filename to tooltip
            if (selectedItem!!.enclosurelink != null) {
                val filename = selectedItem!!.enclosureFilename
                if (filename != null) {
                    menu.findItem(R.id.action_open_enclosure).title = filename
                }
            }

            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_open_in_browser -> {
                    // Open in browser
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse(selectedItem!!.link)))
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                R.id.action_open_enclosure -> {
                    // Open enclosure link
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse(selectedItem!!.enclosurelink)))
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                R.id.action_toggle_unread -> {
                    //
                    if (selectedItem!!.unread) {
                        RssDatabaseService.markItemAsRead(activity, selectedItem!!.id)
                    } else {
                        RssDatabaseService.markItemAsUnread(activity, selectedItem!!.id)
                    }
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                else -> return false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            selectedItem = null
        }
    }

    /**
     * @return SQL selection
     */
    protected val loaderSelection: String?
        get() {
            var filter: String? = null
            if (id > 0) {
                filter = COL_FEED + " IS ? "
            } else if (feedTag != null) {
                filter = COL_TAG + " IS ? "
            }

            val onlyUnread = PrefUtils.isShowOnlyUnread(activity!!)
            if (onlyUnread && filter != null) {
                filter += AND_UNREAD
            } else if (onlyUnread) {
                filter = ONLY_UNREAD
            }

            return filter
        }

    /**
     * @return args that match getLoaderSelection
     */
    protected val loaderSelectionArgs: Array<String>?
        get() {
            var args: Array<String>? = null
            if (id > 0) {
                args = Util.LongsToStringArray(this.id)
            } else if (feedTag != null) {
                args = Util.ToStringArray(this.feedTag)
            }

            return args
        }

    init {
        // Listens on sync broadcasts
        syncReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (RssSyncAdapter.SYNC_BROADCAST == intent.action) {
                    onSyncBroadcast(intent.getBooleanExtra(RssSyncAdapter.SYNC_BROADCAST_IS_ACTIVE, false))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            id = arguments!!.getLong(ARG_FEED_ID, -1)
            title = arguments!!.getString(ARG_FEED_TITLE)
            url = arguments!!.getString(ARG_FEED_URL)
            feedTag = arguments!!.getString(ARG_FEED_TAG)

            // It's a feedTag, use as title
            if (id < 1) {
                title = feedTag
            }

            // Special feedTag
            if (id < 1 && (title == null || title!!.isEmpty())) {
                title = getString(R.string.all_feeds)
            }
        }

        setHasOptionsMenu(true)

        // Load some RSS
        loaderManager.restartLoader(FEEDITEMS_LOADER, Bundle.EMPTY, this)
        // Load feed itself if missing info
        if (id > 0 && (title == null || title!!.isEmpty())) {
            loaderManager.restartLoader(FEED_LOADER, Bundle.EMPTY, this)
        } else {
            // Get notification settings at least
            loaderManager.restartLoader(FEED_SETTINGS_LOADER, Bundle.EMPTY, this)
        }

        if (id > 0 && title?.isNotEmpty() == true) {
            // Else it's done in loader finishing
            activity?.addDynamicShortcutToFeed(title!!, id, null)
            // Report shortcut usage
            activity?.reportShortcutToFeedUsed(id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_feed, container, false)
        recyclerView = rootView.findViewById<View>(android.R.id.list) as RecyclerView

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView!!.setHasFixedSize(true)

        if (TabletUtils.isTablet(activity)) {
            val cols = TabletUtils.numberOfFeedColumns(activity)
            // use a grid layout
            layoutManager = GridLayoutManager(activity,
                    cols)
            // I want the padding header to span the entire width
            (layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (HEADERTYPE == adapter!!.getItemViewType(position)) {
                        cols
                    } else {
                        1
                    }
                }
            }
            // TODO, use better dividers such as simple padding
            // I want some dividers
            recyclerView!!.addItemDecoration(DividerColor(activity, DividerColor.VERTICAL_LIST, 0, cols))
            // I want some dividers
            recyclerView!!.addItemDecoration(DividerColor(activity, DividerColor.HORIZONTAL_LIST))
        } else {
            // use a linear layout manager
            layoutManager = LinearLayoutManager(activity)
            // I want some dividers
            //            recyclerView.addItemDecoration(new DividerColor
            //                    (getActivity(), DividerColor.VERTICAL_LIST, 0, 1));
        }
        recyclerView!!.layoutManager = layoutManager

        // Setup swipe refresh
        swipeRefreshLayout = rootView.findViewById<View>(R.id.swiperefresh) as SwipeRefreshLayout

        // The arrow will cycle between these colors (in order)
        swipeRefreshLayout!!.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3)

        swipeRefreshLayout!!.setOnRefreshListener {
            // Sync this specific feed(s)
            when {
                id > 0 -> activity!!.contentResolver.requestFeedSync(id)
                feedTag != null -> activity!!.contentResolver.requestFeedSync(feedTag!!)
                else -> activity!!.contentResolver.requestFeedSync(-1)
            }
        }

        // Set up the empty view
        emptyView = rootView.findViewById(android.R.id.empty)
        emptyAddFeed = emptyView!!.findViewById(R.id.empty_add_feed)
        (emptyAddFeed as TextView).text = android.text.Html.fromHtml(getString(R.string.empty_feed_add))
        emptyOpenFeeds = emptyView!!.findViewById(R.id.empty_open_feeds)
        (emptyOpenFeeds as TextView).text = android.text.Html.fromHtml(getString(R.string.empty_feed_open))

        emptyAddFeed!!.setOnClickListener {
            startActivity(Intent(activity,
                    EditFeedActivity::class.java))
        }

        emptyOpenFeeds!!.setOnClickListener { (activity as BaseActivity).openNavDrawer() }

        // specify an adapter
        adapter = FeedAdapter(activity!!, this)
        recyclerView!!.adapter = adapter

        // check all button
        checkAllButton = rootView.findViewById(R.id.checkall_button)
        checkAllButton!!.setOnClickListener { markAsRead() }

        // So is toolbar buttons
        notifyCheck = activity!!.findViewById<View>(R.id.notifycheck) as CheckedTextView
        notifyCheck!!.setOnClickListener {
            // Remember that we are switching to opposite
            notify = if (notifyCheck!!.isChecked) 0 else 1
            notifyCheck!!.isChecked = notify == 1
            setNotifications(notify == 1)
        }

        return rootView
    }

    private fun onSyncBroadcast(syncing: Boolean) {
        // Background syncs trigger the sync layout
        if (swipeRefreshLayout!!.isRefreshing != syncing) {
            swipeRefreshLayout!!.isRefreshing = syncing
        }
    }

    override fun onActivityCreated(bundle: Bundle?) {
        super.onActivityCreated(bundle)

        val ab = (activity as BaseActivity).supportActionBar
        ab?.setTitle(title)
        (activity as BaseActivity).enableActionBarAutoHide(recyclerView)
    }

    override fun onResume() {
        super.onResume()
        // List might be shorter than screen once item has been read
        (activity as BaseActivity).showActionBar()
        // Listen on broadcasts
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(syncReceiver,
                IntentFilter(RssSyncAdapter.SYNC_BROADCAST))
    }

    override fun onPause() {
        // Unregister receiver
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(syncReceiver)
        swipeRefreshLayout!!.isRefreshing = false
        super.onPause()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.feed_fragment, menu)

        if (id < 1) {
            menu!!.findItem(R.id.action_edit_feed).isVisible = false
            menu.findItem(R.id.action_delete_feed).isVisible = false
            menu.findItem(R.id.action_add_templated).isVisible = false
        }

        // Set toggleable state
        val menuItem = menu!!.findItem(R.id.action_only_unread)
        val onlyUnread = PrefUtils.isShowOnlyUnread(activity!!)
        menuItem.isChecked = onlyUnread
        menuItem.setTitle(if (onlyUnread) R.string.show_unread_items else R.string.show_all_items)
        if (onlyUnread) {
            menuItem.setIcon(R.drawable.ic_action_visibility_off)
        } else {
            menuItem.setIcon(R.drawable.ic_action_visibility)
        }

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setNotifications(on: Boolean) {
        RssDatabaseService.setNotify(activity, on, this.id, this.feedTag)
    }

    private fun markAsRead() {
        // TODO this actually marks all items as read - whereas UI only displays 50 of them
        RssDatabaseService.markFeedAsRead(activity, this.id, this.feedTag)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        val id = menuItem!!.itemId.toLong()
        if (id == R.id.action_sync.toLong()) {
            // Sync all feeds when menu button pressed
            activity!!.contentResolver.requestFeedSync()
            return true
        } else if (id == R.id.action_edit_feed.toLong() && this.id > 0) {
            val i = Intent(activity, EditFeedActivity::class.java)
            // TODO do not animate the back movement here
            i.putExtra(SHOULD_FINISH_BACK, true)
            i.putExtra(_ID, this.id)
            i.putExtra(CUSTOM_TITLE, customTitle)
            i.putExtra(FEED_TITLE, title)
            i.putExtra(TAG, feedTag)
            i.data = Uri.parse(url)
            startActivity(i)
            return true
        } else if (id == R.id.action_add_templated.toLong() && this.id > 0) {
            val i = Intent(activity, EditFeedActivity::class.java)
            // TODO do not animate the back movement here
            i.putExtra(SHOULD_FINISH_BACK, true)
            i.putExtra(TEMPLATE, true)
            i.putExtra(TAG, feedTag)
            i.data = Uri.parse(url)
            startActivity(i)
            return true
        } else if (id == R.id.action_delete_feed.toLong() && this.id > 0) {
            activity!!.contentResolver
                    .delete(URI_FEEDS, Util.WHEREIDIS,
                            Util.LongsToStringArray(this.id))
            activity!!.contentResolver.notifyAllUris()

            // Remove from shortcuts
            activity?.removeDynamicShortcutToFeed(this.id)

            // Tell activity to open another fragment
            (activity as FeedActivity).showAllFeeds(true)
            return true
        } else if (id == R.id.action_only_unread.toLong()) {
            val onlyUnread = !menuItem.isChecked
            PrefUtils.setPrefShowOnlyUnread(activity!!, onlyUnread)
            menuItem.isChecked = onlyUnread
            if (onlyUnread) {
                menuItem.setIcon(R.drawable.ic_action_visibility_off)
            } else {
                menuItem.setIcon(R.drawable.ic_action_visibility)
            }

            menuItem.setTitle(if (onlyUnread) R.string.show_unread_items else R.string.show_all_items)
            //getActivity().invalidateOptionsMenu();
            // Restart loader
            loaderManager.restartLoader(FEEDITEMS_LOADER, Bundle(), this)
            return true
        } else {
            return super.onOptionsItemSelected(menuItem)
        }
    }

    override fun onCreateLoader(ID: Int, args: Bundle?): Loader<Any>? = when (ID) {
        FEEDITEMS_LOADER -> FeedItemDeltaCursorLoader(activity!!,
                URI_FEEDITEMS.buildUpon()
                        .appendQueryParameter(QUERY_PARAM_LIMIT, "50").build(),
                FEED_ITEM_FIELDS_FOR_LIST,
                loaderSelection,
                loaderSelectionArgs,
                COL_PUBDATE + " DESC") as Loader<Any>
        FEED_LOADER -> CursorLoader(activity!!,
                Uri.withAppendedPath(URI_FEEDS, java.lang.Long.toString(this.id)),
                FEED_FIELDS, null, null, null) as Loader<Any>
        FEED_SETTINGS_LOADER -> {
            val where: String?
            val whereArgs: Array<String>?
            when {
                this.id > 0 -> {
                    where = Util.WHEREIDIS
                    whereArgs = Util.LongsToStringArray(this.id)
                }
                feedTag != null -> {
                    where = COL_TAG + " IS ?"
                    whereArgs = Util.ToStringArray(feedTag)
                }
                else -> {
                    where = null
                    whereArgs = null
                }
            }
            CursorLoader(activity!!, URI_FEEDS,
                    Util.ToStringArray("DISTINCT " + COL_NOTIFY),
                    where, whereArgs, null) as Loader<Any>
        }
        else -> null
    }

    override fun onLoadFinished(cursorLoader: Loader<Any?>?, result: Any?) {
        if (cursorLoader != null) {
            when {
                FEEDITEMS_LOADER == cursorLoader.id -> {
                    val map = result as Map<FeedItemSQL, Int>
                    adapter!!.updateData(map)
                    val empty = adapter!!.itemCount <= HEADER_COUNT
                    emptyView!!.visibility = if (empty) View.VISIBLE else View.GONE
                }
                FEED_LOADER == cursorLoader.id -> {
                    val cursor = result as Cursor
                    if (cursor.moveToFirst()) {
                        val (id1, title1, customTitle1, url1, _, notify1, _, displayTitle) = cursor.asFeed()
                        this.title = title1
                        this.customTitle = customTitle1
                        this.url = url1
                        this.notify = if (notify1) 1 else 0

                        (activity as BaseActivity).supportActionBar?.title = displayTitle
                        notifyCheck!!.isChecked = this.notify == 1

                        // Title has been fetched, so add shortcut
                        activity?.addDynamicShortcutToFeed(displayTitle, id1, null)
                        // Report shortcut usage
                        activity?.reportShortcutToFeedUsed(id)
                    }
                    // Reset loader
                    loaderManager.destroyLoader(cursorLoader.id)
                }
                FEED_SETTINGS_LOADER == cursorLoader.id -> {
                    val cursor = result as Cursor
                    if (cursor.count == 1 && cursor.moveToFirst()) {
                        // Conclusive results
                        this.notify = cursor.getInt(0)
                    } else {
                        this.notify = 0
                    }
                    notifyCheck!!.isChecked = this.notify == 1

                    // Reset loader
                    loaderManager.destroyLoader(cursorLoader.id)
                }
            }
        }
    }

    override fun onLoaderReset(cursorLoader: Loader<Any?>?) {
        if (FEEDITEMS_LOADER == cursorLoader?.id) {
            Log.d(TAG, "onLoaderReset FeedItem")
        }
    }

    inner class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {

        // TODO change format possibly
        internal val shortDateTimeFormat = DateTimeFormat.mediumDate().withLocale(Locale.getDefault())

        /**
         * Returns a new instance of this fragment
         */
        fun newInstance(id: Long, title: String?, url: String?,
                        tag: String?): FeedFragment {
            val fragment = FeedFragment()
            fragment.arguments = bundle {
                setLong(ARG_FEED_ID to id)
                setString(ARG_FEED_TITLE to title)
                setString(ARG_FEED_URL to url)
                setString(ARG_FEED_TAG to tag)
            }
            return fragment
        }
    }
}
