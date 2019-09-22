package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.CoroutineScopedKodeinAwareActivity
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.upsertFeed
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.util.feedParser
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import com.nononsenseapps.feeder.views.FloatLabelLayout
import com.nononsenseapps.jsonfeed.Feed
import kotlinx.coroutines.*
import org.kodein.di.generic.instance
import java.net.URL

const val TEMPLATE = "template"


class EditFeedActivity : CoroutineScopedKodeinAwareActivity() {
    private var id: Long = ID_UNSET
    // Views and shit
    private lateinit var textTitle: EditText
    private lateinit var textUrl: EditText
    private lateinit var textTag: AutoCompleteTextView
    private lateinit var textSearch: EditText
    private lateinit var detailsFrame: View
    private lateinit var listResults: androidx.recyclerview.widget.RecyclerView
    private lateinit var resultAdapter: ResultsAdapter
    private lateinit var searchFrame: View
    private var feedUrl: String? = null
    private lateinit var emptyText: TextView
    private lateinit var loadingProgress: View
    private lateinit var urlLabel: FloatLabelLayout
    private lateinit var titleLabel: FloatLabelLayout
    private lateinit var tagLabel: FloatLabelLayout

    private var feedTitle: String = ""

    internal var searchJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private val settingsViewModel: SettingsViewModel by instance(arg = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        if (shouldBeFloatingWindow()) {
            setupFloatingWindow()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_feed)

        // Not persisted so set nightmode every time we start
        AppCompatDelegate.setDefaultNightMode(settingsViewModel.themePreference)

        // Setup views
        textTitle = findViewById(R.id.feed_title)
        titleLabel = textTitle.parent as FloatLabelLayout
        textUrl = findViewById(R.id.feed_url)
        urlLabel = textUrl.parent as FloatLabelLayout
        textTag = findViewById(R.id.feed_tag)
        tagLabel = textTag.parent as FloatLabelLayout
        detailsFrame = findViewById(R.id.feed_details_frame)
        searchFrame = findViewById(R.id.feed_search_frame)
        textSearch = findViewById(R.id.search_view)
        listResults = findViewById(R.id.results_listview)
        emptyText = findViewById(android.R.id.empty)
        loadingProgress = findViewById(R.id.loading_progress)
        resultAdapter = ResultsAdapter()
        //listResults.emptyView = emptyText
        listResults.setHasFixedSize(true)
        listResults.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        listResults.adapter = resultAdapter

        textSearch.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    actionId == EditorInfo.IME_NULL && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // Hide keyboard
                val f = currentFocus
                if (f != null) {
                    val imm = getSystemService(
                            Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(f.windowToken,
                            0)
                }

                try {
                    // Issue search
                    val url: URL = sloppyLinkToStrictURL(textSearch.text.toString().trim())

                    listResults.visibility = View.GONE
                    emptyText.visibility = View.GONE
                    loadingProgress.visibility = View.VISIBLE

                    searchJob = searchForFeeds(url)
                } catch (exc: Exception) {
                    exc.printStackTrace()
                    Toast.makeText(this@EditFeedActivity,
                            R.string.could_not_load_url,
                            Toast.LENGTH_SHORT).show()
                }

                return@OnEditorActionListener true
            }
            false
        })

        val dao: FeedDao by instance()

        val addButton = findViewById<Button>(R.id.add_button)
        addButton.setOnClickListener { _ ->
            // TODO error checking and stuff like that
            val title = textTitle.text.toString().trim()
            val customTitle = if (title == feedTitle) {
                ""
            } else {
                title
            }

            val feed = com.nononsenseapps.feeder.db.room.Feed(
                    id = id,
                    title = feedTitle,
                    customTitle = customTitle,
                    tag = textTag.text.toString().trim(),
                    url = sloppyLinkToStrictURLNoThrows(textUrl.text.toString().trim())
            )

            launch(Dispatchers.Default) {
                val feedId: Long? = dao.upsertFeed(feed)

                feedId?.let {
                    requestFeedSync(kodein, feedId, ignoreConnectivitySettings = false, forceNetwork = true)
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDS, "$feedId"))
                intent.putExtra(ARG_FEED_TITLE, title)
                        .putExtra(ARG_FEED_URL, feed.url.toString())
                        .putExtra(ARG_FEED_TAG, feed.tag)

                withContext(Dispatchers.Main) {
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }

        // Consider start intent
        val i = intent
        if (i != null) {
            // Existing id
            id = i.getLongExtra(ARG_ID, ID_UNSET)
            // Edit like existing, but it's really new
            val template = i.getBooleanExtra(TEMPLATE, false)

            // Existing item
            if (id > ID_UNSET || template) {
                searchFrame.visibility = View.GONE
                detailsFrame.visibility = View.VISIBLE
                if (id > ID_UNSET) {
                    // Don't allow editing url, but allow copying the text
                    //textUrl.setInputType(InputType.TYPE_NULL);
                    //textUrl.setTextIsSelectable(true);
                    // Focus on tag
                    textTag.requestFocus()
                    addButton.text = getString(R.string.save)
                } else {
                    textUrl.requestFocus()
                }
            } else {
                searchFrame.visibility = View.VISIBLE
                detailsFrame.visibility = View.GONE
                // Focus on search
                searchFrame.requestFocus()
            }

            // Link
            feedUrl = (i.dataString ?: i.getStringExtra(Intent.EXTRA_TEXT) ?: "").trim()
            textSearch.setText(feedUrl)
            // URL
            textUrl.setText(feedUrl)

            // Title
            i.getStringExtra(ARG_TITLE)?.let {
                feedTitle = it
            }
            if (i.hasExtra(ARG_CUSTOMTITLE) && !i.getStringExtra(ARG_CUSTOMTITLE).isBlank()) {
                textTitle.setText(i.getStringExtra(ARG_CUSTOMTITLE))
            } else {
                textTitle.setText(feedTitle)
            }

            // Tag
            i.getStringExtra(ARG_FEED_TAG)?.let {
                // Use append instead of setText to make sure cursor is at end
                textTag.append(it)
            }
        }

        // Create an adapter
        launch(Dispatchers.Default) {
            val data = dao.loadTags()

            val tagsAdapter = ArrayAdapter<String>(this@EditFeedActivity,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    data)

            withContext(Dispatchers.Main) {
                // Set the adapter
                textTag.setAdapter(tagsAdapter)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        settingsViewModel.liveThemePreference.observe(this, androidx.lifecycle.Observer {
            delegate.setLocalNightMode(it)
        })
    }

    private fun shouldBeFloatingWindow(): Boolean {
        val theme = theme
        val floatingWindowFlag = TypedValue()
        if (theme == null || !theme.resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag,
                        true)) {
            // isFloatingWindow flag is not defined in theme
            return false
        }
        return floatingWindowFlag.data != 0
    }

    private fun setupFloatingWindow() {
        // configure this Activity as a floating window, dimming the background
        val params = window.attributes
        params.width = resources
                .getDimensionPixelSize(R.dimen.session_details_floating_width)
        params.height = resources
                .getDimensionPixelSize(R.dimen.session_details_floating_height)
        params.alpha = 1f
        params.dimAmount = 0.7f
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = params
    }

    private fun useEntry(title: String, url: String) {
        // Cancel search task so it doesn't keep showing results
        searchJob?.cancel()
        @Suppress("DEPRECATION")
        feedTitle = android.text.Html.fromHtml(title).toString()
        feedUrl = url.trim()
        textUrl.setText(feedUrl)
        textTitle.setText(feedTitle)
        detailsFrame.visibility = View.VISIBLE
        searchFrame.visibility = View.GONE
        // Focus on tag
        textTag.requestFocus()
        val imm = getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(textTag, 0)
    }

    private inner class FeedResult(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view), View.OnClickListener {

        var textTitle: TextView = view.findViewById(R.id.feed_title)
        var textUrl: TextView = view.findViewById(R.id.feed_url)
        var textDescription: TextView = view.findViewById(R.id.feed_description)
        var item: Feed? = null

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (item != null) {
                useEntry(item!!.title ?: "untitled", item!!.feed_url ?: feedUrl ?: "")
            }
        }
    }

    private inner class ResultsAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<FeedResult>() {

        private var items: List<Feed> = emptyList()
        var data: List<Feed>
            get() = items
            set(value) {
                items = value
                notifyDataSetChanged()
            }

        override fun onBindViewHolder(holder: FeedResult, position: Int) {
            val item = items[position]

            holder.item = item
            holder.textTitle.text = item.title ?: ""
            holder.textDescription.text = item.description ?: ""
            holder.textUrl.text = item.feed_url ?: ""
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedResult =
                FeedResult(LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_feed_result, parent, false))

        override fun getItemCount(): Int = items.size

    }

    private fun searchForFeeds(url: URL): Job = launch(Dispatchers.Default) {
        withContext(Dispatchers.Main) {
            resultAdapter.data = emptyList()
        }
        val results = mutableListOf<Feed>()
        val possibleFeeds = feedParser.getAlternateFeedLinksAtUrl(url).map {
            sloppyLinkToStrictURL(it.first)
        } + url
        possibleFeeds.map {
            launch {
                try {
                    feedParser.parseFeedUrl(it)?.let { feed ->
                        withContext(Dispatchers.Main) {
                            results.add(feed)
                            resultAdapter.data = results
                            // Show results, unless user has clicked on one
                            if (detailsFrame.visibility == View.GONE) {
                                searchFrame.visibility = View.VISIBLE
                                listResults.visibility = View.VISIBLE
                            }
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }.toList().joinAll()

        withContext(Dispatchers.Main) {
            loadingProgress.visibility = View.GONE
            if (resultAdapter.data.isEmpty()) {
                emptyText.text = getString(R.string.no_such_feed)
                emptyText.visibility = View.VISIBLE
            }
        }
    }
}
