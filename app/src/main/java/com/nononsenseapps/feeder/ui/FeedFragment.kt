package com.nononsenseapps.feeder.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareFragment
import com.nononsenseapps.feeder.db.room.FeedTitle
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.di.CURRENTLY_SYNCING_STATE
import com.nononsenseapps.feeder.model.EphemeralState
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.PreviewItem
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.cancelNotification
import com.nononsenseapps.feeder.model.opml.exportOpml
import com.nononsenseapps.feeder.model.opml.importOpml
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.TabletUtils
import com.nononsenseapps.feeder.util.addDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.bundle
import com.nononsenseapps.feeder.util.openGitlabIssues
import com.nononsenseapps.feeder.util.removeDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.reportShortcutToFeedUsed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.instance
import java.io.File

const val ARG_FEED_ID = "feed_id"
const val ARG_FEED_TITLE = "feed_title"
const val ARG_FEED_URL = "feed_url"
const val ARG_FEED_TAG = "feed_tag"
const val ARG_FEED_FULL_TEXT_BY_DEFAULT = "feed_full_text_by_default"
const val ARG_FEED_OPEN_ARTICLES_WITH = "feed_open_articles_with"

@ExperimentalAnimationApi
@FlowPreview
@ExperimentalCoroutinesApi
class FeedFragment : DIAwareFragment() {
    internal lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var id: Long = ID_UNSET
    private var title: String? = ""
    private var url: String? = ""
    private var feedTag: String? = ""
    private var firstFeedLoad: Boolean = true
    private var displayTitle = ""
    private var customTitle = ""
    private var openArticlesWith = ""
    private var notify = false

    private val feedViewModel: FeedViewModel by instance(arg = this)
    private val feedItemsViewModel: FeedItemsViewModel by instance(arg = this)
    private val settingsViewModel: SettingsViewModel by instance()
    private val prefs: Prefs by instance()

    private val ephemeralState: EphemeralState by instance()

    private lateinit var liveDbPreviews: LiveData<PagedList<PreviewItem>>

    private val currentlySyncing: ConflatedBroadcastChannel<Boolean> by instance(tag = CURRENTLY_SYNCING_STATE)

    private lateinit var adapter: FeedItemPagedListAdapter
    private var emptyView: View? = null

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
                lifecycleScope.launchWhenCreated {
                    feedItemsViewModel.markAsNotified(it.toList())
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            GridLayoutManager(
                activity,
                cols
            )
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
            R.color.refresh_progress_3
        )

        swipeRefreshLayout.setOnRefreshListener {
            // Sync this specific feed(s) immediately
            requestFeedSync(
                di = di,
                feedId = id,
                feedTag = feedTag ?: "",
                ignoreConnectivitySettings = true,
                forceNetwork = true,
                parallell = true
            )
        }

        // Set up the empty view
        emptyView = rootView.findViewById(android.R.id.empty)
        emptyView?.let { emptyView ->
            val emptyAddFeed: TextView = emptyView.findViewById(R.id.empty_add_feed)
            @Suppress("DEPRECATION")
            emptyAddFeed.text = android.text.Html.fromHtml(getString(R.string.empty_feed_add))
            val emptyOpenFeeds: TextView = emptyView.findViewById(R.id.empty_open_feeds)
            @Suppress("DEPRECATION")
            emptyOpenFeeds.text = android.text.Html.fromHtml(getString(R.string.empty_feed_open))

            emptyAddFeed.setOnClickListener {
                startActivity(
                    Intent(
                        activity,
                        EditFeedActivity::class.java
                    )
                )
            }

            emptyOpenFeeds.setOnClickListener { (activity as FeedActivity).openNavDrawer() }
        }

