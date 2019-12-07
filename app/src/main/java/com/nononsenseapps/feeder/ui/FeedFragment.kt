package com.nononsenseapps.feeder.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.KodeinAwareFragment
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.di.CURRENTLY_SYNCING_STATE
import com.nononsenseapps.feeder.model.*
import com.nononsenseapps.feeder.model.opml.exportOpml
import com.nononsenseapps.feeder.model.opml.importOpml
import com.nononsenseapps.feeder.ui.filepicker.MyFilePickerActivity
import com.nononsenseapps.feeder.util.*
import com.nononsenseapps.filepicker.AbstractFilePickerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance
import java.io.File

const val ARG_FEED_ID = "feed_id"
const val ARG_FEED_TITLE = "feed_title"
const val ARG_FEED_URL = "feed_url"
const val ARG_FEED_TAG = "feed_tag"

@FlowPreview
@ExperimentalCoroutinesApi
class FeedFragment : KodeinAwareFragment() {
    internal lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var id: Long = ID_UNSET
    private var title: String? = ""
    private var url: String? = ""
    private var feedTag: String? = ""
    private var firstFeedLoad: Boolean = true
    private var displayTitle = ""
    private var customTitle = ""
    private var notify = false

    private val feedViewModel: FeedViewModel by instance(arg = this)
    private val feedItemsViewModel: FeedItemsViewModel by instance(arg = this)

    private val feedDao: FeedDao by instance()
    private val feedItemDao: FeedItemDao by instance()
    private val prefs: Prefs by instance()

    private val ephemeralState: EphemeralState by instance()

    private lateinit var liveDbPreviews: LiveData<PagedList<PreviewItem>>

    private val currentlySyncing: ConflatedBroadcastChannel<Boolean> by instance(tag = CURRENTLY_SYNCING_STATE)

    init {
        // Listens on sync state changes
        lifecycleScope.launchWhenResumed {
            currentlySyncing.asFlow().collect {
                onSyncStateChanged(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arguments ->
            id = arguments.getLong(ARG_FEED_ID, ID_UNSET)
            title = arguments.getString(ARG_FEED_TITLE)
            url = arguments.getString(ARG_FEED_URL)
            feedTag = arguments.getString(ARG_FEED_TAG)

            arguments.getLongArray(EXTRA_FEEDITEMS_TO_MARK_AS_NOTIFIED)?.let {
                lifecycleScope.launch {
                    feedItemDao.markAsNotified(it.toList())
                }
            }
        }

        if (id == ID_UNSET && feedTag?.isNotEmpty() != true) {
            if (id == ID_UNSET) {
                id = prefs.lastOpenFeedId
            }
            if (feedTag?.isNotEmpty() != true) {
                feedTag = prefs.lastOpenFeedTag
            }
        }

        when {
            id == ID_UNSET && feedTag?.isNotEmpty() == true -> {
                title = feedTag
            }
            id == ID_UNSET -> {
                id = ID_ALL_FEEDS
                title = getString(R.string.all_feeds)
            }
            id == ID_ALL_FEEDS -> {
                title = getString(R.string.all_feeds)
            }
        }

        // Set this to equal title in case it's not a feed
        displayTitle = title ?: ""

        setHasOptionsMenu(true)

        // Remember choice in future
        ephemeralState.lastOpenFeedId = id
        ephemeralState.lastOpenFeedTag = feedTag ?: ""
        lifecycleScope.launch {
            prefs.setLastOpenFeed(id, feedTag)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_feed, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(android.R.id.list)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                ephemeralState.firstVisibleListItem = recyclerView.firstVisibleItemPosition
            }
        })

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        recyclerView.layoutManager = if (TabletUtils.isTablet(activity)) {
            val cols = TabletUtils.numberOfFeedColumns(activity)

            // TODO, use better dividers such as simple padding
            // I want some dividers
            recyclerView.addItemDecoration(DividerColor(activity, DividerColor.VERTICAL_LIST, 0, cols))
            // I want some dividers
            recyclerView.addItemDecoration(DividerColor(activity, DividerColor.HORIZONTAL_LIST))

            // use a grid layout
            GridLayoutManager(activity,
                    cols)
        } else {
            // use a linear layout manager
            LinearLayoutManager(activity)
        }

        // Setup swipe refresh
        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh)

