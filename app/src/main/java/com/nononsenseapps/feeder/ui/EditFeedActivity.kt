package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.BackgroundUI
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.upsertFeed
import com.nononsenseapps.feeder.model.FeedParser
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.util.feedParser
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import com.nononsenseapps.feeder.views.FloatLabelLayout
import com.nononsenseapps.jsonfeed.Feed
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.net.URL

const val TEMPLATE = "template"


class EditFeedActivity : AppCompatActivity() {
    private var shouldFinishBack = false
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

    private var searchTask: SearchTask? = null
        set(value) {
            field?.cancel(true)
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (shouldBeFloatingWindow()) {
            setupFloatingWindow()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_feed)

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

                    val inProgressData = mutableListOf<Feed>()
                    searchTask = SearchTask(feedParser,
                            { feed ->
                                inProgressData.add(feed)
                                resultAdapter.data = inProgressData
                                if (inProgressData.isNotEmpty()) {
                                    detailsFrame.visibility = View.GONE
                                    searchFrame.visibility = View.VISIBLE
                                    listResults.visibility = View.VISIBLE
                                }
                            },
                            {
                                loadingProgress.visibility = View.GONE
                                if (resultAdapter.data.isEmpty()) {
                                    emptyText.text = getString(R.string.no_such_feed)
                                    emptyText.visibility = View.VISIBLE
                                }
                            })
                    searchTask?.execute(url)
                } catch (exc: Exception) {
                    Toast.makeText(this@EditFeedActivity,
                            R.string.could_not_load_url,
                            Toast.LENGTH_SHORT).show()
                }

                return@OnEditorActionListener true
            }
            false
        })

        val dao = AppDatabase.getInstance(this).feedDao()

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

            launch(UI) {
                val feedId: Long? = withContext(BackgroundUI) {
                    dao.upsertFeed(feed)
                }

                feedId?.let {
                    launch(BackgroundUI) {
                        requestFeedSync(feedId)
                    }
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDS, "$feedId"))
                intent.putExtra(ARG_FEED_TITLE, title)
                        .putExtra(ARG_FEED_URL, feed.url)
                        .putExtra(ARG_FEED_TAG, feed.tag)

                setResult(RESULT_OK, intent)
                finish()
                if (shouldFinishBack) {
                    // Only care about exit transition
                    overridePendingTransition(R.anim.to_bottom_right,
                            R.anim.to_bottom_right)
                }
            }
        }

        // Consider start intent
        val i = intent
        if (i != null) {
            shouldFinishBack = i.getBooleanExtra(SHOULD_FINISH_BACK, false)
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
        launch(UI) {
            val data = withContext(BackgroundUI) {
                dao.loadTags()
            }

            val tagsAdapter = ArrayAdapter<String>(this@EditFeedActivity,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    data)

            // Set the adapter
            textTag.setAdapter(tagsAdapter)
        }
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

    override fun onBackPressed() {
        super.onBackPressed()
        if (shouldFinishBack) {
            // Only care about exit transition
            overridePendingTransition(0, R.anim.to_bottom_right)
        }
    }

    override fun onDestroy() {
        searchTask?.cancel(true)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean =
            true

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == android.R.id.home && shouldFinishBack) {
            // Was launched from inside app, should just go back
            // Action bar handles other cases.
            finish()
            // Only care about exit transition
            overridePendingTransition(R.anim.to_bottom_right,
                    R.anim.to_bottom_right)
            return true
        }
        return super.onOptionsItemSelected(item)
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

}

class SearchTask(private val feedParser: FeedParser,
                 onProgress: ((Feed) -> Unit)?,
                 onPost: ((Void?) -> Unit)?) : LeakHandlingAsyncTask<URL, Feed, Void>(onProgress, onPost) {

    override fun doInBackground(vararg urls: URL): Void? {
        val url = urls.firstOrNull() ?: return null
        val alts = feedParser.getAlternateFeedLinksAtUrl(url)
        val urlsToParse = when (alts.isNotEmpty()) {
            true -> alts.map { sloppyLinkToStrictURL(it.first) }
            false -> listOf(url)
        }
        if (isCancelled) {
            return null
        }
        urlsToParse.mapNotNull {
            try {
                if (isCancelled) {
                    return null
                }
                Log.d("SearchTask", "Parsing $it")
                val feed = feedParser.parseFeedUrl(it)
                publishProgress(feed)
                feed
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }
        return null
    }
}