        // specify an adapter
        adapter = FeedItemPagedListAdapter(
            requireActivity(),
            feedItemsViewModel,
            settingsViewModel,
            prefs,
            object : ActionCallback {
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

                override fun markAsRead(position: Int, markBelow: Boolean) {
                    recyclerView.adapter?.let { adapter ->
                        lifecycleScope.launch {
                            if (position > NO_POSITION) {
                                val indexes = if (markBelow) {
                                    ((position + 1) until adapter.itemCount)
                                } else {
                                    (0 until position)
                                }
                                val ids = indexes
                                    .asSequence()
                                    .map {
                                        adapter.getItemId(it)
                                    }
                                    .toList()
                                feedItemsViewModel.markAsRead(ids)
                            }
                        }
                    }
                }
            }
        ).also {
            it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                var firstInsertion = true
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (firstInsertion) {
                        ephemeralState.firstVisibleListItem?.let { pos ->
                            if (ephemeralState.lastOpenFeedId == this@FeedFragment.id &&
                                ephemeralState.lastOpenFeedTag == this@FeedFragment.feedTag ?: ""
                            ) {
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
        val newestFirst = prefs.isNewestFirst
        feedItemsViewModel.setNewestFirst(newestFirst)
        liveDbPreviews = feedItemsViewModel.getLiveDbPreviews(
            feedId = id,
            tag = feedTag ?: ""
        )

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveDbPreviews.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            emptyView?.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }

        when {
            id > ID_UNSET -> { // Load feed if feed
                feedViewModel.getLiveFeed(id).observe(viewLifecycleOwner) { feed ->
                    if (feed == null) {
                        // Can happen on dynamic shortcut and feed was deleted
                        requireContext().removeDynamicShortcutToFeed(id)

                        findNavController().navigate(
                            R.id.action_feedFragment_self,
                            bundle {
                                putLong(ARG_FEED_ID, ID_ALL_FEEDS)
                                putString(ARG_FEED_TAG, "")
                            }
                        )
                    } else {
                        this.title = feed.title
                        this.customTitle = feed.customTitle
                        this.displayTitle = feed.displayTitle
                        this.url = feed.url.toString()
                        this.notify = feed.notify
                        this.feedTag = feed.tag
                        this.openArticlesWith = feed.openArticlesWith

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
                }
            }
            else -> { // Load notification settings for all
                (activity as AppCompatActivity?)?.supportActionBar?.title = displayTitle

                activity?.let { activity ->
                    feedViewModel.getLiveFeedsNotify(id, feedTag ?: "")
                        .observe(viewLifecycleOwner) {
                            it.fold(true) { a, b -> a && b }
                                .let { notify ->
                                    this.notify = notify
                                    // Update state of notification toggle
                                    activity.invalidateOptionsMenu()
                                }
                        }
                }
            }
        }
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
            menu.findItem(R.id.action_share)?.isVisible = false
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
                when {
                    feedId > ID_UNSET -> feedViewModel.setNotify(feedId, on)
                    feedId == ID_UNSET && feedTag?.isNotEmpty() == true -> feedViewModel.setNotify(feedTag, on)
                    else -> feedViewModel.setAllNotify(on)
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
        val itemId = menuItem.itemId
        return when {
            itemId == R.id.action_sync -> {
                // Sync all feeds when menu button pressed
                requestFeedSync(
                    di = di,
                    ignoreConnectivitySettings = true,
                    forceNetwork = true,
                    parallell = true
                )
                true
            }
            itemId == R.id.action_edit_feed && this.id > ID_UNSET -> {
                this.id.let { feedId ->
                    lifecycleScope.launch {
                        val feedFullTextByDefault =
                            feedViewModel.getFeed(feedId)?.fullTextByDefault ?: false

                        val i = Intent(activity, EditFeedActivity::class.java)
                        // TODO do not animate the back movement here
                        i.putExtra(ARG_ID, feedId)
                        i.putExtra(ARG_CUSTOMTITLE, customTitle)
                        i.putExtra(ARG_TITLE, title)
                        i.putExtra(ARG_FEED_TAG, feedTag)
                        i.putExtra(ARG_FEED_FULL_TEXT_BY_DEFAULT, feedFullTextByDefault)
                        i.putExtra(ARG_FEED_OPEN_ARTICLES_WITH, openArticlesWith)
                        i.data = Uri.parse(url)
                        startActivity(i)
                    }
                }

                true
            }
            itemId == R.id.action_add_templated && this.id > ID_UNSET -> {
                val i = Intent(activity, EditFeedActivity::class.java)
                // TODO do not animate the back movement here
                i.putExtra(TEMPLATE, true)
                i.putExtra(ARG_FEED_TAG, feedTag)
                i.data = Uri.parse(url)
                startActivity(i)
                true
            }
            itemId == R.id.action_delete_feed -> {
                lifecycleScope.launch {
                    val feeds: List<FeedTitle> = feedViewModel.getVisibleFeeds(id, feedTag)

                    findNavController().navigate(
                        R.id.action_feedFragment_to_deleteFeedsDialogFragment,
                        bundleOf(
                            ARG_FEED_IDS to feeds.map { it.id }.toLongArray(),
                            ARG_FEED_TITLES to feeds.map { it.displayTitle }.toTypedArray()
                        )
                    )
                }
                true
            }
            itemId == R.id.action_only_unread -> {
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
            itemId == R.id.action_notify -> {
                notify = !menuItem.isChecked

                setNotifyMenuItemState(menuItem)
                setNotifications(notify)
                true
            }
            itemId == R.id.action_add -> {
                startActivityForResult(Intent(context, EditFeedActivity::class.java), EDIT_FEED_CODE)
                true
            }
            itemId == R.id.action_opml_export -> {
                // Choose file, then export
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.type = "text/opml"
                intent.putExtra(Intent.EXTRA_TITLE, "feeder.opml")
                startActivityForResult(intent, EXPORT_OPML_CODE)
                true
            }
            itemId == R.id.action_opml_import -> {
                // Choose file
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                intent.putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf("text/plain", "text/xml", "text/opml", "*/*")
                )
                startActivityForResult(intent, IMPORT_OPML_CODE)
                true
            }
            itemId == R.id.action_settings -> {
                findNavController().navigate(R.id.action_feedFragment_to_settingsFragment)
                true
            }
            itemId == R.id.action_reportbug -> {
                try {
                    startActivity(openGitlabIssues())
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.no_email_client, Toast.LENGTH_SHORT).show()
                }
                true
            }
            itemId == R.id.action_share -> {
                url?.let { url ->
                    startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).also { intent ->
                                intent.type = "text/plain"
                                intent.putExtra(Intent.EXTRA_TEXT, url)
                            },
                            getString(R.string.share)
                        )
                    )
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
                        exportOpml(di, uri)
                    }
                }
            }
            IMPORT_OPML_CODE -> {
                val uri: Uri? = data?.data
                if (uri != null) {
                    lifecycleScope.launch {
                        importOpml(di, uri)
                    }
                }
            }
            EDIT_FEED_CODE -> {
                data?.data?.lastPathSegment?.toLong()?.let { id ->
                    findNavController().navigate(
                        R.id.action_feedFragment_self,
                        bundle {
                            putLong(ARG_FEED_ID, id)
                            putString(ARG_FEED_TAG, data.extras?.getString(ARG_FEED_TAG))
                        }
                    )
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