        // The arrow will cycle between these colors (in order)
        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3)

        swipeRefreshLayout.setOnRefreshListener {
            // Sync this specific feed(s) immediately
            requestFeedSync(
                    kodein = kodein,
                    feedId = id,
                    feedTag = feedTag ?: "",
                    ignoreConnectivitySettings = true,
                    forceNetwork = true,
                    parallell = true
            )
        }

        // Set up the empty view
        val emptyView: View = rootView.findViewById(android.R.id.empty)
        val emptyAddFeed: TextView = emptyView.findViewById(R.id.empty_add_feed)
        @Suppress("DEPRECATION")
        emptyAddFeed.text = android.text.Html.fromHtml(getString(R.string.empty_feed_add))
        val emptyOpenFeeds: TextView = emptyView.findViewById(R.id.empty_open_feeds)
        @Suppress("DEPRECATION")
        emptyOpenFeeds.text = android.text.Html.fromHtml(getString(R.string.empty_feed_open))

        emptyAddFeed.setOnClickListener {
            startActivity(Intent(activity,
                    EditFeedActivity::class.java))
        }

        emptyOpenFeeds.setOnClickListener { (activity as FeedActivity).openNavDrawer() }

        // specify an adapter
        val adapter = FeedItemPagedListAdapter(activity!!, object : ActionCallback {
            override fun coroutineScope(): CoroutineScope {
                return lifecycleScope
            }

            override fun onDismiss(item: PreviewItem?) {
                item?.let {
                    lifecycleScope.launch { feedItemsViewModel.toggleReadState(it) }
                }
            }

            override fun onSwipeStarted() {
                // SwipeRefreshLayout does not honor requestDisallowInterceptTouchEvent
                swipeRefreshLayout.isEnabled = false
            }

            override fun onSwipeCancelled() {
                // SwipeRefreshLayout does not honor requestDisallowInterceptTouchEvent
                swipeRefreshLayout.isEnabled = true
            }

            override fun markBelowAsRead(position: Int) {
                recyclerView.adapter?.let { adapter ->
                    lifecycleScope.launch {
                        if (position > NO_POSITION) {
                            val ids = ((position + 1) until adapter.itemCount)
                                    .asSequence()
                                    .map {
                                        adapter.getItemId(it)
                                    }
                                    .toList()
                            feedItemDao.markAsRead(ids)
                        }
                    }
                }
            }
        }).also {
            it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                var firstInsertion = true
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (firstInsertion) {
                        ephemeralState.firstVisibleListItem?.let { pos ->
                            if (ephemeralState.lastOpenFeedId == this@FeedFragment.id &&
                                    ephemeralState.lastOpenFeedTag == this@FeedFragment.feedTag ?: "") {
                                recyclerView.scrollToPosition(pos)
                            }
                        }
                    } else {
                        // If first item is visible, and new items have been added above
                        // then scroll to the top
                        if (positionStart == 0 && recyclerView.firstVisibleItemPosition == 0) {
                            recyclerView.scrollToPosition(0)
                        }
                    }

                    firstInsertion = false
                }
            })
        }
        recyclerView.adapter = adapter

        // Load some RSS
        val onlyUnread = prefs.showOnlyUnread
        feedItemsViewModel.setOnlyUnread(onlyUnread)
        liveDbPreviews = feedItemsViewModel.getLiveDbPreviews(
                feedId = id,
                tag = feedTag ?: ""
        )
        liveDbPreviews.observe(this, Observer<PagedList<PreviewItem>> {
            adapter.submitList(it)
            emptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        })

        when {
            id > ID_UNSET -> { // Load feed if feed
                feedViewModel.getLiveFeed(id).observe(this, Observer {
                    it?.let { feed ->
                        this.title = feed.title
                        this.customTitle = feed.customTitle
                        this.displayTitle = feed.displayTitle
                        this.url = feed.url.toString()
                        this.notify = feed.notify
                        this.feedTag = feed.tag

                        (activity as AppCompatActivity?)?.supportActionBar?.title = displayTitle

                        // Update state of notification toggle
                        activity?.invalidateOptionsMenu()

                        // If user edits the feed then the variables and the UI should reflect it but we shouldn't add
                        // extra statistics on opening the feed.
                        if (firstFeedLoad) {
                            // Title has been fetched, so add shortcut
                            activity?.addDynamicShortcutToFeed(feed.displayTitle, feed.id, null)
                            // Report shortcut usage
                            activity?.reportShortcutToFeedUsed(feed.id)
                        }
                        firstFeedLoad = false
                    }
                })
            }
            id == ID_UNSET -> { // Load notification settings for tag
                (activity as AppCompatActivity?)?.supportActionBar?.title = displayTitle

                activity?.let { activity ->
                    feedTag?.let { feedTag ->
                        feedDao.loadLiveFeedsNotify(tag = feedTag).observe(this, Observer {
                            it.fold(true) { a, b -> a && b }
                                    .let { notify ->
                                        this.notify = notify
                                        // Update state of notification toggle
                                        activity.invalidateOptionsMenu()
                                    }
                        })
                    }
                }
            }
            else -> { // Load notification settings for all
                (activity as AppCompatActivity?)?.supportActionBar?.title = displayTitle

                activity?.let { activity ->
                    feedDao.loadLiveFeedsNotify().observe(this, Observer {
                        it.fold(true) { a, b -> a && b }
                                .let { notify ->
                                    this.notify = notify
                                    // Update state of notification toggle
                                    activity.invalidateOptionsMenu()
                                }
                    })
                }
            }
        }

        return rootView
    }


    private fun onSyncStateChanged(syncing: Boolean) {
        // Background syncs will only disable the animation, never start it
        if (!syncing) {
            if (swipeRefreshLayout.isRefreshing != syncing) {
                swipeRefreshLayout.isRefreshing = syncing
            }
        }
    }

    override fun onActivityCreated(bundle: Bundle?) {
        super.onActivityCreated(bundle)

        // check all button
        (activity as FeedActivity).fabOnClickListener = {
            markAsRead()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.feed, menu)

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (id < 1) {
            menu.findItem(R.id.action_edit_feed)?.isVisible = false
            menu.findItem(R.id.action_delete_feed)?.isVisible = false
        }

        // Set toggleable state
        menu.findItem(R.id.action_only_unread)?.let { menuItem ->
            val onlyUnread = prefs.showOnlyUnread
            menuItem.isChecked = onlyUnread
            menuItem.setTitle(if (onlyUnread) R.string.show_all_items else R.string.show_unread_items)

            menuItem.setIcon(
                    when (onlyUnread) {
                        true -> R.drawable.ic_visibility_off_white_24dp
                        false -> R.drawable.ic_visibility_white_24dp
                    }
            )
        }

        menu.findItem(R.id.action_notify)?.let { menuItem ->
            setNotifyMenuItemState(menuItem)
        }

        super.onPrepareOptionsMenu(menu)
    }

    private fun setNotifyMenuItemState(menuItem: MenuItem) {
        menuItem.isChecked = notify
        if (notify) {
            menuItem.setIcon(R.drawable.ic_notifications_on_white_24dp)
        } else {
            menuItem.setIcon(R.drawable.ic_notifications_off_white_24dp)
        }

        menuItem.setTitle(if (notify) R.string.dont_notify_for_new_items else R.string.notify_for_new_items)
    }

    private fun setNotifications(on: Boolean) {
        val feedId = this.id
        val feedTag = this.feedTag
        val appContext = context?.applicationContext
        if (appContext != null) {
            lifecycleScope.launch {
                // Set as notified so we don't spam
                feedItemsViewModel.markAsNotified(feedId = feedId, tag = feedTag ?: "")
                val dao: FeedDao by instance()
                when {
                    feedId > ID_UNSET -> dao.setNotify(feedId, on)
                    feedId == ID_ALL_FEEDS -> dao.setAllNotify(on)
                    feedTag?.isNotEmpty() == true -> dao.setNotify(feedTag, on)
                }
            }
        }
    }

    /**
     * Mark all items as read in the list
     */
    private fun markAsRead() {
        // Cancel any notifications
        context?.applicationContext?.let { appContext ->
            liveDbPreviews.value?.forEach {
                // Can be null in case of placeholder values
                it?.id?.let { id ->
                    lifecycleScope.launch {
                        cancelNotification(appContext, id)
                    }
                }
            }
        }
        // Then mark as read
        lifecycleScope.launch {
            feedItemsViewModel.markAllAsRead(feedId = id, tag = feedTag ?: "")
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val id = menuItem.itemId
        return when {
            id == R.id.action_sync -> {
                // Sync all feeds when menu button pressed
                requestFeedSync(
                        kodein = kodein,
                        ignoreConnectivitySettings = true,
                        forceNetwork = true,
                        parallell = true
                )
                true
            }
            id == R.id.action_edit_feed && this.id > ID_UNSET -> {
                this.id.let { feedId ->
                    val i = Intent(activity, EditFeedActivity::class.java)
                    // TODO do not animate the back movement here
                    i.putExtra(ARG_ID, feedId)
                    i.putExtra(ARG_CUSTOMTITLE, customTitle)
                    i.putExtra(ARG_TITLE, title)
                    i.putExtra(ARG_FEED_TAG, feedTag)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }

                true
            }
            id == R.id.action_add_templated && this.id > ID_UNSET -> {
                val i = Intent(activity, EditFeedActivity::class.java)
                // TODO do not animate the back movement here
                i.putExtra(TEMPLATE, true)
                i.putExtra(ARG_FEED_TAG, feedTag)
                i.data = Uri.parse(url)
                startActivity(i)
                true
            }
            id == R.id.action_delete_feed && this.id > ID_UNSET -> {
                val feedId = this.id
                val appContext = activity?.applicationContext
                if (appContext != null) {
                    lifecycleScope.launch {
                        feedViewModel.deleteFeedWithId(feedId)

                        // Remove from shortcuts
                        appContext.removeDynamicShortcutToFeed(feedId)
                    }
                }

                // Tell activity to open another fragment
                findNavController().navigate(R.id.action_feedFragment_self, bundle {
                    putLong(ARG_FEED_ID, ID_ALL_FEEDS)
                })
                true
            }
            id == R.id.action_only_unread -> {
                val onlyUnread = !menuItem.isChecked
                prefs.showOnlyUnread = onlyUnread
                menuItem.isChecked = onlyUnread
                if (onlyUnread) {
                    menuItem.setIcon(R.drawable.ic_visibility_off_white_24dp)
                } else {
                    menuItem.setIcon(R.drawable.ic_visibility_white_24dp)
                }

                menuItem.setTitle(if (onlyUnread) R.string.show_unread_items else R.string.show_all_items)

                feedItemsViewModel.setOnlyUnread(onlyUnread)

                true
            }
            id == R.id.action_notify -> {
                notify = !menuItem.isChecked

                setNotifyMenuItemState(menuItem)
                setNotifications(notify)
                true
            }
            id == R.id.action_add -> {
                startActivityForResult(Intent(context, EditFeedActivity::class.java), EDIT_FEED_CODE)
                true
            }
            id == R.id.action_opml_export -> {
                // Choose file, then export
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.type = "text/opml"
                    intent.putExtra(Intent.EXTRA_TITLE, "feeder.opml")
                } else {
                    intent = Intent(context, MyFilePickerActivity::class.java)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_MODE, AbstractFilePickerActivity.MODE_NEW_FILE)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_ALLOW_EXISTING_FILE, true)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_START_PATH,
                            File(Environment.getExternalStorageDirectory(), "feeder.opml").path)
                }
                startActivityForResult(intent, EXPORT_OPML_CODE)
                true
            }
            id == R.id.action_opml_import -> {
                // Choose file
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "*/*"
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,
                            arrayOf("text/plain", "text/xml", "text/opml", "*/*"))
                } else {
                    intent = Intent(context, MyFilePickerActivity::class.java)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_SINGLE_CLICK, true)
                }
                startActivityForResult(intent, IMPORT_OPML_CODE)
                true
            }
            id == R.id.action_settings -> {
                findNavController().navigate(R.id.action_feedFragment_to_settingsFragment)
                true
            }
            id == R.id.action_reportbug -> {
                try {
                    startActivity(openGitlabIssues())
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.no_email_client, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            EXPORT_OPML_CODE -> {
                val uri: Uri? = data?.data
                if (uri != null) {
                    lifecycleScope.launch {
                        exportOpml(kodein, uri)
                    }
                }
            }
            IMPORT_OPML_CODE -> {
                val uri: Uri? = data?.data
                if (uri != null) {
                    lifecycleScope.launch {
                        importOpml(kodein, uri)
                    }
                }
            }
            EDIT_FEED_CODE -> {
                data?.data?.lastPathSegment?.toLong()?.let { id ->
                    findNavController().navigate(R.id.action_feedFragment_self, bundle {
                        putLong(ARG_FEED_ID, id)
                        putString(ARG_FEED_TAG, data.extras?.getString(ARG_FEED_TAG))
                    })
                }
            }
        }
    }
}

val RecyclerView.firstVisibleItemPosition: Int
    get() = with(layoutManager) {
        when (this) {
            is GridLayoutManager -> findFirstVisibleItemPosition()
            is LinearLayoutManager -> findFirstVisibleItemPosition()
            else -> NO_POSITION
        }
    }
