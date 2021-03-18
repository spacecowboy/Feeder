package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.KodeinAwareActivity
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.upsertFeed
import com.nononsenseapps.feeder.model.FeedParser
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import com.nononsenseapps.feeder.views.FloatLabelLayout
import com.nononsenseapps.jsonfeed.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance
import java.net.URL

const val TEMPLATE = "template"

@FlowPreview
@ExperimentalCoroutinesApi
class EditFeedActivity : KodeinAwareActivity() {
    private var id: Long = ID_UNSET

    // Views and shit
    private lateinit var textTitle: EditText
    private lateinit var textUrl: EditText
    private lateinit var textTag: AutoCompleteTextView
    private lateinit var checkboxDefaultFullText: CheckBox
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

    private val feedParser: FeedParser by instance()
    private val feedDao: FeedDao by instance()
    private val prefs: Prefs by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (shouldBeFloatingWindow()) {
            setupFloatingWindow()
        }
        when (prefs.isNightMode) {
            true -> {
                R.style.EditFeedThemeNight
            }
            false -> {
                R.style.EditFeedThemeDay
            }
        }.let {
            setTheme(it)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_feed)

        // Setup views
        textTitle = findViewById(R.id.feed_title)
        titleLabel = textTitle.parent as FloatLabelLayout
        textUrl = findViewById(R.id.feed_url)
        urlLabel = textUrl.parent as FloatLabelLayout
        textTag = findViewById(R.id.feed_tag)
        checkboxDefaultFullText = findViewById(R.id.feed_default_full_text)
        tagLabel = textTag.parent as FloatLabelLayout
        detailsFrame = findViewById(R.id.feed_details_frame)
        searchFrame = findViewById(R.id.feed_search_frame)
        textSearch = findViewById(R.id.search_view)
        listResults = findViewById(R.id.results_listview)
        emptyText = findViewById(android.R.id.empty)
        loadingProgress = findViewById(R.id.loading_progress)
        resultAdapter = ResultsAdapter()
        // listResults.emptyView = emptyText
        listResults.setHasFixedSize(true)
        listResults.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        listResults.adapter = resultAdapter

        textSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_GO ||
                    actionId == EditorInfo.IME_NULL && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    // Hide keyboard
                    val f = currentFocus
                    if (f != null) {
                        val imm = getSystemService(
                            Context.INPUT_METHOD_SERVICE
                        ) as InputMethodManager
                        imm.hideSoftInputFromWindow(
                            f.windowToken,
                            0
                        )
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
                        Toast.makeText(
                            this@EditFeedActivity,
                            R.string.could_not_load_url,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    return@OnEditorActionListener true
                }
                false
            }
        )

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
                fullTextByDefault = checkboxDefaultFullText.isChecked,
                url = sloppyLinkToStrictURLNoThrows(textUrl.text.toString().trim())
            )

            lifecycleScope.launch {
                val feedId: Long? = feedDao.upsertFeed(feed)

                feedId?.let {
                    requestFeedSync(kodein, feedId, ignoreConnectivitySettings = false, forceNetwork = true)
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDS, "$feedId"))
                intent.putExtra(ARG_FEED_TITLE, title)
                    .putExtra(ARG_FEED_URL, feed.url.toString())
                    .putExtra(ARG_FEED_TAG, feed.tag)

                setResult(RESULT_OK, intent)
                finish()
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
                    // textUrl.setInputType(InputType.TYPE_NULL);
                    // textUrl.setTextIsSelectable(true);
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
            if (i.getStringExtra(ARG_CUSTOMTITLE)?.isNotBlank() == true) {
                textTitle.setText(i.getStringExtra(ARG_CUSTOMTITLE))
            } else {
                textTitle.setText(feedTitle)
            }

            // Tag
            i.getStringExtra(ARG_FEED_TAG)?.let {
                // Use append instead of setText to make sure cursor is at end
                textTag.append(it)
            }

            i.getBooleanExtra(ARG_FEED_FULL_TEXT_BY_DEFAULT, false).let {
                checkboxDefaultFullText.isChecked = it
            }
        }

        // Create an adapter
        lifecycleScope.launchWhenCreated {
            val data = feedDao.loadTags()

            val tagsAdapter = ArrayAdapter<String>(
                this@EditFeedActivity,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                data
            )

            // Set the adapter
            textTag.setAdapter(tagsAdapter)
        }
    }

    private fun shouldBeFloatingWindow(): Boolean {
        val theme = theme
        val floatingWindowFlag = TypedValue()
        if (theme == null || !theme.resolveAttribute(
                R.attr.isFloatingWindow, floatingWindowFlag,
                true
            )
        ) {
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
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.showSoftInput(textTag, 0)
    }

    private inner class FeedResult(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

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

    private inner class ResultsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var items: MutableList<Any> = mutableListOf()
        val data: List<Any>
            get() = items

        override fun getItemViewType(position: Int): Int =
            when (items[position]) {
                is Feed -> VIEW_TYPE_FEED_RESULT
                else -> VIEW_TYPE_ERROR_RESULT
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]

            when (holder) {
                is FeedResult -> {
                    when (item) {
                        is Feed -> {
                            holder.item = item
                            holder.textTitle.text = item.title ?: ""
                            holder.textDescription.text = item.description ?: ""
                            holder.textUrl.text = item.feed_url ?: ""
                        }
                    }
                }
                is ErrorResult -> {
                    when (item) {
                        is FeedParser.FeedParsingError -> {
                            holder.textTitle.text = getString(
                                R.string.failed_to_parse,
                                item.url.toString()
                            )
                            holder.textDescription.text = item.message
                        }
                        is Throwable -> {
                            holder.textTitle.text = getString(
                                R.string.failed_to_parse,
                                ""
                            )
                            holder.textDescription.text = item.message
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                VIEW_TYPE_FEED_RESULT -> {
                    FeedResult(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.view_feed_result, parent, false)
                    )
                }
                VIEW_TYPE_ERROR_RESULT -> {
                    ErrorResult(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.view_error_result, parent, false)
                    )
                }
                else -> error("Unknown view type - programmer error")
            }

        override fun getItemCount(): Int = items.size

        fun addFeed(feed: Feed) {
            items.add(feed)
            notifyItemInserted(items.lastIndex)
        }

        fun addError(exception: Throwable) {
            items.add(exception)
            notifyItemInserted(items.lastIndex)
        }

        fun clearData() {
            items.clear()
            notifyDataSetChanged()
        }
    }

    @ExperimentalCoroutinesApi
    private fun searchForFeeds(url: URL): Job = lifecycleScope.launchWhenResumed {
        resultAdapter.clearData()
        val errors: MutableList<Throwable> = mutableListOf()

        flow {
            emit(url)
            feedParser.getAlternateFeedLinksAtUrl(url).forEach {
                emit(sloppyLinkToStrictURL(it.first))
            }
        }
            .mapNotNull {
                try {
                    feedParser.parseFeedUrl(it)
                } catch (t: Throwable) {
                    t
                }
            }
            .flowOn(Dispatchers.Default)
            .onEach {
                when (it) {
                    is Feed -> resultAdapter.addFeed(it)
                    is Throwable -> {
                        val msg = when (it) {
                            is FeedParser.FeedParsingError -> "Error fetching ${it.url}"
                            else -> "Error fetching"
                        }
                        Log.e("FeederFeedSearch", msg, RuntimeException(msg, it))
                        errors.add(it)
                    }
                }
                // Show results, unless user has clicked on one
                if (detailsFrame.visibility == View.GONE && resultAdapter.data.isNotEmpty()) {
                    searchFrame.visibility = View.VISIBLE
                    listResults.visibility = View.VISIBLE
                }
            }
            .onCompletion {
                loadingProgress.visibility = View.GONE
                if (resultAdapter.data.isEmpty()) {
                    if (errors.isEmpty()) {
                        emptyText.text = getString(R.string.no_such_feed)
                        emptyText.visibility = View.VISIBLE
                    } else {
                        // Only show errors in case no feed could be found at all
                        errors.forEach { resultAdapter.addError(it) }
                        searchFrame.visibility = View.VISIBLE
                        listResults.visibility = View.VISIBLE
                    }
                }
            }
            .collect()
    }
}

const val VIEW_TYPE_FEED_RESULT = 1
const val VIEW_TYPE_ERROR_RESULT = 2
